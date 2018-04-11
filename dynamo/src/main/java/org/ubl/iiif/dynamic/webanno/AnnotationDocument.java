package org.ubl.iiif.dynamic.webanno;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class AnnotationDocument {

    @JsonProperty("@context")
    Object context;

    @JsonProperty("@graph")
    List<AnnotationList> graph;

    /**
     *
     * @return List
     */
    List<AnnotationList> getGraph() {
        return this.graph;
    }
}
