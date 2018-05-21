package org.artifactory.api.util;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.joda.time.format.ISODateTimeFormat;

import java.io.IOException;

/**
 * @author Rotem Kfir
 */
public class LongToDateSerializer extends JsonSerializer<Long> {
    @Override
    public void serialize(Long value, JsonGenerator gen, SerializerProvider serializers)
            throws IOException, JsonProcessingException {
        gen.writeString(ISODateTimeFormat.dateTime().print(value));
    }
}
