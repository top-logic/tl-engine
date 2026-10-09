/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.kanban;

import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.react.control.ReactCommand;

/**
 * How a card selection of a {@link ReactKanbanBoardControl} combines with the selection before it.
 *
 * <p>
 * A click with {@code Ctrl} (or {@code Cmd}), and a long press on a touch screen,
 * {@link #isToggle() toggles}; a click with {@code Shift} {@link #isRange() extends}. A selection
 * with neither replaces the selection.
 * </p>
 */
public interface SelectCardModifiers extends ReactCommand {

	/** @see #isToggle() */
	String TOGGLE = "toggle";

	/** @see #isRange() */
	String RANGE = "range";

	/**
	 * Whether the card is added to the selection, or taken out of it when it is selected, instead
	 * of replacing the selection.
	 */
	@Name(TOGGLE)
	boolean isToggle();

	/** @see #isToggle() */
	void setToggle(boolean value);

	/**
	 * Whether the cards from the one clicked last up to this one are added to the selection.
	 */
	@Name(RANGE)
	boolean isRange();

	/** @see #isRange() */
	void setRange(boolean value);

}
