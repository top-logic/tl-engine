/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.sql;

import java.util.List;

import com.top_logic.dob.meta.MOStructure;
import com.top_logic.dob.meta.TypeContext;


/**
 * DBTableMetaObject <em>can</em> be implemented by classes also implementing
 * {@link com.top_logic.dob.meta.MOStructure} .
 * <p>
 * In fact our implementations do implement this interface. All methods are implemented using lazy
 * initialization so that the impact (when not used) should be negligible.
 * </p>
 *
 * @author <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public interface DBTableMetaObject extends MOStructure {

    /**
     * Returns a name suitable as Table--Name for a Database.
     */
    public String getDBName ();
    
    /**
	 * Returns an List of {@link DBAttribute}s for a database table.
	 * 
	 * <p>
	 * Order of the List is important since the layout of the table will be derived from it. It can
	 * return other values than {@link #getAttribute(String)} does (e.g. a primary key derived for
	 * the getUniqueId method(). )
	 * </p>
	 */
    public List<DBAttribute> getDBAttributes();
    
	/**
	 * The number of columns which are used in the database by the attributes in this table all together.
	 */
	public int getDBColumnCount();
    
    /**
     * @see com.top_logic.basic.sql.DBHelper#appendTableOptions(Appendable, boolean, int)
     */
    public boolean isPKeyStorage();

    /**
     * @see com.top_logic.basic.sql.DBHelper#appendTableOptions(Appendable, boolean, int)
     */
    public int getCompress();

	/**
	 * Whether the corresponding {@link TypeContext} supports multiple branches.
	 * 
	 * @see TypeContext#multipleBranches()
	 */
	boolean multipleBranches();
}
