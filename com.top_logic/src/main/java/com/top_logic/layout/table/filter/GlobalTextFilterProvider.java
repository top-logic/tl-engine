/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import java.util.Collections;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.table.TableViewModel;
import com.top_logic.layout.table.component.AbstractTableFilterProvider;
import com.top_logic.layout.table.component.TableFilterProvider;

/**
 * {@link TableFilterProvider} for {@link GlobalTextFilter}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class GlobalTextFilterProvider extends AbstractTableFilterProvider {

	/**
	 * Configuration options for {@link GlobalTextFilterProvider}.
	 */
	public interface Config extends AbstractTableFilterProvider.Config {

		@Override
		@ClassDefault(GlobalTextFilterProvider.class)
		Class<? extends AbstractTableFilterProvider> getImplementationClass();

	}

	/**
	 * Default instance of {@link GlobalTextFilterProvider};
	 */
	public static final TableFilterProvider INSTANCE = new GlobalTextFilterProvider();

	/**
	 * Creates a {@link GlobalTextFilterProvider} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public GlobalTextFilterProvider(InstantiationContext context, Config config) {
		super(context, config);
	}

	private GlobalTextFilterProvider() {
		// Singleton constructor
	}

	@Override
	protected List<ConfiguredFilter> createFilterList(TableViewModel tableViewModel, String filterPosition) {
		return Collections.singletonList(createGlobalTextFilter(tableViewModel));
	}

	private ConfiguredFilter createGlobalTextFilter(TableViewModel tableViewModel) {
		return new GlobalTextFilter(tableViewModel, showNonMatchingOptions());
	}
}
