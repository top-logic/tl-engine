/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui;

import static com.top_logic.basic.shared.collection.iterator.IteratorUtilShared.*;
import static com.top_logic.layout.form.values.Fields.*;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

import com.top_logic.basic.Log;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.exception.ErrorSeverity;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.component.AbstractApplyCommandHandler;
import com.top_logic.layout.form.component.DeclarativeFormComponent;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.layout.form.values.edit.initializer.InitializerIndex;
import com.top_logic.layout.form.values.edit.initializer.InitializerProvider;
import com.top_logic.model.search.ModelBasedSearch;
import com.top_logic.model.search.ModelBasedSearch.SearchConfig;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.compile.SearchExpressionCompiler;
import com.top_logic.model.search.expr.interpreter.DefResolver;
import com.top_logic.model.search.expr.interpreter.TypeResolver;
import com.top_logic.model.search.persistency.expressions.SearchExpressionImpl;
import com.top_logic.model.search.ui.model.Search;
import com.top_logic.model.search.ui.model.exec.ExpressionBuilder;
import com.top_logic.model.search.ui.model.misc.VariableNameProviderAdder;
import com.top_logic.model.search.ui.selector.SearchAndReportConfig;
import com.top_logic.model.search.ui.selector.SearchType;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.ModelService;

/**
 * GUI based editor to create SearchExpressions.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class GUISearchExpressionEditor extends DeclarativeFormComponent implements SearchExpressionEditor {

	/**
	 * The {@link TypedConfiguration} of the {@link SearchComponent}.
	 */
	public interface Config extends DeclarativeFormComponent.Config {

		/** Property name of {@link #getSearchName()}. */
		String SEARCH_NAME = "searchName";

		/**
		 * The name of the {@link com.top_logic.model.search.ModelBasedSearch.Config#getSearches()
		 * search configuration} to be used.
		 */
		@Mandatory
		@Name(SEARCH_NAME)
		String getSearchName();

		@ClassDefault(Search.class)
		@NonNullable
		@Override
		Class<? extends Search> getFormType();

		@BooleanDefault(true)
		@Override
		boolean getCompactLayout();

	}

	private SearchExpressionImpl _searchExpressionModel;

	/** Initialize {@link SearchComponent} from a collection of XML attributes. */
	public GUISearchExpressionEditor(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
		checkSearchName(context, config);
	}

	private void checkSearchName(Log log, Config config) {
		ModelBasedSearch searchModule = ModelBasedSearch.getInstance();
		SearchConfig searchConfig = searchModule.getSearchConfig(config.getSearchName());
		if (searchConfig == null) {
			Set<String> existingSearchConfigs = searchModule.getConfig().getSearches().keySet();
			String message = "There is no search configuration with the name '" + config.getSearchName()
				+ "'. Existing search configs: " + existingSearchConfigs;
			log.error(message, new ConfigurationException(message));
		}
	}

	@Override
	public Search createFormModel() {
		Search search = (Search) super.createFormModel();
		search.setConfigName(getConfig().getSearchName());
		return search;
	}

	@Override
	public Config getConfig() {
		return (Config) super.getConfig();
	}

	/**
	 * The {@link SearchConfig} currently used in this component.
	 */
	public SearchConfig getSearchConfig() {
		String searchName = getConfig().getSearchName();
		ModelBasedSearch searchModule = ModelBasedSearch.getInstance();
		return searchModule.getSearchConfig(searchName);
	}

	@Override
	public FormContext createFormContext() {
		FormContext formContext = formContext(this);

		ConfigurationItem config = createFormModel();
		InitializerProvider initializerProvider = createInitializerProvider();
		boolean compactLayout = getConfig().getCompactLayout();
		EditorFactory.initEditorGroup(formContext, config, initializerProvider, compactLayout);

		return formContext;
	}

	@Override
	protected InitializerIndex createInitializerProvider() {
		InitializerIndex initializers = new InitializerIndex();
		initializers.add(null, null, VariableNameProviderAdder.INSTANCE);
		return initializers;
	}

	private Search getFormModel(FormContext formContext) {
		return (Search) EditorFactory.getModel(formContext);
	}

	@Override
	public Search getFormModel() {
		return getFormModel(getFormContext());
	}

	/**
	 * Set the {@link Search} expression from which the {@link FormContext} is built.
	 */
	public void setFormModel(Search search) {
		FormContainer queryContainer = getFormContext();
		removeMembers(queryContainer);
		InitializerProvider initializerProvider = createInitializerProvider();
		boolean compactLayout = getConfig().getCompactLayout();
		EditorFactory.initEditorGroup(queryContainer, search, initializerProvider, compactLayout);
	}

	private void removeMembers(FormContainer parent) {
		// Create explicit copy of elements to prevent ConcurrentModificationException.
		List<? extends FormMember> membersCopy = toList(parent.getMembers());
		membersCopy.forEach(parent::removeMember);
	}

	private SearchExpression createSearch() {
		return resolveSearchExpression(createRawSearchExpression());
	}

	private SearchExpression resolveSearchExpression(SearchExpression expression) {
		if (expression == null) {
			throw new TopLogicException(I18NConstants.ERROR_NO_SEARCH_EXPRESSION).initSeverity(ErrorSeverity.INFO);
		}
		expression = getOptimizedVariableResolvedSearchExpression(expression);

		resolveSearchExpressionTypes(expression);

		return expression;
	}

	private SearchExpression getOptimizedVariableResolvedSearchExpression(SearchExpression expression) {
		resolveSearchExpressionVariables(expression);

		return optimizeSearchExpression(expression);
	}

	private void resolveSearchExpressionTypes(SearchExpression expression) {
		expression.visit(new TypeResolver(ModelService.getApplicationModel()), null);
	}

	private SearchExpression optimizeSearchExpression(SearchExpression expression) {
		return getSearchConfig().getEnableOptimizations() ? createOptimizedSearchExpression(expression) : expression;
	}

	private SearchExpression createOptimizedSearchExpression(SearchExpression expression) {
		return new SearchExpressionCompiler(PersistencyLayer.getKnowledgeBase().getMORepository()).compile(expression);
	}

	private void resolveSearchExpressionVariables(SearchExpression expression) {
		expression.visit(new DefResolver(), null);
	}

	private SearchExpression createRawSearchExpression() {
		return new ExpressionBuilder().toExpression(getFormModel());
	}

	@Override
	public HandlerResult search(Function<SearchExpression, HandlerResult> algorithm) {
		FormContext formContext = getFormContext();
		if (!formContext.checkAll()) {
			HandlerResult result = new HandlerResult();
			AbstractApplyCommandHandler.fillHandlerResultWithErrors(formContext, result);
			return result;
		}
		return algorithm.apply(createSearch());
	}

	@Override
	public SearchType getType() {
		return SearchType.NORMAL;
	}

	@Override
	public void setFormModel(SearchExpressionImpl expressionWrapper, SearchAndReportConfig searchConfig) {
		setFormModel(searchConfig.getSearch());

		_searchExpressionModel = expressionWrapper;
	}

	@Override
	public SearchExpressionImpl getLoadedPersistentSearchExpression() {
		return _searchExpressionModel;
	}

	@Override
	public void setLoadedPersistentSearchExpression(SearchExpressionImpl expressionWrapper) {
		_searchExpressionModel = expressionWrapper;
	}

}
