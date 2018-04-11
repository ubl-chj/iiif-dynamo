package org.ubl.iiif.dynamic.webanno;

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
