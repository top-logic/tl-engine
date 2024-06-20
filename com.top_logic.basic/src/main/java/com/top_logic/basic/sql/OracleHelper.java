/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.sql;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Types;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.zip.CRC32;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.defaults.StringDefault;

/**
 * This class helps using Oracle before Version 10 using any driver.
 * 
 * This implementation is only here to allow parts of TL to identify the Oracle DB.
 * 
 * @author <a href=mailto:kha@top-logic.com>Klaus Halfmann</a>
 */
public abstract class OracleHelper extends DBHelper { 
    
	/** Oracle SQL type representing "binary float". */
	protected static final int BINARY_FLOAT = 100;

	/** Oracle SQL type representing "binary double". */
	protected static final int BINARY_DOUBLE = 101;

	/**
	 * Configuration options for {@link OracleHelper}.
	 */
	public interface Config extends DBHelper.Config {
		/**
		 * The character set that is used by the server to encode NCHAR data.
		 */
		@StringDefault("AL32UTF8")
		String getCharset();

		/**
		 * @see #getCharset()
		 */
		void setCharset(String charset);
	}

	private Format _clobEscapeFormat = new Format() {

		int _splitSize = 2000;

		@Override
		public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
			String string = obj.toString();
			int stringLength = string.length();
			if (stringLength < _splitSize) {
				escape(toAppendTo, string);
				return toAppendTo;
			}

			int startIndex = 0;
			do {
				int endIndex = Math.min(startIndex + _splitSize, stringLength);
				toAppendTo.append("to_clob(");
				escape(toAppendTo, string.substring(startIndex, endIndex));
				toAppendTo.append(')');
				if (endIndex == stringLength) {
					break;
				}
				toAppendTo.append("||");
				startIndex = endIndex;
			} while (true);

			return toAppendTo;
		}

		@Override
		public Object parseObject(String source, ParsePosition pos) {
			throw new UnsupportedOperationException();
		}

	};

	private final long _nVarcharLimit;

	/**
	 * Creates a {@link OracleHelper} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public OracleHelper(InstantiationContext context, Config config) {
		super(context, config);

		long configuredLimit = config.getNVarcharLimit();
		int charsetLimit = computeNVarcharLimit(config.getCharset());
		_nVarcharLimit = Math.min(configuredLimit, charsetLimit);
	}
	
	@Override
	public boolean supportsBatchInfo() {
		return false;
	}

    /** 
     * Perform checks required for the DBHelper or TopLogic to function on aStm.
     */
    @Override
	protected void internalCheck(Statement aStm) throws SQLException {
        checkSysNLS(aStm, "NLS_CHARACTERSET");
        checkSysNLS(aStm, "NLS_NCHAR_CHARACTERSET");
    }

	@Override
	public boolean supportsUnicodeSupplementaryCharacters() {
		return false;
	}

    /** 
     * Check for expected Parameters in <code>SYS.NLS_DATABASE_PARAMETERS</code>
     */
    private void checkSysNLS(Statement aStm, String param) throws SQLException {
    	String value = getSysNLS(aStm, param);
		if (value != null) {
        	Logger.info("Oracle server parameter '" + param + "' is '" + value + "'.", this);
        }
    }

	private String getSysNLS(Statement statement, String param) throws SQLException {
		String value;

		// TODO: Workaround for not having a PreparedStatement in the signature.
		try (PreparedStatement stmt = statement.getConnection().prepareStatement(
			"SELECT VALUE FROM SYS.NLS_DATABASE_PARAMETERS WHERE PARAMETER=?")) {
			stmt.setString(1, param);
			try (ResultSet res = stmt.executeQuery()) {
				if (res.next()) {
					value = res.getString(1);
				} else {
					Logger.error("Could not determine database parameter '" + param + "'.", OracleHelper.class);
					value = null;
				}
			}
        }
		return value;
	}

    @Override
	protected DBHelper internalInit(Connection con) throws SQLException {
		try (Statement statement = con.createStatement()) {
			String charset = getSysNLS(statement, "NLS_NCHAR_CHARACTERSET");
			int maxNVarchar = computeNVarcharLimit(charset);
			Config config = (Config) getConfig();

			// Note: Must not use _nVarcharLimit for check, since this limit already includes the
			// "configured" charset, which might not match the charset actually used by the server.
			long configuredLimit = config.getNVarcharLimit();

			if (configuredLimit > maxNVarchar) {
				// Create new instance with adjusted limit.
				Config copy = TypedConfiguration.copy(config);
				copy.setNVarcharLimit(maxNVarchar);
				copy.setCharset(charset);
				return SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(copy);
			} else {
				return super.internalInit(con);
			}
		}
    }

	private int computeNVarcharLimit(String charset) {
		// There is an internal limitation in Oracle that a VARCHAR value can occupy only 4000
		// byte. Depending on the character set used, this translates to a different number of
		// characters that can be safely stored in such column.
		int maxNVarchar;
		if ("AL16UTF16".equals(charset)) {
			// Each character occupies 2 byte. Characters form an Unicode supplementary plane
			// occupy 4 bytes, but those count as 2 characters in Java, too.
			maxNVarchar = 2000;
		} else if ("WE8ISO8859P1".equals(charset)) {
			// Each character coccupies exactly 1 byte.
			maxNVarchar = 4000;
		} else {
			// Assume AL32UTF8 (utf-8) as worst case.
			//
			// A character may occupy up to 5 bytes. But characters from the base multi-lingual
			// plane only occupy up to 3 bytes. Since characters form
			// an Unicode supplementary plane count as 2 characters in Java, these require no
			// special handling.
			maxNVarchar = 1333;
		}
		return maxNVarchar;
	}

	@Override
	protected long getNVarcharLimit() {
		return _nVarcharLimit;
	}
    
    @Override
    protected void internalAppendCollatedExpression(Appendable buffer, String sqlExpression, CollationHint collationHint) throws IOException {
    	switch (collationHint) {
    	case NONE: 
    		super.internalAppendCollatedExpression(buffer, sqlExpression, collationHint);
    		break;
    	case BINARY:
            buffer.append("NLSSORT(");
            buffer.append(sqlExpression);
            buffer.append(",'NLS_SORT = BINARY')");
    		break;
    	case NATURAL:
    		buffer.append("NLSSORT(");
    		buffer.append(sqlExpression);
    		buffer.append(",'NLS_SORT = GENERIC_M_AI')");
    		break;
    	}
    }
    
    /** Oracle (10) has an explicit BITAND function */ 
    @Override
    public String fnBitAnd(String expr1, String expr2)
    {
        return "BITAND(" + expr1 + ',' + expr2 + ")";
    }

    /** 
     * Round x to y number of decimal places. 
     * 
     * @see "Oracle® Database SQL Language Reference 11g Release 1, 5 Functions"
     */ 
    @Override
    public String fnTruncate(String x, int y)
    {
        return "TRUNC(" + x + ',' + y + ")";
    }

    /** 
     * Round x to y number of decimal places. 
     * 
     * @see "Oracle® Database SQL Language Reference 11g Release 1, 5 Functions"
     */ 
    @Override
    public String fnRound(String x, int y)
    {
        return "ROUND(" + x + ',' + y + ")";
    }

    @Override
    public String columnRef(String columnName) {
		return '"' + columnName + '"';
	}
	
    @Override
	public final boolean supportsLimitStart() {
		return false;
	}
	
    @Override
	public final boolean supportsLimitStop() {
		return true;
	}
	
    @Override
	public StringBuilder limitStart(StringBuilder sql, int startRow, int stopRow) {
		boolean hasStopRow = stopRow >= 0;
		if (hasStopRow) {
			sql.append("SELECT * FROM (");
		}
		return sql;
	}
	
    @Override
	public StringBuilder limitLast(StringBuilder sql, int startRow, int stopRow) {
		// Note: ROWNUM cannot be used to (easily) skip the first rows for supporting startRow
		// limits.
		//
		// See: http://docs.oracle.com/cd/B19306_01/server.102/b14200/pseudocolumns009.htm
		//
		// Conditions testing for ROWNUM values greater than a positive integer are always false.
		// For example, this query returns no rows:
		//
		// SELECT * FROM employees
		// WHERE ROWNUM > 1;
		//
		// The first row fetched is assigned a ROWNUM of 1 and makes the condition false. The second
		// row to be fetched is now the first row and is also assigned a ROWNUM of 1 and makes the
		// condition false. All rows subsequently fail to satisfy the condition, so no rows are
		// returned.
		boolean hasStopRow = stopRow > 0;
		if (hasStopRow) {
			sql.append(") WHERE");
			if (hasStopRow) {
				sql.append(" ROWNUM <= ");
				sql.append(stopRow);
			}
		}
    	return sql;
    }
    
    /**
     * Appends in <code>[ORGANISATION INDEX [COMPRES compress]]</code>.
     * 
     * @see DBHelper#internalAppendTableOptions(Appendable, boolean, int) 
     */
    @Override
    protected void internalAppendTableOptions(Appendable out, boolean usePKeyStorage, int compress) throws IOException {
        if (usePKeyStorage) {
            if (compress > 0) {
                out.append(" ORGANIZATION INDEX COMPRESS ");
                out.append(Integer.toString(compress));
            } else {
            	out.append(" ORGANIZATION INDEX");
            }
        }
    }
    
    /**
     * @see com.top_logic.basic.sql.DBHelper#getAppendIndex(int)
     * 
     * This will result in <code>[ COMPRES compress]</code>.
     */
    @Override
    public String getAppendIndex(int compress) {
        if (compress > 0) {
            return " COMPRESS " + compress;
        }
        return "";
    }

	/**
	 * Returns char encoding for nchar type (non variable).
	 */
    public String getNCharCharsetName() {
        return "UTF8";
    }

    @Override
    public String forUpdate2() {
    	return " FOR UPDATE";
    }

	@Override
	protected boolean internalPing(Connection connection) throws SQLException {
		return internalPing(connection, "SELECT " + fnNow() + " FROM DUAL");
	}

	@Override
	public String fromNoTable() {
	    return " FROM DUAL";
	}

	@Override
	public int getMaxNameLength() {
		return 30;
	}

	@Override
	public void onDelete(Appendable buffer, DBConstraintType onDelete) throws IOException {
		if (onDelete == DBConstraintType.RESTRICT) {
			// This is the default and cannot explicitly be given as option.
			return;
		}
		super.onDelete(buffer, onDelete);
	}

	@Override
	public void onUpdate(Appendable buffer, DBConstraintType onUpdate) throws IOException {
		// Not supported at all.
	}

	@Override
	public String qualifiedName(String tableName, String partName) {
		String qName = super.qualifiedName(tableName, partName);
		if (qName.length() > 16) {
			// Quirks to make index names short.
			CRC32 crc32 = new CRC32();
			try {
				crc32.update(qName.getBytes("UTF-8"));
			} catch (UnsupportedEncodingException e) {
				throw new UnreachableAssertion(e);
			}
			return partName.substring(0, Math.min(partName.length(), 7)) + '_' + Long.toHexString(crc32.getValue());
		}

		return qName;
	}

	@Override
	protected Format getBooleanFormat() {
		return BooleanAsBitFormat.INSTANCE;
	}

	@Override
	protected Format getBlobFormat() {
		return OracleBlobFormat.INSTANCE;
	}

	@Override
	protected DBType fromProprietarySqlType(int sqlType) {
		switch (sqlType) {
			case BINARY_FLOAT:
				return DBType.FLOAT;
			case BINARY_DOUBLE:
				return DBType.DOUBLE;
		}
		return super.fromProprietarySqlType(sqlType);
	}

	@Override
	protected Format getLiteralFormat(DBType dbType) {
		switch (dbType) {
			case CLOB:
				return _clobEscapeFormat;
			default:
				return super.getLiteralFormat(dbType);
		}
	}

	@Override
	public Savepoint setSavepoint(Connection db) throws SQLException {
		return null;
	}

	@Override
	public void rollback(Connection db, Savepoint savePoint) throws SQLException {
		// Not supported.
	}

	@Override
	public void releaseSavepoint(Connection db, Savepoint savePoint) throws SQLException {
		// Not supported.
	}

	/**
	 * Return if some type needs a size.
	 * 
	 * @param sqlType
	 *        a type as defined in java.sql.Types
	 * @return true when size is needed.
	 */
	@Override
	public boolean noSize(DBType sqlType) {

		switch (sqlType) {
			case BOOLEAN:
			case CLOB:
			case BYTE:
			case SHORT:
			case INT:
			case LONG:
			case FLOAT:
			case DOUBLE:
			case DATE:
			case TIME:
			case DATETIME:
				return true;
			case BLOB:
			case CHAR:
			case DECIMAL:
			case STRING:
				return false;
			default:
				return super.noSize(sqlType);
		}
	}

	@Override
	protected void appendBooleanType(Appendable result, boolean mandatory) throws IOException {
		result.append("NUMBER(1)");
	}

	@Override
	protected void appendShortType(Appendable result, boolean mandatory) throws IOException {
		// -32768
		result.append("NUMBER(6)");
	}

	@Override
	protected void appendIntType(Appendable result, boolean mandatory) throws IOException {
		// -2147483648
		result.append("NUMBER(11)");
	}

	@Override
	protected void appendLongType(Appendable result, boolean mandatory) throws IOException {
		// -9223372036854775808
		result.append("NUMBER(20)");
	}

	@Override
	protected void appendDoubleType(Appendable result, boolean mandatory) throws IOException {
		result.append("BINARY_DOUBLE");
	}

	@Override
	protected void appendFloatType(Appendable result, boolean mandatory) throws IOException {
		result.append("BINARY_FLOAT");
	}

	@Override
	protected void appendCharType(Appendable result, boolean mandatory, boolean binary, boolean castContext)
			throws IOException {
		if (binary) {
			result.append("CHAR(1)");
		} else {
			result.append("NCHAR(1)");
		}
	}

	@Override
	protected void appendStringType(Appendable result, String columnName, long size, boolean mandatory, boolean binary,
			boolean castContext) throws IOException {
		if (binary) {
			result.append("VARCHAR2");
			result.append('(');
			result.append(Long.toString(size));
			result.append(" CHAR)");
		} else {
			result.append("NVARCHAR2");
			size(result, size);
		}
	}

	@Override
	protected void appendClobType(Appendable result, String columnName, long size, boolean mandatory, boolean binary,
			boolean castContext) throws IOException {
		if (binary) {
			result.append("CLOB");
		} else {
			result.append("NCLOB");
		}
	}

	@Override
	public DBType analyzeSqlType(int sqlType, String sqlTypeName, int size, int scale) {
		switch (sqlType) {
			case BINARY_FLOAT:
				return DBType.FLOAT;

			case BINARY_DOUBLE:
				return DBType.DOUBLE;

			case Types.DECIMAL: {
				if (scale == 0) {
					if (size <= 1) {
						return DBType.BOOLEAN;
					} else if (size <= 4) {
						return DBType.BYTE;
					} else if (size <= 6) {
						return DBType.SHORT;
					} else if (size <= 11) {
						return DBType.INT;
					} else if (size <= 20) {
						return DBType.LONG;
					}
				}

				return DBType.DECIMAL;
			}

			case Types.OTHER: {
				if ("NCHAR".equals(sqlTypeName.toUpperCase())) {
					return DBType.CHAR;
				} else if ("TIME".equals(sqlTypeName.toUpperCase())) {
					return DBType.TIME;
				} else if (sqlTypeName.toUpperCase().startsWith("TIMESTAMP")) {
					// Actually "TIMESTAMP(4)"
					return DBType.DATETIME;
				} else if ("NVARCHAR2".equals(sqlTypeName.toUpperCase())) {
					return DBType.STRING;
				} else if ("NCLOB".equals(sqlTypeName.toUpperCase())) {
					return DBType.CLOB;
				} else {
					throw new IllegalArgumentException("JDBC type '" + sqlTypeName + "' not supported.");
				}
			}
		}
		return super.analyzeSqlType(sqlType, sqlTypeName, size, scale);
	}

	@Override
	public boolean analyzeSqlTypeBinary(int sqlType, String sqlTypeName, int size, int octetSize) {
		switch (sqlType) {
			case Types.NCHAR: /* Intentional fall-through */
			case Types.NVARCHAR: /* Intentional fall-through */
			case Types.NCLOB:
				return false;
			case Types.CHAR:
			case Types.VARCHAR:
			case Types.CLOB:
				return true;
		}
		return super.analyzeSqlTypeBinary(sqlType, sqlTypeName, size, octetSize);
	}

	/**
	 * Get a database (vendor/version) specific data-type for the SQL data-type.
	 * 
	 * @param sqlType
	 *        a type as defined in java.sql.Types
	 * @return the database specific data-type
	 */
	@Override
	protected String internalGetDBType(int sqlType, boolean binary) {

		switch (sqlType) {
			case Types.BIGINT:
				return "NUMBER(20)"; // -9223372036854775808
			case Types.INTEGER:
				return "NUMBER(11)"; // -2147483648
			case Types.SMALLINT:
				return "NUMBER(6)"; // -32768
			case Types.TINYINT:
				return "NUMBER(4)"; // -127
			case Types.BOOLEAN:
			case Types.BIT:
				return "NUMBER(1)"; // Supported by Driver this way, well
			case Types.DOUBLE:
				return "BINARY_DOUBLE";
			case Types.FLOAT:
				return "BINARY_FLOAT";

			case Types.NVARCHAR: /* Intentional fall-through */
			case Types.VARCHAR:
			case Types.LONGNVARCHAR: /* Intentional fall-through */
			case Types.LONGVARCHAR:
				return binary ? "VARCHAR2" : "NVARCHAR2";
			case Types.NCHAR: /* Intentional fall-through */
			case Types.CHAR:
				return binary ? "CHAR" : "NCHAR";
			case Types.NCLOB: /* Intentional fall-through */
			case Types.CLOB:
				return binary ? "CLOB" : "NCLOB";

			case Types.DATE:
				return "DATE";
			case Types.TIME:
			case Types.TIMESTAMP:
				return "TIMESTAMP(4)"; // Match Java milliseconds
			case Types.BINARY:
				return "RAW";
			case Types.VARBINARY:
				return "LONG RAW";
			case Types.LONGVARBINARY:
				return "BLOB";
			default:
		}
		return super.internalGetDBType(sqlType, binary);
	}

	/**
	 * Return the name of the current schema via SELECT SYS_CONTEXT.
	 * 
	 * Is there a better solution ?
	 * 
	 * @return null in case DB is unaware of schema .
	 */
	@Override
	public String getCurrentSchema(Connection aCon) throws SQLException {
		try (Statement stm = aCon.createStatement()) {
			try (ResultSet res = stm.executeQuery("SELECT SYS_CONTEXT('USERENV', 'CURRENT_SCHEMA') FROM DUAL")) {
				if (res.next()) {
					return res.getString(1);
				}
			}
			}
		return null;
	}

	/**
	 * assume table-names starting with BIN$ are system-tables.
	 */
	@Override
	public boolean isSystemTable(String aTableName) {
		return aTableName.startsWith("BIN$"); // BIN$ is actually Recyle-Bin :-)
	}

	/**
	 * Map some oracle specific types back to the ones we like.
	 */
	@Override
	public String getColumnTypeName(ResultSetMetaData meta, int aColumn)
			throws SQLException {
		int type = meta.getColumnType(aColumn);
		switch (type) {
			case 93:
				return "java.sql.Timestamp"; // TIMESTAMP(4)
			case 100:
				return "java.lang.Double"; // BINARY_FLOAT
			case 101:
				return "java.lang.Double"; // BINARY_DOUBLE
			case 2004:
				return "java.sql.Blob"; // BLOB
			case 2005:
				return "java.sql.Clob"; // CLOB
		}
		String result = meta.getColumnClassName(aColumn);
		if (result == null || !result.startsWith("java.")) {
			/* if ("oracle.sql.CLOB".equals(result)) { // matched by 2005 above return
			 * "java.sql.Clob"; } if ("oracle.sql.BLOB".equals(result)) { // matched by 2004 above
			 * return "java.sql.Blob"; } else if ("oracle.sql.TIMESTAMP".equals(result)) { return
			 * "java.sql.Timestamp"; // matched by 93 above } */
			Logger.warn("Unsupported non-Java Type '" + result + "'", this);
		}

		return result;
	}

	/**
	 * With Oracle 10 this will do, well
	 */
	@Override
	public boolean supportNullInSetObject() {
		return true;
	}

	/**
	 * Map an Oracle specific type to a Java Type.
	 * 
	 * @param res
	 *        The result set to extract the value from.
	 * @param col
	 *        The column to extract it from
	 * @param dbtype
	 *        the DBType for the derires JAVA-type.
	 * 
	 * @return An Object apropriate for the given DBType.
	 */
	@Override
	public Object mapToJava(ResultSet res, int col, DBType dbtype) throws SQLException {
		Object result;
		switch (dbtype) {
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
			case BOOLEAN: // Oracle always uses NUMBER / BigDecimal, :-/
				boolean theBool = res.getBoolean(col);
				if (res.wasNull())
					return null;
				result = Boolean.valueOf(theBool);
				break;
			case LONG:
				long theLong = res.getLong(col);
				if (res.wasNull())
					return null;
				return Long.valueOf(theLong);
			case ID:
				result = IdentifierUtil.getId(res, col);
				return result;
			case INT:
				int theInt = res.getInt(col);
				if (res.wasNull())
					return null;
				return Integer.valueOf(theInt);
			case SHORT:
				short theShort = res.getShort(col);
				if (res.wasNull())
					return null;
				return Short.valueOf(theShort);
			case BYTE:
				byte theByte = res.getByte(col);
				if (res.wasNull())
					return null;
				return Byte.valueOf(theByte);
			case DECIMAL:
			case DOUBLE:
				double theDouble = res.getDouble(col);
				if (res.wasNull())
					return null;
				return Double.valueOf(theDouble);
			case FLOAT:
				float theFloat = res.getFloat(col);
				if (res.wasNull())
					return null;
				return Float.valueOf(theFloat);
			case CHAR:
				return mapToCharacter(res, col);
			case CLOB:
				return res.getString(col);
			case BLOB:
				return getBlobValue(res, col);
			case STRING:
				result = res.getString(col);
				return result;
			default:
				result = res.getObject(col);
		}
		if (res.wasNull())
			result = null;

		return result;
	}

	@Override
	protected void internalSetNull(PreparedStatement pstm, int col, DBType dbtype) throws SQLException {
		// Oracle complains, if it sees an unsupported column type, even for setNull().
		pstm.setNull(col, dbtype == DBType.BOOLEAN ? Types.INTEGER : dbtype.sqlType);
	}

	/**
	 * Create a valid SQL statement to drop an index.
	 * 
	 * Oracle does not need (or use) the table name
	 * 
	 * @param idxName
	 *        Name of the index to DROP
	 * @param tableName
	 *        Name of the table for the index to DROP.
	 */
	@Override
	public void appendDropIndex(Appendable sql, String idxName, String tableName) throws IOException {
		sql.append("DROP INDEX ");
		sql.append(columnRef(idxName));
	}

	/**
	 * Need to add a COMMIT to the super implementation.
	 * 
	 * @param out
	 *        Output will be written here.
	 */
	@Override
	public int dumpAsInsert(PrintWriter out, String table, ResultSet res) throws SQLException {
		int count = super.dumpAsInsert(out, table, res);
		if (0 != count) {
			out.println("COMMIT;");
			out.println();
		}
		return count;
	}

	@Override
	public void dumpAsInsert(Appendable out, DatabaseContent content) throws IOException {
		super.dumpAsInsert(out, content);
		if (0 != content.getRows().size()) {
			out.append("COMMIT;");
			out.append('\n');
		}
	}

	/**
	 * Return true when there is a chance that a retry of statemets will succeed.
	 * 
	 * @param sqlX
	 *        the Exception for an SQLStatemrn we may retry.
	 */
	@Override
	public boolean canRetry(SQLException sqlX) {

		int errorCode = sqlX.getErrorCode();
		if (errorCode == 17002 // Software caused connection abort: recv failed
			|| errorCode == 17410 // No more data to read from socket
			|| errorCode == 17009 // Closed Statement / (Geschlossene Anweisung ;-)
			|| errorCode == 17008) // Getrennte Verbindung (Found with ORA 9)
			return true;

		// This is from some MySQL docs (Example 5.1 Example of transaction with retry logic).
		// They should not harm with other DBs as well.
		String sqlState = sqlX.getSQLState();
		if (sqlState != null
			&& ("08S01".equals(sqlState) // communications error
				|| "08003".equals(sqlState) // Connection.close() has already been called....
				|| "41000".equals(sqlState) // deadlock
				|| "S1009".equals(sqlState))) // PreparedStatement has been closed / testing
			return true;

		return super.canRetry(sqlX);
	}

	/**
	 * Return maximum Size a static Set can have.
	 * 
	 * For statements like SELECT MAX(..) WHRE ... IN (&lt;ID&gt;,&lt;ID&gt;, .... )
	 */
	@Override
	public int getMaxSetSize() {
		return 999; // Actual size for ORacle 8.1.7 is 1000
	}

	/**
	 * Assumes that there is a Sequence named "SEQ_" + some table name.
	 */
	@Override
	public long prepareSerial(String someTableName, Connection someCon) throws SQLException {
		String sql = "SELECT SEQ_" + someTableName + ".NEXTVAL FROM DUAL";
		try (Statement stm = someCon.createStatement()) {
			try (ResultSet resultSet = stm.executeQuery(sql)) {
				if (resultSet.next()) {
					return resultSet.getLong(1);
				}
				throw new SQLException("Failed to " + sql);
			}
		}
	}

	/**
	 * True when the id returned by prepare serial must be inserted.
	 */
	@Override
	public boolean isSerialNeeded()
			throws SQLException {
		return true; // only Oracle needs this (using Sequences)
	}

	/**
	 * Not supported by Oracle since we use sequences.
	 * 
	 * @see #prepareSerial(String, Connection)
	 */
	@Override
	public long postcareSerial(long id, Statement stm)
			throws SQLException {
		return id; // Id is same as extracted by prepareSerial
	}

	@Override
	public void analyzeTable(Statement aStmt, String aTableName) throws SQLException {
		aStmt.execute("ANALYZE TABLE " + aTableName + " ESTIMATE STATISTICS");
	}

	@Override
	public String tablePattern(String tableName) {
		return tableName.toUpperCase();
	}

	@Override
	protected Format getDateTimeFormat() {
		return dateFormat("'TO_TIMESTAMP('''yyyyMMdd HHmmssSSS'000'',''YYYYMMDD HH24MISSFF'')'");
	}

	@Override
	protected void setClobFromJava(PreparedStatement pstm, Object val, int col) throws SQLException {
		if (val instanceof String) {
			String string = (String) val;
			int size = string.length();
			if (size == 0) {
				/* Ticket #21726: When setting length 0, an error occurs: I/O-Fehler: 0 char of CLOB
				 * data cannot be read. */
				pstm.setClob(col, new StringReader(string));
			} else {
				pstm.setClob(col, new StringReader(string), size);
			}
		} else {
			super.setClobFromJava(pstm, val, col);
		}
	}

	@Override
	protected void setBlob(PreparedStatement pstm, int col, InputStream stream, long size) throws SQLException {
		if (size == 0) {
			/* Ticket #21726: When setting length 0, an error occurs: I/O-Fehler: 0 char of BLOB
			 * data cannot be read. */
			setBlob(pstm, col, stream);
		} else {
			super.setBlob(pstm, col, stream, size);
		}
	}

	@Override
	public void appendChangeColumnName(Appendable result, String tableName, DBType sqlType, String columnName, String newName,
			long size, int precision, boolean mandatory, boolean binary, Object defaultValue) throws IOException {
		result.append(alterTable(tableName));
		result.append("RENAME COLUMN ");
		result.append(columnRef(columnName));
		result.append(" TO ");
		result.append(columnRef(newName));
	}

	/**
	 * @implNote Changing column type must <b>not</b> append mandatory. This leads to errors if the
	 *           mandatory state is not changed.
	 * 
	 * @see com.top_logic.basic.sql.DBHelper#appendChangeColumnType(java.lang.Appendable,
	 *      String, com.top_logic.basic.sql.DBType, java.lang.String, String, long, int,
	 *      boolean, boolean, java.lang.Object)
	 */
	@Override
	public void appendChangeColumnType(Appendable result, String tableName, DBType sqlType, String columnName, String newName,
			long size, int precision, boolean mandatory, boolean binary, Object defaultValue) throws IOException {
		result.append(alterTable(tableName));
		result.append("MODIFY ");
		result.append(columnRef(columnName));
		result.append(" ");
		internalAppendDBType(result, sqlType, size, precision, mandatory, binary, binary, newName);
		appendDefaultValue(result, sqlType, defaultValue);
	}

	@Override
	public void appendChangeMandatory(Appendable result, String tableName, DBType sqlType, String columnName, String newName,
			long size, int precision, boolean mandatory, boolean binary, Object defaultValue) throws IOException {
		result.append(alterTable(tableName));
		result.append("MODIFY ");
		result.append(columnRef(columnName));
		result.append(" ");
		appendMandatory(result, mandatory);
	}
}
