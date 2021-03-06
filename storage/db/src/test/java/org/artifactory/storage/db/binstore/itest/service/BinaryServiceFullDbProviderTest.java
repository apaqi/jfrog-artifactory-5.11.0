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

package org.artifactory.storage.db.binstore.itest.service;

import org.artifactory.common.ArtifactoryHome;
import org.artifactory.storage.binstore.service.BinaryInfo;
import org.jfrog.common.ResourceUtils;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Date: 12/10/12
 * Time: 9:54 PM
 *
 * @author freds
 */
@Test
public class BinaryServiceFullDbProviderTest extends BinaryServiceBaseTest {

    @Override
    protected String getBinaryStoreContent() {
        return
                "<config version=\"2\"> " +
                        "<chain template=\"full-db\"/>" +
                        "   <provider id=\"cache-fs\" type=\"cache-fs\">" +
                        "      <binariesDir>##baseDataDir##</binariesDir>" +
                        "   </provider>" +
                        "</config>";
    }

    @Override
    protected String getBinaryStoreDirName() {
        return new File(ArtifactoryHome.get().getDataDir(), "cache_test").getAbsolutePath();
    }

    @Override
    protected void assertBinaryExistsEmpty(String sha1) {
        defaultAssertBinaryExistsEmpty(sha1);
    }

    @Override
    protected BinaryInfo addBinary(String resName, String sha1, String sha2, String md5, long length) throws IOException {
        try (InputStream resource = ResourceUtils.getResource("/binstore/" + resName)) {
            return binaryStore.addBinary(binaryStore.createBinaryStream(resource, null));
        }
    }

    // TODO [By Gidi] check this change with Fred or Yossi
    @Override
    protected void testPruneAfterLoad() {
        Object[][] binFileData = getBinFileData();
        int totSize = 0;
//        for (Object[] objects : binFileData) {
//            totSize += ((Long) objects[3]).intValue();
//        }
        testPrune(0, 0, totSize);
    }

    @Override
    protected void assertPruneAfterOneGc() {
        testPrune(0, 0, 0);
    }
}
