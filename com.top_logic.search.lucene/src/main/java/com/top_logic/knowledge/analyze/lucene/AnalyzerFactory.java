/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.analyze.lucene;

import org.apache.lucene.analysis.Analyzer;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.knowledge.analyze.lucene.AnalyzerFactory.AnalyzerFactoryConfig;

/**
 * Factory creating {@link Analyzer}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AnalyzerFactory<C extends AnalyzerFactoryConfig<?>> extends AbstractConfiguredInstance<C> {

	/**
	 * Configuration for an {@link AnalyzerFactory}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface AnalyzerFactoryConfig<I extends AnalyzerFactory<?>> extends PolymorphicConfiguration<I> {
		// No configuration here
	}

	/**
	 * Creates a new {@link AnalyzerFactory}.
	 */
	public AnalyzerFactory(InstantiationContext context, C config) {
		super(context, config);
	}

	/**
	 * Creates the {@link Analyzer} to use as document analyzer.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configuration and report messages.
	 * @return The {@link Analyzer} to use, or <code>null</code> in case that errors occurred. In
	 *         that case the given context is informed about the failures.
	 */
	public abstract Analyzer createAnalzyer(InstantiationContext context);

}

