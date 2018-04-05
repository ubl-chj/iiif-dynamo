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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * CollectionNode.
 *
 * @author christopher-johnson
 */
@JsonPropertyOrder({"@id", "@type", "label"})
public class CollectionNode {

    @JsonProperty("@id")
    private String id;

    @JsonProperty("@type")
    private String type;

    @JsonProperty("label")
    private String label;

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
    public String getLabel() {
        return this.label;
    }

    /**
     * @param label label
     */
    public void setLabel(final String label) {
        this.label = label;
    }
}
