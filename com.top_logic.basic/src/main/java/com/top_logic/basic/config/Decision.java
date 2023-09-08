/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import com.top_logic.basic.UnreachableAssertion;

/**
 * Enumeration representing a decision.
 * 
 * <p>
 * This enum represents a boolean decision with a default value. The default value {@link #DEFAULT}
 * works as neutral element in each boolean operation.
 * </p>
 * 
 * <p>
 * Moreover the enum has methods to convert a {@link Decision} to a boolean value and to concatenate
 * different {@link Decision}.
 * </p>
 * 
 * @since 5.7.4
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public enum Decision implements ExternallyNamed {

	/** There is no explicit decision. The default boolean value should be used. */
	DEFAULT("default"),

	/** The decision is <code>true</code>. */
	TRUE("true"),

	/** The decision is <code>false</code>. */
	FALSE("false"),

	;

	private final String _externalName;

	private Decision(String name) {
		_externalName = name;
	}

	@Override
	public String getExternalName() {
		return _externalName;
	}

	/**
	 * Retrieves the boolean value of this {@link Decision}.
	 * 
	 * @param fallback
	 *        Used if this {@link Decision} id {@link #DEFAULT}.
	 * 
	 * @return <code>true</code> for {@link #TRUE}, <code>false</code> for {@link #FALSE}, and the
	 *         given boolean for {@link #DEFAULT}.
	 */
	public boolean toBoolean(boolean fallback) {
		switch (ordinal()) {
			case 0:
				return fallback;
			case 1:
				return true;
			case 2:
				return false;
			default:
				throw noSuchOrdinal();
		}
	}

	/**
	 * Converts the {@link Boolean} tristate <code>null</code>, <code>true</code>,
	 * <code>false</code> to {@link #DEFAULT}, {@link #TRUE}, {@link #FALSE}.
	 */
	public static Decision valueOf(Boolean value) {
		if (value == null) {
			return DEFAULT;
		}
		return valueOf(value.booleanValue());
	}

	/**
	 * Converts the <code>boolean</code> <code>true</code>, <code>false</code> to {@link #TRUE} or
	 * {@link #FALSE}.
	 */
	public static Decision valueOf(boolean value) {
		return value ? TRUE : FALSE;
	}

	/**
	 * Whether this {@link Decision} is not {@link #DEFAULT}.
	 */
	public boolean isDecided() {
		return this != DEFAULT;
	}

	private UnreachableAssertion noSuchOrdinal() {
		return new UnreachableAssertion("Unexpected ordinal " + ordinal() + " for Decision " + this);
	}

	/**
	 * Returns a {@link RuntimeException} to throw in the default case in a switch over all
	 * {@link Decision}s.
	 */
	public static UnreachableAssertion noSuchDecision(Decision decision) {
		return new UnreachableAssertion("Unexpected Decision " + decision);
	}

	/**
	 * Inverts the given decision.
	 * 
	 * @see Decision#not()
	 */
	public static Decision not(Decision decision) {
		return decision.not();
	}

	/**
	 * Inverts this decision.
	 * 
	 * @return {@link #DEFAULT} for {@link #DEFAULT}, {@link #FALSE} for {@link #TRUE}, and
	 *         {@link #TRUE} for {@link #FALSE}.
	 */
	public Decision not() {
		switch (ordinal()) {
			case 0:
				return DEFAULT;
			case 1:
				return FALSE;
			case 2:
				return TRUE;
			default:
				throw noSuchOrdinal();
		}
	}

	/**
	 * Creates the conjunction of the given {@link Decision}s.
	 * 
	 * @see Decision#and(Decision)
	 */
	public static Decision and(Decision first, Decision second) {
		return first.and(second);
	}

	/**
	 * Creates the conjunction with the given {@link Decision}.
	 * 
	 * @return If this or the given ordinal is {@link #DEFAULT} the other is returned. If both are
	 *         {@link #TRUE} then {@link #TRUE} is returned, {@link #FALSE} otherwise.
	 */
	public Decision and(Decision other) {
		switch (ordinal()) {
			case 0:
				return other;
			case 1:
				// this is true
				switch (other) {
					case DEFAULT:
						return TRUE;
					case TRUE:
						return TRUE;
					case FALSE:
						return FALSE;
					default:
						throw noSuchDecision(other);
				}
			case 2:
				return FALSE;
			default:
				throw noSuchOrdinal();
		}
	}

	/**
	 * Creates the disjunction of the given {@link Decision}s.
	 * 
	 * @see Decision#or(Decision)
	 */
	public static Decision or(Decision first, Decision second) {
		return first.or(second);
	}

	/**
	 * Creates the disjunction with the given {@link Decision}.
	 * 
	 * @return If this or the given ordinal is {@link #DEFAULT} the other is returned. If both are
	 *         {@link #FALSE} then {@link #FALSE} is returned, {@link #TRUE} otherwise.
	 */
	public Decision or(Decision other) {
		switch (ordinal()) {
			case 0:
				return other;
			case 1:
				return TRUE;
			case 2:
				// this is false
				switch (other) {
					case DEFAULT:
						return FALSE;
					case TRUE:
						return TRUE;
					case FALSE:
						return FALSE;
					default:
						throw noSuchDecision(other);
				}
			default:
				throw noSuchOrdinal();
		}
	}

}
