package org.artifactory.api.security.ldap;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.naming.directory.Attributes;

@Data
@AllArgsConstructor
public class LdapUserAttributes {

    private LdapUser ldapUser;
    private Attributes attributes;
}
