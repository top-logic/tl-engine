/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.attr.storage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.TLID;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.DBType;
import com.top_logic.dob.AttributeStorage;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.data.DOList;
import com.top_logic.dob.ex.IncompatibleTypeException;
import com.top_logic.dob.meta.AbstractMetaObject;
import com.top_logic.dob.meta.MOCollection;
import com.top_logic.dob.sql.DBAttribute;

/**
 * Abstract implementation of {@link AttributeStorage} just holding service methods.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractAttributeStorage implements AttributeStorage {

	/**
	 * Cache object to indicate that the cache value is invalid.
	 * 
	 * <p>
	 * Such an object is stored in cache, e.g. when errors occur during transforming value from
	 * database to acceptable cache value. When in contrast an exception is thrown at that method,
	 * the corresponding object would not be created, so it would be impossible to fix the problem
	 * from within application.
	 * </p>
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class InvalidCacheValue {

		final RuntimeException _ex;

		/**
		 * Creates a new {@link InvalidCacheValue} due to given problem.
		 * 
		 * @param ex
		 *        The problem causes the code to create this {@link InvalidCacheValue}
		 */
		private InvalidCacheValue(RuntimeException ex) {
			if (ex == null) {
				throw new NullPointerException("'ex' must not be 'null'.");
			}
			_ex = ex;
		}

		/**
		 * Constructs an {@link InvalidCacheValue} and directly logs the error message.
		 */
		public static Object error(String message) {
			return error(message, null);
		}

		/**
		 * Constructs an {@link InvalidCacheValue} and directly logs the error message.
		 */
		public static Object error(String message, Exception ex) {
			Logger.error(message, ConfigurationAttributeStorage.class);
			return new InvalidCacheValue(new ConfigurationError(message, ex));
		}

	}

	/**
	 * Checks that the value is not empty or <code>null</code> when the attribute is "not null".
	 * 
	 * @throws SQLException
	 *         if the value is empty or <code>null</code> and the attribute is "not null".
	 */
	protected static void checkMandatory(DBAttribute attribute, DataObject item, Object value) throws SQLException {
		if (!attribute.isSQLNotNull()) {
			return;
		}
		/* In some DBMS (e.g. ORACLE) an empty string is treated as null, therefore empty strings
		 * must also be omitted. */
		boolean emptyValue = StringServices.isEmpty(value);
		if (emptyValue) {
			throw failNoNotEmptyValueForNotNullAttribute(item, attribute);
		}
	}

	private static SQLException failNoNotEmptyValueForNotNullAttribute(DataObject item, DBAttribute attribute)
			throws SQLException {
		StringBuilder message = new StringBuilder();
		message.append("Value for not null colummn '");
		message.append(attribute.getDBName());
		message.append("' of attribute '");
		message.append(attribute.getAttribute());
		message.append("' of type '");
		message.append(item.tTable().getName());
		message.append("' for object '");
		message.append(item);
		message.append("' can not be set to null or empty.");
		throw new SQLException(message.toString());
	}

	/**
	 * Checks whether the given value is correct for the given attribute.
	 * 
	 * <p>
	 * The check contains "default" checks, as check for correct type, and immutable.
	 * </p>
	 */
	protected void defaultCheck(MOAttribute attribute, DataObject data, Object value)
			throws IncompatibleTypeException, DataObjectException {
		MetaObject attributeType = attribute.getMetaObject();
		MetaObject valueType = AbstractMetaObject.typeOfValue(value);

		if (valueType == null) {
			if (value instanceof Collection) {
				if (value instanceof DOList) {
					valueType = ((DOList) value).tTable();
				} else {
					if (attributeType.getKind() != MetaObject.Kind.collection) {
						throw new IncompatibleTypeException("Not a collection type attribute '" + attribute + "'.");
					}

					MetaObject elementType = ((MOCollection) attributeType).getElementType();
					for (Object o : (Collection<?>) value) {
						MetaObject contentType = AbstractMetaObject.typeOfValue(o);
						if (contentType == null) {
							StringBuilder noContentType = new StringBuilder();
							noContentType.append("Can not determine type for value ");
							noContentType.append(o);
							noContentType.append(". No type is not compatible to collection element type '");
							noContentType.append(elementType.getName());
							noContentType.append("' of attribute '");
							appendAttribute(noContentType, attribute);
							noContentType.append("'.");
							throw new IncompatibleTypeException(noContentType.toString());
						}
						if (!contentType.isSubtypeOf(elementType)) {
							StringBuilder illegalContentType = new StringBuilder();
							illegalContentType.append("Element types not compatible, expected '");
							illegalContentType.append(elementType.getName());
							illegalContentType.append("', got '");
							illegalContentType.append(contentType.getName());
							illegalContentType.append("' of value '");
							illegalContentType.append(o);
							illegalContentType.append("' for attribute '");
							appendAttribute(illegalContentType, attribute);
							illegalContentType.append("'.");
							throw new IncompatibleTypeException(illegalContentType.toString());
						}
					}

					return;
				}
			} else if (value == null) {
				// Null values are considered separately below.
			} else {
				StringBuilder noContentType = new StringBuilder();
				noContentType.append("Cannot determine value type for '");
				noContentType.append(value.getClass().getName());
				noContentType.append("' for attribute '");
				appendAttribute(noContentType, attribute);
				noContentType.append("'.");
				throw new IncompatibleTypeException(noContentType.toString());
			}
		}

		if (value == null || valueType.isSubtypeOf(attributeType)) {
			checkImmutable(attribute, data, value);

			if (value != null) {
				if (valueType == MOPrimitive.STRING && attribute instanceof DBAttribute) {
					int dbLen = ((DBAttribute) attribute).getSQLSize();
					String dataString = (String) value;
					int slen = dataString.length();

					if (dbLen < slen) {
						StringBuilder sizeTooLong = new StringBuilder();
						appendAttribute(sizeTooLong, attribute);
						sizeTooLong.append(", Length of given string (");
						sizeTooLong.append(slen);
						sizeTooLong.append(") is longer than ");
						sizeTooLong.append(dbLen);
						sizeTooLong.append(", will eventually be truncated. value: ");
						if (dataString.length() > 255) {
							sizeTooLong.append(dataString.substring(0, 255));
							sizeTooLong.append("...");
						} else {
							sizeTooLong.append(dataString);
						}
						sizeTooLong.append(" Object: ");
						sizeTooLong.append(data);
						Logger.warn(sizeTooLong.toString(),
							AbstractMOAttributeStorageImpl.class);
					}
				}
			}
			return;
		} else {
			StringBuilder incompatibleType = new StringBuilder();
			incompatibleType.append("Incompatible type '");
			incompatibleType.append(valueType.getName());
			incompatibleType.append("' for attribute '");
			appendAttribute(incompatibleType, attribute);
			incompatibleType.append("'");
			throw new IncompatibleTypeException(incompatibleType.toString());
		}
	}

	/**
	 * Appends a string representation for the given attribute for error messages.
	 */
	protected static StringBuilder appendAttribute(StringBuilder out, MOAttribute attribute) {
		return out.append(attribute.getOwner().getName()).append('.').append(attribute.getName()).append("::")
			.append(attribute.getMetaObject().getName());
	}

	/**
	 * Checks the immutable constraint is not violated.
	 * 
	 * @param attribute
	 *        The attribute to check value for.
	 * @param data
	 *        The object to set new data in.
	 * @param newValue
	 *        The potential new value.
	 * 
	 * @throws DataObjectException
	 *         iff attribute is immutable and there is already a non <code>null</code> value set.
	 */
	protected void checkImmutable(MOAttribute attribute, DataObject data, Object newValue) throws DataObjectException {
		if (attribute.isImmutable() && data.getValue(attribute) != null) {
			StringBuilder immutableAttribute = new StringBuilder();
			immutableAttribute.append("Attribute '");
			appendAttribute(immutableAttribute, attribute);
			immutableAttribute.append("' is immutable you cannot set it twice");
			throw new DataObjectException(immutableAttribute.toString());
		}
	}

	/**
	 * Stores the given value using declaration of the given {@link DBAttribute}.
	 * 
	 * @see #storeValue(ConnectionPool, Object[], int, MOAttribute, DataObject, Object[], long)
	 */
	protected void storeObject(DBAttribute dbAttribute, Object[] stmtArgs, int stmtOffset, DataObject item,
			Object value) throws SQLException {
		checkMandatory(dbAttribute, item, value);
		stmtArgs[stmtOffset + dbAttribute.getDBColumnIndex()] = value;
	}

	/**
	 * Stores the given {@link TLID} value using declaration of the given {@link DBAttribute}.
	 * 
	 * @see #storeValue(ConnectionPool, Object[], int, MOAttribute, DataObject, Object[], long)
	 */
	protected void storeTLID(DBAttribute dbAttribute, Object[] stmtArgs, int stmtOffset, DataObject item,
			TLID value) throws SQLException {
		checkMandatory(dbAttribute, item, value);
		stmtArgs[stmtOffset + dbAttribute.getDBColumnIndex()] = value;
	}

	/**
	 * Sets the value into the storage at the {@link MOAttribute#getCacheIndex()}
	 * 
	 * @param attribute
	 *        the attribute to which the cache belongs.
	 * @param storage
	 *        must not be <code>null</code>
	 * 
	 * @return the unfiltered old stored value
	 */
	protected final Object setSimpleCacheValue(MOAttribute attribute, Object[] storage, Object cacheValue) {
		Object oldValue = getSimpleCacheValue(attribute, storage);
		storage[attribute.getCacheIndex()] = cacheValue;
		return oldValue;
	}

	/**
	 * Returns the value in the storage at the {@link MOAttribute#getCacheIndex()}
	 * 
	 * @param attribute
	 *        the attribute to which the cache belongs.
	 * @param storage
	 *        must not be <code>null</code>
	 * 
	 * @return the unfiltered stored value
	 * 
	 * @throws IllegalStateException
	 *         in case the cache value is currently an {@link InvalidCacheValue}.
	 */
	protected final Object getSimpleCacheValue(MOAttribute attribute, Object[] storage) {
		Object cacheValue = storage[attribute.getCacheIndex()];
		if (cacheValue instanceof InvalidCacheValue) {
			StringBuilder message = new StringBuilder();
			message.append("Cache value for attribute '");
			appendAttribute(message, attribute);
			message.append("' is invalid.");
			throw new IllegalStateException(message.toString(), ((InvalidCacheValue) cacheValue)._ex);
		}
		return cacheValue;
	}

	/**
	 * Fetches the value of the given {@link DBAttribute} from the result set.
	 */
	protected Object fetchObject(DBAttribute dbAttr, DBHelper sqlDialect, ResultSet dbResult, int resultOffset)
			throws SQLException {
		return sqlDialect.mapToJava(dbResult, resultOffset + dbAttr.getDBColumnIndex(), dbAttr.getSQLType());
	}

	/**
	 * Fetches the long value of the given {@link DBAttribute} from the given result set.
	 * 
	 * @throws IllegalArgumentException
	 *         if the given attribute is not of type {@link DBType#LONG}.
	 * @throws SQLException
	 *         if accessing result set fails, or the value of the {@link DBAttribute} was
	 *         <code>null</code>.
	 */
	protected long fetchLong(DBAttribute dbAttr, ResultSet dbResult, int resultOffset) throws SQLException {
		if (dbAttr.getSQLType() != DBType.LONG) {
			throw new IllegalArgumentException("DBAttribute '" + dbAttr + "' of attribute '" + dbAttr.getAttribute()
				+ "' is not of type long: Expected: " + DBType.LONG + ", Actual: " + dbAttr.getSQLType());
		}
		long longValue = dbResult.getLong(resultOffset + dbAttr.getDBColumnIndex());
		if (dbResult.wasNull()) {
			throw new SQLException("DBAttribute '" + dbAttr + "' of attribute '" + dbAttr.getAttribute()
				+ " has value null.");
		}
		return longValue;
	}
	
	/**
	 * Stores the given long value for the given {@link DBAttribute}.
	 */
	protected void storeLong(DBAttribute dbAttr, Object[] stmtArgs, int stmtOffset, long value) {
		if (dbAttr.getSQLType() != DBType.LONG) {
			throw new IllegalArgumentException("DBAttribute '" + dbAttr + "' of attribute '" + dbAttr.getAttribute()
				+ "' is not of type long: Expected: " + DBType.LONG + ", Actual: " + dbAttr.getSQLType());
		}
		stmtArgs[stmtOffset + dbAttr.getDBColumnIndex()] = value;
	}
	

}
