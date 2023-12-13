/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.diagramjs.server;

import com.top_logic.graph.common.model.GraphModel;
import com.top_logic.graph.common.model.impl.SharedGraph;
import com.top_logic.graph.diagramjs.server.util.GraphModelUtil;
import com.top_logic.graph.layouter.LayoutContext;
import com.top_logic.graph.layouter.Sugiyama;
import com.top_logic.graph.layouter.model.LayoutGraph;
import com.top_logic.graph.server.component.builder.GraphModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLType;

/**
 * Builds for the given model a layouted {@link SharedGraph}.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class DiagramJSGraphBuilder implements GraphModelBuilder {

	/**
	 * Singleton {@link DiagramJSGraphBuilder} instance.
	 */
	public static final DiagramJSGraphBuilder INSTANCE = new DiagramJSGraphBuilder();

	private DiagramJSGraphBuilder() {
		// Singleton constructor.
	}

	@Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
		return (aModel instanceof TLModule) || (aModel instanceof TLType);
	}

	@Override
	public void populateGraph(GraphModel graph) {
		// Nothing to do.
	}

	@Override
	public void handleModelCreated(GraphModel graph, Object newObject) {
		// Nothing to do.
	}

	@Override
	public void handleModelChanged(GraphModel graph, Object changedObject) {
		// Nothing to do.
	}

	@Override
	public void handleModelDeleted(GraphModel graph, Object deletedObject) {
		// Nothing to do.
	}

	@Override
	public SharedGraph getModel(Object businessModel, LayoutComponent component) {
		DiagramJSGraphComponent graphComponent = (DiagramJSGraphComponent) component;

		if (businessModel == null || !(businessModel instanceof TLModule)) {
			return null;
		}

		TLModule module = (TLModule) businessModel;

		LayoutContext context = getLayoutContext(graphComponent);

		LayoutGraph graph = GraphModelUtil.createLayoutGraph(module, context);

		Sugiyama.INSTANCE.layout(context, graph);

		SharedGraph graphModel =
			GraphModelUtil.createDiagramJSSharedGraphModel(context.getLabelProvider(), graph, module,
				context.getHiddenElements(), graphComponent.getInvisibleGraphParts());

		return graphModel;
	}

	private LayoutContext getLayoutContext(DiagramJSGraphComponent component) {
		return component.getLayoutContext();
	}

}
