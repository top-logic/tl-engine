/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.diagramjs.server.handler;

import java.util.Collection;
import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.graph.common.model.GraphPart;
import com.top_logic.graph.common.model.impl.SharedGraph;
import com.top_logic.graph.diagramjs.model.DiagramJSGraphModel;
import com.top_logic.graph.diagramjs.server.DiagramJSGraphComponent;
import com.top_logic.graph.diagramjs.server.util.GraphModelUtil;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Handler to delete graph parts of a {@link SharedGraph}.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class DeleteSelectedSharedGraphPartHandler extends AbstractCommandHandler {

	/**
	 * Configuration of {@link DeleteSelectedSharedGraphPartHandler}.
	 *
	 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
	 */
	public interface Config extends AbstractCommandHandler.Config {

		/**
		 * Name of the graph component containing a shared graph.
		 */
		ComponentName getGraphComponentName();
	}

	/**
	 * Creates a {@link DeleteSelectedSharedGraphPartHandler} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public DeleteSelectedSharedGraphPartHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext context, LayoutComponent component, Object model,
			Map<String, Object> args) {
		DiagramJSGraphModel graphModel = getGraphModel(component);

		Collection<? extends GraphPart> selectedGraphParts = graphModel.getSelectedGraphParts();
		CommandHandler deleteHandler = getDeleteHandler();
		for (GraphPart part : selectedGraphParts) {
			deleteHandler.handleCommand(context, component, part.getTag(), args);
		}

		GraphModelUtil.removeGraphParts(graphModel, selectedGraphParts);

		return HandlerResult.DEFAULT_RESULT;
	}

	private DiagramJSGraphModel getGraphModel(LayoutComponent component) {
		ComponentName graphComponentName = ((Config) getConfig()).getGraphComponentName();
		DiagramJSGraphComponent graphComponent = (DiagramJSGraphComponent) component.getComponentByName(graphComponentName);
		return (DiagramJSGraphModel) graphComponent.getGraphModel();
	}

	private CommandHandler getDeleteHandler() {
		return CommandHandlerFactory.getInstance().getHandler("deleteServerGraphPart");
	}

}
