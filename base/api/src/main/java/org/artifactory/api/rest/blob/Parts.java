
package org.artifactory.api.rest.blob;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;
import java.util.Map;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class Parts {

    @JsonProperty("parts_list")
    public List<PartsList> partsList;
    @JsonProperty("checksums_ordinal")
    public Map<String, Long> checksumsOrdinal;
}
