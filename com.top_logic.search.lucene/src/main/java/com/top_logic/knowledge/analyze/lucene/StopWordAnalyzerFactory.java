/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.analyze.lucene;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.StopwordAnalyzerBase;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.core.log.Log;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.binary.BinaryData;

/**
 * {@link AnalyzerFactory} to create {@link StopwordAnalyzerBase}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class StopWordAnalyzerFactory<C extends StopWordAnalyzerFactory.StopWordAnalyzerFactoryConfig<?>>
		extends AnalyzerFactory<C> {

	/**
	 * Configuration for {@link StopWordAnalyzerFactory}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface StopWordAnalyzerFactoryConfig<I extends StopWordAnalyzerFactory<?>>
			extends AnalyzerFactoryConfig<I> {

		/**
		 * The files containing the stop-words to be excluded from indexing.
		 */
		@Key(StopWordConfig.STOP_WORD_LANGUAGE)
		Map<String, StopWordConfig> getStopWordConfigs();

	}

	/**
	 * Creates a new {@link StopWordAnalyzerFactory}.
	 */
	public StopWordAnalyzerFactory(InstantiationContext context, C config) {
		super(context, config);
	}

	/**
	 * Creates a {@link CharArraySet} containing all stop words.
	 * 
	 * @param log
	 *        Log to report errors to.
	 */
	protected CharArraySet readStopWords(Log log) {
		Set<String> allWords = new HashSet<>();
		for (StopWordConfig stopWordConfig : getConfig().getStopWordConfigs().values()) {
			String stopwordFile = stopWordConfig.getFile();
			BinaryData currentFile = FileManager.getInstance().getData(stopwordFile);
			try {
				allWords.addAll(FileUtilities.readWordsFromFile(currentFile));
			} catch (IOException ex) {
				log.error("Unable to read stop words from: " + currentFile, ex);
			}
		}
		return toCharArraySet(allWords);
	}

	/**
	 * Service method to create {@link CharArraySet} for a given string set.
	 */
	protected CharArraySet toCharArraySet(Collection<String> s) {
		return new CharArraySet(s, false);
	}

}

