/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.layout.meta.search;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.top_logic.element.layout.meta.search.AttributedSearchResultSet;
import com.top_logic.model.TLClass;
import com.top_logic.reporting.report.model.ReportConfiguration;

/**
 * The AttributedSearchReportingResultSet keeps the normal search results as well as the information
 * necessary for report generation.
 * 
 * @author <a href="mailto:tbe@top-logic.com">tbe</a>
 */
@Deprecated
public class ReportingAttributedSearchResultSet extends AttributedSearchResultSet {

	/**
	 * Contains key-value pairs for the chart. Keys are generally the field names which are used to
	 * select reporting options.
	 */
	private ReportConfiguration	config;

	public ReportingAttributedSearchResultSet(Collection aResult, TLClass type, List someColumns,
			List someMessages, ReportConfiguration aConfig) {
		this(aResult, Collections.singleton(type), someColumns, someMessages, aConfig);
	}

	/**
	 * Creates a {@link ReportingAttributedSearchResultSet}.
	 * 
	 * @param types
	 *        The meta element being base of this search result, must not be <code>null</code>.
	 */
	public ReportingAttributedSearchResultSet(Collection aResult, Set<? extends TLClass> types, List someColumns,
			List someMessages, ReportConfiguration aConfig) {
		super(aResult, types, someColumns, someMessages);
		this.config = aConfig;
	}
	
	public ReportingAttributedSearchResultSet(AttributedSearchResultSet aResultSet, ReportConfiguration aConfig) {
		this(aResultSet.getResultObjects(), aResultSet.getTypes(), aResultSet.getResultColumns(),
			aResultSet.getSearchMessages(), aConfig);
	}

	/**
	 * This method returns the options.
	 * 
	 * @return Returns the options.
	 */
	public ReportConfiguration getReportConfiguration() {
		return (this.config);
	}
}
