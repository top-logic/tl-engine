/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.sql;

import java.io.IOException;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.Format;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.DefaultConfigConstructorScheme;
import com.top_logic.basic.config.DefaultConfigConstructorScheme.Factory;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.copy.ConfigCopier;
import com.top_logic.basic.sql.MySQL55Helper.MySQL55WithBackslashEscape;

/** 
 * This class helps using MySQL
 * <p>
 *  It helps working around this _small_ differences in these drivers.
 *  The thin driver descends from this class since it needs more help.
 * </p>
 * @author  <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class MySQLHelper extends DBHelper { 

	/**
	 * Configuration options for {@link MySQLHelper}.
	 */
	public interface Config extends DBHelper.Config {
		// No additional properties, just to be able to configure different application-wide
		// defaults.
	}

	/**
	 * {@link MySQLHelper} with backslash as escape symbol.
	 * 
	 * @see MySQL55Helper.MySQL55WithBackslashEscape Version for MySQL 5.5
	 */
	public final static class MySQLWithBackslashEscape extends MySQLHelper {

		/**
		 * Creates a {@link MySQLHelper.MySQLWithBackslashEscape} from configuration.
		 * 
		 * @param context
		 *        The context for instantiating sub configurations.
		 * @param config
		 *        The configuration.
		 */
		@CalledByReflection
		public MySQLWithBackslashEscape(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		protected void internalEscape(Appendable out, String s) throws IOException {
			backslashEscape(out, s);
		}

	}

	private static final long MAX_TINYTEXT_SIZE   = (1L << 8) - 1;
	private static final long MAX_TEXT_SIZE       = (1L << 16) - 1;
	private static final long MAX_MEDIUMTEXT_SIZE = (1L << 24) - 1;
	
	private static final long MAX_TINYBLOB_SIZE   = (1L << 8) - 1;
	private static final long MAX_BLOB_SIZE       = (1L << 16) - 1;
	private static final long MAX_MEDIUMBLOB_SIZE = (1L << 24) - 1;

	/**
	 * Creates a {@link MySQLHelper} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public MySQLHelper(InstantiationContext context, Config config) {
		super(context, config);
	}

    /** 
     * Perform checks required for the DBHelper or TopLogic to function on aStm.
     */
    @Override
	protected void internalCheck(Statement stmt) throws SQLException {
        final String encodingQuery = "SHOW GLOBAL VARIABLES LIKE 'character_set_database'";
		try (ResultSet encodingResult = stmt.executeQuery(encodingQuery)) {
            if (encodingResult.next()) {
                String databaseEncoding = encodingResult.getString(2);
				Logger.info("MySQL server character set is '" + databaseEncoding + "'.", MySQLHelper.class);
            } else {
                Logger.error("Failed to determine the database character set: " + encodingQuery, MySQLHelper.class);
            }
        }
        
		try (ResultSet engineResult = stmt.executeQuery("SHOW ENGINES")) {
        	StringBuilder engines = new StringBuilder();
        	while (engineResult.next()) {
        		String engine = engineResult.getString(1);
        		if (engines.length() > 0) {
        			engines.append(", ");
        		}
        		engines.append(engine);
        	}
    		Logger.info("MySQL with support for the following engines: " + engines, MySQLHelper.class);
		}
    }

	@Override
	protected void appendBooleanType(Appendable result, boolean mandatory) throws IOException {
		result.append("TINYINT");
	}

	@Override
	protected void appendCharType(Appendable result, boolean mandatory, boolean binary, boolean castContext) throws IOException {
		super.appendCharType(result, mandatory, binary, castContext);
    	appendBinaryModifier(result, binary);
	}
	
	@Override
	protected void appendStringType(Appendable result, String columnName, long size, boolean mandatory, boolean binary, boolean castContext) throws IOException {
		super.appendStringType(result, columnName, size, mandatory, binary, castContext);
    	appendBinaryModifier(result, binary);
	}

	/**
	 * Mark the generated type as binary or Unicode depending on the given flag.
	 * 
	 * @param binary Whether the binary or Unicode marker should be written.
	 */
	protected void appendBinaryModifier(Appendable result, boolean binary) throws IOException {
		result.append(" CHARACTER SET ");
		if (binary) {
			internalEscape(result, getBinaryCharsetName());
			result.append(" BINARY");
		} else {
			internalEscape(result, getUnicodeCharsetName());
		}
	}

	@Override
	protected void appendBlobType(Appendable result, long size, boolean mandatory) throws IOException {
		if (size == 0) {
			result.append("LONGBLOB");
		} else if (size <= MAX_TINYBLOB_SIZE) {
			result.append("TINYBLOB");
		} else if (size <= MAX_BLOB_SIZE) {
			result.append("BLOB");
		} else if (size <= MAX_MEDIUMBLOB_SIZE) {
			result.append("MEDIUMBLOB");
		} else {
			result.append("LONGBLOB");
		}
	}

	@Override
	protected void appendClobType(Appendable result, String columnName, long size, boolean mandatory, boolean binary, boolean castContext) throws IOException {
		if (size == 0) {
			result.append("LONGTEXT");
		} else if (size <= MAX_TINYTEXT_SIZE) {
			result.append("TINYTEXT");
		} else if (size <= MAX_TEXT_SIZE) {
			result.append("TEXT");
		} else if (size <= MAX_MEDIUMTEXT_SIZE) {
			result.append("MEDIUMTEXT");
		} else {
			result.append("LONGTEXT");
		}
    	appendBinaryModifier(result, binary);
	}
    
    @Override
    protected void internalAppendCollatedExpression(Appendable buffer, String sqlExpression, CollationHint collationHint) throws IOException {
    	switch (collationHint) {
    	case NONE: 
    		super.internalAppendCollatedExpression(buffer, sqlExpression, collationHint);
    		break;
    	case BINARY:
    		buffer.append("CAST(");
        	super.internalAppendCollatedExpression(buffer, sqlExpression, collationHint);
    		buffer.append(" AS BINARY)");
    		break;
    	case NATURAL:
    		buffer.append("CONVERT(");
    		super.internalAppendCollatedExpression(buffer, sqlExpression, collationHint);
    		buffer.append(" USING ");
            internalEscape(buffer, getUnicodeCharsetName()); 
    		buffer.append(')');
				buffer.append(" COLLATE ");
				buffer.append(getDefaultCollation());
    		break;
    	}
    }
    
    @Override
	protected void internalAppendCastExpression(Appendable buffer, String sqlExpression, DBType dbType, long size,
			int precision, boolean binary) throws IOException {

		buffer.append("CAST(");
		buffer.append(sqlExpression);
		buffer.append(" AS ");
		switch (dbType) {
			case BOOLEAN:
			case BYTE:
			case CHAR:
			case SHORT:
			case INT: {
				buffer.append("SIGNED");
				break;
			}

			case LONG: {
				buffer.append("DECIMAL(20,0)");
				break;
			}

			case ID: {
				if (IdentifierUtil.SHORT_IDS) {
					buffer.append("DECIMAL(" + IdentifierUtil.REFERENCE_DB_SIZE + ",0)");
				} else {
					buffer.append("BINARY");
				}
				break;
			}

			case DOUBLE: {
				buffer.append("DECIMAL");
				break;
			}
			case FLOAT: {
				buffer.append("DECIMAL");
				break;
			}
			case DECIMAL: {
				buffer.append("DECIMAL(");
				StringServices.append(buffer, size);
				buffer.append(",");
				StringServices.append(buffer, precision);
				buffer.append(")");
				break;
			}

			case BLOB:
			case CLOB:
			case STRING: {
				if (binary) {
					buffer.append("BINARY");
					// Note: Adding a size specifier applies a padding with 0x00 bytes, which is not
					// what one would expect.
				} else {
					buffer.append("CHAR");
					if (size > 0) {
						buffer.append("(");
						StringServices.append(buffer, size);
						buffer.append(")");
					}
				}
				break;
			}

			case DATE: {
				buffer.append("DATE");
				break;
			}
			case DATETIME: {
				buffer.append("DATETIME");
				break;
			}
			case TIME: {
				buffer.append("TIME");
				break;
			}
		}
		buffer.append(")");
	}

	private String getDefaultCollation() {
		return "utf8mb4_general_ci";
	}

	/**
	 * Default character set for non binary columns.
	 */
	private String getUnicodeCharsetName() {
		return "utf8mb4";
	}
    
    /**
     * Character set to use for binary marked columns.
     */
    private String getBinaryCharsetName() {
    	return "latin1";
    }
    
    /**
     * Get a database (vendor/version) specific datatype for the sql datatype.
     * 
     * @param   sqlType a type as defined in java.sql.Types
     * @return  the db specific datatype
     */
    @Override
    protected String internalGetDBType(int sqlType, boolean binary) {

        switch (sqlType) {
            // MySQL has some special ideas about these 
            
            case Types.BOOLEAN:  
            case Types.BIT:             return "TINYINT";
            
            case Types.LONGNVARCHAR: /* Intentional fall-through */
            case Types.LONGVARCHAR:
            	// For compatibility with existing KBMeta.xml definitions.
            	return "TEXT";
            	
            case Types.NCLOB: /* Intentional fall-through */
            case Types.CLOB:
            	//  return "LONGTEXT";   // 2^32
            	//  return "MEDIUMTEXT"; // 2^24
            	//  return "TEXT";		 // 2^16
            	return "LONGTEXT";

            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
            case Types.BLOB:
            	return "LONGBLOB";

			case Types.TIMESTAMP:
				return "DATETIME(3)";
        }
        return super.internalGetDBType(sqlType, binary);
    }
    
    @Override
    public boolean noSize(DBType sqlType) {
        switch (sqlType) {
			case CLOB:
			case BLOB:
				return true;
			default:
				return super.noSize(sqlType);
        }
    }
    
    /** We like Long much more than java.math.Bigint */
    @Override
    public String getColumnTypeName(ResultSetMetaData meta, int aColumn) throws SQLException {
        String result = meta.getColumnClassName(aColumn);
        if ("java.math.BigInteger".equals(result)) {
            return Long.class.getName();
        } else if ("[B".equals(result)) {
            // As of http://dev.mysql.com/doc/refman/5.0/en/cj-news-3-0-11.html
            // JDBC complieance
            return Blob.class.getName();
        }
        return result;
    }
    
    @Override
	protected void internalAppendTableOptions(Appendable out, boolean usePKeyStorage, int compress) throws IOException {
        // PACK KEYS in only relevant for MyISAM :-(
        out.append("ENGINE = InnoDB");
        
        out.append(" CHARACTER SET ");
        internalEscape(out, getUnicodeCharsetName()); 
	}
        
    /** MySQL supports a C/Java like syntax */
	@Override
    public String fnBitAnd(String expr1, String expr2)
    {
        return "(" + expr1 + " & " + expr2 + ")";
    }

    /** 
     * Truncate x to y number of decimal places. 
     * 
     * @see "MySQL Manual 12.4.2. Mathematical Functions"
     */ 
    @Override
    public String fnTruncate(String x, int y)
    {
        return "TRUNCATE(" + x + ',' + y + ")";
    }

    /** 
     * Round x to y number of decimal places. 
     * 
     * @see "MySQL Manual 12.4.2. Mathematical Functions"
     */ 
    @Override
    public String fnRound(String x, int y)
    {
        return "ROUND(" + x + ',' + y + ")";
    }

    /** 
     * Return true when there is a chance that a retry of statemets will succeed.
     * 
     * @param sqlX the Exception for an SQLStatemrn we may retry.
     */
    @Override
    public boolean canRetry(SQLException sqlX) {
        
        // This is from some MySQL docs  (Example 5.1 Example of transaction with retry logic).
        // They should not harm with other DBs as well.
        String sqlState  = sqlX.getSQLState();
        if (sqlState != null  
          && ("08S01".equals(sqlState)     // communications error
           || "08003".equals(sqlState)     // Connection.close() has already been called....    
           || "41000".equals(sqlState)     // deadlock
           || "40001".equals(sqlState)     // deadlock
           || "S1009".equals(sqlState)))   // PreparedStatement has been closed / testing
            return true;
        
        // int errorCode = sqlX.getErrorCode();
        // if (errorCode == 17009)           // Closed Statement / (Geschlossene Anweisung ;-)
        //    return true;
        
        return super.canRetry(sqlX);
    }
    
    @Override
	public void analyzeTable(Statement aStmt, String tableName) throws SQLException {
		aStmt.execute("ANALYZE TABLE " + tableRef(tableName));
    }

    @Override
	public void optimizeTable(Statement aStmt, String tableName) throws SQLException {
		aStmt.execute("OPTIMIZE TABLE " + tableRef(tableName));
    }

    @Override
    public String columnRef(String columnName) {
		return '`' + columnName + '`';
	}
	
	@Override
	public String tableRef(String tableName) {
		return '`' + tableName + '`';
	}

	@Override
	public boolean supportsDistinctLob() {
		return true;
	}

	@Override
	public final boolean supportsLimitStart() {
		return true;
	}
	
	@Override
	public final boolean supportsLimitStop() {
		return true;
	}

	@Override
	public final StringBuilder limitLast(StringBuilder sql, int startRow, int stopRow) {
    	if (startRow > 0) {
    		sql.append(" LIMIT ").append(startRow).append(',');
    		if (stopRow >= 0) {
    			sql.append(stopRow - startRow);
    		} else {
    			sql.append("18446744073709551615");
    		}
    	} else if (stopRow >= 0) {
    		sql.append(" LIMIT ").append(stopRow);
    	}
        return sql;
    }
	
	@Override
	public boolean supportsUnicodeSupplementaryCharacters() {
		// See Ticket #1056, Ticket #1854, TestFlexWrapperCluster.
		return false;
	}

	@Override
	protected boolean canBlobCompare() {
		return true;
	}

	@Override
	protected boolean canClobCompare() {
		return true;
	}

	@Override
	public void setFetchSize(Statement stmt, int batchSize) throws SQLException {
		// MySQL hack to convince the server to send results as stream.
		//
		// See http://dev.mysql.com/doc/refman/5.0/en/connector-j-reference-implementation-notes.html
		if (stmt.getResultSetType() != ResultSet.TYPE_FORWARD_ONLY) {
			throw new UnsupportedOperationException(
				"Result set streaming only supported with statements constructed with type ResultSet.TYPE_FORWARD_ONLY.");
		}
		if (stmt.getResultSetConcurrency() != ResultSet.CONCUR_READ_ONLY) {
			throw new UnsupportedOperationException(
			"Result set streaming only supported with statements constructed with concurrency ResultSet.CONCUR_READ_ONLY.");
		}
		stmt.setFetchSize(Integer.MIN_VALUE);
	}

	@Override
	protected void internalAppendComment(Appendable sql, String aComment) throws IOException {
	    sql.append(" COMMENT ");
	    escape(sql, aComment);
	}

	
	@Override
	public String forUpdate2() {
		return " FOR UPDATE";
	}
	
	@Override
	public DBType analyzeSqlType(int sqlType, String sqlTypeName, int size, int scale) {
		switch (sqlType) {
		case Types.REAL:
			if (size <= 24) {
				return DBType.FLOAT;
			} else {
				return DBType.DOUBLE;
			}
		case Types.NVARCHAR: /* Intentional fall-through */
		case Types.VARCHAR:
			if (sqlTypeName.equals("TINYTEXT") || sqlTypeName.equals("TEXT") || sqlTypeName.equals("MEDIUMTEXT") || sqlTypeName.equals("LONGTEXT")) {
				return DBType.CLOB;
			} else {
				return DBType.STRING;
			}
		}
		return super.analyzeSqlType(sqlType, sqlTypeName, size, scale);
	}
	
	@Override
	public boolean analyzeSqlTypeBinary(int sqlType, String sqlTypeName, int size, int octetSize) {
		// Cannot detect.
		return false;
	}
	
	@Override
	protected boolean internalPing(Connection connection) throws SQLException {
		return internalPing(connection, "SELECT " + fnNow() + " FROM DUAL");
	}

	@Override
	public int getMaxNameLength() {
		return 64;
	}

	@Override
	protected Format getTimeFormat() {
		return dateFormat("''HH:mm:ss''");
	}

	@Override
	protected Format getDateFormat() {
		return dateFormat("''yyyy-MM-dd''");
	}

	@Override
	protected Format getDateTimeFormat() {
		return dateFormat("''yyyy-MM-dd HH:mm:ss.SSS''");
	}

	/**
	 * Chooses the {@link MySQLHelper} according to the current settings of the server.
	 */
	public static Class<? extends MySQLHelper> getInstance(Connection connection) throws SQLException {
		DatabaseMetaData meta = connection.getMetaData();
		int major = meta.getDatabaseMajorVersion();
		int minor = meta.getDatabaseMinorVersion();
		boolean need55 = false;
		if (major == 5 && minor <= 5) {
			need55 = true;
		}
		Statement stmt = connection.createStatement();
		try {
			String modeString = "";
			ResultSet modeResult = stmt.executeQuery("SELECT @@SESSION.sql_mode;");
			try {
				if (modeResult.next()) {
					modeString = modeResult.getString(1);
				}
			} finally {
				modeResult.close();
			}

			Set<String> modeSet = new HashSet<>(Arrays.asList(modeString.split("\\s*,\\s*")));
			if (modeSet.contains("NO_BACKSLASH_ESCAPES")) {
				if (need55) {
					return MySQL55Helper.class;
				} else {
					return MySQLHelper.class;
				}
			} else {
				if (need55) {
					return MySQL55WithBackslashEscape.class;
				} else {
					return MySQLWithBackslashEscape.class;
				}
			}
		} finally {
			stmt.close();
		}
	}

	@Override
	protected DBHelper internalInit(Connection con) throws SQLException {
		Class<? extends MySQLHelper> implClass = getInstance(con);
		if (implClass == this.getClass()) {
			return super.internalInit(con);
		}

		try {
			InstantiationContext context = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY;
			Factory factory = DefaultConfigConstructorScheme.getFactory(implClass);

			@SuppressWarnings("unchecked")
			Class<? extends DBHelper.Config> configType =
				(Class<? extends com.top_logic.basic.sql.DBHelper.Config>) factory.getConfigurationInterface();
			DBHelper.Config configCopy = TypedConfiguration.newConfigItem(configType);
			ConfigCopier.copyContent(context, getConfig(), configCopy);
			configCopy.setImplementationClass(implClass);
			return context.getInstance(configCopy);
		} catch (ConfigurationException ex) {
			throw new ConfigurationError("Unable to create SQL dialect implementation.", ex);
		}
	}

	@Override
	public String dropForeignKey(String tableName, String foreignKeyName) {
		return "ALTER TABLE " + tableRef(tableName) + " DROP FOREIGN KEY " + tableRef(foreignKeyName);
	}

	/**
	 * Quotes the given string by replacing <code>'</code> by <code>''</code> and <code>\</code> by
	 * <code>\\</code>.
	 */
	protected void backslashEscape(Appendable out, String s) throws IOException {
		char quote = stringQuoteChar();
		out.append(quote);
		for (int n = 0, cnt = s.length(); n < cnt; n++) {
			char ch = s.charAt(n);
			switch (ch) {
				case '\'':
					out.append('\'');
					out.append(ch);
					break;
				case '\\':
					out.append('\\');
					out.append(ch);
					break;
				default:
					out.append(ch);
					break;
			}
		}
		out.append(quote);
	}

}
