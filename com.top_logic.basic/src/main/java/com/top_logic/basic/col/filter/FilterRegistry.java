/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col.filter;

import java.util.Map;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;

/**
 * A registry for the named filters in the application.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class FilterRegistry {

	/**
	 * Configuration interface defining the filters in the application.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends ConfigurationItem {

		/**
		 * The filter in the application indexed by their name.
		 */
		@Key(NamedFilter.NAME_ATTRIBUTE)
		Map<String, NamedFilter> getFilters();

	}

	/**
	 * The configuration of a {@link Filter} with name.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface NamedFilter extends NamedConfigMandatory {

		/**
		 * The {@link Filter} of this configuration.
		 */
		@InstanceFormat
		@Mandatory
		Filter<Object> getFilter();

	}
	
	/**
	 * Returns the filter which is known under the given name in the application. If there is no
	 * filter, <code>null</code> is returned.
	 * 
	 * @param filterName
	 *        The name of the configured filter.
	 * 
	 * @return The filter for the given name, or <code>null</code> if there is no filter.
	 */
	public static Filter<Object> getFilter(String filterName){
		ApplicationConfig applicationConfig = ApplicationConfig.getInstance();
		Map<String, NamedFilter> allFilters = applicationConfig.getConfig(Config.class).getFilters();
		NamedFilter filterConfig = allFilters.get(filterName);
		if (filterConfig == null) {
			return null;
		}
		return filterConfig.getFilter();
	}

	/**
	 * Returns the name under which the given filter is known in the application. If there is no
	 * mapping, <code>null</code> is returned.
	 * 
	 * @param filter
	 *        Some filter.
	 * 
	 * @return The name of the given filter, or <code>null</code> if there is no such name.
	 */
	public static String getFilterName(Filter<?> filter) {
		return ApplicationConfig.getInstance().getConfig(Config.class).getFilters().values()
			.stream()
			.filter(namedFilter -> filter == namedFilter.getFilter())
			.map(NamedFilter::getName)
			.findFirst()
			.orElse(null);
	}
	
}
