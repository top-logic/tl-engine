/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.sql;

import com.top_logic.basic.sql.DBType;

/**
 * {@link SQLTableModification} that adds an additional column to the base table.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SQLAddColumn extends SQLAlterTable {

	private DBType _type;

	private String _columnName;

	private boolean _mandatory;

	private long _size;

	private int _precision;

	private boolean _binary;

	private Object _defaultValue;

	/**
	 * Creates a new {@link SQLAddColumn}.
	 */
	SQLAddColumn(SQLTable table, String columnName, DBType type) {
		super(table);
		setColumnName(columnName);
		setType(type);
	}

	@Override
	public <R, A> R visit(SQLVisitor<R, A> v, A arg) {
		return v.visitSQLAddColumn(this, arg);
	}

	/**
	 * The actual {@link DBType} of the new column.
	 */
	public DBType getType() {
		return _type;
	}

	/**
	 * Setter for {@link #getType()}.
	 */
	public void setType(DBType type) {
		_type = type;
	}

	/**
	 * Name of the column to add.
	 */
	public String getColumnName() {
		return _columnName;
	}

	/**
	 * Setter for {@link #getColumnName()}.
	 */
	public void setColumnName(String columnName) {
		_columnName = columnName;
	}

	/**
	 * Whether the new column must be non <code>null</code>.
	 */
	public boolean isMandatory() {
		return _mandatory;
	}

	/**
	 * Setter for {@link #isMandatory()}.
	 * 
	 * @return A reference to this {@link SQLAddColumn}.
	 */
	public SQLAddColumn setMandatory(boolean mandatory) {
		_mandatory = mandatory;
		return this;
	}

	/**
	 * Size of the new column. Must be set if {@link DBType#sizeParam} is <code>true</code> for
	 * {@link #getType()}.
	 */
	public long getSize() {
		return _size;
	}

	/**
	 * Setter for {@link #getSize()}.
	 * 
	 * @return A reference to this {@link SQLAddColumn}.
	 */
	public SQLAddColumn setSize(long size) {
		_size = size;
		return this;
	}

	/**
	 * Precision of the new column. Must be set if {@link DBType#precisionParam} is
	 * <code>true</code> for {@link #getType()}.
	 */
	public int getPrecision() {
		return _precision;
	}

	/**
	 * Setter for {@link #getPrecision()}.
	 * 
	 * @return A reference to this {@link SQLAddColumn}.
	 */
	public SQLAddColumn setPrecision(int precision) {
		_precision = precision;
		return this;
	}

	/**
	 * Whether the new column must be marked as binary. Can be set if {@link DBType#binaryParam} is
	 * <code>true</code> for {@link #getType()}.
	 */
	public boolean isBinary() {
		return _binary;
	}

	/**
	 * Setter for {@link #isBinary()}.
	 * 
	 * @return A reference to this {@link SQLAddColumn}.
	 */
	public SQLAddColumn setBinary(boolean binary) {
		_binary = binary;
		return this;
	}

	/**
	 * The default value for the column
	 */
	public Object getDefaultValue() {
		return _defaultValue;
	}

	/**
	 * Setter for {@link #getDefaultValue()}.
	 *
	 * @return A reference to this {@link SQLAddColumn}.
	 */
	public SQLAddColumn setDefaultValue(Object defaultValue) {
		_defaultValue = defaultValue;
		return this;
	}

}

