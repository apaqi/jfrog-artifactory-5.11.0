package org.artifactory.rest.resource.system;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BindServiceInfo {

    private String id;
    private String url;
    private String token;

}
