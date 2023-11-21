/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.diagramjs.server;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.graph.server.model.GraphData;
import com.top_logic.layout.structure.ContentLayoutControlProvider;
import com.top_logic.layout.structure.LayoutControlProvider;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link LayoutControlProvider} for a graph structure.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class DiagramJSGraphControlProvider<C extends DiagramJSGraphControlProvider.Config<?>> extends ContentLayoutControlProvider<C> {

	/**
	 * Configuration options for {@link DiagramJSGraphControlProvider}.
	 */
	public interface Config<I extends DiagramJSGraphControlProvider<?>> extends ContentLayoutControlProvider.Config<I> {
		// Pure marker interface.
	}

	/**
	 * Creates a {@link DiagramJSGraphControlProvider} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public DiagramJSGraphControlProvider(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	protected HTMLFragment createView(LayoutComponent component) {
		GraphData graphData = getGraphData(component);

		return new DiagramJSGraphControl(graphData, (DiagramJSGraphComponent) component);
	}

	private GraphData getGraphData(LayoutComponent component) {
		DiagramJSGraphComponent graphComponent = (DiagramJSGraphComponent) component;

		return graphComponent.getGraphData();
	}

}