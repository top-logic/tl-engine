/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.search.handler;

import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.element.layout.meta.expression.SaveExpressionCommand;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.component.AbstractApplyCommandHandler;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.reporting.flex.search.SearchResultChartConfigComponent;
import com.top_logic.reporting.flex.search.SearchResultStoredReportSelector;
import com.top_logic.reporting.flex.search.model.FlexReport;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.CombinedExecutabilityRule;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.InViewModeExecutable;

/**
 * {@link CommandHandler} to store the changes to the currently selected {@link FlexReport}
 * 
 * @author <a href=mailto:cca@top-logic.com>cca</a>
 */
public class SaveReportCommand extends SaveExpressionCommand {

	/**
	 * <code>COMMAND_ID</code>: The ID used to register this handler
	 */
	public static final String COMMAND_ID = "writeSearchChartReport";

	/**
	 * Creates a new {@link SaveReportCommand}. Default constructor for
	 * {@link CommandHandlerFactory}
	 */
	public SaveReportCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext context, LayoutComponent component,
			Object model, Map<String, Object> someArguments) {
		SearchResultStoredReportSelector selector = (SearchResultStoredReportSelector) component;
		FlexReport report = selector.getCurrentSelection();
		if (report == null) {
			return HandlerResult.DEFAULT_RESULT;
		}
		SearchResultChartConfigComponent target = selector.getTarget();
		String configString = target.getConfigString();
		FormContext configContext = target.getFormContext();
		FormContext selectorContext = selector.getFormContext();

		if (!configContext.checkAll()) {
			HandlerResult result = new HandlerResult();
			AbstractApplyCommandHandler.fillHandlerResultWithErrors(null, configContext, result);
			return result;
		}
		try {
			KnowledgeBase kb = report.getKnowledgeBase();
			try (Transaction tx = kb.beginTransaction()) {
				publish(report, selectorContext);
				report.setValue(FlexReport.ATTRIBUTE_REPORT, configString);
				tx.commit();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return HandlerResult.DEFAULT_RESULT;
	}

	@Override
	public ExecutabilityRule createExecutabilityRule() {
		return CombinedExecutabilityRule.combine(InViewModeExecutable.INSTANCE, NullReportDisabled.INSTANCE);
	}
}