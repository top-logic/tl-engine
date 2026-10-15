/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package it.app.use;

import it.app.Item;

/**
 * Hand-written code of the application that is compiled against the generated binding.
 */
public class ItemLabel {

	/**
	 * The label of the given {@link Item}.
	 */
	public static String label(Item item) {
		return item.getName();
	}

}
