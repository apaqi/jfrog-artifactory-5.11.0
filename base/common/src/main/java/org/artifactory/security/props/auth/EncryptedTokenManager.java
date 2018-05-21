package org.artifactory.security.props.auth;

import org.apache.commons.lang3.tuple.Pair;
import org.artifactory.common.ArtifactoryHome;
import org.jfrog.security.crypto.EncryptionWrapper;
import org.jfrog.security.crypto.JFrogBase58;
import org.jfrog.security.crypto.exception.CryptoRuntimeException;

import java.security.MessageDigest;
import java.util.Set;

/**
 * This interface is for managing user properties that require encryption/decryption.
 *
 * @author Rotem Kfir
 */
public interface EncryptedTokenManager {

    /**
     * @return The names of the properties that require encryption/decryption
     */
    Set<String> getPropKeys();
}
