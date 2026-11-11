/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.table;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.layout.view.channel.InputScript;
import com.top_logic.layout.view.channel.Supplies;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.ui.ScriptContextVariables;

/**
 * Configuration of a table that tints each of its rows in a color role computed from the row's
 * object.
 *
 * @implNote {@link RowFunction#rowRole(RowRoleConfig)} compiles the configured expression for the
 *           rows of a table.
 */
public interface RowRoleConfig extends ConfigurationItem {

	/** Configuration name for {@link #getRowRole()}. */
	String ROW_ROLE = "row-role";

	/**
	 * TL-Script expression computing the color role a row is tinted in from the row's object.
	 *
	 * <p>
	 * The expression sees the row object as the variable {@value RowAsConfig#DEFAULT_ROW_AS}, or by
	 * the name the table gives it with {@value RowAsConfig#ROW_AS}, and the values of the table's
	 * inputs as variables named after their channels or by their <code>as</code> names.
	 * </p>
	 *
	 * <p>
	 * The result is the name of a role: <code>error</code>, <code>warning</code>,
	 * <code>success</code> and <code>info</code> mark a state, <code>brand</code> sets a row
	 * apart, <code>neutral</code> names the role without a tint, and <code>category-1</code> to
	 * <code>category-8</code> tell rows apart whose meaning the application defines. Nothing or an empty text leaves the
	 * row without a role, a text naming no role is an error. The design system decides how a role
	 * looks in each theme. The highlight of a selected row wins over its role; a cell with a
	 * {@link AbstractColumnDeclaration.Config#getCellRole() role of its own} is drawn in that role.
	 * </p>
	 *
	 * <p>
	 * The role follows the row's data: it is computed again whenever the table updates its rows. A
	 * group header row gets no role. The {@link RowClassConfig#getRowClass() row class} next to it
	 * is the hook for the application's own stylesheet.
	 * </p>
	 *
	 * <p>
	 * Example marking the failed objects of a table:
	 * </p>
	 *
	 * <pre>
	 * $row.get(`my:Type#state`) == `my:State#failed` ? "error" : null
	 * </pre>
	 */
	@Name(ROW_ROLE)
	@Nullable
	@Supplies(value = RowAsConfig.DEFAULT_ROW_AS, renamedBy = RowAsConfig.ROW_AS)
	@ScriptContextVariables(InputScript.Variables.class)
	Expr getRowRole();

}
