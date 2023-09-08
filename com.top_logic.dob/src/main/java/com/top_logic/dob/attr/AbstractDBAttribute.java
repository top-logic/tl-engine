/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.attr;


import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.sql.SQLH;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.schema.config.PrimitiveAttributeConfig;
import com.top_logic.dob.sql.DBAttribute;

/**
 * Base class for {@link MOAttribute} and {@link DBAttribute} implementations.
 * 
 * @author    <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
 */
public abstract class AbstractDBAttribute extends AbstractMOAttribute implements DBAttribute {

	private String dbName;
	private int dbIndex = -1;
	private final DBAttribute[] dbMapping;

	/**
	 * Creates a {@link AbstractDBAttribute}.
	 * 
	 * @param name
	 *        See {@link #getName()}
	 * @param type
	 *        See {@link #getMetaObject()}
	 * @param dbName
	 *        See {@link #getDBName()}
	 */
	public AbstractDBAttribute(String name, MetaObject type, String dbName) {
		super(name, type);
		this.dbName = dbName;
		this.dbMapping = new DBAttribute[] { this };
	}
	
	/**
	 * Creates a new {@link AbstractDBAttribute} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link AbstractDBAttribute}.
	 */
	public AbstractDBAttribute(InstantiationContext context, PrimitiveAttributeConfig config) {
		super(context, config);
		setMetaObject(config.getValueType());
		this.dbMapping = new DBAttribute[] { this };
		setDBName(config.getDBNameEffective());
	}

	@Override
	public DBAttribute[] getDbMapping() {
		return dbMapping;
	}

	@Override
	public String getDBName () {
        if (dbName == null) {
            dbName = SQLH.mangleDBName(getName());
        }
        return dbName;
    }
    
    /**
     * @see #getDBName()
     */
    public void setDBName(String dbName) {
    	checkUpdate();
		this.dbName = dbName;
	}

	@Override
	public int getDBColumnIndex() {
		if (this.dbIndex == -1) {
			throw new IllegalStateException("DB index was not yet initialized.");
		}
		return this.dbIndex;
	}

	@Override
	public void initDBColumnIndex(int index) {
		checkUpdate();
		if (this.dbIndex != -1) {
			throw new IllegalStateException("This attribute '" + getName() + "' already has a dbColumnIndex '" + this.dbIndex + "'.");
		}
		this.dbIndex = index;
	}
	
	protected final void initFrom(AbstractDBAttribute orig) {
		super.initFrom(orig);
		
		this.dbName = orig.dbName;
	}
	
	public int getDBSize() {
		return 1;
	}

	@Override
	public MOAttribute getAttribute() {
		return this;
	}

}
