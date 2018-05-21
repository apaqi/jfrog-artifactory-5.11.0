/*
 *
 * Copyright 2016 JFrog Ltd. All rights reserved.
 * JFROG PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package org.artifactory.addon.replication.event;

/**
 * @author Noam Y. Tenne
 */
public enum ReplicationEventType {
    MKDIR,
    DEPLOY,
    DELETE,
    PROPERTY_CHANGE,
    STATS_CHANGE,
    EMPTY
}
