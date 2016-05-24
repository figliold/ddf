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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import ddf.action.Action;
import ddf.catalog.cache.ResourceCacheInterface;
import ddf.catalog.data.Metacard;

@RunWith(MockitoJUnitRunner.class)
public class CacheOnlyMetacardTransformerActionProviderTest extends AbstractActionProviderTest {

    private static final String ACTION_PROVIDER_ID = "catalog.data.metacard.cache.only";
    
    private static final String METACARD_TRANSFORMER_ID = "resource";
    
    private static final String CACHE_ONLY = "cacheonly";
    
    private static final String BAD_PROTOCOL = "badProtocol://";
    
    private static final String INVALID_IP_ADDRESS = "23^&*#";
    
    private static final String EXPECTED_ACTION_URL = SAMPLE_SECURE_PROTOCOL + SAMPLE_IP + ":" + SAMPLE_SECURE_PORT + SAMPLE_SERVICES_ROOT + "/catalog/sources/" + SAMPLE_SOURCE_NAME + "/" + SAMPLE_ID + "?transform=" + METACARD_TRANSFORMER_ID + "&" + CACHE_ONLY;

    @Mock
    private ResourceCacheInterface mockResourceCache;
    
    @Mock
    private Metacard mockMetacard;
    
    @Before
    public void setup() {
        when(mockMetacard.getSourceId()).thenReturn(SAMPLE_SOURCE_NAME);
        when(mockMetacard.getId()).thenReturn(SAMPLE_ID);
    }
    
    @Test
    public void testCanHandleResourceCached() {
        // Setup
        CacheOnlyMetacardTransformerActionProvider actionProvider = getCacheOnlyMetacardTransformerActionProvider(SAMPLE_SECURE_PROTOCOL, SAMPLE_IP);
        setCached(actionProvider, true);
        
        // Perform Test
        boolean canHandle = actionProvider.canHandle(mockMetacard);
        
        // Verify
        assertThat(canHandle, is(false));
    }
    
    @Test
    public void testCanHandle() {
        // Setup
        CacheOnlyMetacardTransformerActionProvider actionProvider = getCacheOnlyMetacardTransformerActionProvider(SAMPLE_SECURE_PROTOCOL, SAMPLE_IP);
        setCached(actionProvider, true);
        
        // Test
        boolean canHandle = actionProvider.canHandle(SAMPLE_ID);
        
        // Verify
        assertThat(canHandle, is(false));
    }
    
    @Test
    public void testGetActionsResourceNotCached() {
        // Setup
        CacheOnlyMetacardTransformerActionProvider actionProvider = getCacheOnlyMetacardTransformerActionProvider(SAMPLE_SECURE_PROTOCOL, SAMPLE_IP);
        setCached(actionProvider, false);

        
        // Perform Test
        boolean canHandle = actionProvider.canHandle(mockMetacard);
        List<Action> actions = actionProvider.getActions(mockMetacard);
        
        // Verify
        assertThat(canHandle, is(true));
        assertThat(actions, hasSize(1));
        String actualUrl = actions.get(0).getUrl().toString();
        assertThat(actualUrl, is(EXPECTED_ACTION_URL));
    }
    
    @Test
    public void testGetActionsMalformedUrl() {
        // Setup
        CacheOnlyMetacardTransformerActionProvider actionProvider = getCacheOnlyMetacardTransformerActionProvider(BAD_PROTOCOL, SAMPLE_IP);
        setCached(actionProvider, true);

        // Perform Test
        List<Action> actions = actionProvider.getActions(mockMetacard);
        
        // Verify
        assertThat(actions, hasSize(0));
    }
    
    @Test
    public void testGetActionsInvalidUriSyntax() {
        // Setup
        CacheOnlyMetacardTransformerActionProvider actionProvider = getCacheOnlyMetacardTransformerActionProvider(SAMPLE_SECURE_PROTOCOL, INVALID_IP_ADDRESS);
        setCached(actionProvider, true);
        
        // Perform Test
        List<Action> actions = actionProvider.getActions(mockMetacard);
        
        // Verify
        assertThat(actions, hasSize(0));
    }
    
    private void setCached(CacheOnlyMetacardTransformerActionProvider actionProvider, boolean cached) {
        when(mockResourceCache.containsValid(getKey(actionProvider), mockMetacard)).thenReturn(cached);
    }
    
    private String getKey(CacheOnlyMetacardTransformerActionProvider actionProvider) {
        return String.format("%s-%s", actionProvider.getSource(mockMetacard), SAMPLE_ID);
    }
    
    private CacheOnlyMetacardTransformerActionProvider getCacheOnlyMetacardTransformerActionProvider(String protocol, String ipAddress) {
        CacheOnlyMetacardTransformerActionProvider actionProvider = new CacheOnlyMetacardTransformerActionProvider(ACTION_PROVIDER_ID, METACARD_TRANSFORMER_ID, mockResourceCache);
        configureActionProvider(protocol,
                ipAddress,
                SAMPLE_SECURE_PORT,
                SAMPLE_SERVICES_ROOT,
                SAMPLE_ID);
        return actionProvider;
    }
}
