/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.oauth.server;

import junit.framework.Test;

import com.meterware.httpunit.WebResponse;
import com.nimbusds.oauth2.sdk.as.AuthorizationServerMetadata;

import com.top_logic.oauth.server.OAuthAuthorizationServer;

/**
 * Test of an {@link OAuthAuthorizationServer} with dynamic client registration switched off.
 */
public class TestOAuthRegistrationDisabled extends AbstractOAuthServerTest {

	/**
	 * The registration endpoint is not found.
	 */
	public void testRegistrationNotFound() throws Exception {
		WebResponse response = postRegistration(REDIRECT_URI);

		assertEquals(404, response.getResponseCode());
	}

	/**
	 * The metadata names no registration endpoint, and no client ID metadata documents.
	 */
	public void testMetadataWithoutRegistration() throws Exception {
		AuthorizationServerMetadata metadata = metadata();

		assertNull(metadata.getRegistrationEndpointURI());
		assertEquals(Boolean.FALSE,
			metadata.getCustomParameter(OAuthAuthorizationServer.CLIENT_ID_METADATA_DOCUMENT_SUPPORTED));
	}

	/**
	 * The suite of tests to execute.
	 */
	public static Test suite() {
		return suite(TestOAuthRegistrationDisabled.class);
	}

}
