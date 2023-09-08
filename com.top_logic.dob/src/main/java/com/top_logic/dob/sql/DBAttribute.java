/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.sql;

import com.top_logic.basic.sql.DBType;
import com.top_logic.dob.MOAttribute;

/**
 * DBAttribute can be implemented by classes also implementing
 * {@link com.top_logic.dob.MOAttribute}.
 * 
 * <p>
 * In fact our implementations do implement this interface. All methods are
 * implemented using lazy initialisation so that the impact (when not used)
 * should be neglelible.
 * </p>
 * <p>
 * The interface is used for Structured as well as for basic MetaObjects.
 * Structured MetaObjects should as well implement the
 * {@link com.top_logic.dob.sql.DBTableMetaObject}.
 * </p>
 * 
 * @author <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public interface DBAttribute {

	public static final int DEFAULT_DB_OFFSET = 1;

	/**
	 * Empty array of type {@link DBAttribute}.
	 */
	public static final DBAttribute[] NO_DB_ATTRIBUTES = new DBAttribute[0];
	
    /**
     * Returns a name suiteable as Table- or Column-Name for a Database.
     *
     * @return null in case such a name can not be created.
     */
    public String getDBName ();
    
    /**
	 * The column type to use.
	 */
    public DBType getSQLType();
    
    /**
     * get the Size of the desired SQL field (eg. for VARCHAR)
     *
     * @return 0 in case a Size is not required.
     */
    public int getSQLSize();

    /**
     * get the Precision of the desired SQL field (eg. for DECIMAL)
     *
     * @return 0 in case a Precision is not required.
     */
    public int getSQLPrecision();

    /**
     * Whether a string typed column should use binary compare (no collating sequence). 
     */
    public boolean isBinary();

	/**
	 * Sets the column index of this {@link DBAttribute} in the database.
	 * 
	 * @param index
	 *        the DB index.
	 */
	public void initDBColumnIndex(int index);
	
	/**
	 * The zero-based column index in the defining table this {@link DBAttribute} stores to.
	 * 
	 * If there is no corresponding column it returns <code>-1</code>.
	 */
	public int getDBColumnIndex();
	
	
	/**
	 * Returns the {@link MOAttribute} corresponding to this {@link DBAttribute}.
	 */
	MOAttribute getAttribute();
	
	/**
	 * Whether the represented database column is "not null".
	 * 
	 * @return <code>true</code> iff the database column must be "not null".
	 */
	default boolean isSQLNotNull() {
		return getAttribute().isMandatory();
	}

}
