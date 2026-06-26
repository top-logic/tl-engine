/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.table;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.react.control.ReactCommandArguments;

/**
 * Typed arguments of the {@link TableViewControl} {@code selectByKey} command: the business key of
 * the row to select, as projected onto each row.
 *
 * <p>
 * The {@link Label} doubles as the {@link com.top_logic.layout.form.values.edit.ConfigLabelProvider}
 * template that renders a recorded step for humans.
 * </p>
 */
@Label("Select row '{key}'")
public interface SelectByKeyArguments extends ReactCommandArguments {

	/** @see #getKey() */
	String KEY = "key";

	/**
	 * Business key of the row to select (the {@code key} projected onto each row).
	 */
	@Name(KEY)
	@Mandatory
	String getKey();

}
