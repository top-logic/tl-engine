/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.server.ui;

import java.util.Collection;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.graphic.flow.data.Diagram;
import com.top_logic.graphic.flow.data.Widget;
import com.top_logic.mig.html.ModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link ModelBuilder} creating a {@link Diagram}.
 */
public interface FlowChartBuilder extends ModelBuilder {

	@Override
	Diagram getModel(Object businessModel, LayoutComponent aComponent);

	/**
	 * Find the business objects to observe for change for a given diagram element.
	 * 
	 * @param node
	 *        A diagram element.
	 * @param component
	 *        The diagram component.
	 */
	default Collection<?> getObserved(Widget node, LayoutComponent component) {
		return CollectionUtil.asList(node.getUserObject());
	}

}
