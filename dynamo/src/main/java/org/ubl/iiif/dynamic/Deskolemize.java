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

package org.ubl.iiif.dynamic;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Deskolemize.
 *
 * @author christopher-johnson
 */
public final class Deskolemize {

    private Deskolemize() {
    }

    /**
     * Converts a SkolemIRI to a BNode.
     *
     * @param input the SkolemIRI to convert.
     * @return a BNode.
     */
    static String convertSkolem(final String input) {
        final Pattern p = Pattern.compile("trellis:bnode/([0-9a-f]{8}-([0-9a-f]{4}-){3}[0-9a-f]{44})");
        final Matcher m = p.matcher(input);
        final StringBuffer sb = new StringBuffer(input.length());
        while (m.find()) {
            final String id = m.group(1);
            final String bnode = "_:b" + id;
            m.appendReplacement(sb, Matcher.quoteReplacement(bnode));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    /**
     * Converts a trellis:data node to a concrete hostname.
     *
     * @param input the dataset to convert.
     * @param hostname the hostname.
     * @return a BNode.
     */
    static String convertHostname(final String input, final String hostname) {
        final Pattern p = Pattern.compile("(trellis:data/)(.*)");
        final Matcher m = p.matcher(input);
        final StringBuffer sb = new StringBuffer(input.length());
        while (m.find()) {
            final String path = m.group(2);
            final String node = hostname + "/" + path;
            m.appendReplacement(sb, Matcher.quoteReplacement(node));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    static boolean isNotEmpty(final String input) {
        final Pattern p = Pattern.compile("^<");
        final Matcher m = p.matcher(input);
        return m.find();
    }
}
