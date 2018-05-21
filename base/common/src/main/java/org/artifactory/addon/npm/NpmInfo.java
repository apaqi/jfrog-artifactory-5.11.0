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

package org.artifactory.addon.npm;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Chen Keinan
 */
public class NpmInfo {

    private static final Pattern GIT_URL = Pattern.compile(
            "(?:https?:\\/\\/|git(?::\\/\\/|@))(gist.github.com|github.com)[:\\/](.*?)(?:.git)?$");

    private String name;
    private String version;
    private String license;
    private String keywords;
    private String description;
    private String repository;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRepository() {
        return repository;
    }

    public void setRepository(String repository) {
        if (repository != null) {
            Matcher matcher = GIT_URL.matcher(repository);
            if (matcher.matches()) {
                repository = "https://" + matcher.group(1) + "/" + matcher.group(2);
            }
            this.repository = repository;
        }
    }
}
