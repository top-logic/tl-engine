/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.scriptable;

/**
 * Interface directly bound by a property of {@link Holder}.
 */
public interface Provider extends BaseProvider {

	/**
	 * Creates a value for the given context, see {@link #isTransient()}.
	 *
	 * @param context
	 *        The context of the new value.
	 * @param count
	 *        The number of values.
	 * @return The created value.
	 */
	Object create(Object context, int count);

	/**
	 * Whether the value is transient.
	 *
	 * @return Whether the value is not stored.
	 */
	default boolean isTransient() {
		return false;
	}

	/**
	 * Not implementable by a script.
	 */
	static Provider none() {
		return null;
	}

	/**
	 * Undocumented in-app, no warning expected.
	 */
	void undocumentedParam(Object value);

	void undocumented();

}
