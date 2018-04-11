package org.ubl.iiif.dynamic.webanno;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class SearchHit {
    @JsonProperty("@type")
    private String type = "search:Hit";

    @JsonProperty("annotations")
    private List<String> annoIds;

    /**
     * @param annoIds List
     */
    public void setAnnotations(final List<String> annoIds) {
        this.annoIds = annoIds;
    }
}
