/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.component.TabComponent;
import com.top_logic.layout.component.TabComponent.TabbedLayoutComponent;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLModel;
import com.top_logic.model.search.ui.selector.SearchSelectorComponent;

/**
 * {@link LayoutComponent} for {@link TLModel}-based search.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class SearchComponent extends ScriptComponent {

	/**
	 * Configuration for the {@link SearchComponent}.
	 */
	public interface Config extends ScriptComponent.Config {

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
