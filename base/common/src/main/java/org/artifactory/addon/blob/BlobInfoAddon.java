package org.artifactory.addon.blob;

import org.artifactory.addon.Addon;
import org.artifactory.addon.license.EnterprisePlusAddon;
import org.artifactory.api.rest.blob.BlobInfo;
import org.artifactory.api.rest.blob.ClosestBlobInfoRequest;

/**
 * @author Rotem Kfir
 */
@EnterprisePlusAddon
public interface BlobInfoAddon extends Addon {

    BlobInfo getClosestBlobInfo(ClosestBlobInfoRequest request, String auth);
}
