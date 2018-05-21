package org.artifactory.security.ldap;

import org.easymock.EasyMock;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.support.LdapContextSource;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import java.util.Hashtable;

/**
 * @author Inbar Tal
 */
public class LdapContextSourceMock extends LdapContextSource {

    private static final String USER_NAME_WITH_LIST_CONTENTS_FULL_DN = "uid=userListContents,ou=People,dc=jfrog,dc=org";
    private static final String USER_NAME_WITHOUT_LIST_CONTENTS_FULL_DN = "uid=userWithoutListContents,ou=People,dc=jfrog,dc=org";
    private static final String USER_NAME_WITH_LIST_CONTENTS_DN = "uid=userListContents,ou=People";
    private static final String USER_NAME_WITHOUT_LIST_CONTENTS_DN = "uid=userWithoutListContents,ou=People";

    protected DirContext getDirContextInstance(Hashtable environment)
            throws NamingException {
        return new InitialLdapContext(environment, null);
    }

    @Override
    public DirContext getContext(String principalFullDn, String password) {
        LdapContext ctx = EasyMock.createMock(LdapContext.class);
        //if we have list contents permission
        Attributes attrs = new BasicAttributes("uid","userId");
        try {
            EasyMock.expect(ctx.getResponseControls()).andReturn(null).anyTimes();
            //we expect to call to getAttributes only in case of direct login to LDAP.(login with user permissions)
            if (principalFullDn.equals(USER_NAME_WITH_LIST_CONTENTS_FULL_DN)) {
                EasyMock.expect(ctx.getAttributes(new DistinguishedName(USER_NAME_WITH_LIST_CONTENTS_DN), null))
                        .andReturn(attrs);
            } else if (principalFullDn.equals(USER_NAME_WITHOUT_LIST_CONTENTS_FULL_DN)) {
                EasyMock.expect(ctx.getAttributes(new DistinguishedName(USER_NAME_WITHOUT_LIST_CONTENTS_DN), null))
                        .andThrow(new Exception("user don't have list contents permission"));
            }
        } catch (NamingException e) {
            e.printStackTrace();
        }
        EasyMock.replay(ctx);
        return ctx;
    }
}
