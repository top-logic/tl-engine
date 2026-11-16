/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.accesstoken;

import java.util.Date;

import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLNamed;

/**
 * An access token of the type {@value #ACCESS_TOKEN_TYPE} in the module {@value #MODULE}: a
 * credential a person creates for their own account and hands to a client.
 *
 * <p>
 * How a token is made, stored, found and refused is described in {@link AccessTokens}; what it
 * admits a client to is given by its {@link #getPermissions() permissions}, see
 * {@link AccessTokenPermissions}.
 * </p>
 */
public interface AccessToken extends TLNamed {

	/** Name of the module that defines the type. */
	String MODULE = "tl.accounts";

	/** Name of the type in the {@link #MODULE}. */
	String ACCESS_TOKEN_TYPE = "AccessToken";

	/** Name of the table that stores tokens. */
	String TABLE = "PersonalAccessToken";

	/** @see #getOwner() */
	String OWNER_ATTR = "owner";

	/** @see #getHint() */
	String HINT_ATTR = "hint";

	/** @see #getSecretHash() */
	String SECRET_HASH_ATTR = "secretHash";

	/** @see #getPermissions() */
	String PERMISSIONS_ATTR = "permissions";

	/** @see #getSessionKey() */
	String SESSION_KEY_ATTR = "sessionKey";

	/** @see #getCreatedAt() */
	String CREATED_AT_ATTR = "createdAt";

	/** @see #getExpiresAt() */
	String EXPIRES_AT_ATTR = "expiresAt";

	/** @see #getLastUsed() */
	String LAST_USED_ATTR = "lastUsed";

	/**
	 * The account the token acts as.
	 */
	default Person getOwner() {
		return (Person) tValueByName(OWNER_ATTR);
	}

	/**
	 * @see #getOwner()
	 */
	default void setOwner(Person newValue) {
		tUpdateByName(OWNER_ATTR, newValue);
	}

	/**
	 * The beginning of the token, to tell it apart from other tokens of its owner.
	 */
	default String getHint() {
		return (String) tValueByName(HINT_ATTR);
	}

	/**
	 * @see #getHint()
	 */
	default void setHint(String newValue) {
		tUpdateByName(HINT_ATTR, newValue);
	}

	/**
	 * The lower-case hex SHA-256 of the whole token.
	 */
	default String getSecretHash() {
		return (String) tValueByName(SECRET_HASH_ATTR);
	}

	/**
	 * @see #getSecretHash()
	 */
	default void setSecretHash(String newValue) {
		tUpdateByName(SECRET_HASH_ATTR, newValue);
	}

	/**
	 * The names of the permissions the token grants, separated by
	 * {@link AccessTokens#PERMISSION_SEPARATOR}.
	 *
	 * @see AccessTokens#permissions(AccessToken)
	 * @see AccessTokens#hasPermission(AccessToken, String)
	 */
	default String getPermissions() {
		return (String) tValueByName(PERMISSIONS_ATTR);
	}

	/**
	 * @see #getPermissions()
	 */
	default void setPermissions(String newValue) {
		tUpdateByName(PERMISSIONS_ATTR, newValue);
	}

	/**
	 * The key of the session the token was issued from, if it has a permission that
	 * {@link AccessTokenPermissions.PermissionConfig#isBindingSession() binds to the session};
	 * <code>null</code> otherwise.
	 */
	default String getSessionKey() {
		return (String) tValueByName(SESSION_KEY_ATTR);
	}

	/**
	 * @see #getSessionKey()
	 */
	default void setSessionKey(String newValue) {
		tUpdateByName(SESSION_KEY_ATTR, newValue);
	}

	/**
	 * When the token was created.
	 */
	default Date getCreatedAt() {
		return (Date) tValueByName(CREATED_AT_ATTR);
	}

	/**
	 * @see #getCreatedAt()
	 */
	default void setCreatedAt(Date newValue) {
		tUpdateByName(CREATED_AT_ATTR, newValue);
	}

	/**
	 * From this moment on the token is refused; <code>null</code> if it does not expire.
	 */
	default Date getExpiresAt() {
		return (Date) tValueByName(EXPIRES_AT_ATTR);
	}

	/**
	 * @see #getExpiresAt()
	 */
	default void setExpiresAt(Date newValue) {
		tUpdateByName(EXPIRES_AT_ATTR, newValue);
	}

	/**
	 * When a client last authenticated with the token, to within
	 * {@link AccessTokens#LAST_USED_GRANULARITY}; <code>null</code> if never.
	 */
	default Date getLastUsed() {
		return (Date) tValueByName(LAST_USED_ATTR);
	}

	/**
	 * @see #getLastUsed()
	 */
	default void setLastUsed(Date newValue) {
		tUpdateByName(LAST_USED_ATTR, newValue);
	}

}
