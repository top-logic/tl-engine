/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.attr;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.sql.DBType;
import com.top_logic.dob.AttributeStorage;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.schema.config.PrimitiveAttributeConfig;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.dob.sql.DBMetaObject;
import com.top_logic.dob.util.MetaObjectUtils;


/** Straightforward implementation of MOAttribute.
 * 
 * @author 	Klaus HAlfmann / Marco Perra
 */
public class MOAttributeImpl extends AbstractDBAttribute {
        
	/** @see #getSQLType() */
	private DBType sqlType;

    /** The Size of an SQL type (eg. for VARCHAR)
     */
    private int sqlSize;

    /** The Precision of a SQL field (eg. for DECIMAL)
     */
    private int sqlPrecision;
    
    /**
     * @see #isBinary()
     */
    private boolean binary;

	private AttributeStorage _storage;
    

    /** Special CTor to initalize the DBAttribute Value, too
     *
     * @param aName	        name of the Attribute
     * @param aDbName       Name suiteable as ColumnName for Database.
     * @param aType	        type of the Attribute	
     * @param mandatory     designates an Attribute that must be set
     * @param immutable     designates an Attribute that can be set only once
     * @param aSqlType      {@link DBType} of the corresponding database column.
     * @param aSqlSize      the Size of sn SQL type (eg. for VARCHAR)
     * @param aSqlPrecision  the Precision of a SQL field (eg. for DECIMAL) 
     */     
	public MOAttributeImpl(String aName, String aDbName, MetaObject aType, boolean mandatory, boolean immutable,
			DBType aSqlType, int aSqlSize, int aSqlPrecision)
    {
		super(aName, aType, aDbName);

		initAttributeStorage();
        setMandatory(mandatory);
        setImmutable(immutable);
        this.sqlType      = aSqlType;
        this.sqlSize      = aSqlSize;
        this.sqlPrecision = aSqlPrecision;
    }

	private void initAttributeStorage() {
		setStorage(MetaObjectUtils.getDefaultStorage(getMetaObject()));
	}

    /** Standdard CTor to initalize all the members.
     *
     * @param aName	      name of the Attribute
     * @param aType	      type of the Attribute	
     * @param mandatory   designates an Attribute that must be set
     * @param immutable   designates an Attribute that can be set only once
     */
    public MOAttributeImpl (String aName, MetaObject aType,
                            boolean mandatory, boolean immutable)
    {
		super(aName, aType, null);

		initAttributeStorage();
        setMandatory(mandatory);
        setImmutable(immutable);
        
        initSqlAnnotations(aType);
    }

	private void initSqlAnnotations(MetaObject aType) {
		if (aType instanceof DBMetaObject) {
            DBMetaObject dbMeta = ((DBMetaObject) aType);
            
            this.sqlType      = dbMeta.getDefaultSQLType();
            this.sqlSize      = dbMeta.getDefaultSQLSize();
            this.sqlPrecision = dbMeta.getDefaultSQLPrecision();
        } else {
			this.sqlType = DBType.STRING;
        	this.sqlSize      = 0;
        	this.sqlPrecision = 0;
        }
		this.binary = false;
	}

    /** Standdard CTor to initalize most of the members.
     *
     * @param aName	            name of the Attribute
     * @param aType	            type of the Attribute	
     * @param aMandatoryFlag    designates an Attribute that must be set
     */
    public MOAttributeImpl (String aName, MetaObject aType,
                            boolean aMandatoryFlag) {
        this (aName, aType, aMandatoryFlag, !IMMUTABLE);
    }

    /** CTor to create a !mandatory object. 
     *
     * @param aName	name of the Attribute
     * @param aType	type of the Attribute	
     */
    public MOAttributeImpl (String aName, MetaObject aType) {
        this (aName, aType, !MANDATORY,  !IMMUTABLE);
    }

	/**
	 * Creates a {@link MOAttributeImpl}.
	 * 
	 * @param aName
	 *        See {@link #getName()}
	 * @param aDbName
	 *        See {@link #getDBName()}
	 * @param mandatory
	 *        See {@link #isMandatory()}
	 * @param immutable
	 *        See {@link #isImmutable()}
	 * @param storageType
	 *        The attribute that defines the {@link #getMetaObject()},
	 *        {@link #getSQLType()}, {@link #getSQLSize()},
	 *        {@link #getSQLPrecision()}, and {@link #isBinary()} properties.
	 */
	public MOAttributeImpl(String aName, String aDbName, boolean mandatory, boolean immutable, MOAttribute storageType) {
		super(aName, storageType.getMetaObject(), aDbName);

		setStorage(storageType.getStorage());
		setMandatory(mandatory);
		setImmutable(immutable);
		copySqlAnnotations(storageType);
	}
    
	public MOAttributeImpl(MOAttributeImpl original) {
		this(original.getName(), original.getDBName(), original.isMandatory(), original.isImmutable(), original);
	}

	private void copySqlAnnotations(MOAttribute anAttr) {
		if (anAttr instanceof DBAttribute) {
			DBAttribute dbMeta = ((DBAttribute) anAttr);
            
            this.sqlType      = dbMeta.getSQLType();
            this.sqlSize      = dbMeta.getSQLSize();
            this.sqlPrecision = dbMeta.getSQLPrecision();
            this.binary       = dbMeta.isBinary();
        } else {
			this.sqlType = DBType.STRING;
        	this.sqlSize      = 0;
        	this.sqlPrecision = 0;
        	this.binary       = false;
        }
	}

	/**
	 * Creates a new {@link MOAttributeImpl} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link MOAttributeImpl}.
	 */
	public MOAttributeImpl(InstantiationContext context, PrimitiveAttributeConfig config) {
		super(context, config);
		if (config.getStorage() != null) {
			setStorage(config.getStorage());
		} else {
			initAttributeStorage();
		}
		initSqlAnnotations(getMetaObject());
		if (config.getDBType() != null) {
			setSQLType(config.getDBType());
		}
		if (config.getDBSize() != null) {
			setSQLSize(config.getDBSize().intValue());
		}
		if (config.getDBPrecision() != null) {
			setSQLPrecision(config.getDBPrecision().intValue());
		}
		if (config.isBinary() != null) {
			setBinary(config.isBinary().booleanValue());
		}
	}

    /**
	 * Type of the corresponding database column.
	 */
    @Override
	public DBType getSQLType() {
        return sqlType;
    }
    
    /**
     * get the Size of the desired SQL field (eg. for VARCHAR)
     *
     * @return 0 in case a Size is not required.
     */
    @Override
	public int getSQLSize() {
        return sqlSize;
    }

    /**
     * get the Precision of the desired SQL field (eg. for DECIMAL)
     *
     * @return 0 in case a Precision is not required.
     */
    @Override
	public int getSQLPrecision() {
        return sqlPrecision;
    }

    @Override
	public boolean isBinary() {
    	return binary;
    }
    
    /**
	 * @see #getSQLType()
	 */
	public void setSQLType(DBType aType) {
        sqlType = aType;
    }
    
    /**
     * get the Size of the desired SQL field (eg. for VARCHAR)
     *
     * @param aSize 0 in case a Size is not required or known.
     */
    public void setSQLSize(int aSize) {
        sqlSize = aSize;
    }

    /**
     * get the Precision of the desired SQL field (eg. for DECIMAL)
     *
     * @param aPrecision 0 in case a Precision is not required.
     */
    public void setSQLPrecision(int aPrecision) {
        sqlPrecision = aPrecision;
    }

    /**
     * Set the {@link #isBinary()} flag to the given value.
     */
    public void setBinary(boolean value) {
		checkUpdate();
		this.binary = value;
	}
    
    @Override
	public MOAttribute copy() {
		MOAttributeImpl copy = new MOAttributeImpl(getName(), typeRef(getMetaObject()));
		copy.initFrom(this);
		copy.setStorage(getStorage());
		return copy;
    }
    
    protected final void initFrom(MOAttributeImpl orig) {
    	super.initFrom(orig);
    	this.binary = orig.binary;
    	this.sqlPrecision = orig.sqlPrecision;
    	this.sqlSize = orig.sqlSize;
    	this.sqlType = orig.sqlType;
	}

    /** Pseudo CTor to create a mandatory Attribute from a given Name and Class.
     *<p>
     * As of now this will work with primitive types and java.util.Date only.
     *</p>
     * @param aName     name of the Attribute
     * @param aClass    template arbitrary object to create an matching Attribute for.
     *
     * @return an attribute implementation with given name and type.
     */
    public static MOAttributeImpl makeAttribute(String aName, Class aClass) {
        // Try to create a primitve first ...
        MetaObject theType = MOPrimitive.getPrimitive(aClass);

        // Nothing else is supported (yet)
        if (theType == null) {
            throw new IllegalArgumentException("Type '" + aClass.getName() + "' not supported.");
        }

        return new MOAttributeImpl(aName, theType, MANDATORY);
    }

    /** Pseudo CTor to create an Attribute from a given Name and Object.
     *<p>
     * As of now this will work with primitive types and java.util.Date only.
     *</p>
     * @param aName     name of the Attribute
     * @param aTemplate arbitrary object to create an matching Attribute for.   
     *
     * @return an attribute implementation with given name and type of the given template value.
     */
    public static MOAttributeImpl makeAttribute(String aName, Object aTemplate) {
        return makeAttribute(aName, aTemplate.getClass());
    }
    
	@Override
	public AttributeStorage getStorage() {
		return _storage;
	}

	@Override
	public void setStorage(AttributeStorage storage) {
		_storage = storage;
	}

}
