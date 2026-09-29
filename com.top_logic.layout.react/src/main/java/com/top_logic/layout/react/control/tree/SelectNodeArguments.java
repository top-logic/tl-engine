/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.tree;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.react.control.ReactCommand;

/**
 * Typed arguments of the {@link ReactTreeControl} select command: which tree node to select and the
 * modifier keys that turn the gesture into a toggle or range selection.
 *
 * <p>
 * The {@link Label} doubles as the {@link com.top_logic.layout.form.values.edit.ConfigLabelProvider}
 * template that renders a recorded step for humans.
 * </p>
 */
@Label("Select node '{nodeId}'")
public interface SelectNodeArguments extends ReactCommand {

	/** @see #getNodeId() */
	String NODE_ID = "nodeId";

	/** @see #isCtrlKey() */
	String CTRL_KEY = "ctrlKey";

	/** @see #isShiftKey() */
	String SHIFT_KEY = "shiftKey";

	/**
	 * The id of the tree node to select.
	 */
	@Name(NODE_ID)
	@Mandatory
	String getNodeId();

	/**
	 * Whether to toggle the node's selection (multi-select), as a {@code Ctrl}-click does.
	 */
	@Name(CTRL_KEY)
	boolean isCtrlKey();

	/**
	 * Whether to extend the selection as a range from the anchor (multi-select), as a
	 * {@code Shift}-click does.
	 */
	@Name(SHIFT_KEY)
	boolean isShiftKey();

}
