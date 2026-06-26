/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.tree;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.react.control.ReactCommandArguments;

/**
 * Typed arguments of the {@link ReactTreeControl} context-menu command: which tree node the menu
 * targets and the pixel position to open it at.
 *
 * <p>
 * The {@link Label} doubles as the {@link com.top_logic.layout.form.values.edit.ConfigLabelProvider}
 * template that renders a recorded step for humans.
 * </p>
 */
@Label("Open context menu on node '{nodeId}'")
public interface ContextMenuArguments extends ReactCommandArguments {

	/** @see #getNodeId() */
	String NODE_ID = "nodeId";

	/** @see #getX() */
	String X = "x";

	/** @see #getY() */
	String Y = "y";

	/**
	 * The id of the tree node the context menu targets.
	 */
	@Name(NODE_ID)
	@Mandatory
	String getNodeId();

	/**
	 * The horizontal pixel position to open the menu at.
	 */
	@Name(X)
	@Mandatory
	int getX();

	/**
	 * The vertical pixel position to open the menu at.
	 */
	@Name(Y)
	@Mandatory
	int getY();

}
