/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.top_logic.basic.sql.DBHelper;
import com.top_logic.dob.AttributeStorage;
import com.top_logic.dob.MOAttribute;

/**
 * Special {@link AttributeStorage} that must be implemented by {@link AttributeStorage} that serves
 * as {@link AttributeStorage} for attributes containing the branch of an object.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface IBranchStorage extends AttributeStorage {

	/**
	 * Retrieves the branch of the object from the {@link ResultSet} containing the data of the
	 * object.
	 * 
	 * @param sqlDialect
	 *        The SQL dialect of the database, the given {@link ResultSet} belongs to.
	 * @param dbResult
	 *        The {@link ResultSet} containing the data to retrieve branch from.
	 * @param resultOffset
	 *        The default offset in the result set where the data for the object begins.
	 * @param branchAttribute
	 *        The branch attribute of the corresponding type.
	 * @throws SQLException
	 *         iff database error occurred.
	 */
	long getBranch(DBHelper sqlDialect, ResultSet dbResult, int resultOffset, MOAttribute branchAttribute) throws SQLException;

}

