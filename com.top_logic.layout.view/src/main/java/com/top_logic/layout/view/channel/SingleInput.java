/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.channel;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Format;

/**
 * The single channel whose value a configured element reads as a named value.
 *
 * <p>
 * The counterpart of {@link Inputs} for an element working on exactly one object: its scripts see
 * the value of the {@link #getInput() input} as a variable named after the channel, or by the name
 * given with {@value CommaSeparatedInputRefs#AS}, see {@link InputScript}.
 * </p>
 */
public interface SingleInput extends ConfigurationItem {

	/** Configuration name for {@link #getInput()}. */
	String INPUT = "input";

	/**
	 * The channel whose current value the scripts of this configuration see as a variable.
	 *
	 * <p>
	 * The variable is named after the channel, or by the name given with
	 * {@value CommaSeparatedInputRefs#AS}:
	 * </p>
	 *
	 * <pre>
	 * input="selection as ticket"
	 * </pre>
	 */
	@Name(INPUT)
	@Format(InputRefFormat.class)
	InputRef getInput();

}
