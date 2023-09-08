/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.server.ui;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.graph.server.model.GraphDropTarget;
import com.top_logic.graph.server.model.NoGraphDrop;
import com.top_logic.layout.table.component.BuilderComponent;

/**
 * A general component for graphs.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public abstract class AbstractGraphComponent extends BuilderComponent {

	/**
	 * {@link AbstractGraphComponent} configuration.
	 *
	 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
	 */
	public interface Config extends BuilderComponent.Config {

		/**
		 * Property name of {@link #getGraphDrop()}.
		 */
		String GRAPH_DROP = "graphDrop";

		/**
		 * Algorithm handling drop operations on the displayed graph.
		 */
		@Name(GRAPH_DROP)
		@ItemDefault(NoGraphDrop.class)
		PolymorphicConfiguration<GraphDropTarget> getGraphDrop();
	}

	/**
	 * Creates a {@link AbstractGraphComponent} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public AbstractGraphComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

}
