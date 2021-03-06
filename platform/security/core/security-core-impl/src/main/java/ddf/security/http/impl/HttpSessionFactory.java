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
package ddf.security.http.impl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import ddf.security.SecurityConstants;
import ddf.security.common.util.SecurityTokenHolder;
import ddf.security.http.SessionFactory;

public class HttpSessionFactory implements SessionFactory {

    /**
     * Synchronized method because of jettys getSession method is not thread safe. Additionally,
     * assures a SAML {@link SecurityTokenHolder} has been set on the {@link SecurityConstants#SAML_ASSERTION} attribute
     *
     * @param httpRequest
     * @return
     */
    @Override
    public synchronized HttpSession getOrCreateSession(HttpServletRequest httpRequest) {
        HttpSession session = httpRequest.getSession(true);
        if (session.getAttribute(SecurityConstants.SAML_ASSERTION) == null) {
            session.setAttribute(SecurityConstants.SAML_ASSERTION, new SecurityTokenHolder());
        }
        return session;
    }
}
