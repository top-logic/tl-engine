/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.accesstoken;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.element.util.ElementWebTestSetup;

import com.top_logic.base.security.device.TLSecurityDeviceManager;
import com.top_logic.element.accesstoken.AccessToken;
import com.top_logic.element.accesstoken.AccessTokenPermissions;
import com.top_logic.element.accesstoken.AccessTokens;
import com.top_logic.element.accesstoken.I18NConstants;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.model.TLClass;
import com.top_logic.util.error.TopLogicException;

/**
 * Test of {@link AccessTokens}: the format of a token, what is kept of it, how it is found, expires
 * and records its use, and the {@link AccessTokenPermissions permissions} it grants.
 *
 * <p>
 * The permissions <code>test-*</code> are contributed by the test configuration
 * <code>element.test.config.xml</code>.
 * </p>
 */
public class TestAccessTokens extends BasicTestCase {

	private static final String A = "test-a";

	private static final String B = "test-b";

	private static final String C = "test-c";

	/** Binds to the session. */
	private static final String OBSERVE = "test-observe";

	/** Binds to the session and implies {@link #OBSERVE}. */
	private static final String ACT = "test-act";

	private static final String SESSION = "session-1";

	private final List<AccessToken> _created = new ArrayList<>();

	@Override
	protected void tearDown() throws Exception {
		for (AccessToken token : _created) {
			if (token.tValid()) {
				AccessTokens.revoke(token);
			}
		}
		_created.clear();
		super.tearDown();
	}

	/** A new token is the prefix and the secret, and is recognised as a token. */
	public void testANewTokenHasTheFormat() {
		String token = AccessTokens.newToken();

		assertTrue(token, token.startsWith(AccessTokens.PREFIX));
		assertEquals(token, AccessTokens.PREFIX.length() + 43, token.length());
		assertTrue(token, token.substring(AccessTokens.PREFIX.length()).matches("[0-9A-Za-z]+"));
		assertTrue(AccessTokens.hasFormat(token));
	}

	/**
	 * What is not a token is told apart without a lookup - which is also what lets another bearer
	 * scheme configured beside this one take the request.
	 */
	public void testWhatIsNotATokenIsRecognised() {
		String token = AccessTokens.newToken();
		String secret = token.substring(AccessTokens.PREFIX.length());

		assertFalse("Another prefix.", AccessTokens.hasFormat("ghp_" + secret));
		assertFalse("Too short.", AccessTokens.hasFormat(token.substring(0, token.length() - 1)));
		assertFalse("Too long.", AccessTokens.hasFormat(token + "A"));
		assertFalse("A character outside the alphabet.",
			AccessTokens.hasFormat(token.substring(0, token.length() - 1) + "-"));
		assertFalse("An OAuth access token, a JWT.",
			AccessTokens.hasFormat("eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJhbm5hIn0.c2lnbmF0dXJl"));
		assertFalse(AccessTokens.hasFormat(null));
	}

	/** The stored hash is the hex SHA-256 of the token: the FIPS 180-2 test vector "abc". */
	public void testTheHashIsSha256() {
		assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
			AccessTokens.hash("abc"));
	}

	/** The hint keeps the prefix and four characters of the secret, and no more. */
	public void testTheHintKeepsFourCharactersOfTheSecret() {
		String token = AccessTokens.newToken();

		assertEquals(token.substring(0, AccessTokens.PREFIX.length() + 4) + "…", AccessTokens.hint(token));
	}

	/**
	 * A token is stored in its own table as its hash and hint, never as itself, and is found again
	 * by the token.
	 */
	public void testStoredAsHashAndFoundByToken() {
		AccessTokens.Created created = create("stored", null, List.of(A), null);
		AccessToken token = created.token();
		String plain = created.plain();

		TLClass type = AccessTokens.accessTokenType();
		assertNotNull("tl-element defines the type, also without any module offering an endpoint.", type);
		assertEquals(type, token.tType());
		assertEquals(AccessToken.TABLE, token.tTable().getName());

		assertEquals(AccessTokens.hash(plain), token.getSecretHash());
		assertEquals(AccessTokens.hint(plain), token.getHint());
		assertEquals("stored", token.getName());
		assertEquals(root(), token.getOwner());
		assertNotNull(token.getCreatedAt());
		assertFalse("The plain token is not part of the record.", created.toString().contains(plain));

		assertEquals(token, AccessTokens.find(plain));
		assertNull("A token never created.", AccessTokens.find(AccessTokens.newToken()));
	}

	/** A revoked token is no longer found. */
	public void testRevokedTokenIsNotFound() {
		AccessTokens.Created created = create("revoked", null, List.of(A), null);

		AccessTokens.revoke(created.token());

		assertNull(AccessTokens.find(created.plain()));
	}

	/** A token with a validity expires after that many days; one without never does. */
	public void testExpiry() {
		Instant now = Instant.now();
		AccessToken expiring = create("expiring", Integer.valueOf(1), List.of(A), null).token();
		AccessToken unlimited = create("unlimited", null, List.of(A), null).token();

		assertFalse(AccessTokens.isExpired(expiring, now));
		assertTrue(AccessTokens.isExpired(expiring, now.plus(Duration.ofDays(2))));
		assertNull(unlimited.getExpiresAt());
		assertFalse(AccessTokens.isExpired(unlimited, now.plus(Duration.ofDays(10000))));
	}

	/** A token needs a name and, if given, a positive validity. */
	public void testNameAndValidityAreChecked() {
		assertRefused(I18NConstants.ERROR_TOKEN_NAME_MISSING, () -> create(" ", null, List.of(A), null));
		assertRefused(I18NConstants.ERROR_TOKEN_VALIDITY_NOT_POSITIVE,
			() -> create("zero", Integer.valueOf(0), List.of(A), null));
	}

	/** The last use is written at most once within {@link AccessTokens#LAST_USED_GRANULARITY}. */
	public void testLastUsedIsThrottled() {
		AccessToken token = create("used", null, List.of(A), null).token();
		assertNull(token.getLastUsed());

		Instant first = Instant.now();
		AccessTokens.touch(token, first);
		assertEquals(first.toEpochMilli(), token.getLastUsed().getTime());

		AccessTokens.touch(token, first.plus(AccessTokens.LAST_USED_GRANULARITY).minusSeconds(1));
		assertEquals("Within the granularity, nothing is written.", first.toEpochMilli(),
			token.getLastUsed().getTime());

		Instant later = first.plus(AccessTokens.LAST_USED_GRANULARITY);
		AccessTokens.touch(token, later);
		assertEquals(later.toEpochMilli(), token.getLastUsed().getTime());
	}

	/** A token has exactly the permissions it was created with. */
	public void testTokenHasItsPermissions() {
		AccessToken token = create("a b", null, List.of(A, B), null).token();

		assertEquals(List.of(A, B), AccessTokens.permissions(token));
		assertTrue(AccessTokens.hasPermission(token, A));
		assertTrue(AccessTokens.hasPermission(token, B));
		assertFalse(AccessTokens.hasPermission(token, C));
	}

	/** A permission implies the permissions it is configured to imply, not the other way round. */
	public void testImpliedPermission() {
		AccessToken act = create("act", null, List.of(ACT), SESSION).token();
		AccessToken observe = create("observe", null, List.of(OBSERVE), SESSION).token();

		assertTrue(AccessTokens.hasPermission(act, ACT));
		assertTrue(AccessTokens.hasPermission(act, OBSERVE));
		assertTrue(AccessTokens.hasPermission(observe, OBSERVE));
		assertFalse(AccessTokens.hasPermission(observe, ACT));
	}

	/** A token needs at least one permission, and only offered ones. */
	public void testPermissionsAreChecked() {
		assertRefused(I18NConstants.ERROR_NO_PERMISSION, () -> create("none", null, List.of(), null));
		assertRefused(I18NConstants.ERROR_UNKNOWN_PERMISSION__NAME.fill("test-unknown"),
			() -> create("unknown", null, List.of(A, "test-unknown"), null));
	}

	/** The session key is stored only for a token with a permission that binds to the session. */
	public void testSessionKeyOnlyForSessionBindingPermission() {
		AccessToken unbound = create("unbound", null, List.of(A), SESSION).token();
		AccessToken bound = create("bound", null, List.of(A, OBSERVE), SESSION).token();

		assertNull(unbound.getSessionKey());
		assertEquals(SESSION, bound.getSessionKey());

		try {
			create("no session", null, List.of(OBSERVE), null);
			fail("A permission that binds to the session requires a session.");
		} catch (IllegalArgumentException ex) {
			// Expected.
		}
	}

	private AccessTokens.Created create(String name, Integer validForDays, Collection<String> permissions,
			String sessionKey) {
		AccessTokens.Created created = AccessTokens.create(root(), name, validForDays, permissions, sessionKey);
		_created.add(created.token());
		return created;
	}

	private static void assertRefused(Object expectedKey, Runnable action) {
		try {
			action.run();
			fail("Expected refusal: " + expectedKey);
		} catch (TopLogicException ex) {
			assertEquals(expectedKey, ex.getErrorKey());
		}
	}

	private static Person root() {
		return PersonManager.getManager().getRoot();
	}

	/**
	 * The suite of tests to perform.
	 */
	public static Test suite() {
		Test suite = ServiceTestSetup.createSetup(new TestSuite(TestAccessTokens.class),
			TLSecurityDeviceManager.Module.INSTANCE, PersonManager.Module.INSTANCE,
			AccessTokenPermissions.Module.INSTANCE);
		return ElementWebTestSetup.createElementWebTestSetup(suite);
	}

}
