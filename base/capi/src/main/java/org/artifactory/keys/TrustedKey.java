package org.artifactory.keys;

public interface TrustedKey {
    String getKid();

    String getTrustedKey();

    String getFingerprint();

    String getAlias();

    Long getIssued();

    String getIssuedBy();

    Long getExpiry();
}
