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

public class Service {

    @JsonProperty("@id")
    private String id;

    @JsonProperty("@context")
    private String context;

    @JsonProperty("profile")
    private String profile;

    /**
     * TemplateService.
     */
    public Service() {
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
     * @param profile profile
     */
    public void setProfile(final String profile) {
        this.profile = profile;
    }

    /**
     * @return String
     */
    @JsonIgnore
    public String getProfile() {
        return this.profile;
    }

    /**
     * @return String
     */
    @JsonIgnore
    public String getContext() {
        return this.context;
    }

    /**
     * @param context context
     */
    @JsonIgnore
    public void setContext(final String context) {
        this.context = context;
    }
}
