/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.ubleipzig.dynamo.camel;

import static de.ubleipzig.webanno.AbstractSerializer.serialize;
import static java.net.URLEncoder.encode;
import static org.apache.camel.Exchange.CONTENT_TYPE;
import static org.apache.camel.Exchange.HTTP_CHARACTER_ENCODING;
import static org.apache.camel.Exchange.HTTP_METHOD;
import static org.apache.camel.Exchange.HTTP_QUERY;
import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;
import static org.apache.camel.LoggingLevel.INFO;

import com.github.jsonldjava.core.JsonLdError;

import de.ubleipzig.dynamo.JsonLdProcessorUtils;
import de.ubleipzig.dynamo.QueryUtils;
import de.ubleipzig.webanno.CollectionBuilder;
import de.ubleipzig.webanno.ManifestBuilder;
import de.ubleipzig.webanno.templates.AnnotationList;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Hashtable;
import java.util.Objects;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.main.Main;
import org.apache.camel.main.MainListenerSupport;
import org.apache.camel.main.MainSupport;
import org.apache.camel.util.IOHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;


/**
 * Dynamo.
 *
 * @author christopher-johnson
 */
public class Dynamo {

    private static final Logger LOGGER = LoggerFactory.getLogger(Dynamo.class);
    private static final JedisConnectionFactory CONNECTION_FACTORY = new JedisConnectionFactory();
    private static RedisTemplate<String, String> redisTemplate;

    static {
        CONNECTION_FACTORY.setHostName("redis");
        CONNECTION_FACTORY.setPort(6379);
        CONNECTION_FACTORY.afterPropertiesSet();
    }

    /**
     * @param args String[]
     * @throws Exception Exception
     */
    public static void main(final String[] args) throws Exception {
        final Dynamo selector = new Dynamo();
        selector.init();
    }

    /**
     * @throws Exception Exception
     */
    private void init() throws Exception {
        final Main main = new Main();
        main.addRouteBuilder(new QueryRoute());
        main.addMainListener(new Events());
        final JndiRegistry registry = new JndiRegistry(createInitialContext());
        redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(CONNECTION_FACTORY);
        redisTemplate.setDefaultSerializer(new StringRedisSerializer());
        redisTemplate.afterPropertiesSet();
        Objects.requireNonNull(registry).bind("redisTemplate", redisTemplate);
        main.setPropertyPlaceholderLocations("file:${env:DYNAMO_HOME}/de.ubleipzig.dynamo.cfg");
        main.run();
    }

    /**
     * Events.
     */
    public static class Events extends MainListenerSupport {

        @Override
        public void afterStart(final MainSupport main) {
            System.out.println("Dynamo is now started!");
        }

        @Override
        public void beforeStop(final MainSupport main) {
            System.out.println("Dynamo is now being stopped!");
        }
    }

    /**
     * QueryRoute.
     */
    public static class QueryRoute extends RouteBuilder {

        private static final String contentTypeNTriples = "application/n-triples";
        private static final String contentTypeJsonLd = "application/ld+json";
        private static final String contentTypeHTMLForm = "application/x-www-form-urlencoded";
        private static final String EMPTY = "empty";
        private static final String HTTP_ACCEPT = "Accept";
        private static final String SPARQL_QUERY = "type";
        private static final String V1_SET = "v1";
        private static final String V2_SET = "v2";

        /**
         * configure.
         */
        public void configure() {

            from("jetty:http://{{dynamo.host}}:{{dynamo.port}}{{dynamo.prefix}}?"
                    + "optionsEnabled=true&matchOnUriPrefix=true&sendServerVersion=false"
                    + "&httpMethodRestrict=GET,OPTIONS")
                    .routeId("Sparqler")
                    .removeHeaders(HTTP_ACCEPT)
                    .setHeader("Access-Control-Allow-Origin")
                    .constant("*")
                    .choice()
                    .when(header(HTTP_METHOD).isEqualTo("GET"))
                    .to("direct:redis-get");
            from("direct:redis-get")
                    .routeId("RedisGet")
                    .process(e -> {
                        final String httpquery = e.getIn().getHeader(HTTP_QUERY).toString();
                        if (redisTemplate.opsForValue().get(httpquery) != null) {
                            e.getIn().setBody(redisTemplate.opsForValue().get(httpquery));
                            LOGGER.info("Getting query result from Redis Cache");
                        } else {
                            e.getIn().setHeader(CONTENT_TYPE, EMPTY);
                        }
                    })
                    .choice()
                    .when(header(CONTENT_TYPE).isEqualTo(EMPTY))
                    .to("direct:sparql");
            from("direct:sparql")
                    .routeId("SparqlerGet")
                    .removeHeader(CONTENT_TYPE)
                    .choice()
                        .when(header(SPARQL_QUERY).isEqualTo("meta"))
                            .to("direct:getMetaGraph")
                        .when(header(SPARQL_QUERY).isEqualTo("collection"))
                            .to("direct:getCollectionGraph");
            from("direct:getMetaGraph")
                    .setHeader(HTTP_METHOD)
                    .constant("POST")
                    .setHeader(CONTENT_TYPE)
                    .constant( contentTypeHTMLForm + "; charset=utf-8")
                    .setHeader(HTTP_ACCEPT)
                    .constant(contentTypeNTriples)
                    .process(e -> e.getIn()
                                   .setBody(sparqlConstruct(
                                           QueryUtils.getQuery("canvas-anno.sparql", getV1(e), getV2(e)))))
                    .to("http4:{{triplestore.baseUrl}}?useSystemProperties=true&bridgeEndpoint=true")
                    .filter(header(HTTP_RESPONSE_CODE).isEqualTo(200))
                    .setHeader(CONTENT_TYPE)
                    .constant(contentTypeNTriples)
                    .convertBodyTo(String.class)
                    .log(INFO, LOGGER, "Getting query results as n-triples")
                    .to("direct:toJsonLd");
            from("direct:getCollectionGraph")
                    .setHeader(HTTP_METHOD)
                    .constant("POST")
                    .setHeader(CONTENT_TYPE)
                    .constant("application/x-www-form-urlencoded; charset=utf-8")
                    .setHeader(HTTP_ACCEPT)
                    .constant(contentTypeNTriples)
                    .process(e -> e.getIn()
                                   .setBody(sparqlConstruct(QueryUtils.getQuery("collection.sparql"))))
                    .to("http4:{{triplestore.baseUrl}}?useSystemProperties=true&bridgeEndpoint=true")
                    .filter(header(HTTP_RESPONSE_CODE).isEqualTo(200))
                    .setHeader(CONTENT_TYPE)
                    .constant(contentTypeNTriples)
                    .convertBodyTo(String.class)
                    .log(INFO, LOGGER, "Getting query results as n-triples")
                    .to("direct:toJsonLd");
            from("direct:toJsonLd")
                    .routeId("JsonLd")
                    .log(INFO, LOGGER, "Serializing n-triples as Json-Ld")
                    .choice()
                        .when(header(SPARQL_QUERY).isEqualTo("meta"))
                            .to("direct:serializeMeta")
                        .when(header(SPARQL_QUERY).isEqualTo("collection"))
                            .to("direct:serializeCollection");
            from("direct:serializeMeta")
                    .process(e -> {
                        processJsonLdExchange(e, "context.json", "anno-frame.json");
                    })
                    .removeHeader(HTTP_ACCEPT)
                    .choice()
                    .when(header(CONTENT_TYPE).isEqualTo(EMPTY))
                    .to("direct:buildEmpty")
                    .otherwise()
                    .to("direct:buildManifest");
            from("direct:serializeCollection")
                    .process(e -> {
                        processJsonLdExchange(e, "context.json", "collection-frame.json");
                     })
                    .removeHeader(HTTP_ACCEPT)
                    .choice()
                    .when(header(CONTENT_TYPE).isEqualTo(EMPTY))
                    .to("direct:buildEmpty")
                    .otherwise()
                    .to("direct:buildCollection");
            from("direct:buildEmpty")
                    .routeId("EmptyBuilder")
                    .log(INFO, LOGGER, "Serializing Empty Document")
                    .process(e -> {
                        final AnnotationList emptyList = new AnnotationList();
                        e.getIn().setBody(serialize(emptyList).orElse(""));
                    });
            from("direct:buildManifest")
                    .routeId("ManifestBuilder")
                    .setHeader(HTTP_CHARACTER_ENCODING)
                    .constant("UTF-8")
                    .setHeader(CONTENT_TYPE)
                    .constant(contentTypeJsonLd)
                    .log(INFO, LOGGER, "Building Manifest")
                    .process(e -> {
                        final ManifestBuilder builder = new ManifestBuilder(e.getIn()
                                                                             .getBody()
                                                                             .toString());
                        e.getIn().setBody(builder.build());
                    })
                    .to("direct:redis-put");
            from("direct:buildCollection")
                    .routeId("CollectionBuilder")
                    .removeHeader(HTTP_ACCEPT)
                    .setHeader(HTTP_CHARACTER_ENCODING)
                    .constant("UTF-8")
                    .setHeader(CONTENT_TYPE)
                    .constant(contentTypeJsonLd)
                    .log(INFO, LOGGER, "Building Collection")
                    .process(e -> {
                        final CollectionBuilder builder = new CollectionBuilder(e.getIn()
                                                                                 .getBody()
                                                                                 .toString());
                        e.getIn().setBody(builder.build());
                    })
                    .to("direct:redis-put");
            from("direct:redis-put")
                    .routeId("RedisPut")
                    .log(INFO, LOGGER, "Storing query result in Redis Cache")
                    .process(e -> {
                        final String httpquery = e.getIn().getHeader(HTTP_QUERY).toString();
                        final String body = e.getIn().getBody().toString();
                        if (null == redisTemplate.opsForValue().get(httpquery)) {
                            redisTemplate.opsForValue().set(httpquery, body);
                        }
                        e.getIn().setBody(redisTemplate.opsForValue().get(httpquery));
                    });
        }

        /**
         * @param command String
         * @return String
         */
        private static String sparqlConstruct(final String command) {
            try {
                return "query=" + encode(command, "UTF-8");
            } catch (final IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }

        /**
         * @param e Exchange
         * @return String
         */
        private static String getV1(final Exchange e) {
            final Object optHdr = e.getIn()
                                   .getHeader(V1_SET);
            return (String) optHdr;
        }

        /**
         * @param e Exchange
         * @return String
         */
        private static String getV2(final Exchange e) {
            final Object optHdr = e.getIn()
                                   .getHeader(V2_SET);
            return (String) optHdr;
        }

        private void processJsonLdExchange(final Exchange e, final String contextUri, final String frameUri)
                throws IOException, JsonLdError {
            final String body = e.getIn().getBody().toString();
            if ( body != null && !body.isEmpty()) {
                e.getIn()
                 .setBody(JsonLdProcessorUtils.toJsonLd(e.getIn()
                                                         .getBody()
                                                         .toString(), contextUri, frameUri));
            } else {
                e.getIn().setHeader(CONTENT_TYPE, EMPTY);
            }
        }
    }


    /**
     *
     * @return InitialContext Context
     * @throws Exception Exception
     */
    private static Context createInitialContext() throws Exception {
        final InputStream in = Dynamo.class
                .getClassLoader()
                .getResourceAsStream("jndi.properties");
        try {
            final Properties properties = new Properties();
            properties.load(in);
            return new InitialContext(new Hashtable<>(properties));
        } finally {
            IOHelper.close(in);
        }
    }
}
