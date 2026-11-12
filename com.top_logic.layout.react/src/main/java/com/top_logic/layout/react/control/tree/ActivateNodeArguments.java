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
import com.top_logic.layout.react.control.identity.DisplayedObjectId;

/**
 * Typed arguments of the {@link ReactTreeControl} activate command: which node the user opened by
 * double-clicking it, or by pressing {@code Enter} while it carried the keyboard focus.
 *
 * <p>
 * The {@link Label} doubles as the {@link com.top_logic.layout.form.values.edit.ConfigLabelProvider}
 * template that renders a recorded step for humans. The node is given by the id the tree displays
 * it under, which a recorded step carries as the business key of the node's object, see
 * {@link DisplayedObjectId}.
 * </p>
 */
@Label("Activate node '{nodeId}'")
public interface ActivateNodeArguments extends ReactCommand {

	/** @see #getNodeId() */
	String NODE_ID = SelectNodeArguments.NODE_ID;

	/**
	 * The id of the tree node to activate, as the tree published it in its
	 * {@link ReactTreeControl#NODES}.
	 */
	@Name(NODE_ID)
	@Mandatory
	@DisplayedObjectId
	String getNodeId();

}
