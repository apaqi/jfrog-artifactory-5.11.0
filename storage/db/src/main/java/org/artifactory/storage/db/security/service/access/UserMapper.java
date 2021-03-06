package org.artifactory.storage.db.security.service.access;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.artifactory.api.security.UserInfoBuilder;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.model.xstream.security.UserProperty;
import org.artifactory.security.*;
import org.jfrog.access.model.Realm;
import org.jfrog.access.model.UserStatus;
import org.jfrog.access.rest.imports.ImportUserRequest;
import org.jfrog.access.rest.user.*;
import org.jfrog.access.rest.user.User;
import org.jfrog.common.ClockUtils;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.artifactory.storage.db.security.service.access.GroupMapper.fromAccessRealm;
import static org.artifactory.storage.db.security.service.access.GroupMapper.toAccessRealm;
import static org.artifactory.storage.db.security.service.access.UserMapper.ArtifactoryBuiltInUserProperty.*;
import static org.artifactory.storage.db.security.service.access.UserPropertiesSearchHelper.*;

/**
 * Helper class to map Artifactory user to Access user details and vice versa.
 *
 * @author Yossi Shaul
 */
public class UserMapper {

    public static boolean isFieldSensitive(String propKey) {
        return Arrays.stream(ArtifactoryBuiltInUserProperty.values())
                .filter(property -> property.name().equals(propKey))
                .findFirst()
                .map(ArtifactoryBuiltInUserProperty::isSensitive)
                .orElse(true);
    }

    /**
     * List of custom data which maps to build in user info properties in Artifactory
     */
    public enum ArtifactoryBuiltInUserProperty {
        artifactory_admin(false),
        private_key(true),
        public_key(true),
        gen_password_key(true),
        updatable_profile(false),
        bintray_auth(true),
        disabled_password(false);

        private boolean sensitive;

        ArtifactoryBuiltInUserProperty(boolean sensitive) {
            this.sensitive = sensitive;
        }

        public static boolean contains(String value) {
            return Arrays.stream(ArtifactoryBuiltInUserProperty.values())
                    .anyMatch(property -> property.name().equals(value));
        }

        public boolean isSensitive() {
            return sensitive;
        }
    }

    /**
     * Converts an Access user to Artifactory {@link UserInfo}.
     *
     * @param user User model from Access server
     * @return User info built from the Access user
     */
    @Nonnull
    public static MutableUserInfo toArtifactoryUser(@Nonnull UserBase user) {
        //TODO: [by YS] other custom properties (also encrypted)
        UserInfoBuilder builder = new UserInfoBuilder(user.getUsername())
                .email(user.getEmail())
                .admin(user.getBooleanCustomData(artifactory_admin.name()))
                .privateKey(user.getCustomData(private_key.name()))
                .publicKey(user.getCustomData(public_key.name()))
                .updatableProfile(user.getBooleanCustomData(updatable_profile.name()))
                .bintrayAuth(user.getCustomData(bintray_auth.name()))
                .lastLogin(user.getLastLoginTime(), user.getLastLoginIp())
                .credentialsExpired(user.isPasswordExpired())
                .genPasswordKey(user.getCustomData(gen_password_key.name()))
                .groups(groupsFromUser(user))
                .groupAdmin(isGroupAdmin(user))
                .passwordDisabled(user.getBooleanCustomData(disabled_password.name()))
                .password(new SaltedPassword(user.getPasswordHash(), null))
                .locked(UserStatus.LOCKED.equals(user.getStatus()))
                .realm(fromAccessRealm(user.getRealm()));

        user.getCustomData().keySet().stream()
                .filter(k -> !ArtifactoryBuiltInUserProperty.contains(k))
                .filter(k -> !isSearchableHelperProperty(user, k))
                .map(k -> new UserProperty(k, user.getCustomData(k)))
                .forEach(builder::addProp);

        return builder.build();
    }

    private static Set<UserGroupInfo> groupsFromUser(UserBase user) {
        return Stream.of(user)
                .flatMap(UserMapper::getUserGroups)
                .map((nameRealm) -> InfoFactoryHolder.get().createUserGroup(nameRealm.getLeft(), fromAccessRealm(nameRealm.getRight())))
                .collect(Collectors.toSet());
    }

    private static Stream<Pair<String, Realm>> getUserGroups(UserBase user) {
        if (user instanceof User) {
            return ((User) user).getGroups().stream()
                    .map((group) -> Pair.of(group, Realm.INTERNAL));
        } else if (user instanceof UserWithGroups) {
            return ((UserWithGroups) user).getGroups().stream()
                    .map((group) -> Pair.of(group.getName(), group.getRealm()));
        }

        throw new IllegalArgumentException(user.getClass().getCanonicalName() +
                " is not a supported child of " + UserBase.class.getCanonicalName());
    }

    private static Boolean isGroupAdmin(UserBase user) {
        if (user instanceof UserWithGroups) {
            return ((UserWithGroups) user).getGroups().stream().map(GroupMapper::toArtifactoryGroup)
                    .anyMatch(GroupInfo::isAdminPrivileges);
        }
        return null;

    }

    static UpdateUserRequest toUpdateUserRequest(MutableUserInfo user, boolean includeCustomProperties) {
        return toAccessUser(UpdateUserRequest.create(), user, includeCustomProperties)
                .passwordExpired(user.isCredentialsExpired());
    }

    /**
     * Converts an Artifactory user to Access {@link UserRequest} without any extra Artifactory user properties.
     *
     * @param user User model from Artifactory
     * @return User request built from the Artifactory user
     */
    @Nonnull
    static UserRequest toAccessUser(@Nonnull UserInfo user) {
        return toAccessUser(user, false);
    }

    /**
     * Converts an Artifactory user to Access {@link UserRequest} with or without any extra Artifactory user properties.
     *
     * @param user User model from Artifactory
     * @return User request built from the Artifactory user
     */
    @Nonnull
    static UserRequest toAccessUser(@Nonnull UserInfo user, boolean includeCustomProperties) {
        return toAccessUser(UserRequest.create(), user, includeCustomProperties);
    }

    @Nonnull
    private static <T extends UserRequest> T toAccessUser(T builder, @Nonnull UserInfo user,
                                                          boolean includeCustomProperties) {
        builder.username(user.getUsername())
                .email(user.getEmail())
                .realm(toAccessRealm(user.getRealm()))
                .status(toAccessStatus(user))
                .groups(user.getGroups().stream().map(UserGroupInfo::getGroupName).collect(Collectors.toSet()));
        addCustomData(builder, user, includeCustomProperties);

        if (isUpdate(builder)) {
            if (user.getPassword() != null) {
                builder.password(user.getPassword());
            }
        } else {
            builder.password(user.getPassword());
        }
        return builder;
    }

    /**
     * Converts an Artifactory user to full Access user for import purposes (including any extra Artifactory user properties).
     *
     * @param user User model from Artifactory
     * @return Import User request built from the Artifactory user
     */
    @Nonnull
    public static ImportUserRequest toFullAccessUser(@Nonnull UserInfo user) {
        ImportUserRequest.Builder builder = ImportUserRequest.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .email(user.getEmail())
                .realm(toAccessRealm(user.getRealm()))
                .status(toAccessStatus(user))
                .created(ClockUtils.epochMillis())
                .modified(ClockUtils.epochMillis())
                .lastLoginTime(user.getLastLoginTimeMillis())
                .lastLoginIp(user.getLastLoginClientIp())
                .statusLastModified(ClockUtils.epochMillis())
                .passwordLastModified(user.isCredentialsExpired() ? 0 : getPasswordCreated(user))
                .groups(toUserGroups(user.getGroups()));

        addCustomData(builder, user, true);

        return builder.build();
    }

    private static long getPasswordCreated(UserInfo user) {
        return user.getUserProperty("passwordCreated").filter(StringUtils::isNumeric).map(Long::parseLong)
                .orElseGet(ClockUtils::epochMillis);
    }

    private static UserStatus toAccessStatus(@Nonnull UserInfo user) {
        return user.isEnabled() ? (user.isLocked() ? UserStatus.LOCKED : UserStatus.ENABLED) : UserStatus.DISABLED;
    }

    private static void addCustomData(CustomDataBuilder builder, UserInfo user, boolean includeCustomProperties) {
        UserInfo decryptedUserInfo = UserPropertiesSearchHelper.decryptUserProperties(user);

        addCustomData(builder, artifactory_admin, decryptedUserInfo.isAdmin());
        addCustomData(builder, private_key, decryptedUserInfo.getPrivateKey());
        addCustomData(builder, public_key, decryptedUserInfo.getPublicKey());
        addCustomData(builder, gen_password_key, decryptedUserInfo.getGenPasswordKey());
        addCustomData(builder, updatable_profile, decryptedUserInfo.isUpdatableProfile());
        addCustomData(builder, bintray_auth, decryptedUserInfo.getBintrayAuth());
        addCustomData(builder, disabled_password, decryptedUserInfo.isPasswordDisabled());

        if (includeCustomProperties) {
            decryptedUserInfo.getUserProperties().forEach(p -> builder.addCustomData(p.getPropKey(),
                    StringUtils.isBlank(p.getPropValue()) ? null :
                            p.getPropValue(), true));

            //TODO [RK] they should have a special prefix so that they could be differentiated when we get user from access to RT
            // add search hints
            if (shouldUseSearchProp()) {
                Set<String> allKeys = decryptedUserInfo.getUserProperties().stream()
                        .map(UserPropertyInfo::getPropKey)
                        .collect(Collectors.toSet());

                decryptedUserInfo.getUserProperties().stream()
                        .filter(field -> !field.getPropKey().endsWith(SEARCHABLE_PROP_POSTFIX))
                        .map(p -> getSearchableProp(p.getPropKey(), p.getPropValue()))
                        .filter(Objects::nonNull)
                        .filter(searchableProp -> !allKeys.contains(searchableProp.getKey()))
                        .forEach(p -> builder.addCustomData(p.getKey(), p.getValue(), false));
            }
        }
    }

    private static void addCustomData(CustomDataBuilder builder, ArtifactoryBuiltInUserProperty property, boolean value) {
        if (value || isUpdate(builder)) {
            builder.addCustomData(property.name(), value ? "true" : null, property.sensitive);
        }
    }

    private static void addCustomData(CustomDataBuilder builder, ArtifactoryBuiltInUserProperty property,
                                      String value) {
        if (!StringUtils.isBlank(value)) {
            builder.addCustomData(property.name(), value, property.sensitive);
        } else if (isUpdate(builder)) {
            builder.addCustomData(property.name(), null, false);
        }
    }

    private static Set<ImportUserRequest.UserGroup> toUserGroups(Set<UserGroupInfo> groups) {
        return groups.stream()
                .map(group -> new ImportUserRequest.UserGroup(group.getGroupName(), toAccessRealm(group.getRealm())))
                .collect(Collectors.toSet());
    }


    private static boolean isUpdate(Object builder) {
        return (builder instanceof UpdateUserRequest);
    }
}
