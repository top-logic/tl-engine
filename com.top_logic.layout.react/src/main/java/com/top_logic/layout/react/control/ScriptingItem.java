/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control;

import java.util.List;

/**
 * An item a container control displays - a row of a table, a card of a board - with the controls
 * it displays for it.
 *
 * <p>
 * The container describes the item in its own projected state (a row by its cell texts, a card by
 * the label of its object), so the headless projection shows a control displayed for the item only
 * if the user can operate it - an editable field, an enabled button or link. Such a control is
 * addressed under a node of the item, e.g. {@code tableView/row[v12]/button[Open]}, so that it is
 * addressable per item.
 * </p>
 *
 * @param role
 *        The role of the item node, e.g. {@code "row"}.
 * @param id
 *        The id the container displays the item under, one of its
 *        {@link ScriptingControl#objectIdentities() object identities}. Consists of address-safe
 *        characters, as the ids allocated by a {@link com.top_logic.layout.react.ReactContext} do.
 * @param object
 *        The object the item stands for, {@code null} for an item standing for no object, e.g. the
 *        header row of a group.
 * @param controls
 *        The controls the container displays for the item.
 *
 * @see ScriptingControl#scriptingItems()
 */
public record ScriptingItem(String role, String id, Object object, List<ReactControl> controls) {

	/**
	 * Creates a {@link ScriptingItem}.
	 */
	public ScriptingItem {
		controls = List.copyOf(controls);
	}

}
