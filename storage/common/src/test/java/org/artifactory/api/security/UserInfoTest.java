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

package org.artifactory.api.security;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.artifactory.model.xstream.security.UserGroupImpl;
import org.artifactory.model.xstream.security.UserImpl;
import org.artifactory.security.MutableUserInfo;
import org.artifactory.security.UserInfo;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.testng.Assert.*;

/**
 * Tests the UserInfo.
 *
 * @author Yossi Shaul
 */
@Test
public class UserInfoTest {

    public void copyConstructor() {
        MutableUserInfo orig = new UserImpl("momo");
        orig.setGenPasswordKey("blablablablabla");
        UserInfo copy = new UserImpl(orig);
        assertTrue(EqualsBuilder.reflectionEquals(orig, copy), "Orig and copy differ");

        orig.setPrivateKey("myprivatekey");
        orig.setPublicKey("mypublickey");
        copy = new UserImpl(orig);
        assertTrue(EqualsBuilder.reflectionEquals(orig, copy),
                "Orig and copy differ after setting public/private keys");
    }

    public void addRemoveGroup() {
        MutableUserInfo userInfo = new UserImpl("momo");
        userInfo.addGroup("mygroup");
        assertTrue(userInfo.getGroups().contains(new UserGroupImpl("mygroup")));
        userInfo.removeGroup("mygroup");
        assertTrue(userInfo.getGroups().isEmpty(), "groups should now be empty");

        Set<String> strings = new HashSet<String>();
        strings.add("mygroup2");
        userInfo.setInternalGroups(strings);
        assertTrue(userInfo.getGroups().contains(new UserGroupImpl("mygroup2")));
    }

    public void getUserProperty() {
        MutableUserInfo userInfo = new UserImpl("momo");
        userInfo.putUserProperty("find", "me");
        Optional<String> value = userInfo.getUserProperty("find");
        assertTrue(value.isPresent());
        assertEquals(value.get(), "me");
    }

    public void getUserPropertyNotExist() {
        MutableUserInfo userInfo = new UserImpl("momo");
        userInfo.putUserProperty("find", "me");
        Optional<String> value = userInfo.getUserProperty("not_exist");
        assertFalse(value.isPresent());
    }

    public void getUserPropertyUserWithNoProperties() {
        MutableUserInfo userInfo = new UserImpl("momo");
        Optional<String> value = userInfo.getUserProperty("not_exist");
        assertFalse(value.isPresent());
    }
}
