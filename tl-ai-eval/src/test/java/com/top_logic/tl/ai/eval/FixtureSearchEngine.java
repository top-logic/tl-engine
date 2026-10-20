/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.eval;

import java.io.IOException;
import java.util.List;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.tl.ai.core.tools.web.WebSearchEngine;
import com.top_logic.tl.ai.core.tools.web.WebSearchHit;
import com.top_logic.tl.ai.core.tools.web.WebSearchQuery;
import com.top_logic.tl.ai.core.tools.web.WebSearchResults;

/**
 * A {@link WebSearchEngine} that answers the queries a test wrote into {@link FixtureWeb}.
 *
 * <p>
 * A query no test wrote goes to the {@link Config#getDelegate() delegate}, the real engine a run
 * against a real model searches with. Without a delegate it fails as a search engine fails, so a
 * script that searches for something else learns it from the tool's answer rather than from an empty
 * result.
 * </p>
 */
public class FixtureSearchEngine implements WebSearchEngine {

	/** Configuration of {@link FixtureSearchEngine}. */
	public interface Config extends PolymorphicConfiguration<FixtureSearchEngine> {

		/** @see #getDelegate() */
		String DELEGATE = "delegate";

		/**
		 * The engine that answers every query no test wrote; empty for none.
		 *
		 * @see #DELEGATE
		 */
		@Name(DELEGATE)
		PolymorphicConfiguration<? extends WebSearchEngine> getDelegate();
	}

	private final WebSearchEngine _delegate;

	/** Creates a {@link FixtureSearchEngine} from configuration. */
	public FixtureSearchEngine(InstantiationContext context, Config config) {
		_delegate = config.getDelegate() == null ? null : context.getInstance(config.getDelegate());
	}

	@Override
	public boolean supportsFreshness() {
		return _delegate != null && _delegate.supportsFreshness();
	}

	@Override
	public boolean supportsLanguage() {
		return _delegate != null && _delegate.supportsLanguage();
	}

	@Override
	public WebSearchResults search(WebSearchQuery query) throws IOException, InterruptedException {
		List<WebSearchHit> hits = FixtureWeb.hits(query.query());
		if (hits == null && _delegate != null) {
			return _delegate.search(query);
		}
		if (hits == null) {
			throw new IOException("The fixture web has no results for the query: " + query.query());
		}
		return new WebSearchResults("", hits.subList(0, Math.min(query.count(), hits.size())));
	}
}
