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
import com.top_logic.layout.scripting.recorder.ref.ModelName;

/**
 * The {@link TableViewControl} {@code selectByKey} command: selects the row whose business object
 * has the given {@link ModelName identity}.
 *
 * <p>
 * The key is the identity the headless projection puts on each row, so an agent copies a row's
 * {@code key} verbatim into this command. The {@link Label} doubles as the
 * {@link com.top_logic.layout.form.values.edit.ConfigLabelProvider} template that renders a recorded
 * step for humans — the {@link ModelName} renders through its own label, i.e. by the row's readable
 * identity.
 * </p>
 */
@Label("Select row {key}")
public interface SelectByKeyArguments extends ReactCommand {

	/** @see #getKey() */
	String KEY = "key";

	/**
	 * The business identity of the row to select (the {@code key} projected onto each row).
	 */
	@Name(KEY)
	@Mandatory
	ModelName getKey();

	/** @see #getKey() */
	void setKey(ModelName value);

}
