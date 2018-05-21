package org.artifactory.addon.go;

/**
 * Specifies the git provider
 *
 * @author Liz Dashevski
 */
public enum GoGitProvider {
    Github("github.com/"),
    BitBucket("bitbucket.org/"), //unsupported by vgo for now
    Gopkgin("gopkg.in/"),
    GoGoogleSource("go.googlesource.com/");

    public String prefix;

    GoGitProvider(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public String toString() {
        return prefix;
    }
}

