package org.artifactory.storage.db.security.service.access;

import org.apache.commons.lang3.tuple.Pair;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.crypto.CryptoHelper;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.model.xstream.security.UserProperty;
import org.artifactory.security.MutableUserInfo;
import org.artifactory.security.UserInfo;
import org.artifactory.security.UserPropertyInfo;
import org.jfrog.access.client.AccessClient;
import org.jfrog.access.rest.user.UpdateUserRequest;
import org.jfrog.access.rest.user.UserBase;
import org.jfrog.security.crypto.EncryptionWrapper;
import org.jfrog.security.crypto.JFrogBase58;
import org.jfrog.security.crypto.exception.CryptoRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.security.MessageDigest;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Holds a bunch of logic to specify how to search for user props in Access so that the Access Service may
 * remain (ugly) concern free.
 * <p>
 * TODO [by dan]: The only true solution to this encrypted props logic is to deprecate and replace all Artifactory
 * TODO [by dan]: generated tokens with Access Tokens.
 *
 * @author Dan Feldman
 * @author Saffi Hartal
 */
public class UserPropertiesSearchHelper {
    private static final Logger log = LoggerFactory.getLogger(UserPropertiesSearchHelper.class);

    /**
     * Denotes the special properties used to search for aes-encrypted keys
     */
    public final static String SEARCHABLE_PROP_POSTFIX = "_shash";

    /**
     * findUserByProperty
     * In HA considering during decrypt encrypt:
     * master: decrypting, node: search for user property which is unexpectedly unencrypted - it must search plaintext.
     * master: encrypting node: search for user property which is unexpectedly encrypted - fail
     * Future scenario: key rotation without full decrypt:
     * re-encrypt on the fly - IF (not implemented yet) we will use on-the-fly user re-encrypte
     * we might convert back with a older algorithm key, it would break the lookup. re-encrypt would fix that.
     */
    static UserInfo findUserByProperty(AccessClient client, String key, String val, boolean exactKeyMatch) {
        UserInfo foundUserInfo;
        if (shouldEncryptProperty(key)) {
            if (shouldUseSearchProp()) {
                foundUserInfo = findUserByEncryptedValueOfPropertySuffix(client, key, val, exactKeyMatch, false);
                if (foundUserInfo != null) {
                    return foundUserInfo;
                }

                // Backward compatibility to support old type of search property. Remove this on 6.0
                foundUserInfo = findUserByEncryptedValueOfPropertySuffix(client, key, val, exactKeyMatch, true);
                if (foundUserInfo != null) {
                    return foundUserInfo;
                }
            }
        }

        foundUserInfo = findUserByProp(client, key, val, exactKeyMatch);

        if (foundUserInfo != null) {
            return foundUserInfo;
        }

        String encrypted = encrypt(val);

        if (encrypted != null && !encrypted.equals(val)) {
            // Backward compatibility to support old type of search property. Remove this on 6.0
            foundUserInfo = findUserByProp(client, key, encrypted, exactKeyMatch);
            if (foundUserInfo != null) {
                return foundUserInfo;
            }
        }

        return null;
    }

    private static UserInfo findUserByEncryptedValueOfPropertySuffix(AccessClient client, String keySuffix, String val, boolean exactKeyMatch, boolean withArtifactoryKey) {
        //Searchable prop for encrypted values is actually a prop called <prop_key>_shash which we use to search by
        Pair<String, String> searchableProp = getSearchableProp(keySuffix, val, withArtifactoryKey);
        if (searchableProp == null) {
            //This shouldn't really happen since we validated this is the correct flow with shouldEncrypt()
            log.warn("Failed to get searchable user property for key {}. Aborting user search.", keySuffix);
            return null;
        }
        List<UserInfo> decryptedUsersFound = findUsersByPropertyInternal(client, searchableProp.getKey(), searchableProp.getValue(), exactKeyMatch);
        //Several users might return for the same searchable prop - we have to decrypt each of them to see which value matches the actual searched val
        return decryptedUsersFound.stream()
                .filter(user -> matchedValueAndkeySuffix(user, keySuffix, val))
                .findFirst()
                .orElse(null);
    }

    private static UserInfo findUserByProp(AccessClient client, String key, String val, boolean exactKeyMatch) {
        List<UserInfo> usersByProp = findUsersByPropertyInternal(client, key, val, exactKeyMatch);
        return usersByProp.isEmpty() ? null : usersByProp.get(0);
    }

    private static List<UserInfo> findUsersByPropertyInternal(AccessClient client, String key, String value, boolean exactKeyMatch) {
        return client.users().findUsersByCustomData(key, value, exactKeyMatch).getUsers().stream()
                .map(UserMapper::toArtifactoryUser)
                .map(UserPropertiesSearchHelper::decryptUserProperties)
                .collect(Collectors.toList());
    }

    static boolean matchedValueAndkeySuffix(UserInfo user, String keySuffix, String val) {
        return user.getUserProperties().stream()
                .filter(it -> it.getPropKey().endsWith(keySuffix))
                .anyMatch(it -> val.equals(it.getPropValue()));
    }

    private static boolean shouldEncryptProperty(String key) {
        return UserMapper.isFieldSensitive(key);
    }

    /**
     * Used to include the dedicated searchable properties for each encrypted user property where applicable.
     */
    static void addSearchablePropIfNeeded(UpdateUserRequest updateRequest, String propKey, String val) {
        if (!shouldUseSearchProp()) {
            return;
        }
        Pair<String, String> searchableProp = getSearchableProp(propKey, val);
        //In case of encrypted user prop, this pair will contain the searchable key that should be removed, null otherwise.
        if (searchableProp != null) {
            //this is an encrypted user prop, remove its search prop as well.
            updateRequest.addCustomData(searchableProp.getKey(), searchableProp.getValue());
        }
    }

    static void deleteSearchablePropIfNeeded(UpdateUserRequest updateRequest, String propKey) {
        if (!shouldUseSearchProp()) {
            return;
        }
        Pair<String, String> searchableProp = getSearchableProp(propKey, " ");
        //In case of encrypted user prop, this pair will contain the searchable key that should be removed, null otherwise.
        if (searchableProp != null) {
            //this is an encrypted user prop, remove its search prop as well.
            updateRequest.addCustomData(searchableProp.getKey(), null);
        }
    }


    public static Pair<String, String> getSearchableProp(String key, String val) {
        return getSearchableProp(key, val, false);
    }

    /**
     * Encrypted user properties are saved with special keys to search by, this method is responsible for specifying
     * the key and value of that search prop for any given {@param key} and {@param val}
     */
    public static Pair<String, String> getSearchableProp(String key, String val, boolean withArtifactoryKey) {
        if (val == null) {
            return null;
        }

        MessageDigest digest = JFrogBase58.getSha256MessageDigest();

        // Remove this part of the if on 6
        if (withArtifactoryKey) {
            EncryptionWrapper wrapper = ArtifactoryHome.get().getArtifactoryEncryptionWrapper();
            // todo the current code decrypt is a fix
            // need to have all add/update requests in one point unencrypted. then we will aleays have the val decrypted.
            try {
                val = wrapper.decryptIfNeeded(val).getDecryptedData();
            } catch (CryptoRuntimeException ex) {
                log.trace("prop {} failed decrypt {} ", key, val, ex);
            }

            digest.update(val.getBytes());

            //Salt the value checksum with key fingerprint to avoid dictionary attacks
            digest.update(wrapper.getFingerprint().getBytes());
        } else {
            digest.update(val.getBytes());
        }

        // Decided length of 6 - should test with 1 in order to see collisions
        return Pair.of(key + SEARCHABLE_PROP_POSTFIX, JFrogBase58.encode(digest.digest()).substring(0, 6));
    }

    public static boolean isSearchableHelperProperty(@Nonnull UserBase user, String k) {
        return k.endsWith(SEARCHABLE_PROP_POSTFIX) &&
                user.getCustomData().containsKey(k.substring(0, k.length() - SEARCHABLE_PROP_POSTFIX.length()));
    }

    /**
     * To support backwards compatibility with non-aes encrypted instances, the decision whether to use the dedicated
     * search props is taken here.
     */
    static boolean shouldUseSearchProp() {
        // Always encrypting in access
        return true;
    }

    private static String encrypt(String val) {
        return CryptoHelper.encryptIfNeeded(ArtifactoryHome.get(), val);
    }

    static UserInfo decryptUserProperties(UserInfo user) {
        Function<String, String> decryptor = v -> CryptoHelper.decryptIfNeeded(ArtifactoryHome.get(), v);
        Set<UserPropertyInfo> userDecryptedProperties = user.getUserProperties().stream()
                .map(p -> new UserProperty(p.getPropKey(), decryptor.apply(p.getPropValue()))).collect(
                        Collectors.toSet());
        MutableUserInfo userInfo = InfoFactoryHolder.get().copyUser(user);
        userInfo.setUserProperties(userDecryptedProperties);
        return userInfo;
    }
}
