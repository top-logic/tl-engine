/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.element.layout.meta.search.AttributedSearchResultSet;
import com.top_logic.element.meta.TypeSpec;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.layout.channel.ChannelSPI;
import com.top_logic.layout.channel.ComponentChannel;
import com.top_logic.layout.channel.TypedChannelSPI;
import com.top_logic.layout.component.TabComponent;
import com.top_logic.layout.component.TabComponent.TabbedLayoutComponent;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.Layout;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassProperty;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.impl.TransientModelFactory;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.search.ui.selector.SearchSelectorComponent;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

/**
 * {@link LayoutComponent} for {@link TLModel}-based search.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class SearchComponent extends Layout {

	private static final String RESULT_CHANNEL_NAME = "result";

	private static final Map<String, ChannelSPI> MODEL_AND_RESULT_CHANNEL = channels(MODEL_CHANNEL,
		new TypedChannelSPI<>(RESULT_CHANNEL_NAME, AttributedSearchResultSet.class, null));

	/**
	 * Configuration for the {@link SearchComponent}.
	 */
	public interface Config extends Layout.Config {

		/**
		 * Property name of {@link #getSearchExpressionSelectorName()}.
		 */
		public static final String SEARCH_EXPRESSION_SELECTOR_NAME = "searchExpressionSelectorName";

		/**
		 * Property name of {@link #getTabbarComponentName()}.
		 */
		public static final String SEARCH_EXPRESSION_EDITORS_TABBAR_NAME = "searchExpressionEditorsTabbarName";

		/**
		 * Name of the GUI search component.
		 */
		@Name(SEARCH_EXPRESSION_EDITORS_TABBAR_NAME)
		ComponentName getTabbarComponentName();

		/**
		 * Name of the SearchSelector component.
		 */
		@Name(SEARCH_EXPRESSION_SELECTOR_NAME)
		ComponentName getSearchExpressionSelectorName();

	}

	/**
	 * Creates an {@link SearchComponent} instance.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param atts
	 *        {@link SearchComponent} configuration.
	 */
	public SearchComponent(InstantiationContext context, Config atts) throws ConfigurationException {
		super(context, atts);
	}

	@Override
	protected Map<String, ChannelSPI> channels() {
		return MODEL_AND_RESULT_CHANNEL;
	}

	/**
	 * Channel containing the results of a model based search.
	 */
	public ComponentChannel getResultChannel() {
		return getChannel(RESULT_CHANNEL_NAME);
	}

	/**
	 * Executes the given model based search model part and propagates the result on the
	 * corresponding channel.
	 * 
	 * @param expression
	 *        Search model.
	 */
	public void search(SearchExpression expression) {
		Collection<?> results = getResults(expression);

		Set<TLClass> searchedTypes = SearchUtil.getSearchedTypes(expression);
		if (searchedTypes.isEmpty() && !results.isEmpty()) {
			// Cannot determine static type of query, use typing by example.
			searchedTypes = new HashSet<>();
			TLClass resultType = null;
			TLClassProperty resultPart = null;
			Collection<Object> resultObjects = new ArrayList<>(results.size());
			for (Object result : results) {
				if (result instanceof TLObject) {
					TLStructuredType type = ((TLObject) result).tType();
					if (type instanceof TLClass) {
						searchedTypes.add((TLClass) type);
					}
					resultObjects.add(result);
				} else {
					if (resultType == null) {
						resultType = TransientModelFactory.createTransientClass(ModelService.getApplicationModel(),
							"SearchResult");
						resultPart = TransientModelFactory.addClassProperty(resultType, "result",
							TLModelUtil.findType(TypeSpec.OBJECT_TYPE));
						searchedTypes.add(resultType);
					}

					TLObject obj = TransientModelFactory.createTransientObject(resultType);
					obj.tUpdate(resultPart, result);
					resultObjects.add(obj);
				}
			}
			results = resultObjects;
		}
		if (!searchedTypes.isEmpty()) {
			Object resultSet =
				new AttributedSearchResultSet((Collection<TLObject>) results, (Set<? extends TLClass>) searchedTypes,
					null,
					null);
			getResultChannel().set(resultSet);
		}
	}

	private Collection<?> getResults(SearchExpression expression) {
		KnowledgeBase defaultKnowledgeBase = PersistencyLayer.getKnowledgeBase();
		TLModel defaultTLModel = ModelService.getApplicationModel();
		Object result = QueryExecutor.compile(defaultKnowledgeBase, defaultTLModel, expression).execute();

		if (result instanceof Collection) {
			return (Collection<?>) result;
		} else {
			return Collections.singleton(result);
		}
	}

	/**
	 * Current active {@link SearchExpressionEditor} to create a model based search
	 *         expression.
	 */
	public SearchExpressionEditor getActiveSearchExpressionEditor() {
		TabComponent tabbar = getSearchExpressionEditorsTabbar();
		int selectedIndex = tabbar.getSelectedIndex();
		TabbedLayoutComponent card = tabbar.getCard(selectedIndex);
		return (SearchExpressionEditor) card.getContent();
	}

	/**
	 * {@link TabComponent} containing all available {@link SearchExpressionEditor}s to
	 *         create a model based search expression.
	 */
	public TabComponent getSearchExpressionEditorsTabbar() {
		Config config = (Config) getConfig();
		ComponentName tabbarComponentName = config.getTabbarComponentName();
		return (TabComponent) getComponentByName(tabbarComponentName);
	}

	/**
	 * {@link LayoutComponent} for selecting a stored model based search expression.
	 */
	public SearchSelectorComponent getSearchExpressionSelector() {
		Config config = (Config) getConfig();
		ComponentName searchExpressionSelectorComponentName = config.getSearchExpressionSelectorName();
		return (SearchSelectorComponent) getComponentByName(searchExpressionSelectorComponentName);
	}

}
