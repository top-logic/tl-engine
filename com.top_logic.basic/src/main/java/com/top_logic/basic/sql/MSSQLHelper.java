/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.sql;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.text.Format;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.config.InstantiationContext;


/**
 * Database Helper for Microsoft SQL Server.
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class MSSQLHelper extends DBHelper {

	private static final int DATETIME = -151;

	/**
	 * Configuration options for {@link MSSQLHelper}.
	 */
	public interface Config extends DBHelper.Config {
		// No additional properties, just to be able to configure different application-wide
		// defaults.
	}

	/**
	 * Creates a {@link MSSQLHelper} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public MSSQLHelper(InstantiationContext context, Config config) {
		super(context, config);
	}
	
    @Override
    protected void appendBooleanType(Appendable result, boolean mandatory) throws IOException {
    	if (mandatory) {
    		result.append("BIT");
    	} else {
    		result.append("TINYINT");
    	}
    }
    
    @Override
    protected void appendFloatType(Appendable result, boolean mandatory) throws IOException {
    	result.append("FLOAT(24)");
    }
    
    @Override
    protected void appendDoubleType(Appendable result, boolean mandatory) throws IOException {
    	result.append("FLOAT(53)");  
    }
    
	@Override
	public boolean supportsUnicodeSupplementaryCharacters() {
		return false;
	}

    @Override
    protected void appendCharType(Appendable result, boolean mandatory, boolean binary, boolean castContext) throws IOException {
    	result.append(binary ? "CHAR" : "NCHAR");
    	size(result, 1);
    	
		if (!castContext) {
			appendCollationDefinition(result, binary);
		}
    }
    
    @Override
    protected void appendStringType(Appendable result, String columnName, long size, boolean mandatory, boolean binary, boolean castContext) throws IOException {
    	if (binary) {
    		result.append("VARCHAR");
    		if (size > 0 && size <= 8000) {
    			size(result, size);
    		} else {
    			result.append("(max)");
    		}
    	} else {
    		result.append("NVARCHAR");
    		if (size > 0 && size <= 4000) {
    			size(result, size);
    		} else {
    			result.append("(max)");
    		}
    	}
		if (!castContext) {
			appendCollationDefinition(result, binary);
		}
    }
    
	private void appendCollationDefinition(Appendable result, boolean binary) throws IOException {
		if (binary) {
    		result.append(" COLLATE Latin1_General_BIN2");
    	} else {
    		result.append(" COLLATE Latin1_General_CI_AS");
    	}
    }
    
    @Override
    protected void appendClobType(Appendable result, String columnName, long size, boolean mandatory, boolean binary, boolean castContext) throws IOException {
    	appendStringType(result, columnName, size, mandatory, binary, castContext);
    }
    
    @Override
    protected void appendDateType(Appendable result, boolean mandatory) throws IOException {
    	// Since MSSQL 2008:
    	//
    	// result.append("DATE");
    	appendDateTimeType(result, mandatory);
    }
    
    @Override
    public DBType analyzeSqlType(int sqlType, String sqlTypeName, int size, int scale) {
    	switch (sqlType) {
    	case Types.REAL: 
    	case Types.DOUBLE: 
    	case Types.FLOAT: 
			if (size <= 24) {
				return DBType.FLOAT;
			} else {
				return DBType.DOUBLE;
			}
		
		default:
			return super.analyzeSqlType(sqlType, sqlTypeName, size, scale);
    	}
    }
    
    @Override
    public boolean analyzeSqlTypeBinary(int sqlType, String sqlTypeName, int size, int octetSize) {
    	switch (sqlType) {
    	case Types.NCHAR:
    	case Types.NVARCHAR:
    		return false;
    	case Types.CHAR: 
    		return sqlTypeName.toUpperCase().equals("CHAR");
    	case Types.VARCHAR: 
    		return sqlTypeName.toUpperCase().startsWith("VARCHAR");
    	}
    	return super.analyzeSqlTypeBinary(sqlType, sqlTypeName, size, octetSize);
    }
    
    @Override
    protected void internalAppendCollatedExpression(Appendable buffer, String sqlExpression, CollationHint collationHint) throws IOException {
    	super.internalAppendCollatedExpression(buffer, sqlExpression, collationHint);
    	switch (collationHint) {
    	case NONE: 
    		break;
    	case BINARY:
    		appendCollationDefinition(buffer, true);
    		break;
    	case NATURAL:
    		appendCollationDefinition(buffer, false);
    		break;
    	}
    }
    
	@Override
	protected void internalAppendCastExpression(Appendable buffer, String sqlExpression, DBType dbType, long size,
			int precision, boolean binary) throws IOException {
		super.internalAppendCastExpression(buffer, sqlExpression, dbType, size, precision, binary);
		appendCollationDefinition(buffer, binary);
	}

    /**
     * Care about some types special for MS-SQLServer.
     */
    @Override
	protected String internalGetDBType(int sqlType, boolean binary) {
        switch (sqlType) {
            case Types.BIT:             return "BIT";
			case Types.BOOLEAN:
				throw new UnreachableAssertion(
					"Type BOOLEAN depends on 'mandatory'. It is processed in appendBooleanType");
            case Types.TINYINT:         return "TINYINT";
            case Types.SMALLINT:        return "SMALLINT";
            case Types.INTEGER:         return "INTEGER";
            case Types.BIGINT:          return "BIGINT";
            case Types.FLOAT:           return "FLOAT(24)";
            case Types.REAL:            return "REAL";   
            case Types.DOUBLE:          return "FLOAT(53)";  
            case Types.NUMERIC:         return "DECIMAL";
            case Types.DECIMAL:         return "DECIMAL";
            case Types.NCHAR:           /* Intentional fall-through */
            case Types.CHAR:
            case Types.NVARCHAR:        /* Intentional fall-through */
            case Types.VARCHAR:
            case Types.LONGNVARCHAR:    /* Intentional fall-through */
            case Types.LONGVARCHAR:     return binary ? "VARCHAR" : "NVARCHAR";
            case Types.NCLOB:           /* Intentional fall-through */
            case Types.CLOB:            return binary ? "TEXT" : "NTEXT";
            case Types.DATE:            return "DATETIME";
            case Types.TIME:            return "DATETIME";
            case Types.TIMESTAMP:       return "DATETIME";
            case Types.BINARY:          return "BINARY";
            case Types.VARBINARY:       return "VARBINARY(MAX)";
            case Types.LONGVARBINARY:   return "VARBINARY(MAX)";
            case Types.BLOB:            return "VARBINARY(MAX)";
            default: 
            	throw new IllegalArgumentException("Undefined SQL type '" + sqlType + "'.");
        }
    }

    /**
     * Return if some type needs a size.
     * 
     * This implemtantion currently was tested with MySQL and MSSQL.
     * 
     * @param   sqlType a type as defined in java.sql.Types
     * @return  true when size is needed.
     */
    @Override
	public boolean noSize(DBType sqlType) {

        switch (sqlType) {
			case BOOLEAN:
			case BYTE:
			case SHORT:
			case INT:
			case LONG:
			case ID:
			case CLOB:
			case DATE:
			case TIME:
			case FLOAT:
			case DOUBLE:
			case DATETIME:
				return true;
			case BLOB:
			case CHAR:
			case DECIMAL:
			case STRING:
				return false;
        }
		return super.noSize(sqlType);
    }
    
	@Override
	public void onUpdate(Appendable buffer, DBConstraintType onDelete) throws IOException {
		if (onDelete == DBConstraintType.RESTRICT) {
			// This is the default and cannot explicitly be given as option.
			return;
		}

		super.onUpdate(buffer, onDelete);
	}

	@Override
	protected String toSql(DBConstraintType constraintType) {
		switch (constraintType) {
			case RESTRICT:
				return "NO ACTION";
			default:
				return super.toSql(constraintType);
		}
	}

    /** 
     * Indicates whether <code>setObject()</code> will allow null-values.
     * 
     * The driver 
     */
    @Override
	public boolean supportNullInSetObject() {
        return false;    // let's hope for the best
    }
    /**
     * Ignore the given table name as reseververd for System usage.
     * 
     * This is a woraround for drivers not implementing {@link DatabaseMetaData#getTables(java.lang.String, java.lang.String, java.lang.String, java.lang.String[])} 
     * correcty. (as of now MSSQL)
     */
	@Override
	public boolean isSystemTable(String aTableName) {
        if ("dtproperties".equals(aTableName)) {
            return true;
        }
        return super.isSystemTable(aTableName);
	}
    
    /** 
     * Return true when there is a chance that a retry of statemets will succeed.
     * 
     * @param sqlX the Exception for an SQLStatement we may retry.
     */
	@Override
	public boolean canRetry(SQLException sqlX) {
        String theState = sqlX.getSQLState();
        int errorCode = sqlX.getErrorCode();
		if ("HY010".equals(theState) // Invalid state, the Connection object is closed.
         || "08S01".equals(theState) // I/O Error: Stream 1 attempting to read when no request has been sent
         || (errorCode == 1205) // Deadlock
         || (theState == null) //MSSQL driver does not use state or error code in case of closed connection 
        ) {
            return true;
        }
		Logger.info("Can not retry code:" + errorCode 
                              + " state:" + theState, sqlX, this);
		return false;
	}
    
    /**
     * OverWritten to get the correct MSSQL drop index statement
	 */
	@Override
	public void appendDropIndex(Appendable sql, String idxName, String tableName) throws IOException {
		sql.append("DROP INDEX ");
		sql.append(tableRef(tableName));
		sql.append('.');
		sql.append(columnRef(idxName));
	}
    
	@Override
	public String forUpdate1() {
		return " WITH (UPDLOCK)";
	}
	
	@Override
	protected boolean internalPing(Connection connection) throws SQLException {
		return internalPing(connection, "SELECT " + fnNow());
	}

	@Override
	public String selectNoBlockHint() {
	    return " WITH(READPAST)";
	}
	
	@Override
	public int getMaxNameLength() {
		return 128;
	}

	@Override
	public String columnRef(String columnName) {
		StringBuilder b = new StringBuilder(columnName.length() + 2);
		b.append('"');
		b.append(columnName);
		b.append('"');
		return b.toString();
	}

	@Override
	protected Format getBlobFormat() {
		return MSSQLBlobFormat.INSTANCE;
	}

	@Override
	protected Format getBooleanFormat() {
		return BooleanAsBitFormat.INSTANCE;
	}

	@Override
	protected DBType fromProprietarySqlType(int sqlType) {
		switch (sqlType) {
			case DATETIME:
				return DBType.DATETIME;
		}
		return super.fromProprietarySqlType(sqlType);
	}

	@Override
	protected Format getLiteralFormat(DBType dbType) {
		switch (dbType) {
			case BYTE:
				return MSSQLByteFormat.INSTANCE;
			default:
				return super.getLiteralFormat(dbType);
		}
	}

	@Override
	protected Format getDateTimeFormat() {
		return dateFormat("''yyyy-MM-dd'T'HH:mm:ss.SSS''");
	}

	@Override
	protected void internalEscape(Appendable out, String str) throws IOException {
		// Need transformation to convert char to nvarchar
		// https://docs.microsoft.com/en-us/sql/t-sql/data-types/nchar-and-nvarchar-transact-sql
		out.append('N');
		super.internalEscape(out, str);
	}

	@Override
	public String fnCurrDate() {
		// Using {fn CURRDATE()} leads to:
		/* Bei der Konvertierung eines char-Datentyps in einen datetime-Datentyp liegt der
		 * datetime-Wert außerhalb des gültigen Bereichs. */
		return "CAST(GETDATE() AS DATETIME)";
	}

	@Override
	public void appendChangeColumnName(Appendable result, String tableName, DBType sqlType, String columnName, String newName,
			long size, int precision, boolean mandatory, boolean binary, Object defaultValue) throws IOException {
		result.append("EXEC sp_rename ");
		result.append(columnRef(tableName + "." + columnName));
		result.append(", ");
		result.append(columnRef(newName));
		result.append(", 'COLUMN'");
	}

	@Override
	public void appendChangeColumnType(Appendable result, String tableName, DBType sqlType, String columnName, String newName,
			long size, int precision, boolean mandatory, boolean binary, Object defaultValue) throws IOException {
		result.append(alterTable(tableName));
		result.append("ALTER COLUMN ");
		result.append(columnRef(columnName));
		result.append(" ");
		appendDBType(result, sqlType, columnName, size, precision, mandatory, binary, defaultValue);
	}

	@Override
	public void appendChangeMandatory(Appendable result, String tableName, DBType sqlType, String columnName, String newName,
			long size, int precision, boolean mandatory, boolean binary, Object defaultValue) throws IOException {
		result.append(alterTable(tableName));
		result.append("ALTER COLUMN ");
		result.append(columnRef(columnName));
		result.append(" ");
		appendDBType(result, sqlType, columnName, size, precision, mandatory, binary, defaultValue);
	}
}
