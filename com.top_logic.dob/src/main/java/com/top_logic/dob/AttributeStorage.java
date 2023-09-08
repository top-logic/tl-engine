/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.dob.meta.ObjectContext;
import com.top_logic.dob.sql.DBAttribute;

/**
 * Storage strategy of a {@link MOAttribute}.
 * 
 * <p>
 * The application value of an attribute (the value returned from
 * {@link DataObject#getValue(MOAttribute)}) is internally stored in potentially multiple slots of
 * an object's storage array. When setting a value into an object's attribute, the following
 * sequence happens:
 * </p>
 * 
 * <ol>
 * <li>
 * The application value is converted into a cache value using
 * {@link #fromApplicationToCacheValue(MOAttribute, Object)}. This may convert the attribute value
 * as seen by the application into an internal value suitable for temporary caching. The cache value
 * may be identical to the application value. But the cache value may drop some aspect from the
 * application value, e.g. to enable garbage collecting the application value, while the identifier
 * necessary to retrieve the application value is still in the cache.</li>
 * 
 * <li>
 * The cache value then is stored into the object's internal storage array using the method
 * {@link #setCacheValue(MOAttribute, DataObject, Object[], Object)}. The first index used for
 * storage in the given storage array is determined by the given attribute. The number of slots used
 * (if any) is determined by the {@link AttributeStorage} implementation.</li>
 * 
 * <li>
 * When the object is stored to persistent storage, the object's internal storage array is
 * transformed into a prepared statement's parameter array using the method
 * {@link #storeValue(ConnectionPool, Object[], int, MOAttribute, DataObject, Object[], long)}. This
 * method reads the cache from the given object's storage array and transforms it into primitive
 * values compatible with JDBC.</li>
 * 
 * </ol>
 * 
 * <p>
 * The number of slots in an object's storage array is determined by the sum of all
 * {@link #getCacheSize()} values of all attributes declared by the object's type.
 * </p>
 * 
 * <p>
 * The number of database columns required to store versions of objects of a certain type is
 * determined by the sum of the sizes of of all {@link MOAttribute#getDbMapping()} arrays of all
 * attributes declared by the object's type. Their JDBC types is determined by
 * {@link DBAttribute#getSQLType()} of the corresponding {@link DBAttribute}s in the attribute's
 * {@link MOAttribute#getDbMapping()}.
 * </p>
 * 
 * @see MOAttribute#getStorage()
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface AttributeStorage {

	/** Constant to call methods if no ObjectContext is available. */
	public static final ObjectContext NO_CONTEXT = null;

	/**
	 * Returns the value of the given attribute in the given storage.
	 * 
	 * @param attribute
	 *        The {@link MOAttribute} to get value for.
	 * @param item
	 *        The object to get cache value from.
	 * @param storage
	 *        Must not be <code>null</code>. Moreover it must be the storage of an object which has
	 *        the owner of the given {@link MOAttribute} as type.
	 * 
	 * @return The value of the given attribute in the given storage.
	 * 
	 * @see AttributeStorage#setCacheValue(MOAttribute, DataObject, Object[], Object)
	 * @see AttributeStorage#getApplicationValue(MOAttribute, DataObject, ObjectContext, Object[])
	 */
	Object getCacheValue(MOAttribute attribute, DataObject item, Object[] storage);

	/**
	 * Sets the new value of the given attribute in the given storage.
	 * 
	 * @param attribute
	 *        The {@link MOAttribute} to set value for.
	 * @param item
	 *        The item to set cache value into.
	 * @param storage
	 *        Must not be <code>null</code>. Moreover it must be the storage of an object which has
	 *        the owner of the given {@link MOAttribute} as type.
	 * @return The old value of the given attribute in the given storage.
	 * 
	 * @see AttributeStorage#getCacheValue(MOAttribute, DataObject, Object[])
	 * @see AttributeStorage#setApplicationValue(MOAttribute, DataObject, ObjectContext, Object[],
	 *      Object)
	 * @see AttributeStorage#initCacheValue(MOAttribute, DataObject, Object[], Object)
	 */
	Object setCacheValue(MOAttribute attribute, DataObject item, Object[] storage, Object cacheValue);

	/**
	 * Initialises the value of the given attribute in the given storage.
	 * 
	 * <p>
	 * In contrast to {@link AttributeStorage#setCacheValue(MOAttribute, DataObject, Object[], Object)} it is
	 * expected that the value for the item was never set before.
	 * </p>
	 * 
	 * @param attribute
	 *        The {@link MOAttribute} to initialise value for.
	 * @param item
	 *        The item to initialise cache value for.
	 * @param storage
	 *        Must not be <code>null</code>. Moreover it must be the storage of an object which has
	 *        the owner of the given {@link MOAttribute} as type.
	 * 
	 * @see AttributeStorage#setCacheValue(MOAttribute, DataObject, Object[], Object)
	 */
	void initCacheValue(MOAttribute attribute, DataObject item, Object[] storage, Object cacheValue);

	/**
	 * Translates the given cache value to an application value.
	 * 
	 * @param attribute
	 *        The attribute the cache value belongs to.
	 * @param context
	 *        The context object used to translate the given cache value to an application value.
	 * @param cacheValue
	 *        A cache value.
	 * 
	 * @return The corresponding application value
	 * 
	 * @see AttributeStorage#fromApplicationToCacheValue(MOAttribute, Object)
	 */
	Object fromCacheToApplicationValue(MOAttribute attribute, ObjectContext context, Object cacheValue);

	/**
	 * Translates the given application value to a cache value.
	 * 
	 * @param attribute
	 *        The {@link MOAttribute} to create a cache value for.
	 * @param applicationValue
	 *        The value to translate.
	 * 
	 * @return The cache version of the given application value.
	 * 
	 * @see AttributeStorage#fromCacheToApplicationValue(MOAttribute, ObjectContext, Object)
	 */
	Object fromApplicationToCacheValue(MOAttribute attribute, Object applicationValue);

	/**
	 * Reads the cache value of the given {@link MOAttribute} from a database result and returns it.
	 * 
	 * @param dbResult
	 *        The result to create value from.
	 * @param resultOffset
	 *        For determination of the first point in the {@link ResultSet} which provides
	 *        information for the {@link MOStructure}, the given attribute belongs to.
	 * @param attribute
	 *        The attribute to fetch value for.
	 * @param context
	 *        The data object for which the value of the given {@link MOAttribute} is read.
	 * 
	 * @return The internal cache value of the given {@link MOAttribute} in the database. To get the
	 *         application value you must call
	 *         {@link AttributeStorage#fromCacheToApplicationValue(MOAttribute, ObjectContext, Object)}
	 *         .
	 * 
	 * @throws SQLException
	 *         When accessing the {@link ResultSet} fails.
	 */
	Object fetchValue(DBHelper sqlDialect, ResultSet dbResult, int resultOffset, MOAttribute attribute,
			ObjectContext context) throws SQLException;
	
	/**
	 * Reads the cache value of the given {@link MOAttribute} from a database result for the given
	 * {@link DataObject} and stores it in the cache.
	 * 
	 * @param pool
	 *        Database which was used to create the given result.
	 * @param dbResult
	 *        The result to create value from.
	 * @param resultOffset
	 *        For determination of the first point in the {@link ResultSet} which provides
	 *        information for the {@link MOStructure}, the given attribute belongs to.
	 * @param attribute
	 *        The attribute to fetch value for.
	 * @param context
	 *        The data object for which the value of the given {@link MOAttribute} is read.
	 * 
	 * @throws SQLException
	 *         When accessing the {@link ResultSet} fails.
	 */
	void loadValue(ConnectionPool pool, ResultSet dbResult, int resultOffset, MOAttribute attribute, DataObject item,
			Object[] storage, ObjectContext context) throws SQLException;

	/**
	 * Stores the value of the given {@link MOAttribute} in the given storage into the array that
	 * serves as input for a {@link PreparedStatement}.
	 * 
	 * @param pool
	 *        Database in which the {@link PreparedStatement} will be executed.
	 * @param stmtArgs
	 *        The array to fill the value in.
	 * @param stmtOffset
	 *        Index in the prepared statements parameter array (zero-based), where the first column
	 *        of this type is filled in.
	 * @param attribute
	 *        The {@link MOAttribute} to store value for.
	 * @param item
	 *        The item holding the given storage.
	 * @param storage
	 *        The storage to receive cache value from.
	 * @param currentCommitNumber
	 *        The commit number of the current transaction.
	 */
	void storeValue(ConnectionPool pool, Object[] stmtArgs, int stmtOffset, MOAttribute attribute,
			DataObject item, Object[] storage, long currentCommitNumber) throws SQLException;

	/**
	 * Sets the given application value as value for the given {@link MOAttribute} into the given
	 * storage.
	 * 
	 * @param attribute
	 *        The {@link MOAttribute} to set application value for.
	 * @param item
	 *        The item to set application value to.
	 * @param context
	 *        The context to get information from.
	 * @param storage
	 *        The cache to store the data into.
	 * @param applicationValue
	 *        The application value to store into the cache.
	 * @return The old application value
	 * 
	 * @see AttributeStorage#getApplicationValue(MOAttribute, DataObject, ObjectContext, Object[])
	 * @see AttributeStorage#setCacheValue(MOAttribute, DataObject, Object[], Object)
	 * @see AttributeStorage#initApplicationValue(MOAttribute, DataObject, ObjectContext, Object[],
	 *      Object)
	 */
	Object setApplicationValue(MOAttribute attribute, DataObject item, ObjectContext context, Object[] storage,
			Object applicationValue);

	/**
	 * Initialises the given application value as value for the given {@link MOAttribute} into the
	 * given storage.
	 * 
	 * <p>
	 * In contrast to
	 * {@link AttributeStorage#setApplicationValue(MOAttribute, DataObject, ObjectContext, Object[], Object)} it
	 * is expected that no value for the given attribute was set before.
	 * </p>
	 * 
	 * @param attribute
	 *        The {@link MOAttribute} to initialise application value for.
	 * @param item
	 *        The item to initialise application value for.
	 * @param context
	 *        The context to get information from.
	 * @param storage
	 *        The cache to store the data into.
	 * @param applicationValue
	 *        The application value to store into the cache.
	 * 
	 * @see AttributeStorage#setApplicationValue(MOAttribute, DataObject, ObjectContext, Object[], Object)
	 */
	void initApplicationValue(MOAttribute attribute, DataObject item, ObjectContext context, Object[] storage,
			Object applicationValue);

	/**
	 * Reads the value of the given attribute from the storage and returns the application version
	 * of it.
	 * 
	 * @param attribute
	 *        The {@link MOAttribute} to read application value for.
	 * @param item
	 *        The item to get application value from.
	 * @param context
	 *        The context in which the application value is build.
	 * @param storage
	 *        The storage to get data from.
	 * 
	 * @return The application value of the attribute.
	 * 
	 * @see AttributeStorage#setApplicationValue(MOAttribute, DataObject, ObjectContext, Object[], Object)
	 * @see AttributeStorage#getCacheValue(MOAttribute, DataObject, Object[])
	 */
	Object getApplicationValue(MOAttribute attribute, DataObject item, ObjectContext context, Object[] storage);

	/**
	 * Checks whether the given value is a valid value for the given attribute in the given
	 * {@link DataObject}.
	 * 
	 * @param attribute
	 *        The attribute to check validity for.
	 * @param data
	 *        The {@link DataObject} for which the value of this attribute shall be set.
	 * @param value
	 *        The potential value of this attribute in the given {@link DataObject}.
	 * 
	 * @throws DataObjectException
	 *         iff the given value is not valid as value of this {@link MOAttribute} in the given
	 *         {@link DataObject}.
	 */
	void checkAttributeValue(MOAttribute attribute, DataObject data, Object value) throws DataObjectException;

	/**
	 * Checks whether the given values are the equal with respect to the given {@link MOAttribute}.
	 * 
	 * <p>
	 * The given values are application values.
	 * </p>
	 * 
	 * @param attribute
	 *        The attribute to check equality for.
	 * @param val1
	 *        The first value. May be <code>null</code>.
	 * @param val2
	 *        The second value. May be <code>null</code>.
	 * 
	 * @return Whether the given values are equal with respect to the given {@link MOAttribute
	 *         attribute}.
	 */
	boolean sameValue(MOAttribute attribute, Object val1, Object val2);

	/**
	 * The number of slots in the cache used by this storage strategy.
	 */
	int getCacheSize();

	/**
	 * Whether setting values is not supported.
	 */
	default boolean isDerived() {
		return false;
	}

}
