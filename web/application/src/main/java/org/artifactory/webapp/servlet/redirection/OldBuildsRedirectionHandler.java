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

package org.artifactory.webapp.servlet.redirection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import static org.artifactory.webapp.servlet.redirection.OldRedirectionUtils.servletPathStartsWith;

/**
 * @author Gidi Shabat
 */
public class OldBuildsRedirectionHandler implements RedirectionHandler {
    private static final Logger log = LoggerFactory.getLogger(OldBuildsRedirectionHandler.class);

    @Override
    public boolean shouldRedirect(ServletRequest request) {
        return servletPathStartsWith(request, "/webapp/builds");
    }

    @Override
    public void redirect(ServletRequest request, ServletResponse response) {
        try {
            OldRedirectionUtils.redirect(request, response, "/webapp/builds", "/webapp/#/builds");
        } catch (Exception e) {
            log.error("Failed to redirect old generation builds page to new generation builds page request.",e);
        }
    }
}
