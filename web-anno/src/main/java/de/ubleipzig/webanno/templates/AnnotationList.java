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
package de.ubleipzig.webanno.templates;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import de.ubleipzig.scb.vocabulary.SC;

import java.util.List;

@JsonPropertyOrder({"@context", "@id", "@type", "resources", "hits"})
public class AnnotationList {

    @JsonProperty("@context")
    private List<String> contexts;

    @JsonProperty("@id")
    private String id;

    @JsonProperty("@type")
    private String type = SC._AnnotationList;

    @JsonProperty("resources")
    private List<TaggingAnnotation> resources;

    @JsonProperty("hits")
    private List<SearchHit> hits;

    /**
     * @param contexts List
     */
    public void setContexts(final List<String> contexts) {
        this.contexts = contexts;
    }

    /**
     * @param id String
     */
    public void setId(final String id) {
        this.id = id;
    }

    /**
     * @param resources List
     */
    public void setResources(final List<TaggingAnnotation> resources) {
        this.resources = resources;
    }

    /**
     * @param hits List
     */
    public void setSearchHits(final List<SearchHit> hits) {
        this.hits = hits;
    }

    /**
     * @return List
     */
    public List<TaggingAnnotation> getResources() {
        return this.resources;
    }
}
