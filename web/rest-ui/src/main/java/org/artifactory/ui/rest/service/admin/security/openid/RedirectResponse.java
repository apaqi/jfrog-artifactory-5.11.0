package org.artifactory.ui.rest.service.admin.security.openid;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.artifactory.rest.common.model.BaseModel;

/**
 * @author Noam Shemesh
 */
@Value
@EqualsAndHashCode(callSuper = true)
@Builder(builderClassName = "Builder")
public class RedirectResponse extends BaseModel {
    private String url;

    @Override
    public String toString() {
        return super.toString();
    }
}
