/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.server.ui;

import com.top_logic.graphic.flow.model.FlowDiagram;
import com.top_logic.mig.html.ModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link ModelBuilder} creating a {@link FlowDiagram}.
 */
public interface FlowChartBuilder extends ModelBuilder {

	@Override
	FlowDiagram getModel(Object businessModel, LayoutComponent aComponent);

}
