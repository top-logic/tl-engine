/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.filter.EqualsFilter;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.layout.LabelProvider;

/**
 * A {@link Filter} that filters {@link Object}s based on their labels (provided by a
 * {@link LabelProvider}).
 * 
 * @see LabelFilter A more general solution.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class LabelMatcher implements Filter<Object> {

	/**
	 * Configuration options for {@link LabelMatcher}.
	 */
	public interface Config<I extends LabelMatcher> extends PolymorphicConfiguration<I> {

		/**
		 * The {@link LabelProvider} to create the label to match for the filtered objects..
		 */
		PolymorphicConfiguration<? extends LabelProvider> getLabelProvider();

		/**
		 * @see #getLabelProvider()
		 */
		void setLabelProvider(PolymorphicConfiguration<? extends LabelProvider> value);

		/**
		 * The matched label.
		 */
		String getExpectedName();

		/**
		 * @see #getExpectedName()
		 */
		void setExpectedName(String value);
		
	}

	private final LabelProvider labelProvider;
	
	private final String expectedLabel;

	/**
	 * Creates a {@link LabelMatcher} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public LabelMatcher(InstantiationContext context, Config<?> config) {
		this.labelProvider = context.getInstance(config.getLabelProvider());
		this.expectedLabel = config.getExpectedName();
	}
	
	/**
	 * Creates a new {@link LabelMatcher} that matches only objects that have
	 * the given label using the given {@link LabelProvider}.
	 * 
	 * <p>
	 * Does exactly the same as an {@link LabelFilter} with an embedded
	 * {@link EqualsFilter}.
	 * </p>
	 */
	public LabelMatcher(LabelProvider labelProvider, String expectedLabel) {
		assert expectedLabel != null : "Expected label must not be null.";
		this.labelProvider = labelProvider;
		this.expectedLabel = expectedLabel;
	}
	
	@Override
	public boolean accept(Object anObject) {
		return expectedLabel.equals(labelProvider.getLabel(anObject));
	}

}
