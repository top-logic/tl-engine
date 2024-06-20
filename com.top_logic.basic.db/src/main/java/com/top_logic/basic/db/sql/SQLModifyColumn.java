/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.sql;

import com.top_logic.basic.sql.DBType;

/**
 * {@link SQLAlterTable} that modifies an aspect of a database column.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SQLModifyColumn extends SQLAddColumn {

	/**
	 * The aspect of the column to modify.
	 */
	public enum ModificationAspect {
		/**
		 * The column name is modified.
		 */
		NAME,

		/**
		 * The column type is modified.
		 */
		TYPE,

		/**
		 * The nullable aspect is modified.
		 */
		MANDATORY;
	}

	private ModificationAspect _modificationAspect;

	private String _newName;

	SQLModifyColumn(SQLTable table, String columnName, ModificationAspect aspect, DBType type) {
		super(table, columnName, type);
		_modificationAspect = aspect;
		_newName = columnName;
	}

	@Override
	public <R, A> R visit(SQLVisitor<R, A> v, A arg) {
		return v.visitSQLModifyColumn(this, arg);
	}

	@Override
	public SQLModifyColumn setBinary(boolean binary) {
		return (SQLModifyColumn) super.setBinary(binary);
	}

	@Override
	public SQLModifyColumn setMandatory(boolean mandatory) {
		return (SQLModifyColumn) super.setMandatory(mandatory);
	}

	/**
	 * The new name for a rename.
	 */
	public String getNewName() {
		return _newName;
	}

	/**
	 * Sets the new name for a rename.
	 */
	public SQLModifyColumn setNewName(String newName) {
		_newName = newName;
		return this;
	}

	@Override
	public SQLModifyColumn setDefaultValue(Object defaultValue) {
		return (SQLModifyColumn) super.setDefaultValue(defaultValue);
	}

	@Override
	public SQLModifyColumn setPrecision(int precision) {
		return (SQLModifyColumn) super.setPrecision(precision);
	}

	@Override
	public SQLModifyColumn setSize(long size) {
		return (SQLModifyColumn) super.setSize(size);
	}

	/**
	 * The aspect of the column that has to be modified.
	 */
	public ModificationAspect getModificationAspect() {
		return _modificationAspect;
	}

	/**
	 * Setter for {@link #getModificationAspect()}.
	 */
	public SQLModifyColumn setModificationAspect(ModificationAspect modificationAspect) {
		_modificationAspect = modificationAspect;
		return this;
	}
}

