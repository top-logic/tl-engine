/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.selector;

import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.*;

import java.util.Collection;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.element.layout.meta.expression.SaveExpressionCommand;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.util.WrapperUtil;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.form.component.AbstractApplyCommandHandler;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.search.persistency.expressions.SearchExpressionImpl;
import com.top_logic.model.search.ui.GUISearchExpressionEditor;
import com.top_logic.model.search.ui.model.Search;
import com.top_logic.reporting.flex.search.SearchResultChartConfigComponent;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * The {@link Command} that saves the search configured in the given {@link GUISearchExpressionEditor}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class SaveSearchCommand extends SaveExpressionCommand {

	/**
	 * The id under which this {@link CommandHandler} is registered at the
	 * {@link CommandHandlerFactory}.
	 */
	public static final String COMMAND_ID = "saveModelSearch";

	/**
	 * Called by the {@link TypedConfiguration} for creating a {@link SaveSearchCommand}.
	 * <p>
	 * <b>Don't call directly.</b> Use
	 * {@link InstantiationContext#getInstance(PolymorphicConfiguration)} instead.
	 * </p>
	 * 
	 * @param context
	 *        For error reporting and instantiation of dependent configured objects.
	 * @param config
	 *        The configuration for the new instance.
	 */
	@CalledByReflection
	public SaveSearchCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext displayContext, LayoutComponent untypedComponent,
			Object model, Map<String, Object> arguments) {
		SearchSelectorComponent component = (SearchSelectorComponent) untypedComponent;
		HandlerResult errorResult = checkSearchAndReportFormContexts(component);
		if (errorResult != null) {
			return errorResult;
		}
		SearchAndReportConfig searchExpression = component.getExpression();
		String searchExpressionString = TypedConfiguration.toString(searchExpression);
		SearchExpressionImpl expressionWrapper = component.getSelected();
		Transaction transaction = WrapperUtil.getKnowledgeBase(expressionWrapper).beginTransaction();
		try {
			expressionWrapper.setExpression(searchExpressionString);
			expressionWrapper.setVersion(Search.VERSION);
			publish(expressionWrapper, component.getFormContext());
			transaction.commit();
		} catch (KnowledgeBaseException ex) {
			throw new RuntimeException("Failed to persist search expression. Cause: " + ex.getMessage(), ex);
		} finally {
			transaction.rollback();
		}
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * {@link FormContext#checkAll() Checks} the {@link FormContext}s of the search and report
	 * component.
	 * 
	 * @param selectorComponent
	 *        The {@link SearchSelectorComponent} itself is not checked. Is not allowed to be null.
	 * @return Null, if there is no error.
	 */
	private HandlerResult checkSearchAndReportFormContexts(SearchSelectorComponent selectorComponent) {
		FormComponent searchExpressionEditor = (FormComponent) selectorComponent.getActiveSearchExpressionEditor();
		SearchResultChartConfigComponent reportComponent = selectorComponent.getReportComponent();
		return checkFormContexts(list(selectorComponent, searchExpressionEditor, reportComponent));
	}

	/**
	 * {@link FormContext#checkAll() Checks} the {@link FormContext}s of all the given components.
	 * 
	 * @param components
	 *        Is not allowed to be or contain null.
	 * @return Null, if there is no error.
	 */
	public static HandlerResult checkFormContexts(Collection<? extends FormComponent> components) {
		for (FormComponent component : components) {
			if (!component.getFormContext().checkAll()) {
				return AbstractApplyCommandHandler.createErrorResult(component.getFormContext());
			}
		}
		return null;
	}

}
