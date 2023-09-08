/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.sql.dummy;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

/**
 * A {@link PreparedStatement} for tests.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class DummyPreparedStatement extends DummyStatement implements PreparedStatement {

	private final String _sql;

	/** Creates a {@link DummyPreparedStatement} for the given sql. */
	public DummyPreparedStatement(String sql) {
		_sql = sql;
	}

	@Override
	public ResultSet executeQuery() throws SQLException {
		return setResultSet(new DummyResultSet(DummyDataSource.parseAffectedRows(_sql)));
	}

	@Override
	public int executeUpdate() throws SQLException {
		return setUpdateCount(DummyDataSource.parseAffectedRows(_sql));
	}

	@Override
	public void setNull(int parameterIndex, int sqlType) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setBoolean(int parameterIndex, boolean x) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setByte(int parameterIndex, byte x) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setShort(int parameterIndex, short x) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setInt(int parameterIndex, int x) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setLong(int parameterIndex, long x) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setFloat(int parameterIndex, float x) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setDouble(int parameterIndex, double x) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setString(int parameterIndex, String x) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setBytes(int parameterIndex, byte[] x) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setDate(int parameterIndex, Date x) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setTime(int parameterIndex, Time x) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Deprecated
	@Override
	public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clearParameters() throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setObject(int parameterIndex, Object x) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean execute() throws SQLException {
		return execute(_sql);
	}

	@Override
	public void addBatch() throws SQLException {
		addBatch(_sql);
	}

	@Override
	public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setRef(int parameterIndex, Ref x) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setBlob(int parameterIndex, Blob x) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setClob(int parameterIndex, Clob x) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setArray(int parameterIndex, Array x) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public ResultSetMetaData getMetaData() throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setURL(int parameterIndex, URL x) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public ParameterMetaData getParameterMetaData() throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setRowId(int parameterIndex, RowId x) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setNString(int parameterIndex, String value) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setNClob(int parameterIndex, NClob value) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setClob(int parameterIndex, Reader reader) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setNClob(int parameterIndex, Reader reader) throws SQLException {
		throw new UnsupportedOperationException();
	}

}