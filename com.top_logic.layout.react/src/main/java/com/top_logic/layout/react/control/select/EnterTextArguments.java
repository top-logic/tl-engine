/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.select;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.layout.react.control.ReactCommand;

/**
 * Typed arguments of the {@link ReactDropdownSelectControl} command {@link #COMMAND}: the text the
 * user entered into a select field that {@link ReactDropdownSelectControl#hasFreeEntry() accepts
 * free entry}.
 *
 * <p>
 * The text names the value itself rather than an option id allocated for the session, so a
 * recorded entry replays in a later session as it stands. The {@link Label} doubles as the
 * recorder-step rendering template.
 * </p>
 */
@Label("Enter '{text}' in '{target}'")
public interface EnterTextArguments extends ReactCommand {

	/** The name of the command entering a text. */
	String COMMAND = "enterText";

	/** @see #getText() */
	String TEXT = "text";

	/**
	 * The text the user entered, {@code null} for an empty one.
	 */
	@Name(TEXT)
	@Nullable
	String getText();

	/**
	 * Sets {@link #getText()}.
	 */
	void setText(String text);

}
