/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.index.config;

import java.util.regex.Pattern;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.tl.ai.index.text.Chunker;

/**
 * How the texts of indexed objects are cut into chunks.
 */
public interface ChunkingConfig extends ConfigurationItem {

	/** Configuration name of {@link #getMaxChars()}. */
	String MAX_CHARS = "max-chars";

	/** Configuration name of {@link #getOverlapSentences()}. */
	String OVERLAP_SENTENCES = "overlap-sentences";

	/** Configuration name of {@link #getPageMarker()}. */
	String PAGE_MARKER = "page-marker";

	/** Configuration name of {@link #getMaxTextChars()}. */
	String MAX_TEXT_CHARS = "max-text-chars";

	/**
	 * The largest chunk in characters.
	 *
	 * <p>
	 * 1,200 characters are about 300 tokens of German text, below the 512 tokens of the smaller
	 * models with the label in front.
	 * </p>
	 *
	 * @see #MAX_CHARS
	 */
	@Name(MAX_CHARS)
	@IntDefault(1200)
	int getMaxChars();

	/**
	 * How many sentences a chunk repeats from the one before it.
	 *
	 * @see #OVERLAP_SENTENCES
	 */
	@Name(OVERLAP_SENTENCES)
	@IntDefault(1)
	int getOverlapSentences();

	/**
	 * A regular expression for a line that starts a page, with the page number as its first group,
	 * e.g. {@code ^\[p\.(\d+)\]$}. A chunk never crosses a page, so a hit can name its page.
	 *
	 * @see #PAGE_MARKER
	 */
	@Name(PAGE_MARKER)
	@Nullable
	String getPageMarker();

	/**
	 * The most characters of one text that are indexed; the rest of a longer text is left out.
	 *
	 * @see #MAX_TEXT_CHARS
	 */
	@Name(MAX_TEXT_CHARS)
	@IntDefault(2_000_000)
	int getMaxTextChars();

	/**
	 * The chunker of this configuration.
	 */
	static Chunker chunker(ChunkingConfig config) {
		String marker = config.getPageMarker();
		return new Chunker(config.getMaxChars(), config.getOverlapSentences(),
			marker == null || marker.isBlank() ? null : Pattern.compile(marker, Pattern.MULTILINE));
	}

}
