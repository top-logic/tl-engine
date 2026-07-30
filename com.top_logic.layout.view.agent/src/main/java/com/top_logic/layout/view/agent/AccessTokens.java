/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.agent;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import com.top_logic.basic.StringServices;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

/**
 * The access tokens through which an account lets an agent into its session.
 *
 * <p>
 * A token is a persistent object owned by the account that issued it. Its secret is stored as a
 * hash only: the plain secret exists once, in the response of
 * {@link #issue(Person, String, int, boolean, String)}, and is shown to the owner right there. A
 * token that is lost is revoked and re-issued, never recovered.
 * </p>
 *
 * <p>
 * The plain secret reads {@code tlagt_<id>_<secret>}. The id part identifies the token object
 * without a search over all tokens; the secret part is compared against the stored hash.
 * </p>
 */
public class AccessTokens {

	/** Qualified name of the access token type. */
	public static final String TYPE = "tl.agent:AccessToken";

	/** Name of the token's owner reference. */
	public static final String OWNER = "owner";

	/** Name of the token's label attribute. */
	public static final String LABEL = "label";

	/** Name of the attribute holding the hash of the token's secret. */
	public static final String SECRET_HASH = "secretHash";

	/** Name of the attribute telling whether the token may change anything. */
	public static final String MAY_ACT = "mayAct";

	/** Name of the attribute holding the key of the session the token admits an agent to. */
	public static final String SESSION_KEY = "sessionKey";

	/**
	 * Name of the token's issue time attribute.
	 *
	 * <p>
	 * Not named {@code created}: that is a reserved system attribute of every persistent item,
	 * holding the technical creation timestamp in another representation.
	 * </p>
	 */
	public static final String ISSUED = "issued";

	/** Name of the token's expiry time attribute. */
	public static final String EXPIRES = "expires";

	/** Name of the attribute holding the time the token was last presented. */
	public static final String LAST_USED = "lastUsed";

	/** Name of the attribute holding the token's public identifier. */
	public static final String TOKEN_ID = "tokenId";

	/** Qualified name of the transient type carrying the input of the issue form. */
	public static final String REQUEST_TYPE = "tl.agent:NewAccessToken";

	/** Qualified name of the transient type carrying an issued token to the dialog showing it. */
	public static final String ISSUED_TYPE = "tl.agent:IssuedAccessToken";

	/** Name of the attribute holding the invitation, on the issued-token carrier. */
	public static final String INVITATION = "invitation";

	/** Name of the requested validity in hours, on the issue form input. */
	public static final String VALID_HOURS = "validHours";


	/** Prefix identifying a plain secret as an access token of this application. */
	private static final String SECRET_PREFIX = "tlagt_";

	private static final String SECRET_SEPARATOR = "_";

	private static final int SECRET_BYTES = 32;

	private static final int ID_BYTES = 8;

	private static final SecureRandom RANDOM = new SecureRandom();

	/**
	 * Issues a token for the given account.
	 *
	 * @param owner
	 *        The account the agent acts as.
	 * @param label
	 *        The owner's name for the token, telling one agent from another when revoking.
	 * @param validHours
	 *        How long the token remains valid, in hours.
	 * @param mayAct
	 *        Whether the agent may change anything, as opposed to observing only.
	 * @param sessionKey
	 *        The key of the session the agent is admitted to.
	 * @return The plain secret, to be shown to the owner once. It cannot be recovered afterwards.
	 */
	public static String issue(Person owner, String label, int validHours, boolean mayAct, String sessionKey) {
		String secret = randomString(SECRET_BYTES);
		String tokenId = randomString(ID_BYTES);

		TLObject token = ModelService.getInstance().getFactory().createObject(type(), null);
		token.tUpdate(part(TOKEN_ID), tokenId);
		token.tUpdate(part(OWNER), owner);
		token.tUpdate(part(LABEL), label);
		token.tUpdate(part(SECRET_HASH), hash(secret));
		token.tUpdate(part(MAY_ACT), Boolean.valueOf(mayAct));
		token.tUpdate(part(SESSION_KEY), sessionKey);
		Date now = new Date();
		token.tUpdate(part(ISSUED), now);
		token.tUpdate(part(EXPIRES), new Date(now.getTime() + validHours * 3600_000L));

		return SECRET_PREFIX + tokenId + SECRET_SEPARATOR + secret;
	}

	/**
	 * Resolves the plain secret an agent presented.
	 *
	 * @param credential
	 *        The secret as presented, never empty.
	 * @return The token, or <code>null</code> if the credential is none of this application's, names
	 *         no token, or does not match the named token's secret. Expiry and revocation are
	 *         <em>not</em> checked here, see {@link #isValid(TLObject)}.
	 */
	public static TLObject resolve(String credential) {
		if (!credential.startsWith(SECRET_PREFIX)) {
			return null;
		}
		String rest = credential.substring(SECRET_PREFIX.length());
		int separator = rest.indexOf(SECRET_SEPARATOR);
		if (separator < 0) {
			return null;
		}
		String id = rest.substring(0, separator);
		String secret = rest.substring(separator + 1);
		if (StringServices.isEmpty(id) || StringServices.isEmpty(secret)) {
			return null;
		}

		TLObject token = byId(id);
		if (token == null) {
			return null;
		}
		String expectedHash = (String) token.tValue(part(SECRET_HASH));
		if (expectedHash == null || !MessageDigest.isEqual(
			expectedHash.getBytes(StandardCharsets.UTF_8), hash(secret).getBytes(StandardCharsets.UTF_8))) {
			return null;
		}
		return token;
	}

	/**
	 * Whether the given token still admits an agent, i.e. has not expired.
	 *
	 * <p>
	 * A withdrawn token is not checked here: withdrawing {@link #withdraw(TLObject) deletes} it.
	 * </p>
	 */
	public static boolean isValid(TLObject token) {
		Date expires = (Date) token.tValue(part(EXPIRES));
		return expires == null || expires.after(new Date());
	}

	/**
	 * Records that the given token was just presented, so that its owner sees an agent using it.
	 */
	public static void markUsed(TLObject token) {
		token.tUpdate(part(LAST_USED), new Date());
	}

	/**
	 * Withdraws the given token by deleting it: an agent presenting it is no longer admitted.
	 *
	 * <p>
	 * The token is deleted rather than marked, because a withdrawn one can never admit anyone
	 * again — its secret exists nowhere but in the agent that holds it — so a kept row would only
	 * grow the owner's list.
	 * </p>
	 */
	public static void withdraw(TLObject token) {
		token.tDelete();
	}

	/**
	 * The token type.
	 */
	public static TLClass type() {
		return (TLClass) TLModelUtil.findType(ModelService.getApplicationModel(), TYPE);
	}

	/**
	 * The transient type carrying an issued token to the dialog showing it.
	 */
	public static TLClass issuedType() {
		return (TLClass) TLModelUtil.findType(ModelService.getApplicationModel(), ISSUED_TYPE);
	}

	/**
	 * The given attribute of the token type.
	 *
	 * @param name
	 *        One of the attribute name constants of this class.
	 */
	public static TLStructuredTypePart part(String name) {
		return type().getPart(name);
	}

	/**
	 * All tokens of the application, newest last.
	 */
	public static List<TLObject> all() {
		return MetaElementUtil.getAllInstancesOf(type(), TLObject.class);
	}

	/**
	 * The tokens the given account issued.
	 */
	public static List<TLObject> of(Person owner) {
		List<TLObject> result = new ArrayList<>();
		for (TLObject token : all()) {
			if (owner.equals(token.tValue(part(OWNER)))) {
				result.add(token);
			}
		}
		return result;
	}

	private static TLObject byId(String id) {
		for (TLObject token : all()) {
			if (id.equals(token.tValue(part(TOKEN_ID)))) {
				return token;
			}
		}
		return null;
	}

	private static String randomString(int bytes) {
		byte[] raw = new byte[bytes];
		RANDOM.nextBytes(raw);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
	}

	private static String hash(String secret) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			return Base64.getEncoder()
				.encodeToString(digest.digest(secret.getBytes(StandardCharsets.UTF_8)));
		} catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException("SHA-256 is not available.", ex);
		}
	}

	private AccessTokens() {
		// Static utility.
	}

}
