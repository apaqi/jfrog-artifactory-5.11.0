package org.artifactory.addon.replicator;

import org.artifactory.addon.Addon;

/**
 * @author Yoaz Menda
 */
public interface ReplicatorAddon extends Addon {

    ReplicatorDetails register(ReplicatorRegistrationRequest registrationRequest);

    String getExternalUrl();
}
