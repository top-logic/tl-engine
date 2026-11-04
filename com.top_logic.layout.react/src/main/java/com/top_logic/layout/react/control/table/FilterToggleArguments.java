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
 * The {@link Label} doubles as the {@link com.top_logic.layout.form.values.edit.ConfigLabelProvider}
 * template that renders a recorded step for humans.
 * </p>
 */
@Label("Toggle filter value '{key}'")
public interface FilterToggleArguments extends ReactCommand {

	/** @see #getKey() */
	String KEY = "key";

	/**
	 * The key of the value to check or uncheck, as the list tells it with each of its entries.
	 */
	@Name(KEY)
	@Mandatory
	String getKey();

}
