/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.model.partition.criteria;

import com.top_logic.reporting.report.model.partition.function.PartitionFunction;

/**
 * The StringCriteria is a filter for {@link PartitionFunction}s based on Strings.
 * 
 * @author    <a href="mailto:tbe@top-logic.com">tbe</a>
 */
@Deprecated
public class StringCriteria implements Criteria{

	private String criteria;

	/** 
	 * Creates a {@link StringCriteria}.
	 * 
	 */
	public StringCriteria(String aCriteria) {
		this.criteria = aCriteria;
	}

	public Object getCriteria() {
		return this.criteria;
	}
	
	@Override
	public String getCriteriaTyp() {
		return "string-criteria";
	}
}

