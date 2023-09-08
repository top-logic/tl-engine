/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.attr.storage;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.top_logic.basic.ExtID;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.dob.AttributeStorage;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.attr.ExtIDAttribute;
import com.top_logic.dob.ex.IncompatibleTypeException;
import com.top_logic.dob.meta.ObjectContext;
import com.top_logic.dob.sql.DBAttribute;

/**
 * {@link AttributeStorage} storing an {@link ExtID}.
 * 
 * <p>
 * It is expected that the {@link MOAttribute} has a {@link MOAttribute#getDbMapping()} which has
 * two long columns.
 * </p>
 * 
 * @see ExtIDAttribute
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ExtIDAttributeStorage extends SimpleMOAttributeStorageImpl {

	/** Singleton instance of {@link ExtIDAttributeStorage} */
	public static final ExtIDAttributeStorage INSTANCE = new ExtIDAttributeStorage();

	@Override
	public Object fetchValue(DBHelper sqlDialect, ResultSet dbResult, int resultOffset, MOAttribute attribute,
			ObjectContext context) throws SQLException {
		long systemId = dbResult.getLong(resultOffset + systemAttribute(attribute).getDBColumnIndex());
		if (dbResult.wasNull()) {
			return null;
		}
		long objectId = dbResult.getLong(resultOffset + objectAttribute(attribute).getDBColumnIndex());
		return new ExtID(systemId, objectId);
	}

	private DBAttribute objectAttribute(MOAttribute attribute) {
		return attribute.getDbMapping()[1];
	}

	private DBAttribute systemAttribute(MOAttribute attribute) {
		return attribute.getDbMapping()[0];
	}

	@Override
	public void storeValue(ConnectionPool pool, Object[] stmtArgs, int stmtOffset, MOAttribute attribute,
			DataObject item, Object[] storage, long currentCommitNumber) throws SQLException {
		ExtID extid = (ExtID) getCacheValue(attribute, item, storage);
		if (extid == null) {
			storeObject(systemAttribute(attribute), stmtArgs, stmtOffset, item, null);
			storeObject(objectAttribute(attribute), stmtArgs, stmtOffset, item, null);
		} else {
			storeLong(systemAttribute(attribute), stmtArgs, stmtOffset, extid.systemId());
			storeLong(objectAttribute(attribute), stmtArgs, stmtOffset, extid.objectId());
		}
	}

	/** Do not use standard check, because there is no correct MOPrimitive for an {@link ExtID}. */
	@Override
	protected void defaultCheck(MOAttribute attribute, DataObject data, Object value)
			throws IncompatibleTypeException, DataObjectException {
		checkImmutable(attribute, data, value);
		if (value == null) {
			// ok
			return;
		}
		if (!(value instanceof ExtID)) {
			StringBuilder incompatibleType = new StringBuilder();
			incompatibleType.append("Incompatible type '");
			incompatibleType.append(value.getClass().getName());
			incompatibleType.append("' for attribute '");
			appendAttribute(incompatibleType, attribute);
			incompatibleType.append("'");
			throw new IncompatibleTypeException(incompatibleType.toString());

		}
	}

}

