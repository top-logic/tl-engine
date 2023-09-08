/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.attr.storage;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.TLID;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.attr.AbstractMOReference;
import com.top_logic.dob.sql.DBAttribute;

/**
 * {@link AbstractMOAttributeStorageImpl} for {@link AbstractMOReference}s.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class MOReferenceStorageImpl extends AbstractMOAttributeStorageImpl {

	/**
	 * Stores the given value using declaration of the given {@link DBAttribute}.
	 * 
	 * @see #fetchValue(DBHelper, ResultSet, int, MOAttribute, com.top_logic.dob.meta.ObjectContext)
	 */
	protected TLID readId(DBAttribute dbAttribute, ResultSet dbResult, int resultOffset) throws SQLException {
		return IdentifierUtil.getId(dbResult, resultOffset + dbAttribute.getDBColumnIndex());
	}

	/**
	 * Stores the given value using declaration of the given {@link DBAttribute}.
	 * 
	 * @see #fetchValue(DBHelper, ResultSet, int, MOAttribute, com.top_logic.dob.meta.ObjectContext)
	 */
	protected Object readValue(DBAttribute dbAttribute, DBHelper sqlDialect, ResultSet dbResult,
			int resultOffset) throws SQLException {
		return sqlDialect.mapToJava(dbResult, resultOffset + dbAttribute.getDBColumnIndex(), dbAttribute.getSQLType());
	}

}
