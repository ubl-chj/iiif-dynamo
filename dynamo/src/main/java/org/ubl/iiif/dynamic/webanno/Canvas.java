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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public class Canvas {

    @JsonProperty("@id")
    private String id;

    @JsonProperty("@type")
    private String type;

    @JsonProperty("images")
    private List<Object> images;

    @JsonProperty("metadata")
    private List<Metadata> metadata;

    @JsonProperty("label")
    private String label;

    @JsonProperty("height")
    private int height;

    @JsonProperty("width")
    private int width;

    @JsonProperty("sc:metadataLabels")
    private Map<Object, Object> metadataLabels;

    /**
     * @return String
     */
    @JsonIgnore
    public String getLabel() {
        return this.label;
    }

    /**
     * @return String
     */
    @JsonIgnore
    public List<Metadata> getMetadata() {
        return this.metadata;
    }

    /**
     * @return String
     */
    @JsonIgnore
    public Map<Object, Object> getMetadataLabels() {
        return this.metadataLabels;
    }
}
