/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.action;

import java.util.List;

import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.layout.scripting.action.ModelAction;

/**
 * Configuration of {@link SetTableColumnsOp}.
 * 
 * @see SetTableColumnsOp
 * 
 * @since 5.7.5
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface SetTableColumns extends ModelAction {

	/**
	 * The columns to display.
	 */
	@Format(CommaSeparatedStrings.class)
	List<String> getColumns();

	/** @see #getColumns() */
	void setColumns(List<String> columns);

	/** Whether the names in the {@link #getColumns()} are labels. */
	boolean isLabel();

	/** @see #isLabel() */
	void setLabel(boolean value);

}
