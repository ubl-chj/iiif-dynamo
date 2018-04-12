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

package de.ubleipzig.iiif.dynamic.webanno;

import static de.ubleipzig.iiif.dynamic.webanno.Constants.searchService;
import static de.ubleipzig.iiif.dynamic.webanno.Constants.searchServiceContext;
import static de.ubleipzig.iiif.dynamic.webanno.Constants.searchServiceId;
import static de.ubleipzig.iiif.dynamic.webanno.Constants.trellisManifestBase;
import static de.ubleipzig.iiif.dynamic.webanno.Constants.trellisSequenceBase;

import com.fasterxml.jackson.core.type.TypeReference;

import de.ubleipzig.iiif.dynamic.webanno.templates.Canvas;
import de.ubleipzig.iiif.dynamic.webanno.templates.Manifest;
import de.ubleipzig.iiif.dynamic.webanno.templates.Sequence;
import de.ubleipzig.iiif.dynamic.webanno.templates.Service;
import de.ubleipzig.iiif.dynamic.webanno.templates.Targets;
import de.ubleipzig.scb.vocabulary.SC;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ManifestBuilder extends AbstractSerializer {

    private final String body;

    /**
     * @param body String
     */
    public ManifestBuilder(final String body) {
        this.body = body;
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
        sequence.setViewingHint("paged");
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
        manifest.setContext(SC.CONTEXT);
        manifest.setId(id);
        manifest.setSequences(sequences);
        final Service service = new Service();
        service.setContext(searchServiceContext);
        service.setId(searchServiceId);
        service.setProfile(searchService);
        manifest.setService(service);
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
}
