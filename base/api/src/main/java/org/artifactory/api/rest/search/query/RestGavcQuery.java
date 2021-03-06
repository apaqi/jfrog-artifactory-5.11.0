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

package org.artifactory.api.rest.search.query;

/**
 * REST API GAVC query object
 *
 * @author Noam Y. Tenne
 */
public class RestGavcQuery extends BaseRestQuery {

    private String groupId;
    private String artifactId;
    private String version;
    private String classifier;

    /**
     * Default constructor
     */
    public RestGavcQuery() {
    }

    /**
     * Returns the group ID to search for
     *
     * @return Group ID to search for
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * Sets the group ID to search for
     *
     * @param groupId Group ID to search for
     */
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    /**
     * Returns the artifact ID to search for
     *
     * @return Artifact ID to search for
     */
    public String getArtifactId() {
        return artifactId;
    }

    /**
     * Sets the artifact ID to search for
     *
     * @param artifactId Artifact ID to search for
     */
    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    /**
     * Returns the version to search for
     *
     * @return Version to search for
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the version to search for
     *
     * @param version Version to search for
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Returns the classifier to search for
     *
     * @return Classifier to search for
     */
    public String getClassifier() {
        return classifier;
    }

    /**
     * Sets the classifier to search for
     *
     * @param classifier Classifier to search for
     */
    public void setClassifier(String classifier) {
        this.classifier = classifier;
    }
}