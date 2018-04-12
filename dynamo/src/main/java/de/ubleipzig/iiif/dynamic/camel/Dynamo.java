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

package de.ubleipzig.iiif.dynamic.camel;

import static de.ubleipzig.iiif.dynamic.FromRdf.toJsonLd;
import static de.ubleipzig.iiif.dynamic.QueryUtils.getQuery;
import static de.ubleipzig.iiif.dynamic.webanno.AbstractSerializer.serialize;
import static java.net.URLEncoder.encode;
import static org.apache.camel.Exchange.CONTENT_TYPE;
import static org.apache.camel.Exchange.HTTP_CHARACTER_ENCODING;
import static org.apache.camel.Exchange.HTTP_METHOD;
import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;
import static org.apache.camel.LoggingLevel.INFO;

import com.github.jsonldjava.core.JsonLdError;

import de.ubleipzig.iiif.dynamic.webanno.CollectionBuilder;
import de.ubleipzig.iiif.dynamic.webanno.ManifestBuilder;
import de.ubleipzig.iiif.dynamic.webanno.templates.AnnotationList;

import java.io.IOException;
import java.io.UncheckedIOException;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.main.Main;
import org.apache.camel.main.MainListenerSupport;
import org.apache.camel.main.MainSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Dynamo.
 *
 * @author christopher-johnson
 */
public class Dynamo {

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

        private static final Logger LOGGER = LoggerFactory.getLogger(Dynamo.class);

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

            final PropertiesComponent pc = getContext().getComponent("properties", PropertiesComponent.class);
            pc.setLocation("classpath:application.properties");

            from("jetty:http://{{rest.host}}:{{rest.port}}{{rest.prefix}}?"
                    + "optionsEnabled=true&matchOnUriPrefix=true&sendServerVersion=false"
                    + "&httpMethodRestrict=GET,OPTIONS")
                    .routeId("Sparqler")
                    .removeHeaders(HTTP_ACCEPT)
                    .setHeader("Access-Control-Allow-Origin")
                    .constant("*")
                    .choice()
                    .when(header(HTTP_METHOD).isEqualTo("GET"))
                    .to("direct:sparql");
            from("direct:sparql")
                    .routeId("SparqlerGet")
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
                                           getQuery("canvas-anno.sparql", getV1(e), getV2(e)))))
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
                                   .setBody(sparqlConstruct(getQuery("collection.sparql"))))
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
                    });
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
                 .setBody(toJsonLd(e.getIn()
                                    .getBody()
                                    .toString(), contextUri, frameUri));
            } else {
                e.getIn().setHeader(CONTENT_TYPE, EMPTY);
            }
        }
    }
}
