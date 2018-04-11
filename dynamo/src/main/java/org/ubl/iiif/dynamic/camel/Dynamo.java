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

package org.ubl.iiif.dynamic.camel;

import static java.net.URLEncoder.encode;
import static org.apache.camel.Exchange.CONTENT_TYPE;
import static org.apache.camel.Exchange.HTTP_CHARACTER_ENCODING;
import static org.apache.camel.Exchange.HTTP_METHOD;
import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;
import static org.ubl.iiif.dynamic.FromRdf.toJsonLd;
import static org.ubl.iiif.dynamic.QueryUtils.getQuery;

import java.io.IOException;
import java.io.UncheckedIOException;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.main.Main;
import org.apache.camel.main.MainListenerSupport;
import org.apache.camel.main.MainSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ubl.iiif.dynamic.webanno.CollectionBuilder;
import org.ubl.iiif.dynamic.webanno.ManifestBuilder;

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
        private static final String HTTP_ACCEPT = "Accept";
        private static final String SPARQL_QUERY = "type";
        private static final String V1_SET = "v1";
        private static final String V2_SET = "v2";

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
                    .log(LoggingLevel.INFO, LOGGER, "Getting query results as n-triples")
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
                    .log(LoggingLevel.INFO, LOGGER, "Getting query results as n-triples")
                    .to("direct:toJsonLd");
            from("direct:toJsonLd")
                    .routeId("JsonLd")
                    .log(LoggingLevel.INFO, LOGGER, "Serializing n-triples as Json-Ld")
                    .choice()
                        .when(header(SPARQL_QUERY).isEqualTo("meta"))
                            .to("direct:serializeMeta")
                        .when(header(SPARQL_QUERY).isEqualTo("collection"))
                            .to("direct:serializeCollection");
            from("direct:serializeMeta")
                    .process(e -> {
                        try {
                            final String contextUri = "context.json";
                            final String frameUri = "anno-frame.json";
                            e.getIn()
                             .setBody(toJsonLd(e.getIn()
                                                .getBody()
                                                .toString(), contextUri, frameUri));
                        } catch (final Exception ex) {
                            throw new RuntimeCamelException("Empty SPARQL Result Set", ex);
                        }
                    })
                    .removeHeader(HTTP_ACCEPT)
                    .setHeader(HTTP_CHARACTER_ENCODING)
                    .constant("UTF-8")
                    .setHeader(CONTENT_TYPE)
                    .constant(contentTypeJsonLd)
                    .to("direct:buildManifest");
            from("direct:serializeCollection")
                    .process(e -> {
                        try {
                            final String contextUri = "context.json";
                            final String frameUri = "collection-frame.json";
                            e.getIn()
                             .setBody(toJsonLd(e.getIn()
                                                .getBody()
                                                .toString(), contextUri, frameUri));
                        } catch (final Exception ex) {
                            throw new RuntimeCamelException("Couldn't serialize to JsonLd", ex);
                        }
                    })
                    .removeHeader(HTTP_ACCEPT)
                    .setHeader(HTTP_CHARACTER_ENCODING)
                    .constant("UTF-8")
                    .setHeader(CONTENT_TYPE)
                    .constant(contentTypeJsonLd)
                    .to("direct:buildCollection");
            from("direct:buildManifest")
                    .routeId("ManifestBuilder")
                    .log(LoggingLevel.INFO, LOGGER, "Building Manifest")
                    .process(e -> {
                        final ManifestBuilder builder = new ManifestBuilder(e.getIn()
                                                                             .getBody()
                                                                             .toString());
                        e.getIn().setBody(builder.build());
                    });
            from("direct:buildCollection")
                    .routeId("CollectionBuilder")
                    .log(LoggingLevel.INFO, LOGGER, "Building Collection")
                    .process(e -> {
                        final CollectionBuilder builder = new CollectionBuilder(e.getIn()
                                                                                 .getBody()
                                                                                 .toString());
                        e.getIn().setBody(builder.build());
                    });
        }
    }
}
