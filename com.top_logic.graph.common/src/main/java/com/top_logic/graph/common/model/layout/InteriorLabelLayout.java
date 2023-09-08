/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.common.model.layout;

import com.top_logic.basic.config.annotation.Name;
import com.top_logic.graph.common.model.Label;
import com.top_logic.graph.common.model.Node;
import com.top_logic.graph.common.model.geometry.Insets;

/**
 * {@link LabelLayout} to place a {@link Label} within a {@link Node}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface InteriorLabelLayout extends LabelLayout {

	/**
	 * Property name of {@link #getPosition()}.
	 */
	String POSITION = "position";

	/**
	 * Property name of {@link #getInsets()}.
	 */
	String INSETS = "insets";

	/**
	 * Where to place the label inside the {@link Node}.
	 */
	@Name(POSITION)
	InteriorLayoutPosition getPosition();

	/**
	 * Placement offsets.
	 */
	@Name(INSETS)
	Insets getInsets();

}
