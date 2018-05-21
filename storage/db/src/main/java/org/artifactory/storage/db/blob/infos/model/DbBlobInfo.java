package org.artifactory.storage.db.blob.infos.model;

import lombok.Builder;

/**
 * @author Inbar Tal
 */

@Builder(builderClassName = "Builder")
public class DbBlobInfo {

    private String checksum;
    private String blobInfo;

    public DbBlobInfo(String checksum, String blobInfo) {
        this.checksum = checksum;
        this.blobInfo = blobInfo;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setBlobInfo(String blobInfo) {
        this.blobInfo = blobInfo;
    }

    public String getBlobInfo() {
        return blobInfo;
    }
}
