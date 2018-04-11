package org.ubl.iiif.dynamic.webanno;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TaggingAnnotation {

    @JsonProperty("@id")
    private String id;

    @JsonProperty("@type")
    private String type;

    @JsonProperty("resource")
    private TextualBody body;

    @JsonProperty("on")
    private String target;

    @JsonProperty("motivation")
    private String motivation;

    /**
     * @return String
     */
    public String getId() {
        return this.id;
    }

}
