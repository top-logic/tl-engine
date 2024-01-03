/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.selector;

import com.top_logic.basic.config.annotation.Name;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.ui.model.Search;
import com.top_logic.model.search.ui.model.structure.SearchPart;
import com.top_logic.reporting.flex.chart.config.ChartConfig;

/**
 * The combination of a {@link Search} expression and a {@link ChartConfig}.
 * <p>
 * This type has to be a subtype of {@link SearchPart}, as it contains a {@link Search}, and that is
 * a {@link SearchPart} itself, declaring a container property of type {@link SearchPart}. It is
 * therefore not allowed to put it into a container that is not a {@link SearchPart}, as that would
 * violate the container property type.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public interface SearchAndReportConfig extends SearchPart {

	/** Property name of {@link #getSearch()}. */
	String SEARCH = "search";

	/** Property name of {@link #getReport()}. */
	String CHART = "chart";

	/** Property name of {@link #getSearchType()}. */
	String TYPE = "type";

	/** Never null. */
	@Name(SEARCH)
	Search getSearch();

	/** @see #getSearch() */
	void setSearch(Search search);

	/**
	 * TL-Script search expression.
	 */
	Expr getExpertSearch();

	/**
	 * @see #getExpertSearch()
	 */
	void setExpertSearch(Expr search);

	/** Never null. */
	@Name(CHART)
	ChartConfig getReport();

	/** @see #getReport() */
	void setReport(ChartConfig report);

	/**
	 * Where the search expression is created.
	 * 
	 * @see SearchType
	 */
	@Name(TYPE)
	SearchType getSearchType();

	/**
	 * @see #getSearchType()
	 */
	void setSearchType(SearchType type);

}
