/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.select;

import java.util.List;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.react.control.ReactCommandArguments;

/**
 * Typed arguments of the dropdown {@code valueChanged} command: the option value ids the client
 * selected (the {@code value} ids from the streamed option descriptors).
 *
 * <p>
 * The {@link Label} doubles as the recorder-step rendering template. For a replay-stable recording
 * the dropdown rewrites this command to a {@code selectByKey} of business keys; see
 * {@link ReactDropdownSelectControl}.
 * </p>
 */
@Label("Change the selection")
public interface OptionsChangedArguments extends ReactCommandArguments {

	/** @see #getValue() */
	String VALUE = "value";

	/**
	 * The session-allocated value ids of the selected options.
	 */
	@Name(VALUE)
	List<String> getValue();

}
