/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.channel;

import java.util.List;

import com.top_logic.basic.config.container.ConfigPart;
import com.top_logic.basic.config.annotation.Binding;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;

/**
 * The channels whose values a command or an action reads as named arguments.
 *
 * <p>
 * Each channel's value is an argument named after the channel, or by the name given with
 * {@code as}. A TL-Script of the configuration refers to the argument as a variable of that name.
 * </p>
 */
public interface NamedInputs extends ConfigPart {

	/** Configuration name for {@link #getInputs()}. */
	String INPUTS = "inputs";

	/**
	 * The channels whose current values are named arguments, in declaration order.
	 *
	 * <p>
	 * The channels are written either as a comma-separated attribute, an entry optionally renaming
	 * its argument:
	 * </p>
	 *
	 * <pre>
	 * inputs="selection as ticket, editMode"
	 * </pre>
	 *
	 * <p>
	 * or as nested elements, one per channel:
	 * </p>
	 *
	 * <pre>
	 * &lt;inputs&gt;
	 *   &lt;input channel="selection" as="ticket"/&gt;
	 *   &lt;input channel="editMode"/&gt;
	 * &lt;/inputs&gt;
	 * </pre>
	 */
	@Name(INPUTS)
	@Format(CommaSeparatedInputRefs.class)
	@Binding(InputRefsBinding.class)
	List<InputRef> getInputs();

}
