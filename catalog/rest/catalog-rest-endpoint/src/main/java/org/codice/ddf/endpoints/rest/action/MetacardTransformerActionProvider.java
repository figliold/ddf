/**
 * Copyright (c) Codice Foundation
 * <p>
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details. A copy of the GNU Lesser General Public License
 * is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package org.codice.ddf.endpoints.rest.action;

import static org.codice.ddf.endpoints.rest.RESTService.CONTEXT_ROOT;
import static org.codice.ddf.endpoints.rest.RESTService.SOURCES_PATH;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.commons.lang.CharEncoding;
import org.codice.ddf.catalog.actions.AbstractMetacardActionProvider;
import org.codice.ddf.configuration.SystemBaseUrl;

import ddf.action.Action;
import ddf.action.ActionProvider;
import ddf.action.impl.ActionImpl;
import ddf.catalog.data.Metacard;

public class MetacardTransformerActionProvider extends AbstractMetacardActionProvider {

    static final String DESCRIPTION_PREFIX =
            "Provides a URL to the metacard that transforms the return value via the ";

    static final String DESCRIPTION_SUFFIX = " transformer";

    static final String TITLE_PREFIX = "Export as ";

    private String metacardTransformerId;

    /**
     * Constructor to instantiate this Metacard {@link ActionProvider}
     *
     * @param actionProviderId
     * @param metacardTransformerId
     */
    public MetacardTransformerActionProvider(String actionProviderId,
            String metacardTransformerId) {
        super(actionProviderId,
                TITLE_PREFIX + metacardTransformerId,
                DESCRIPTION_PREFIX + metacardTransformerId + DESCRIPTION_SUFFIX);
        this.metacardTransformerId = metacardTransformerId;
    }

    @Override
    protected URL getMetacardActionUrl(String metacardSource, Metacard metacard)
            throws IOException {
        String encodedMetacardId = URLEncoder.encode(metacard.getId(), CharEncoding.UTF_8);
        String encodedMetacardSource = URLEncoder.encode(metacardSource, CharEncoding.UTF_8);
        return getActionUrl(encodedMetacardSource, encodedMetacardId);
    }

    protected Action createMetacardAction(String actionProviderId, String title, String description,
            URL url) {
        return new ActionImpl(actionProviderId, title, description, url);
    }

    private URL getActionUrl(String metacardSource, String metacardId)
            throws MalformedURLException {
        return new URL(SystemBaseUrl.constructUrl(String.format("%s%s/%s/%s?transform=%s",
                CONTEXT_ROOT,
                SOURCES_PATH,
                metacardSource,
                metacardId,
                metacardTransformerId), true));
    }
}
