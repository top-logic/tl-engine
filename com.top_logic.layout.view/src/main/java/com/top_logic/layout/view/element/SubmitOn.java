/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import com.top_logic.basic.config.ExternallyNamed;

/**
 * When a {@link ValueInputElement} runs its submit command on a value the user types - a text, a
 * number.
 *
 * <p>
 * The entered value reaches the value channel in either case, when the input is left as well as on
 * Enter. Only whether the command runs at that point depends on this choice. A value that is picked
 * - from a dropdown, a date picker, a checkbox - runs the command with every choice, whichever value
 * is configured.
 * </p>
 */
public enum SubmitOn implements ExternallyNamed {

	/**
	 * The command runs when the user presses Enter in the input.
	 *
	 * <p>
	 * For an input whose value is a request, such as a search term or the key of an object to jump
	 * to: leaving the input after typing keeps the value without acting on it. A text area, in
	 * which Enter starts a new line, runs no command then.
	 * </p>
	 */
	ENTER("enter"),

	/**
	 * The command runs on each edit the user finishes: by pressing Enter, or by leaving the input
	 * after changing its value - once per edit.
	 *
	 * <p>
	 * For an input whose value is applied as soon as it is entered, such as a setting the view
	 * acts on.
	 * </p>
	 */
	CHANGE("change");

	private final String _externalName;

	SubmitOn(String externalName) {
		_externalName = externalName;
	}

	@Override
	public String getExternalName() {
		return _externalName;
	}
}
