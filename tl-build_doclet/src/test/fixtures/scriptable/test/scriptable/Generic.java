/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.scriptable;

/**
 * Interface with type parameters.
 *
 * @param <T>
 *        The argument type.
 */
public interface Generic<T> {

	/**
	 * Operation with a generic argument.
	 *
	 * @param input
	 *        The input.
	 * @return The result.
	 */
	Object runGeneric(T input);

}
