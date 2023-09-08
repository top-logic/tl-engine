/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.model.partition.criteria;

import com.top_logic.reporting.report.model.partition.Partition;

/**
 * A Criteria defines the filters that are used to create a {@link Partition}. This can be date ranges,
 * numbers, string constraints etc.
 * 
 * @author <a href="mailto:tbe@top-logic.com">tbe</a>
 */
@Deprecated
public interface Criteria {

	public String getCriteriaTyp();

}
