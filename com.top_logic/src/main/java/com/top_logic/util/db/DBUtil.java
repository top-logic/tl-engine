/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.db;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.TLID;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;

/**
 * Holds static util methods for easy database access.
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public class DBUtil {


    /**
     * Gets <code>true</code>, if the ResultSet has at least one row.
     */
    public static class ResultAsBoolean implements ResultExtractor<Boolean> {

		/**
		 * Singleton {@link DBUtil.ResultAsBoolean} instance.
		 */
		public static final DBUtil.ResultAsBoolean INSTANCE = new DBUtil.ResultAsBoolean();

		private ResultAsBoolean() {
			// Singleton constructor.
		}

        @Override
		public Boolean extractResult(ResultSet result) throws SQLException {
            return Boolean.valueOf(result.next());
        }
    }


    /**
     * Gets the first column of the first row of the ResultSet as Integer.
     */
    public static class ResultAsInteger implements ResultExtractor<Integer> {

		/**
		 * Singleton {@link DBUtil.ResultAsInteger} instance.
		 */
		public static final DBUtil.ResultAsInteger INSTANCE = new DBUtil.ResultAsInteger();

		private ResultAsInteger() {
			// Singleton constructor.
		}

        @Override
		public Integer extractResult(ResultSet result) throws SQLException {
            if (result.next()) {
                return Integer.valueOf(result.getInt(1));
            }
            return Integer.valueOf(0);
        }
    }



    /**
     * Gets the first column of the first row of the ResultSet as Long.
     */
    public static class ResultAsLong implements ResultExtractor<Long> {

		/**
		 * Singleton {@link DBUtil.ResultAsLong} instance.
		 */
		public static final DBUtil.ResultAsLong INSTANCE = new DBUtil.ResultAsLong();

		private ResultAsLong() {
			// Singleton constructor.
		}

        @Override
		public Long extractResult(ResultSet result) throws SQLException {
            if (result.next()) {
                return Long.valueOf(result.getLong(1));
            }
            return Long.valueOf(0);
        }
    }



    /**
     * Gets the first column of the first row of the ResultSet as String.
     */
    public static class ResultAsString implements ResultExtractor<String> {

		/**
		 * Singleton {@link DBUtil.ResultAsString} instance.
		 */
		public static final DBUtil.ResultAsString INSTANCE = new DBUtil.ResultAsString();

		private ResultAsString() {
			// Singleton constructor.
		}

        @Override
		public String extractResult(ResultSet result) throws SQLException {
            if (result.next()) {
                return result.getString(1);
            }
            return null;
        }
    }



    /**
     * Gets the first column of the first row of the ResultSet as Java Date.
     */
    public static class ResultAsJavaDate implements ResultExtractor<Date> {

		/**
		 * Singleton {@link DBUtil.ResultAsJavaDate} instance.
		 */
		public static final DBUtil.ResultAsJavaDate INSTANCE = new DBUtil.ResultAsJavaDate();

		private ResultAsJavaDate() {
			// Singleton constructor.
		}

        @Override
		public Date extractResult(ResultSet result) throws SQLException {
            Long time = ResultAsLong.INSTANCE.extractResult(result);
            return time == null || time.longValue() == 0 ? null : new Date(time.longValue());
        }
    }



    /**
     * Gets the first column of the first row of the ResultSet as Timestamp.
     */
    public static class ResultAsTimeStamp implements ResultExtractor<Date> {

		/**
		 * Singleton {@link DBUtil.ResultAsTimeStamp} instance.
		 */
		public static final DBUtil.ResultAsTimeStamp INSTANCE = new DBUtil.ResultAsTimeStamp();

		private ResultAsTimeStamp() {
			// Singleton constructor.
		}

        @Override
		public Date extractResult(ResultSet result) throws SQLException {
            if (result.next()) {
				return result.getTimestamp(1, CalendarUtil.createCalendar());
            }
            return null;
        }
    }



    /**
     * Gets a list of strings which are the result of the (one column) database query.
     */
	public static class ResultAsStringList implements ResultExtractor<List<String>> {

		/**
		 * Singleton {@link DBUtil.ResultAsStringList} instance.
		 */
		public static final DBUtil.ResultAsStringList INSTANCE = new DBUtil.ResultAsStringList();

		private ResultAsStringList() {
			// Singleton constructor.
		}

        @Override
		public List<String> extractResult(ResultSet result) throws SQLException {
            ArrayList<String> resultList = new ArrayList<>();
            while (result.next()) {
                resultList.add(result.getString(1));
            }
            return resultList;
        }
    }

	/**
	 * Gets a list of {@link Integer} objects which are the result of the (one column) database
	 * query.
	 */
	public static class ResultAsIntList implements ResultExtractor<List<Integer>> {

		/**
		 * Singleton {@link DBUtil.ResultAsIntList} instance.
		 */
		public static final DBUtil.ResultAsIntList INSTANCE = new DBUtil.ResultAsIntList();

		private ResultAsIntList() {
			// Singleton constructor.
		}

		@Override
		public List<Integer> extractResult(ResultSet result) throws SQLException {
			ArrayList<Integer> resultList = new ArrayList<>();
			while (result.next()) {
				resultList.add(Integer.valueOf(result.getInt(1)));
			}
			return resultList;
		}
	}

	/**
	 * Gets a list of {@link Long} objects which are the result of the (one column) database query.
	 */
	public static class ResultAsLongList implements ResultExtractor<List<Long>> {

		/**
		 * Singleton {@link DBUtil.ResultAsLongList} instance.
		 */
		public static final DBUtil.ResultAsLongList INSTANCE = new DBUtil.ResultAsLongList();

		private ResultAsLongList() {
			// Singleton constructor.
		}

		@Override
		public List<Long> extractResult(ResultSet result) throws SQLException {
			ArrayList<Long> resultList = new ArrayList<>();
			while (result.next()) {
				resultList.add(Long.valueOf(result.getLong(1)));
			}
			return resultList;
		}
	}

	/**
	 * Gets a list of strings which are the result of the (one column) database query.
	 */
	public static class ResultAsIdList implements ResultExtractor<List<TLID>> {

		/**
		 * Singleton {@link DBUtil.ResultAsIdList} instance.
		 */
		public static final DBUtil.ResultAsIdList INSTANCE = new DBUtil.ResultAsIdList();

		private ResultAsIdList() {
			// Singleton constructor.
		}

		@Override
		public List<TLID> extractResult(ResultSet result) throws SQLException {
			ArrayList<TLID> resultList = new ArrayList<>();
			while (result.next()) {
				resultList.add(IdentifierUtil.getId(result, 1));
			}
			return resultList;
		}
	}


    /**
     * Gets a list of string[] which are the result of the (multiple column) database query.
     * Each string[] in the list represents a row of the result.
     */
	public static class ResultAsMatrix implements ResultExtractor<List<Object[]>> {

		private final DBHelper _sqlDialect;

		private final DBType[] _types;

		/**
		 * Creates a {@link ResultAsMatrix}.
		 * 
		 * @param sqlDialect
		 *        The {@link DBHelper} to use.
		 * @param types
		 *        The types to fetch.
		 */
		public ResultAsMatrix(DBHelper sqlDialect, DBType... types) {
			_sqlDialect = sqlDialect;
			_types = types;
		}

        @Override
		public List<Object[]> extractResult(ResultSet result) throws SQLException {
			int columnCount = _types.length;
			ArrayList<Object[]> resultList = new ArrayList<>();
            while (result.next()) {
				Object[] row = new Object[columnCount];
				for (int i = 0; i < columnCount; i++) {
					int offset = i + 1;
					row[i] = _sqlDialect.mapToJava(result, offset, _types[i]);
                }
                resultList.add(row);
            }
            return resultList;
        }
    }


    /**
     * Gets a table as string[][] which is the result of the (multiple column) database
     * query, including column headers in the first row.
     */
    public static class ResultAsTable implements ResultExtractor<String[][]> {

		/**
		 * Singleton {@link DBUtil.ResultAsTable} instance.
		 */
		public static final DBUtil.ResultAsTable INSTANCE = new DBUtil.ResultAsTable();

		private ResultAsTable() {
			// Singleton constructor.
		}

        @Override
		public String[][] extractResult(ResultSet result) throws SQLException {
            String[] theRow;
            ArrayList<String[]> theResultList = new ArrayList<>();
            ResultSetMetaData metaData = result.getMetaData();
            int columnCount = metaData.getColumnCount();
            theRow = new String[columnCount];
            for (int i = 0; i < columnCount;) {
                theRow[i++] = metaData.getColumnLabel(i);
            }
            theResultList.add(theRow);
            while (result.next()) {
                theRow = new String[columnCount];
				for (int i = 0; i < columnCount; i++) {
					Object value = result.getObject(i + 1);
					String text;
					if (value == null) {
						text = null;
					} else if (value instanceof Clob) {
						Clob clob = (Clob) value;
						long fullLength = clob.length();
						if (fullLength > 4096) {
							text = clob.getSubString(1, 4096) + "...";
						} else {
							text = clob.getSubString(1, (int) fullLength);
						}
					} else {
						text = value.toString();
					}
					theRow[i] = text;
                }
                theResultList.add(theRow);
            }
            return theResultList.toArray(new String[theResultList.size()][]);
        }
    }


    /**
     * Gets a list of wrappers which are the result of the (one column) database query.
     */
	public static class ResultAsWrapperList<T extends Wrapper> implements ResultExtractor<List<T>> {

        // The KO type(s) of the wrappers to resolve.
        private final String koType;

		private final Class<T> _expectedType;

		/**
		 * Creates a {@link ResultAsWrapperList}.
		 * 
		 * @param koType
		 *        The monomorphic type name to resolve objects with.
		 */
		public ResultAsWrapperList(String koType, Class<T> expectedType) {
			assert koType != null;
            this.koType = koType;
			_expectedType = expectedType;
        }

        @Override
		public List<T> extractResult(ResultSet result) throws SQLException {
			ArrayList<T> resultList = new ArrayList<>();
            while (result.next()) {
				TLID theID = IdentifierUtil.getId(result, 1);
				T wrapper = CollectionUtil.dynamicCast(_expectedType, WrapperFactory.getWrapper(theID, koType));
                if (wrapper != null) {
                    resultList.add(wrapper);
                } else {
					Logger.warn("Failed to getWrapper for ID '" + theID + "' and koType '" + koType + "'.",
						DBUtil.class);
                }
            }
            return resultList;
        }
    }

    /**
     * Gets a list of wrappers which are the result of the (one column) database query.
     */
	public static class ResultAsMapFromStringToWrapperList<T extends Wrapper> implements
			ResultExtractor<Map<TLID, List<T>>> {

        // The KO type(s) of the wrappers to resolve.
        private final String koType;

		private final Class<T> _expectedType;

		/**
		 * Creates a {@link ResultAsMapFromStringToWrapperList}.
		 * 
		 * @param koType
		 *        The monomorphic type to resolve objects from.
		 * @param expectedType
		 *        the expected {@link Wrapper} type.
		 */
		public ResultAsMapFromStringToWrapperList(String koType, Class<T> expectedType) {
			assert koType != null;
            this.koType = koType;
			_expectedType = expectedType;
        }

        @Override
		public Map<TLID, List<T>> extractResult(ResultSet result) throws SQLException {
			Map<TLID, List<T>> theResult = new HashMap<>();
            while (result.next()) {
				TLID theKeyID = IdentifierUtil.getId(result, 1);
				TLID theValueID = IdentifierUtil.getId(result, 2);
				List<T> resultList = theResult.get(theKeyID);
                if ((resultList) == null) {
					resultList = new ArrayList<>();
                    theResult.put(theKeyID, resultList);
                }

				T wrapper = resolve(theValueID);
                if (wrapper != null) {
                    resultList.add(wrapper);
                } else {
                    Logger.warn("Failed to getWrapper for ID '" + theValueID + "'" + (koType == null ? "." : (" and koType '" + koType + "'.")), DBUtil.class);
                }
            }
            return theResult;
        }

		private T resolve(TLID id) {
			Wrapper genericWrapper = WrapperFactory.getWrapper(id, koType);
			if (genericWrapper == null) {
				return null;
			}
			T wrapper = CollectionUtil.dynamicCast(_expectedType, genericWrapper);
			return wrapper;
		}
    }



    /**
     * Convenience method to create a parameter array for prepared statements containing the
     * elements given in the argument list.
     *
     * @param params
     *        the elements of the new array
     * @return an array containing the elements given in the argument list
     */
    public static Object[] params(Object... params) {
        return params;
    }



    /**
     * Executes a write statement on the database.
     *
     * @param aConnection
     *        the connection to use for the database query
     * @param aStatement
     *        the statement to execute
     * @param params
     *        the parameters of the statement; may be <code>null</code> if the statement has
     *        no parameters
     * @return the amount of database entries that have been changed (inserted / updated /
     *         removed)
     * @throws SQLException
     *         if some error occurs while requesting the database
     */
    public static int executeUpdate(Connection aConnection, String aStatement, Object[] params) throws SQLException {
        if (params == null) {
            Statement stm = aConnection.createStatement();
            try {
                return stm.executeUpdate(aStatement);
            }
            finally {
                stm.close();
            }
        }
        PreparedStatement pstm = aConnection.prepareStatement(aStatement);
        try {
            setVector(pstm, params);
            return pstm.executeUpdate();
        } finally {
            pstm.close();
        }
    }

    /**
     * Executes a write statement on the database.
     *
     * @param aConnection
     *        the connection to use for the database query
     * @param aStatement
     *        the statement to execute
     * @return the amount of database entries that have been changed (inserted / updated /
     *         removed)
     * @throws SQLException
     *         if some error occurs while requesting the database
     */
    public static int executeUpdate(Connection aConnection, String aStatement) throws SQLException {
        return executeUpdate(aConnection, aStatement, null);
    }

    /**
     * Executes a write statement on the database and commits the changes.
     *
     * @param aConnectionPool
     *        the connection pool to borrow a write connection to use for the database
     *        update from
     * @param aStatement
     *        the statement to execute
     * @param params
     *        the parameters of the statement; may be <code>null</code> if the statement has
     *        no parameters
     * @return the amount of database entries that have been changed (inserted / updated /
     *         removed)
     * @throws SQLException
     *         if some error occurs while requesting the database
     */
    public static int executeDirectUpdate(ConnectionPool aConnectionPool, String aStatement, Object[] params) throws SQLException {
        PooledConnection connection = aConnectionPool.borrowWriteConnection();
        try {
            int result = executeUpdate(connection, aStatement, params);
            connection.commit();
            return result;
        }
        finally {
            aConnectionPool.releaseWriteConnection(connection);
        }
    }

    /**
     * Executes a write statement on the database and commits the changes.
     *
     * @param aStatement
     *        the statement to execute
     * @param params
     *        the parameters of the statement; may be <code>null</code> if the statement has
     *        no parameters
     * @return the amount of database entries that have been changed (inserted / updated /
     *         removed)
     * @throws SQLException
     *         if some error occurs while requesting the database
     */
    public static int executeDirectUpdate(String aStatement, Object[] params) throws SQLException {
        return executeDirectUpdate(ConnectionPoolRegistry.getDefaultConnectionPool(), aStatement, params);
    }

    /**
     * Executes a write statement on the database and commits the changes.
     *
     * @param aStatement
     *        the statement to execute
     * @return the amount of database entries that have been changed (inserted / updated /
     *         removed)
     * @throws SQLException
     *         if some error occurs while requesting the database
     */
    public static int executeDirectUpdate(String aStatement) throws SQLException {
        return executeDirectUpdate(aStatement, null);
    }



    /**
     * Executes a read statement on the database.
     * @param aConnection
     *        the connection to use for the database query
     * @param aStatement
     *        the statement to execute
     * @param params
     *        the parameters of the statement; may be <code>null</code> if the statement has
     *        no parameters
     * @param aExtractor
     *        the extractor to extract the relevant information from the query result
     * @param <T>
     *        the result type of the query
     * @return the result of the query as specified by the ResultExtractor
     * @throws SQLException
     *         if some error occurs while requesting the database
     */
    public static <T> T executeQuery(Connection aConnection, String aStatement, Object[] params, ResultExtractor<T> aExtractor) throws SQLException {
        Statement stm = null;
        if (params == null) {
            stm = aConnection.createStatement();
        }
        else {
            stm = aConnection.prepareStatement(aStatement);
            setVector((PreparedStatement)stm, params);
        }

        try {
            ResultSet res = stm instanceof PreparedStatement ? ((PreparedStatement)stm).executeQuery() : stm.executeQuery(aStatement);
            try {
                return aExtractor.extractResult(res);
            }
            finally {
                res.close();
            }
        }
        finally {
            stm.close();
        }
    }

    /**
     * Executes a read statement on the database.
     *
     * @param <T>
     *        the result type of the query
     * @param aConnectionPool
     *        the connection pool to borrow a read connection to use for the database query
     *        from
     * @param aStatement
     *        the statement to execute
     * @param params
     *        the parameters of the statement; may be <code>null</code> if the statement has
     *        no parameters
     * @param aExtractor
     *        the extractor to extract the relevant information from the query result
     * @return the result of the query as specified by the ResultExtractor
     * @throws SQLException
     *         if some error occurs while requesting the database
     */
    public static <T> T executeQuery(ConnectionPool aConnectionPool, String aStatement, Object[] params, ResultExtractor<T> aExtractor) throws SQLException {
        DBHelper dbHelper = aConnectionPool.getSQLDialect();
        int retry = dbHelper.retryCount();
        while (true) {
            PooledConnection aConnection = aConnectionPool.borrowReadConnection();
            try {
                return executeQuery(aConnection, aStatement, params, aExtractor);
            }
            catch (SQLException e) {
                if (!dbHelper.canRetry(e) || --retry <= 0) {
                    throw e;
                }
                aConnection.closeConnection(e);
            }
            finally {
                aConnectionPool.releaseReadConnection(aConnection);
            }
        }
    }

    /**
     * Executes a read statement on the database, using default connection pool.
     *
     * @param <T>
     *        the result type of the query
     * @param aStatement
     *        the statement to execute
     * @param params
     *        the parameters of the statement; may be <code>null</code> if the statement has
     *        no parameters
     * @param aExtractor
     *        the extractor to extract the relevant information from the query result
     * @return the result of the query as specified by the ResultExtractor
     * @throws SQLException
     *         if some error occurs while requesting the database
     */
    public static <T> T executeQuery(String aStatement, Object[] params, ResultExtractor<T> aExtractor) throws SQLException {
        return executeQuery(ConnectionPoolRegistry.getDefaultConnectionPool(), aStatement, params, aExtractor);
    }

    /**
     * Executes a read statement on the database, using default connection pool.
     *
     * @param <T>
     *        the result type of the query
     * @param aStatement
     *        the statement to execute
     * @param aExtractor
     *        the extractor to extract the relevant information from the query result
     * @return the result of the query as specified by the ResultExtractor
     * @throws SQLException
     *         if some error occurs while requesting the database
     */
    public static <T> T executeQuery(String aStatement, ResultExtractor<T> aExtractor) throws SQLException {
        return executeQuery(aStatement, null, aExtractor);
    }



    /**
     * Executes an exist statement on the database.
     *
     * @param aConnection
     *        the connection to use for the database query
     * @param aStatement
     *        the statement to execute
     * @param params
     *        the parameters of the statement; may be <code>null</code> if the statement has
     *        no parameters
     * @return <code>true</code> if the database returned a result, <code>false</code>
     *         otherwise
     * @throws SQLException
     *         if some error occurs while requesting the database
     */
    public static boolean executeQueryAsBoolean(Connection aConnection, String aStatement, Object[] params) throws SQLException {
        return executeQuery(aConnection, aStatement, params, ResultAsBoolean.INSTANCE).booleanValue();
    }

    /**
     * Executes an exist statement on the database.
     *
     * @param aStatement
     *        the statement to execute
     * @param params
     *        the parameters of the statement; may be <code>null</code> if the statement has
     *        no parameters
     * @return <code>true</code> if the database returned a result, <code>false</code>
     *         otherwise
     * @throws SQLException
     *         if some error occurs while requesting the database
     */
    public static boolean executeQueryAsBoolean(ConnectionPool aConnectionPool, String aStatement, Object[] params) throws SQLException {
        return executeQuery(aConnectionPool, aStatement, params, ResultAsBoolean.INSTANCE).booleanValue();
    }

    /**
     * Executes an exist statement on the database.
     *
     * @param aStatement
     *        the statement to execute
     * @param params
     *        the parameters of the statement; may be <code>null</code> if the statement has
     *        no parameters
     * @return <code>true</code> if the database returned a result, <code>false</code>
     *         otherwise
     * @throws SQLException
     *         if some error occurs while requesting the database
     */
    public static boolean executeQueryAsBoolean(String aStatement, Object[] params) throws SQLException {
        return executeQuery(aStatement, params, ResultAsBoolean.INSTANCE).booleanValue();
    }

    /**
     * Executes an exist statement on the database.
     *
     * @param aStatement
     *        the statement to execute
     * @return <code>true</code> if the database returned a result, <code>false</code>
     *         otherwise
     * @throws SQLException
     *         if some error occurs while requesting the database
     */
    public static boolean executeQueryAsBoolean(String aStatement) throws SQLException {
        return executeQueryAsBoolean(aStatement, null);
    }


    /**
     * Executes a single value result read statement on the database.
     *
     * @param aConnection
     *        the connection to use for the database query
     * @param aStatement
     *        the statement to execute
     * @param params
     *        the parameters of the statement; may be <code>null</code> if the statement has
     *        no parameters
     * @return the result of the query as int
     * @throws SQLException
     *         if some error occurs while requesting the database
     */
    public static int executeQueryAsInteger(Connection aConnection, String aStatement, Object[] params) throws SQLException {
        return executeQuery(aConnection, aStatement, params, ResultAsInteger.INSTANCE).intValue();
    }

    /**
     * Executes a single value result read statement on the database.
     *
     * @param aConnectionPool
     *        the connection pool to borrow a read connection to use for the database query
     *        from
     * @param aStatement
     *        the statement to execute
     * @param params
     *        the parameters of the statement; may be <code>null</code> if the statement has
     *        no parameters
     * @return the result of the query as int
     * @throws SQLException
     *         if some error occurs while requesting the database
     */
    public static int executeQueryAsInteger(ConnectionPool aConnectionPool, String aStatement, Object[] params) throws SQLException {
        return executeQuery(aConnectionPool, aStatement, params, ResultAsInteger.INSTANCE).intValue();
    }

    /**
     * Executes a single value result read statement on the database.
     *
     * @param aStatement
     *        the statement to execute
     * @param params
     *        the parameters of the statement; may be <code>null</code> if the statement has
     *        no parameters
     * @return the result of the query as int
     * @throws SQLException
     *         if some error occurs while requesting the database
     */
    public static int executeQueryAsInteger(String aStatement, Object[] params) throws SQLException {
        return executeQuery(aStatement, params, ResultAsInteger.INSTANCE).intValue();
    }

    /**
     * Executes a single value result read statement on the database.
     *
     * @param aStatement
     *        the statement to execute
     * @return the result of the query as int
     * @throws SQLException
     *         if some error occurs while requesting the database
     */
    public static int executeQueryAsInteger(String aStatement) throws SQLException {
        return executeQueryAsInteger(aStatement, null);
    }


    /**
     * Executes a single value result read statement on the database.
     *
     * @param aConnection
     *        the connection to use for the database query
     * @param aStatement
     *        the statement to execute
     * @param params
     *        the parameters of the statement; may be <code>null</code> if the statement has
     *        no parameters
     * @return the result of the query as long
     * @throws SQLException
     *         if some error occurs while requesting the database
     */
    public static long executeQueryAsLong(Connection aConnection, String aStatement, Object[] params) throws SQLException {
        return executeQuery(aConnection, aStatement, params, ResultAsLong.INSTANCE).longValue();
    }

    /**
     * Executes a single value result read statement on the database.
     *
     * @param aConnectionPool
     *        the connection pool to borrow a read connection to use for the database query
     *        from
     * @param aStatement
     *        the statement to execute
     * @param params
     *        the parameters of the statement; may be <code>null</code> if the statement has
     *        no parameters
     * @return the result of the query as long
     * @throws SQLException
     *         if some error occurs while requesting the database
     */
    public static long executeQueryAsLong(ConnectionPool aConnectionPool, String aStatement, Object[] params) throws SQLException {
        return executeQuery(aConnectionPool, aStatement, params, ResultAsLong.INSTANCE).longValue();
    }

    /**
     * Executes a single value result read statement on the database.
     *
     * @param aStatement
     *        the statement to execute
     * @param params
     *        the parameters of the statement; may be <code>null</code> if the statement has
     *        no parameters
     * @return the result of the query as long
     * @throws SQLException
     *         if some error occurs while requesting the database
     */
    public static long executeQueryAsLong(String aStatement, Object[] params) throws SQLException {
        return executeQuery(aStatement, params, ResultAsLong.INSTANCE).longValue();
    }

    /**
     * Executes a single value result read statement on the database.
     *
     * @param aStatement
     *        the statement to execute
     * @return the result of the query as long
     * @throws SQLException
     *         if some error occurs while requesting the database
     */
    public static long executeQueryAsLong(String aStatement) throws SQLException {
        return executeQueryAsLong(aStatement, null);
    }


    /**
     * Executes a single value result read statement on the database.
     *
     * @param aConnection
     *        the connection to use for the database query
     * @param aStatement
     *        the statement to execute
     * @param params
     *        the parameters of the statement; may be <code>null</code> if the statement has
     *        no parameters
     * @return the result of the query as String
     * @throws SQLException
     *         if some error occurs while requesting the database
     */
    public static String executeQueryAsString(Connection aConnection, String aStatement, Object[] params) throws SQLException {
        return executeQuery(aConnection, aStatement, params, ResultAsString.INSTANCE);
    }

    /**
     * Executes a single value result read statement on the database.
     *
     * @param aConnectionPool
     *        the connection pool to borrow a read connection to use for the database query
     *        from
     * @param aStatement
     *        the statement to execute
     * @param params
     *        the parameters of the statement; may be <code>null</code> if the statement has
     *        no parameters
     * @return the result of the query as String
     * @throws SQLException
     *         if some error occurs while requesting the database
     */
    public static String executeQueryAsString(ConnectionPool aConnectionPool, String aStatement, Object[] params) throws SQLException {
        return executeQuery(aConnectionPool, aStatement, params, ResultAsString.INSTANCE);
    }

    /**
     * Executes a single value result read statement on the database.
     *
     * @param aStatement
     *        the statement to execute
     * @param params
     *        the parameters of the statement; may be <code>null</code> if the statement has
     *        no parameters
     * @return the result of the query as String
     * @throws SQLException
     *         if some error occurs while requesting the database
     */
    public static String executeQueryAsString(String aStatement, Object[] params) throws SQLException {
        return executeQuery(aStatement, params, ResultAsString.INSTANCE);
    }

    /**
     * Executes a single value result read statement on the database.
     *
     * @param aStatement
     *        the statement to execute
     * @return the result of the query as String
     * @throws SQLException
     *         if some error occurs while requesting the database
     */
    public static String executeQueryAsString(String aStatement) throws SQLException {
        return executeQueryAsString(aStatement, null);
    }


    /**
     * Executes a single value result read statement on the database.
     *
     * @param aConnection
     *        the connection to use for the database query
     * @param aStatement
     *        the statement to execute
     * @param params
     *        the parameters of the statement; may be <code>null</code> if the statement has
     *        no parameters
     * @return the result of the query as Java Date
     * @throws SQLException
     *         if some error occurs while requesting the database
     */
    public static Date executeQueryAsJavaDate(Connection aConnection, String aStatement, Object[] params) throws SQLException {
        return executeQuery(aConnection, aStatement, params, ResultAsJavaDate.INSTANCE);
    }

    /**
     * Executes a single value result read statement on the database.
     *
     * @param aConnectionPool
     *        the connection pool to borrow a read connection to use for the database query
     *        from
     * @param aStatement
     *        the statement to execute
     * @param params
     *        the parameters of the statement; may be <code>null</code> if the statement has
     *        no parameters
     * @return the result of the query as Java Date
     * @throws SQLException
     *         if some error occurs while requesting the database
     */
    public static Date executeQueryAsJavaDate(ConnectionPool aConnectionPool, String aStatement, Object[] params) throws SQLException {
        return executeQuery(aConnectionPool, aStatement, params, ResultAsJavaDate.INSTANCE);
    }

    /**
     * Executes a single value result read statement on the database.
     *
     * @param aStatement
     *        the statement to execute
     * @param params
     *        the parameters of the statement; may be <code>null</code> if the statement has
     *        no parameters
     * @return the result of the query as Java Date
     * @throws SQLException
     *         if some error occurs while requesting the database
     */
    public static Date executeQueryAsJavaDate(String aStatement, Object[] params) throws SQLException {
        return executeQuery(aStatement, params, ResultAsJavaDate.INSTANCE);
    }

    /**
     * Executes a single value result read statement on the database.
     *
     * @param aStatement
     *        the statement to execute
     * @return the result of the query as Java Date
     * @throws SQLException
     *         if some error occurs while requesting the database
     */
    public static Date executeQueryAsJavaDate(String aStatement) throws SQLException {
        return executeQueryAsJavaDate(aStatement, null);
    }


    /**
     * Executes a single column result read statement on the database.
     *
     * @param aConnection
     *        the connection to use for the database query
     * @param aStatement
     *        the statement to execute
     * @param params
     *        the parameters of the statement; may be <code>null</code> if the statement has
     *        no parameters
     * @return the result of the query as list of strings (the first column only)
     * @throws SQLException
     *         if some error occurs while requesting the database
     */
	public static List<String> executeQueryAsStringList(Connection aConnection, String aStatement, Object[] params)
			throws SQLException {
        return executeQuery(aConnection, aStatement, params, ResultAsStringList.INSTANCE);
    }

    /**
     * Executes a single column result read statement on the database.
     *
     * @param aConnectionPool
     *        the connection pool to borrow a read connection to use for the database query
     *        from
     * @param aStatement
     *        the statement to execute
     * @param params
     *        the parameters of the statement; may be <code>null</code> if the statement has
     *        no parameters
     * @return the result of the query as list of strings (the first column only)
     * @throws SQLException
     *         if some error occurs while requesting the database
     */
	public static List<String> executeQueryAsStringList(ConnectionPool aConnectionPool, String aStatement,
			Object[] params)
			throws SQLException {
        return executeQuery(aConnectionPool, aStatement, params, ResultAsStringList.INSTANCE);
    }

    /**
     * Executes a single column result read statement on the database.
     *
     * @param aStatement
     *        the statement to execute
     * @param params
     *        the parameters of the statement; may be <code>null</code> if the statement has
     *        no parameters
     * @return the result of the query as list of strings (the first column only)
     * @throws SQLException
     *         if some error occurs while requesting the database
     */
	public static List<String> executeQueryAsStringList(String aStatement, Object[] params) throws SQLException {
        return executeQuery(aStatement, params, ResultAsStringList.INSTANCE);
    }

	/**
	 * Executes a single column result read statement on the database.
	 * 
	 * @param aStatement
	 *        the statement to execute
	 * @param params
	 *        the parameters of the statement; may be <code>null</code> if the statement has no
	 *        parameters
	 * @return the result of the query as list of {@link Integer} objects (the first column only)
	 * @throws SQLException
	 *         if some error occurs while requesting the database
	 */
	public static List<Integer> executeQueryAsIntList(String aStatement, Object[] params) throws SQLException {
		return executeQuery(aStatement, params, ResultAsIntList.INSTANCE);
	}

	/**
	 * Executes a single column result read statement on the database.
	 * 
	 * @param aStatement
	 *        the statement to execute
	 * @param params
	 *        the parameters of the statement; may be <code>null</code> if the statement has no
	 *        parameters
	 * @return the result of the query as list of object identifiers (the first column only)
	 * @throws SQLException
	 *         if some error occurs while requesting the database
	 */
	public static List<TLID> executeQueryAsIdList(String aStatement, Object[] params) throws SQLException {
		return executeQuery(aStatement, params, ResultAsIdList.INSTANCE);
	}
	
    /**
     * Executes a single column result read statement on the database.
     *
     * @param aStatement
     *        the statement to execute
     * @return the result of the query as list of strings (the first column only)
     * @throws SQLException
     *         if some error occurs while requesting the database
     */
	public static List<String> executeQueryAsStringList(String aStatement) throws SQLException {
        return executeQueryAsStringList(aStatement, null);
    }

	/**
	 * Executes a single column result read statement on the database.
	 * 
	 * @param aStatement
	 *        the statement to execute
	 * @return the result of the query as list of object identifiers (the first column only)
	 * @throws SQLException
	 *         if some error occurs while requesting the database
	 */
	public static List<TLID> executeQueryAsIdList(String aStatement) throws SQLException {
		return executeQueryAsIdList(aStatement, null);
	}
	

    /**
	 * Executes a multiple columns result read statement on the database.
	 * 
	 * @param sqlDialect
	 *        The {@link DBHelper} to use.
	 * @param aConnection
	 *        the connection to use for the database query
	 * @param aStatement
	 *        the statement to execute
	 * @param params
	 *        the parameters of the statement; may be <code>null</code> if the statement has no
	 *        parameters
	 * @param types
	 *        The types to fetch.
	 * @return the result of the query as list of string arrays
	 * @throws SQLException
	 *         if some error occurs while requesting the database
	 */
	public static List<Object[]> executeQueryAsMatrix(DBHelper sqlDialect, Connection aConnection, String aStatement,
			Object[] params, DBType[] types) throws SQLException {
		return executeQuery(aConnection, aStatement, params, new ResultAsMatrix(sqlDialect, types));
    }

    /**
	 * Executes a multiple columns result read statement on the database.
	 * 
	 * @param aConnectionPool
	 *        the connection pool to borrow a read connection to use for the database query from
	 * @param aStatement
	 *        the statement to execute
	 * @param params
	 *        the parameters of the statement; may be <code>null</code> if the statement has no
	 *        parameters
	 * @param types
	 *        The types to fetch.
	 * 
	 * @return the result of the query as list of string arrays
	 * @throws SQLException
	 *         if some error occurs while requesting the database
	 */
	public static List<Object[]> executeQueryAsMatrix(ConnectionPool aConnectionPool, String aStatement,
			Object[] params, DBType[] types) throws SQLException {
		return executeQuery(aConnectionPool, aStatement, params, new ResultAsMatrix(aConnectionPool.getSQLDialect(),
			types));
    }

    /**
	 * Executes a multiple columns result read statement on the database.
     * @param aStatement
	 *        the statement to execute
     * @param params
	 *        the parameters of the statement; may be <code>null</code> if the statement has no
	 *        parameters
	 * @param types
	 *        The types to fetch.
	 * 
	 * @return the result of the query as list of string arrays
	 * @throws SQLException
	 *         if some error occurs while requesting the database
	 */
	public static List<Object[]> executeQueryAsMatrix(String aStatement, Object[] params, DBType[] types) throws SQLException {
		return executeQuery(aStatement, params, new ResultAsMatrix(ConnectionPoolRegistry.getDefaultConnectionPool()
			.getSQLDialect(), types));
    }

    /**
	 * Executes a multiple columns result read statement on the database.
     * @param aStatement
	 *        the statement to execute
	 * @param types
	 *        The types to fetch.
	 * 
	 * @return the result of the query as list of string arrays
	 * @throws SQLException
	 *         if some error occurs while requesting the database
	 */
    public static List<Object[]> executeQueryAsMatrix(String aStatement, DBType[] types) throws SQLException {
		return executeQueryAsMatrix(aStatement, null, types);
    }


    /**
     * Executes a multiple columns result read statement on the database.
     *
     * @param aConnection
     *        the connection to use for the database query
     * @param aStatement
     *        the statement to execute
     * @param params
     *        the parameters of the statement; may be <code>null</code> if the statement has
     *        no parameters
     * @return the result of the query as table inclusive column headers
     * @throws SQLException
     *         if some error occurs while requesting the database
     */
    public static String[][] executeQueryAsTable(Connection aConnection, String aStatement, Object[] params) throws SQLException {
        return executeQuery(aConnection, aStatement, params, ResultAsTable.INSTANCE);
    }

    /**
     * Executes a multiple columns result read statement on the database.
     *
     * @param aConnectionPool
     *        the connection pool to borrow a read connection to use for the database query
     *        from
     * @param aStatement
     *        the statement to execute
     * @param params
     *        the parameters of the statement; may be <code>null</code> if the statement has
     *        no parameters
     * @return the result of the query as table inclusive column headers
     * @throws SQLException
     *         if some error occurs while requesting the database
     */
    public static String[][] executeQueryAsTable(ConnectionPool aConnectionPool, String aStatement, Object[] params) throws SQLException {
        return executeQuery(aConnectionPool, aStatement, params, ResultAsTable.INSTANCE);
    }

    /**
     * Executes a multiple columns result read statement on the database.
     *
     * @param aStatement
     *        the statement to execute
     * @param params
     *        the parameters of the statement; may be <code>null</code> if the statement has
     *        no parameters
     * @return the result of the query as table inclusive column headers
     * @throws SQLException
     *         if some error occurs while requesting the database
     */
    public static String[][] executeQueryAsTable(String aStatement, Object[] params) throws SQLException {
        return executeQuery(aStatement, params, ResultAsTable.INSTANCE);
    }

    /**
     * Executes a multiple columns result read statement on the database.
     *
     * @param aStatement
     *        the statement to execute
     * @return the result of the query as table inclusive column headers
     * @throws SQLException
     *         if some error occurs while requesting the database
     */
    public static String[][] executeQueryAsTable(String aStatement) throws SQLException {
        return executeQueryAsTable(aStatement, null);
    }


    /**
     * Executes a single column result read statement on the database.
     *
     * @param aConnection
     *        the connection to use for the database query
     * @param aStatement
     *        the statement to execute
     * @param params
     *        the parameters of the statement; may be <code>null</code> if the statement has
     *        no parameters
     * @param koType
     *        the koType of the objects to get wrappers for; may be <code>null</code>
     * @return the result of the query as list of wrappers (the first column only)
     * @throws SQLException
     *         if some error occurs while requesting the database
     */
	public static <T extends Wrapper> List<T> executeQueryAsWrapperList(Connection aConnection, String aStatement,
			Object[] params, String koType, Class<T> expectedType) throws SQLException {
		return executeQuery(aConnection, aStatement, params, new ResultAsWrapperList<>(koType, expectedType));
    }

    /**
     * Executes a single column result read statement on the database.
     *
     * @param aConnectionPool
     *        the connection pool to borrow a read connection to use for the database query
     *        from
     * @param aStatement
     *        the statement to execute
     * @param params
     *        the parameters of the statement; may be <code>null</code> if the statement has
     *        no parameters
     * @param koType
     *        the koType of the objects to get wrappers for; may be <code>null</code>
     * @return the result of the query as list of wrappers (the first column only)
     * @throws SQLException
     *         if some error occurs while requesting the database
     */
	public static <T extends Wrapper> List<T> executeQueryAsWrapperList(ConnectionPool aConnectionPool,
			String aStatement, Object[] params, String koType, Class<T> expectedType) throws SQLException {
		return executeQuery(aConnectionPool, aStatement, params, new ResultAsWrapperList<>(koType, expectedType));
    }

    /**
     * Executes a single column result read statement on the database.
     *
     * @param aStatement
     *        the statement to execute
     * @param params
     *        the parameters of the statement; may be <code>null</code> if the statement has
     *        no parameters
     * @param koType
     *        the koType of the objects to get wrappers for; may be <code>null</code>
     * @return the result of the query as list of wrappers (the first column only)
     * @throws SQLException
     *         if some error occurs while requesting the database
     */
	public static <T extends Wrapper> List<T> executeQueryAsWrapperList(String aStatement, Object[] params,
			String koType, Class<T> expectedType) throws SQLException {
		return executeQuery(aStatement, params, new ResultAsWrapperList<>(koType, expectedType));
    }

    /**
     * Executes a single column result read statement on the database.
     *
     * @param aStatement
     *        the statement to execute
     * @param koType
     *        the koType of the objects to get wrappers for; may be <code>null</code>
     * @return the result of the query as list of wrappers (the first column only)
     * @throws SQLException
     *         if some error occurs while requesting the database
     */
	public static <T extends Wrapper> List<T> executeQueryAsWrapperList(String aStatement, String koType,
			Class<T> expectedType) throws SQLException {
		return executeQueryAsWrapperList(aStatement, null, koType, expectedType);
    }

    /**
     * Sets the parameters from the given vector to the prepared statement.
     *
     * @param aPstm
     *        the prepared statement to set the parameters
     * @param params
     *        the parameter array to set
     * @throws SQLException
     *         if some error occurs while requesting the database
     */
    public static void setVector(PreparedStatement aPstm, Object[] params) throws SQLException {
        for (int i = 0, j = 1; i < params.length; i++) {
            aPstm.setObject(j++, params[i]);
        }
    }



    /**
     * Returns the current time of the DB server in milliseconds.
     *
     * @return the difference, measured in milliseconds, between the current time of the DB
     *         and midnight, January 1, 1970 UTC.
     * @see System#currentTimeMillis()
     * @throws SQLException
     *         if some error occurs while requesting the database
     */
    public static long currentTimeMillis() throws SQLException {
        DBHelper sqlDialect = ConnectionPoolRegistry.getDefaultConnectionPool().getSQLDialect();
        String statement = "SELECT " + sqlDialect.fnNow() + sqlDialect.fromNoTable();
        return DBUtil.executeQuery(statement, null, DBUtil.ResultAsTimeStamp.INSTANCE).getTime();
    }

    /**
     * Returns the current time of the DB server in milliseconds.
     *
     * @return the difference, measured in milliseconds, between the current time of the DB
     *         and midnight, January 1, 1970 UTC.
     * @see System#currentTimeMillis()
     * @throws SQLException
     *         if some error occurs while requesting the database
     */
    public static long currentTimeMillis(Connection aConnection, DBHelper aSQLDialect) throws SQLException {
        String statement = "SELECT " + aSQLDialect.fnNow() + aSQLDialect.fromNoTable();
		return DBUtil.executeQuery(aConnection, statement, null, DBUtil.ResultAsTimeStamp.INSTANCE)
			.getTime();
    }

}
