/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.table;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.model.search.expr.config.dom.Expr;

/**
 * Configuration of a table that marks each of its rows with a CSS class computed from the row's
 * object.
 *
 * @implNote {@link RowClassFunction} evaluates the configured function for the rows of a table.
 */
public interface RowClassConfig extends ConfigurationItem {

	/** Configuration name for {@link #getRowClass()}. */
	String ROW_CLASS = "row-class";

	/**
	 * TL-Script function computing the CSS class of a row from the row's object.
	 *
	 * <p>
	 * The function receives the values of the table's declared inputs first, in declaration order,
	 * and the row object last. A table without inputs passes the row object only.
	 * </p>
	 *
	 * <p>
	 * The result is put on the row: nothing or an empty string gives the row no class, a list
	 * gives it all of its members as classes, any other value its text. The standard classes
	 * tl-status-error, tl-status-warning, tl-status-success and tl-status-info tint the row in the
	 * color of the corresponding status. Any other class is styled by the application's own
	 * stylesheet.
	 * </p>
	 *
	 * <p>
	 * The class follows the row's data: it is computed again whenever the table updates its rows. A
	 * group header row gets no class.
	 * </p>
	 *
	 * <p>
	 * Example marking the failed objects of a table:
	 * </p>
	 *
	 * <pre>
	 * row -&gt; $row.get(`my:Type#state`) == `my:State#failed` ? "tl-status-error" : null
	 * </pre>
	 */
	@Name(ROW_CLASS)
	@Nullable
	Expr getRowClass();

}
