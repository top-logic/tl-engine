/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.react.flow.operations;

import com.top_logic.react.flow.data.GraphEdge;
import com.top_logic.react.flow.svg.SvgWriter;

/**
 * Operations for a {@link GraphEdge}.
 */
public interface GraphEdgeOperations extends WidgetOperations {

	@Override
	GraphEdge self();

	@Override
	default void draw(SvgWriter out) {
		// Rendering is implemented in the react.flow module.
	}

}
