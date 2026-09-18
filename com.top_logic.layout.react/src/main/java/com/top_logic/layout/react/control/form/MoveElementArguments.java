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
 * Typed arguments of the {@link ReactValueListControl#CMD_MOVE_ELEMENT} command: which of the values
 * to move, and where to.
 *
 * <p>
 * The {@link Label} doubles as the {@link com.top_logic.layout.form.values.edit.ConfigLabelProvider}
 * template that renders a recorded step for humans.
 * </p>
 */
@Label("Move value {index} of '{target}' to position {targetIndex}")
public interface MoveElementArguments extends ReactCommand {

	/** @see #getIndex() */
	String INDEX = "index";

	/** @see #getTargetIndex() */
	String TARGET_INDEX = "targetIndex";

	/**
	 * The position of the value to move, counted from {@code 0}.
	 */
	@Name(INDEX)
	@Mandatory
	int getIndex();

	/**
	 * The position the moved value ends up at, counted from {@code 0}.
	 *
	 * <p>
	 * The position within the values as they are after the move, so that the last position is one
	 * less than the number of values, whether the value moves forwards or backwards.
	 * </p>
	 */
	@Name(TARGET_INDEX)
	@Mandatory
	int getTargetIndex();

}
