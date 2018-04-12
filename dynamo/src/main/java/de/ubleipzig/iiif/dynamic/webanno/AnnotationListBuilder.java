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

import com.fasterxml.jackson.core.type.TypeReference;

import de.ubleipzig.iiif.dynamic.webanno.templates.AnnotationDocument;
import de.ubleipzig.iiif.dynamic.webanno.templates.AnnotationList;
import de.ubleipzig.iiif.dynamic.webanno.templates.SearchHit;
import de.ubleipzig.iiif.dynamic.webanno.templates.TaggingAnnotation;
import de.ubleipzig.scb.vocabulary.SC;
import de.ubleipzig.scb.vocabulary.SEARCH;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * AnnotationListBuilder.
 */
public class AnnotationListBuilder extends AbstractSerializer {

    private final String body;

    /**
     * @param body String
     */
    public AnnotationListBuilder(final String body) {
        this.body = body;
    }

    /**
     * @return List
     */
    public AnnotationList readBody() {
        try {
            final AnnotationDocument doc = MAPPER.readValue(body, new TypeReference<AnnotationDocument>() {
            });
            final List<AnnotationList> graph = doc.getGraph();
            return graph.get(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     *
     * @param graph AnnotationList
     * @return List
     */
    public List<TaggingAnnotation> getResources(final AnnotationList graph) {
        return graph.getResources();
    }

    /**
     * @return String
     */
    public String build() {
        final AnnotationList graph = readBody();
        final List<TaggingAnnotation> tagList = getResources(graph);
        final AnnotationList annotationList = new AnnotationList();
        final List<String> contexts = new ArrayList<>();
        contexts.add(SC.CONTEXT);
        contexts.add(SEARCH.CONTEXT);
        annotationList.setContexts(contexts);
        annotationList.setId("host:search");
        annotationList.setResources(tagList);
        final List<String> annoIdList = new ArrayList<>();
        final SearchHit searchHit = new SearchHit();
        tagList.forEach(t -> {
            annoIdList.add(t.getId());
        });
        searchHit.setAnnotations(annoIdList);
        final List<SearchHit> hits = new ArrayList<>();
        hits.add(searchHit);
        annotationList.setSearchHits(hits);
        final Optional<String> json = serialize(annotationList);
        return json.orElse(null);
    }
}
