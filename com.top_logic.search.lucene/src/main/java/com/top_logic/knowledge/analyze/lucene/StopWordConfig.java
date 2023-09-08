/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.analyze.lucene;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Name;

/**
 * Configuration for stop words for {@link StopWordAnalyzerFactory}.
 */
public interface StopWordConfig extends ConfigurationItem {

	/**
	 * The attribute to configure the name of the entity to enable.
	 */
	String STOP_WORD_LANGUAGE = "language";

	/**
	 * The language for which this is the stop-words configuration.
	 */
	@Name(STOP_WORD_LANGUAGE)
	String getLanguage();

	/**
	 * The file in which the stop-words are stored.
	 */
	String getFile();

}
