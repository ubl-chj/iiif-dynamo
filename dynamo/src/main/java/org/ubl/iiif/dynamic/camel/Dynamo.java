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
import java.util.Objects;

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
import org.ubl.iiif.dynamic.webanno.ManifestBuilder;


public class Dynamo {

    /**
     *
     * @param args String[]
     * @throws Exception Exception
     */
    public static void main(final String[] args) throws Exception {
        final Dynamo selector = new Dynamo();
        selector.init();
    }

    private static String sparqlConstruct(final String command) {
        try {
            return "query=" + encode(command, "UTF-8");
        } catch (final IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private void init() throws Exception {
        final Main main = new Main();
        main.addRouteBuilder(new QueryRoute());
        main.addMainListener(new Events());
        main.run();
    }

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

    public static class QueryRoute extends RouteBuilder {

        private static final Logger LOGGER = LoggerFactory.getLogger(QueryRoute.class);

        private static final String HTTP_ACCEPT = "Accept";
        private static final String SPARQL_QUERY = "type";
        private static final String V1_SET = "v1";
        private static final String V2_SET = "v2";

        private static String getV1(final Exchange e) {
            final Object optHdr = e.getIn()
                                   .getHeader(V1_SET);
            return (String) optHdr;
        }

        private static String getV2(final Exchange e) {
            final Object optHdr = e.getIn()
                                   .getHeader(V2_SET);
            return (String) optHdr;
        }

        /**
         *
         */
        public void configure() {

            final PropertiesComponent pc =
                    getContext().getComponent("properties", PropertiesComponent.class);
            pc.setLocation("classpath:application.properties");

            from("jetty:http://{{rest.host}}:{{rest.port}}{{rest.prefix}}?"
                    + "optionsEnabled=true&matchOnUriPrefix=true&sendServerVersion=false"
                    + "&httpMethodRestrict=GET,OPTIONS").routeId("Sparqler")
                                                        .removeHeaders(HTTP_ACCEPT)
                                                        .choice()
                                                        .when(header(HTTP_METHOD).isEqualTo("GET"))
                                                        .to("direct:sparql");
            from("direct:sparql").routeId("SparqlerGet")
                                 .choice()
                                 .when(header(SPARQL_QUERY).isEqualTo("meta"))
                                 .setHeader(HTTP_METHOD)
                                 .constant("POST")
                                 .setHeader(CONTENT_TYPE)
                                 .constant("application/x-www-form-urlencoded; " + "charset=utf-8")
                                 .setHeader(HTTP_ACCEPT)
                                 .constant("application/n-triples")
                                 .process(e -> e.getIn()
                                                .setBody(sparqlConstruct(
                                                        getQuery("canvas-anno.rq", getV1(e), getV2(e)))))
                                 .log(LoggingLevel.INFO, LOGGER, String.valueOf(body()))
                                 .to("http4:{{triplestore.baseUrl}}?useSystemProperties=true&bridgeEndpoint=true")
                                 .filter(header(HTTP_RESPONSE_CODE).isEqualTo(200))
                                 .setHeader(CONTENT_TYPE)
                                 .constant("application/n-triples")
                                 .convertBodyTo(String.class)
                                 .log(LoggingLevel.INFO, LOGGER, "Getting query results as n-triples")
                                 .to("direct:toJsonLd");
            from("direct:toJsonLd").routeId("JsonLd")
                                   .log(LoggingLevel.INFO, LOGGER, "Serializing n-triples as Json-Ld")
                                   .process(e -> {
                                       try {
                                           final String contentType = e.getIn()
                                                                       .getHeader(SPARQL_QUERY, String.class);
                                           if (Objects.equals(contentType, "meta")) {
                                               final String contextUri = "context.json";
                                               final String frameUri = "anno-frame.json";
                                               e.getIn()
                                                .setBody(toJsonLd(e.getIn()
                                                                   .getBody()
                                                                   .toString(), contextUri, frameUri));
                                           }
                                       } catch (final Exception ex) {
                                           throw new RuntimeCamelException("Couldn't serialize to JsonLd", ex);
                                       }
                                   })
                                   .removeHeader(HTTP_ACCEPT)
                                   .setHeader(HTTP_METHOD)
                                   .constant("GET")
                                   .setHeader(HTTP_CHARACTER_ENCODING)
                                   .constant("UTF-8")
                                   .setHeader(CONTENT_TYPE)
                                   .constant("application/ld+json")
                                   .to("direct:buildManifest");
            from("direct:buildManifest").routeId("ManifestBuilder")
                                        .log(LoggingLevel.INFO, LOGGER, "Building Manifest")
                                        .process(e -> {
                                            final ManifestBuilder builder = new ManifestBuilder(e.getIn()
                                                                                           .getBody()
                                                                                           .toString());
                                            e.getIn()
                                             .setBody(builder.build());
                                        });
        }
    }
}
