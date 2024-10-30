/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.attr;

import java.sql.Blob;
import java.sql.Clob;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.LongID;
import com.top_logic.basic.StringID;
import com.top_logic.basic.TLID;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.sql.DBType;
import com.top_logic.dob.AttributeStorage;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.attr.storage.BinaryAttributeStorage;
import com.top_logic.dob.attr.storage.MOAttributeStorageImpl;
import com.top_logic.dob.meta.AbstractMetaObject;
import com.top_logic.dob.meta.TypeContext;
import com.top_logic.dob.sql.DBMetaObject;

/**
 * A primitive type of a column in the database schema definition.
 * 
 * @author Klaus Halfmann / Karsten Buch / Marco Perra
 */
@Format(MOPrimitiveFormat.class)
public final class MOPrimitive extends AbstractMetaObject implements DBMetaObject {

    /** The default Size for SQLTypes (CHAR, VARCHAR). */
    public static final int DEFAULT_SQL_SIZE = 10;

    /** The default precision for SQLTypes (DECIMAL). */
    public static final int DEFAULT_SQL_PREC = 2;   // not known ...

    /**
     * Constant primitive for common, lightweight use.
     */
    public static final MOPrimitive STRING    =
		new MOPrimitive("String", DBType.STRING, 150, 0);

    /**
     * Constant primitive for common, lightweight use.
     *
     * The Mapping of Bit is DB Specific.
     * Your may wish to use BIT for some Databases.
     *
     */
    public static final MOPrimitive BOOLEAN   =
		new MOPrimitive("Boolean", DBType.BOOLEAN, 1, 0);

    /**
     * Constant primitive for common, lightweight use.
     */
    public static final MOPrimitive INTEGER   =
		new MOPrimitive("Integer", DBType.INT, 12, 0);

    /**
     * Constant primitive for common, lightweight use.
     */
    public static final MOPrimitive SHORT     =
		new MOPrimitive("Short", DBType.SHORT);

    /**
     * Constant primitive for common, lightweight use.
     */
    public static final MOPrimitive LONG      =
		new MOPrimitive("Long", DBType.LONG, 20, 0);

    /**
     * Constant primitive for common, lightweight use.
     */
    public static final MOPrimitive TLID      =
		new MOPrimitive("ID", DBType.ID, IdentifierUtil.REFERENCE_DB_SIZE, 0);
    
    /**
     * Constant primitive for common, lightweight use.
     */
    public static final MOPrimitive BYTE      =
		new MOPrimitive("Byte", DBType.BYTE);

    /**
     * Constant primitive for common, lightweight use.
     */
    public static final MOPrimitive BLOB      =
		new MOPrimitive("Blob", DBType.BLOB, Integer.MAX_VALUE, 0);
	static {
		BLOB._defaultStorage = BinaryAttributeStorage.INSTANCE;
	}
    /**
     * Constant primitive for common, lightweight use.
     */
    public static final MOPrimitive CLOB    =
		new MOPrimitive("Clob", DBType.CLOB, Integer.MAX_VALUE, 0);

    /**
     * Constant primitive for common, lightweight use.
     */
    public static final MOPrimitive CHARACTER =
		new MOPrimitive("Character", DBType.CHAR, 1, 0);

    /**
     * Constant primitive for common, lightweight use.
     */
    public static final MOPrimitive VOID      =
		new MOPrimitive("Void");

    /**
     * Constant primitive for common, lightweight use.
     */
    public static final MOPrimitive FLOAT     =
		new MOPrimitive("Float", DBType.FLOAT);

    /**
     * Constant primitive for common, lightweight use.
     */
    public static final MOPrimitive DOUBLE    =
		new MOPrimitive("Double", DBType.DOUBLE);
    
    /**
     * Constant primitive for common, lightweight use.
     */
    public static final MOPrimitive DATE    =
		new MOPrimitive("Timestamp", DBType.DATETIME);
	
    /**
     * Constant primitive for common, lightweight use.
     */
    public static final MOPrimitive SQL_DATE    =
		new MOPrimitive("Day", DBType.DATE);

    /**
     * Constant primitive for common, lightweight use.
     */
    public static final MOPrimitive SQL_TIME    =
		new MOPrimitive("Time", DBType.TIME);

    /**
     * Constant primitive for common, lightweight use.
     */
    public static final MOPrimitive SQL_TIMESTAMP  = DATE;
    
    static {
    	final HashSet<MOPrimitive> dateCompatibleSet = CollectionUtil.newSet(3); 
    	dateCompatibleSet.add(DATE);
    	dateCompatibleSet.add(SQL_DATE);
    	dateCompatibleSet.add(SQL_TIME);
    	DATE.supertypes = dateCompatibleSet;
    	SQL_DATE.supertypes = dateCompatibleSet;
    	SQL_TIME.supertypes = dateCompatibleSet;
    	
    	STRING.supertypes = Collections.singleton(CLOB);
    	
    	INTEGER.supertypes = Collections.singleton(LONG);
    }

    /**
     * Set of all the primitive types for lookups.
     */
	public static final Map<String, MOPrimitive> PRIMITIVES;

	/** The default column type for this primitive of this type. */
	private final DBType _defaultSQLType;

    /** The default SQL Size for a primitive of this type. */
	private final int _defaultSQLSize;

    /** The default SQL Precision for a primitive of this type. */
	private final int _defaultSQLPrec;

	/** @see #getDefaultStorage() */
	private AttributeStorage _defaultStorage = MOAttributeStorageImpl.INSTANCE;
    
	/**
	 * The set of {@link MetaObject} this {@link MOPrimitive} is a subtype of.
	 * 
	 * @see #isSubtypeOf(MetaObject)
	 */
	private Set<? extends MetaObject> supertypes = Collections.emptySet();

    static {
		Map<String, MOPrimitive> types = new HashMap<>(53);
		enter(types, STRING, String.class);
		enter(types, BOOLEAN, "boolean", Boolean.class);
		enter(types, INTEGER, "int", Integer.class);
		enter(types, SHORT, "short", Short.class);
		enter(types, LONG, "long", Long.class, NextCommitNumberFuture.class);
		enter(types, TLID, "id", TLID.class, LongID.class, StringID.class);
		enter(types, BYTE, "byte", Byte.class);
		enter(types, BLOB, "blob", Blob.class);
		enter(types, CLOB, "clob", Clob.class);
		enter(types, CHARACTER, "char", Character.class);
		enter(types, VOID, "void", Void.class);
		enter(types, FLOAT, "float", Float.class);
		enter(types, DOUBLE, "double", Double.class, java.math.BigDecimal.class);
		enter(types, DATE, "Date", Date.class, java.sql.Timestamp.class);
		enter(types, SQL_DATE, java.sql.Date.class);
		enter(types, SQL_TIME, java.sql.Time.class);

		PRIMITIVES = Collections.unmodifiableMap(types);
    }
    
	private static void enter(Map<String, MOPrimitive> types, MOPrimitive type) {
		enterDirect(types, type, type.getName());
	}

	private static void enter(Map<String, MOPrimitive> types, MOPrimitive type, Class<?> alternativeName) {
		enter(types, type);
		enterDirect(types, type, alternativeName.getName());
	}

	private static void enter(Map<String, MOPrimitive> types, MOPrimitive type, String alternativeName,
			Class<?>... classNames) {
		enter(types, type);
		for (Class<?> className : classNames) {
			enterDirect(types, type, className.getName());
		}
		enterDirect(types, type, alternativeName);
	}

	private static void enterDirect(Map<String, MOPrimitive> types, MOPrimitive type, String alternativeName) {
		types.put(alternativeName, type);
	}

    /** Pseudo CTor to construct a primitive from a given (Class-) name
     *
     * @return null when no such MOPrimitive exists.
     */
    public static MOPrimitive getPrimitive(String name) {
		return PRIMITIVES.get(name);
    }

	/**
	 * All known {@link MOPrimitive} types.
	 */
	public static Collection<MOPrimitive> getAllPrimitives() {
		return new HashSet<>(PRIMITIVES.values());
	}

	/** Pseudo CTor to construct a primitive from a given Class.
	 *
	 * @return null when no such MOPrimitive exists.
	 */
	public static MOPrimitive getPrimitive(Class<?> primitive) {
	    return getPrimitive(primitive.getName());
	}

    /**
	 * Delivers the description of the instance of this class.
	 *
	 * @param name
	 *        The canonical name of the primitive type.
	 */
	protected MOPrimitive(String name) {
		this(name, DBType.STRING);
    }

    /**
	 * Delivers the description of the instance of this class.
	 *
	 * @param name
	 *        The canonical name of the primitive type.
	 * @param sqlType
	 *        See {@link #getDefaultSQLType()}.
	 */
	protected MOPrimitive(String name, DBType sqlType) {
		this(name, sqlType, DEFAULT_SQL_SIZE, DEFAULT_SQL_PREC);
    }

    /**
	 * Delivers the description of the instance of this class.
	 *
	 * @param name
	 *        The canonical name of the primitive type.
	 * @param sqlType
	 *        See {@link #getDefaultSQLType()}.
	 * @param sqlSize
	 *        default Size for Primitives of this type
	 * @param sqlPrec
	 *        default Precision for Primitives of this type
	 */
	protected MOPrimitive(String name, DBType sqlType, int sqlSize, int sqlPrec) {
		super(name);
		_defaultSQLType = sqlType;
		_defaultSQLSize = sqlSize;
		_defaultSQLPrec = sqlPrec;
    }

    @Override
	public Kind getKind() {
    	return Kind.primitive;
    }
    
    @Override
	public DBType getDefaultSQLType()  {
		return _defaultSQLType;
    }
    
    /**
     * get the Size of the desired SQL field (eg. for VARCHAR)
     *<p>
     *  This can be used as default value for 
     *  {@link com.top_logic.dob.sql.DBAttribute#getSQLSize()}
     *</p>
     * @return 0 in case a Size is not required.
     */
    @Override
	public int getDefaultSQLSize() {
		return _defaultSQLSize;
    }

    /**
     * get the Precision of the desired SQL field (eg. for DECIMAL)
     *<p>
     *  This can be used as default value for 
     *  {@link com.top_logic.dob.sql.DBAttribute#getSQLPrecision()}
     *</p>
     * @return 0 in case a Precision is not required.
     */
    @Override
	public int getDefaultSQLPrecision()  {
		return _defaultSQLPrec;
    }

    @Override
    public boolean isSubtypeOf(MetaObject anObject) {
    	switch (anObject.getKind()) {
			case ANY:
				return true;
			case alternative:
				return isSpecialisationOf(anObject);
			default:
				if (anObject == this) {
					return true;
				}
				return supertypes.contains(anObject);
		}
    }
    
    @Override
	public MetaObject copy() {
    	throw new UnsupportedOperationException("Primitive types are singletons.");
    }
    
    @Override
	public MetaObject resolve(TypeContext context) throws DataObjectException {
    	return this;
    }

	/**
	 * Default {@link AttributeStorage} to use when storing attributes of this type.
	 */
	@FrameworkInternal
	public AttributeStorage getDefaultStorage() {
		return _defaultStorage;
	}
}
