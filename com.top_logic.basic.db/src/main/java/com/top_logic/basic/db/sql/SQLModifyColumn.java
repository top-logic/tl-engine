/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.sql;

import com.top_logic.basic.sql.DBType;

/**
 * {@link AbstractSQLTableModification} that modifies an aspect of a database column.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SQLModifyColumn extends SQLAddColumn {

	/**
	 * The aspect of the column to modify.
	 */
	public enum ModificationAspect {
		/**
		 * The column type is modified.
		 */
		TYPE,

		/**
		 * The nullable aspect is modified.
		 */
		MANDATORY;
	}

	private ModificationAspect _modificationAspect = ModificationAspect.TYPE;

	SQLModifyColumn(String columnName, ModificationAspect aspect, DBType type) {
		super(columnName, type);
		_modificationAspect = aspect;
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

