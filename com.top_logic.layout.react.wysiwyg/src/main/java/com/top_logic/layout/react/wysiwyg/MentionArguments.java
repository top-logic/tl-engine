/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.wysiwyg;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.layout.react.control.ReactCommand;

/**
 * Typed arguments of the command {@link #COMMAND} asking a {@link ReactWysiwygControl} for the
 * objects a {@link Mention} offers: the trigger character typed and the text typed after it.
 *
 * <p>
 * The {@link Label} doubles as the recorder-step rendering template.
 * </p>
 */
@Label("Mention '{trigger}{query}' in '{target}'")
public interface MentionArguments extends ReactCommand {

	/** The name of the command asking for the objects of a mention. */
	String COMMAND = "mention";

	/** @see #getTrigger() */
	String TRIGGER = "trigger";

	/** @see #getQuery() */
	String QUERY = "query";

	/**
	 * The character that started the mention, the {@link Mention#getTrigger() trigger} of the
	 * mention asked for.
	 */
	@Name(TRIGGER)
	@Nullable
	String getTrigger();

	/**
	 * The text typed after the trigger character, {@code null} for none.
	 */
	@Name(QUERY)
	@Nullable
	String getQuery();

}
