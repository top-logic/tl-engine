/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.schema.config.annotation;

import java.util.List;

import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.dob.sql.DBIndex;

/**
 * Strategy for translating index columns for custom indices in custom type to a technical index
 * columns list.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface IndexColumnsStrategy {

	/**
	 * Creates the index columns for the given index with respect to technical columns such as
	 * branch and revision.
	 * 
	 * @param type
	 *        The type for which the index is created.
	 * @param index
	 *        The index to create the column list for.
	 * @return The columns that should be used for the corresponding database index.
	 */
	List<DBAttribute> createIndexColumns(MOClass type, DBIndex index);

}
