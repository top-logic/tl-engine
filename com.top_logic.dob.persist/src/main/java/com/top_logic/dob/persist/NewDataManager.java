/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.persist;

import java.io.StringReader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.TLID;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.sql.CommitContext;
import com.top_logic.basic.sql.CommitContextWrapper;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.MSSQLHelper;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.NamedValues;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.simple.GenericDataObject;

/**
 * Optimized Reimplementation of the (Flex)Datamanager that will allow
 * searching.
 * 
 * Null values will not be stored, but indicate the removal of an Attribute.
 * Support for nested DataObjects is incomplete.
 * 
 * 
 * @author <a href="mailto:kha@top-logic.com">kha</a>
 */
public class NewDataManager extends AbstractDataManager {

	/**
	 * SQL statements used in {@link NewDataManager}.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static class Statements {

		private static final String IDENTIFIER_COLUMN_NAME = "IDENTIFIER";

		private static final String TYPE_COLUMN_NAME = "TYPE";

		private static final String TVAL_COLUMN_NAME = "TVAL";

		private static final String SVAL_COLUMN_NAME = "SVAL";

		private static final String DVAL_COLUMN_NAME = "DVAL";

		private static final String LVAL_COLUMN_NAME = "LVAL";

		private static final String VAL_TYPE_COLUMN_NAME = "VAL_TYPE";

		private static final String ATTR_COLUMN_NAME = "ATTR";

		private static final String DO_STORAGE_TABLE_NAME = "DO_STORAGE";

		/** SQL statement to insert a new column into DO_STORAGE */
		protected final String DO_INSERT;
		
		/** Number of parameter in {@link #DO_INSERT}. */
		static final int NUMBER_INSERT_PARAMETERS = 8;

		/** SQL statement to delete a complete DO entry. */
		protected final String DO_DELETE;

		/** SQL statement  to delete a single attribute. */
		protected final String DO_DELETE_ATTR;

		/** SQL statement to update an attribute. */
		protected final String DO_UPDATE_ATTR;

		/** SQL statement to select all attributes for a single DataObject. */
		protected final String DO_SELECT;

		/**
		 * Creates a {@link Statements}.
		 *
		 * @param sqlDialect The target SQL dialect.
		 */
		public Statements(DBHelper sqlDialect) {
			DO_INSERT = createInsert(sqlDialect);
			DO_DELETE = createDelete(sqlDialect);
			DO_DELETE_ATTR = createDeleteAttr(sqlDialect);
			DO_UPDATE_ATTR = createUpdateAttr(sqlDialect);
			DO_SELECT = createSelect(sqlDialect);
		}

		/**
		 * Create the {@link #DO_INSERT} statement.
		 */
		protected String createInsert(DBHelper sqlDialect) {
			StringBuilder sb = new StringBuilder();

			sb.append("INSERT INTO ");
			sb.append(sqlDialect.tableRef(DO_STORAGE_TABLE_NAME));
			sb.append(lockHintCreate(sqlDialect));
			sb.append(" VALUES(?,?,?,?,?,?,?,?)");

			return sb.toString();
		}

		/**
		 * Create the {@link #DO_SELECT} statement.
		 */
		protected String createSelect(DBHelper sqlDialect) {
			StringBuilder sb = new StringBuilder();

			sb.append("SELECT ");
			sb.append(sqlDialect.columnRef(ATTR_COLUMN_NAME));
			sb.append(",");
			sb.append(sqlDialect.columnRef(VAL_TYPE_COLUMN_NAME));
			sb.append(",");
			sb.append(sqlDialect.columnRef(LVAL_COLUMN_NAME));
			sb.append(",");
			sb.append(sqlDialect.columnRef(DVAL_COLUMN_NAME));
			sb.append(",");
			sb.append(sqlDialect.columnRef(SVAL_COLUMN_NAME));
			sb.append(",");
			sb.append(sqlDialect.columnRef(TVAL_COLUMN_NAME));
			sb.append(lockHintRead(sqlDialect));
			sb.append(" FROM ");
			sb.append(sqlDialect.tableRef(DO_STORAGE_TABLE_NAME));
			sb.append(" ");
			sb.append(" WHERE ");
			sb.append(sqlDialect.columnRef(TYPE_COLUMN_NAME));
			sb.append("=? AND ");
			sb.append(sqlDialect.columnRef(IDENTIFIER_COLUMN_NAME));
			sb.append("=?");

			return sb.toString();
		}

		/**
		 * Create the {@link #DO_UPDATE_ATTR} statement.
		 */
		protected String createUpdateAttr(DBHelper sqlDialect) {
			StringBuilder sb = new StringBuilder();

			sb.append("UPDATE ");
			sb.append(sqlDialect.tableRef(DO_STORAGE_TABLE_NAME));
			sb.append(" ");
			sb.append(lockHintModify(sqlDialect));
			sb.append(" SET ");
			sb.append(sqlDialect.columnRef(VAL_TYPE_COLUMN_NAME));
			sb.append("=?, ");
			sb.append(sqlDialect.columnRef(LVAL_COLUMN_NAME));
			sb.append("=?, ");
			sb.append(sqlDialect.columnRef(DVAL_COLUMN_NAME));
			sb.append("=?, ");
			sb.append(sqlDialect.columnRef(SVAL_COLUMN_NAME));
			sb.append("=?, ");
			sb.append(sqlDialect.columnRef(TVAL_COLUMN_NAME));
			sb.append("=?");
			sb.append(" WHERE ");
			sb.append(sqlDialect.columnRef(TYPE_COLUMN_NAME));
			sb.append("=? AND ");
			sb.append(sqlDialect.columnRef(IDENTIFIER_COLUMN_NAME));
			sb.append("=? AND ");
			sb.append(sqlDialect.columnRef(ATTR_COLUMN_NAME));
			sb.append("=?");

			return sb.toString();
		}

		/**
		 * Create the {@link #DO_DELETE_ATTR} statement.
		 */
		protected String createDeleteAttr(DBHelper sqlDialect) {
			StringBuilder sb = new StringBuilder();

			sb.append("DELETE FROM ");
			sb.append(sqlDialect.tableRef(DO_STORAGE_TABLE_NAME));
			sb.append(lockHintModify(sqlDialect));
			sb.append(" WHERE ");
			sb.append(sqlDialect.columnRef(TYPE_COLUMN_NAME));
			sb.append("=? AND ");
			sb.append(sqlDialect.columnRef(IDENTIFIER_COLUMN_NAME));
			sb.append("=? AND ");
			sb.append(sqlDialect.columnRef(ATTR_COLUMN_NAME));
			sb.append("=?");

			return sb.toString();
		}

		/**
		 * Create the {@link #DO_DELETE} statement.
		 */
		protected String createDelete(DBHelper sqlDialect) {
			StringBuilder sb = new StringBuilder();

			sb.append("DELETE FROM ");
			sb.append(sqlDialect.tableRef(DO_STORAGE_TABLE_NAME));
			sb.append(lockHintModify(sqlDialect));
			sb.append(" WHERE ");
			sb.append(sqlDialect.columnRef(TYPE_COLUMN_NAME));
			sb.append("=? AND ");
			sb.append(sqlDialect.columnRef(IDENTIFIER_COLUMN_NAME));
			sb.append("=?");

			return sb.toString();
		}

		
		/**
		 * Locking hint for data access operations to prevent deadlocks of
		 * concurrent updates.
		 * 
		 * @param sqlDialect
		 *        The current SQL dialect.
		 * @return The lock hint SQL fragment for the given SQL dialect.
		 */
		protected String lockHintRead(DBHelper sqlDialect) {
			return "";
		}
		
		/**
		 * Locking hint for data creation operations to prevent deadlocks of
		 * concurrent updates.
		 * 
		 * @param sqlDialect
		 *        The current SQL dialect.
		 * @return The lock hint SQL fragment for the given SQL dialect.
		 */
		protected String lockHintCreate(DBHelper sqlDialect) {
			return "";
		}

		/**
		 * Locking hint for data modification operations to prevent deadlocks of
		 * concurrent updates.
		 * 
		 * @param sqlDialect
		 *        The current SQL dialect.
		 * @return The lock hint SQL fragment for the given SQL dialect.
		 */
		protected String lockHintModify(DBHelper sqlDialect) {
			if (sqlDialect instanceof MSSQLHelper) {
				// Changes produce locks in the primary index of the table that
				// block searches that are performed during other parallel
				// modifications, even if independent identifier ranges are affected.
				//
				// Alternative wäre " WITH(TABLOCK HOLDLOCK)" in lockHintModify() und lockHintCreate().
				return " WITH(READPAST)";
			} else {
				return "";
			}
		}
		
	}
	
	protected final Statements statements;

	/**
	 * @param context
	 *        {@link InstantiationContext} context to instantiate sub configurations.
	 * @param config
	 *        Configuration for {@link NewDataManager}.
	 */
	public NewDataManager(InstantiationContext context, Config config) throws SQLException {
		super(context, config);

		this.statements = new Statements(dbHelper);
	}

	/**
	 * Create a new GenericDataObject with the given Attributes.
	 */
	@Override
	public DataObject createDataObject(String aType, TLID anID, int size) {
		return new GenericDataObject(aType, anID, size);
	}

	/**
	 * Load a new DataObject from the database.
	 * 
	 * As of now this will NOT work with inner Objcts only ...
	 * 
	 * @return null when no Object can be found.
	 */
	@Override
	public DataObject load(String aType, TLID anID) throws SQLException {
		GenericDataObject result = null;
		int retry = dbHelper.retryCount();
		List<Object[]> linkList = null;
		while (retry-- > 0) {
			PooledConnection readConnection = connectionPool.borrowReadConnection();
			try {
				try (PreparedStatement pstm = readConnection.prepareStatement(statements.DO_SELECT)) {
					pstm.setString(1, aType);
					IdentifierUtil.setId(pstm, 2, anID);
					try (ResultSet res = pstm.executeQuery()) {
						if (res.next()) {
							result = new GenericDataObject(aType, anID, DEFAULT_SIZE);
							linkList = putResult(result.getMap(), res);
						}
					}
				}
			} catch (SQLException sqx) {
				if (retry > 0 && dbHelper.canRetry(sqx)) {
					readConnection.closeConnection(sqx);
					continue;
				}
				throw sqx;
			} finally {
				connectionPool.releaseReadConnection(readConnection);
			}
			break;
		} // while retry

		// Avoid recursion by loading the inner objects here.
		if (linkList != null && result != null) {
			int size = linkList.size();
			Map theMap = result.getMap();
			for (int i = 0; i < size; i++) {
				Object nameTypeiD[] = linkList.get(i);
				DataObject innerDO = load((String) nameTypeiD[1], (TLID) nameTypeiD[2]);
				theMap.put(nameTypeiD[0], innerDO);
			}
		}

		return result;
	}

	/**
	 * Fill a GenericDataObject from a ResultSet.
	 * 
	 * must be called from a synchronized context.
	 * 
	 * @param aMap
	 *        The Map of the dataObject to be filled.
	 * @param res
	 *        A ResultSet based on a DO_STORAGE.
	 * 
	 * @return List of String[3] with name, type and If of inner Objects (neede
	 *         to avoid unsupported recursion)
	 */
	private List<Object[]> putResult(Map aMap, ResultSet res) throws SQLException {

		List<Object[]> linkList = null;
		do {
			String name = res.getString(1);
			char val_tye = (char) res.getByte(2);
			switch (val_tye) {
			case STRING_TYPE:
				String s = res.getString(5); // SVAL
				if (s == null) {
					s = dbHelper.getClobValue(res, 6);
				    if (res.wasNull()) {
                        // since null values are not stored,
                        s = ""; // this must have been an empty String
				    }
				}
				aMap.put(name, s);
				break;
			case INTEGER_TYPE:
					aMap.put(name, Integer.valueOf(res.getInt(3))); // LVAL
				break;
			case LONG_TYPE:
					aMap.put(name, Long.valueOf(res.getLong(3))); // LVAL
				break;
			case DATE_TYPE:
				long tmp = res.getLong(3);
				aMap.put(name, new Date(tmp)); // LVAL
				break;
			case FLOAT_TYPE:
					aMap.put(name, Float.valueOf(res.getFloat(4))); // DVAL
				break;
			case DOUBLE_TYPE:
					aMap.put(name, Double.valueOf(res.getDouble(4)));// DVAL
				break;
			case BOOLEAN_TRUE:
				aMap.put(name, Boolean.TRUE);
				break;
			case BOOLEAN_FALSE:
				aMap.put(name, Boolean.FALSE);
				break;
			case LINK_TYPE:
				if (linkList == null) {
						linkList = new ArrayList<>();
				}
				String theString = res.getString(5); // SVAL
				int thePos = theString.indexOf(' ');

				String type = theString.substring(0, thePos);
					TLID id = IdentifierUtil.fromExternalForm(theString.substring(thePos + 1));
					linkList.add(new Object[] { name, type, id });
				break;
			default:
				Logger.warn("Unknown Type '" + val_tye + "'", this);
			}
		} while (res.next());
		return linkList;
	}

	/**
	 * Store some arbitrary DataObject by inserting the values.
	 */
	private boolean storeAny(CommitContext context, DataObject anObject) throws UnreachableAssertion, SQLException {
		TLID theID = anObject.getIdentifier();
		String theType = anObject.tTable().getName();
		
		return storeAnyValues(context, theType, theID, anObject);
	}

	
	private boolean storeAnyValues(CommitContext context, String type, TLID id, NamedValues values)
			throws UnreachableAssertion, SQLException {
		try {
			String[] theAttributes = values.getAttributeNames();

			PooledConnection connection = context.getConnection();
			try (PreparedStatement pstm = connection.prepareStatement(statements.DO_DELETE)) {
				pstm.setString(1, type);
				IdentifierUtil.setId(pstm, 2, id);
				pstm.execute();
			}

			// Do not commit() here, must be triggered externally ...
			// cache.getConnection().commit();

			boolean result = true;
			List doList = null;
			int len = theAttributes.length;

			int maxBatchSize = dbHelper.getMaxBatchSize(Statements.NUMBER_INSERT_PARAMETERS);
			int numberBatches = 0;

			try (PreparedStatement pstm = connection.prepareStatement(statements.DO_INSERT)) {
				pstm.clearParameters();
				Object theValue;
				String theKey;
				for (int thePos = 0; thePos < len; thePos++) {
					pstm.setString(1, type);
					IdentifierUtil.setId(pstm, 2, id);
					theKey = theAttributes[thePos];
					try {
						theValue = values.getAttributeValue(theKey);
					} catch (NoSuchAttributeException ex) {
						throw new UnreachableAssertion(ex);
					}
					if (theValue == null)
						continue; // null values will not be stored

					pstm.setString(3, theKey);
					this.store(context, pstm, 4, theKey, theValue);

					pstm.addBatch();
					numberBatches++;
					if (numberBatches >= maxBatchSize) {
						pstm.executeBatch();
						numberBatches = 0;
					}
					if (theValue instanceof DataObject) {
						if (doList == null)
							doList = new ArrayList();
						doList.add(theValue);
					}
				}
				if (numberBatches > 0) {
					pstm.executeBatch();
				}
			}
			if (doList != null) {
				len = doList.size();
				for (int i = 0; i < len; i++) {
					if (!this.storeAny(context, (DataObject) doList.get(i))) {
						result = false;
						break;
					}
				}
			}

			return result;
		} catch (SQLException ex) {
			fail(type, id, ex);
			throw ex;
		}
	}

	private void fail(String type, TLID id, SQLException ex) {
		Logger.error("Failed to store values for object '" + id + "' of type '" + type + "'.", ex, NewDataManager.class);
	}

	/**
	 * Store the given single attribute using the Prepared statement.
	 * 
	 * @param ind
	 *        index where VAL_TYPE is found in pstm.
	 */
	private void store(CommitContext context, PreparedStatement pstm, int ind,
			String attributeName, Object anObject) throws SQLException {
		if (anObject instanceof String) {
			pstm.setByte(ind, STRING_TYPE);
			String s = (String) anObject;
			pstm.setNull(ind + 1, Types.INTEGER);
			pstm.setNull(ind + 2, Types.DOUBLE);
			if (s.length() < 254) {
				pstm.setString(ind + 3, s);
				pstm.setNull(ind + 4, Types.CLOB);
			} else {
				pstm.setNull(ind + 3, Types.VARCHAR);
				pstm.setCharacterStream(ind + 4, new StringReader(s), s.length());
			}
		} else if (anObject instanceof Number) {
			if (anObject instanceof Integer) {
				pstm.setByte(ind, INTEGER_TYPE);
				pstm.setInt(ind + 1, ((Integer) anObject).intValue());
				pstm.setNull(ind + 2, Types.DOUBLE);
				pstm.setNull(ind + 3, Types.VARCHAR);
				pstm.setNull(ind + 4, Types.CLOB);
			} else if (anObject instanceof Double) {
				pstm.setByte(ind, DOUBLE_TYPE);
				pstm.setNull(ind + 1, Types.INTEGER);
				pstm.setDouble(ind + 2, ((Double) anObject).doubleValue());
				pstm.setNull(ind + 3, Types.VARCHAR);
				pstm.setNull(ind + 4, Types.CLOB);
			} else if (anObject instanceof Float) {
				pstm.setByte(ind, FLOAT_TYPE);
				pstm.setNull(ind + 1, Types.INTEGER);
				pstm.setFloat(ind + 2, ((Float) anObject).floatValue());
				pstm.setNull(ind + 3, Types.VARCHAR);
				pstm.setNull(ind + 4, Types.CLOB);
			} else if (anObject instanceof Long) {
				pstm.setByte(ind, LONG_TYPE);
				pstm.setLong(ind + 1, ((Long) anObject).longValue());
				pstm.setNull(ind + 2, Types.DOUBLE);
				pstm.setNull(ind + 3, Types.VARCHAR);
				pstm.setNull(ind + 4, Types.CLOB);
			} else {
				throw new SQLException("Can not store " + anObject.getClass()
						+ " as flex attribute '" + attributeName + "'");
			}
		} else if (anObject instanceof Date) {
			pstm.setByte(ind, DATE_TYPE);
			long l = ((Date) anObject).getTime();
			pstm.setLong(ind + 1, l);
			pstm.setNull(ind + 2, Types.DOUBLE);
			pstm.setNull(ind + 3, Types.VARCHAR);
			pstm.setNull(ind + 4, Types.CLOB);
		} else if (anObject instanceof Boolean) {
			if (((Boolean) anObject).booleanValue())
				pstm.setByte(ind, BOOLEAN_TRUE);
			else
				pstm.setByte(ind, BOOLEAN_FALSE);
			pstm.setNull(ind + 1, Types.INTEGER);
			pstm.setNull(ind + 2, Types.DOUBLE);
			pstm.setNull(ind + 3, Types.VARCHAR);
			pstm.setNull(ind + 4, Types.CLOB);
		} else if (anObject instanceof DataObject) {
			pstm.setByte(ind, LINK_TYPE);
			pstm.setNull(ind + 1, Types.INTEGER);
			pstm.setNull(ind + 2, Types.DOUBLE);
			DataObject theDO = (DataObject) anObject;
			if (anObject instanceof GenericDataObject) {
				GenericDataObject theEDo = (GenericDataObject) theDO;
				pstm.setString(ind + 3, theEDo.getMetaObjectName() + ' '
					+ IdentifierUtil.toExternalForm(theEDo.getIdentifier()));

			} else {
				pstm.setString(ind + 3, theDO.tTable().getName() + ' '
					+ IdentifierUtil.toExternalForm(theDO.getIdentifier()));
			}
			pstm.setNull(ind + 4, Types.CLOB);
			// store(theDO, context); // must be done by caller, will ruin pstm
		} else {
			pstm.setNull(ind + 1, Types.INTEGER);
			pstm.setNull(ind + 2, Types.DOUBLE);
			pstm.setNull(ind + 3, Types.VARCHAR);
			pstm.setNull(ind + 4, Types.CLOB);
			throw new SQLException("Can not store " + anObject.getClass()
					+ " as flex attribute '" + attributeName + "'");
		}
	}

	/**
	 * Insert a Map of Attributes into the DO_STORAGE table.
	 */
	private void addAttributes(CommitContext context, String aType, TLID anID, Map aMap) throws SQLException {
		int maxBatchSize = dbHelper.getMaxBatchSize(Statements.NUMBER_INSERT_PARAMETERS);
		try (PreparedStatement pstm = context.getConnection().prepareStatement(statements.DO_INSERT)) {
			int numberBatches = 0;
			for (Iterator theIt = aMap.keySet().iterator(); theIt.hasNext();) {
				String theKey = (String) theIt.next();
				Object theValue = aMap.get(theKey);

				pstm.clearParameters();
				pstm.setString(1, aType);
				IdentifierUtil.setId(pstm, 2, anID);
				pstm.setString(3, theKey);
				this.store(context, pstm, 4, theKey, theValue);
				pstm.addBatch();
				numberBatches++;
				if (numberBatches >= maxBatchSize) {
					pstm.executeBatch();
					numberBatches = 0;
				}
			}
			if (numberBatches > 0) {
				pstm.executeBatch();
			}
		}
	}

	/**
	 * Update a Map of Attributes into the DO_STORAGE table.
	 */
	private void updateAttributes(CommitContext context, String aType, TLID anID, Map aMap) throws SQLException {
		int maxBatchSize = dbHelper.getMaxBatchSize(8);
		int numberBatches = 0;
		try (PreparedStatement pstm = context.getConnection().prepareStatement(statements.DO_UPDATE_ATTR)) {
			for (Iterator theIt = aMap.keySet().iterator(); theIt.hasNext();) {
				String theKey = (String) theIt.next();
				Object theValue = aMap.get(theKey);

				pstm.clearParameters();
				this.store(context, pstm, 1, theKey, theValue);
				pstm.setString(6, aType);
				IdentifierUtil.setId(pstm, 7, anID);
				pstm.setString(8, theKey);
				pstm.addBatch();
				numberBatches++;
				if (numberBatches >= maxBatchSize) {
					pstm.executeBatch();
					numberBatches = 0;
				}
			}
			if (numberBatches > 0) {
				pstm.executeBatch();
			}
		}
	}

	/**
	 * Delete a Map of Attributes from the DO_STORAGE table.
	 */
	private void deleteAttributes(CommitContext context, String aType, TLID anID, Map aMap) throws SQLException {
		List children = null;
		int maxBatchSize = dbHelper.getMaxBatchSize(3);
		try (PreparedStatement pstm = context.getConnection().prepareStatement(statements.DO_DELETE_ATTR)) {
			int numberBatches = 0;
			for (Iterator theIt = aMap.keySet().iterator(); theIt.hasNext();) {
				String theKey = (String) theIt.next();
				Object theValue = aMap.get(theKey);

				pstm.clearParameters();
				pstm.setString(1, aType);
				IdentifierUtil.setId(pstm, 2, anID);
				pstm.setString(3, theKey);
				pstm.addBatch();
				numberBatches++;
				if (numberBatches >= maxBatchSize) {
					pstm.executeBatch();
					numberBatches = 0;
				}

				if (theValue instanceof DataObject) {
					if (children == null) {
						children = new ArrayList();
					}
					children.add(theValue);
				}
			}
			if (numberBatches > 0) {
				pstm.executeBatch();
			}
		}
		if (children != null) {
			int len = children.size();
			for (int i = 0; i < len; i++) {
				delete((DataObject) children.get(i), context);
			}
		}

	}
	
	@Override
	protected boolean internalStore(CommitContext context, String type, TLID id, NamedValues values)
			throws SQLException {
		
		if (values instanceof GenericDataObject) {
			return storeGenericValues(context, type, id, (GenericDataObject) values);
		} else {
			return storeAnyValues(context, type, id, values);
		}
	}

	private boolean storeGenericValues(CommitContext context, String type, TLID id, GenericDataObject values)
			throws SQLException {
		try {
			Map theMap = values.deleteMap;

			// Due to strange setValue cycles key may be duplicated
			// so instead of an actual update we do a remove + insert
			// See TestNewDataManger testTransientChanges for an idea ..

			if (theMap != null && theMap.size() > 0) {
				deleteAttributes(context, type, id, theMap);
			}

			theMap = values.insertMap;
			if (theMap != null && theMap.size() > 0) {
				addAttributes(context, type, id, theMap);
			}

			theMap = values.updateMap;
			if (theMap != null && theMap.size() > 0) {
				updateAttributes(context, type, id, theMap);
			}

			values.resetMaps();
			return true;
		} catch (SQLException ex) {
			fail(type, id, ex);
			throw ex;
		}
	}

	/** Lowlevel part of DataObject deletion */
	private boolean deleteFlat(CommitContext context, DataObject anObject) throws SQLException {
		TLID theId = anObject.getIdentifier();
		String theType;
		if (anObject instanceof GenericDataObject) {
			GenericDataObject theGDo = (GenericDataObject) anObject;
			theType = theGDo.getMetaObjectName();
		} else {
			theType = anObject.tTable().getName();
		}

		return deleteFlatValues(context, theType, theId);
	}

	private boolean deleteFlatValues(CommitContext context, String type, TLID id) throws SQLException {
		try (PreparedStatement pstm = context.getConnection().prepareStatement(statements.DO_DELETE)) {
			pstm.setString(1, type);
			IdentifierUtil.setId(pstm, 2, id);

			// TODO KHA: This looks like a bug: May affect more than 1 line.
			return 1 == pstm.executeUpdate();
		}
	}

	/** Called to remove all SubObjects of a given DataObject. */
	protected boolean deleteChildren(CommitContext context, NamedValues anObject)
			throws SQLException {

		String[] theNames = anObject.getAttributeNames();
		boolean isOK = true;
		try {
			for (int thePos = 0; thePos < theNames.length && isOK; thePos++) {
				Object theValue = anObject.getAttributeValue(theNames[thePos]);

				if (theValue instanceof DataObject) {
					DataObject theDO = (DataObject) theValue;
					isOK = deleteFlat(context, theDO)
							|| deleteChildren(context, theDO);
				}
			}
		} catch (NoSuchAttributeException ignored) { /* ignored */
		}
		return isOK;
	}

	/**
	 * Delete the given DataObject from the Storage.
	 */
	@Override
	public boolean delete(DataObject anObject) throws SQLException {
		PooledConnection connection = connectionPool.borrowWriteConnection();
		try {
			CommitContextWrapper commitContext = new CommitContextWrapper(connection);
			
			boolean result = deleteChildren(commitContext, anObject) | deleteFlat(commitContext, anObject);
			connection.commit();
			
			return result;
		} catch (SQLException ex) {
			Logger.error("failed to delete " + anObject, ex, this);
			throw ex;
		} finally {
			connectionPool.releaseWriteConnection(connection);
		}
	}

	/**
	 * Delete the given DataObject from the Storage using given Context.
	 */
	@Override
	public boolean delete(DataObject anObject, CommitContext context) throws SQLException {
		boolean result;
		try {
			result = deleteChildren(context, anObject) | deleteFlat(context, anObject);
		} catch (SQLException e) {
			Logger.error("failed to delete " + anObject, e, this);
			throw e;
		}
		return result;
	}
	
	@Override
	public boolean deleteValues(CommitContext context, String type, TLID id, NamedValues values) throws SQLException {
		boolean result;
		try {
			result = deleteChildren(context, values) | deleteFlatValues(context, type, id);
		} catch (SQLException e) {
			Logger.error("failed to delete " + values, e, this);
			throw e;
		}
		return result;
	}
}
