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
package org.codice.ddf.security.handler.saml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;

import org.apache.cxf.staxutils.StaxUtils;
import org.apache.cxf.ws.security.tokenstore.SecurityToken;
import org.codice.ddf.security.common.HttpUtils;
import org.codice.ddf.security.common.jaxrs.RestSecurity;
import org.codice.ddf.security.handler.api.AuthenticationHandler;
import org.codice.ddf.security.handler.api.HandlerResult;
import org.codice.ddf.security.handler.api.SAMLAuthenticationToken;
import org.codice.ddf.security.policy.context.ContextPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import ddf.security.SecurityConstants;
import ddf.security.assertion.impl.SecurityAssertionImpl;
import ddf.security.common.util.SecurityTokenHolder;
import ddf.security.http.SessionFactory;

/**
 * Checks for a SAML assertion that has been returned to us in the ddf security cookie. If it exists, it
 * is retrieved and converted into a SecurityToken.
 */
public class SAMLAssertionHandler implements AuthenticationHandler {
    /**
     * SAML type to use when configuring context policy.
     */
    public static final String AUTH_TYPE = "SAML";

    private static final Logger LOGGER = LoggerFactory.getLogger(SAMLAssertionHandler.class);

    private static final String SAML_NAMESPACE = "urn:oasis:names:tc:SAML:2.0:assertion";

    private static final String EVIDENCE = "<%1$s:Evidence xmlns:%1$s=\"urn:oasis:names:tc:SAML:2.0:assertion\">%2$s</%1$s:Evidence>";

    private static final Pattern SAML_PREFIX = Pattern.compile("<(?<prefix>\\w+?):Assertion\\s.*");

    private SessionFactory sessionFactory;

    public SAMLAssertionHandler() {
        LOGGER.debug("Creating SAML Assertion handler.");
    }

    @Override
    public String getAuthenticationType() {
        return AUTH_TYPE;
    }

    @Override
    public HandlerResult getNormalizedToken(ServletRequest request, ServletResponse response,
            FilterChain chain, boolean resolve) {
        HandlerResult handlerResult = new HandlerResult();
        String realm = (String) request.getAttribute(ContextPolicy.ACTIVE_REALM);

        SecurityToken securityToken;
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String authHeader = ((HttpServletRequest) request)
                .getHeader(SecurityConstants.SAML_HEADER_NAME);

        // check for full SAML assertions coming in (federated requests, etc.)
        if (authHeader != null) {
            String[] tokenizedAuthHeader = authHeader.split(" ");
            if (tokenizedAuthHeader.length == 2 && tokenizedAuthHeader[0].equals("SAML")) {
                String encodedSamlAssertion = tokenizedAuthHeader[1];
                LOGGER.trace("Header retrieved");
                try {
                    String tokenString = RestSecurity.inflateBase64(encodedSamlAssertion);
                    LOGGER.trace("Header value: {}", tokenString);
                    securityToken = new SecurityToken();

                    Element thisToken = null;

                    if (tokenString.contains(SAML_NAMESPACE)) {
                        try {
                            thisToken = StaxUtils.read(new StringReader(tokenString))
                                    .getDocumentElement();
                        } catch (XMLStreamException e) {
                            LOGGER.warn(
                                    "Unexpected error converting XML string to element - proceeding without SAML token.",
                                    e);
                        }
                    } else {
                        thisToken = parseAssertionWithoutNamespace(tokenString);
                    }

                    securityToken.setToken(thisToken);
                    SAMLAuthenticationToken samlToken = new SAMLAuthenticationToken(null,
                            securityToken, realm);
                    handlerResult.setToken(samlToken);
                    handlerResult.setStatus(HandlerResult.Status.COMPLETED);
                } catch (IOException e) {
                    LOGGER.warn("Unexpected error converting header value to string", e);
                }
                return handlerResult;
            }
        }

        // Check for legacy SAML cookie
        Map<String, Cookie> cookies = HttpUtils.getCookieMap(httpRequest);
        Cookie samlCookie = cookies.get(SecurityConstants.SAML_COOKIE_NAME);
        if (samlCookie != null) {
            String cookieValue = samlCookie.getValue();
            LOGGER.trace("Cookie retrieved");
            try {
                String tokenString = RestSecurity.inflateBase64(cookieValue);
                LOGGER.trace("Cookie value: {}", tokenString);
                securityToken = new SecurityToken();
                Element thisToken = StaxUtils.read(new StringReader(tokenString))
                        .getDocumentElement();
                securityToken.setToken(thisToken);
                SAMLAuthenticationToken samlToken = new SAMLAuthenticationToken(null, securityToken,
                        realm);
                handlerResult.setToken(samlToken);
                handlerResult.setStatus(HandlerResult.Status.COMPLETED);
            } catch (IOException e) {
                LOGGER.warn(
                        "Unexpected error converting cookie value to string - proceeding without SAML token.",
                        e);
            } catch (XMLStreamException e) {
                LOGGER.warn(
                        "Unexpected error converting XML string to element - proceeding without SAML token.",
                        e);
            }
            return handlerResult;
        }

        HttpSession session = httpRequest.getSession(false);
        if (session == null && httpRequest.getRequestedSessionId() != null) {
            session = sessionFactory.getOrCreateSession(httpRequest);
        }
        if (session != null) {
            //Check if there is a SAML Assertion in the session
            //If so, create a SAMLAuthenticationToken using the sessionId
            SecurityTokenHolder savedToken = (SecurityTokenHolder) session
                    .getAttribute(SecurityConstants.SAML_ASSERTION);
            if (savedToken != null && savedToken.getSecurityToken(realm) != null) {
                SecurityAssertionImpl assertion = new SecurityAssertionImpl(
                        savedToken.getSecurityToken(realm));
                if (assertion.getNotOnOrAfter() == null
                        || assertion.getNotOnOrAfter().getTime() - System.currentTimeMillis() > 0) {
                    LOGGER.trace("Creating SAML authentication token with session.");
                    SAMLAuthenticationToken samlToken = new SAMLAuthenticationToken(null,
                            session.getId(), realm);
                    handlerResult.setToken(samlToken);
                    handlerResult.setStatus(HandlerResult.Status.COMPLETED);
                    return handlerResult;
                } else {
                    LOGGER.trace(
                            "SAML token in session has expired - removing from session and returning with no results");
                    savedToken.remove(realm);
                }
            } else {
                LOGGER.trace("No SAML token located in session - returning with no results");
            }
        } else {
            LOGGER.trace("No HTTP Session - returning with no results");
        }

        return handlerResult;
    }

    private Element parseAssertionWithoutNamespace(String assertion) {
        Element result = null;

        Matcher prefix = SAML_PREFIX.matcher(assertion);
        if (prefix.find()) {

            Thread thread = Thread.currentThread();
            ClassLoader loader = thread.getContextClassLoader();
            thread.setContextClassLoader(SAMLAssertionHandler.class.getClassLoader());

            try {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                dbf.setNamespaceAware(true);

                String evidence = String.format(EVIDENCE, prefix.group("prefix"), assertion);

                Element root = dbf.newDocumentBuilder()
                        .parse(new ByteArrayInputStream(evidence.getBytes())).getDocumentElement();

                result = ((Element) root.getChildNodes().item(0));
            } catch (ParserConfigurationException | SAXException | IOException ex) {
                LOGGER.warn("Unable to parse SAML assertion", ex);
            } finally {
                thread.setContextClassLoader(loader);
            }
        }

        return result;
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * If an error occured during the processing of the request, this method will get called. Since
     * SAML handling is typically processed first, then we can assume that there was an error with
     * the presented SAML assertion - either it was invalid, or the reference didn't match a
     * cached assertion, etc. In order not to get stuck in a processing loop, we will return a 401
     * status code.
     *
     * @param servletRequest  http servlet request
     * @param servletResponse http servlet response
     * @param chain           rest of the request chain to be invoked after security handling
     * @return result containing the potential credentials and status
     * @throws ServletException
     */
    @Override
    public HandlerResult handleError(ServletRequest servletRequest, ServletResponse servletResponse,
            FilterChain chain) throws ServletException {
        HandlerResult result = new HandlerResult();

        HttpServletRequest httpRequest = servletRequest instanceof HttpServletRequest ?
                (HttpServletRequest) servletRequest :
                null;
        HttpServletResponse httpResponse = servletResponse instanceof HttpServletResponse ?
                (HttpServletResponse) servletResponse :
                null;
        if (httpRequest == null || httpResponse == null) {
            return result;
        }

        LOGGER.debug(
                "In error handler for saml - setting status code to 401 and returning status REDIRECTED.");

        // we tried to process an invalid or missing SAML assertion
        try {
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.flushBuffer();
        } catch (IOException e) {
            LOGGER.debug("Failed to send auth response", e);
        }
        result.setStatus(HandlerResult.Status.REDIRECTED);
        return result;

    }
}
