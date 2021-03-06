/**
 * Copyright (c) Codice Foundation
 * <p/>
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details. A copy of the GNU Lesser General Public License
 * is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package org.codice.ddf.security.validator.anonymous;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import org.apache.cxf.sts.request.ReceivedToken;
import org.apache.cxf.sts.token.validator.TokenValidatorParameters;
import org.apache.cxf.sts.token.validator.TokenValidatorResponse;
import org.apache.cxf.ws.security.sts.provider.model.secext.BinarySecurityTokenType;
import org.codice.ddf.security.handler.api.AnonymousAuthenticationToken;
import org.codice.ddf.security.handler.api.BSTAuthenticationToken;
import org.junit.Before;
import org.junit.Test;
import org.opensaml.xml.util.Base64;

import ddf.security.principal.AnonymousPrincipal;

public class AnonymousValidatorTest {

    ReceivedToken receivedToken;

    ReceivedToken receivedBadToken;

    ReceivedToken receivedTokenIpv6;

    ReceivedToken receivedTokenBadIp;

    AnonymousValidator validator;

    TokenValidatorParameters parameters;

    ReceivedToken receivedAnyRealmToken;

    @Before
    public void setup() {
        validator = new AnonymousValidator();
        validator.setSupportedRealm(Arrays.asList("DDF"));
        AnonymousAuthenticationToken anonymousAuthenticationToken = new AnonymousAuthenticationToken(
                "DDF", "127.0.0.1");

        AnonymousAuthenticationToken anonymousAuthenticationTokenAnyRealm = new AnonymousAuthenticationToken(
                "*", "127.0.0.1");

        AnonymousAuthenticationToken anonymousAuthenticationTokenIpv6 = new AnonymousAuthenticationToken(
                "*", "0:0:0:0:0:0:0:1");

        AnonymousAuthenticationToken anonymousAuthenticationTokenBadIp = new AnonymousAuthenticationToken(
                "*", "123.abc.45.def");

        BinarySecurityTokenType binarySecurityTokenType = new BinarySecurityTokenType();
        binarySecurityTokenType
                .setValueType(AnonymousAuthenticationToken.ANONYMOUS_TOKEN_VALUE_TYPE);
        binarySecurityTokenType.setEncodingType(BSTAuthenticationToken.BASE64_ENCODING);
        binarySecurityTokenType.setId(AnonymousAuthenticationToken.BST_ANONYMOUS_LN);
        binarySecurityTokenType.setValue(anonymousAuthenticationToken.getEncodedCredentials());
        JAXBElement<BinarySecurityTokenType> binarySecurityTokenElement = new JAXBElement<BinarySecurityTokenType>(
                new QName(
                        "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd",
                        "BinarySecurityToken"), BinarySecurityTokenType.class,
                binarySecurityTokenType);

        BinarySecurityTokenType binarySecurityTokenType2 = new BinarySecurityTokenType();
        binarySecurityTokenType2
                .setValueType(AnonymousAuthenticationToken.ANONYMOUS_TOKEN_VALUE_TYPE);
        binarySecurityTokenType2.setEncodingType(BSTAuthenticationToken.BASE64_ENCODING);
        binarySecurityTokenType2.setId(AnonymousAuthenticationToken.BST_ANONYMOUS_LN);
        binarySecurityTokenType2
                .setValue(Base64.encodeBytes("NotAnonymous".getBytes(), Base64.DONT_BREAK_LINES));
        JAXBElement<BinarySecurityTokenType> binarySecurityTokenElement2 = new JAXBElement<BinarySecurityTokenType>(
                new QName(
                        "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd",
                        "BinarySecurityToken"), BinarySecurityTokenType.class,
                binarySecurityTokenType2);

        BinarySecurityTokenType binarySecurityTokenType3 = new BinarySecurityTokenType();
        binarySecurityTokenType3
                .setValueType(AnonymousAuthenticationToken.ANONYMOUS_TOKEN_VALUE_TYPE);
        binarySecurityTokenType3.setEncodingType(BSTAuthenticationToken.BASE64_ENCODING);
        binarySecurityTokenType3.setId(AnonymousAuthenticationToken.BST_ANONYMOUS_LN);
        binarySecurityTokenType3
                .setValue(anonymousAuthenticationTokenAnyRealm.getEncodedCredentials());
        JAXBElement<BinarySecurityTokenType> binarySecurityTokenElement3 = new JAXBElement<BinarySecurityTokenType>(
                new QName(
                        "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd",
                        "BinarySecurityToken"), BinarySecurityTokenType.class,
                binarySecurityTokenType3);

        BinarySecurityTokenType binarySecurityTokenType4 = new BinarySecurityTokenType();
        binarySecurityTokenType4
                .setValueType(AnonymousAuthenticationToken.ANONYMOUS_TOKEN_VALUE_TYPE);
        binarySecurityTokenType4.setEncodingType(BSTAuthenticationToken.BASE64_ENCODING);
        binarySecurityTokenType4.setId(AnonymousAuthenticationToken.BST_ANONYMOUS_LN);
        binarySecurityTokenType4
                .setValue(anonymousAuthenticationTokenIpv6.getEncodedCredentials());
        JAXBElement<BinarySecurityTokenType> binarySecurityTokenElement4 = new JAXBElement<BinarySecurityTokenType>(
                new QName(
                        "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd",
                        "BinarySecurityToken"), BinarySecurityTokenType.class,
                binarySecurityTokenType4);

        BinarySecurityTokenType binarySecurityTokenType5 = new BinarySecurityTokenType();
        binarySecurityTokenType5
                .setValueType(AnonymousAuthenticationToken.ANONYMOUS_TOKEN_VALUE_TYPE);
        binarySecurityTokenType5.setEncodingType(BSTAuthenticationToken.BASE64_ENCODING);
        binarySecurityTokenType5.setId(AnonymousAuthenticationToken.BST_ANONYMOUS_LN);
        binarySecurityTokenType5
                .setValue(anonymousAuthenticationTokenBadIp.getEncodedCredentials());
        JAXBElement<BinarySecurityTokenType> binarySecurityTokenElement5 = new JAXBElement<BinarySecurityTokenType>(
                new QName(
                        "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd",
                        "BinarySecurityToken"), BinarySecurityTokenType.class,
                binarySecurityTokenType5);

        receivedToken = new ReceivedToken(binarySecurityTokenElement);
        receivedAnyRealmToken = new ReceivedToken(binarySecurityTokenElement3);
        receivedBadToken = new ReceivedToken(binarySecurityTokenElement2);
        receivedTokenIpv6 = new ReceivedToken(binarySecurityTokenElement4);
        receivedTokenBadIp = new ReceivedToken(binarySecurityTokenElement5);
        parameters = new TokenValidatorParameters();
        parameters.setToken(receivedToken);
    }

    @Test
    public void testCanHandleToken() throws JAXBException {
        boolean canHandle = validator.canHandleToken(receivedToken);

        assertTrue(canHandle);
    }

    @Test
    public void testCanHandleAnyRealmToken() throws JAXBException {
        boolean canHandle = validator.canHandleToken(receivedAnyRealmToken);

        assertTrue(canHandle);
    }

    @Test
    public void testCanValidateToken() {
        TokenValidatorResponse response = validator.validateToken(parameters);

        assertEquals(ReceivedToken.STATE.VALID, response.getToken().getState());

        assertThat(response.getToken().getPrincipal(), instanceOf(AnonymousPrincipal.class));
    }

    @Test
    public void testCanValidateAnyRealmToken() {
        TokenValidatorParameters params = new TokenValidatorParameters();
        params.setToken(receivedAnyRealmToken);
        TokenValidatorResponse response = validator.validateToken(params);

        assertEquals(ReceivedToken.STATE.VALID, response.getToken().getState());
    }

    @Test
    public void testCanValidateIpv6Token() {
        TokenValidatorParameters params = new TokenValidatorParameters();
        params.setToken(receivedTokenIpv6);
        TokenValidatorResponse response = validator.validateToken(params);

        assertEquals(ReceivedToken.STATE.VALID, response.getToken().getState());
    }

    @Test
    public void testCanValidateBadIpToken() {
        TokenValidatorParameters params = new TokenValidatorParameters();
        params.setToken(receivedTokenBadIp);
        TokenValidatorResponse response = validator.validateToken(params);

        assertEquals(ReceivedToken.STATE.INVALID, response.getToken().getState());
    }

    @Test
    public void testCanValidateBadToken() {
        parameters.setToken(receivedBadToken);
        TokenValidatorResponse response = validator.validateToken(parameters);

        assertEquals(ReceivedToken.STATE.INVALID, response.getToken().getState());
    }
}
