/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.locking.token;

import com.top_logic.base.locking.LockService;
import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.model.TLObject;

/**
 * An atomic lock of a given {@link #getKind()} on a single {@link #getName() base object}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class Token {

	/**
	 * A kind of {@link Token}.
	 * 
	 * @see Token#getKind()
	 */
	public enum Kind implements ExternallyNamed {

		/**
		 * An exclusive {@link Token} an only be acquired by exactly one process.
		 * 
		 * <p>
		 * Holding an exclusive {@link Token} also prevents other processes from acquiring
		 * {@link #SHARED} {@link Token}s on the same object.
		 * </p>
		 */
		EXCLUSIVE("exclusive"),

		/**
		 * Multiple shared {@link Token}s can be acquired concurrently be different processes.
		 * 
		 * <p>
		 * Holding a shared {@link Token} prevents other processes from acquiring {@link #EXCLUSIVE}
		 * tokens on the same object but allows other processes to acquire other {@link #SHARED}
		 * tokens on the same object.
		 * </p>
		 */
		SHARED("shared");

		private String _name;

		/**
		 * Creates a {@link Token.Kind}.
		 */
		private Kind(String name) {
			_name = name;
		}

		@Override
		public String getExternalName() {
			return _name;
		}
	}

	/**
	 * Creates a global {@link Token} (not bound to an object).
	 */
	public static Token newGlobalToken(Kind kind, String name) {
		return createToken(kind, null, name);
	}

	/**
	 * Creates a new {@link Kind#EXCLUSIVE} token for the given object.
	 */
	public static Token newExclusiveDefaultToken(TLObject object) {
		return newExclusiveToken(object, Token.DEFAULT_OPERATION);
	}

	/**
	 * Creates a new {@link Kind#SHARED} token for the given object.
	 */
	public static Token newSharedDefaultToken(TLObject object) {
		return newSharedToken(object, Token.DEFAULT_OPERATION);
	}

	/**
	 * Creates a new {@link Kind#EXCLUSIVE} token for the given object.
	 */
	public static Token newExclusiveToken(TLObject object, String name) {
		return newToken(Kind.EXCLUSIVE, object, name);
	}

	/**
	 * Creates a new {@link Kind#SHARED} token for the given object.
	 */
	public static Token newSharedToken(TLObject object, String name) {
		return newToken(Kind.SHARED, object, name);
	}

	/**
	 * Creates a custom token for the given object.
	 */
	public static Token newToken(Kind kind, TLObject object, String name) {
		return createToken(kind, object, name);
	}

	private static Token createToken(Kind kind, TLObject object, String name) {
		if (name == null) {
			name = DEFAULT_OPERATION;
		}

		assert object != null
			|| !DEFAULT_OPERATION.equals(name) : "A default token must not be requested without context object.";

		return new Token(kind, object, name);
	}

	private final Kind _kind;

	private final Key _key;

	/**
	 * The default lock operation performed by this component.
	 * 
	 * @see LockService#createLock(String, Object...)
	 */
	public static final String DEFAULT_OPERATION = "editValues";

	private long _tokenId;

	/**
	 * Creates a {@link Token}.
	 *
	 * @param kind
	 *        See {@link #getKind()}.
	 * @param object
	 *        See {@link #getObject()}.
	 * @param name
	 *        See {@link #getName()}.
	 */
	private Token(Kind kind, TLObject object, String name) {
		_kind = kind;
		_key = new Key(object, name);
	}

	/**
	 * The {@link Kind} of token to acquire.
	 */
	public Kind getKind() {
		return _kind;
	}

	/**
	 * The unique name of the atomic lock to acquire.
	 */
	public String getName() {
		return _key.getName();
	}

	/**
	 * The {@link TLObject} for which this token is allocated, or <code>null</code> for a
	 * system-global {@link Token}.
	 */
	public TLObject getObject() {
		return _key.getObject();
	}

	/**
	 * The {@link Token} identifier.
	 */
	public Key getKey() {
		return _key;
	}

	/**
	 * Identifier for a {@link Token}.
	 */
	public final static class Key {

		private final TLObject _object;

		private final String _name;

		Key(TLObject object, String name) {
			assert name != null : "The token aspect name must not be null.";
			_object = object;
			_name = name;
		}

		/**
		 * The base object for a token.
		 * 
		 * <p>
		 * A system-global token has no object (<code>null</code>).
		 * </p>
		 */
		public TLObject getObject() {
			return _object;
		}

		/**
		 * The token name relative to {@link #getObject()}.
		 */
		public String getName() {
			return _name;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((_name == null) ? 0 : _name.hashCode());
			result = prime * result + ((_object == null) ? 0 : _object.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Key other = (Key) obj;
			if (_name == null) {
				if (other._name != null)
					return false;
			} else if (!_name.equals(other._name))
				return false;
			if (_object == null) {
				if (other._object != null)
					return false;
			} else if (!_object.equals(other._object))
				return false;
			return true;
		}
	}

	void setId(long tokenId) {
		_tokenId = tokenId;
	}

	long getId() {
		return _tokenId;
	}

	@Override
	public String toString() {
		return "Token(" + getKind() + ", " + getObject() + ", " + getName() + ")";
	}

}
