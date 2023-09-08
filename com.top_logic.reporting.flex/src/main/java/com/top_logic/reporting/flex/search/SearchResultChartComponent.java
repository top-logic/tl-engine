/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.search;


import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.knowledge.searching.SearchResultSet;
import com.top_logic.mig.html.layout.CommandRegistry;
import com.top_logic.reporting.flex.chart.component.ConfiguredChartComponent;
import com.top_logic.reporting.flex.search.handler.DisplayDetailsCommand;
import com.top_logic.tool.boundsec.CommandHandler;

/**
 * {@link ConfiguredChartComponent} with registered detail-handler that works with
 * {@link SearchResultSet}.
 * 
 * @author <a href="mailto:cca@top-logic.com">cca</a>
 */
public class SearchResultChartComponent extends ConfiguredChartComponent {
	
	/**
	 * Configuration for the {@link SearchResultChartComponent}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends ConfiguredChartComponent.Config {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		@Override
		default void modifyIntrinsicCommands(CommandRegistry registry) {
			ConfiguredChartComponent.Config.super.modifyIntrinsicCommands(registry);
			registry.registerButton(DisplayDetailsCommand.COMMAND_ID);
		}

	}

	/**
	 * Creates a new {@link SearchResultChartComponent}
	 */
	public SearchResultChartComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

	@Override
	protected CommandHandler getDetailsHandler() {
		return getCommandById(DisplayDetailsCommand.COMMAND_ID);
	}

}
