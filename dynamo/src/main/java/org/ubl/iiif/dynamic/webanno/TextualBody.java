package org.ubl.iiif.dynamic.webanno;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TextualBody {

    @JsonProperty("@id")
    private String id;

    @JsonProperty("@type")
    private String type;

    @JsonProperty("value")
    private String value;

    /**
     * @param id id
     */
    public void setId(final String id) {
        this.id = id;
    }

    /**
     * @param type type
     */
    public void setType(final String type) {
        this.type = type;
    }

    /**
     * @return String
     */
    public String getValue() {
        return this.value;
    }

    /**
     * @param value value
     */
    public void setValue(final String value) {
        this.value = value;
    }
}
