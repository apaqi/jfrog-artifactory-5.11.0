package org.artifactory.addon.license;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation representing addons that are blocked when using Artifactory edge license
 *
 * @author Nadav Yogev
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EdgeBlockedAddon {
}
