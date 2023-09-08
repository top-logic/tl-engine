/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.search.chart;

import java.util.Collection;
import java.util.Collections;

import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.element.layout.meta.search.AttributedSearchResultSet;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.reporting.flex.chart.config.datasource.ChartDataSource;
import com.top_logic.reporting.flex.chart.config.datasource.ComponentDataContext;
import com.top_logic.reporting.flex.chart.config.gui.ChartContextObserver;
import com.top_logic.reporting.flex.chart.config.gui.InteractiveBuilder;

/**
 * {@link ChartDataSource} providing the objects from the current search result.
 * 
 * @author <a href=mailto:cca@top-logic.com>cca</a>
 */
public class SearchResultDataSource implements ChartDataSource<ComponentDataContext>,
		InteractiveBuilder<SearchResultDataSource, ChartContextObserver>,
		ConfiguredInstance<SearchResultDataSource.Config> {

	/**
	 * Config-interface for {@link SearchResultDataSource}.
	 */
	public interface Config extends PolymorphicConfiguration<SearchResultDataSource> {

		/**
		 * the name of the search-component with the search-result that should provide the
		 *         objects
		 */
		ComponentName getSearchComponent();

	}

	private final Config _config;

	/**
	 * Config-Constructor for {@link SearchResultDataSource}.
	 * 
	 * @param aContext
	 *        - default config-constructor
	 * @param aConfig
	 *        - default config-constructor
	 */
	public SearchResultDataSource(InstantiationContext aContext, Config aConfig) {
		_config = aConfig;
	}

	@Override
	public Collection<?> getRawData(ComponentDataContext context) {
		MainLayout mainLayout = context.getComponent().getMainLayout();
		LayoutComponent searchComponent = mainLayout.getComponentByName(getConfig().getSearchComponent());
		if (searchComponent == null) {
			searchComponent = context.getComponent().getMaster();
		}
		if (searchComponent == null) {
			return Collections.emptyList();
		}
		Object model = searchComponent.getModel();
		if (model instanceof AttributedSearchResultSet) {
			AttributedSearchResultSet resultSet = (AttributedSearchResultSet) model;
			return resultSet.getResultObjects();
		}
		return Collections.emptyList();

	}

	@Override
	public Config getConfig() {
		return _config;
	}

	@Override
	public void createUI(FormContainer container, ChartContextObserver arg) {
		// no gui-elements necessary
	}

	@Override
	public SearchResultDataSource build(FormContainer container) {
		return this;
	}

}