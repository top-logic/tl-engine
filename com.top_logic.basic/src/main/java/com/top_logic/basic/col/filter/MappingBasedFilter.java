/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col.filter;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;

/**
 * This {@link Filter} uses a {@link Mapping} to retrieve the value before check of fitting
 * criteria.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class MappingBasedFilter<S, D> implements Filter<S> {

	/**
	 * Configuration options for {@link MappingBasedFilter}.
	 */
	public interface Config<S, D, I extends MappingBasedFilter<?, ?>> extends DelegatingFilterConfig<D, I> {
		/**
		 * The {@link Mapping} to apply before filtering using {@link #getFilter()}.
		 */
		PolymorphicConfiguration<? extends Mapping<? super S, ? extends D>> getMapping();
	}

	private Mapping<? super S, ? extends D> mapping;
	private Filter<? super D> innerFilter;

	/**
	 * Creates a {@link MappingBasedFilter} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public MappingBasedFilter(InstantiationContext context, Config<S, D, ?> config) {
		this(context.getInstance(config.getMapping()), context.getInstance(config.getFilter()));
	}

	/**
	 * @param mapping
	 *        - provider for the value to be checked by the evaluation filter, must not be null
	 * @param evaluationFilter
	 *        - filter, which decides, whether an given element is acceptable, or not, must not be null
	 */
	public MappingBasedFilter(Mapping<? super S, ? extends D> mapping, Filter<? super D> evaluationFilter) {
		assert mapping != null : "Mapping must not be null!";
		assert evaluationFilter != null : "Filter for criteria evaluation must not be null!";
		
		this.mapping = mapping;
		this.innerFilter = evaluationFilter;
	}

	@Override
	public boolean accept(S anObject) {
		return innerFilter.accept(mapping.map(anObject));
	}

}
