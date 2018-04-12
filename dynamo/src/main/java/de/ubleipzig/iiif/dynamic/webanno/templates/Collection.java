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

package de.ubleipzig.iiif.dynamic.webanno.templates;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import de.ubleipzig.scb.vocabulary.SC;

import java.util.List;

/**
 * Collection.
 *
 * @author christopher-johnson
 */
@JsonPropertyOrder({"@context", "@id", "@type", "attribution", "logo", "manifests"})
public class Collection {

    @JsonProperty("@context")
    private String context;

    @JsonProperty("@id")
    private String id;

    @JsonProperty("@type")
    private String type = SC._Collection;

    @JsonProperty("attribution")
    private String attribution = "Provided by Leipzig University";

    @JsonProperty("logo")
    private String logo = "http://iiif.ub.uni-leipzig.de/ubl-logo.png";

    @JsonProperty("manifests")
    private List<CollectionNode> collectionNode;

    /**
     * Collection.
     */
    public Collection() {
    }

    /**
     * @param context String
     */
    public void setContext(final String context) {
        this.context = context;
    }

    /**
     * @param id String
     */
    public void setId(final String id) {
        this.id = id;
    }

    /**
     * @param type String
     */
    public void setType(final String type) {
        this.type = type;
    }

    /**
     * @param attribution String
     */
    public void setAttribution(final String attribution) {
        this.attribution = attribution;
    }

    /**
     * @param logo String
     */
    public void setLogo(final String logo) {
        this.logo = logo;
    }

    /**
     * @param collectionNode List
     */
    public void setCollectionNodes(final List<CollectionNode> collectionNode) {
        this.collectionNode = collectionNode;
    }

}