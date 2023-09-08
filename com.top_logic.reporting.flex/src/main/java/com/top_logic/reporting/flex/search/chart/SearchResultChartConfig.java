/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.search.chart;

import java.util.List;

import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.element.layout.meta.search.AttributedSearchResultSet;
import com.top_logic.model.TLClass;
import com.top_logic.reporting.flex.chart.config.ChartConfig;
import com.top_logic.reporting.flex.chart.config.util.MetaElementProvider;

/**
 * {@link ChartConfig} with additional information about the {@link TLClass} type and the search
 * result columns.
 */
public interface SearchResultChartConfig extends ChartConfig {

	/**
	 * The options displayed in the GUI depends on the result type of the search.
	 * 
	 * @return indirect getter for {@link TLClass}, see {@link MetaElementProvider}
	 */
	public MetaElementProvider getType();

	/**
	 * see {@link #getType()}
	 */
	public void setType(MetaElementProvider type);

	/**
	 * The columns to display in the details dialog.
	 * 
	 * @see AttributedSearchResultSet#getResultColumns()
	 */
	@Format(CommaSeparatedStrings.class)
	@FormattedDefault("name")
	public List<String> getColumns();

	/**
	 * see {@link #getColumns()}
	 */
	public void setColumns(List<String> columns);

}