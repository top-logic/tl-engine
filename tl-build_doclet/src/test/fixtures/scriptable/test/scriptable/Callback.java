/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.scriptable;

/**
 * Interface Callback.
 */
public interface Callback {

	/**
	 * Operation of Callback.
	 *
	 * @param input
	 *        The input of Callback.
	 * @return The result of Callback.
	 */
	Object runCallback(Object input);

}
