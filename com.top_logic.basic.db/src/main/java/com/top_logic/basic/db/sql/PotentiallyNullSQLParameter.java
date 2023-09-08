/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.sql;

import com.top_logic.basic.sql.DBType;

/**
 * {@link SQLParameter} that may also be filled with <code>null</code> values.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
class PotentiallyNullSQLParameter extends SQLParameter {

	PotentiallyNullSQLParameter(DBType jdbcType, Conversion conversion, String name) {
		super(jdbcType, conversion, name);
	}

	@Override
	boolean isPotentiallyNull() {
		return true;
	}

}

