/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col.filter.configurable;

import com.top_logic.basic.col.filter.AbstractCompositeFilter;
import com.top_logic.basic.col.filter.configurable.ConfigurableFilter.Config;
import com.top_logic.basic.col.filter.typed.AbstractTypedFilter;
import com.top_logic.basic.config.InstantiationContext;

/**
 * The default base class for {@link ConfigurableFilter}s.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public abstract class AbstractConfigurableFilter<T, C extends Config<?>> extends AbstractTypedFilter<T>
		implements ConfigurableFilter<C> {

	private final C _config;

	/**
	 * Creates an {@link AbstractCompositeFilter}.
	 * 
	 * @param context
	 *        For error reporting and instantiation of dependent configured objects.
	 * @param config
	 *        The configuration for the new instance.
	 */
	public AbstractConfigurableFilter(InstantiationContext context, C config) {
		_config = config;
	}

	@Override
	public C getConfig() {
		return _config;
	}

	@Override
	public String toString() {
		return _config.toString();
	}

}
