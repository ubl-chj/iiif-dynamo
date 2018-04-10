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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;

import org.apache.camel.CamelContext;
import org.apache.camel.LoggingLevel;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ubl.iiif.dynamic.FromRdf;
import org.ubl.iiif.dynamic.QueryUtils;
import org.ubl.iiif.dynamic.webanno.CollectionBuilder;

public final class QueryCollectionRouteTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueryCollectionRouteTest.class);

    private static final String HTTP_ACCEPT = "Accept";
    private static final String SPARQL_QUERY = "type";

    private QueryCollectionRouteTest() {
    }

    public static void main(final String[] args) throws Exception {
        LOGGER.info("About to run SPARQL API...");

        final CamelContext camelContext = new DefaultCamelContext();
        camelContext.addRoutes(new RouteBuilder() {
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
                        .when(header(SPARQL_QUERY).isEqualTo("collection"))
                        .setHeader(HTTP_METHOD)
                        .constant("POST")
                        .setHeader(CONTENT_TYPE)
                        .constant("application/x-www-form-urlencoded; " + "charset=utf-8")
                        .setHeader(HTTP_ACCEPT)
                        .constant("application/n-triples")
                        .process(e -> e.getIn()
                                       .setBody(sparqlSelect(
                                               QueryUtils.getQuery("collection.rq"))))
                        .log(LoggingLevel.INFO, LOGGER, String.valueOf(body()))
                        .to("http4:{{triplestore.baseUrl}}?useSystemProperties=true&bridgeEndpoint=true")
                        .filter(header(HTTP_RESPONSE_CODE).isEqualTo(200))
                        .setHeader(CONTENT_TYPE)
                        .constant("application/n-triples")
                        .convertBodyTo(String.class)
                        .log(LoggingLevel.INFO, LOGGER, "Getting query results as n-triples")
                        .to("direct:toJsonLd");
                from("direct:toJsonLd")
                        .routeId("JsonLd")
                        .log(LoggingLevel.INFO, LOGGER, "Serializing n-triples as Json-Ld")
                        .process(e -> {
                            try {
                                final String contentType = e.getIn()
                                                            .getHeader(SPARQL_QUERY, String.class);
                                if (Objects.equals(contentType, "collection")) {
                                    final String contextUri = "context.json";
                                    final String frameUri = "collection-frame.json";
                                    e.getIn()
                                     .setBody(FromRdf.toJsonLd(e.getIn()
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
                        .to("direct:buildCollection");
                from("direct:buildCollection")
                        .routeId("CollectionBuilder")
                        .log(LoggingLevel.INFO, LOGGER, "Building Collection")
                        .process(e -> {
                            final CollectionBuilder builder = new CollectionBuilder(e.getIn()
                                                                                     .getBody()
                                                                                     .toString());
                            e.getIn()
                             .setBody(builder.build());
                        });

            }
        });

        camelContext.start();

        // let it run for 5 minutes before shutting down
        Thread.sleep(5 * 60 * 1000);

        camelContext.stop();
    }

    private static String sparqlSelect(final String command) {
        try {
            return "query=" + encode(command, "UTF-8");
        } catch (final IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}