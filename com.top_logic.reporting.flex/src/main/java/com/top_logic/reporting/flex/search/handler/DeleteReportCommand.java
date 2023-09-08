/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.search.handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.layout.meta.search.QueryUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.reporting.flex.search.SearchResultStoredReportSelector;
import com.top_logic.reporting.flex.search.model.FlexReport;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.CombinedExecutabilityRule;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.InViewModeExecutable;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link CommandHandler} to delete the currently selected {@link FlexReport}
 * 
 * @author <a href=mailto:cca@top-logic.com>cca</a>
 */
public class DeleteReportCommand extends AbstractCommandHandler {

	/**
	 * <code>COMMAND_ID</code>: The ID used to register this handler
	 */
	public static final String COMMAND_ID = "deleteStoredConfigChartReport";

	/**
	 * Configuration for {@link DeleteReportCommand}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends AbstractCommandHandler.Config {

		@FormattedDefault(QueryUtils.OWNER_WRITE_NAME)
		@Override
		CommandGroupReference getGroup();

	}

	/**
	 * Creates a new {@link DeleteReportCommand}. Default constructor for
	 * {@link CommandHandlerFactory}
	 */
	public DeleteReportCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public ResKey getDefaultI18NKey() {
		return I18NConstants.DELETE;
	}

	@Override
	public HandlerResult handleCommand(DisplayContext commandContext, LayoutComponent component,
			Object model, Map<String, Object> arguments) {
		SearchResultStoredReportSelector selector = (SearchResultStoredReportSelector) component;
		FlexReport report = selector.getCurrentSelection();
		HandlerResult result = new HandlerResult();

		if (report != null) {
			try {
				KnowledgeBase kb = report.getKnowledgeBase();
				try (Transaction tx = kb.beginTransaction()) {
					report.tDelete();
					tx.commit();
				}
				SelectField field = selector.getSelectorField();
				List<Object> options =
					new ArrayList<>(CollectionUtil.dynamicCastView(Object.class, field.getOptions()));
				options.remove(report);
				field.setAsSelection(Collections.EMPTY_LIST);
				field.setOptions(options);
			} catch (Exception ex) {
				result.setException(new TopLogicException(this.getClass(), "reporting.report.delete.failed", ex));
			}
		}

		return result;
	}

	@Override
	public ExecutabilityRule createExecutabilityRule() {
		return CombinedExecutabilityRule.combine(InViewModeExecutable.INSTANCE, NullReportDisabled.INSTANCE);
	}

}