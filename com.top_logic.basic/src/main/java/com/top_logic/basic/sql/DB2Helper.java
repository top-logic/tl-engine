/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.sql;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.Format;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;

/** This class helps using DB2.
 * <p>
 *  It helps working around this _small_ differences in these drivers.
 *  
 * </p>
 * @author  Klaus Halfmann
 */
public class DB2Helper extends DBHelper  
{

	/**
	 * Configuration options for {@link DB2Helper}.
	 */
	public interface Config extends DBHelper.Config {
		// No additional properties, just to be able to configure different application-wide
		// defaults.
	}

	/**
	 * Creates a {@link DB2Helper} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public DB2Helper(InstantiationContext context, Config config) {
		super(context, config);
	}

    /** We use a Sequence based on the table name.
     */
    @Override
	public long prepareSerial(String tableName,
                              Connection con) throws SQLException {
        long        result  = 0;
        Statement   stm     = null;
        ResultSet   res     = null;
        try {
            stm = con.createStatement();
            res = stm.executeQuery("VALUES NEXTVAL FOR SEQ_" + tableName);
            if (res.next()) {
                result = res.getLong(1);
            }
        }
        finally {
            if (res != null) {
                res.close();
            }
            if (stm != null) {
                stm.close();
            }
        }
        return result;
    }

    /** Call this Function to get the serial number after doing the
     *  actual insert.
     */
    @Override
	public long postcareSerial(long id, Statement stm)
        throws SQLException
    {
        return id;  
    }

	@Override
	protected void appendBooleanType(Appendable result, boolean mandatory) throws IOException {
		result.append(internalGetDBType(Types.BOOLEAN, false)).append("(1)");
	}

	@Override
	public void internalSetFromJava(PreparedStatement pstm, Object val, int col, DBType dbtype) throws SQLException {
		switch (dbtype) {
			case LONG:
				setLongFromJava(pstm, val, col);
				break;
			default:
				super.internalSetFromJava(pstm, val, col, dbtype);
		}
	}

	@Override
	protected void appendStringType(Appendable result, String columnName, long size, boolean mandatory, boolean binary, boolean castContext)
			throws IOException {
		if (!binary) {
			long actualSize = varcharColumnSize(size);
			super.appendStringType(result, columnName, actualSize, mandatory, binary, castContext);
			result.append(" CHECK (CHAR_LENGTH(");
			result.append(columnRef(columnName));
			result.append(", CODEUNITS16)<=");
			result.append(Long.toString(size));
			result.append(')');
		} else {
			super.appendStringType(result, columnName, size, mandatory, binary, castContext);
		}
	}

	private long varcharColumnSize(long size) {
		/* Size in database is byte size not character size. If unicode char needs more than one
		 * byte, the size is not enough. */
		long actualSize = 4 * size;
		return actualSize;
	}

	@Override
	protected void appendFloatType(Appendable result, boolean mandatory) throws IOException {
		result.append(internalGetDBType(Types.FLOAT, false));
	}

	@Override
	public String fromNoTable() {
		return " FROM SYSIBM.SYSDUMMY1";
	}

	@Override
	public String fnNow() {
		/* 3 means precision in milliseconds (0: seconds, 1: 1/10 seconds, ...) */
		return "current_timestamp(3)";
	}

	@Override
	public boolean supportsLimitStart() {
		return false;
	}

	@Override
	public boolean supportsLimitStop() {
		return true;
	}

	@Override
	protected void internalAppendCollatedExpression(Appendable buffer, String sqlExpression, CollationHint collationHint)
			throws IOException {
		switch (collationHint) {
			case NATURAL:
				buffer.append("COLLATION_KEY_BIT(").append(sqlExpression).append(",'CLDR181_LDE')");
				break;
			case BINARY:
				// DB2 must be configured to sort binary by default
			case NONE:
			default:
				super.internalAppendCollatedExpression(buffer, sqlExpression, collationHint);
		}
	}

	@Override
	public StringBuilder limitLast(StringBuilder sql, int startRow, int stopRow) {
		if (stopRow != -1) {
			return sql.append(" FETCH FIRST ").append(stopRow).append(" ROWS ONLY");
		} else {
			return sql;
		}
	}

	@Override
	public boolean canRetry(SQLException sqlX) {
		String sqlState = sqlX.getSQLState();
		if (sqlState.startsWith("08")) {
			// some problem with connection
			return true;
		}
		if (sqlState.startsWith("40")) {
			// problem during rollback of transaction
			return true;
		}
		return super.canRetry(sqlX);
	}
	
	@Override
	public boolean allowParameterColumn() {
		return false;
	}

	@Override
	public String getTruncateTableStatement(String tableName) {
		return super.getTruncateTableStatement(tableName) + " IMMEDIATE";
	}

	@Override
	public String forUpdate2() {
		return " FOR UPDATE WITH RR USE AND KEEP EXCLUSIVE LOCKS";
	}

	@Override
	public int getMaxNameLength() {
		return 128;
	}

	@Override
	public String nullSpec() {
		return "DEFAULT NULL";
	}

	@Override
	public String tablePattern(String tableName) {
		return tableName.toUpperCase();
	}

	@Override
	public DBType analyzeSqlType(int sqlType, String sqlTypeName, int size, int scale) {
		switch (sqlType) {
			case Types.REAL:
				return DBType.FLOAT;
			default:
				return super.analyzeSqlType(sqlType, sqlTypeName, size, scale);
		}
	}

	@Override
	protected Format getBlobFormat() {
		return DB2BlobFormat.INSTANCE;
	}

	@Override
	protected Format getDateTimeFormat() {
		return dateFormat("'TIMESTAMP ('''yyyy-MM-dd-HH.mm.ss.SSS000'')");
	}

}
