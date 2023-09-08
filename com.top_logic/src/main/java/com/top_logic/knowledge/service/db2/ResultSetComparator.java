/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Comparator;

import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.knowledge.service.KnowledgeBaseRuntimeException;

/**
 * Comparator of {@link ResultSet} by a given set of attributes.
 * 
 * <p>
 * The {@link ResultSetComparator} is constructed with a sequence of {@link DBAttribute}s. It is
 * expected that the {@link ResultSet} to compare have both columns with
 * {@link DBAttribute#getDBName()} of these attributes and the corresponding values are not
 * <code>null</code> and comparable.
 * </p>
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
class ResultSetComparator implements Comparator<ResultSet> {

	/**
	 * Order attributes.
	 */
	private final DBAttribute[] _orderAttributes;

	/**
	 * Order direction.
	 * 
	 * <p>
	 * Must have the same length as the given attribute array. If an index is <code>true</code>, the
	 * corresponding attribute is compared descending.
	 * </p>
	 */
	private final boolean[] _orderDirection;

	public ResultSetComparator(DBAttribute[] orderAttributes, boolean[] orderDirection) {
		if (orderAttributes.length != orderDirection.length) {
			StringBuilder incorrectOrderDirection = new StringBuilder();
			incorrectOrderDirection.append("Number order attribute and order direction must match: ");
			incorrectOrderDirection.append(Arrays.toString(orderAttributes));
			incorrectOrderDirection.append(" ");
			incorrectOrderDirection.append(Arrays.toString(orderDirection));
			throw new IllegalArgumentException(incorrectOrderDirection.toString());
		}
		_orderAttributes = orderAttributes;
		_orderDirection = orderDirection;
	}

	@Override
	public int compare(ResultSet rs1, ResultSet rs2) {
		try {
			return internalCompare(rs1, rs2);
		} catch (SQLException ex) {
			throw new KnowledgeBaseRuntimeException("Unable to compare result set.", ex);
		}
	}

	private int internalCompare(ResultSet rs1, ResultSet rs2) throws SQLException {
		for (int i = 0; i < _orderAttributes.length; i++) {
			int compare = compare(rs1, rs2, _orderAttributes[i], _orderDirection[i]);
			if (compare != 0) {
				return compare;
			}
		}
		return 0;
	}

	private int compare(ResultSet rs1, ResultSet rs2, DBAttribute orderAttribute, boolean descending)
			throws SQLException {
		Object o1 = rs1.getObject(orderAttribute.getDBName());
		Object o2 = rs2.getObject(orderAttribute.getDBName());
		@SuppressWarnings({ "unchecked", "rawtypes" })
		int compareResult = ((Comparable) o1).compareTo(o2);
		if (descending) {
			compareResult = -1 * compareResult;
		}
		return compareResult;
	}

}
