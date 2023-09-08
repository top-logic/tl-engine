/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.model;

import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.sql.DBType;

/**
 * Definition of a columns of a {@link DBTable}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface DBColumn extends DBTablePart {

	/**
	 * @see #getType()
	 */
	String TYPE = "type";

	/**
	 * @see #isMandatory()
	 */
	String MANDATORY = "mandatory";

	/**
	 * @see #isBinary()
	 */
	String BINARY = "binary";

	/**
	 * @see #getSize()
	 */
	String SIZE = "size";

	/**
	 * @see #getPrecision()
	 */
	String PRECISION = "precision";

	/**
	 * The JDBC column type.
	 */
	@Name(TYPE)
	@Mandatory
	DBType getType();
	
	/**
	 * Sets the {@link #getType()} property.
	 */
	void setType(DBType value);

	/**
	 * Whether this column must contain a non <code>null</code> value. 
	 */
	@Name(MANDATORY)
	boolean isMandatory();
	
	/**
	 * Sets the {@link #isMandatory()} property.
	 */
	void setMandatory(boolean value);
	
	/**
	 * Whether this column contains binary for string typed columns. 
	 */
	@Name(BINARY)
	boolean isBinary();
	
	/**
	 * Sets the {@link #isBinary()} property.
	 */
	void setBinary(boolean value);
	
	/**
	 * Size limit constraint.
	 * 
	 * @return The size constraint of this column, or <code>0</code> for no
	 *         limit.
	 */
	@Name(SIZE)
	long getSize();
	
	/**
	 * Sets the {@link #getSize()} property.
	 */
	void setSize(long value);

	/**
	 * Precision limit constraint for columns of decimal type.
	 * 
	 * @return The precision constraint of this column, or <code>0</code> for no
	 *         limit.
	 */
	@Name(PRECISION)
	int getPrecision();
	
	/**
	 * Sets the {@link #getPrecision()} property.
	 */
	void setPrecision(int value);

}
