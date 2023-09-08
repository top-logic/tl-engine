/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.server.component.builder;

import com.top_logic.graph.common.model.GraphModel;
import com.top_logic.graph.common.model.impl.SharedGraph;
import com.top_logic.mig.html.ModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link ModelBuilder} creating a {@link GraphModel} allowing to update this model through the
 * {@link GraphBuilder} interface.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface GraphModelBuilder extends ModelBuilder, GraphBuilder {

	@Override
	SharedGraph getModel(Object businessModel, LayoutComponent aComponent);

}
