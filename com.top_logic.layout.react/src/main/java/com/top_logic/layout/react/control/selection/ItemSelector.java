/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.selection;

import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;

import com.top_logic.table.SelectionMode;

/**
 * A control displaying objects as items the user selects by a click, see {@link ItemSelection}.
 *
 * <p>
 * The view through which a selection channel binding reads and displays the selection of such a
 * control, whatever arrangement it displays its items in.
 * </p>
 */
public interface ItemSelector {

	/**
	 * Whether one item or several may be selected.
	 */
	SelectionMode getSelectionMode();

	/**
	 * Whether an item is displayed for the given object.
	 */
	boolean displays(Object item);

	/**
	 * The selected objects, empty if there are none.
	 *
	 * <p>
	 * The selection is kept while an object's item is not displayed, and highlighted again once it
	 * is.
	 * </p>
	 */
	Set<Object> getSelection();

	/**
	 * Highlights the items of the given objects, without reporting them to the
	 * {@link #setSelectionListener(Consumer) selection listener}.
	 *
	 * @param items
	 *        The selected objects, empty for no selection. In the selection mode
	 *        {@link SelectionMode#SINGLE}, the caller passes at most one.
	 */
	void setSelection(Collection<?> items);

	/**
	 * Sets the listener informed when the user changes the selection.
	 *
	 * @param listener
	 *        Receives the selected objects; {@code null} for no listener.
	 */
	void setSelectionListener(Consumer<Set<Object>> listener);

}
