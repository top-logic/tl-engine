/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.common.model.styles;

import com.top_logic.basic.config.annotation.Name;
import com.top_logic.graph.common.model.Node;

/**
 * {@link NodeStyle} for collapsible {@link Node}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface CollapsibleNodeStyle extends DecoratorNodeStyle {

	/** Property name of {@link #getCollapse()}. */
	String COLLAPSE = "collapse";

	/**
	 * Whether the user is allowed to collapse and expand nodes.
	 */
	@Name(COLLAPSE)
	boolean getCollapse();

}
