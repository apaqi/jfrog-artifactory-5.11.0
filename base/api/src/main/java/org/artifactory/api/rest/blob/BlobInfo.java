
package org.artifactory.api.rest.blob;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;


@Data
public class BlobInfo {
    public String checksum;
    public Parts parts;
    public String version;

    public static BlobInfo emptyBlobInfo(String checksum) {
        BlobInfo blobinfo = new BlobInfo();
        blobinfo.checksum = checksum;
        blobinfo.parts = new Parts();
        blobinfo.parts.setChecksumsOrdinal(new HashMap<>());
        blobinfo.parts.setPartsList(new ArrayList<>());
        return blobinfo;
    }
}
