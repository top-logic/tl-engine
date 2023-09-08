/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.filter;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.col.filter.configurable.AbstractConfigurableFilter;
import com.top_logic.basic.col.filter.configurable.ConfigurableFilter;
import com.top_logic.basic.col.filter.typed.FilterResult;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.tools.NameBuilder;
import com.top_logic.model.ModelKind;
import com.top_logic.model.TLClass;

/**
 * An {@link ConfigurableFilter} that accepts abstract types that have none or only a single
 * direct subtype.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class UselessTypeFilter
		extends AbstractConfigurableFilter<TLClass, AbstractConfigurableFilter.Config<UselessTypeFilter>> {

	/**
	 * Called by the {@link TypedConfiguration} for creating a {@link UselessTypeFilter}.
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
	public UselessTypeFilter(InstantiationContext context, Config<UselessTypeFilter> config) {
		super(context, config);
	}

	@Override
	public Class<?> getType() {
		return TLClass.class;
	}

	@Override
	public FilterResult matchesTypesafe(TLClass tlClass) {
		if (tlClass.getModelKind() == ModelKind.ASSOCIATION) {
			/* PersistentAssociation implements TLClass, too, but throws exceptions. */
			return FilterResult.INAPPLICABLE;
		}
		if (!tlClass.isAbstract()) {
			return FilterResult.FALSE;
		}
		return FilterResult.valueOf(tlClass.getSpecializations().size() <= 1);
	}

	@Override
	public String toString() {
		return new NameBuilder(this).build();
	}

}
