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

import java.util.List;

/**
 * Sequence.
 *
 * @author christopher-johnson
 */
@JsonPropertyOrder({"@id", "@type", "canvases"})
public class Sequence {

    @JsonProperty("@id")
    private String id;

    @JsonProperty("@type")
    private String type = "sc:Sequence";

    @JsonProperty("canvases")
    private List<Canvas> canvases;

    /**
     *
     * @param id String
     * @param canvases List
     */
    public Sequence(final String id, final List<Canvas> canvases) {
        this.id = id;
        this.canvases = canvases;
    }
}
