/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.tree;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.react.control.ModelKeyArguments;

/**
 * Arguments of the {@link ReactTreeControl} command opening the context menu of the node whose
 * business object has the given {@link #getKey() identity}, as a right-click on it does.
 *
 * <p>
 * The key is the identity the headless projection puts on each node, so an agent copies a node's
 * {@code key} verbatim into this command.
 * </p>
 */
@Label("Open context menu on node {key}")
public interface ContextMenuByKeyArguments extends ModelKeyArguments {

	/** @see #getX() */
	String X = ContextMenuArguments.X;

	/** @see #getY() */
	String Y = ContextMenuArguments.Y;

	/**
	 * The horizontal pixel position to open the menu at.
	 */
	@Name(X)
	@Mandatory
	int getX();

	/** @see #getX() */
	void setX(int value);

	/**
	 * The vertical pixel position to open the menu at.
	 */
	@Name(Y)
	@Mandatory
	int getY();

	/** @see #getY() */
	void setY(int value);

}
