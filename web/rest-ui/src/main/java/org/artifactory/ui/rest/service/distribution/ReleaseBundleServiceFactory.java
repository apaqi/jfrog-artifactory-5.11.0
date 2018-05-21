package org.artifactory.ui.rest.service.distribution;

import org.springframework.beans.factory.annotation.Lookup;

public abstract class ReleaseBundleServiceFactory {

    @Lookup
    public abstract GetAllReleaseBundleService getAllBundles();

    @Lookup
    public abstract GetAllReleaseBundleVersionsService getAllBundleVersions();

    @Lookup
    public abstract GetReleaseBundleService getReleaseBundle();

    @Lookup
    public abstract DeleteReleaseBundleService deleteReleaseBundle();
}
