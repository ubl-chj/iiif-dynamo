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

/**
 * Constants.
 */
public final class Constants {
    static String dynamoBase = "http://workspaces.ub.uni-leipzig.de:9095/dynamo?type=meta&v1=";
    static String presentationContext = "http://iiif.io/api/presentation/2/context.json";
    static String trellisCollectionBase = "trellis:data/collection/vp/";
    static String trellisManifestBase = "trellis:data/collection/vp/manifest/";
    static String trellisSequenceBase = "trellis:data/collection/vp/sequence/";
    static String scManifestType = "sc:Manifest";
    static String searchService = "http://iiif.io/api/search/0/search";
    static String searchServiceId = "http://workspaces.ub.uni-leipzig.de:9097/search";
    static String searchServiceContext = "http://iiif.io/api/search/0/context.json";

    private Constants() {
    }
}
