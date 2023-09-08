/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta.search.quick;

import java.util.List;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.Mappings;
import com.top_logic.basic.col.filter.FilterFactory;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.element.layout.meta.search.AttributedSearchComponent;
import com.top_logic.element.layout.meta.search.quick.AbstractSearchCommand.SearchConfig;
import com.top_logic.element.layout.meta.search.quick.QuickSearchCommand.QuickSearchConfig;
import com.top_logic.layout.InvisibleView;
import com.top_logic.layout.component.configuration.AbstractViewConfiguration;
import com.top_logic.mig.html.layout.ComponentCollector;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.ComponentNameFormat;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutUtils;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.mig.html.layout.WithGotoConfiguration;
import com.top_logic.util.Resources;

/**
 * Search input control with a full text search engine in its background.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class QuickSearchViewConfiguration extends AbstractViewConfiguration<QuickSearchViewConfiguration.Config> {

	/**
	 * Configuration options for {@link QuickSearchViewConfiguration}.
	 */
	public interface Config
			extends AbstractViewConfiguration.Config<QuickSearchViewConfiguration>, WithGotoConfiguration,
			QuickSearchCommand.Config {

		/**
		 * Configuration name of the value {@link #hasNoSearchComponent()}.
		 */
		String NO_SEARCH_COMPONENT = "no-search-component";

		/**
		 * @see #isActive()
		 */
		String ACTIVE = "active";

		/**
		 * @see #getMaxResults()
		 */
		String MAX_RESULTS = "maxResults";

		/**
		 * @see #getSearchComponent()
		 */
		String SEARCH_COMPONENT = "searchComponent";

		/** Flag, if the quick search input field should be activated (and therefore be visible). */
		@Name(ACTIVE)
		@BooleanDefault(true)
		boolean isActive();

		/**
		 * The maximum number of results shown in the drop-down dialog.
		 */
		@Name(MAX_RESULTS)
		@IntDefault(20)
		int getMaxResults();

		/**
		 * The name of the search component to go to when too many results are found.
		 */
		@Name(SEARCH_COMPONENT)
		ComponentName getSearchComponent();

		/**
		 * Whether no search component should be used when too many results are found.
		 */
		@Name(NO_SEARCH_COMPONENT)
		boolean hasNoSearchComponent();

	}

	/** Unique ID for the quick search input field. */
	public static final String QUICK_SEARCH_FIELD = "quickSearch";

	/**
	 * Creates a {@link QuickSearchViewConfiguration} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public QuickSearchViewConfiguration(InstantiationContext context, QuickSearchViewConfiguration.Config config)
			throws ConfigurationException {
		super(context, config);
	}

	@Override
	public HTMLFragment createView(LayoutComponent component) {
		if (getConfig().isActive()) {
			return createSearchView(component);
		} else {
			return InvisibleView.INSTANCE;
		}
	}

	/**
	 * Actual implementation of {@link #createView(LayoutComponent)} if quicksearch configuration is
	 * active.
	 * 
	 * @see #createView(LayoutComponent)
	 */
	protected HTMLFragment createSearchView(LayoutComponent component) {
		QuickSearchConfig quickSearchConfig = new QuickSearchConfig();
		MainLayout mainLayout = component.getMainLayout();
		AttributedSearchComponent searchComponent;
		try {
			searchComponent = getSearchComponent(mainLayout);
		} catch (ConfigurationException ex) {
			throw new ConfigurationError(ex);
		}
		quickSearchConfig.setSearchComponent(searchComponent);
		quickSearchConfig.setMaxResults(getConfig().getMaxResults());
		setConfiguredTypes(quickSearchConfig);
		quickSearchConfig.getGotoTargets().putAll(LayoutUtils
			.resolveGotoTargets(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, mainLayout,
				getConfig().getGotoTargets().values()));

		return createSearchView(getConfig().getName(), quickSearchConfig);
	}

	private void setConfiguredTypes(SearchConfig searchConfig) {
		List<String> excludeTypes = getConfig().getExcludeTypes();
		List<String> includeTypes = getConfig().getIncludeTypes();
		if (includeTypes.isEmpty() && excludeTypes.isEmpty()) {
			QuickSearchCommand.Config config = QuickSearchCommand.quickSearchConfig();
			includeTypes = config.getIncludeTypes();
			excludeTypes = config.getExcludeTypes();
		}
		searchConfig.getExcludeTypes().addAll(SearchConfig.resolveTypes(excludeTypes));
		searchConfig.getIncludeTypes().addAll(SearchConfig.resolveTypes(includeTypes));
	}

	/**
	 * Creates the {@link HTMLFragment} to display as search input.
	 * 
	 * @param fieldName
	 *        Name of the field to display.
	 * @param config
	 *        Configuration for the search command.
	 */
	protected HTMLFragment createSearchView(String fieldName, QuickSearchConfig config) {
		return new QuickSearchView(fieldName, config);
	}

	private AttributedSearchComponent getSearchComponent(MainLayout mainLayout) throws ConfigurationException {
		if (getConfig().hasNoSearchComponent()) {
			return null;
		}
		ComponentName searchComponentName = getConfig().getSearchComponent();
		if (searchComponentName == null) {
			return getUniqueSearchComponent(mainLayout);
		}
		return findSearchComponent(mainLayout, searchComponentName);
	}

	private AttributedSearchComponent findSearchComponent(MainLayout mainLayout, ComponentName searchComponentName) {
		LayoutComponent searchComponent = mainLayout.getComponentByName(searchComponentName);
		if (searchComponent == null) {
			String nameSpec = ComponentNameFormat.INSTANCE.getSpecification(searchComponentName);
			Logger.error(Resources.getSystemInstance()
				.getString(I18NConstants.NO_SUCH_SEARCH_COMPONENT.fill(nameSpec)), QuickSearchViewConfiguration.class);
		}
		if (!(searchComponent instanceof AttributedSearchComponent)) {
			String nameSpec = ComponentNameFormat.INSTANCE.getSpecification(searchComponentName);
			Logger.error(Resources.getSystemInstance().getString(I18NConstants.INVALID_SEARCH_COMPONENT.fill(nameSpec)),
				QuickSearchViewConfiguration.class);
		}
		return (AttributedSearchComponent) searchComponent;
	}

	private AttributedSearchComponent getUniqueSearchComponent(MainLayout mainLayout) throws ConfigurationException {
		ComponentCollector collector =
			new ComponentCollector(FilterFactory.createClassFilter(AttributedSearchComponent.class));
		mainLayout.acceptVisitorRecursively(collector);
		List<LayoutComponent> searchComponents = collector.getFoundElements();
		switch (searchComponents.size()) {
			case 0: {
				return null;
			}
			case 1: {
				return (AttributedSearchComponent) searchComponents.get(0);
			}
			default: {
				List<String> componentNames = Mappings.map(new Mapping<LayoutComponent, String>() {
				
					@Override
					public String map(LayoutComponent input) {
						return input.getName().qualifiedName();
					}
				}, searchComponents);
				throw new ConfigurationException(I18NConstants.MULTIPLE_SEARCH_COMPONENTS.fill(componentNames),
					Config.SEARCH_COMPONENT, StringServices.EMPTY_STRING);
			}
		}
	}
}
