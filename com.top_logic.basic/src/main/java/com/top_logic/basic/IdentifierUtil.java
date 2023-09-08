/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.sql.DBType;

/**
 * Algorithms to store and convert internal object identifiers.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class IdentifierUtil {

	/**
	 * Whether short identifiers are enabled.
	 */
	public static final boolean SHORT_IDS = true;

	private static final TLID NULL_ID;
	static {
		if (SHORT_IDS) {
			NULL_ID = LongID.mkId(0);
		} else {
			NULL_ID = StringID.valueOf("#");
		}
	}

	/**
	 * {@link TLID} that is used to store in mandatory database column as representation for
	 * <code>null</code>.
	 */
	@FrameworkInternal
	public static TLID nullIdForMandatoryDatabaseColumns() {
		return NULL_ID;
	}

	/**
	 * Convert an identifier to an external representation.
	 * 
	 * @see #fromExternalForm(String)
	 */
	public static String toExternalForm(TLID identifier) {
		if (identifier == null) {
			return null;
		}
		return identifier.toExternalForm();
	}

	/**
	 * Parse an identifier from an external representation.
	 * 
	 * @see #toExternalForm(TLID)
	 */
	public static TLID fromExternalForm(String externalId) {
		if (IdentifierUtil.SHORT_IDS) {
			return LongID.fromExternalForm(externalId);
		} else {
			return StringID.fromExternalForm(externalId);
		}
	}

	/**
	 * Stores an identifier into a {@link PreparedStatement} parameter.
	 * 
	 * @param pstm
	 *        See {@link PreparedStatement#setObject(int, Object)}.
	 * @param index
	 *        See {@link PreparedStatement#setObject(int, Object)}.
	 * @param id
	 *        The identifier to store.
	 * @throws SQLException
	 *         See {@link PreparedStatement#setObject(int, Object)}.
	 */
	public static void setId(PreparedStatement pstm, int index, TLID id) throws SQLException {
		if (id == null) {
			pstm.setNull(index, DBType.ID.sqlType);
		} else {
			if (IdentifierUtil.SHORT_IDS) {
				pstm.setLong(index, ((LongID) id).longValue());
			} else {
				pstm.setString(index, ((StringID) id).stringValue());
			}
		}
	}

	/**
	 * Retrieves an identifier from a {@link ResultSet}.
	 * 
	 * @param result
	 *        See {@link ResultSet#getObject(int)}.
	 * @param index
	 *        See {@link ResultSet#getObject(int)}.
	 * @throws SQLException
	 *         See {@link ResultSet#getObject(int)}.
	 */
	public static TLID getId(ResultSet result, int index) throws SQLException {
		if (IdentifierUtil.SHORT_IDS) {
			long id = result.getLong(index);
			if (result.wasNull()) {
				return null;
			} else {
				return LongID.valueOf(id);
			}
		} else {
			String id = result.getString(index);
			if (result.wasNull()) {
				return null;
			} else {
				return StringID.valueOf(id);
			}
		}
	}

	/**
	 * The database column size of an identifier column.
	 */
	public static final int REFERENCE_DB_SIZE = SHORT_IDS ? 20 : 70;

}
