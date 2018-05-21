package org.artifactory.addon.binary.provider;

import org.artifactory.addon.Addon;

import java.io.InputStream;

public interface BinaryProviderApiAddon extends Addon {

    InputStream getBinaryBySha256(String sha256);

    InputStream getBinaryPartBySha256(String sha256, long start, long end);
}
