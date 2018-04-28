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

import static de.ubleipzig.webanno.AbstractSerializer.serialize;
import static java.net.URLEncoder.encode;
import static org.apache.camel.Exchange.CONTENT_TYPE;
import static org.apache.camel.Exchange.HTTP_CHARACTER_ENCODING;
import static org.apache.camel.Exchange.HTTP_METHOD;
import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;

import de.ubleipzig.iiif.dynamic.JsonLdProcessorUtils;
import de.ubleipzig.iiif.dynamic.QueryUtils;
import de.ubleipzig.webanno.AnnotationListBuilder;
import de.ubleipzig.webanno.templates.AnnotationList;

import java.io.IOException;
import java.io.UncheckedIOException;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class QueryAnnotationListRouteTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueryAnnotationListRouteTest.class);

    private static final String HTTP_ACCEPT = "Accept";
    private static final String EMPTY = "empty";
    private static final String V1_SET = "q";
    private static final String CONTEXT = "context.json";
    private static final String FRAME = "searchanno-frame.json";

    private QueryAnnotationListRouteTest() {
    }

    public static void main(final String[] args) throws Exception {
        LOGGER.info("About to run SPARQL API...");

        final CamelContext camelContext = new DefaultCamelContext();
        camelContext.addRoutes(new RouteBuilder() {
            public void configure() {
                final PropertiesComponent pc = getContext().getComponent("properties", PropertiesComponent.class);
                pc.setLocation("classpath:application.properties");

                from("jetty:http://{{rest.host}}:{{searchservice.port}}{{searchservice.prefix}}?"
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
                        .setHeader(HTTP_METHOD)
                        .constant("POST")
                        .setHeader(CONTENT_TYPE)
                        .constant("application/x-www-form-urlencoded; " + "charset=utf-8")
                        .setHeader(HTTP_ACCEPT)
                        .constant("application/n-triples")
                        .process(e -> e.getIn()
                                       .setBody(sparqlSelect(
                                               QueryUtils.getQuery("searchanno.sparql",  getV1(e)))))
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
                        .log(LoggingLevel.INFO, LOGGER, "Marshalling RDF to Json-Ld")
                        .process(e -> {
                            final String body = e.getIn().getBody().toString();
                            if ( body != null && !body.isEmpty()) {

                                e.getIn().setBody(JsonLdProcessorUtils.toJsonLd(e.getIn().getBody().toString(),
                                        CONTEXT, FRAME));
                            } else {
                                e.getIn().setHeader(CONTENT_TYPE, EMPTY);
                            }
                        })
                        .removeHeader(HTTP_ACCEPT)
                        .choice()
                        .when(header(CONTENT_TYPE).isEqualTo(EMPTY))
                            .to("direct:buildEmptyList")
                        .otherwise()
                        .to("direct:buildAnnotationList");
                from("direct:buildEmptyList")
                        .routeId("EmptyListBuilder")
                        .process(e -> {
                            final AnnotationList emptyList = new AnnotationList();
                            e.getIn().setBody(serialize(emptyList).orElse(""));
                        });
                from("direct:buildAnnotationList")
                        .routeId("AnnotationListBuilder")
                        .setHeader(HTTP_CHARACTER_ENCODING)
                        .constant("UTF-8")
                        .setHeader(CONTENT_TYPE)
                        .constant("application/ld+json")
                        .log(LoggingLevel.INFO, LOGGER, "Building Collection")
                        .process(e -> {
                            final AnnotationListBuilder builder = new AnnotationListBuilder(e.getIn()
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

    private static String getV1(final Exchange e) {
        final Object optHdr = e.getIn()
                               .getHeader(V1_SET);
        return (String) optHdr;
    }
}
