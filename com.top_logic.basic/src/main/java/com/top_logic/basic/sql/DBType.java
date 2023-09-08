/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.sql;

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.sql.Types;
import java.text.ParseException;
import java.util.Base64;
import java.util.Date;

import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.TLID;
import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.basic.config.XmlDateTimeFormat;
import com.top_logic.basic.config.annotation.Format;

/**
 * Wrapper enumeration for a selection of type codes from {@link Types} used in
 * the <i>TopLogic</i> relational data binding.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Format(DBTypeFormat.class)
public enum DBType implements ExternallyNamed {

	/**
	 * Database type able to store a <code>boolean</code> value.
	 */
	BOOLEAN("boolean", Types.BOOLEAN, false, false, false),
	
	/**
	 * Database type able to store a <code>char</code> value.
	 */
	CHAR("char", Types.CHAR, false, false, true),
	
	/**
	 * Database type able to store a <code>byte</code> value.
	 */
	BYTE("byte", Types.TINYINT, false, false, false),
	
	/**
	 * Database type able to store a <code>short</code> value.
	 */
	SHORT("short", Types.SMALLINT, false, false, false),
	
	/**
	 * Database type able to store an <code>int</code> value.
	 */
	INT("int", Types.INTEGER, false, false, false),
	
	/**
	 * Database type able to store a <code>long</code> value.
	 */
	LONG("long", Types.BIGINT, false, false, false),
	
	/**
	 * Database type able to store the day, month, year part of a {@link Date} value.
	 */
	DATE("date", Types.DATE, false, false, false),
	
	/**
	 * Database type able to store the hour, minute, second part of a {@link Date} value.
	 */
	TIME("time", Types.TIME, false, false, false),
	
	/**
	 * Database type able to store a {@link Date} value.
	 */
	DATETIME("datetime", Types.TIMESTAMP, false, false, false),
	
	/**
	 * Database type able to store a {@link String} value.
	 */
	STRING("string", Types.VARCHAR, true, false, true),
	
	/**
	 * Database type able to store a long {@link String} or {@link Reader} value.
	 */
	CLOB("clob", Types.CLOB, true, false, true),
	
	/**
	 * Database type able to store a <code>byte[]</code> or {@link InputStream} value.
	 */
	BLOB("blob", Types.BLOB, true, false, false),
	
	/**
	 * Database type able to store a <code>float</code> value.
	 */
	FLOAT("float", Types.FLOAT, false, false, false),
	
	/**
	 * Database type able to store a <code>double</code> value.
	 */
	DOUBLE("double", Types.DOUBLE, false, false, false),

	/**
	 * Database type able to store a {@link BigDecimal} value.
	 */
	DECIMAL("decimal", Types.DECIMAL, true, true, false),

	/**
	 * Database type able to store a {@link com.top_logic.basic.TLID} value.
	 */
	ID("id",
			IdentifierUtil.SHORT_IDS ? LONG.sqlType : STRING.sqlType,
			IdentifierUtil.SHORT_IDS ? LONG.sizeParam : STRING.sizeParam,
			IdentifierUtil.SHORT_IDS ? LONG.precisionParam : STRING.precisionParam,
			IdentifierUtil.SHORT_IDS ? LONG.binaryParam : STRING.binaryParam
	),

	;
	
	private final String _externalName;

	/**
	 * The corresponding type code from JDBC {@link Types}.
	 */
	public final int sqlType;
	
	/**
	 * Whether this type requires a size annotation.
	 */
	public final boolean sizeParam;
	
	/**
	 * Whether this type requires a precision annotation.
	 */
	public final boolean precisionParam;

	/**
	 * Whether this type supports a binary annotation.
	 */
	public final boolean binaryParam;
	
	
	private DBType(String name, int sqlType, boolean sizeParam, boolean precisionParam, boolean binaryParam) {
		_externalName = name;
		this.sqlType = sqlType;
		this.sizeParam = sizeParam;
		this.precisionParam = precisionParam;
		this.binaryParam = binaryParam;
	}
	
	@Override
	public String getExternalName() {
		return _externalName;
	}

	/**
	 * Computes the {@link DBType} for a literal based on the class.
	 * 
	 * @param value
	 *        The object to get {@link DBType} for. Must not be <code>null</code>.
	 * 
	 * @return The {@link DBType} which can be used as database column to store the given value.
	 * 
	 * @throws UnsupportedOperationException
	 *         when no {@link DBType} for the given value can be determined.
	 */
	public static DBType fromLiteralValue(Object value) {
		if (value instanceof Boolean) {
			return BOOLEAN;
		} else if (value instanceof Byte) {
			return BYTE;
		} else if (value instanceof Character) {
			return CHAR;
		} else if (value instanceof java.sql.Date) {
			return DATE;
		} else if (value instanceof Date) {
			return DATETIME;
		} else if (value instanceof BigDecimal) {
			return DECIMAL;
		} else if (value instanceof Float) {
			return DOUBLE;
		} else if (value instanceof Double) {
			return DOUBLE;
		} else if (value instanceof TLID) {
			return ID;
		} else if (value instanceof Integer) {
			return LONG;
		} else if (value instanceof Long) {
			return LONG;
		} else if (value instanceof Short) {
			return SHORT;
		} else if (value instanceof java.sql.Time) {
			return TIME;
		} else if (value instanceof String) {
			return STRING;
		} else if (value == null) {
			throw new IllegalArgumentException("Literal value cannot be null.");
		}
	
		throw new UnsupportedOperationException("Unsupported type " + value.getClass().getName() + "; value: " + value);
	}

	/**
	 * Lookup the corresponding {@link DBType} for a given JDBC type.
	 * 
	 * @param sqlDialect
	 *        The SQL dialect that may provide special subtypes.
	 * @param sqlType
	 *        The JDBC type, see {@link Types}.
	 * @param scale
	 *        The number of digits right to the decimal point for a numeric type.
	 * @return The corresponding {@link DBType}.
	 * 
	 * @throws IllegalArgumentException
	 *         If the given JDBC type does not exist, or is not supported.
	 */
	public static DBType fromSqlType(DBHelper sqlDialect, int sqlType, int scale) throws IllegalArgumentException {
		switch (sqlType) {
			case Types.BOOLEAN:
				return BOOLEAN;
			case Types.BIT:
				return BOOLEAN;

			case Types.TINYINT:
				return BYTE;
			case Types.SMALLINT:
				return SHORT;
			case Types.INTEGER:
				return INT;
			case Types.BIGINT:
				return LONG;

			case Types.FLOAT:
				return FLOAT;

			case Types.DOUBLE:
				return DOUBLE;
			case Types.REAL:
				return DOUBLE;
			case Types.NUMERIC:
				return scale == 0 ? LONG : DOUBLE;

			case Types.DECIMAL:
				return DECIMAL;

			case Types.TIME:
				return TIME;
			case Types.DATE:
				return DATE;
			case Types.TIMESTAMP:
				return DATETIME;

			case Types.CHAR:
				return CHAR;
			case Types.NCHAR:
				return CHAR;

			case Types.VARCHAR:
				return STRING;
			case Types.NVARCHAR:
				return STRING;

			case Types.CLOB:
				return CLOB;
			case Types.NCLOB:
				return CLOB;
			case Types.LONGVARCHAR:
				return CLOB;
			case Types.LONGNVARCHAR:
				return CLOB;

			case Types.BLOB:
				return BLOB;
			case Types.VARBINARY:
				return BLOB;
			case Types.LONGVARBINARY:
				return BLOB;
			case Types.BINARY:
				return BLOB;
		}

		return sqlDialect.fromProprietarySqlType(sqlType);
	}
	
	/**
	 * Parses the given formatted value for the given {@link DBType}, such that the result can be
	 * used to store as value in a column of the given type.
	 * 
	 * @param type
	 *        Type of the column to create value for.
	 * @param value
	 *        Value to parse. May be <code>null</code>.
	 * @throws ParseException
	 *         When parsing fails.
	 */
	public static Object parseSQLValue(DBType type, String value) throws ParseException {
		if (StringServices.isEmpty(value) ) {
			return null;
		}
		switch (type) {
			case BLOB:
				return Base64.getDecoder().decode(value);
			case BOOLEAN:
				return Boolean.parseBoolean(value);
			case BYTE:
				return Byte.parseByte(value);
			case CHAR:
				if (value.length()>1) {
					throw new ParseException("More than one character in " + value, 1);
				}
				return value.charAt(0);
			case CLOB:
				return new StringReader(value);
			case DATE:
			case DATETIME:
			case TIME:
				return XmlDateTimeFormat.INSTANCE.parseObject(value);
			case DECIMAL:
				return BigDecimal.valueOf(Long.parseLong(value));
			case DOUBLE:
				return Double.parseDouble(value);
			case FLOAT:
				return Float.parseFloat(value);
			case ID:
				return IdentifierUtil.fromExternalForm(value);
			case INT:
				return Integer.parseInt(value);
			case LONG:
				return Long.parseLong(value);
			case SHORT:
				return Short.parseShort(value);
			case STRING:
				return value;
			default:
				throw new IllegalArgumentException("Unknown DBType " + type);

		}

	}

}
