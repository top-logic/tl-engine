/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.analyze.lucene;

import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;

import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.core.log.Log;

/**
 * {@link StopWordAnalyzerFactory} instantiating an {@link Analyzer} class byreflection.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DefaultAnalyzerFactory extends StopWordAnalyzerFactory<DefaultAnalyzerFactory.Config> {

	/** Constructor signature which is suitable for most {@link Analyzer} subclasses */
	private static final Class<?>[] ANALYZER_CONSTRUCTOR_SIGNATURE = new Class<?>[] { Set.class };

	/**
	 * Configuration for {@link DefaultAnalyzerFactory}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends StopWordAnalyzerFactory.StopWordAnalyzerFactoryConfig<DefaultAnalyzerFactory> {

		/**
		 * The Lucene document analyzer used for indexing.
		 */
		Class<? extends Analyzer> getDocumentAnalyzer();

	}

	/**
	 * Creates a new {@link DefaultAnalyzerFactory}.
	 */
	public DefaultAnalyzerFactory(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public Analyzer createAnalzyer(InstantiationContext context) {
		CharArraySet allWords = readStopWords(context);
		try {
			return ConfigUtil.newInstance(getConfig().getDocumentAnalyzer(), ANALYZER_CONSTRUCTOR_SIGNATURE, allWords);
		} catch (ConfigurationException ex) {
			String message = "The configured analyzer does not support stop words: " + ex.getMessage();
			context.info(message, Log.WARN);

			try {
				return ConfigUtil.newInstance(getConfig().getDocumentAnalyzer());
			} catch (ConfigurationException ex1) {
				context.error("Failed to initialize document analyzer.", ex1);
				return null;
			}
		}
	}

}

