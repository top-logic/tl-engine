/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.scriptable;

/**
 * Super interface of {@link Provider}.
 */
public interface BaseProvider {

	/**
	 * Initializes the provider.
	 *
	 * @param name
	 *        The name of the provider.
	 */
	void init(String name);

}
