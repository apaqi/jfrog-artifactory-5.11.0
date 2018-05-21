package org.artifactory.ui.rest.service.admin.security.user.userprofile;

import org.artifactory.security.UserInfo;

/**
 * @author Tamir Hadad
 */
public interface UserProfileHelperService {

    boolean authenticate(UserInfo userInfo, String enteredCurrentPassword);

    UserInfo loadUserInfo();

}
