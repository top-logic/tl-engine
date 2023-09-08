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
import com.top_logic.basic.col.filter.typed.TypedFilter;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.tools.NameBuilder;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.annotate.AnnotationLookup;
import com.top_logic.model.annotate.DisplayAnnotations;

/**
 * A {@link TypedFilter} that accepts {@link DisplayAnnotations#isHidden(AnnotationLookup) hidden}
 * {@link TLModelPart}s.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class HiddenModelPartFilter
		extends AbstractConfigurableFilter<TLModelPart, ConfigurableFilter.Config<HiddenModelPartFilter>> {

	/**
	 * Called by the {@link TypedConfiguration} for creating a {@link HiddenModelPartFilter}.
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
	public HiddenModelPartFilter(InstantiationContext context, Config<HiddenModelPartFilter> config) {
		super(context, config);
	}

	@Override
	public Class<?> getType() {
		return TLModelPart.class;
	}

	@Override
	public FilterResult matchesTypesafe(TLModelPart typePart) {
		boolean booleanResult = DisplayAnnotations.isHidden(typePart);
		return FilterResult.valueOf(booleanResult);
	}

	@Override
	public String toString() {
		return new NameBuilder(this).build();
	}

}
