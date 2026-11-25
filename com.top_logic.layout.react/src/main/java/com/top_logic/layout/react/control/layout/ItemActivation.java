/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.layout;

/**
 * What activating an item of an activatable {@link ReactLayoutControl} runs, see
 * {@link ReactLayoutControl#setActivation(ItemActivation, boolean)}.
 *
 * <p>
 * The activation decides per object: an item whose object it offers no {@link #action(Object)
 * action} for cannot be activated, one whose action is {@link ItemAction#disabled() disabled}
 * shows it, and activating it has no effect. When what it offers changes, the owner of the
 * activation reports it to the container ({@link ReactLayoutControl#updateActivation()}).
 * </p>
 */
public interface ItemActivation {

	/**
	 * What activating the item of the given object offers.
	 *
	 * @param item
	 *        The object of a displayed item.
	 * @return The action of the item, {@code null} for an item that cannot be activated.
	 */
	ItemAction action(Object item);

	/**
	 * Runs the activation for the item of the given object, as the user opens it.
	 *
	 * <p>
	 * An object the activation refuses - the action is disabled or not offered for it - is not
	 * activated, without a message: the user may just have picked the item.
	 * </p>
	 *
	 * @param item
	 *        The object of the activated item.
	 */
	void activate(Object item);

}
