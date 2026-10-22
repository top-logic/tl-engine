/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.eval;

import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.IntDefault;

/**
 * An {@link Expectation} on the sources of the answer: it cites enough of the pages the run read, and
 * links nothing the run did not read.
 *
 * <p>
 * Deterministic, and that is its value next to {@link JudgeExpectation}: whether a link points at a
 * read page is a fact the run recorded, not an opinion. A cited source is a footnote the citation
 * check kept - so it was on the run's reading list, the lead's or a worker's. An unread link is a web
 * link in the model's own text that did not become a footnote: an invented address, a search result
 * never opened, a page remembered from training.
 * </p>
 *
 * <pre>
 * &lt;citations label="cites-what-it-read" min-sources="3" min-domains="2"/&gt;
 * </pre>
 */
@TagName(CitationExpectation.TAG_NAME)
public interface CitationExpectation extends Expectation {

	/** The XML tag this expectation is written as. */
	String TAG_NAME = "citations";

	/** @see #getMinSources() */
	String MIN_SOURCES = "min-sources";

	/** @see #getMinDomains() */
	String MIN_DOMAINS = "min-domains";

	/** @see #getMaxUnreadLinks() */
	String MAX_UNREAD_LINKS = "max-unread-links";

	/**
	 * How many distinct sources the answer must cite at least.
	 *
	 * <p>
	 * At least 1, which is what makes this expectation fail a run that answered without a source.
	 * </p>
	 *
	 * @see #MIN_SOURCES
	 */
	@Name(MIN_SOURCES)
	@IntDefault(1)
	int getMinSources();

	/**
	 * From how many distinct web hosts the cited sources must come at least; 0 for no bound.
	 *
	 * <p>
	 * A report that cites three pages of one vendor is one voice, however many footnotes it has.
	 * {@code www.} is not part of the host.
	 * </p>
	 *
	 * @see #MIN_DOMAINS
	 */
	@Name(MIN_DOMAINS)
	int getMinDomains();

	/**
	 * How many web links of the model's text may point at pages no run read.
	 *
	 * @see #MAX_UNREAD_LINKS
	 */
	@Name(MAX_UNREAD_LINKS)
	int getMaxUnreadLinks();
}
