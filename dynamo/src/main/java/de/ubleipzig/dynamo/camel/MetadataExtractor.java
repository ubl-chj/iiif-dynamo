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

import static de.ubleipzig.dynamo.JsonLdProcessorUtils.toRDF;
import static de.ubleipzig.webanno.AbstractSerializer.serialize;
import static org.apache.camel.Exchange.CONTENT_TYPE;
import static org.apache.camel.Exchange.HTTP_METHOD;
import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;
import static org.apache.camel.LoggingLevel.INFO;
import static org.apache.commons.rdf.api.RDFSyntax.NTRIPLES;
import static org.apache.jena.core.rdf.model.ModelFactory.createDefaultModel;

import com.github.jsonldjava.core.JsonLdError;

import de.ubleipzig.dynamo.QueryUtils;
import de.ubleipzig.webanno.templates.MetadataMap;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.TreeMap;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.main.Main;
import org.apache.camel.main.MainListenerSupport;
import org.apache.camel.main.MainSupport;
import org.apache.camel.util.IOHelper;
import org.apache.commons.rdf.api.Graph;
import org.apache.commons.rdf.jena.JenaRDF;
import org.apache.jena.arq.query.Query;
import org.apache.jena.arq.query.QueryExecution;
import org.apache.jena.arq.query.QueryExecutionFactory;
import org.apache.jena.arq.query.QueryFactory;
import org.apache.jena.arq.query.QuerySolution;
import org.apache.jena.arq.query.ResultSet;
import org.apache.jena.arq.riot.Lang;
import org.apache.jena.arq.riot.RDFDataMgr;
import org.apache.jena.core.rdf.model.Literal;
import org.apache.jena.core.rdf.model.Model;
import org.apache.jena.core.rdf.model.ModelFactory;
import org.apache.jena.core.rdf.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetadataExtractor {
    private static final Logger LOGGER = LoggerFactory.getLogger(MetadataExtractor.class);
    private static final String HTTP_ACCEPT = "Accept";
    private static final String SPARQL_QUERY = "type";
    private static final String MANIFEST_URI = "manifest";
    private static final String contentTypeJsonLd = "application/ld+json";
    private static final JenaRDF rdf = new JenaRDF();
    private static final String EMPTY = "empty";

    /**
     * @param args String[]
     * @throws Exception Exception
     */
    public static void main(final String[] args) throws Exception {
        final MetadataExtractor selector = new MetadataExtractor();
        selector.init();
    }

    /**
     * @throws Exception Exception
     */
    private void init() throws Exception {
        final Main main = new Main();
        main.addRouteBuilder(new MetadataExtractor.QueryRoute());
        main.addMainListener(new MetadataExtractor.Events());
        final JndiRegistry registry = new JndiRegistry(createInitialContext());
        main.setPropertyPlaceholderLocations("file:${env:DYNAMO_HOME}/de.ubleipzig.dynamo.cfg");
        main.run();
    }

    /**
     * Events.
     */
    public static class Events extends MainListenerSupport {

        @Override
        public void afterStart(final MainSupport main) {
            System.out.println("MetadataExtractor is now started!");
        }

        @Override
        public void beforeStop(final MainSupport main) {
            System.out.println("MetadataExtractor is now being stopped!");
        }
    }

    /**
     * QueryRoute.
     */
    public static class QueryRoute extends RouteBuilder {


        /**
         *  configure.
         */
        public void configure() {
            from("jetty:http://{{rest.host}}:{{rest.port}}{{rest.prefix}}?" +
                    "optionsEnabled=true&matchOnUriPrefix=true&sendServerVersion=false" +
                    "&httpMethodRestrict=GET,OPTIONS")
                    .routeId("Extractor")
                  .removeHeaders(HTTP_ACCEPT)
                  .setHeader("Access-Control-Allow" + "-Origin")
                  .constant("*")
                  .choice()
                  .when(header(HTTP_METHOD).isEqualTo("GET"))
                  .to("direct:getManifest");
            from("direct:getManifest")
                  .process(e -> e.getIn().setHeader(Exchange.HTTP_URI, e.getIn().getHeader(MANIFEST_URI)))
                  .to("http4")
                  .filter(header(HTTP_RESPONSE_CODE).isEqualTo(200))
                  .setHeader(CONTENT_TYPE)
                  .constant(contentTypeJsonLd)
                  .convertBodyTo(String.class)
                  .log(INFO, LOGGER, "Fetching Json-LD document")
                  .to("direct:toRDF");
            from("direct:toRDF").choice()
                  .when(header(SPARQL_QUERY).isEqualTo("extract"))
                  .log(INFO, LOGGER, "Extracting Metadata from Json-LD document")
                  .process(MetadataExtractor::processJsonLdExchange);
            }
    }

    private static void processJsonLdExchange(final Exchange e) throws IOException, JsonLdError {
        final String body = e.getIn().getBody().toString();
        if (body != null && !body.isEmpty()) {
            final InputStream is = toRDF(body);
            final Graph graph = getGraph(is);
            final org.apache.jena.core.graph.Graph jenaGraph = rdf.asJenaGraph(Objects.requireNonNull(graph));
            final Model model = ModelFactory.createModelForGraph(jenaGraph);
            final String q = QueryUtils.getQuery("metadata.sparql");
            final Query query = QueryFactory.create(q);
            try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
                final ResultSet results = qexec.execSelect();
                final Map<String, String> metadata = new TreeMap<>();
                if (results.hasNext()) {
                    while (results.hasNext()) {
                        final QuerySolution qs = results.next();
                        final Resource id = qs.getResource("manifest");
                        final Literal k = qs.getLiteral("k").asLiteral();
                        final Literal v = qs.getLiteral("mvalue").asLiteral();
                        final Literal l = qs.getLiteral("title").asLiteral();
                        metadata.put(k.getString(), v.getString());
                        metadata.put("Title", l.getString());
                        metadata.put("@id", id.getURI());
                    }
                }
                final MetadataMap metadataMap = new MetadataMap();
                metadataMap.setMetadataMap(metadata);
                final Optional<String> json = serialize(metadataMap);
                e.getIn().setBody(json.orElse(null));
            }
        } else {
            e.getIn().setHeader(CONTENT_TYPE, EMPTY);
        }
    }

    private static Graph getGraph(final InputStream stream) {
        final Model model = createDefaultModel();
        if (rdf.asJenaLang(NTRIPLES).isPresent()) {
            final Lang lang = rdf.asJenaLang(NTRIPLES).get();
            RDFDataMgr.read(model, stream, null, lang);
            return rdf.asGraph(model);
        }
        return null;
    }

    /**
     * @return InitialContext Context
     * @throws Exception Exception
     */
    private static Context createInitialContext() throws Exception {
        final InputStream in = MetadataExtractor.class.getClassLoader()
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