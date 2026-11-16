/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.accesstoken;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLClass;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.ModelService;

/**
 * {@link AccessToken Access tokens}: how one is made, stored, found and refused, and what it
 * permits.
 *
 * <h2>The format</h2>
 *
 * <p>
 * A token is {@value #PREFIX} followed by {@value #SECRET_LENGTH} characters drawn uniformly from
 * {@code [0-9A-Za-z]} by a secure random generator - {@code 43 * log2(62)}, about 256 bits of
 * randomness, see {@link SecretTokens}. The prefix makes a token recognisable: a secret scanner (the secret scanning of a
 * code host, a pre-commit hook) can match a leaked one by pattern, a person can tell it from a
 * password at a glance, and an endpoint can tell it from another bearer token without a database
 * lookup, see {@link #hasFormat(String)}. Base 62 rather than base 64, so that a double
 * click selects the whole token.
 * </p>
 *
 * <h2>What is stored</h2>
 *
 * <p>
 * Only the SHA-256 of the token, never the token. An unsalted fast hash is right here and would be
 * wrong for a password: the secret is 256 random bits, not something a person chose, so there is no
 * dictionary to run against the hash, and the lookup by hash is what makes a request one indexed
 * query. A copy of the database therefore yields no usable token. The {@link #hint(String) hint}
 * keeps the prefix and four characters of the secret, so that a person can match a row to the
 * client configuration it went into; four of 43 characters leave the rest unguessable.
 * </p>
 *
 * <p>
 * The plain token exists once: it is returned from
 * {@link #create(Person, String, Integer, Collection, String)} to be shown to its owner, and
 * nothing here writes it anywhere, a log included.
 * </p>
 *
 * <h2>What a token permits</h2>
 *
 * <p>
 * A token carries the names of its permissions, which the modules offering an endpoint contribute
 * to the {@link AccessTokenPermissions}. An endpoint accepts a token only if it
 * {@link #hasPermission(AccessToken, String) has} the endpoint's permission. A permission that
 * {@link AccessTokenPermissions.PermissionConfig#isBindingSession() binds to the session} acts only
 * in the session the token was created in, whose key the token stores.
 * </p>
 */
public final class AccessTokens {

	/** What every token starts with. */
	public static final String PREFIX = "tlpat_";

	/** Number of random characters after the {@link #PREFIX}. */
	static final int SECRET_LENGTH = SecretTokens.SECRET_LENGTH;

	/** What separates the permission names in {@link AccessToken#getPermissions()}. */
	public static final String PERMISSION_SEPARATOR = " ";

	/** Number of secret characters the {@link #hint(String) hint} keeps. */
	static final int HINT_LENGTH = 4;

	/**
	 * How stale {@link AccessToken#getLastUsed()} may be.
	 *
	 * <p>
	 * Writing it on every request would make every authenticated request a commit; a person asking "is this
	 * token still in use" needs the day, not the second.
	 * </p>
	 */
	public static final Duration LAST_USED_GRANULARITY = Duration.ofMinutes(1);

	private AccessTokens() {
		// Static utility.
	}

	/**
	 * A token that was just created.
	 *
	 * @param token
	 *        The stored token.
	 * @param plain
	 *        The token itself, to be shown once and never again.
	 */
	public record Created(AccessToken token, String plain) {
		@Override
		public String toString() {
			// Keeps the plain token out of anything that prints this record.
			return "Created[" + token + "]";
		}
	}

	/**
	 * Creates a token for {@code owner}.
	 *
	 * <p>
	 * Commits in a transaction of its own, or as part of the caller's if one is open.
	 * </p>
	 *
	 * @param owner
	 *        The account the token acts as.
	 * @param name
	 *        What the owner calls it, typically the client it is meant for.
	 * @param validForDays
	 *        The number of calendar days the token is accepted, counted from now;
	 *        <code>null</code> for a token that does not expire.
	 * @param permissions
	 *        Names of the {@link AccessTokenPermissions permissions} the token grants.
	 * @param sessionKey
	 *        The key of the session the token is issued from. Stored only if one of the
	 *        permissions {@link AccessTokenPermissions#isBindingSession(Collection) binds to the
	 *        session}, and required then.
	 * @throws TopLogicException
	 *         If the name is empty, the validity is not positive, no permission is given or a
	 *         permission is not offered by the application.
	 */
	public static Created create(Person owner, String name, Integer validForDays, Collection<String> permissions,
			String sessionKey) {
		if (name == null || name.isBlank()) {
			throw new TopLogicException(I18NConstants.ERROR_TOKEN_NAME_MISSING);
		}
		if (validForDays != null && validForDays.intValue() < 1) {
			throw new TopLogicException(I18NConstants.ERROR_TOKEN_VALIDITY_NOT_POSITIVE);
		}
		Set<String> granted = checkPermissions(permissions);
		boolean bindsSession = AccessTokenPermissions.getInstance().isBindingSession(granted);
		if (bindsSession && (sessionKey == null || sessionKey.isEmpty())) {
			throw new IllegalArgumentException("A token with a permission that binds to the session needs a session.");
		}

		String plain = newToken();
		Instant now = Instant.now();
		try (Transaction tx = kb().beginTransaction(ResKey.text("Create personal access token"))) {
			AccessToken token = (AccessToken) ModelService.getInstance().getFactory().createObject(accessTokenType());
			token.setOwner(owner);
			token.setName(name.strip());
			token.setHint(hint(plain));
			token.setSecretHash(hash(plain));
			token.setPermissions(String.join(PERMISSION_SEPARATOR, granted));
			if (bindsSession) {
				token.setSessionKey(sessionKey);
			}
			token.setCreatedAt(Date.from(now));
			if (validForDays != null) {
				// Calendar days in the server's zone, so that "30 days" ends at the time of day it was
				// created, across a change to or from daylight saving time.
				token.setExpiresAt(Date.from(
					now.atZone(ZoneId.systemDefault()).plusDays(validForDays.longValue()).toInstant()));
			}
			tx.commit();
			return new Created(token, plain);
		}
	}

	private static Set<String> checkPermissions(Collection<String> permissions) {
		Set<String> result = new LinkedHashSet<>();
		if (permissions != null) {
			AccessTokenPermissions offered = AccessTokenPermissions.getInstance();
			for (String permission : permissions) {
				if (offered.getPermission(permission) == null) {
					throw new TopLogicException(I18NConstants.ERROR_UNKNOWN_PERMISSION__NAME.fill(permission));
				}
				result.add(permission);
			}
		}
		if (result.isEmpty()) {
			throw new TopLogicException(I18NConstants.ERROR_NO_PERMISSION);
		}
		return result;
	}

	/**
	 * The names of the permissions {@code token} was created with.
	 *
	 * @see #hasPermission(AccessToken, String)
	 */
	public static List<String> permissions(AccessToken token) {
		String permissions = token.getPermissions();
		if (permissions == null || permissions.isBlank()) {
			return List.of();
		}
		return new ArrayList<>(Arrays.asList(permissions.strip().split(PERMISSION_SEPARATOR + "+")));
	}

	/**
	 * Whether {@code token} grants the permission {@code name}, either directly or because one of
	 * its permissions {@link AccessTokenPermissions.PermissionConfig#getImplies() implies} it.
	 */
	public static boolean hasPermission(AccessToken token, String name) {
		return AccessTokenPermissions.getInstance().withImplied(permissions(token)).contains(name);
	}

	/**
	 * Deletes a token, which revokes it: the next request presenting it is refused.
	 */
	public static void revoke(AccessToken token) {
		try (Transaction tx = kb().beginTransaction(ResKey.text("Revoke personal access token"))) {
			token.tDelete();
			tx.commit();
		}
	}

	/**
	 * The stored token that {@code presented} is, if any.
	 *
	 * @param presented
	 *        What a caller sent. Not checked for the format; see {@link #hasFormat(String)}.
	 * @return The token, or <code>null</code> if none has this hash - never created, or revoked.
	 */
	public static AccessToken find(String presented) {
		Iterator<KnowledgeItem> hits =
			kb().getObjectsByAttribute(AccessToken.TABLE, AccessToken.SECRET_HASH_ATTR, hash(presented));
		return hits.hasNext() ? hits.next().getWrapper() : null;
	}

	/**
	 * Whether {@code token} is refused at {@code now} because its validity ran out.
	 */
	public static boolean isExpired(AccessToken token, Instant now) {
		Date expiresAt = token.getExpiresAt();
		return expiresAt != null && !now.isBefore(expiresAt.toInstant());
	}

	/**
	 * Records that {@code token} was used at {@code now}, unless that is already known to within
	 * {@link #LAST_USED_GRANULARITY}.
	 */
	public static void touch(AccessToken token, Instant now) {
		Date lastUsed = token.getLastUsed();
		if (lastUsed != null && Duration.between(lastUsed.toInstant(), now).compareTo(LAST_USED_GRANULARITY) < 0) {
			return;
		}
		try (Transaction tx = kb().beginTransaction(ResKey.text("Record use of personal access token"))) {
			token.setLastUsed(Date.from(now));
			tx.commit();
		}
	}

	/**
	 * Whether {@code candidate} looks like a token at all: the {@link #PREFIX} and
	 * {@link #SECRET_LENGTH} characters of the alphabet.
	 */
	public static boolean hasFormat(String candidate) {
		return SecretTokens.hasFormat(PREFIX, candidate);
	}

	/**
	 * A new random token.
	 */
	public static String newToken() {
		return SecretTokens.newToken(PREFIX);
	}

	/**
	 * What is stored to find {@code token} again: the lower-case hex SHA-256 of its UTF-8 bytes.
	 */
	public static String hash(String token) {
		return SecretTokens.hash(token);
	}

	/**
	 * What is shown of {@code token} once it is stored: the prefix, the first
	 * {@value #HINT_LENGTH} secret characters and an ellipsis.
	 */
	public static String hint(String token) {
		return token.substring(0, PREFIX.length() + HINT_LENGTH) + "…";
	}

	/**
	 * The type <code>tl.accounts:AccessToken</code>.
	 */
	public static TLClass accessTokenType() {
		return (TLClass) TLModelUtil.findType(AccessToken.MODULE, AccessToken.ACCESS_TOKEN_TYPE);
	}

	private static KnowledgeBase kb() {
		return PersistencyLayer.getKnowledgeBase();
	}
}
