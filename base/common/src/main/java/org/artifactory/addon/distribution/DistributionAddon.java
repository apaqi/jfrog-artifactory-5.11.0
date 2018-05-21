package org.artifactory.addon.distribution;

import org.artifactory.addon.Addon;
import org.artifactory.addon.license.EnterprisePlusAddon;
import org.artifactory.api.rest.distribution.bundle.models.FileSpec;
import org.artifactory.api.common.BasicStatusHolder;

import javax.annotation.Nonnull;
import java.io.OutputStream;
import java.util.Map;


@EnterprisePlusAddon
public interface DistributionAddon extends Addon {

    String validateFileAndGetChecksum(@Nonnull FileSpec fileSpec);

    BasicStatusHolder distributeArtifact(@Nonnull FileSpec fileSpec, String delegateToken, String auth);

    void distributeArtifactStreaming(@Nonnull FileSpec fileSpec, String delegateToken, String auth, String checksum, OutputStream outputStream);
}
