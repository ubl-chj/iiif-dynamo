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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * TemplateMetadata.
 *
 * @author christopher-johnson
 */
public class Metadata {

    @JsonProperty("@id")
    private String id;

    @JsonProperty("label")
    private String label;

    @JsonProperty("value")
    private String value;

    /**
     * TemplateMetadata.
     */
    public Metadata() {
    }

    /**
     * @return String
     */
    public String getId() {
        return this.id;
    }

    /**
     * @param id id
     */
    public void setId(final String id) {
        this.id = id;
    }

    /**
     * @return String
     */
    @JsonIgnore
    public String getValue() {
        return this.value;
    }

    /**
     * @return label
     */
    @JsonIgnore
    public String getLabel() {
        return this.label;
    }

}
