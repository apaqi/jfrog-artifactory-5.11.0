package org.artifactory.rest.services;

import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.config.VersionInfo;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.rest.resource.system.AuthProviderInfo;
import org.artifactory.rest.resource.system.EnterprisePlusServiceInfo;
import org.artifactory.rest.resource.system.ServiceRegistryInfo;
import org.artifactory.rest.resource.system.SystemServiceInfo;
import org.artifactory.security.access.AccessService;
import org.jfrog.access.common.ServiceId;
import org.jfrog.access.common.ServiceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static org.artifactory.api.rest.constant.SystemRestConstants.PATH_ACCESS_PROXY_ROOT_PREFIX;

@Service
public class SystemTopologyResourceHelper {

    private AccessService accessService;

    private CentralConfigService centralConfigService;

    private AuthorizationService authenticationService;

    @Autowired
    public SystemTopologyResourceHelper(AuthorizationService authenticationService,
            CentralConfigService centralConfigService, AccessService accessService) {
        this.accessService = accessService;
        this.authenticationService = authenticationService;
        this.centralConfigService = centralConfigService;
    }


    public SystemServiceInfo createSystemServiceInfo(EnterprisePlusServiceInfo enterprisePlusServiceInfo,
            String artifactoryUrl) {
        VersionInfo versionInfo = centralConfigService.getVersionInfo();
        ServiceId serviceId = accessService.getArtifactoryServiceId();
        String artServiceId = serviceId.getFormattedName();
        String artVersion = versionInfo.getVersion();
        String artType = serviceId.getServiceType();
        return new SystemServiceInfo(artServiceId, artVersion, artType, artifactoryUrl,
                enterprisePlusServiceInfo);
    }

    public EnterprisePlusServiceInfo createEnterprisePlusServiceInfo(String artifactoryUrl) {
        AuthProviderInfo accessProviderInfo = createAccessProviderInfo(artifactoryUrl);
        ServiceRegistryInfo serviceRegistryInfo = createServiceRegistryInfo();
        return new EnterprisePlusServiceInfo(accessProviderInfo, serviceRegistryInfo);
    }

    private ServiceRegistryInfo createServiceRegistryInfo() {
        //for now return empty registryInfo
        return new ServiceRegistryInfo("", "");
    }

    private AuthProviderInfo createAccessProviderInfo(String artifactoryUrl) {
        String accessServiceId;
        try {
            accessServiceId = accessService.getAccessClient().system().getAccessServiceId().getFormattedName();
        } catch (IOException e) {
            throw new IllegalStateException("Could not retrieve AccessServiceId");
        }
        String accessServiceType = ServiceType.ACCESS;
        String accessServiceUrl = null;
        if (authenticationService.isAdmin()) {
            accessServiceUrl = artifactoryUrl + "/" + PATH_ACCESS_PROXY_ROOT_PREFIX;
        }
        return new AuthProviderInfo(accessServiceId, accessServiceUrl, accessServiceType);
    }
}
