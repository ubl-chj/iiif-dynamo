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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * QueryUtils.
 *
 * @author christopher-johnson
 */
public final class QueryUtils {

    private QueryUtils() {
    }

    /**
     * @param query query
     * @param v1 v1
     * @param v2 v2
     * @return String
     */
    private static String replaceNode(final String query, final String v1, final String v2) {

        final Pattern p = Pattern.compile("(\"\\?v1)\" \"(\\?v2)\"");
        final Matcher m = p.matcher(query);
        final StringBuffer sb = new StringBuffer(query.length());
        while (m.find()) {
            m.appendReplacement(sb, "\"" + replace(m.group(1), v1) + "\" \"" + replace(m.group(2), v2) + "\"");
        }
        m.appendTail(sb);
        return sb.toString();
    }

    /**
     * @param query query
     * @param v1 v1
     * @return String
     */
    private static String replaceNode(final String query, final String v1) {

        final Pattern p = Pattern.compile("(\"\\?v1)\"");
        final Matcher m = p.matcher(query);
        final StringBuffer sb = new StringBuffer(query.length());
        while (m.find()) {
            m.appendReplacement(sb, "\"" + replace(m.group(1), v1) + "\"");
        }
        m.appendTail(sb);
        return sb.toString();
    }

    /**
     * @param group group
     * @param value value
     * @return String
     */
    private static String replace(final String group, final String value) {
        return Matcher.quoteReplacement(value);
    }

    /**
     * @param qname String
     * @param V1 String
     * @param V2 String
     * @return String
     * @throws IOException IOException
     */
    public static String getQuery(final String qname, final String V1, final String V2) throws IOException {
        final ClassLoader classloader = Thread.currentThread()
                                              .getContextClassLoader();
        final InputStream is = classloader.getResourceAsStream(qname);
        final String out = readFile(is);
        return replaceNode(out, V1, V2);
    }

    /**
     * @param qname String
     * @param V1 String
     * @return String
     * @throws IOException IOException
     */
    public static String getQuery(final String qname, final String V1) throws IOException {
        final ClassLoader classloader = Thread.currentThread()
                                              .getContextClassLoader();
        final InputStream is = classloader.getResourceAsStream(qname);
        final String out = readFile(is);
        return replaceNode(out, V1);
    }

    /**
     * @param qname String
     * @return String
     * @throws IOException IOException
     */
    public static String getQuery(final String qname) throws IOException {
        final ClassLoader classloader = Thread.currentThread()
                                              .getContextClassLoader();
        final InputStream is = classloader.getResourceAsStream(qname);
        return readFile(is);
    }

    /**
     * @param in InputStream
     * @return String
     * @throws IOException
     */
    private static String readFile(final InputStream in) throws IOException {
        final StringBuilder inobj = new StringBuilder();
        try (BufferedReader buf = new BufferedReader(new InputStreamReader(in, "UTF-8"))) {
            String line;
            while ((line = buf.readLine()) != null) {
                inobj.append(line)
                     .append("\n");
            }
        }
        return inobj.toString();
    }
}
