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
package org.artifactory.repo.remote.browse;

import com.google.common.collect.Lists;
import com.google.common.net.InternetDomainName;
import org.artifactory.common.ConstantValues;
import org.artifactory.util.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class which helps to list urls under a given url. This has been tested with Apache 1.3.33 server listing, as
 * the one used at ibiblio, and with Apache 2.0.53 server listing, as the one on mirrors.sunsite.dk.
 */
public class HtmlRepositoryBrowser extends RemoteRepositoryBrowser {
    private final InternetDomainName bintrayDomain;
    private static final Logger log = LoggerFactory.getLogger(HtmlRepositoryBrowser.class);

    private static final Pattern PATTERN = Pattern.compile(
            "<a[^>]*href=[\"|']([^\"']*)[\"|'][^>]*>(?:<[^>]+>)*?([^<>]+?)(?:<[^>]+>)*?</a>",
            Pattern.CASE_INSENSITIVE);

    public HtmlRepositoryBrowser(HttpExecutor client) {
        super(client);
        bintrayDomain = getBintrayDomain();
    }

    @Override
    public List<RemoteItem> listContent(String url) throws IOException {
        String result = getFileListContent(url);
        return parseHtml(result, url);
    }

    List<RemoteItem> parseHtml(String htmlText, String url) throws MalformedURLException {
        List<RemoteItem> items = Lists.newArrayList();
        Matcher matcher = PATTERN.matcher(htmlText);
        URL baseUrl = new URL(url);
        while (matcher.find()) {
            // get the href text and the displayed text
            String href = matcher.group(1);
            String text = matcher.group(2);

            if ((href == null) || (text == null)) {
                // the groups were not found (shouldn't happen, really)
                continue;
            }

            text = text.trim();

            // handle complete URL listings
            if (href.startsWith("http:") || href.startsWith("https:")) {
                try {
                    href = new URL(href).getPath();
                    if (!href.startsWith(baseUrl.getPath())) {
                        // ignore URLs which aren't children of the base URL
                        continue;
                    }
                    href = href.substring(baseUrl.getPath().length());
                } catch (Exception ignore) {
                    // incorrect URL, ignore
                    continue;
                }
            }

            if (href.startsWith("../")) {
                // we are only interested in sub-URLs, not parent URLs, so skip this one
                continue;
            }

            // absolute href: convert to relative one
            if (href.startsWith("/")) {
                int slashIndex = href.substring(0, href.length() - 1).lastIndexOf('/');
                href = href.substring(slashIndex + 1);
            }

            // relative to current href: convert to simple relative one
            if (href.startsWith("./")) {
                href = href.substring("./".length());
            }

            if (isBintray(baseUrl.getHost())) {
                href = new String(text);
            }

            // exclude those where they do not match
            // href will never be truncated, text may be truncated by apache
            if (text.endsWith("..>")) {
                // text is probably truncated, we can only check if the href starts with text
                if (!href.startsWith(text.substring(0, text.length() - 3))) {
                    continue;
                }
            } else if (text.endsWith("..&gt;")) {
                // text is probably truncated, we can only check if the href starts with text
                if (!href.startsWith(text.substring(0, text.length() - 6))) {
                    continue;
                }
            } else {
                // text is not truncated, so it must match the url after stripping optional
                // trailing slashes
                String strippedHref = href.endsWith("/") ? href.substring(0, href.length() - 1) : href;
                String strippedText = text.endsWith("/") ? text.substring(0, text.length() - 1) : text;
                if (!HttpUtils.decodeUri(strippedHref).equalsIgnoreCase(strippedText)) {
                    continue;
                }
            }

            boolean directory = href.endsWith("/");
            String childUrl = baseUrl.toExternalForm();
            if (!childUrl.endsWith("/")) {
                childUrl += "/";
            }
            childUrl += href;
            items.add(new RemoteItem(childUrl, directory));
            log.debug("Found remote item: {}", childUrl);
        }

        return items;
    }

    private boolean isBintray(String url) {
        try {
            return bintrayDomain.equals(InternetDomainName.from(url).topPrivateDomain());
        } catch (Exception e) {
            log.debug("url : " + url + " is not under public domain");
        }
        return false;
    }

    private InternetDomainName getBintrayDomain() {
        String bintrayHost;
        try {
            bintrayHost = new URL(ConstantValues.bintrayUrl.getString()).getHost();
        } catch (MalformedURLException e) {
            log.error(String.format("Failed to parse bintray URL '%s' falling back to bintray.com",
                    ConstantValues.bintrayUrl.getString()));
            bintrayHost = "bintray.com";
        }
        return InternetDomainName.from(bintrayHost);
    }
}
