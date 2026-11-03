/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.security.auth.pac4j.servlet;

import junit.framework.TestCase;

import org.pac4j.core.profile.definition.CommonProfileDefinition;
import org.pac4j.oidc.profile.OidcProfile;
import org.pac4j.oidc.profile.OidcProfileDefinition;

import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.oauth2.sdk.token.RefreshToken;

import com.top_logic.base.accesscontrol.ExternalIdentity;
import com.top_logic.base.accesscontrol.ExternalUserMapping;
import com.top_logic.security.auth.pac4j.servlet.Pac4jAuthenticationServlet;

/**
 * Tests the {@link ExternalIdentity} that {@link Pac4jAuthenticationServlet} hands to the
 * {@link ExternalUserMapping} of a pac4j login.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestPac4jAuthenticationServlet extends TestCase {

	/** The name of the pac4j client that has authenticated the user. */
	private static final String CLIENT = "test-oidc";

	/** The OpenID Connect subject of the user. */
	private static final String SUBJECT = "3f2b8c1e-7a44-4d0e-9a51-0c6d2e8f1b97";

	/** The login name extracted from the profile. */
	private static final String NAME = "alice";

	/** The e-mail address the identity provider reports. */
	private static final String EMAIL = "alice@example.com";

	/**
	 * A pac4j OpenID Connect login yields the client, the subject, the extracted name and the
	 * claims of the identity provider.
	 */
	public void testIdentityOfOidcLogin() {
		ExternalIdentity identity = Pac4jAuthenticationServlet.createIdentity(oidcProfile(), NAME);

		assertEquals(CLIENT, identity.getClientName());
		assertEquals(SUBJECT, identity.getSubject());
		assertEquals(NAME, identity.getName());
		assertEquals(EMAIL, identity.getClaim(CommonProfileDefinition.EMAIL));
		assertEquals(Boolean.TRUE, identity.getClaim(OidcProfileDefinition.EMAIL_VERIFIED));
	}

	/**
	 * The tokens that pac4j keeps among the attributes of the profile are credentials, not claims
	 * about the user, and do not reach the mapping.
	 */
	public void testNoTokensInClaims() {
		ExternalIdentity identity = Pac4jAuthenticationServlet.createIdentity(oidcProfile(), NAME);

		assertFalse(identity.getClaims().containsKey(OidcProfileDefinition.ACCESS_TOKEN));
		assertFalse(identity.getClaims().containsKey(OidcProfileDefinition.ID_TOKEN));
		assertFalse(identity.getClaims().containsKey(OidcProfileDefinition.REFRESH_TOKEN));
	}

	private static OidcProfile oidcProfile() {
		OidcProfile profile = new OidcProfile();
		profile.setId(SUBJECT);
		profile.setClientName(CLIENT);
		profile.addAttribute(CommonProfileDefinition.EMAIL, EMAIL);
		profile.addAttribute(OidcProfileDefinition.EMAIL_VERIFIED, Boolean.TRUE);
		profile.setAccessToken(new BearerAccessToken("access-secret"));
		profile.setIdTokenString("header.payload.signature");
		profile.setRefreshToken(new RefreshToken("refresh-secret"));
		return profile;
	}

}
