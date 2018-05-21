/*
 *
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2016 JFrog Ltd.
 *
 * Artifactory is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Artifactory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Artifactory.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.artifactory.ui.rest.model.replication;

import org.artifactory.rest.common.model.BaseModel;

/**
 * @author gidis
 */
public class GlobalReplicationConfig extends BaseModel {
    private boolean blockPullReplications;

    private boolean blockPushReplications;

    public boolean isBlockPullReplications() {
        return blockPullReplications;
    }

    public void setBlockPullReplications(boolean blockPullReplications) {
        this.blockPullReplications = blockPullReplications;
    }

    public boolean isBlockPushReplications() {
        return blockPushReplications;
    }

    public void setBlockPushReplications(boolean blockPushReplications) {
        this.blockPushReplications = blockPushReplications;
    }
}
