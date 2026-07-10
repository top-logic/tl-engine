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
 * Typed arguments of the {@link ReactTreeControl} drop command: which tree node the drop targets and
 * the drop position relative to it.
 *
 * <p>
 * The {@link Label} doubles as the {@link com.top_logic.layout.form.values.edit.ConfigLabelProvider}
 * template that renders a recorded step for humans.
 * </p>
 */
@Label("Drop on node '{nodeId}'")
public interface DropArguments extends ReactCommand {

	/** @see #getNodeId() */
	String NODE_ID = "nodeId";

	/** @see #getPosition() */
	String POSITION = "position";

	/**
	 * The id of the tree node the drop targets.
	 */
	@Name(NODE_ID)
	@Mandatory
	String getNodeId();

	/**
	 * The drop position relative to the node (e.g. before/on/after).
	 */
	@Name(POSITION)
	String getPosition();

}
