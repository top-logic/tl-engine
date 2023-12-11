/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.diagramjs.server;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.graph.common.model.Node;
import com.top_logic.graph.common.model.impl.SharedGraph;
import com.top_logic.graph.diagramjs.model.impl.DefaultDiagramJSGraphModel;
import com.top_logic.graph.diagramjs.server.util.GraphModelUtil;
import com.top_logic.graph.layouter.LayoutContext;
import com.top_logic.graph.server.model.GraphData;
import com.top_logic.graph.server.model.GraphDropEvent;
import com.top_logic.graph.server.model.GraphDropTarget;
import com.top_logic.layout.tree.model.DefaultTreeTableModel.DefaultTreeTableNode;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLType;

/**
 * {@link TLTypeDrop} drop in a {@link DiagramJSGraphControl}.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class TLTypeDrop implements GraphDropTarget {

	private LayoutComponent _component;

	/**
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Config for {@link TLTypeDrop}.
	 */
	public TLTypeDrop(InstantiationContext context, PolymorphicConfiguration<GraphDropTarget> config) {
		context.resolveReference(InstantiationContext.OUTER, LayoutComponent.class, c -> _component = c);
	}

	@Override
	public boolean dropEnabled(GraphData model) {
		return true;
	}

	@Override
	public void handleDrop(GraphDropEvent event) {
		SharedGraph graph = event.getTarget().getGraph();

		for (Object droppedObject : event.getData()) {
			DefaultTreeTableNode tableNode = (DefaultTreeTableNode) droppedObject;

			if (tableNode.getBusinessObject() instanceof TLType) {
				createDiagramJSGraphNode(event, tableNode, graph);
			}
		}
	}

	private void createDiagramJSGraphNode(GraphDropEvent event, DefaultTreeTableNode tableNode, SharedGraph graph) {
		TLType type = (TLType) tableNode.getBusinessObject();
		DiagramJSGraphComponent graphComponent = (DiagramJSGraphComponent) _component;

		if (graph.getGraphPart(type) == null) {
			DefaultDiagramJSGraphModel graphModel = (DefaultDiagramJSGraphModel) graph;

			LayoutContext context = graphComponent.getLayoutContext();

			Node node =
				GraphModelUtil.insertNodeIntoGraph(graphModel, type, context, graphComponent.getInvisibleGraphParts());

			node.setX(event.getX());
			node.setY(event.getY());
		}
	}

}
