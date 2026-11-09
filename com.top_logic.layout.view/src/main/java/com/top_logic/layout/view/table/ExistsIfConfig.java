/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.table;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.view.channel.InputScript;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.ui.ScriptContextVariables;

/**
 * Configuration of a column declaration whose columns exist only while a condition holds.
 *
 * @see ColumnSetup#exists()
 */
public interface ExistsIfConfig extends ConfigurationItem {

	/** Configuration name for {@link #getExistsIf()}. */
	String EXISTS_IF = "exists-if";

	/**
	 * The condition under which the columns of this declaration exist for the table: an expression
	 * yielding a boolean.
	 *
	 * <p>
	 * While the condition does not hold, the columns do not exist for the table: they are neither
	 * displayed nor offered in the column selection, and a sort, a filter or a grouping by them
	 * takes no effect. The condition is followed: as soon as one of
	 * the channels it reads takes a new value, the columns appear or disappear, without the table
	 * being built again. The user's arrangement of the columns - their place, their width, whether
	 * they are shown - is kept meanwhile and applies again as soon as they exist again.
	 * </p>
	 *
	 * <p>
	 * This is no choice of the user, in contrast to whether a column is shown from the start
	 * (<code>visible</code>): a column that is not shown from the start is offered in the column
	 * selection, a column that does not exist is not.
	 * </p>
	 *
	 * <p>
	 * The expression sees the values of the inputs of the declaration as variables named after
	 * their channels or by their <code>as</code> names, of the table for a declaration without
	 * inputs of its own. It sees no row: whether a column exists is decided for the whole table.
	 * Nothing at all counts as <code>false</code>. Unset (the default), the columns always exist.
	 * </p>
	 *
	 * <p>
	 * Example showing the cost columns only while the channel <code>showCosts</code> holds
	 * <code>true</code>:
	 * </p>
	 *
	 * <pre>
	 * &lt;column name="costs" exists-if="$showCosts"/&gt;
	 * </pre>
	 */
	@Name(EXISTS_IF)
	@ScriptContextVariables(InputScript.Variables.class)
	Expr getExistsIf();

}
