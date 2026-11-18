/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.select;

import java.util.List;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.json.JsonBinding;
import com.top_logic.basic.config.json.SimpleJsonListBinding;
import com.top_logic.layout.react.control.ReactCommand;
import com.top_logic.layout.react.control.form.FieldValueArguments;
import com.top_logic.layout.react.control.form.ReactFormFieldControl;
import com.top_logic.layout.react.control.identity.DisplayedObjectId;

/**
 * Typed arguments of the {@link ReactFormFieldControl#CMD_VALUE_CHANGED} command of a
 * {@link ReactDropdownSelectControl}: the options that make up the new value.
 *
 * <p>
 * The value of a select is a list of options, not the text the base field assumes - hence its own
 * typed arguments. An option is given by the id the control displays it under, which a recorded
 * step carries as the key of the option, see {@link DisplayedObjectId}.
 * </p>
 */
@Label("Set '{target}' to '{value}'")
public interface SelectValueArguments extends ReactCommand {

	/**
	 * The ids of the options that make up the new value, in the order the value holds them; an
	 * empty list clears the field.
	 *
	 * <p>
	 * Each is the id under which the control displays the option, as projected for an agent and
	 * as the client receives it in the option descriptors.
	 * </p>
	 */
	@Name(FieldValueArguments.VALUE)
	@DisplayedObjectId
	@JsonBinding(SimpleJsonListBinding.class)
	List<String> getValue();

}
