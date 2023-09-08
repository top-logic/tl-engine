/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.sql;

import com.top_logic.basic.sql.DBType;
import com.top_logic.dob.MetaObject;

/** 
 * DBMetaObject <em>can</em> be implemented by classes also implelementing
 * {@link com.top_logic.dob.MetaObject} .
 *<p>
 * In fact our implementations do implement this interface. All methods
 * are implemented using lazy initialisation so that the impact (when
 * not used) should be neglelible.
 *</p>
 *<p>
 *  The interface is used for Structured as well as for basic MetaObjects.
 *  Structured MetaObjects should as well implement the 
 *  {@link com.top_logic.dob.sql.DBTableMetaObject}.
 *</p>
 *
 * @author    <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public interface DBMetaObject extends MetaObject {

    /**
	 * Default column type for this primitive type.
	 * 
	 * <p>
	 * This is the default for {@link com.top_logic.dob.sql.DBAttribute#getSQLType()} of an
	 * attribute using this primitive type.
	 * </p>
	 */
    public DBType getDefaultSQLType();
    
    /**
     * get the Size of the desired SQL field (eg. for VARCHAR)
     *<p>
     *  This can be used as default value for 
     *  {@link com.top_logic.dob.sql.DBAttribute#getSQLSize()}
     *</p>
     * @return 0 in case a Size is not required.
     */
    public int getDefaultSQLSize();

    /**
     * get the Precision of the desired SQL field (eg. for DECIMAL)
     *<p>
     *  This can be used as default value for 
     *  {@link com.top_logic.dob.sql.DBAttribute#getSQLPrecision()}
     *</p>
     * @return 0 in case a Precision is not required.
     */
    public int getDefaultSQLPrecision();
}
