/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.sql;

import com.top_logic.dob.meta.MOIndex;
import com.top_logic.dob.xml.DOXMLConstants;

/** 
 * DBIndex <em>can</em> be implemented by classes also implelementing
 * {@link com.top_logic.dob.meta.MOIndex} .
 *<p>
 * In fact our implementations do implement this interface. All methods
 * are implemented using lazy initialisation so that the impact (when
 * not used) should be neglelible.
 *</p>
 *
 * @author    <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public interface DBIndex extends MOIndex {

    /** Constant to be used when setting/checking the inMemory Flag */
    public static final boolean IN_MEMORY = true;

	/** Constant to be used when setting/checking the {@link #isCustom()} Flag */
	public static final boolean CUSTOM = true;

	/** Constant to be used when setting {@link #getCompress()} flag */
	int NO_COMPRESS = DOXMLConstants.NO_DB_COMPRESS;

    /**
     * Returns a name suiteable as Index-Name for a Database.
     */
    public String getDBName ();
    
    /** Hint for the Implementation to create the index not via
        the Database, but directly in Memory */
    public boolean isInMemory();
    
    /**
     * @see com.top_logic.basic.sql.DBHelper#getAppendIndex(int)
     */
    public int getCompress();

	/**
	 * Whether technical columns should not automatically be added to the index.
	 */
	public boolean isCustom();

}
