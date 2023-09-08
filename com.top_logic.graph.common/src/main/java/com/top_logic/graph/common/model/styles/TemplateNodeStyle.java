/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.common.model.styles;

import com.top_logic.basic.config.annotation.Binding;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.config.format.XMLFragmentString;
import com.top_logic.graph.common.model.geometry.Insets;

/**
 * {@link NodeStyle} that renders an SVG template.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface TemplateNodeStyle extends NodeStyle {

	/**
	 * The SVG template content.
	 */
	@Name("svgContent")
	@Binding(XMLFragmentString.class)
	String getSvgContent();

	/**
	 * Insets that define the inside of the node, where content nodes may be rendered.
	 */
	@Name("insets")
	@ItemDefault
	Insets getInsets();

}
