/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.meta;

import java.util.List;

import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MOPart;
import com.top_logic.dob.sql.DBAttribute;

/**
 * MOIndex contains hints for the Implementation to speed up access to Objects.
 *
 * a MOIndex should normally be part of a {@link com.top_logic.dob.meta.MOStructure}
 *
 * @author  Klaus Halfmann
 */
public interface MOIndex extends MOPart {

    /** Constant to be used when setting/checking the unique Flag */
    public static final boolean UNIQUE = true;

    /** 
     * Hint that index is in Memory only.
     *
     * As of now suhc indexes are not create in the DB, but can
     * only created when all Objects are held in memory wich none
     * of the current implementations does.
     */
    public static final boolean IN_MEMORY = true;

	/**
	 * Returns a collection of the {@link MOAttribute} to which the attributes
	 * in {@link #getKeyAttributes()} belongs to
	 */
    public List<MOAttribute> getAttributes();

	/**
	 * The list of {@link DBAttribute}s that make up the key of this {@link MOIndex}.
	 */
	public List<DBAttribute> getKeyAttributes();
    

    /** A Unique index will contain only one element per key, */
    public boolean isUnique();
    
    /**
     * Creates an unresolved copy of this {@link MOIndex}.
     */
	MOIndex copy();

	/**
	 * Resolves this index in the context of the given resolved owner.
	 */
	public MOIndex resolve(MOStructure owner);
}
