package org.ubl.iiif.dynamic.webanno;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

@JsonPropertyOrder({"@context", "@id", "@type", "resources", "hits"})
public class AnnotationList {

    @JsonProperty("@context")
    private List<String> contexts;

    @JsonProperty("@id")
    private String id;

    @JsonProperty("@type")
    private String type = "sc:AnnotationList";

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
