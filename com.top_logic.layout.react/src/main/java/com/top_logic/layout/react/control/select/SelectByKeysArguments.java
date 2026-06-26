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
 * Typed arguments of the dropdown {@code selectByKey} command: the option business keys to select.
 * This is the replay-stable form a recorded selection takes, so it resolves again in a later
 * session.
 *
 * <p>
 * The {@link Label} doubles as the recorder-step rendering template.
 * </p>
 */
@Label("Select options by key")
public interface SelectByKeysArguments extends ReactCommandArguments {

	/** @see #getKeys() */
	String KEYS = "keys";

	/**
	 * The business keys (the {@code key} projected onto each option) of the options to select.
	 */
	@Name(KEYS)
	List<String> getKeys();

}
