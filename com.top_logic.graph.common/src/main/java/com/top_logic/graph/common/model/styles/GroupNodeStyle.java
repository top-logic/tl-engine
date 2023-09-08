/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.common.model.styles;

import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.graph.common.model.geometry.Insets;
import com.top_logic.graph.common.model.layout.LabelLayout;

/**
 * {@link NodeStyle} for group nodes.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface GroupNodeStyle extends NodeStyle {

	/**
	 * Property name of {@link #getWrapped()}.
	 */
	String WRAPPED = "wrapped";

	/**
	 * Property name of {@link #getInsets()}.
	 */
	String INSETS = "insets";

	/**
	 * Property name of {@link #getButtonPlacement()}.
	 */
	String BUTTON_PLACEMENT = "buttonPlacement";

	/**
	 * Insets for group nodes to prevent content nodes from overlapping with visual elements created
	 * by this style.
	 */
	@Name(INSETS)
	@ItemDefault
	Insets getInsets();

	/**
	 * Delegate style that is responsible for the visual appearance.
	 */
	@Name(WRAPPED)
	@Mandatory
	@DefaultContainer
	NodeStyle getWrapped();

	/**
	 * Determines the positioning of the expand/collapse button.
	 */
	@Name(BUTTON_PLACEMENT)
	LabelLayout getButtonPlacement();

}
