/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.search.handler;

import java.util.Map;

import com.top_logic.base.services.simpleajax.AJAXCommandHandler;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.reporting.flex.search.SearchResultStoredReportSelector;
import com.top_logic.reporting.flex.search.model.FlexReport;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.CombinedExecutabilityRule;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.InViewModeExecutable;

/**
 * {@link CommandHandler} to reset to the persistent state of the currently selected
 * {@link FlexReport}
 * 
 * @author <a href=mailto:cca@top-logic.com>cca</a>
 */
public class ResetReportCommand extends AJAXCommandHandler {

	/**
	 * <code>COMMAND_ID</code>: The ID used to register this handler
	 */
	public static final String COMMAND_ID = "resetStoredConfigChartReport";

	/**
	 * Creates a new {@link ResetReportCommand}. Default constructor for
	 * {@link CommandHandlerFactory}
	 */
	public ResetReportCommand(InstantiationContext context, Config config) {
		super(context, config);
		this.confirm = true;
	}

	@Override
	public HandlerResult handleCommand(DisplayContext context, LayoutComponent component,
			Object model, Map<String, Object> someArguments) {
		SearchResultStoredReportSelector selector = (SearchResultStoredReportSelector) component;
		FlexReport report = selector.getCurrentSelection();
		if (report == null) {
			selector.getTarget().resetGUI();
		} else {
			selector.loadReport(report);
		}
		return HandlerResult.DEFAULT_RESULT;
	}

	@Override
	public ExecutabilityRule createExecutabilityRule() {
		return CombinedExecutabilityRule.combine(InViewModeExecutable.INSTANCE, NullReportDisabled.INSTANCE);
	}
}