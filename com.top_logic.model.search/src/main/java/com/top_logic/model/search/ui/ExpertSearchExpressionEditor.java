/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui;

import java.util.function.Function;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.NumberOfRows;
import com.top_logic.layout.codeedit.control.CodeEditorControl;
import com.top_logic.layout.form.component.AbstractApplyCommandHandler;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.template.util.SpanGroup;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.layout.form.values.edit.annotation.ControlProvider;
import com.top_logic.layout.form.values.edit.annotation.CssClass;
import com.top_logic.layout.form.values.edit.annotation.UseTemplate;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.search.persistency.expressions.SearchExpressionImpl;
import com.top_logic.model.search.ui.selector.SearchAndReportConfig;
import com.top_logic.model.search.ui.selector.SearchType;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Code editor to create TL-Script based {@link SearchExpression}s.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven F�rster</a>
 */
public class ExpertSearchExpressionEditor extends FormComponent implements SearchExpressionEditor {

	/**
	 * GUI model which is displayed in the {@link ExpertSearchExpressionEditor}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	@UseTemplate(SpanGroup.class)
	public interface SearchConfig extends ConfigurationItem {

		/**
		 * The search expression to execute.
		 */
		@Mandatory
		@ControlProvider(TLScriptCodeEditorControl.CP.class)
		@CssClass(CodeEditorControl.FULL_SIZE_CSS_CLASS)
		@NumberOfRows(min = -1, max = -1)
		Expr getSearchExpression();

		/**
		 * Setter for {@link #getSearchExpression()}.
		 */
		void setSearchExpression(Expr value);
	}

	private SearchExpressionImpl _loadedPersistentSearchExpression;

	/**
	 * Creates an {@link ExpertSearchExpressionEditor} instance.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        {@link ExpertSearchExpressionEditor} configuration.
	 */
	public ExpertSearchExpressionEditor(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

	@Override
	public HandlerResult search(Function<SearchExpression, HandlerResult> algorithm) {
		FormContext formContext = getFormContext();
		if (!formContext.checkAll()) {
			HandlerResult result = new HandlerResult();
			AbstractApplyCommandHandler.fillHandlerResultWithErrors(formContext, result);
			return result;
		}
		return algorithm.apply(QueryExecutor.compileExprOptional(getFormModel().getSearchExpression()));
	}

	/**
	 * The currently displayed {@link SearchConfig}.
	 */
	public SearchConfig getFormModel() {
		return (SearchConfig) EditorFactory.getModel(getFormContext());
	}

	@Override
	public SearchType getType() {
		return SearchType.EXPERT;
	}

	@Override
	public void setFormModel(SearchExpressionImpl expressionWrapper, SearchAndReportConfig searchConfig) {
		setSearchExpressionDataField(searchConfig.getExpertSearch());

		_loadedPersistentSearchExpression = expressionWrapper;
	}

	private void setSearchExpressionDataField(Expr tlScriptSearchExpression) {
		getFormModel().setSearchExpression(tlScriptSearchExpression);
	}

	@Override
	public SearchExpressionImpl getLoadedPersistentSearchExpression() {
		return _loadedPersistentSearchExpression;
	}

	@Override
	public void setLoadedPersistentSearchExpression(SearchExpressionImpl persistentSearchExpression) {
		_loadedPersistentSearchExpression = persistentSearchExpression;
	}

}
