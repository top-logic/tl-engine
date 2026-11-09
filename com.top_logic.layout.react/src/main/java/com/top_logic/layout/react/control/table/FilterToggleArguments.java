/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.table;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.react.control.ReactCommand;

/**
 * Typed arguments of the {@link ReactFilterListControl} {@code toggle} command: which value of the
 * list to check or uncheck.
 *
 * <p>
 * The value is named by its {@link #getKey() key}, its position among the options - the identity the
 * options filter itself stores a selection by, independent of the language and of the type of the
 * value. A recorded step adds the {@link #getLabel() label} of the value for its reader.
 * </p>
 *
 * <p>
 * The {@link Label} doubles as the {@link com.top_logic.layout.form.values.edit.ConfigLabelProvider}
 * template that renders a recorded step for humans.
 * </p>
 */
@Label("Toggle filter value '{label}'")
public interface FilterToggleArguments extends ReactCommand {

	/** @see #getKey() */
	String KEY = "key";

	/** @see #getLabel() */
	String LABEL = "label";

	/**
	 * The key of the value to check or uncheck, as the list tells it with each of its entries.
	 */
	@Name(KEY)
	@Mandatory
	String getKey();

	/** @see #getKey() */
	void setKey(String value);

	/**
	 * The label of the value as the list displayed it when the step was recorded; only for the reader
	 * of the step, the value is found by its {@link #getKey() key}.
	 */
	@Name(LABEL)
	String getLabel();

	/** @see #getLabel() */
	void setLabel(String value);

}
