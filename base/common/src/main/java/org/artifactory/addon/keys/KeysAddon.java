package org.artifactory.addon.keys;

import org.artifactory.addon.Addon;
import org.artifactory.addon.license.EnterprisePlusAddon;
import org.artifactory.keys.TrustedKey;
import org.artifactory.sapi.common.ExportSettings;
import org.artifactory.sapi.common.ImportSettings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

/**
 * @author Rotem Kfir
 */
@EnterprisePlusAddon
public interface KeysAddon extends Addon {

    @Nonnull
    List<TrustedKey> findAllTrustedKeys();

    Optional<TrustedKey> findTrustedKeyById(String kid);

    Optional<TrustedKey> findTrustedKeyByAlias(String alias);

    TrustedKey createTrustedKey(String key, @Nullable String alias);

    void deleteTrustedKey(String kid);

    void exportTo(ExportSettings settings);

    void importFrom(ImportSettings settings);
}
