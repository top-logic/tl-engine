/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.filter;

import static com.top_logic.model.util.TLModelUtil.*;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.col.filter.configurable.AbstractConfigurableFilter;
import com.top_logic.basic.col.filter.configurable.ConfigurableFilter;
import com.top_logic.basic.col.filter.typed.FilterResult;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.tools.NameBuilder;
import com.top_logic.model.TLType;

/**
 * A {@link ConfigurableFilter} that accepts only global types.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class GlobalTypeFilter extends AbstractConfigurableFilter<TLType, AbstractConfigurableFilter.Config<?>> {

	/**
	 * Called by the {@link TypedConfiguration} for creating a {@link GlobalTypeFilter}.
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
	public GlobalTypeFilter(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public Class<?> getType() {
		return TLType.class;
	}

	@Override
	protected FilterResult matchesTypesafe(TLType type) {
		return FilterResult.valueOf(isGlobal(type));
	}

	@Override
	public String toString() {
		return new NameBuilder(this).build();
	}

}
