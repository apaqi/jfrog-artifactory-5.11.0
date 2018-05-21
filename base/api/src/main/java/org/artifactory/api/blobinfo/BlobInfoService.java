package org.artifactory.api.blobinfo;

import org.artifactory.api.rest.blob.BlobInfo;

public interface BlobInfoService {

    BlobInfo getBlobInfo(String checksum, String mimeType, String auth);

}
