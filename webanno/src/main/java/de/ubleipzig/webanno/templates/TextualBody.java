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
