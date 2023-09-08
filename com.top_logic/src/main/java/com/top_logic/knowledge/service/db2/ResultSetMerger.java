/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.sql.ResultSet;

import com.top_logic.basic.sql.ResultSetProxy;
import com.top_logic.dob.sql.DBAttribute;

/**
 * Factory class to create a {@link ResultSet} that merges multiple {@link ResultSet} into one.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class ResultSetMerger extends ResultSetProxy {

	/**
	 * Creates a {@link ResultSet} that merges the results of all given {@link ResultSet}s.
	 * 
	 * <p>
	 * The {@link ResultSet}s are merged using the given order attributes. Each {@link ResultSet}
	 * must respect the same order.
	 * </p>
	 */
	public static ResultSet newMultipleResultSet(ResultSet[] results, DBAttribute[] orderAttributes,
			boolean[] orderDirection) {
		switch (results.length) {
			case 1: {
				return results[0];
			}
			case 2: {
				ResultSetComparator comparator = new ResultSetComparator(orderAttributes, orderDirection);
				return new TwoResultSetMerge(results[0], results[1], comparator);
			}
			default: {
				ResultSetComparator comparator = new ResultSetComparator(orderAttributes, orderDirection);
				return new MultipleResultSet(results, comparator);
			}
		}

	}

}
