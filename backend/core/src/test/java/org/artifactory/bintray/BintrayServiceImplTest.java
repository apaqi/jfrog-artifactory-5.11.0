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

package org.artifactory.bintray;

import org.apache.commons.io.IOUtils;
import org.jfrog.common.ResourceUtils;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Shay Yaakov
 */
@Test
public class BintrayServiceImplTest {

    public void testGetPackagesToDeploy() throws IOException {
        BintrayServiceImpl bintrayService = new BintrayServiceImpl();
        InputStream inputStream = ResourceUtils.getResource("/org/artifactory/bintray/packagesToDeploy.json");
        byte[] bytes = IOUtils.toByteArray(inputStream);
        List<String> packagesList = ReflectionTestUtils.invokeMethod(bintrayService, "getPackagesList", bytes);
        assertEquals(packagesList.size(), 2, "Expected 2 packages");
        assertTrue(packagesList.contains("package2"), "Expected to find package1");
        assertTrue(packagesList.contains("package3"), "Expected to find package2");
    }
}
