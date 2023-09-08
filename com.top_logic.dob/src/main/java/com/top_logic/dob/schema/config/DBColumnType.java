/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.schema.config;

import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.sql.DBType;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.dob.xml.DOXMLConstants;

/**
 * Definition of the type aspect of a database column.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface DBColumnType {

	/**
	 * @see DBAttribute#getSQLType()
	 */
	@Name(DOXMLConstants.DB_TYPE_ATTRIBUTE)
	DBType getDBType();

	/** @see #getDBType() */
	void setDBType(DBType value);

	/**
	 * @see DBAttribute#getSQLSize()
	 */
	@Name(DOXMLConstants.DB_SIZE_ATTRIBUTE)
	Integer getDBSize();

	/** @see #getDBSize() */
	void setDBSize(Integer value);

	/**
	 * @see DBAttribute#getSQLPrecision()
	 */
	@Name(DOXMLConstants.DB_PREC_ATTRIBUTE)
	Integer getDBPrecision();

	/** @see #getDBPrecision() */
	void setDBPrecision(Integer value);

	/**
	 * @see DBAttribute#isBinary()
	 */
	@Name(DOXMLConstants.BINARY_ATTRIBUTE)
	Boolean isBinary();

	/** @see #isBinary() */
	void setBinary(Boolean value);

}
