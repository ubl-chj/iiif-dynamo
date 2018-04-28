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

package de.ubleipzig.iiif.dynamic;

import static org.slf4j.LoggerFactory.getLogger;

import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.utils.JsonUtils;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;

/**
 * JsonLdProcessorUtils.
 *
 * @author christopher-johnson
 */
public final class JsonLdProcessorUtils {

    private JsonLdProcessorUtils() {
    }

    private static final Logger log = getLogger(JsonLdProcessorUtils.class);

    /**
     * @param ntriples String
     * @param contextUri String
     * @param frameUri String
     * @return String
     * @throws IOException IOException
     * @throws JsonLdError JsonLdError
     */
    public static String toJsonLd(final String ntriples, final String contextUri, final String frameUri) throws
            IOException, JsonLdError {

        final JsonLdOptions opts = new JsonLdOptions();
        opts.setUseNativeTypes(true);
        final ClassLoader classloader = Thread.currentThread()
                                              .getContextClassLoader();
        final InputStream is = classloader.getResourceAsStream(contextUri);
        final Object ctxobj = JsonUtils.fromInputStream(is);
        if (PatternUtils.isNotEmpty(ntriples)) {
            final String graph = PatternUtils.convertSkolem(ntriples);
            final Object outobj = com.github.jsonldjava.core.JsonLdProcessor.fromRDF(graph, opts);
            final Object compactobj = com.github.jsonldjava.core.JsonLdProcessor.compact(outobj, ctxobj, opts);
            final InputStream fs = classloader.getResourceAsStream(frameUri);
            final Object frame = JsonUtils.fromInputStream(fs);
            final Object frameobj = com.github.jsonldjava.core.JsonLdProcessor.frame(compactobj, frame, opts);
            log.debug(JsonUtils.toPrettyString(compactobj));
            return JsonUtils.toPrettyString(frameobj);
        } else {
            final String empty = "empty SPARQL result set";
            log.info(empty);
        }
        return null;
    }
}
