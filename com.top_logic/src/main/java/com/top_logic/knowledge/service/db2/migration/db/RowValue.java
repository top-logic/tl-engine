/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.db;

import java.util.Map;

import com.top_logic.dob.meta.MOStructure;

/**
 * Representation of a row in a database table.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface RowValue {
	
	/**
	 * Name of the table this row belongs to.
	 */
	MOStructure getTable();

	/**
	 * Mapping from column name to column value of the represented table row.
	 */
	Map<String, Object> getValues();

}
