/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.provider;

import java.util.Collections;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.table.TableViewModel;
import com.top_logic.layout.table.component.AbstractTableFilterProvider;
import com.top_logic.layout.table.filter.AllOperatorsProvider;
import com.top_logic.layout.table.filter.ComparableFilter;
import com.top_logic.layout.table.filter.ComparableFilterConfiguration;
import com.top_logic.layout.table.filter.ConfiguredFilter;

/**
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class WebFolderAttributeFilterProvider extends AbstractTableFilterProvider {

	/**
	 * Singleton {@link WebFolderAttributeFilterProvider} instance.
	 */
	public static final WebFolderAttributeFilterProvider INSTANCE = new WebFolderAttributeFilterProvider();

	private WebFolderAttributeFilterProvider() {
		// Singleton constructor.
	}
	
	/**
	 * Creates a {@link WebFolderAttributeFilterProvider} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public WebFolderAttributeFilterProvider(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected List<ConfiguredFilter> createFilterList(TableViewModel model, String filterPosition) {
		ConfiguredFilter theFilter =
			new ComparableFilter(
				new ComparableFilterConfiguration(model, filterPosition, AllOperatorsProvider.INSTANCE,
					new WebFolderAttributeFilterComparator(), false, showSeparateOptionEntries()),
				getSeparateOptionDisplayThreshold(),
				showNonMatchingOptions());

		return Collections.singletonList(theFilter);
	}
}