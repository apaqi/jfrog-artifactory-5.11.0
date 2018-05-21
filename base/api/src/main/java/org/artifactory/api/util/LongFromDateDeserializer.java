package org.artifactory.api.util;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.joda.time.format.ISODateTimeFormat;

import java.io.IOException;

/**
 * @author Rotem Kfir
 */
public class LongFromDateDeserializer extends JsonDeserializer<Long> {
    @Override
    public Long deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException, JsonProcessingException {
        String dateTime = jsonParser.getText();
        return ISODateTimeFormat.dateTime().parseMillis(dateTime);
    }
}
