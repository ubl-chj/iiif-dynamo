package org.ubl.iiif.dynamic.webanno;

import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class AnnotationListBuilder extends AbstractSerializer {

    private final String body;

    /**
     * @param body String
     */
    public AnnotationListBuilder(final String body) {
        this.body = body;
    }

    /**
     * @return List
     */
    public AnnotationList readBody() {
        try {
            final AnnotationDocument doc = MAPPER.readValue(body, new TypeReference<AnnotationDocument>() {
            });
            final List<AnnotationList> graph = doc.getGraph();
            return graph.get(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<TaggingAnnotation> getResources(final AnnotationList graph) {
        return graph.getResources();
    }

    /**
     * @return String
     */
    public String build() {
        final AnnotationList graph = readBody();
        final List<TaggingAnnotation> tagList = getResources(graph);
        AnnotationList annotationList = new AnnotationList();
        List<String> contexts = new ArrayList<>();
        contexts.add("http://iiif.io/api/presentation/2/context.json");
        contexts.add("http://iiif.io/api/search/0/context.json");
        annotationList.setContexts(contexts);
        annotationList.setId("host:search");
        annotationList.setResources(tagList);
        List<String> annoIdList = new ArrayList<>();
        SearchHit searchHit = new SearchHit();
        tagList.forEach(t -> {
            annoIdList.add(t.getId());
        });
        searchHit.setAnnotations(annoIdList);
        List<SearchHit> hits = new ArrayList<>();
        hits.add(searchHit);
        annotationList.setSearchHits(hits);
        final Optional<String> json = serialize(annotationList);
        return json.orElse(null);
    }
}
