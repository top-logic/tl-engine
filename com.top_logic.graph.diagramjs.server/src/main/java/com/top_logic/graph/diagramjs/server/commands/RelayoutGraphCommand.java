/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.diagramjs.server.commands;

import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.graph.common.model.impl.SharedGraph;
import com.top_logic.graph.diagramjs.server.DiagramJSGraphComponent;
import com.top_logic.graph.diagramjs.server.util.GraphModelUtil;
import com.top_logic.graph.layouter.LayoutContext;
import com.top_logic.graph.layouter.Sugiyama;
import com.top_logic.graph.layouter.model.LayoutGraph;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLModule;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Command to relayout the given graph.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class RelayoutGraphCommand extends AbstractCommandHandler {

	/**
	 * Creates a {@link RelayoutGraphCommand}.
	 * 
	 * @param context
	 *        {@link InstantiationContext} context to instantiate sub configurations.
	 * @param config
	 *        {@link RelayoutGraphCommand} configuration.
	 */
	public RelayoutGraphCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent component, Object model,
			Map<String, Object> someArguments) {
		DiagramJSGraphComponent graphComponent = (DiagramJSGraphComponent) component;
		LayoutContext context = graphComponent.getLayoutContext();

		TLModule enclosingModule = GraphModelUtil.getEnclosingModule(model);
		LayoutGraph graph = GraphModelUtil.createLayoutGraph(enclosingModule, context.showTableInterfaceTypes());
		Sugiyama.INSTANCE.layout(context, graph);

		SharedGraph graphModel =
			GraphModelUtil.createDiagramJSSharedGraphModel(context.getLabelProvider(), graph, enclosingModule);
		graphModel.setSelectedGraphParts(graphComponent.getGraphModel().getSelectedGraphParts());

		graphComponent.setGraphModel(graphModel);

		return HandlerResult.DEFAULT_RESULT;
	}

}
