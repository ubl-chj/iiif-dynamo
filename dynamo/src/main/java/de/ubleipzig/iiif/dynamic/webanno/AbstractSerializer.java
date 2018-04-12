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
package de.ubleipzig.iiif.dynamic.webanno;

import static com.fasterxml.jackson.core.util.DefaultIndenter.SYSTEM_LINEFEED_INSTANCE;
import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;
import static java.util.Optional.empty;
import static java.util.Optional.of;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Optional;

public abstract class AbstractSerializer {


    protected static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.configure(WRITE_DATES_AS_TIMESTAMPS, false);
        MAPPER.configure(INDENT_OUTPUT, true);
    }

    /**
     * Serialize the Collection.
     *
     * @param collection manifest
     * @return the Collection as a JSON string
     */
    public static Optional<String> serialize(final Object collection) {
        try {
            return of(MAPPER.writer(CollectionBuilder.PrettyPrinter.instance).writeValueAsString(collection));
        } catch (final JsonProcessingException ex) {
            return empty();
        }
    }


    protected static class PrettyPrinter extends DefaultPrettyPrinter {

        public static final PrettyPrinter instance = new PrettyPrinter();

        public PrettyPrinter() {
            _arrayIndenter = SYSTEM_LINEFEED_INSTANCE;
        }
    }
}
