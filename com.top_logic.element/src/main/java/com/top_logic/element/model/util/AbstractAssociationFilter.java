/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.util;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.col.filter.configurable.AbstractConfigurableFilter;
import com.top_logic.basic.col.filter.configurable.ConfigurableFilter;
import com.top_logic.basic.col.filter.typed.FilterResult;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.tools.NameBuilder;
import com.top_logic.element.meta.kbbased.PersistentAssociation;
import com.top_logic.model.TLAssociation;

/**
 * An {@link ConfigurableFilter} that accepts abstract {@link TLAssociation}s.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class AbstractAssociationFilter extends
		AbstractConfigurableFilter<PersistentAssociation, AbstractConfigurableFilter.Config<AbstractAssociationFilter>> {

	/**
	 * Called by the {@link TypedConfiguration} for creating a {@link AbstractAssociationFilter}.
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
	public AbstractAssociationFilter(InstantiationContext context, Config<AbstractAssociationFilter> config) {
		super(context, config);
	}

	@Override
	public Class<?> getType() {
		return PersistentAssociation.class;
	}

	@Override
	public FilterResult matchesTypesafe(PersistentAssociation association) {
		return FilterResult.valueOf(association.isAbstract());
	}

	@Override
	public String toString() {
		return new NameBuilder(this).build();
	}

}
