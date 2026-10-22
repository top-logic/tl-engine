/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.channel;

import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Binding;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;

/**
 * The channels whose values a configured element, channel, command or action reads as named
 * values.
 *
 * <p>
 * A script of a view computes its result from more than the value it is applied to: from what is
 * selected elsewhere, from what a filter above it holds, from the object a dialog was opened for.
 * Such a value comes from a {@link ViewChannel} the configuration names as an input. Each input is
 * a variable of the configuration's scripts, named after the channel, or by the name given with
 * {@value CommaSeparatedInputRefs#AS}. A script refers to it as <code>$name</code> without declaring
 * it.
 * </p>
 *
 * @see InputScript
 * @see NamedInputs
 */
public interface Inputs extends ConfigurationItem {

	/** Configuration name for {@link #getInputs()}. */
	String INPUTS = "inputs";

	/**
	 * The channels whose current values the scripts of this configuration see as variables, in
	 * declaration order.
	 *
	 * <p>
	 * Each value is a variable named after its channel, or by the name given with
	 * {@value CommaSeparatedInputRefs#AS}. The channels are read whenever a script is evaluated, so
	 * what a script computes follows what the referenced channels currently hold.
	 * </p>
	 *
	 * <p>
	 * The channels are written either as a comma-separated attribute, an entry optionally renaming
	 * its variable:
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
