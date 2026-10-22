/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.form;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.react.control.ReactCommand;

/**
 * Typed arguments of the {@link ReactFileChipsControl#CMD_REMOVE_CHIP} command: which of the files
 * to drop.
 *
 * <p>
 * The {@link Label} doubles as the {@link com.top_logic.layout.form.values.edit.ConfigLabelProvider}
 * template that renders a recorded step for humans.
 * </p>
 */
@Label("Remove file {key} of '{target}'")
public interface RemoveChipArguments extends ReactCommand {

	/** @see #getKey() */
	String KEY = "key";

	/**
	 * The key of the chip displaying the file to remove.
	 */
	@Name(KEY)
	@Mandatory
	String getKey();

}
