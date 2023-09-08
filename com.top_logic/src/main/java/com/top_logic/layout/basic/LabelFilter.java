/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.filter.DelegatingFilterConfig;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.layout.LabelProvider;

/**
 * A {@link Filter} that filters {@link Object}s based on their labels (provided by a
 * {@link LabelProvider}) and a filter implementation for those labels.
 * 
 * @see LabelMatcher A specialized version matching exactly one label.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class LabelFilter implements Filter<Object> {

	/**
	 * Configuration options for {@link LabelFilter}.
	 */
	@TagName("with-label")
	public interface Config<I extends LabelFilter> extends DelegatingFilterConfig<String, I> {

		/**
		 * The {@link LabelProvider} to create the {@link String} representation for the filtered
		 * objects.
		 */
		PolymorphicConfiguration<? extends LabelProvider> getLabelProvider();

		/**
		 * @see #getLabelProvider()
		 */
		void setLabelProvider(PolymorphicConfiguration<? extends LabelProvider> value);
		
	}
	
	private final LabelProvider labelProvider;
	
	private final Filter<? super String> labelFilterImpl;

	/**
	 * Creates a {@link LabelFilter} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public LabelFilter(InstantiationContext context, Config<?> config) {
		this(context.getInstance(config.getLabelProvider()), context.getInstance(config.getFilter()));
	}

	/**
	 * Creates a new {@link LabelFilter}.
	 * 
	 * @see LabelMatcher if objects should be matched that have exactly a given
	 *      label.
	 */
	public LabelFilter(LabelProvider labelProvider, Filter<? super String> labelFilterImpl) {
		this.labelProvider = labelProvider;
		this.labelFilterImpl = labelFilterImpl;
	}
	
	@Override
	public boolean accept(Object anObject) {
		return labelFilterImpl.accept(labelProvider.getLabel(anObject));
	}

}
