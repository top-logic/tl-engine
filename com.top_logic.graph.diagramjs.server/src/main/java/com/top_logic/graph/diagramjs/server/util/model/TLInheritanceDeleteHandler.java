/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.diagramjs.server.util.model;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.graph.common.model.GraphPart;
import com.top_logic.graph.diagramjs.model.DiagramJSGraphModel;
import com.top_logic.graph.diagramjs.server.DiagramJSGraphComponent;
import com.top_logic.graph.diagramjs.server.util.GraphModelUtil;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Delete handler for {@link TLInheritance}.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class TLInheritanceDeleteHandler extends AbstractCommandHandler {

	/**
	 * Configuration of {@link TLInheritanceDeleteHandler}.
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
	 * Creates a {@link TLInheritanceDeleteHandler} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public TLInheritanceDeleteHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext context, LayoutComponent component, Object model,
			Map<String, Object> args) {
		if (model instanceof TLInheritance) {
			ComponentName graphComponentName = ((Config) getConfig()).getGraphComponentName();
			DiagramJSGraphComponent graphComponent =
				(DiagramJSGraphComponent) component.getComponentByName(graphComponentName);

			DiagramJSGraphModel graphModel = (DiagramJSGraphModel) graphComponent.getGraphModel();
			Collection<? extends GraphPart> selectedGraphParts = graphModel.getSelectedGraphParts();

			GraphModelUtil.deleteInheritance((TLInheritance) model, graphComponent);

			GraphModelUtil.removeGraphParts(graphModel, selectedGraphParts);
			graphModel.setSelectedGraphParts(Collections.emptySet());
		}

		return HandlerResult.DEFAULT_RESULT;
	}

}
