/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.model;

import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.func.Function2;

/**
 * Reference by {@link #getName()} to an {@link DBIndex} in some context {@link DBTable}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface DBIndexRef extends NamedConfigMandatory {

	/**
	 * Implementation of {@link DBIndex#getColumns()}.
	 */
	class ReferencedIndex extends Function2<DBIndex, DBTable, DBIndexRef> {
		@Override
		public DBIndex apply(DBTable table, DBIndexRef ref) {
			if (table == null) {
				return null;
			}

			DBIndex primaryKey = table.getPrimaryKey();
			if (primaryKey != null && matches(primaryKey, ref)) {
				return primaryKey;
			}

			for (DBIndex index : table.getIndices()) {
				if (matches(index, ref)) {
					return index;
				}
			}
			return null;
		}

		private boolean matches(DBIndex index, DBIndexRef ref) {
			return index.getName().equals(ref.getName());
		}
	}

}
