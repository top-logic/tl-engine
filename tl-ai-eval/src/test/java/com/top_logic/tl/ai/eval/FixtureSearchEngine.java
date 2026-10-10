/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.eval;

import java.io.IOException;
import java.util.List;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.tl.ai.core.tools.web.WebSearchEngine;
import com.top_logic.tl.ai.core.tools.web.WebSearchHit;
import com.top_logic.tl.ai.core.tools.web.WebSearchQuery;
import com.top_logic.tl.ai.core.tools.web.WebSearchResults;

/**
 * A {@link WebSearchEngine} that answers the queries a test wrote into {@link FixtureWeb}.
 *
 * <p>
 * A query no test wrote fails as a search engine fails, so a script that searches for something
 * else learns it from the tool's answer rather than from an empty result.
 * </p>
 */
public class FixtureSearchEngine implements WebSearchEngine {

	/** Configuration of {@link FixtureSearchEngine}. */
	public interface Config extends PolymorphicConfiguration<FixtureSearchEngine> {
		// No properties: what it answers is written by the test.
	}

	/** Creates a {@link FixtureSearchEngine} from configuration. */
	public FixtureSearchEngine(InstantiationContext context, Config config) {
		// Nothing to configure.
	}

	@Override
	public boolean supportsFreshness() {
		return false;
	}

	@Override
	public boolean supportsLanguage() {
		return false;
	}

	@Override
	public WebSearchResults search(WebSearchQuery query) throws IOException {
		List<WebSearchHit> hits = FixtureWeb.hits(query.query());
		if (hits == null) {
			throw new IOException("The fixture web has no results for the query: " + query.query());
		}
		return new WebSearchResults("", hits.subList(0, Math.min(query.count(), hits.size())));
	}
}
