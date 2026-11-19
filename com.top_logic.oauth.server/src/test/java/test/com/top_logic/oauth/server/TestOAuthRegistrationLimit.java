/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.oauth.server;

import java.time.Duration;
import java.time.Instant;

import junit.framework.Test;

import com.meterware.httpunit.WebResponse;
import com.nimbusds.oauth2.sdk.OAuth2Error;
import com.nimbusds.oauth2.sdk.client.ClientRegistrationErrorResponse;

import com.top_logic.oauth.server.OAuthAuthorizationServer;

/**
 * Test of the limit on dynamically registered clients of the {@link OAuthAuthorizationServer},
 * configured to {@value #MAX_CLIENTS} clients.
 */
public class TestOAuthRegistrationLimit extends AbstractOAuthServerTest {

	/** The configured maximum number of registered clients. */
	private static final int MAX_CLIENTS = 3;

	/**
	 * Registrations beyond the limit are refused until the clean-up has removed unused clients.
	 */
	public void testRegistrationsAreCapped() throws Exception {
		server().cleanUp(Instant.now().plus(Duration.ofDays(2)));
		for (int n = 0; n < MAX_CLIENTS; n++) {
			register(REDIRECT_URI);
		}

		WebResponse refused = postRegistration(REDIRECT_URI);
		assertEquals(503, refused.getResponseCode());
		ClientRegistrationErrorResponse error = ClientRegistrationErrorResponse.parse(httpResponse(refused));
		assertEquals(OAuth2Error.TEMPORARILY_UNAVAILABLE.getCode(), error.getErrorObject().getCode());

		OAuthAuthorizationServer.CleanUpResult result = server().cleanUp(Instant.now().plus(Duration.ofDays(2)));
		assertEquals(MAX_CLIENTS, result.clients());
		register(REDIRECT_URI);
	}

	/**
	 * The suite of tests to execute.
	 */
	public static Test suite() {
		return suite(TestOAuthRegistrationLimit.class);
	}

}
