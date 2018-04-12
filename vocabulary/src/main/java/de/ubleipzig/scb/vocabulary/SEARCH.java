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

package de.ubleipzig.scb.vocabulary;

import org.apache.commons.rdf.api.IRI;

public final class SEARCH extends BaseVocabulary {

    /* CONTEXT */
    public static final String CONTEXT = "http://iiif.io/api/search/0/context.json";

    public static final String SEARCH = "http://iiif.io/api/search/0#";

    /* Properties */
    public static final IRI ignored = createIRI(SEARCH + "ignored");
    public static final IRI match = createIRI(SEARCH + "match");
    public static final IRI before = createIRI(SEARCH + "before");
    public static final IRI after = createIRI(SEARCH + "after");
    public static final IRI count = createIRI(SEARCH + "count");
    public static final IRI hasHitList = createIRI(SEARCH + "hasHitList");
    public static final IRI hasTermList = createIRI(SEARCH + "hasTermList");
    public static final IRI refines = createIRI(SEARCH + "refines");
    public static final IRI hasSelector = createIRI(SEARCH + "hasSelector");

    private SEARCH() {
        super();
    }

}
