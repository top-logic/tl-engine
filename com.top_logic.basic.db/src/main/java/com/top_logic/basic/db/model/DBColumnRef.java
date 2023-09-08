/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.func.Function3;

/**
 * A reference by {@link #getName()} to a {@link DBColumn} defined in some context.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface DBColumnRef extends NamedConfigMandatory {

	/**
	 * Implementation of {@link DBIndex#getColumns()}.
	 * 
	 * <p>
	 * Note: The argument <code>columns</code> is necessary to ensure that the result is updated,
	 * whenever the table columns change. Without this argument, the result of the derived property
	 * stays empty, if the column references are set before table is populated with columns.
	 * </p>
	 */
	class ReferencedColumns extends Function3<List<DBColumn>, DBTable, List<DBColumn>, List<DBColumnRef>> {
		@Override
		public List<DBColumn> apply(DBTable table, List<DBColumn> columns, List<DBColumnRef> references) {
			if ((table == null) || (references == null)) {
				return Collections.emptyList();
			}
			ArrayList<DBColumn> result = new ArrayList<>(references.size());
			for (DBColumnRef ref : references) {
				DBColumn column = table.getColumn(ref.getName());
				if (column == null) {
					continue;
				}
				result.add(column);
			}
			return Collections.unmodifiableList(result);
		}
	}

}
