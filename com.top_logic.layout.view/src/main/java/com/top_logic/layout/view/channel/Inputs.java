/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.channel;

import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.basic.config.annotation.Name;

/**
 * The channels a configured element or action reads the leading arguments of its functions from.
 *
 * <p>
 * A function of a view computes its result from more than the value it is applied to: from what is
 * selected elsewhere, from what a filter above it holds, from the object a dialog was opened for.
 * Such a value comes from a {@link ViewChannel} the configuration names as an input, and every
 * function of that configuration receives the values of those channels ahead of the arguments it
 * is called with.
 * </p>
 *
 * @implNote {@link ChannelInputs} resolves the declared references against the session and builds
 *           the argument arrays the functions are called with.
 */
public interface Inputs extends ConfigurationItem {

	/** Configuration name for {@link #getInputs()}. */
	String INPUTS = "inputs";

	/**
	 * References to the {@link ViewChannel}s whose current values become the leading positional
	 * arguments of this configuration's TL-Script functions, in declaration order.
	 *
	 * <p>
	 * The channels are read whenever a function is applied, so what a function computes follows
	 * what the referenced channels currently hold.
	 * </p>
	 *
	 * <p>
	 * The references are written either as a comma-separated attribute:
	 * </p>
	 *
	 * <pre>
	 * inputs="selectedCustomer, editMode"
	 * </pre>
	 *
	 * <p>
	 * or as nested elements, one per channel:
	 * </p>
	 *
	 * <pre>
	 * &lt;inputs&gt;
	 *   &lt;input channel="selectedCustomer"/&gt;
	 *   &lt;input channel="editMode"/&gt;
	 * &lt;/inputs&gt;
	 * </pre>
	 */
	@Name(INPUTS)
	@Format(CommaSeparatedChannelRefs.class)
	@ListBinding(format = ChannelRefFormat.class, tag = "input", attribute = "channel")
	List<ChannelRef> getInputs();

}
