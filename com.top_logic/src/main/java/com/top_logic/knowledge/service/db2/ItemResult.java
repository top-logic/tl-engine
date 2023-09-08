/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.sql.SQLException;

import com.top_logic.basic.TLID;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MOReference.ReferencePart;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.dob.meta.ObjectContext;
import com.top_logic.dob.sql.DBAttribute;

/**
 * Interface describing DB based access to the data of a {@link MOStructure}
 * typed object.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ItemResult extends QueryResult {

	/**
	 * Retrieve the generic values of the given structure.
	 */
	Object getValue(MOAttribute attribute, ObjectContext context) throws SQLException;

	/**
	 * Retrieve the value of the given attribute as {@link String}.
	 */
	String getStringValue(DBAttribute attribute) throws SQLException;

	/**
	 * Retrieve the value of the given part of the the given attribute as {@link String}.
	 */
	String getStringValue(MOReference attribute, ReferencePart part) throws SQLException;

	/**
	 * Retrieve the value of the given CLOB attribute as {@link String}.
	 */
	String getClobStringValue(DBAttribute attribute) throws SQLException;

	/**
     * Retrieve the value of the given BLOB attribute as {@link BinaryData}.
     */
	BinaryData getBlobValue(DBAttribute nameAttr, DBAttribute contentTypeAttr, DBAttribute sizeAttr,
			DBAttribute blobAttr) throws SQLException;

	/**
	 * Retrieve the value of the given attribute as <code>int</code>.
	 */
	int getIntValue(DBAttribute attribute) throws SQLException;

	/**
	 * Retrieve the value of the given attribute as <code>long</code>.
	 */
	long getLongValue(DBAttribute attribute) throws SQLException;

	/**
	 * Retrieve the value of the given attribute as <code>float</code>.
	 */
	float getFloatValue(DBAttribute attribute) throws SQLException;

	/**
	 * Retrieve the value of the given attribute as <code>double</code>.
	 */
	double getDoubleValue(DBAttribute attribute) throws SQLException;

	/**
	 * Retrieve the value of the given attribute as <code>byte</code>.
	 */
	byte getByteValue(DBAttribute attribute) throws SQLException;

	/**
	 * Retrieve the value of the given attribute as internal identifier.
	 */
	TLID getIDValue(DBAttribute attribute) throws SQLException;

}
