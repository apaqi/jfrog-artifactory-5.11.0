package org.artifactory.ui.rest.service.admin.security.keys;

import org.springframework.beans.factory.annotation.Lookup;

/**
 * @author Inbar Tal
 */
public abstract class TrustedKeysServiceFactory {

    @Lookup
    public abstract GetAllTrustedKeysService getAllTrustedKeys();

    @Lookup
    public abstract GetTrustedKeyService getTrustedKeyById();

    @Lookup
    public abstract CreateTrustedKeyService createTrustedKey();

    @Lookup
    public abstract DeleteTrustedKeysService deleteTrustedKeys();

    @Lookup
    public abstract ValidateTrustedKeyService validateTrustedKey();

}
