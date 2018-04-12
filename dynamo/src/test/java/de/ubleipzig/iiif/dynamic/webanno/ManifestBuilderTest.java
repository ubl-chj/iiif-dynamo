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

import de.ubleipzig.iiif.dynamic.webanno.templates.Canvas;
import de.ubleipzig.iiif.dynamic.webanno.templates.Manifest;
import de.ubleipzig.iiif.dynamic.webanno.templates.Sequence;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

public class ManifestBuilderTest {

    private InputStream sourceFile = ManifestBuilderTest.class.getResourceAsStream("/unordered-targets.json");

    private static String read(final InputStream input) throws IOException {
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(input))) {
            return buffer.lines().collect(Collectors.joining("\n"));
        }
    }

    @Test
    void orderCanvasesTest() {
        try {
            final String json = read(sourceFile);
            final ManifestBuilder builder = new ManifestBuilder(json);
            final List<Canvas> graph = builder.readBody();
            System.out.println(graph);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void assembleManifestTest() {
        try {
            final String json = read(sourceFile);
            final ManifestBuilder builder = new ManifestBuilder(json);
            final List<Canvas> graph = builder.readBody();
            final List<Sequence> sequences = builder.getSequence(graph);
            final Manifest manifest = builder.getManifest(sequences);
            System.out.println(manifest);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void buildManifestTest() {
        try {
            final String json = read(sourceFile);
            final ManifestBuilder builder = new ManifestBuilder(json);
            final String out = builder.build();
            System.out.println(out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
