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

package org.artifactory.ui.rest.model.admin.configuration.repository.distribution.rule;

import org.artifactory.api.bintray.distribution.rule.DistributionRuleToken;
import org.artifactory.md.Properties;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.util.JsonUtil;

import java.util.List;
import java.util.Map;

/**
 * @author Dan Feldman
 */
public class DistributionRuleTokenModel extends DistributionRuleToken implements RestModel {

    public DistributionRuleTokenModel() {

    }

    public DistributionRuleTokenModel(DistributionRuleToken distToken) {
        this.token = distToken.getToken();
        this.value = distToken.getValue();
    }

    @Override
    public String getToken() {
        return token;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void populateValue(RepoPath path, Properties pathProperties) {
        //No need to set value on the model itself.
    }

    @Override
    public void populateValue(RepoPath path, Map<String, List<String>> pathProperties) throws Exception {

    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
