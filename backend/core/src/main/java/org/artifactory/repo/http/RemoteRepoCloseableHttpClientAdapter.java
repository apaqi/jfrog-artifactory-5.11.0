/*
 * Copyright (c) 2018. JFrog Ltd. All rights reserved. JFROG PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 */

package org.artifactory.repo.http;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.artifactory.repo.HttpRepo;
import org.artifactory.repo.service.InternalRepositoryService;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Adapter for using HttpRepo as if it was an http client.
 * That way a simple testable code used both by the repo and minimal unittest.
 */
public class RemoteRepoCloseableHttpClientAdapter extends CloseableHttpClient {

    public final URI uri;
    public final HttpHost host;
    private final HttpRepo remoteRepo;
    private CloseableHttpResponse resp;

    public RemoteRepoCloseableHttpClientAdapter(HttpRepo remoteRepo) {
        this.remoteRepo = remoteRepo;
        try {
            this.uri = new URI(remoteRepo.getUrl());
            this.host = new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme());
        } catch (URISyntaxException e) {
            throw new RuntimeException("Not valid HttpRepo", e);
        }
    }

    public static CloseableHttpClient clientOf(InternalRepositoryService repositryService, String repoKey) {

        HttpRepo remoteRepo = (HttpRepo) repositryService.remoteRepositoryByKey(repoKey);
        return clientOf(remoteRepo);
    }

    public static CloseableHttpClient clientOf(HttpRepo remoteRepo) {
        return new RemoteRepoCloseableHttpClientAdapter(remoteRepo);
    }

    @Override
    protected CloseableHttpResponse doExecute(HttpHost httpHost, HttpRequest httpRequest, HttpContext httpContext)
            throws IOException {
        HttpRequestBase asBase = (HttpRequestBase) httpRequest;
        resp = remoteRepo.executeMethod(asBase, httpContext);
        return resp;
    }

    @Override
    public void close() throws IOException {
        remoteRepo.close();
    }

    @Override
    public HttpParams getParams() {
        return null;
    }

    @Override
    public ClientConnectionManager getConnectionManager() {
        return null;
    }
}
