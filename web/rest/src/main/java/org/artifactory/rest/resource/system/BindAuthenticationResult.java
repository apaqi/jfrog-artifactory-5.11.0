package org.artifactory.rest.resource.system;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.artifactory.rest.resource.token.TokenResponseModel;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BindAuthenticationResult {

    private BindAuthenticationStatusResult status;
    private TokenResponseModel token;

}
