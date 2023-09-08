/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.analyze.lucene;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.StopwordAnalyzerBase;
import org.apache.lucene.analysis.de.GermanAnalyzer;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.binary.BinaryData;

/**
 * {@link StopWordAnalyzerFactory} creating a {@link GermanAnalyzer}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class GermanAnalyzerFactory extends StopWordAnalyzerFactory<GermanAnalyzerFactory.Config> {

	/**
	 * Configuration for a {@link GermanAnalyzerFactory}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends StopWordAnalyzerFactory.StopWordAnalyzerFactoryConfig<GermanAnalyzerFactory> {

		/**
		 * The file containing the words that should be excluded from the German stemmer.
		 */
		String getGermanStemmerExclusion();

	}

	/**
	 * Creates a new {@link GermanAnalyzerFactory}.
	 */
	public GermanAnalyzerFactory(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public StopwordAnalyzerBase createAnalzyer(InstantiationContext context) {
		// Set the stem exclusion table for the German Analyzer
		BinaryData currentFile = FileManager.getInstance().getDataOrNull(getConfig().getGermanStemmerExclusion());
		CharArraySet stemExclusionSet;
		if (currentFile != null) {
			stemExclusionSet = loadStemExclusion(context, currentFile);
		} else {
			stemExclusionSet = CharArraySet.EMPTY_SET;
		}
		return new GermanAnalyzer(readStopWords(context), stemExclusionSet);

	}

	private CharArraySet loadStemExclusion(InstantiationContext context, BinaryData currentFile) {
		CharArraySet stemExclusionSet;
		try {
			List<String> stemArr = FileUtilities.readWordsFromFile(currentFile);
			stemExclusionSet = new CharArraySet(stemArr, false);
		} catch (IOException ex) {
			context.error("stem table file with name: " + getConfig().getGermanStemmerExclusion() + " not readable",
				ex);
			stemExclusionSet = CharArraySet.EMPTY_SET;
		}
		return stemExclusionSet;
	}

}
