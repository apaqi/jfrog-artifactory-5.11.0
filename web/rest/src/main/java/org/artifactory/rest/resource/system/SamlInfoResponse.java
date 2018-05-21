package org.artifactory.rest.resource.system;

import lombok.Data;
import org.artifactory.rest.common.model.RestModel;

@Data
public class SamlInfoResponse implements RestModel {
    private String loginUrl;
    private String logoutUrl;
    private boolean enabled;
}
