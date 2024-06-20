/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.sql;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.CharBuffer;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.Log;
import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.Logger;
import com.top_logic.basic.Settings;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.TLID;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.col.TupleFactory.Tuple;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.DefaultConfigConstructorScheme;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.FileBasedBinaryContent;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.binary.AbstractBinaryData;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.basic.io.binary.MemoryBinaryContent;
import com.top_logic.basic.io.character.CharacterContent;
import com.top_logic.basic.time.CalendarUtil;

/** This class is a generic helper class for JDBC drivers. 
 * <p>
 *  It helpes working around this _small_ differences in 
 *  those SQL-drivers.
 *  
 * </p>
 * @author  <a href=mailto:kha@top-logic.com>Klaus Halfmann</a>
 */
public class DBHelper implements ConfiguredInstance<DBHelper.Config> {

	/**
	 * Configuration of the {@link DBHelper}.
	 * 
	 * @since 5.8.0
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends PolymorphicConfiguration<DBHelper> {

		/**
		 * @see #getAdjustFromConnection()
		 */
		String ADJUST_FROM_CONNECTION = "adjust-from-connection";

		/** Name of the property defined by {@link #getMaxNumberBatchParameter()}. */
		String MAX_NUMBER_PATCH_PARAMETER_NAME = "max-number-batch-parameter";

		/**
		 * Default value of number of parameters that the can be contained in one batch.
		 * 
		 * <p>
		 * Note: MySQL can handle (2^16)-1 "?". To ensure functionality a less value is used.
		 * </p>
		 */
		int MAX_NUMBER_BATCH_PARAMETER = 60000;

		/**
		 * @see #getVarcharLimit()
		 */
		String VARCHAR_LIMIT = "varchar-limit";

		/**
		 * @see #getNVarcharLimit()
		 */
		String NVARCHAR_LIMIT = "nvarchar-limit";

		/**
		 * Whether to adjust settings by querying the database.
		 * 
		 * <p>
		 * Automatic adjustment can be disabled, e.g. if the SQL settings are configured for a
		 * connection pool.
		 * </p>
		 * 
		 * @see AbstractConfiguredConnectionPool.Config#getSQLDialect()
		 */
		@Name(ADJUST_FROM_CONNECTION)
		@BooleanDefault(true)
		boolean getAdjustFromConnection();

		/**
		 * Defines the number of parameter that can occur in one batch execution.
		 */
		@IntDefault(MAX_NUMBER_BATCH_PARAMETER)
		@Name(MAX_NUMBER_PATCH_PARAMETER_NAME)
		int getMaxNumberBatchParameter();

		/**
		 * @see #getMaxNumberBatchParameter()
		 */
		void setMaxNumberBatchParameter(int value);

		/**
		 * The number of characters supported in a VARCHAR type (depends on the database
		 * characterset settings).
		 */
		@Name(VARCHAR_LIMIT)
		@LongDefault(Long.MAX_VALUE)
		long getVarcharLimit();

		/**
		 * @see #getVarcharLimit()
		 */
		void setVarcharLimit(long value);

		/**
		 * The number of characters supported in a NVARCHAR type (depends on the database
		 * characterset settings).
		 */
		@Name(NVARCHAR_LIMIT)
		@LongDefault(Long.MAX_VALUE)
		long getNVarcharLimit();

		/**
		 * @see #getNVarcharLimit()
		 */
		void setNVarcharLimit(long value);

	}

	private static ThreadLocal<Calendar> SYSTEM_CALENDAR = new ThreadLocal<>();

	/**
	 * Retrieve a matching {@link DBHelper} for the given {@link Connection}.
	 * 
	 * <p>
	 * This will extract the driver info from the connection an create a subclass of DBHelper, in
	 * case the vendor is not know a generic class is returned.
	 * </p>
	 */
	public static DBHelper getDBHelper(Connection con)
        throws SQLException
	{
		return createDefaultInstance(inspectDialect(con)).init(con);
	}

	/**
	 * Guesses the {@link DBHelper} implementation for the {@link DatabaseMetaData} of the given
	 * connection.
	 */
	public static Class<? extends DBHelper> inspectDialect(Connection con) throws SQLException {
		DatabaseMetaData meta = con.getMetaData();

        String driver  = meta.getDriverName();
        // String version = meta.getDriverVersion();
        if (driver.indexOf("MySQL") >= 0) {   
            int major = meta.getDatabaseMajorVersion();
            int minor = meta.getDatabaseMinorVersion();
            String version = meta.getDatabaseProductVersion();
			if (major < 5 // just to be sure
				|| (major == 5 && minor < 5)) { // can't check for last Version, sorry
				throw new SQLException("Need at least MySQL 5.5 (is " + version + ")");
            }
			return MySQLHelper.getInstance(con);
        }
		if (driver.indexOf("MariaDB") >= 0) {
			return MySQLHelper.getInstance(con);
		}
        if (driver.startsWith("Oracle")) {
			int driverVersionMajor = meta.getDriverMajorVersion();
            // int minor = meta.getDriverMinorVersion();
			// String version = meta.getDriverVersion();
			int dbVersion = meta.getDatabaseMajorVersion();
			if (driverVersionMajor <= 9) {
                throw new SQLException("Oracle 9 and lower are not supported");
            }
			if (driverVersionMajor >= 12) {
				if(dbVersion < 12) {
					return Oracle12toLowerHelper.class;
				}
				return Oracle12Helper.class;
            }
			return Oracle10Helper.class;
        }
		if (driver.indexOf("Microsoft") >= 0 || driver.indexOf("SQLServer") >= 0 || driver.indexOf("jTDS") >= 0) {
			int dbMajorVersion = meta.getDatabaseMajorVersion();
			if (dbMajorVersion <= 9) {
				return MSSQLHelper90.class;
			}
			return MSSQLHelper.class;
        }
        if (driver.startsWith("JDBC-ODBC Bridge")) { 
            String product = meta.getDatabaseProductName();
            if (product.startsWith("ACCESS")) {
				return MSAccessHelper.class;
            }
        }
		if (driver.contains("IBM")) {
			return DB2Helper.class;
        }
		if (driver.contains("H2")) {
			return H2Helper.class;
		}
		if (driver.contains("PostgreSQL")) {
			return PostgreSQLHelper.class;
		}
        /*
        if (driver.startsWith("Informix")) {
            return InformixHelper.INSTANCE;
        }
        */
        Logger.warn(driver + " not supported by DBHelper, trying defaults", 
                    DBHelper.class);
		return DBHelper.class; // fall back to generic case
    }

	/**
	 * Creates {@link DBHelper} implementation with all configuration set to its default values.
	 * 
	 * @param type
	 *        The {@link DBHelper} type.
	 * @return The new {@link DBHelper} instance.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends DBHelper> T createDefaultInstance(Class<T> type) {
		try {
			return (T) DefaultConfigConstructorScheme.getFactory(type).createDefaultInstance();
		} catch (ConfigurationException ex) {
			throw new RuntimeException("Unable to allocate default '" + type.getName() + "' instance.", ex);
		}
	}

	/**
	 * Adjust this instance to the settings of the target database.
	 * 
	 * @param con
	 *        {@link Connection} to the target database.
	 * @return This instance.
	 */
	final DBHelper init(Connection con) throws SQLException {
		if (_config.getAdjustFromConnection()) {
			return internalInit(con);
		}
		return this;
	}

	/**
	 * @param con
	 *        {@link Connection} to the target database.
	 * @return The final instance to use.
	 * @throws SQLException
	 *         If accessing the database fails.
	 * @see #init(Connection)
	 */
	protected DBHelper internalInit(Connection con) throws SQLException {
		return this;
	}

	private Format _escapeFormat = new Format() {

		@Override
		public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
			escape(toAppendTo, obj.toString());
			return toAppendTo;
		}

		@Override
		public Object parseObject(String source, ParsePosition pos) {
			throw new UnsupportedOperationException();
		}

	};

	private Format _toStringFormat = new Format() {

		@Override
		public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
			toAppendTo.append(obj);
			return toAppendTo;
		}

		@Override
		public Object parseObject(String source, ParsePosition pos) {
			throw new UnsupportedOperationException();
		}

	};

	private Format _idFormat = new Format() {

		@Override
		public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
			toAppendTo.append(((TLID) obj).toStorageValue());
			return toAppendTo;
		}

		@Override
		public Object parseObject(String source, ParsePosition pos) {
			throw new UnsupportedOperationException();
		}

	};

    /** 
     * Return true when there is a chance that a retry of statemets will succeed.
     * 
     * @param sqlX the Exception for an SQLStatement we may retry.
     */
    public boolean canRetry(SQLException sqlX) {
        
        // Some special cases that will happen with WAS 5.1 only 
        // String clName = sqlX.getClass().getName();         // Worst case
        // return clName.endsWith("ObjectClosedException");   // WAS 5.1
        return false;
    }

    /** 
     * Return the default number of retries that will make sense.
     */
    public int retryCount() {
        return 3;
    }

	public static DBHelper getDBHelper(ConnectionPool connectionPool) throws SQLException {
		return connectionPool.getSQLDialect();
	}

    /** 
     * Perform checks required for the DBHelper or TopLogic to function on aCon.
     */
    public final void check(Connection aCon) {
		try (Statement stm = aCon.createStatement()) {
            internalCheck(stm);
        } catch (SQLException sqx) {
            Logger.error("Failed to check", sqx, this);
        }
    }

    /** 
     * Perform checks required for the DBHelper or TopLogic to function on aStm.
     * 
     * @param aStm The {@link Statement} to perform the check on.
     * 
     * @throws SQLException If access to the database fails.
     */
    protected void internalCheck(Statement aStm) throws SQLException {
        Logger.info("No database check implemented for '" + this.getClass().getSimpleName() + "'.", this);
    }

	private final Config _config;

	private final int _maxNumberBatchParameter;

	/** Place holder for null Values */
	private static final String NULL_PLACEHOLDER = "#";

	/**
	 * Format that interprets <code>boolean</code> values as "1" and "0", since boolean literals are
	 * not supported in most SQL dialects.
	 */
	private static final Format SQL_BOOLEAN_FORMAT = new Format() {
		@Override
		public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
			if (obj == null) {
				toAppendTo.append("NULL");
				return toAppendTo;
			}

			if (obj instanceof Boolean) {
				toAppendTo.append(((Boolean) obj).booleanValue() ? "1" : "0");
				return toAppendTo;
			}

			toAppendTo.append(obj.toString());
			return toAppendTo;
		}

		@Override
		public Object parseObject(String source, ParsePosition pos) {
			throw new UnsupportedOperationException();
		}
	};
	
	/**
	 * Creates a {@link DBHelper} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public DBHelper(InstantiationContext context, Config config) {
		_config = config;
		_maxNumberBatchParameter = config.getMaxNumberBatchParameter();
	}

	@Override
	public Config getConfig() {
		return _config;
	}

    /** Some Databases do not support this :(. */ 
    public String fnNow()
    {
        return "{fn NOW()}";
    }

    /** Some Databases do not support this :(. */ 
    public String fnCurrDate()
    {
        return "{fn CURDATE()}";
    }

    /** Some Databases do not support this :(. */ 
    public String fnCurrTime()
    {
        return "{fn CURTIME()}";
    }
    
    /** 
     * Truncate x to y number of decimal places. 
     * 
     * @see "http://java.sun.com/j2se/1.3/docs/guide/jdbc/spec/jdbc-spec.frame11.html" 
     */ 
    public String fnTruncate(String x, int y)
    {
        return "{fn TRUNCATE(" + x + ',' + y + ")}";
    }

    /** 
     * Round x to y number of decimal places.
     * 
     * @see "http://java.sun.com/j2se/1.3/docs/guide/jdbc/spec/jdbc-spec.frame11.html" 
     */ 
    public String fnRound(String x, int y)
    {
        return "{fn ROUND(" + x + ',' + y + ")}";
    }

    /** There is no escape Sequence for this */ 
    public String fnBitAnd(String expr1, String expr2)
    {
        throw new UnsupportedOperationException("There is no generic BitAdd fucntion for " + this.getClass().getSimpleName());
    }

    /**
     * Return the name of the current schema for aCon.
     * 
     * @return null in case DB is unaware of schema .
     */
    public String getCurrentSchema(Connection connection) throws SQLException  {
        return null;
    }
    
   /** 
    * True when the id returned by {@link #prepareSerial(String, Connection)}
    * must be inserted.
    */
   public boolean isSerialNeeded() 
        throws SQLException
   {
       return false;    // only Oracle needs this (using Sequences)
   }

   /** 
    * Call this Function to prepare a serial number.
     *
     * With Oracle we might use Sequences.
     * 
     * @param tableName name of table we want a unique ID for.
     */
    public long prepareSerial(String tableName, Connection connection) 
         throws SQLException
    {
        return 0;   // Dont know how to care for serial numbers
    }

    /** 
     * Call this Function to get the serial number after doing the actual insert.
     * 
     * You must set {@link Statement#RETURN_GENERATED_KEYS} on {@link Statement#executeUpdate(String, int)})
     * or {@link Connection#prepareStatement(String, int)}. Otherwise this may not work for some JDBC-Drivers.
     */
    public long postcareSerial(long id, Statement stm)
        throws SQLException
    {
        // return id;
        // might not work with all DBs though
		try (ResultSet keys = stm.getGeneratedKeys()) {
            if (keys.next())
                return keys.getLong(1);
            else {
                // Happens with jTDS Driver which wont work without this Flas
                throw new SQLException("No generatedKeys. Did you use Statement.RETURN_GENERATED_KEYS ?");
            }
        }
    }
    
    /** Some Drivers do not implement this (or not as expected) :-( */
    public String getColumnTypeName(ResultSetMetaData meta, int column) 
                throws SQLException {
        return meta.getColumnClassName(column);
    }
    
	/**
	 * Read input file with SQL statements and fire them to a database
	 * 
	 * @param aReader
	 *        {@link Reader} to read SQL statements separated by ';'
	 * @param aStmt
	 *        JDBC statement to be used to fire SQL to the database.
	 * 
	 * @throws IOException
	 *         If reading from the given {@link Reader} fails.
	 * @throws SQLException
	 *         If the database fails executing the given statements.
	 *         
	 * @deprecated Use {@link SQLLoader#executeSQL(Reader)}
	 */
    @Deprecated
    public final void fireSQLStatements(Reader aReader, Statement aStmt) throws SQLException, IOException {
    	fireSQLStatements(aReader, aStmt, false);
    }

	/**
	 * Read input file with SQL statements and fire them to a database. Logs to {@link Logger}.
	 *         
	 * @deprecated Use {@link SQLLoader#executeSQL(Reader)}
	 */
    @Deprecated
	public final void fireSQLStatements(Reader aReader, Statement aStmt, boolean ignoreProblems)
			throws SQLException, IOException {
		fireSQLStatements(aReader, aStmt, new LogProtocol(DBHelper.class), ignoreProblems);
	}
	
	/**
	 * Read input file with SQL statements and fire them to a database
	 * 
	 * @param aReader
	 *        {@link Reader} to read SQL statements separated by ';'
	 * @param aStmt
	 *        JDBC statement to be used to fire SQL to the database.
	 * @param log
	 *        Log to log problems to.
	 * @param ignoreProblems
	 *        Whether all statements should be fired independently of errors that occurred during
	 *        processing. If one of the statements fails, the first {@link SQLException} that was
	 *        encountered is re-thrown as result of this call.
	 * 
	 * @throws IOException
	 *         If reading from the given {@link Reader} fails.
	 * @throws SQLException
	 *         If the database fails executing the given statements.
	 *         
	 * @deprecated Use {@link SQLLoader#executeSQL(Reader)}
	 */
    @Deprecated
	public final void fireSQLStatements(Reader aReader, Statement aStmt, Log log, boolean ignoreProblems)
			throws SQLException, IOException {
		new SQLLoader(aStmt.getConnection()).setLog(log).setContinueOnError(ignoreProblems).executeSQL(aReader);
    }

	/**
	 * Executes the given SQL script within the given transaction.
	 * 
	 * @param sql
	 *        The SQL script containing multiple SQL statements.
	 * @param connection
	 *        The transaction to execute the script in.
	 * @throws SQLException
	 *         If executing a statement in the script fails.
	 * 
	 * @deprecated Use {@link SQLLoader#executeSQL(String)}
	 */
    @Deprecated
	public final void fireSQLStatements(String sql, Connection connection) throws SQLException {
		try {
			fireSQLStatements(new StringReader(sql), connection);
		} catch (IOException ex) {
			throw new UnreachableAssertion("Reading from string must not fail.", ex);
		}
	}

    /** read input file with SQL statements and fire them to a database
     *
     * @param   aReader   Reader, e.g. a FileReader
     * @param   aConn     JDBC connection   
	 *         
	 * @deprecated Use {@link SQLLoader#executeSQL(Reader)}
     */
    @Deprecated
    public final void fireSQLStatements(Reader aReader, Connection aConn) throws SQLException, IOException {
		try (Statement theStmt = aConn.createStatement()) {
			fireSQLStatements(aReader, theStmt);
		}
    }
 
	/** 
	 * Get the OutputStream to a BLOB column identified by the given SQL query.
	 *
	 * <p>Note that this is the default implementation that should work with
	 * most databases. Unfortunately there are some databases/driver combinations 
	 * (like Oracle with THIN JDBC driver) which will not store BLOBs in a 
	 * standard JDBC manner via 
	 * {@link java.sql.PreparedStatement#setBinaryStream(int, java.io.InputStream, int)}.</p>
	 * 
	 * <p>
	 * <b>Preconditions:</b><br/>
	 * <ul> 
	 * <li>
	 * Set auto commit mode to false via 
	 * {@link java.sql.Connection#setAutoCommit(boolean)}
	 * </li>
	 * <li>
	 * Only the BLOB entry should have the place holder 
	 * &quot;?&quot; for setting parameter via a PreparedStatement, the other 
	 * paramters should be set directly in this statement string like in normal 
	 * INSERT/UPDATE statements
	 * </li>
	 * <li>
	 * One of the columns or a combination of columns provided in the 
	 * INSERT/UPDATE statement has to be the primary key. Automatic
	 * generation of a unique ID will not work for storing a new BLOB
	 * with this method.
	 * </li>
	 * </ul>
	 * </p>
	 * 
	 * <p>
	 * <b>Postconditions:</b><br/>
	 * <ul>
	 * <li>
	 * Close the OutputStream after writing BLOB.
	 * </li>
	 * <li>
	 * Commit changes via {@link java.sql.Connection#commit()}
	 * </li>
	 * <li>
	 * Set auto commit mode to true via  {@link java.sql.Connection#setAutoCommit(boolean)}
	 * </li>
	 * </ul>
	 * </p>
	 * 
	 * @param   connection    The connection to the database
	 * @param   stmt    The SQL statement identifying the BLOB
	 * @param   maxSize The maximum size of the BLOB
	 * 
	 * @throws SQLException in case of a database error
	 */
	public OutputStream getBLOBOutputStream(PooledConnection connection, String stmt, int maxSize) 
		throws java.sql.SQLException, java.io.IOException {
            
		PreparedQuery pq = new PreparedQuery(connection, stmt);
		QueryPipedStreams qps = new QueryPipedStreams(pq, 1 , maxSize);
		return qps.getOutputStream();
	}

	/**
	 * Reads a CLOB value from a result set at the given index
	 * 
	 * @param result
	 *        the database result set to read values from
	 * @param index
	 *        the index in the given result set, which stores the CLOB value
	 * 
	 * @return the CLOB value at index <code>index</code> in the given
	 *         <code>resultSet</code>
	 * 
	 * @throws SQLException
	 *         if accessing the result fails.
	 */
	public String getClobValue(ResultSet result, int index) throws SQLException {
		try {
			Clob clob = result.getClob(index);
			if (clob == null) {
				return null;
			}
			if (clob.length() + 1 > Integer.MAX_VALUE) {
				throw new SQLException("Cannot fetch extremely long CLOB value as string.");
			}
			int length = (int) clob.length();

			// Note: The allocated buffer must be one char greater than the length of the string
			// being read, because the reader will not attempt to read a character, after the buffer
			// is full and never reach the end of stream condition below.
			CharBuffer buffer = CharBuffer.allocate(length + 1);
			try (Reader reader = clob.getCharacterStream()) {
				while (true) {
					int direct = reader.read(buffer);
					if (direct < 0) {
						break;
					}
				}
			}
			buffer.flip();
			return buffer.toString();
		} catch (IOException ex) {
			throw (SQLException) new SQLException("Reading stream attribute failed.").initCause(ex);
		}
	}

	/**
	 * Reads a BLOB value from a result set at the given index
	 * 
	 * @param result
	 *        the database result set to read values from
	 * @param index
	 *        the index in the given result set, which stores the CLOB value
	 * 
	 * @return the BLOB content at index <code>index</code> in the given
	 *         <code>resultSet</code>
	 * 
	 * @throws SQLException
	 *         if accessing the result fails.
	 */
	public BinaryData getBlobValue(ResultSet result, int index) throws SQLException {
		final Blob blob = result.getBlob(index);
		if (blob == null) {
			return null;
		}
		try {
			final BinaryContent content;
			final long length = blob.length();
			if (length > 4096) {
				try {
					File tmp = File.createTempFile("blob", ".data", Settings.getInstance().getTempDir());
					try (InputStream in = blob.getBinaryStream()) {
						FileUtilities.copyToFile(in, tmp);
					}
					content = FileBasedBinaryContent.createBinaryContent(tmp);
				} catch (IOException ex) {
					throw new SQLException("Error materializing blob value.", ex);
				}
			} else {
				content = new MemoryBinaryContent(blob.getBytes(1, (int) length), "BLOB");
			}
			return new AbstractBinaryData() {
				@Override
				public String getName() {
					return "BLOB";
				}

				@Override
				public InputStream getStream() throws IOException {
					return content.getStream();
				}

				@Override
				public long getSize() {
					return length;
				}

				@Override
				public String getContentType() {
					return CONTENT_TYPE_OCTET_STREAM;
				}
			};
		} finally {
			blob.free();
		}
	}
	
	/**
	 * Append the DB specific type declaration for the given type with the given modifiers and no
	 * default value to the given {@link Appendable}.
	 * 
	 * @param result
	 *        The {@link Appendable} to append to.
	 * @param sqlType
	 *        The SQL type code according to constants defined in {@link Types}.
	 * @param columnName
	 *        Name of the column for which the DB type must be append.
	 * @param size
	 *        The size modifier, or <code>0</code> if the size modifier is not applicable.
	 * @param precision
	 *        The precision modifier, or <code>-1</code>, if the precision modifier is not
	 *        applicable.
	 * @param mandatory
	 *        Whether <code>null</code> could not be stored.
	 * @param binary
	 *        The binary modifier.
	 */
	public final void appendDBType(Appendable result, DBType sqlType, String columnName, long size, int precision, boolean mandatory, boolean binary) {
		appendDBType(result, sqlType, columnName, size, precision, mandatory, binary, null);
	}

	/**
	 * Append the DB specific type declaration for the given type with the given modifiers and no
	 * default value to the given {@link Appendable}.
	 * 
	 * @param result
	 *        The {@link Appendable} to append to.
	 * @param sqlType
	 *        The SQL type code according to constants defined in {@link Types}.
	 * @param columnName
	 *        Name of the column for which the DB type must be append.
	 * @param size
	 *        The size modifier, or <code>0</code> if the size modifier is not applicable.
	 * @param precision
	 *        The precision modifier, or <code>-1</code>, if the precision modifier is not
	 *        applicable.
	 * @param mandatory
	 *        Whether <code>null</code> could not be stored.
	 * @param binary
	 *        The binary modifier.
	 * @param defaultValue
	 *        Default for the column. <code>null</code> defaults are not set.
	 */
	public final void appendDBType(Appendable result, DBType sqlType, String columnName, long size, int precision,
			boolean mandatory, boolean binary, Object defaultValue) {
		try {
			internalAppendDBType(result, sqlType, size, precision, mandatory, binary, false, columnName);
			appendDefaultValue(result, sqlType, defaultValue);
			appendMandatory(result, mandatory);
		} catch (IOException ex) {
			throw wrapIOException(ex);
		}
	}

	/**
	 * Appends the "DEFAULT" annotation of the type definition.
	 */
	protected void appendDefaultValue(Appendable result, DBType sqlType, Object defaultValue) throws IOException {
		if (defaultValue != null) {
			result.append(StringServices.BLANK_CHAR);
			result.append("DEFAULT ");
			literal(result, sqlType, defaultValue);
		}
	}

	/**
	 * Appends the mandatory annotation of the type definition.
	 */
	protected void appendMandatory(Appendable result, boolean mandatory) throws IOException {
		result.append(StringServices.BLANK_CHAR);
		if (mandatory) {
			result.append(notNullSpec());
		} else {
			result.append(nullSpec());
		}
	}

	/**
	 * Appends the part of the column type that has to do with the actual type definition.
	 */
	protected final void internalAppendDBType(Appendable result, DBType dbType, long size, int precision,
			boolean mandatory, boolean binary, boolean castContext, String columnName) throws IOException {
		switch (adjustType(dbType, size, precision, mandatory, binary)) {
        case BOOLEAN:
        	appendBooleanType(result, mandatory);
        	return;
        
        case BYTE:
        	appendByteType(result, mandatory);
        	return;
        	
        case SHORT:
        	appendShortType(result, mandatory);
        	return;

        case INT:
        	appendIntType(result, mandatory);
        	return;

        case LONG:
        	appendLongType(result, mandatory);
        	return;

			case ID:
				appendIDType(result, columnName, mandatory, castContext);
				return;

        case FLOAT:
        	appendFloatType(result, mandatory);
        	return;

        case DOUBLE:
        	appendDoubleType(result, mandatory);
        	return;
        	
        case DECIMAL:
        	appendDecimalType(result, size, precision);
        	return;
        
        case CHAR:
        	appendCharType(result, mandatory, binary, castContext);
        	return;

        case STRING:
			appendStringType(result, columnName, size, mandatory, binary, castContext);
        	return;
        
        case DATE:
        	appendDateType(result, mandatory);
        	return;

        case TIME:
        	appendTimeType(result, mandatory);
        	return;

        case DATETIME:
        	appendDateTimeType(result, mandatory);
        	return;
        
        case CLOB:
        	appendClobType(result, columnName, size, mandatory, binary, castContext);
        	return;

        case BLOB:
        	appendBlobType(result, size, mandatory);
        	return;
        }
        
        throw new IllegalArgumentException("Undefined DB type '" + dbType + "'.");
	}

	/**
	 * Adjusts the given {@link DBType} (the the database-independent specification) to the concrete
	 * {@link DBType} used for the specific RDBMS.
	 * 
	 * @param dbType
	 *        The database-independent type specification.
	 * @param size
	 *        The size modifier, or <code>0</code> if the size modifier is not applicable.
	 * @param precision
	 *        The precision modifier, or <code>-1</code>, if the precision modifier is not
	 *        applicable.
	 * @param mandatory
	 *        Whether <code>null</code> could not be stored.
	 * @param binary
	 *        The binary modifier.
	 * @return The concrete {@link DBType} to use.
	 */
	protected DBType adjustType(DBType dbType, long size, int precision, boolean mandatory, boolean binary) {
		switch (dbType) {
			case STRING: {
				if (size <= varcharLimit(binary)) {
					return DBType.STRING;
				} else {
					return DBType.CLOB;
				}
			}

			default: {
				return dbType;
			}
		}
	}

	/**
	 * Whether the concrete column type used for the given abstract type specification can be
	 * compared in the specific RDBMS.
	 * 
	 * @param dbType
	 *        The database-independent type specification.
	 * @param size
	 *        The size modifier, or <code>0</code> if the size modifier is not applicable.
	 * @param precision
	 *        The precision modifier, or <code>-1</code>, if the precision modifier is not
	 *        applicable.
	 * @param mandatory
	 *        Whether <code>null</code> could not be stored.
	 * @param binary
	 *        The binary modifier.
	 * @return The concrete {@link DBType} to use.
	 * 
	 * @see #canBlobCompare()
	 * @see #canClobCompare()
	 * @see #adjustType(DBType, long, int, boolean, boolean)
	 */
	public boolean isComparable(DBType dbType, long size, int precision, boolean mandatory, boolean binary) {
		switch (adjustType(dbType, size, precision, mandatory, binary)) {
			case BLOB: {
				return canBlobCompare();
			}
			case CLOB: {
				return canClobCompare();
			}
			default: {
				return true;
			}
		}
	}

	/**
	 * Whether {@link DBType#BLOB} types can be compared.
	 */
	protected boolean canClobCompare() {
		return false;
	}

	/**
	 * Whether {@link DBType#CLOB} types can be compared.
	 */
	protected boolean canBlobCompare() {
		return false;
	}

	/**
	 * Append a DB-specific type that can store a <code>boolean</code> value to
	 * the given {@link Appendable}.
	 */
	protected void appendBooleanType(Appendable result, boolean mandatory) throws IOException {
    	result.append("BOOLEAN");
	}

	/**
	 * Append a DB-specific type that can store a <code>byte</code> value to
	 * the given {@link Appendable}.
	 */
	protected void appendByteType(Appendable result, boolean mandatory) throws IOException {
		result.append(internalGetDBType(Types.TINYINT, false));
	}

	/**
	 * Append a DB-specific type that can store a single <code>char</code> value to the given
	 * {@link Appendable}.
	 * 
	 * @param mandatory
	 *        Whether <code>null</code> could not be stored.
	 * @param binary
	 *        The binary modifier.
	 * @param castContext
	 *        Whether this method is called to added type to "cast" expression.
	 */
	protected void appendCharType(Appendable result, boolean mandatory, boolean binary, boolean castContext) throws IOException {
		result.append("CHAR");
		size(result, 1);
	}

	/**
	 * Append a DB-specific type that can store a <code>short</code> value to
	 * the given {@link Appendable}.
	 */
	protected void appendShortType(Appendable result, boolean mandatory) throws IOException {
		result.append("SMALLINT");
	}
	
	/**
	 * Append a DB-specific type that can store a <code>int</code> value to
	 * the given {@link Appendable}.
	 */
	protected void appendIntType(Appendable result, boolean mandatory) throws IOException {
		result.append("INTEGER");
	}

	/**
	 * Append a DB-specific type that can store a <code>long</code> value to
	 * the given {@link Appendable}.
	 */
	protected void appendLongType(Appendable result, boolean mandatory) throws IOException {
		result.append("BIGINT");
	}

	/**
	 * Append a DB-specific type that can store a {@link TLID} value to the given {@link Appendable}
	 * .
	 * 
	 * @param columnName
	 *        The name of column to append type for.
	 * @param mandatory
	 *        Whether the column is mandatory.
	 * @param castContext
	 *        Whether this method is called to added type to "cast" expression.
	 */
	protected void appendIDType(Appendable result, String columnName, boolean mandatory, boolean castContext)
			throws IOException {
		if (IdentifierUtil.SHORT_IDS) {
			appendLongType(result, mandatory);
		} else {
			appendStringType(result, columnName, IdentifierUtil.REFERENCE_DB_SIZE, mandatory, true, castContext);
		}
	}

	/**
	 * Append a DB-specific type that can store a <code>float</code> value to
	 * the given {@link Appendable}.
	 */
	protected void appendFloatType(Appendable result, boolean mandatory) throws IOException {
		result.append("FLOAT");
	}
	
	/**
	 * Append a DB-specific type that can store a <code>double</code> value to
	 * the given {@link Appendable}.
	 */
	protected void appendDoubleType(Appendable result, boolean mandatory) throws IOException {
		result.append("DOUBLE");
	}

	/**
	 * Append a DB-specific type that can store a {@link BigDecimal} value to
	 * the given {@link Appendable}.
	 */
	protected void appendDecimalType(Appendable result, long size, int precision) throws IOException {
		result.append("DECIMAL");
		size(result, size, precision);
	}
	
    /**
	 * Maximum size of a <code>VARCHAR</code> column in Java characters.
	 * 
	 * @param binary
	 *        Whether the string column to create is binary or not.
     */
	private long varcharLimit(boolean binary) {
		if (binary) {
			return _config.getVarcharLimit();
		} else {
			return getNVarcharLimit();
		}
	}

	/**
	 * @see Config#getNVarcharLimit()
	 */
	protected long getNVarcharLimit() {
		return _config.getNVarcharLimit();
	}

	/**
	 * Append a DB-specific type that can store a {@link String} value to the given
	 * {@link Appendable}.
	 * 
	 * @param columnName
	 *        The name of column to append type for.
	 * @param size
	 *        Size of the column.
	 * @param mandatory
	 *        Whether the column is mandatory.
	 * @param binary
	 *        Whether the column is binary
	 * @param castContext
	 *        Whether this method is called to added type to "cast" expression.
	 */
	protected void appendStringType(Appendable result, String columnName, long size, boolean mandatory, boolean binary,
			boolean castContext) throws IOException {
		if (size <= 0) {
			throw new IllegalArgumentException("Size of column '" + columnName
				+ "' of type String must be greater than 0.");
		}
		result.append("VARCHAR");
		size(result, size);
	}

	/**
	 * Append a DB-specific type that can store the date part of a {@link Date}
	 * value to the given {@link Appendable}.
	 */
	protected void appendDateType(Appendable result, boolean mandatory) throws IOException {
		result.append(internalGetDBType(DBType.DATE.sqlType, false));
	}

	/**
	 * Append a DB-specific type that can store the time part of a {@link Date}
	 * value to the given {@link Appendable}.
	 */
	protected void appendTimeType(Appendable result, boolean mandatory) throws IOException {
		result.append(internalGetDBType(DBType.TIME.sqlType, false));
	}

	/**
	 * Append a DB-specific type that can store a {@link Date} value to the
	 * given {@link Appendable}.
	 */
	protected void appendDateTimeType(Appendable result, boolean mandatory) throws IOException {
		result.append(internalGetDBType(DBType.DATETIME.sqlType, false));
	}

	/**
	 * Append a DB-specific type that can store a long {@link String} or contents of a
	 * {@link Reader} to the given {@link Appendable}.
	 * 
	 * @param columnName
	 *        The name of column to append type for.
	 * @param size
	 *        Size of the column.
	 * @param mandatory
	 *        Whether the column is mandatory.
	 * @param binary
	 *        Whether the column is binary
	 * @param castContext
	 *        Whether this method is called to added type to "cast" expression.
	 */
	protected void appendClobType(Appendable result, String columnName, long size, boolean mandatory, boolean binary, boolean castContext) throws IOException {
		result.append("CLOB");
	}

	/**
	 * Append a DB-specific type that can store a <code>byte[]</code> value or the contents of an
	 * {@link InputStream} to the given {@link Appendable}.
	 * 
	 * @param size
	 *        The size modifier, or <code>0</code> if the size modifier is not applicable.
	 * @param mandatory
	 *        Whether the column is mandatory.
	 */
	protected void appendBlobType(Appendable result, long size, boolean mandatory) throws IOException {
		result.append(internalGetDBType(Types.BLOB, true));
	}

	/**
	 * Write a size annotation of the given size to the given {@link Appendable}.
	 */
	protected void size(Appendable result, long size) throws IOException {
		result.append('(');
		result.append(Long.toString(size));
	    result.append(')');
	}

	/**
	 * Write a size and precision annotation of the given size and precision to
	 * the given {@link Appendable}.
	 */
	protected void size(Appendable result, long size, int precision) throws IOException {
		result.append('(');
		result.append(Long.toString(size));
    	result.append(',');
    	result.append(Integer.toString(precision));
		result.append(')');
	}
	
	/**
	 * Whether the given SQL type (from {@link Types}) can be annotated with a
	 * size annotation.
	 */
    protected boolean sizeAnnotation(int sqlType) {
        switch (sqlType) {
        case Types.NVARCHAR: /* Intentional fall-through */
        case Types.VARCHAR:
        case Types.VARBINARY:    
        case Types.DECIMAL:
        case Types.NUMERIC:
        	return true;
        }
        return false;
    }
    
    /**
     * Whether the given SQL type (from {@link Types}) can be annotated with a
     * precision annotation.
     */
	protected boolean precisionAnnotation(int sqlType) {
        switch (sqlType) {
        case Types.DECIMAL:        
        case Types.NUMERIC:
        	return true;
        }
        return false;
	}
	
	/**
	 * Get a database (vendor/version) specific datatype for the sql datatype.
	 * 
	 * @param sqlType
	 *        A type as defined in {@link Types}.
	 * @param binary
	 *        Whether the column is declared binary meaning binary compare and
	 *        binary search.
	 * @return SQL fragment representing the DB specific datatype declaration
	 */
    protected String internalGetDBType(int sqlType, boolean binary) {
        switch (sqlType) {
            case Types.BIT:             
            case Types.BOOLEAN:         return "BIT";
            case Types.TINYINT:         return "TINYINT";
            case Types.SMALLINT:        return "SMALLINT";
            case Types.INTEGER:         return "INTEGER";
            case Types.BIGINT:          return "BIGINT";
            case Types.FLOAT:           return "FLOAT";
            case Types.REAL:            return "REAL";
            case Types.DOUBLE:          return "DOUBLE";
            case Types.NUMERIC:         return "NUMERIC";
            case Types.DECIMAL:         return "DECIMAL";
            case Types.CHAR:            return "CHAR";
            case Types.NCHAR:           return "NCHAR";
            case Types.VARCHAR:         return "VARCHAR";
            case Types.NVARCHAR:        return "NVARCHAR";
            case Types.LONGVARCHAR:     return "LONGVARCHAR";
            case Types.LONGNVARCHAR:    return "LONGNVARCHAR";
            case Types.DATE:            return "DATE";
            case Types.TIME:            return "TIME";
            case Types.TIMESTAMP:       return "TIMESTAMP";
            case Types.BINARY:          return "BINARY";
            case Types.VARBINARY:       return "VARBINARY";
            case Types.LONGVARBINARY:   return "LONGVARBINARY";
            case Types.NCLOB:           return "NCLOB";
            case Types.CLOB:            return "CLOB";
            case Types.BLOB:            return "BLOB";
            default: 
            	throw new IllegalArgumentException("Undefined SQL type '" + sqlType + "'.");
        }
    }
    
	/**
	 * Append a collation sensitive expression to the given buffer.
	 * 
	 * @param sqlExpression
	 *        SQL expression that is used in a collation relevant location such
	 *        as an ORDER BY statement.
	 * @param collationHint
	 *        The collation to use, see {@link CollationHint}.
	 */
    public final void appendCollatedExpression(Appendable buffer, String sqlExpression, CollationHint collationHint) { 
    	try {
    		internalAppendCollatedExpression(buffer, sqlExpression, collationHint);
    	} catch (IOException ex) {
    		throw wrapIOException(ex);
    	}
    }

	protected void internalAppendCollatedExpression(Appendable buffer, String sqlExpression, CollationHint collationHint) throws IOException {
		buffer.append(sqlExpression);
	}

	/**
	 * Append a cast of the given expression to the given type.
	 * 
	 * @param sqlExpression
	 *        SQL expression that should be cast..
	 * @param dbType
	 *        The type to cast to.
	 * @param size
	 *        The size of the target type.
	 * @param precision
	 *        The precision of the target type.
	 * @param binary
	 *        The binary flag of the target type.
	 */
	public final void appendCastExpression(Appendable buffer, String sqlExpression, DBType dbType, long size,
			int precision, boolean binary) {
		try {
			internalAppendCastExpression(buffer, sqlExpression, dbType, size, precision, binary);
		} catch (IOException ex) {
			throw wrapIOException(ex);
		}
	}

	/**
	 * Implementation of
	 * {@link #appendCastExpression(Appendable, String, DBType, long, int, boolean)}
	 */
	protected void internalAppendCastExpression(Appendable buffer, String sqlExpression, DBType dbType, long size,
			int precision, boolean binary) throws IOException {
		buffer.append("CAST(");
		buffer.append(sqlExpression);
		buffer.append(" AS ");
		internalAppendDBType(buffer, dbType, size, precision, false, binary, true, null);
		buffer.append(")");
	}

    /**
     * Appends a comment to a column or a table, if the DB supports comments.
     *
     * @param aComment the comment to append
     */
    public final void appendComment(Appendable sql, String aComment) {
        try {
            internalAppendComment(sql, aComment);
        } catch (IOException ex) {
            throw wrapIOException(ex);
        }
    }

    protected void internalAppendComment(Appendable sql, String aComment) throws IOException {
        // does nothing here; this is subclass dependent
    }

    /**
     * Return if some type needs a size.
     * 
     * This implementation currently was tested with MySQL and MSSQL.
     * 
     * @param   sqlType a type as defined in java.sql.Types
     * @return  true when size is needed.
     */
    public boolean noSize(DBType sqlType) {

        switch (sqlType) {
			case BOOLEAN:
			case BYTE:
			case SHORT:
			case INT:
			case LONG:
			case DOUBLE:
			case DATE:
			case TIME:
			case DATETIME:
				return true;
			case BLOB:
			case CLOB:
			case STRING:
			case CHAR:
			case DECIMAL:
			case FLOAT:
				return false;
			case ID:
				if (IdentifierUtil.SHORT_IDS) {
					return true;
				} else {
					return false;
				}
        }

		throw new AssertionError("Unsupported type: " + sqlType);
    }
    
    /** Return the DDL specifiaction for a NOT NULL column .*/
    public String notNullSpec() {
        return "NOT NULL";
    }

	/**
	 * Return the DDL specifiaction for a non-mandatory column.
	 * 
	 * @see #nullLiteral()
	 */
    public String nullSpec() {
        return "NULL";
    }

    /**
	 * Map a DB specific type to a Java Type.
	 * 
	 * This implemtantion currently was tested with MySQL only.
	 * 
	 * @param res
	 *        The resultSet to extract the value from.
	 * @param col
	 *        The column to extract it from (JDBC column, i.e. it first column has index 1)
	 * @param dbtype
	 *        the DBType for the derires JAVA-type.
	 * 
	 * @return An Object apropriate for the given DBType.
	 * 
	 * @see #setFromJava(PreparedStatement, Object, int, DBType)
	 */
    public Object mapToJava(ResultSet res, int col, DBType dbtype) throws SQLException {
        Object result;        
        switch (dbtype) {
        	
        	/* trivalent logic */
			case BOOLEAN:
                result = res.getBoolean(col) ? Boolean.TRUE : Boolean.FALSE;
                if (res.wasNull())
                    result = null;
                return result;
                
			case BYTE:
                result = Byte.valueOf(res.getByte(col));
                if (res.wasNull())
                    result = null;
                return result;

			case SHORT:
				result = Short.valueOf(res.getShort(col));
				if (res.wasNull())
					result = null;
				return result;

			case INT:
				result = Integer.valueOf(res.getInt(col));
				if (res.wasNull())
					result = null;
				return result;
				
			case LONG:
				result = Long.valueOf(res.getLong(col));
				if (res.wasNull())
					result = null;
				return result;

			case ID:
				result = IdentifierUtil.getId(res, col);
				return result;

			case FLOAT:
				result = Float.valueOf(res.getFloat(col));
				if (res.wasNull())
					result = null;
				return result;

			case DECIMAL:
			case DOUBLE:
				result = Double.valueOf(res.getDouble(col));
				if (res.wasNull())
					result = null;
				return result;

            /* ensure we always handle java.util.Date objects */
			case DATE:
				java.sql.Date theDate = getDate(res, col);
                return (theDate != null) ? new java.util.Date(theDate.getTime()) : null;
			case TIME:
				java.sql.Time theTime = getTime(res, col);
                return (theTime != null) ? new java.util.Date(theTime.getTime()) : null;
			case DATETIME:
				java.sql.Timestamp theTimeStamp = getTimestamp(res, col);
                return (theTimeStamp != null) ? new java.util.Date(theTimeStamp.getTime()) : null;
			case CHAR:
				return mapToCharacter(res, col);
			case CLOB:
				return getClobValue(res, col);
			case BLOB:
				return getBlobValue(res, col);
			case STRING:
				return res.getString(col);

			/* all other cases should work here */
            default:
                result = res.getObject(col);
        }
        return result;
    }

	/**
	 * Retrieves a {@link Character} value from the given result set.
	 */
	protected Object mapToCharacter(ResultSet res, int col) throws SQLException {
		String stringResult = res.getString(col);
		if (res.wasNull()) {
			return null;
		}
		return Character.valueOf(stringResult.charAt(0));
	}
    
    /**
	 * Set a Java type on a Prepared statement, care about DB specific types.
	 * 
	 * <p>
	 * Handles <code>null</code> value. Actual implementation in
	 * {@link #internalSetFromJava(PreparedStatement, Object, int, DBType)}
	 * </p>
	 * 
	 * @param pstm
	 *        Insert the value into this statement
	 * @param value
	 *        The Value to set
	 * @param col
	 *        The column to set the value in.
	 * @param type
	 *        {@link DBType} representing a SQL type as found in {@link java.sql.Types}. See
	 *        {@link DBType#fromLiteralValue(Object)} and
	 *        {@link DBType#fromSqlType(DBHelper, int, int)}.
	 * 
	 * @see #mapToJava(ResultSet, int, DBType)
	 */
	public final void setFromJava(PreparedStatement pstm, Object value, int col, DBType type) throws SQLException {

		if (value == null) {
			internalSetNull(pstm, col, type);
		} else {
			internalSetFromJava(pstm, value, col, type);
		}
	}

	protected void internalSetNull(PreparedStatement pstm, int col, DBType dbtype) throws SQLException {
		pstm.setNull(col, dbtype.sqlType);
	}

	/**
	 * Sets a non null Java type on a Prepared statement, care about DB specific types.
	 * 
	 * @param val
	 *        not <code>null</code>
	 * 
	 * @see #setFetchSize(Statement, int)
	 */
	protected void internalSetFromJava(PreparedStatement pstm, Object val, int col, DBType dbtype) throws SQLException {
		assert val != null : "value must not be null";
		switch (dbtype) {
			case DATE:
				setDateFromJava(pstm, val, col);
				break;
			case TIME:
				setTimeFromJava(pstm, val, col);
				break;
			case DATETIME:
				setTimestampFromJava(pstm, val, col);
				break;
			case SHORT:
				pstm.setShort(col, ((Short) val).shortValue());
				break;
			case CHAR:
				setCharFromJava(pstm, val, col);
				break;
			case BOOLEAN:
				pstm.setBoolean(col, (Boolean) val);
				break;
			case BYTE:
				setByteFromJava(pstm, val, col);
				break;
			case STRING:
				pstm.setString(col, val.toString());
				break;
			case CLOB: {
				setClobFromJava(pstm, val, col);
				break;
			}
			case ID: {
				IdentifierUtil.setId(pstm, col, (TLID) val);
				break;
			}
			case BLOB: {
				setBlobFromJava(pstm, val, col);
				break;
			}
			default:
				pstm.setObject(col, val);
		}
	}

	/**
	 * Sets a {@link Byte} into the given column.
	 * 
	 * @param pstm
	 *        The {@link PreparedStatement} to insert value in.
	 * @param val
	 *        The value to set.
	 * @param col
	 *        The column to set value in.
	 */
	protected void setByteFromJava(PreparedStatement pstm, Object val, int col) throws SQLException {
		if (!(val instanceof Number)) {
			throw new SQLException("Dont know how to convert a " + val.getClass() + " to a "
				+ Number.class.getName());
		}
		long value = ((Number) val).longValue();
		if (value < Byte.MIN_VALUE || value > Byte.MAX_VALUE) {
			throw new SQLException("Number '" + value + "' is out of range to store as byte");
		}
		pstm.setByte(col, (byte) value);
	}

	/**
	 * Sets a {@link Character} into the given column.
	 * 
	 * @param pstm
	 *        The {@link PreparedStatement} to insert value in.
	 * @param val
	 *        The value to set.
	 * @param col
	 *        The column to set value in.
	 */
	protected void setCharFromJava(PreparedStatement pstm, Object val, int col) throws SQLException {
		if (val instanceof Character) {
			pstm.setString(col, val.toString());
		} else {
			throw new SQLException("Dont know how to convert a " + val.getClass() + " to a "
				+ Character.class.getName());
		}
	}

	/**
	 * Sets a {@link PreparedStatement#setTimestamp(int, Timestamp) timestamp} into the given
	 * {@link PreparedStatement}.
	 * 
	 * @param pstm
	 *        The {@link PreparedStatement} to insert value in.
	 * @param val
	 *        The value to set.
	 * @param col
	 *        The column to set value in.
	 */
	protected void setTimestampFromJava(PreparedStatement pstm, Object val, int col) throws SQLException {
		Timestamp timeStamp;
		if (val instanceof Timestamp) {
			timeStamp = (Timestamp) val;
		} else if (val instanceof java.util.Date) {
			timeStamp = new Timestamp(((java.util.Date) val).getTime());
		} else {
			throw new SQLException("Dont know how to convert a " + val.getClass() + " to a "
				+ Timestamp.class.getName());
		}
		setTimestamp(pstm, col, timeStamp);
	}

	/**
	 * Sets a {@link PreparedStatement#setTime(int, Time) time} into the given
	 * {@link PreparedStatement}.
	 * 
	 * @param pstm
	 *        The {@link PreparedStatement} to insert value in.
	 * @param val
	 *        The value to set.
	 * @param col
	 *        The column to set value in.
	 */
	protected void setTimeFromJava(PreparedStatement pstm, Object val, int col) throws SQLException {
		Time time;
		if (val instanceof Time) {
			time = (Time) val;
		} else if (val instanceof java.util.Date) {
			long timeStamp = ((java.util.Date) val).getTime();
			time = new Time(timeStamp);
		} else {
			throw new SQLException("Dont know how to convert a " + val.getClass() + " to a "
				+ Time.class.getName());
		}
		setTime(pstm, col, time);
	}

	/**
	 * Sets a {@link PreparedStatement#setDate(int, Date) date} into the given
	 * {@link PreparedStatement}
	 * 
	 * @param pstm
	 *        The {@link PreparedStatement} to insert value in.
	 * @param val
	 *        The value to set.
	 * @param col
	 *        The column to set value in.
	 */
	protected void setDateFromJava(PreparedStatement pstm, Object val, int col) throws SQLException {
		Date date;
		if (val instanceof Date) {
			date = (Date) val;
		} else if (val instanceof java.util.Date) {
			long timeStamp = ((java.util.Date) val).getTime();
			date = new Date(timeStamp);
		} else {
			throw new SQLException("Dont know how to convert a " + val.getClass() + " to a "
				+ Date.class.getName());
		}
		setDate(pstm, col, date);
	}
    
	/**
	 * Sets a {@link PreparedStatement#setLong(int, long) long} into the given
	 * {@link PreparedStatement}.
	 * 
	 * @param pstm
	 *        The {@link PreparedStatement} to insert value in.
	 * @param val
	 *        The value to set.
	 * @param col
	 *        The column to set value in.
	 */
	protected void setLongFromJava(PreparedStatement pstm, Object val, int col) throws SQLException {
		long longValue;
		if (val instanceof Number) {
			longValue = ((Number) val).longValue();
		} else {
			throw new SQLException("Dont know how to convert a " + val.getClass() + " to a long");
		}
		pstm.setLong(col, longValue);
	}

	/**
	 * Sets a {@link DBType#CLOB} value into the given {@link PreparedStatement}.
	 * 
	 * @param pstm
	 *        The {@link PreparedStatement} to insert value in.
	 * @param val
	 *        The value to set.
	 * @param col
	 *        The column to set value in.
	 */
	protected void setClobFromJava(PreparedStatement pstm, Object val, int col) throws SQLException {
		if (val instanceof Reader) {
			Reader reader = (Reader) val;
			setClob(pstm, col, reader);
		}
		else if (val instanceof CharacterContent) {
			try {
				CharacterContent content = (CharacterContent) val;
				setClob(pstm, col, content.getReader());
			} catch (IOException ex) {
				throw new SQLException("Accessing character content failed.", ex);
			}
		}
		else if (val instanceof String) {
			String string = (String) val;
			StringReader theReader = new StringReader(string);
			setClob(pstm, col, string.length(), theReader);
		}
		else if (val instanceof Clob) {
			Clob clob = (Clob) val;
			setClob(pstm, col, clob);
		}
		else {
			pstm.setObject(col, val);
		}
	}

	/**
	 * Sets a {@link DBType#CLOB} value with a fixed length into the given
	 * {@link PreparedStatement}.
	 * 
	 * @param pstm
	 *        The {@link PreparedStatement} to insert value in.
	 * @param col
	 *        The column to set value in.
	 * @param length
	 *        Number of chars that the reader contains.
	 * @param reader
	 *        {@link Reader} to read SQL statements
	 */
	protected void setClob(PreparedStatement pstm, int col, int length, Reader reader) throws SQLException {
		pstm.setClob(col, reader, length);
	}

	/**
	 * Sets a {@link DBType#CLOB} value into the given {@link PreparedStatement}.
	 * 
	 * @param pstm
	 *        The {@link PreparedStatement} to insert value in.
	 * @param col
	 *        The column to set value in.
	 * @param reader
	 *        {@link Reader} to read SQL statements
	 */
	protected void setClob(PreparedStatement pstm, int col, Reader reader) throws SQLException {
		pstm.setClob(col, reader);
	}

	/**
	 * Sets a {@link DBType#CLOB} value into the given {@link PreparedStatement}.
	 * 
	 * @param pstm
	 *        The {@link PreparedStatement} to insert value in.
	 * @param col
	 *        The column to set value in.
	 * @param clob
	 *        The value to set.
	 */
	protected void setClob(PreparedStatement pstm, int col, Clob clob) throws SQLException {
		pstm.setClob(col, clob.getCharacterStream(), clob.length());
	}

	/**
	 * Sets a {@link DBType#BLOB} value into the given {@link PreparedStatement}.
	 * 
	 * @param pstm
	 *        The {@link PreparedStatement} to insert value in.
	 * @param val
	 *        The value to set.
	 * @param col
	 *        The column to set value in.
	 */
	protected void setBlobFromJava(PreparedStatement pstm, Object val, int col) throws SQLException {
		if (val instanceof InputStream) {
			InputStream stream = (InputStream) val;
			setBlob(pstm, col, stream);
		}
		else if (val instanceof BinaryContent) {
			BinaryContent content = (BinaryContent) val;
			setBinaryContent(pstm, col, content);
		}
		else if (val instanceof BinaryDataSource) {
			BinaryDataSource content = (BinaryDataSource) val;
			setBinaryContent(pstm, col, content.toData());
		}
		else if (val instanceof Blob) {
			Blob blob = (Blob) val;
			setBlob(pstm, col, blob.getBinaryStream(), blob.length());
		}
		else {
			pstm.setObject(col, val);
		}
	}

	private void setBinaryContent(PreparedStatement pstm, int col, BinaryContent content) throws SQLException {
		try {
			InputStream stream = content.getStream();
			if (content instanceof BinaryData) {
				BinaryData data = (BinaryData) content;
				long size = data.getSize();
				if (size >= 0) {
					setBlob(pstm, col, stream, size);
				} else {
					/* Ticket #27297: Negative size cannot be true. The driver could interpret
					 * the value as "correct", which would lead to errors. */
					setBlob(pstm, col, stream);
				}
			} else {
				setBlob(pstm, col, stream);
			}
		} catch (IOException ex) {
			throw new SQLException("Access to data failed.", ex);
		}
	}

	/**
	 * @param pstm
	 *        The {@link PreparedStatement} to insert value in.
	 * @param col
	 *        The column to set value in.
	 * @param stream
	 *        Stream representing the underlying binary data.
	 * @param size
	 *        Size of the binary data.
	 */
	protected void setBlob(PreparedStatement pstm, int col, InputStream stream, long size) throws SQLException {
		pstm.setBlob(col, stream, size);
	}

	/**
	 * @param pstm
	 *        The {@link PreparedStatement} to insert value in.
	 * @param col
	 *        The column to set value in.
	 * @param stream
	 *        Stream representing the underlying binary data.
	 */
	protected void setBlob(PreparedStatement pstm, int col, InputStream stream) throws SQLException {
		pstm.setBlob(col, stream);
	}

    /**
     * Appends table options after CREATE TABLE(...) to support storage along the primaryKey.
     *
     * @param out The {@link Appendable} the options are appended to.
     * @param usePKeyStorage true - if possible the data will be stored along the PrimaryKey
     *                              this will improve performance when accessing adjacent data along the primary key.
     *                       false - use default way to store data and primary key.
     * @param compress       if supported by the DB compress the first part of the primary key.
     * 
     * (e.g. for Oracle this will result in <code>ORGANISATION INDEX COMPRES compress</code>)
     */
    public final Appendable appendTableOptions(Appendable out, boolean usePKeyStorage, int compress) {
    	try {
			internalAppendTableOptions(out, usePKeyStorage, compress);
		} catch (IOException ex) {
			throw wrapIOException(ex);
		}
    	return out;
    }

    /**
     * Implementation of {@link #appendTableOptions(Appendable, boolean, int)}
     */
	protected void internalAppendTableOptions(Appendable out, boolean usePKeyStorage, int compress) throws IOException {
        // Nothing to do.
	}
    
    /**
     * Return a modifier after create table to support Storage along the primaryKey.
     * 
     * @param compress    if supported by the DB compress the first part of the index.
     *                    Any value &lt;= 0 will be ignored.
     * 
     * (e.g. for MySQL we will create another type of table to support locking)
     */
    public String getAppendIndex(int compress) {
        return "";
    }

    /** 
     * Indicates whether <code>setObject()</code> will allow null-values.
     * 
     * (Is there any way to find out some other way ?)
     */
    public boolean supportNullInSetObject() {
        return true;    // let's hope for the best
    }

	/**
	 * Whether a B/CLOB column can be part of the selected columns, if the DISTINCT modifier is
	 * given.
	 */
	public boolean supportsDistinctLob() {
		return false;
	}

	/**
	 * Checks whether for the given {@link DBType} and the given arguments a CLOB or BLOB database
	 * column is created.
	 * 
	 * @param columnSqlType
	 *        The SQL type code according to constants defined in {@link Types}.
	 * @param size
	 *        The size modifier, or <code>0</code> if the size modifier is not applicable.
	 * @param precision
	 *        The precision modifier, or <code>-1</code>, if the precision modifier is not
	 *        applicable.
	 * @param mandatory
	 *        Whether <code>null</code> could not be stored.
	 * @param binary
	 *        The binary modifier.
	 * 
	 * @see #appendDBType(Appendable, DBType, String, long, int, boolean, boolean)
	 */
	public boolean isLobType(DBType columnSqlType, long size, int precision, boolean mandatory, boolean binary) {
		switch (columnSqlType) {
			case CLOB:
				return true;
			case BLOB:
				return true;
			case STRING:
				return size > varcharLimit(binary);
			default:
				return false;
		}
	}

    /** 
     * Analyze Table defined by aTableName.
     * 
     * Analyze should need only a small amount of time.
     * It usually helps the database to improve Acesseplans
     * along the indexes of a Database.
     */
    public void analyzeTable(Statement stmt, String aTableName) throws SQLException {
		Logger.info("Don't know how to analyzeTable()", this);
    }

    /** 
     * Optimize Table defined by aTableName.
     * 
     * Optimize may take a long time but should leave the
     * table in an optimal state. Optimize is usually apropriate
     * after large deletes or reorganizations.
     */
    public void optimizeTable(Statement stmt, String aTableName) throws SQLException {
		Logger.info("Don't know how to optimizeTable()", this);
    }

    /**
     * Create a valid SQL statement to drop an index.
     * 
     * DBMS have quite different ideas about indexes though.
     * 
     * @param   idxName     Name of the index to DROP
     * @param   tableName   Name of the table for the index to DROP.
     */
	public final String dropIndex(String idxName, String tableName) {
		StringBuffer sql = new StringBuffer(64);
		try {
			appendDropIndex(sql, idxName, tableName);
		} catch (IOException ex) {
			throw new UnreachableAssertion("StringBuffer does not throw an IOException.", ex);
		}
		return sql.toString();
    }

	/**
	 * Appends a valid SQL statement to drop an index.
	 * 
	 * DBMS have quite different ideas about indexes though.
	 * 
	 * @param sql
	 *        The {@link Appendable} to append drop script to.
	 * @param idxName
	 *        Name of the index to DROP
	 * @param tableName
	 *        Name of the table for the index to DROP.
	 * @throws IOException
	 *         iff given {@link Appendable} throws it.
	 */
	public void appendDropIndex(Appendable sql, String idxName, String tableName) throws IOException {
		sql.append("DROP INDEX ");
		sql.append(columnRef(idxName));
        sql.append(" ON ");
		sql.append(tableRef(tableName));
	}

	/**
	 * Appends a valid SQL statement with the case insensitive LIKE keyword.
	 * 
	 * @param sql
	 *        The {@link Appendable} to append the case insensitive script to.
	 * @throws IOException
	 *         iff given {@link Appendable} throws it.
	 */
	public void appendLikeCaseInsensitive(Appendable sql) throws IOException {
		sql.append("LIKE");
	}

	/**
	 * Appends a valid SQL statement with the case sensitive LIKE keyword.
	 * 
	 * @param sql
	 *        The {@link Appendable} to append the case sensitive script to.
	 * @throws IOException
	 *         iff given {@link Appendable} throws it.
	 */
	public void appendLikeCaseSensitive(Appendable sql) throws IOException {
		sql.append("LIKE");
	}
    
	/** Dump a given table as INSERT suiteable for the Helpers Database.
	 * 
	 * @param out Output will be written here.
	 * @param stm a Statement used to create some "SELECT * FROM table" statements.
	 */
	public void dumpAsInsert(PrintWriter out, Statement stm, String table) throws SQLException {
		try (ResultSet res = stm.executeQuery("SELECT * FROM " + tableRef(table))) {
            dumpAsInsert(out, table, res);
        }
	}

    /** Dump a given ResultSet as INSERT suiteable for the Helpers Database.
     * 
     * The Resultset will be exhaustet.
     * 
	 * @param out Output will be written here.
	 * @return the number of rows written.
     */
    public int dumpAsInsert(PrintWriter out, String table, ResultSet res) throws SQLException {
    	ResultSetMetaData meta  = res.getMetaData();
    	int               cols  = meta.getColumnCount();
    	int               count = 0;
    	while (res.next()) {
    		out.write("INSERT INTO ");
			out.write(tableRef(table));
			out.write(" VALUES (");
			addValue(out, 1, res, DBType.fromSqlType(this, meta.getColumnType(1), meta.getScale(1)));
    		for (int i=2; i<=cols; i++) {
				out.write(",\t");
				addValue(out, i, res, DBType.fromSqlType(this, meta.getColumnType(i), meta.getScale(i)));
    		}
			out.println(");");
			count++;
    	}
    	return count;
    }

	/**
	 * Reads the content of the given table.
	 * 
	 * @param stmt
	 *        The statement to execute SELECT query.
	 * @param table
	 *        The table to get contents from.
	 * 
	 * @return The content of the table. May be empty but not <code>null</code>.
	 */
	public DatabaseContent fetchTableContent(Statement stmt, String table) throws SQLException {
		try (ResultSet resultSet = stmt.executeQuery("SELECT * FROM " + tableRef(table))) {
			DBType[] columnTypes = getColumnTypes(resultSet);
			DatabaseContent content = new DatabaseContent(table, columnTypes);
			addRows(resultSet, columnTypes, content);
			return content;
		}
	}

	private DBType[] getColumnTypes(ResultSet resultSet) throws SQLException {
		ResultSetMetaData metaData = resultSet.getMetaData();
		DBType[] columnTypes = new DBType[metaData.getColumnCount()];
		for (int i = 0; i < columnTypes.length; i++) {
			columnTypes[i] = DBType.fromSqlType(this, metaData.getColumnType(i + 1), metaData.getScale(i + 1));
		}
		return columnTypes;
	}

	private void addRows(ResultSet resultSet, DBType[] columnTypes, DatabaseContent content) throws SQLException {
		while (resultSet.next()) {
			content.addRow(getRow(resultSet, columnTypes));
		}
	}

	private Object[] getRow(ResultSet resultSet, DBType[] columnTypes) throws SQLException {
		Object[] row = new Object[columnTypes.length];
		for (int i = 0; i < columnTypes.length; i++) {
			row[i] = mapToJava(resultSet, i + 1, columnTypes[i]);
		}
		return row;
	}

	/**
	 * Dump the given {@link DatabaseContent} as INSERT suitable for the database.
	 * 
	 * @param out
	 *        Output to append content to.
	 * @param content
	 *        The actual content to write.
	 * @throws IOException
	 *         iff thrown by <code>out</code>.
	 */
	public void dumpAsInsert(Appendable out, DatabaseContent content) throws IOException {
		DBType[] columnTypes = content.getColumnTypes();
		CharSequence table = tableRef(content.getTableName());
		for (Object[] row : content.getRows()) {
			out.append("INSERT INTO ");
			out.append(table);
			out.append(" VALUES (");
			literal(out, columnTypes[0], row[0]);
			for (int i = 1; i < columnTypes.length; i++) {
				out.append(", ");
				literal(out, columnTypes[i], row[i]);
			}
			out.append(");");
			out.append('\n');
		}
	}

	/**
	 * Add a Value for a (DB-Specific) Insert Statement.
	 * 
	 * The default bevaiour should work for simple cases.
	 * 
	 * @param out  Output will be written here.
	 * @param col  The column to write (1..n)
	 * @param res  The resultSet to extract some value from
	 * @param type The desired SQL-Type.
	 */
	public final void addValue(PrintWriter out, int col, ResultSet res, DBType type) throws SQLException {
		Object value = mapToJava(res, col, type);
		literal(out, type, value);
	}

	/**
	 * Appends a database adequate representation of the value to the SQL.
	 * 
	 * @param sql
	 *        the sql to append representation.
	 * @param value
	 *        the value to encode
	 */
	public final void appendValue(Appendable sql, Object value) {
		try {
			internalAppendValue(sql, value);
		} catch (IOException ex) {
			throw wrapIOException(ex);
		}
	}

	/**
	 * Actual implementation of {@link #appendValue(Appendable, Object)}.
	 */
	protected void internalAppendValue(Appendable sql, Object value) throws IOException {
		if (value == null) {
			appendNullValue(sql);
			return;
		}
		if (value instanceof Number) {
			appendNumberValue(sql, (Number) value);
			return;
		}
		if (value instanceof String) {
			appendStringValue(sql, (String) value);
			return;
		}

		if (value instanceof Character) {
			appendCharValue(sql, (Character) value);
			return;
		}

		if (value instanceof Boolean) {
			appendBooleanValue(sql, (Boolean) value);
			return;
		}

		if (value instanceof java.util.Date) {
			appendDateValue(sql, (java.util.Date) value);
			return;
		}

		if (value instanceof Tuple) {
			appendTupleValue(sql, (Tuple) value);
			return;
		}

		if (value instanceof TLID) {
			internalAppendValue(sql, ((TLID) value).toStorageValue());
			return;
		}

		throw new IllegalArgumentException("Can not append value of type " + value.getClass() + ": " + value);
	}

	/**
	 * Appends a representation of the given {@link Date} to the given SQL.
	 */
	private void appendDateValue(Appendable sql, java.util.Date value) throws IOException {
		Format dateTimeFormat = getDateTimeFormat();
		String format = dateTimeFormat.format(value);
		sql.append(format);
	}

	/**
	 * Appends null to the given SQL.
	 */
	protected void appendNullValue(Appendable sql) throws IOException {
		sql.append(String.valueOf((Object) null));
	}

	/**
	 * Appends a representation of the given {@link Boolean} to the given SQL.
	 * 
	 * @param value
	 *        not <code>null</code>.
	 */
	protected final void appendBooleanValue(Appendable sql, Boolean value) throws IOException {
		sql.append(getBooleanFormat().format(value));
	}

	/**
	 * Appends a representation of the given {@link String} to the given SQL.
	 * 
	 * @param value
	 *        not <code>null</code>.
	 */
	protected void appendStringValue(Appendable sql, String value) throws IOException {
		internalEscape(sql, value);
	}

	/**
	 * Appends a representation of the given Character to the given SQL.
	 * 
	 * @param value
	 *        not <code>null</code>.
	 */
	protected void appendCharValue(Appendable sql, char value) throws IOException {
		appendStringValue(sql, String.valueOf(value));
	}

	/**
	 * Appends a representation of the given {@link Number} to the given SQL.
	 * 
	 * @param value
	 *        not <code>null</code>.
	 */
	protected void appendNumberValue(Appendable sql, Number value) throws IOException {
		sql.append(value.toString());
	}	

	/**
	 * Appends a representation of the given {@link Tuple} to the given SQL.
	 * 
	 * @param value
	 *        The literal tuple.
	 */
	protected void appendTupleValue(Appendable sql, Tuple value) throws IOException {
		sql.append('(');
		for (int n = 0, cnt = value.size(); n < cnt; n++) {
			if (n > 0) {
				sql.append(',');
			}
			appendValue(sql, value.get(n));
		}
		sql.append(')');
	}

	/**
     * Escape a String according to the SQL conventions.
     */
    public final void escape(Appendable out, String str) {
        try {
			internalEscape(out, str);
		} catch (IOException ex) {
			throw wrapIOException(ex);
		}
    }

    /**
     * Implementation of the final {@link #escape(Appendable, String)} method.
     */
	protected void internalEscape(Appendable out, String str) throws IOException {
		int len    = str.length();
		char quote = stringQuoteChar();
		
		out.append(quote);
		for (int i = 0; i < len; i++) {
		    char ch = str.charAt(i);
		    if (ch == quote)
		        out.append("''");
		    else
		        out.append(ch);
		}
		out.append(quote);
	}

	/**
	 * Character that is used to quote string literals.
	 */
	public char stringQuoteChar() {
		return '\'';
	}
    
    /**
     * Ignore the given table name as reserved for System usage.
     * 
     * This is a workaround for drivers not implementing {@link DatabaseMetaData#getTables(java.lang.String, java.lang.String, java.lang.String, java.lang.String[])} 
     * correctly. (as of now MSSQL)
     */
    public boolean isSystemTable(String aTableName) {
        return false; 
    }

    /**
     * Return maximum Size a static Set can have.
     * 
     * For statements like SELECT MAX(..) WHRE ... IN (&lt;ID&gt;,&lt;ID&gt;, .... )
     */
    public int getMaxSetSize() {
		return 1000;
    }

	/**
	 * Return maximum number of batches that can be added to a {@link Statement} before
	 * {@link Statement#executeBatch() execute} must be called.
	 * 
	 * @param numberParameters
	 *        An upper bound for the number of parameters in the {@link PreparedStatement}.
	 * 
	 * @see Statement#addBatch(String)
	 * @see PreparedStatement#addBatch()
	 * 
	 * @throws IllegalArgumentException
	 *         iff number of parameters is negative.
	 */
	public int getMaxBatchSize(int numberParameters) {
		switch (numberParameters) {
			case 0: {
				/* No parameter in prepared statement, i.e. statement is constant. This is strange,
				 * but not illegal. */
				return _maxNumberBatchParameter;
			}
			default: {
				if (numberParameters < 0) {
					throw new IllegalArgumentException("Can not handle negative number of parameters.");
				}
				return _maxNumberBatchParameter / numberParameters;
			}
		}
	}

    /**
     * A quoted column name reference for the column with the given name.
     */
	public String columnRef(String columnName) {
		return columnName;
	}

    /**
     * A quoted table name reference for the table with the given name.
     */
	public String tableRef(String tableName) {
		return tableName;
	}
	
	/**
	 * Whether this database internally supports skipping the first rows of a query.
	 * 
	 * @see #limitStart(StringBuilder, int, int)
	 * @see #limitColumns(StringBuilder, int, int)
	 * @see #limitLast(StringBuilder, int, int)
	 */
    public boolean supportsLimitStart() {
    	return false;
    }

	/**
	 * Whether this database internally supports limiting the number of fetched rows.
	 * 
	 * <p>
	 * The result is always <code>true</code>, if {@link #supportsLimitStart()} returns
	 * <code>true</code>.
	 * </p>
	 * 
	 * @see #limitStart(StringBuilder, int, int)
	 * @see #limitStart(StringBuilder, int)
	 * @see #limitColumns(StringBuilder, int, int)
	 * @see #limitColumns(StringBuilder, int)
	 * @see #limitLast(StringBuilder, int, int)
	 * @see #limitLast(StringBuilder, int)
	 */
    public boolean supportsLimitStop() {
    	return false;
    }

	/**
	 * Adds an optimization hint to the given statement that allows the database to optimize the
	 * query for fetching only the first rows.
	 * 
	 * <p>
	 * This method must be called directly after the statement keyword ( <code>SELECT</code>, ...).
	 * </p>
	 * 
	 * <p>
	 * Note: The statement must always be constructed using all, the
	 * <ul>
	 * <li>{@link #limitStart(StringBuilder, int)},</li>
	 * <li>{@link #limitColumns(StringBuilder, int)},</li>
	 * <li>{@link #limitLast(StringBuilder, int)}</li>
	 * </ul>
	 * methods to become database independent.
	 * </p>
	 * 
	 * <p>
	 * Note: If the query uses an <code>ORDER BY</code> clause, a matching index is required to
	 * benefit from this optimizations.
	 * </p>
	 * 
	 * @param rowCount
	 *        The maximum number of rows to fetch.
	 * 
	 * @see #limitStart(StringBuilder, int)
	 * @see #limitColumns(StringBuilder, int)
	 * @see #limitLast(StringBuilder, int)
	 * @see #supportsLimitStart()
	 * @see #supportsLimitStop()
	 */
    public final StringBuilder limitStart(StringBuilder sql, int rowCount) {
    	return limitStart(sql, 0, rowCount);
    }
    
	/**
	 * Adds an optimization hint to the given statement that allows the database to optimize the
	 * query for fetching only the first rows.
	 * 
	 * <p>
	 * This method must be called at the end of the statement.
	 * </p>
	 * 
	 * <p>
	 * Note: The statement must always be constructed using all, the
	 * <ul>
	 * <li>{@link #limitStart(StringBuilder, int)},</li>
	 * <li>{@link #limitColumns(StringBuilder, int)},</li>
	 * <li>{@link #limitLast(StringBuilder, int)}</li>
	 * </ul>
	 * methods to become database independent.
	 * </p>
	 * 
	 * <p>
	 * Note: If the query uses an <code>ORDER BY</code> clause, a matching index is required to
	 * benefit from this optimizations.
	 * </p>
	 * 
	 * @param rowCount
	 *        The maximum number of rows to fetch.
	 * 
	 * @see #limitStart(StringBuilder, int)
	 * @see #limitColumns(StringBuilder, int)
	 * @see #limitLast(StringBuilder, int)
	 * @see #supportsLimitStart()
	 * @see #supportsLimitStop()
	 */
    public final StringBuilder limitLast(StringBuilder sql, int rowCount) {
        return limitLast(sql, 0, rowCount);
    }

	/**
	 * Requests limiting the fetched rows to the window given by the first and last row.
	 * 
	 * <p>
	 * This method must be called before constructing the <code>SELECT</code> clause of the
	 * statement.
	 * </p>
	 * 
	 * <p>
	 * Note: The statement must always be constructed using all, the
	 * <ul>
	 * <li>{@link #limitStart(StringBuilder, int, int)},</li>
	 * <li>{@link #limitColumns(StringBuilder, int, int)},</li>
	 * <li>{@link #limitLast(StringBuilder, int, int)}</li>
	 * </ul>
	 * methods to become database independent.
	 * </p>
	 * 
	 * <p>
	 * Note: For accessing pages of the result, an <code>ORDER BY</code> clause is required. For
	 * efficiency, a matching index is required.
	 * </p>
	 * 
	 * <p>
	 * If this feature is not supported by the database, nothing is added to the statement and the
	 * effect must be produced by skipping row of the result set.
	 * </p>
	 * 
	 * @param startRow
	 *        The zero-based index of the first row of the result to fetch.
	 * @param stopRow
	 *        The zero-based index of the first row to skip, or <code>-1</code> to fetch all rows.
	 * 
	 * @see #limitStart(StringBuilder, int, int)
	 * @see #limitColumns(StringBuilder, int, int)
	 * @see #limitLast(StringBuilder, int, int)
	 * @see #supportsLimitStart()
	 * @see #supportsLimitStop()
	 */
    public StringBuilder limitStart(StringBuilder sql, int startRow, int stopRow) {
    	return sql;
    }

	/**
	 * Requests limiting the fetched rows to the first given number of rows.
	 * 
	 * <p>
	 * This method must be called directly after the <code>SELECT</code> clause of the statement.
	 * </p>
	 * 
	 * <p>
	 * Note: The statement must always be constructed using all, the
	 * <ul>
	 * <li>{@link #limitStart(StringBuilder, int)},</li>
	 * <li>{@link #limitColumns(StringBuilder, int)},</li>
	 * <li>{@link #limitLast(StringBuilder, int)}</li>
	 * </ul>
	 * methods to become database independent.
	 * </p>
	 * 
	 * <p>
	 * Note: For accessing pages of the result, an <code>ORDER BY</code> clause is required. For
	 * efficiency, a matching index is required.
	 * </p>
	 * 
	 * <p>
	 * If this feature is not supported by the database, nothing is added to the statement and the
	 * effect must be produced by skipping row of the result set.
	 * </p>
	 * 
	 * @param rowCount
	 *        The maximum number of rows to fetch.
	 * 
	 * @see #limitStart(StringBuilder, int)
	 * @see #limitColumns(StringBuilder, int)
	 * @see #limitLast(StringBuilder, int)
	 * @see #supportsLimitStart()
	 * @see #supportsLimitStop()
	 */
	public final StringBuilder limitColumns(StringBuilder sql, int rowCount) {
		return limitColumns(sql, 0, rowCount);
	}

	/**
	 * Requests limiting the fetched rows to the window given by the first and last row.
	 * 
	 * <p>
	 * This method must be called after constructing the <code>SELECT</code> clause of the
	 * statement.
	 * </p>
	 * 
	 * <p>
	 * Note: The statement must always be constructed using all, the
	 * <ul>
	 * <li>{@link #limitStart(StringBuilder, int, int)},</li>
	 * <li>{@link #limitColumns(StringBuilder, int, int)},</li>
	 * <li>{@link #limitLast(StringBuilder, int, int)}</li>
	 * </ul>
	 * methods to become database independent.
	 * </p>
	 * 
	 * <p>
	 * Note: For accessing pages of the result, an <code>ORDER BY</code> clause is required. For
	 * efficiency, a matching index is required.
	 * </p>
	 * 
	 * <p>
	 * If this feature is not supported by the database, nothing is added to the statement and the
	 * effect must be produced by skipping row of the result set.
	 * </p>
	 * 
	 * @param startRow
	 *        The zero-based index of the first row of the result to fetch.
	 * @param stopRow
	 *        The zero-based index of the first row to skip, or <code>-1</code> to fetch all rows.
	 * 
	 * @see #limitStart(StringBuilder, int, int)
	 * @see #limitColumns(StringBuilder, int, int)
	 * @see #limitLast(StringBuilder, int, int)
	 * @see #supportsLimitStart()
	 * @see #supportsLimitStop()
	 */
    public StringBuilder limitColumns(StringBuilder sql, int startRow, int stopRow) {
    	return sql;
    }

    /**
	 * Requests limiting the fetched rows to the window given by the first and last row.
	 * 
	 * <p>
	 * This method must be called at the end of the statement.
	 * </p>
	 * 
	 * <p>
	 * Note: The statement must always be constructed using all, the
	 * <ul>
	 * <li>{@link #limitStart(StringBuilder, int, int)},</li>
	 * <li>{@link #limitColumns(StringBuilder, int, int)},</li>
	 * <li>{@link #limitLast(StringBuilder, int, int)}</li>
	 * </ul>
	 * methods to become database independent.
	 * </p>
	 * 
	 * <p>
	 * Note: For accessing pages of the result, an <code>ORDER BY</code> clause is required. For
	 * efficiency, a matching index is required.
	 * </p>
	 * 
	 * <p>
	 * If this feature is not supported by the database, nothing is added to the statement and the
	 * effect must be produced by skipping row of the result set.
	 * </p>
	 * 
	 * @param startRow
	 *        The zero-based index of the first row of the result to fetch.
	 * @param stopRow
	 *        The zero-based index of the first row to skip, or <code>-1</code> to fetch all rows.
	 * 
	 * @see #limitStart(StringBuilder, int, int)
	 * @see #limitColumns(StringBuilder, int, int)
	 * @see #limitLast(StringBuilder, int, int)
	 * @see #supportsLimitStart()
	 * @see #supportsLimitStop()
	 */
    public StringBuilder limitLast(StringBuilder sql, int startRow, int stopRow) {
    	return sql;
    }
    
	private RuntimeException wrapIOException(IOException ex) {
		return new RuntimeException(ex);
	}

	public boolean supportsUnicodeSupplementaryCharacters() {
		return true;
	}

	/**
	 * Whether executing a batch update provided detailed information about the updated rows.
	 * 
	 * <p>
	 * A driver that may return {@link Statement#SUCCESS_NO_INFO} as result of a batch must override
	 * this method and return <code>false</code>.
	 * </p>
	 * 
	 * @see PreparedStatement#executeBatch()
	 */
	public boolean supportsBatchInfo() {
		return true;
	}

	/**
	 * Sets the given fetch size on the given statement.
	 * 
	 * <p>
	 * Setting the fetch size allows to read the statements result in streaming
	 * mode preventing to load all result data into memory.
	 * </p>
	 * 
	 * @param stmt
	 *        The statement to set the chunk size on.
	 * @param batchSize
	 *        The chunck size.
	 * @throws SQLException
	 *         If the driver fails setting the parameters.
	 */
	public void setFetchSize(Statement stmt, int batchSize) throws SQLException {
		stmt.setFetchDirection(ResultSet.FETCH_FORWARD);
		stmt.setFetchSize(batchSize);
	}

	/**
	 * DBMS specific modifier for locking a row upon select that must be
	 * inserted direclty before WHERE.
	 * 
	 * <p>
	 * Note: In a statement, both methods {@link #forUpdate1()} and
	 * {@link #forUpdate2()} must be used to get row-level locking in a DMBS
	 * independent manner.
	 * </p>
	 * 
	 * @see #forUpdate2()
	 */
	public String forUpdate1() {
		return "";
	}
	
	/**
	 * DBMS specific modifier for locking a row upon select that must be
	 * inserted at the end of the statement.
	 *
	 * <p>
	 * Note: In a statement, both methods {@link #forUpdate1()} and
	 * {@link #forUpdate2()} must be used to get row-level locking in a DMBS
	 * independent manner.
	 * </p>
	 * 
	 * @see #forUpdate1()
	 */
	public String forUpdate2() {
		return "";
	}

	/**
	 * Create a table name pattern for use with
	 * {@link DatabaseMetaData#getColumns(String, String, String, String)} that
	 * matches the given table name (that was used to literally in the table
	 * create statement).
	 */
	public String tablePattern(String tableName) {
		return tableName;
	}

	/**
	 * Analyze the type description reported by
	 * {@link DatabaseMetaData#getColumns(String, String, String, String)}.
	 * 
	 * @param sqlType
	 *        The {@link Types} code.
	 * @param sqlTypeName
	 *        The implementation specific type name.
	 * @param size
	 *        The reported size.
	 * @param scale
	 *        The reported number of decimal digits right to the decimal point.
	 * @return The corresponding implementation independent {@link DBType}.
	 */
	public DBType analyzeSqlType(int sqlType, String sqlTypeName, int size, int scale) {
		return DBType.fromSqlType(this, sqlType, scale);
	}

	/**
	 * Analyze the type description reported by
	 * {@link DatabaseMetaData#getColumns(String, String, String, String)}.
	 * 
	 * @param sqlType
	 *        The {@link Types} code.
	 * @param sqlTypeName
	 *        The implementation specific type name.
	 * @param size
	 *        Size reported be the meta data.
	 * @param octetSize
	 *        Size in octets reported be the meta data.
	 * @return Whether the corresponding column was created with a binary
	 *         modifier.
	 */
	public boolean analyzeSqlTypeBinary(int sqlType, String sqlTypeName, int size, int octetSize) {
		return false;
	}

	/**
	 * Whether the given connection is still alive.
	 * 
	 * @param connection
	 *        the connection to test.
	 * @return Whether the given connection is alive.
	 */
    public final boolean ping(Connection connection) {
		try {
			return internalPing(connection);
		} catch (SQLException ex) {
			Logger.debug("Connection validation failed.", ex, DBHelper.class);
			return false;
		}
	}

	/**
	 * Implementation of {@link #ping(Connection)}
	 * 
	 * @param connection
	 *        The connection to test.
	 * @return Whether the given connection is still alive. Implementations can
	 *         also throw {@link SQLException}, if the given connection is no
	 *         longer alive.
	 * @throws SQLException
	 *         If the given connection is not alive.
	 */
	protected boolean internalPing(Connection connection) throws SQLException {
		DatabaseMetaData metaData = connection.getMetaData();
		try (ResultSet resultSet = metaData.getCatalogs()) {
			if (resultSet.next()) {
				resultSet.getString(1);
			}
		}
		return true;
	}

    public String fromNoTable() {
        return "";
    }

    public String selectNoBlockHint() {
        return "";
    }

    /**
     * The maximum length of table and column names.
     */
	public int getMaxNameLength() {
		return 1024;
	}

	/**
	 * Executes the given SQL and returns <code>true</code> if there is a result.
	 * 
	 * <p>
	 * Helper method for {@link #internalPing(Connection)}. It is expected that the
	 * <tt>selectSQL</tt> selects a timestamp in the first column.
	 * </p>
	 */
	protected boolean internalPing(Connection connection, String selectSQL) throws SQLException {
		try (Statement statement = connection.createStatement()) {
			try (ResultSet resultSet = statement.executeQuery(selectSQL)) {
				if (resultSet.next()) {
					resultSet.getTimestamp(1);
					return true;
				} else {
					return false;
				}
			}
		}
	}

	/**
	 * Creates the SQL to truncate given table.
	 */
	public String getTruncateTableStatement(String tableName) {
		return "TRUNCATE TABLE " + tableRef(tableName);
	}

	/**
	 * Appends a SQL set literal to the given buffer.
	 * 
	 * @param sql
	 *        The buffer to append.
	 * @param dbType
	 *        The {@link Types} type of the contents.
	 * @param values
	 *        The literal values.
	 */
	public void literalSet(Appendable sql, DBType dbType, Iterable<?> values) {
		Format format = getLiteralFormat(dbType);

		try {
			sql.append('(');
			boolean first = true;
			for (Object value : values) {
				if (first) {
					first = false;
				} else {
					sql.append(',');
				}
				sql.append(format.format(value));
			}
			sql.append(')');
		} catch (IOException ex) {
			throw wrapIOException(ex);
		}
	}

	/**
	 * Appends a SQL literal value to the given buffer.
	 * 
	 * @param sql
	 *        The buffer to append.
	 * @param dbType
	 *        The {@link Types} type of the value.
	 * @param value
	 *        The literal value.
	 */
	public void literal(Appendable sql, DBType dbType, Object value) {
		try {
			if (value == null) {
				sql.append(nullLiteral());
				return;
			}

			Format format = getLiteralFormat(dbType);
			sql.append(format.format(value));
		} catch (IOException ex) {
			throw wrapIOException(ex);
		}
	}

	/**
	 * A literal for the <code>null</code> value.
	 * 
	 * @see #nullSpec()
	 */
	public String nullLiteral() {
		return "NULL";
	}

	/**
	 * The {@link Format} to produce a literal value for the given {@link Types} type.
	 */
	protected Format getLiteralFormat(DBType dbType) {
		switch (dbType) {
			case STRING:
			case CHAR:
			case CLOB:
				return _escapeFormat;
			case BYTE:
			case SHORT:
			case INT:
			case LONG:
			case FLOAT:
			case DOUBLE:
			case DECIMAL:
				return _toStringFormat;
			case ID:
				return _idFormat;
			case TIME:
				return getTimeFormat();
			case DATE:
				return getDateFormat();
			case DATETIME:
				return getDateTimeFormat();
			case BOOLEAN:
				return getBooleanFormat();
			case BLOB:
				return getBlobFormat();
		}
		throw new IllegalArgumentException("Unsupported SQL type '" + dbType + "'");
	}

	/**
	 * {@link Format} to create a {@link DBType#BOOLEAN} literalS from a {@link Boolean}.
	 */
	protected Format getBooleanFormat() {
		return SQL_BOOLEAN_FORMAT;
	}

	/**
	 * {@link Format} to create a {@link DBType#TIME} literal from a {@link Date}.
	 */
	protected Format getTimeFormat() {
		return getDateTimeFormat();
	}

	/**
	 * {@link Format} to create a {@link DBType#DATE} literal from a {@link Date}.
	 */
	protected Format getDateFormat() {
		return getDateTimeFormat();
	}

	/**
	 * {@link Format} to create a {@link DBType#DATETIME} literal from a {@link Date}.
	 */
	protected Format getDateTimeFormat() {
		return dateFormat("''yyyy-MM-dd HH:mm:ss.SSS''");
	}

	/**
	 * {@link Format} to create a {@link DBType#BLOB} literal from a <code>byte[]</code>.
	 */
	protected Format getBlobFormat() {
		return SLQBlobFormat.INSTANCE;
	}

	/**
	 * Adds a <code>ON DELETE</code> clause to the given buffer.
	 */
	public void onDelete(Appendable buffer, DBConstraintType onDelete) throws IOException {
		buffer.append("ON DELETE ");
		buffer.append(toSql(onDelete));
	}

	/**
	 * Adds a <code>ON UPDATE</code> clause to the given buffer.
	 */
	public void onUpdate(Appendable buffer, DBConstraintType onUpdate) throws IOException {
		buffer.append("ON UPDATE ");
		buffer.append(toSql(onUpdate));
	}

	/**
	 * Translates the given {@link DBConstraintType} to an SQL keyword.
	 */
	protected String toSql(DBConstraintType constraintType) {
		switch (constraintType) {
			case CASCADE:
				return "CASCADE";
			case CLEAR:
				return "SET NULL";
			case RESTRICT:
				return "RESTRICT";
			default:
				throw new UnreachableAssertion("All types covered: " + constraintType);
		}
	}

	/**
	 * Creates a <code>DROP</code> statement for a foreign key constraint.
	 */
	public String dropForeignKey(String tableName, String foreignKeyName) {
		return alterTable(tableName) + "DROP CONSTRAINT " + tableRef(foreignKeyName);
	}

	/**
	 * Creates the "ALTER TABLE " prefix for schema modification commands.
	 */
	protected String alterTable(String tableName) {
		return "ALTER TABLE " + tableRef(tableName) + " ";
	}

	/**
	 * Creates a qualified name for a table part.
	 */
	public String qualifiedName(String tableName, String partName) {
		return tableName + '_' + partName;
	}

	/**
	 * Whether a '?' is allowed to be in a result column.
	 */
	public boolean allowParameterColumn() {
		return true;
	}

	protected DBType fromProprietarySqlType(int sqlType) {
		throw new IllegalArgumentException("JDBC type '" + sqlType + "' not supported.");
	}

	/**
	 * Fetches the {@link Timestamp} value of the given column from the given {@link ResultSet}.
	 * 
	 * <p>
	 * This method converts the stored value to the correct system time zone.
	 * </p>
	 * 
	 * @see ResultSet#getTimestamp(int, Calendar)
	 */
	public Timestamp getTimestamp(ResultSet res, int col) throws SQLException {
		return res.getTimestamp(col, getSystemCalendar());
	}

	/**
	 * Fetches the {@link Timestamp} value of the given column from the given {@link ResultSet}.
	 * 
	 * <p>
	 * This method converts the stored value to the correct system time zone.
	 * </p>
	 * 
	 * @see ResultSet#getTimestamp(String, Calendar)
	 */
	public Timestamp getTimestamp(ResultSet res, String col) throws SQLException {
		return res.getTimestamp(col, getSystemCalendar());
	}

	/**
	 * Fetches the {@link Time} value of the given column from the given {@link ResultSet}.
	 * 
	 * <p>
	 * This method converts the stored value to the correct system time zone.
	 * </p>
	 * 
	 * @see ResultSet#getTime(int, Calendar)
	 */
	public Time getTime(ResultSet res, int col) throws SQLException {
		return res.getTime(col, getSystemCalendar());
	}

	/**
	 * Fetches the {@link Time} value of the given column from the given {@link ResultSet}.
	 * 
	 * <p>
	 * This method converts the stored value to the correct system time zone.
	 * </p>
	 * 
	 * @see ResultSet#getTime(String, Calendar)
	 */
	public Time getTime(ResultSet res, String col) throws SQLException {
		return res.getTime(col, getSystemCalendar());
	}

	/**
	 * Fetches the {@link Date} value of the given column from the given {@link ResultSet}.
	 * 
	 * <p>
	 * This method converts the stored value to the correct system time zone.
	 * </p>
	 * 
	 * @see ResultSet#getDate(int, Calendar)
	 */
	public Date getDate(ResultSet res, int col) throws SQLException {
		return res.getDate(col, getSystemCalendar());
	}

	/**
	 * Fetches the {@link Date} value of the given column from the given {@link ResultSet}.
	 * 
	 * <p>
	 * This method converts the stored value to the correct system time zone.
	 * </p>
	 * 
	 * @see ResultSet#getDate(String, Calendar)
	 */
	public Date getDate(ResultSet res, String col) throws SQLException {
		return res.getDate(col, getSystemCalendar());
	}

	/**
	 * Sets the {@link Date} value to the given {@link PreparedStatement} at the given column.
	 * 
	 * <p>
	 * This method converts the value from system time zone to database time zone.
	 * </p>
	 * 
	 * @see PreparedStatement#setDate(int, Date, Calendar)
	 */
	public void setDate(PreparedStatement pstm, int col, Date date) throws SQLException {
		pstm.setDate(col, date, getSystemCalendar());
	}

	/**
	 * Sets the {@link Time} value to the given {@link PreparedStatement} at the given column.
	 * 
	 * <p>
	 * This method converts the value from system time zone to database time zone.
	 * </p>
	 * 
	 * @see PreparedStatement#setTime(int, Time, Calendar)
	 */
	public void setTime(PreparedStatement pstm, int col, Time time) throws SQLException {
		pstm.setTime(col, time, getSystemCalendar());
	}

	/**
	 * Sets the {@link Timestamp} value to the given {@link PreparedStatement} at the given column.
	 * 
	 * <p>
	 * This method converts the value from system time zone to database time zone.
	 * </p>
	 * 
	 * @see PreparedStatement#setTimestamp(int, Timestamp, Calendar)
	 */
	public void setTimestamp(PreparedStatement pstm, int col, Timestamp timestamp) throws SQLException {
		pstm.setTimestamp(col, timestamp, getSystemCalendar());
	}

	/**
	 * Returns a system calendar to use for API's like
	 * {@link PreparedStatement#setDate(int, Date, Calendar)} or
	 * {@link ResultSet#getDate(int, Calendar)}.
	 * 
	 * <p>
	 * The {@link Calendar} is thread-local stored.
	 * </p>
	 */
	protected final Calendar getSystemCalendar() {
		Calendar cal = SYSTEM_CALENDAR.get();
		if (cal == null) {
			cal = CalendarUtil.createCalendar();
			SYSTEM_CALENDAR.set(cal);
		}
		return cal;
	}

	protected Format dateFormat(String pattern) {
		SimpleDateFormat format = new SimpleDateFormat(pattern);
		format.setTimeZone(getSystemCalendar().getTimeZone());
		return format;
	}

	/**
	 * @param res
	 *        Database result set.
	 * @param blobIndex
	 *        Column index of blob.
	 * @return Blob data stream.
	 * @throws SQLException
	 *         If the column index is not valid.
	 */
	public InputStream getBinaryStream(ResultSet res, int blobIndex) throws SQLException {
		return res.getBinaryStream(blobIndex);
	}

	/**
	 * @see Connection#setSavepoint()
	 * 
	 * @param connection
	 *        Connection to a specific database.
	 * @throws SQLException
	 *         Database access error occurs.
	 */
	public Savepoint setSavepoint(Connection connection) throws SQLException {
		return null;
	}

	/**
	 * @see Connection#rollback()
	 * 
	 * @param connection
	 *        Connection to a specific database.
	 * @param savePoint
	 *        Point within the current transaction can be referenced.
	 * @throws SQLException
	 *         Database access error occurs.
	 */
	public void rollback(Connection connection, Savepoint savePoint) throws SQLException {
		// Do nothing.
	}

	/**
	 * @see Connection#releaseSavepoint(Savepoint)
	 * 
	 * @param connection
	 *        Connection to a specific database.
	 * @param savePoint
	 *        Point within the current transaction can be referenced.
	 * @throws SQLException
	 *         Database access error occurs.
	 */
	public void releaseSavepoint(Connection connection, Savepoint savePoint) throws SQLException {
		// Do nothing.
	}

	/**
	 * @param type
	 *        The database-independent type specification.
	 * @return Null Placeholder for the given type in a not null column.
	 */
	public Object getNullPlaceholder(DBType type) {
		switch (type) {
			case BOOLEAN:
				return Boolean.FALSE;
			case CHAR:
				return Character.valueOf('\u0000');
			case SHORT:
				return Short.valueOf((short) 0);
			case BYTE:
				return Byte.valueOf((byte) 0);
			case INT:
				return Integer.valueOf(0);
			case ID:
			case LONG:
				return Long.valueOf(0L);
			case FLOAT:
				return Float.valueOf(0.0f);
			case DECIMAL:
			case DOUBLE:
				return Double.valueOf(0.0d);
			case CLOB:
			case STRING:
				return NULL_PLACEHOLDER;
			case TIME:
			case DATETIME:
			case DATE:
				return new Date(0);
			case BLOB:
				return new byte[0];
		}

		throw new UnreachableAssertion("No such type: " + type);
	}

	/**
	 * Appends the SQL command to modify the name of a column.
	 * 
	 * <p>
	 * Appends the part of the "ALTER TABLE" that describes the modification of a column, i.e.
	 * "MODIFY &lt;columnName&gt; &lt;type&gt;".
	 * </p>
	 * 
	 * @see #appendChangeMandatory(Appendable, String, DBType, String, String, long, int, boolean,
	 *      boolean, Object)
	 */
	public void appendChangeColumnName(Appendable result, String tableName, DBType sqlType, String columnName, String newName,
			long size, int precision, boolean mandatory, boolean binary, Object defaultValue) throws IOException {
		result.append(alterTable(tableName));
		result.append("CHANGE ");
		result.append(columnRef(columnName));
		result.append(" ");
		result.append(columnRef(newName));
		result.append(" ");
		appendDBType(result, sqlType, newName, size, precision, mandatory, binary, defaultValue);
	}

	/**
	 * Appends the SQL command to modify the type of a column.
	 * 
	 * <p>
	 * Appends the part of the "ALTER TABLE" that describes the modification of a column, i.e.
	 * "MODIFY &lt;columnName&gt; &lt;type&gt;".
	 * </p>
	 * 
	 * @see #appendChangeMandatory(Appendable, String, DBType, String, String, long, int, boolean,
	 *      boolean, Object)
	 */
	public void appendChangeColumnType(Appendable result, String tableName, DBType sqlType, String columnName,
			String newName,
			long size, int precision, boolean mandatory, boolean binary, Object defaultValue) throws IOException {
		result.append(alterTable(tableName));
		appendModifyColumnKeyword(result);
		result.append(columnRef(columnName));
		result.append(" ");
		appendDBType(result, sqlType, newName, size, precision, mandatory, binary, defaultValue);
	}

	/**
	 * Appends the SQL command to modify the mandatory state of a column.
	 * 
	 * <p>
	 * Appends the part of the "ALTER TABLE" that describes the modification of a column, i.e.
	 * "MODIFY &lt;columnName&gt; &lt;NULL|NON NULL&gt;".
	 * </p>
	 * 
	 * @implNote Here also the type is added, because most databases needs it.
	 * 
	 * @see #appendChangeColumnType(Appendable, String, DBType, String, String, long, int, boolean,
	 *      boolean, Object)
	 */
	public void appendChangeMandatory(Appendable result, String tableName, DBType sqlType, String columnName,
			String newName,
			long size, int precision, boolean mandatory, boolean binary, Object defaultValue) throws IOException {
		result.append(alterTable(tableName));
		appendModifyColumnKeyword(result);
		result.append(columnRef(columnName));
		result.append(" ");
		appendDBType(result, sqlType, newName, size, precision, mandatory, binary, defaultValue);
	}

	/**
	 * Appends the modify column keyword.
	 */
	protected void appendModifyColumnKeyword(Appendable result) throws IOException {
		result.append("MODIFY ");
	}

}
