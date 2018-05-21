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

package org.artifactory.ui.rest.service.admin.advanced.systemlogs;

import org.apache.commons.lang.StringUtils;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.rest.common.service.StreamRestResponse;
import org.artifactory.ui.rest.model.admin.advanced.systemlogs.SystemLogFile;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * @author Lior Hasson
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetSysLogDownloadLinkService implements RestService {
    private File logDir = ContextHelper.get().getArtifactoryHome().getLogDir();

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        updateResponseWithLogFile(request, response);
    }

    /**
     * update response with the log data file
     *
     * @param artifactoryRequest  - encapsulate data related to the request
     * @param artifactoryResponse - encapsulate data related to the response
     */
    private void updateResponseWithLogFile(ArtifactoryRestRequest artifactoryRequest,
            RestResponse artifactoryResponse) {
        String selectedLog = artifactoryRequest.getQueryParamByKey("id");
        if (StringUtils.isEmpty(selectedLog)) {
            artifactoryResponse.error("Log file name (id) cannot be empty");
            return;
        }

        if (selectedLog.contains("..")) {
            artifactoryResponse.error("Log file name (id) cannot contain relative paths");
            return;
        }

        File systemLogFile = new File(logDir, selectedLog);
        if (!systemLogFile.exists()) {
            artifactoryResponse.error("Log file name (id) not found: " + selectedLog);
            return;
        }

        ((StreamRestResponse) artifactoryResponse).setDownloadFile(systemLogFile.getName());
        ((StreamRestResponse) artifactoryResponse).setDownload(true);
        SystemLogFile logFileModel = new SystemLogFile();

        try {
            FileInputStream stream = new FileInputStream(systemLogFile);
            logFileModel.setStream(stream);
            artifactoryResponse.iModel(logFileModel);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
