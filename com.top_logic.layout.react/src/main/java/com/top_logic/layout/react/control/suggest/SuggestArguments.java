/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.suggest;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.layout.react.control.ReactCommand;

/**
 * Typed arguments of the command {@link #COMMAND} asking a control for the {@link Suggestion}s of
 * a {@link SuggestionSource}: the text typed so far and the caret position in it.
 *
 * <p>
 * The {@link Label} doubles as the recorder-step rendering template.
 * </p>
 */
@Label("Suggest for '{text}' in '{target}'")
public interface SuggestArguments extends ReactCommand {

	/** The name of the command asking for suggestions. */
	String COMMAND = "suggest";

	/** @see #getText() */
	String TEXT = "text";

	/** @see #getCaret() */
	String CARET = "caret";

	/**
	 * The text typed so far, {@code null} for an empty one.
	 */
	@Name(TEXT)
	@Nullable
	String getText();

	/**
	 * The caret position, a character offset in {@link #getText()}.
	 */
	@Name(CARET)
	int getCaret();

}
