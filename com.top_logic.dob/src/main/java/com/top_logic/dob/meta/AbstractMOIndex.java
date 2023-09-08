/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.meta;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.sql.SQLH;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.dob.sql.DBIndex;

/**
 * Base class for {@link DBIndex} implementations.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractMOIndex extends MOPartImpl implements DBIndex {

    /** A unique index will contain only one element per key. */
    private boolean unique;

    /** Name suitable as ColumnName for Database. */
    private String dbName;

    /** create the index not in the db but in Memory only. */
    private boolean inMemory;

    /** Hint for the database to compress the primary key. */
	private int compress = NO_COMPRESS;
    
	private List<MOAttribute> attributes;

	/**
	 * @see #isCustom()
	 */
	private final boolean _custom;

	/**
	 * Creates a {@link AbstractMOIndex}.
	 * 
	 * @param name
	 *        See {@link #getName()}.
	 * @param dbName
	 *        See {@link #getDBName()}.
	 * @param unique
	 *        See {@link #isUnique()}.
	 * @param custom
	 *        See {@link #isCustom()}.
	 * @param inMemory
	 *        See {@link #isInMemory()}.
	 * @param compress
	 *        See {@link #getCompress()}.
	 */
	public AbstractMOIndex(String name, String dbName, boolean unique, boolean custom, boolean inMemory, int compress) {
		super(name);
		
        this.dbName       = dbName;
        this.unique       = unique;
        this.inMemory     = inMemory;
        this.compress     = compress;
		_custom = custom;
	}

    @Override
	public boolean isUnique() {
        return unique;
    }
   
    @Override
	public boolean isCustom() {
		return _custom;
	}

	@Override
	public String getDBName () {
        if (dbName == null) {
            dbName = SQLH.mangleDBName(getName());
        }
        return dbName;
    }
    
    @Override
	public boolean isInMemory() {
        return inMemory;
    }
    
	@Override
	public int getCompress() {
	    return compress;
	}
	
	@Override
	public List<MOAttribute> getAttributes() {
		if (isFrozen()) {
			return attributes;
		} else {
			return computeAttributes();
		}
	}
	
	private List<MOAttribute> computeAttributes() {
		List<MOAttribute> result = new ArrayList<>();
		for (DBAttribute column: getKeyAttributes()) {
			MOAttribute attribute = column.getAttribute();
			if (!result.contains(attribute)) {
				result.add(attribute);
			}
		}
		return result;
	}

	@Override
	protected void afterFreeze() {
		super.afterFreeze();
		attributes = computeAttributes();
	}

}
