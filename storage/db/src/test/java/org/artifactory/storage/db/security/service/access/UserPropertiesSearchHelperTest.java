package org.artifactory.storage.db.security.service.access;

import org.artifactory.model.xstream.security.UserImpl;
import org.artifactory.model.xstream.security.UserProperty;
import org.artifactory.security.UserPropertyInfo;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Set;

import static org.artifactory.storage.db.security.service.access.UserPropertiesSearchHelper.matchedValueAndkeySuffix;

public class UserPropertiesSearchHelperTest {
    @Test
    public void testMatchedValueAndkeySuffix() throws Exception {
        UserImpl user = new UserImpl();
        Set<UserPropertyInfo> props = new HashSet<>();
        user.setUserProperties(props);
        props.add(new UserProperty("hello", "world"));
        Assert.assertFalse(matchedValueAndkeySuffix(user, "basicauth", "found"));
        props.add(new UserProperty("basicauth", "found"));
        Assert.assertTrue(matchedValueAndkeySuffix(user, "basicauth", "found"));

        // not found
        Assert.assertFalse(matchedValueAndkeySuffix(user, "basicauth", "notfound"));
        Assert.assertFalse(matchedValueAndkeySuffix(user, "prefix.basicauth", "found"));

        props.clear();
        Assert.assertFalse(matchedValueAndkeySuffix(user, "basicauth", "found"));
        props.add(new UserProperty("prefix.basicauth", "found"));
        Assert.assertTrue(matchedValueAndkeySuffix(user, "basicauth", "found"));
        Assert.assertTrue(matchedValueAndkeySuffix(user, "prefix.basicauth", "found"));
        props.add(new UserProperty("hello", "world"));
        Assert.assertTrue(matchedValueAndkeySuffix(user, "basicauth", "found"));
        Assert.assertTrue(matchedValueAndkeySuffix(user, "prefix.basicauth", "found"));

        props.clear();
        props.add(new UserProperty("hello", "world"));
        Assert.assertFalse(matchedValueAndkeySuffix(user, "basicauth", "also"));
        props.add(new UserProperty("basicauth", "found"));
        props.add(new UserProperty("prefix.basicauth", "also"));
        Assert.assertTrue(matchedValueAndkeySuffix(user, "basicauth", "also"));
        Assert.assertTrue(matchedValueAndkeySuffix(user, "basicauth", "found"));


    }

}