/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model.options;

import static java.util.Collections.*;

import java.util.List;

import com.top_logic.basic.func.Function1;
import com.top_logic.model.search.ModelBasedSearch;
import com.top_logic.model.search.ModelBasedSearch.SearchConfig;
import com.top_logic.model.search.ui.model.combinator.SearchExpressionCombinator;

/**
 * Option provider for {@link SearchExpressionCombinator}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class ConfiguredSearchExpressionCombinators extends Function1<List<SearchExpressionCombinator>, String> {

	/** The {@link ConfiguredSearchExpressionCombinators} instance. */
	public static final ConfiguredSearchExpressionCombinators INSTANCE = new ConfiguredSearchExpressionCombinators();

	@Override
	public List<SearchExpressionCombinator> apply(String configName) {
		SearchConfig searchConfig = ModelBasedSearch.getInstance().getSearchConfig(configName);
		if (searchConfig == null) {
			return emptyList();
		}
		return searchConfig.getCombinators();
	}

}
