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

package org.artifactory.addon.replication;

import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.testng.annotations.Test;

import java.io.StringWriter;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Noam Y. Tenne
 */
@Test
public class RemoteReplicationSettingsTest {

    public void testConstructor() {
        RepoPath repoPath = InternalRepoPathFactory.create("momo", "pop");
        StringWriter responseWriter = new StringWriter();
        RemoteReplicationSettings settings = new RemoteReplicationSettings(repoPath, true, 15, true,
                true, ReplicationAddon.Overwrite.never, responseWriter, "momo.com", 100);

        assertEquals(settings.getRepoPath(), repoPath, "Unexpected repo path.");
        assertTrue(settings.isProgress(), "Unexpected progress display state.");
        assertEquals(settings.getMark(), 15, "Unexpected mark.");
        assertTrue(settings.isDeleteExisting(), "Unexpected delete existing state.");
        assertTrue(settings.isIncludeProperties(), "Unexpected property inclusion state.");
        assertEquals(settings.getOverwrite(), ReplicationAddon.Overwrite.never,
                "Unexpected overwrite switch state.");
        assertEquals(settings.getResponseWriter(), responseWriter, "Unexpected response writer.");
        assertEquals(settings.getUrl(), "momo.com", "Unexpected target URL.");
        assertEquals(settings.getSocketTimeoutMillis(), 100, "Unexpected socket timeout.");
    }
}
