/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.selector;

import static com.top_logic.basic.config.TypedConfiguration.*;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.col.Provider;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.basic.util.Utils;
import com.top_logic.element.layout.meta.expression.ExpressionSelectorComponent;
import com.top_logic.element.layout.meta.search.AttributedSearchResultSet;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.layout.component.TabComponent;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.mig.html.layout.CommandRegistry;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.search.persistency.expressions.SearchExpression;
import com.top_logic.model.search.persistency.expressions.SearchExpressionImpl;
import com.top_logic.model.search.ui.ExpertSearchExpressionEditor;
import com.top_logic.model.search.ui.GUISearchExpressionEditor;
import com.top_logic.model.search.ui.SearchComponent;
import com.top_logic.model.search.ui.SearchExpressionEditor;
import com.top_logic.model.search.ui.model.Search;
import com.top_logic.reporting.flex.chart.config.ChartConfig;
import com.top_logic.reporting.flex.search.SearchResultChartConfigComponent;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.error.ErrorHandlingHelper;

/**
 * A {@link FormComponent} for managing {@link SearchExpressionImpl}s.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class SearchSelectorComponent extends ExpressionSelectorComponent<SearchExpressionImpl> {

	/** {@link ConfigurationItem} for the {@link SearchSelectorComponent}. */
	public interface Config extends ExpressionSelectorComponent.Config {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		/** Property name of {@link #getReportComponent()}. */
		String REPORT_COMPONENT = "reportComponent";

		/** Provides the {@link SearchExpression}s that the user can choose from. */
		@InstanceFormat
		@InstanceDefault(SearchExpressionProvider.class)
		Provider<? extends List<? extends SearchExpressionImpl>> getSearchExpressionProvider();

		@Override
		default void modifyIntrinsicCommands(CommandRegistry registry) {
			ExpressionSelectorComponent.Config.super.modifyIntrinsicCommands(registry);
			registry.registerButton(SaveSearchCommand.COMMAND_ID);
			registry.registerButton(DeleteSearchCommand.COMMAND_ID);
			registry.registerButton(ResetSearchCommand.COMMAND_ID);
		}

		/** @see SearchSelectorComponent#getReportComponent() */
		@Mandatory
		@Name(REPORT_COMPONENT)
		ComponentName getReportComponent();

	}

	private static final String FIELD_NAME_EXPRESSION_SELECTOR = "expressionSelector";

	/**
	 * Called by the {@link TypedConfiguration} for creating a
	 * {@link SearchSelectorComponent}.
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
	public SearchSelectorComponent(InstantiationContext context, Config config)
			throws ConfigurationException {
		super(context, config);
	}

	@Override
	public Config getConfig() {
		return (Config) super.getConfig();
	}

	/**
	 * The {@link SearchResultChartConfigComponent} whose {@link ChartConfig} is saved and loaded.
	 */
	public SearchResultChartConfigComponent getReportComponent() {
		ComponentName componentName = getConfig().getReportComponent();
		return (SearchResultChartConfigComponent) getMainLayout().getComponentByName(componentName);
	}

	@Override
	protected boolean supportsInternalModel(Object object) {
		if (object == null) {
			return true;
		}
		if (!isSupportedExpression(object)) {
			return false;
		}
		return super.supportsInternalModel(object);
	}

	@Override
	protected List<? extends SearchExpressionImpl> getStoredExpressions() {
		return getConfig().getSearchExpressionProvider().get();
	}

	/**
	 * The {@link KnowledgeBase} that this component uses.
	 * 
	 * @return Never null.
	 */
	protected KnowledgeBase getKnowledgeBase() {
		return PersistencyLayer.getKnowledgeBase();
	}

	/**
	 * The combined {@link SearchAndReportConfig} which contains both the search and the report that
	 * is displayed.
	 */
	public SearchAndReportConfig getExpression() {
		SearchAndReportConfig item = TypedConfiguration.newConfigItem(SearchAndReportConfig.class);
		/* Copying the items is necessary, as they might already have a different
		 * ConfigPart#container(). And it is not allowed to have two containers. */
		SearchExpressionEditor searchExpressionEditor = getActiveSearchExpressionEditor();

		setSearchExpression(item, searchExpressionEditor);
		setReport(item);
		setSearchExpressionEditorType(item, searchExpressionEditor);

		return item;
	}

	private void setSearchExpression(SearchAndReportConfig item, SearchExpressionEditor searchExpressionEditor) {
		if (searchExpressionEditor instanceof ExpertSearchExpressionEditor) {
			item.setExpertSearch(
				((ExpertSearchExpressionEditor) searchExpressionEditor).getFormModel().getSearchExpression());
		} else if (searchExpressionEditor instanceof GUISearchExpressionEditor) {
			item.setSearch(copy(((GUISearchExpressionEditor) searchExpressionEditor).getFormModel()));
		} else {
			Logger.error("Unknown search expression editor type", this);
		}
	}

	private void setSearchExpressionEditorType(SearchAndReportConfig item, SearchExpressionEditor editor) {
		item.setSearchType(editor.getType());
	}

	private void setReport(SearchAndReportConfig item) {
		item.setReport(copy(getReport()));
	}

	private ChartConfig getReport() {
		return getReportComponent().createChartConfig();
	}

	@Override
	public void loadExpression(SearchExpressionImpl expressionWrapper) throws ConfigurationException {
		if (expressionWrapper == null) {
			resetActiveSearchExpressionEditor();
			resetResultChannel();
			resetReportComponent();
		} else {
			loadExistentExpression(expressionWrapper);
		}
	}

	private void resetReportComponent() {
		SearchResultChartConfigComponent reportComponent = getReportComponent();

		reportComponent.removeFormContext();
		reportComponent.getFormContext();
		reportComponent.invalidate();
	}

	private void resetResultChannel() {
		getSearchComponent().getResultChannel().set(createEmptyResultSet());
	}

	private AttributedSearchResultSet createEmptyResultSet() {
		return new AttributedSearchResultSet(Collections.emptySet(), Collections.emptySet(), null, null);
	}

	private void resetActiveSearchExpressionEditor() {
		FormComponent searchEditor = (FormComponent) getActiveSearchExpressionEditor();
		searchEditor.removeFormContext();
		searchEditor.getFormContext();

		getActiveSearchExpressionEditor().setLoadedPersistentSearchExpression(null);

		searchEditor.invalidate();
	}

	private void loadExistentExpression(SearchExpressionImpl expressionWrapper) throws ConfigurationException {
		SearchAndReportConfig searchAndReport = getSearchAndReportConfig(expressionWrapper);
		SearchComponent searchComponent = getSearchComponent();
		TabComponent tabbar = searchComponent.getSearchExpressionEditorsTabbar();
		List<LayoutComponent> children = tabbar.getChildList();

		for (LayoutComponent child : children) {
			if (child instanceof SearchExpressionEditor) {
				if (((SearchExpressionEditor) child).getType().equals(searchAndReport.getSearchType())) {
					tabbar.makeVisible(child);
					((SearchExpressionEditor) child).setFormModel(expressionWrapper, searchAndReport);

					HandlerResult result = ((SearchExpressionEditor) child).search(expr -> {
						if (expr != null) {
							searchComponent.search(expr);
						}
						return HandlerResult.DEFAULT_RESULT;
					});
					if (!result.isSuccess()) {
						result.setErrorTitle(I18NConstants.EXECUTING_STORED_QUERY_FAILED);
						ErrorHandlingHelper.transformHandlerResult(child.getWindowScope(), result);
					}

					break;
				}
			}
		}

		ChartConfig report = searchAndReport.getReport();
		getReportComponent().loadConfig(report);
	}

	private SearchAndReportConfig getSearchAndReportConfig(SearchExpressionImpl expressionWrapper)
			throws ConfigurationException {
		String expressionString = expressionWrapper.getExpression();

		return parseExpression(expressionString);
	}

	/**
	 * Current active editor to create search expressions.
	 */
	public SearchExpressionEditor getActiveSearchExpressionEditor() {
		return getSearchComponent().getActiveSearchExpressionEditor();
	}

	private SearchComponent getSearchComponent() {
		return (SearchComponent) getParent();
	}

	private SearchAndReportConfig parseExpression(String expression) throws ConfigurationException {
		return (SearchAndReportConfig) TypedConfiguration.fromString(expression);
	}

	@Override
	public String getExpressionSelectorName() {
		return FIELD_NAME_EXPRESSION_SELECTOR;
	}

	@Override
	protected boolean isSupportedVersion(SearchExpressionImpl model) {
		return Utils.equals(model.getVersion(), Search.VERSION);
	}

	@Override
	protected Class<SearchExpressionImpl> getWrapperClass() {
		return SearchExpressionImpl.class;
	}

}
