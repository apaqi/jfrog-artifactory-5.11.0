package org.artifactory.storage.tx;

/**
 * Empty abstract implementation of the {@link SessionResource} interface to be used as adapter.
 *
 * @author Yossi Shaul
 */
public abstract class SessionResourceAdapter implements SessionResource {
    @Override
    public void beforeCommit() {
        // default empty implementation
    }

    @Override
    public void afterCompletion(boolean commit) {
        // default empty implementation
    }
}
