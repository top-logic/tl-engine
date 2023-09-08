/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.model;

import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.func.Function2;

/**
 * A reference by {@link #getName()} to a {@link DBTable} defined in a {@link DBSchema}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface DBTableRef extends NamedConfigMandatory {

	/**
	 * Implementation of {@link DBIndex#getColumns()}.
	 */
	class ReferencedTable extends Function2<DBTable, DBSchema, DBTableRef> {
		@Override
		public DBTable apply(DBSchema schema, DBTableRef ref) {
			if (schema == null || ref == null) {
				return null;
			}
			return schema.getTable(ref.getName());
		}
	}

}
