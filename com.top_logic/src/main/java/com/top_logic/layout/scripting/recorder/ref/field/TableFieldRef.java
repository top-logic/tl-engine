/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.field;

import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.scripting.recorder.ref.ModelName;

/**
 * Reference to a {@link FormMember} in a table cell.
 * 
 * <p>
 * For identification, the row object and the column name of the table cell is used.
 * </p>
 * 
 * @author <a href=mailto:Jan.Stolzenburg@top-logic.com>Jan Stolzenburg</a>
 */
public interface TableFieldRef extends FieldRef {

	/**
	 * The name of the column to access.
	 */
	String getColumnName();

	/** @see #getColumnName() */
	void setColumnName(String columnName);

	/**
	 * The row object of the row to access.
	 */
	ModelName getRowObject();

	/** @see #getRowObject() */
	void setRowObject(ModelName rowObject);

}
