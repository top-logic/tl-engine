/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.search.handler;

import java.util.Map;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.layout.meta.expression.ExpressionSelectorComponent;
import com.top_logic.element.layout.meta.expression.NewExpressionCommand;
import com.top_logic.element.layout.meta.search.AttributedSearchResultSet;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseFactory;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.component.AbstractApplyCommandHandler;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.reporting.flex.search.NewStoredConfigChartReportComponent;
import com.top_logic.reporting.flex.search.SearchResultChartConfigComponent;
import com.top_logic.reporting.flex.search.model.FlexReport;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link CommandHandler} to create a {@link FlexReport} of format-version 2.
 * 
 * @author <a href=mailto:cca@top-logic.com>cca</a>
 */
public class CreateReportCommand extends NewExpressionCommand {


	/** The ID of this command handler. */
	public static final String COMMAND_ID = "newStoredConfigChartReport";

	/**
	 * Creates a {@link CreateReportCommand}.
	 */
	public CreateReportCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	private FlexReport createNewReport(KnowledgeBase kb, FormContext aContext, LayoutComponent aComponent)
			throws Exception {
		SearchResultChartConfigComponent master = getConfigurator(aComponent);

		AttributedSearchResultSet resultSet = (AttributedSearchResultSet) master.getModel();
		String typeNames = FlexReport.names(resultSet.getTypes());
		
		String theName = (String) (aContext.getField(NewStoredConfigChartReportComponent.NAME_ATTRIBUTE)).getValue();
		KnowledgeObject theObject = kb.createKnowledgeObject(FlexReport.KO_TYPE);
		FlexReport theStoredReport = (FlexReport) WrapperFactory.getWrapper(theObject);
		storeOwner(theStoredReport);
		String theString = master.getConfigString();
		theStoredReport.setValue(FlexReport.ATTRIBUTE_NAME, theName);
		theStoredReport.setValue(FlexReport.ATTRIBUTE_REPORT, theString);
		theStoredReport.setValue(FlexReport.ATTRIBUTE_BO_TYPE, typeNames);
		theStoredReport.setValue(FlexReport.ATTRIBUTE_VERSION, NewStoredConfigChartReportComponent.STORED_REPORT_VERSION);
		publish(theStoredReport, aContext);
		return theStoredReport;
	}

	@Override
	public Object createObject(LayoutComponent component, Object createContext, FormContainer formContainer,
			Map<String, Object> arguments) {
	    return null;
	}
	
	@Override
	public HandlerResult handleCommand(DisplayContext aCommandContext, LayoutComponent aComponent, Object model, Map<String, Object> aArguments) {
		FormComponent comp = (FormComponent) aComponent;
		FormContext context = comp.getFormContext();
		ResKey error = null;
		FlexReport report = null;

		if (context.checkAll()) {
			try {
				KnowledgeBase kb = KnowledgeBaseFactory.getInstance().getDefaultKnowledgeBase();
				try (Transaction tx = kb.beginTransaction()) {
					report = createNewReport(kb, context, aComponent);
					if (report != null) {
						tx.commit();
						SearchResultChartConfigComponent master = getConfigurator(aComponent);
						ExpressionSelectorComponent<?> selectorComponent = (ExpressionSelectorComponent<?>) master.getSelector();
						selectorComponent.getSelectorField().setAsSingleSelection(report);
					} else {
						tx.rollback();
					}
				}
			} catch (Exception ex) {
				error = I18NConstants.FAILED;
				Logger.error("Unable to create a new query", ex, this);
			}
		}
		else {
			HandlerResult result = new HandlerResult();
			AbstractApplyCommandHandler.fillHandlerResultWithErrors(null, context, result);
			return result;
		}

		if (error != null) {
			HandlerResult result = new HandlerResult();
			result.addErrorMessage(error);
			return result;
		}
		else {
			comp.closeDialog();
			return HandlerResult.DEFAULT_RESULT;
		}
	}

	private SearchResultChartConfigComponent getConfigurator(LayoutComponent aComponent) {
		return (SearchResultChartConfigComponent) aComponent.getDialogParent();
	}
}