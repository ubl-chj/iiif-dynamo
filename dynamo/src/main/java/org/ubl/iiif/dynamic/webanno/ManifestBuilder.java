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

package org.ubl.iiif.dynamic.webanno;

import static com.fasterxml.jackson.core.util.DefaultIndenter.SYSTEM_LINEFEED_INSTANCE;
import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.ubl.iiif.dynamic.webanno.Constants.presentationContext;
import static org.ubl.iiif.dynamic.webanno.Constants.trellisManifestBase;
import static org.ubl.iiif.dynamic.webanno.Constants.trellisSequenceBase;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ManifestBuilder {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.configure(WRITE_DATES_AS_TIMESTAMPS, false);
        MAPPER.configure(INDENT_OUTPUT, true);
    }

    private final String body;

    /**
     * @param body String
     */
    public ManifestBuilder(final String body) {
        this.body = body;
    }

    /**
     * Serialize the Manifest.
     *
     * @param manifest manifest
     * @return the Manifest as a JSON string
     */
    public static Optional<String> serialize(final Object manifest) {
        try {
            return of(MAPPER.writer(PrettyPrinter.instance)
                            .writeValueAsString(manifest));
        } catch (final JsonProcessingException ex) {
            return empty();
        }
    }

    /**
     * @return List
     */
    public List<Canvas> readBody() {
        try {
            final Targets targets = MAPPER.readValue(body, new TypeReference<Targets>() {
            });
            final List<Canvas> graph = targets.getGraph();
            graph.sort(Comparator.comparing(Canvas::getLabel));
            return graph;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param graph graph
     * @return List
     */
    public List<Sequence> getSequence(final List<Canvas> graph) {
        final String id = trellisSequenceBase + UUID.randomUUID();
        final List<Sequence> sequences = new ArrayList<>();
        final Sequence sequence = new Sequence(id, graph);
        sequences.add(sequence);
        return sequences;
    }

    /**
     * @param sequences sequences
     * @return Manifest
     */
    public Manifest getManifest(final List<Sequence> sequences) {
        final String id = trellisManifestBase + UUID.randomUUID();
        final Manifest manifest = new Manifest();
        manifest.setContext(presentationContext);
        manifest.setId(id);
        manifest.setSequences(sequences);
        return manifest;
    }

    /**
     * @return String
     */
    public String build() {
        final List<Canvas> body = readBody();
        final List<Sequence> sequences = getSequence(body);
        final Manifest manifest = getManifest(sequences);
        final Optional<String> json = serialize(manifest);
        return json.orElse(null);
    }

    private static class PrettyPrinter extends DefaultPrettyPrinter {

        public static final PrettyPrinter instance = new PrettyPrinter();

        public PrettyPrinter() {
            _arrayIndenter = SYSTEM_LINEFEED_INSTANCE;
        }
    }
}
