/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.meta;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.sql.DBAttribute;

/**
 * Arrangement of the columns of an index over versioned rows in multiple branches.
 *
 * <p>
 * Such an index consists of three groups of columns: the branch columns, the key columns
 * identifying the indexed value, and the revision columns. The revision columns always come last,
 * since queries only use them in range conditions. The branch columns either lead the index
 * (default) or follow the key columns directly before the revision columns, see
 * {@link TypeContext#branchLast()}.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class IndexColumnOrder {

	private IndexColumnOrder() {
		// Static utility.
	}

	/**
	 * Arranges the given column groups into the column list of an index.
	 *
	 * @param branchLast
	 *        Whether the branch columns follow the key columns instead of leading the index.
	 * @param branchColumns
	 *        The branch columns of the index, may be empty.
	 * @param keyColumns
	 *        The columns identifying the indexed value.
	 * @param revisionColumns
	 *        The revision columns of the index, may be empty.
	 * @return The index columns: either branch, key, revision columns, or with
	 *         <code>branchLast</code> key, branch, revision columns.
	 */
	public static List<DBAttribute> arrange(boolean branchLast, List<? extends DBAttribute> branchColumns,
			List<? extends DBAttribute> keyColumns, List<? extends DBAttribute> revisionColumns) {
		List<DBAttribute> result =
			new ArrayList<>(branchColumns.size() + keyColumns.size() + revisionColumns.size());
		if (!branchLast) {
			result.addAll(branchColumns);
		}
		result.addAll(keyColumns);
		if (branchLast) {
			result.addAll(branchColumns);
		}
		result.addAll(revisionColumns);
		return result;
	}

	/**
	 * Moves the columns of the {@link BasicTypes#BRANCH_ATTRIBUTE_NAME} attribute directly before
	 * the columns of the {@link BasicTypes#REV_MAX_ATTRIBUTE_NAME} attribute, or to the end of the
	 * index, if it has no such column.
	 *
	 * <p>
	 * The order of all other columns is kept.
	 * </p>
	 *
	 * @param columns
	 *        The index columns.
	 * @return The rearranged index columns.
	 *
	 * @see #arrange(boolean, List, List, List)
	 */
	public static List<DBAttribute> branchLast(List<? extends DBAttribute> columns) {
		List<DBAttribute> branchColumns = new ArrayList<>(1);
		List<DBAttribute> keyColumns = new ArrayList<>(columns.size());
		List<DBAttribute> revisionColumns = new ArrayList<>(1);
		for (DBAttribute column : columns) {
			String attributeName = attributeName(column);
			if (BasicTypes.BRANCH_ATTRIBUTE_NAME.equals(attributeName)) {
				branchColumns.add(column);
			} else if (BasicTypes.REV_MAX_ATTRIBUTE_NAME.equals(attributeName)) {
				revisionColumns.add(column);
			} else {
				keyColumns.add(column);
			}
		}
		return arrange(true, branchColumns, keyColumns, revisionColumns);
	}

	private static String attributeName(DBAttribute column) {
		MOAttribute attribute = column.getAttribute();
		return attribute == null ? null : attribute.getName();
	}

}
