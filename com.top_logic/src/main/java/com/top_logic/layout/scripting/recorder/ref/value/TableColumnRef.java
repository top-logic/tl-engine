/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.value;

import com.top_logic.basic.config.ConfigurationItem;

/**
 * The information to identify a column in a given table.
 * <p>
 * Either the {@link #getColumnName()} or the {@link #getColumnLabel()} has to to be set. Setting
 * both is not allowed.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public interface TableColumnRef extends ConfigurationItem {

	/**
	 * Column of the cell that this assertion is about.
	 */
	String getColumnName();

	/**
	 * @see #getColumnName()
	 */
	void setColumnName(String columnName);

	/**
	 * Column label of the cell that this assertion is about.
	 */
	String getColumnLabel();

	/**
	 * @see #getColumnLabel()
	 */
	void setColumnLabel(String columnlabel);

}
