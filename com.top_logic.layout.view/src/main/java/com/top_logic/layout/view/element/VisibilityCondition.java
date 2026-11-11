/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.layout.view.channel.InputScript;
import com.top_logic.layout.view.channel.Inputs;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.ui.ScriptContextVariables;

/**
 * The condition under which a UI unit (navigation item, sidebar group and section, tab, accordion
 * section, tile) is shown in its container.
 *
 * <p>
 * The unit is shown while the {@link #getExpr() expression} yields a true value for the current
 * values of the {@link #getInputs() inputs}, and left out of its container otherwise. The
 * condition is decided anew when an input takes a new value, and when an object an input holds is
 * changed.
 * </p>
 *
 * <pre>
 * &lt;tab id="maturity"&gt;
 *   &lt;visibility inputs="element" expr="$element.instanceOf(`my.app:Project`)"/&gt;
 *   ...
 * &lt;/tab&gt;
 * </pre>
 *
 * @see WithVisibility
 * @see UnitVisibility
 */
public interface VisibilityCondition extends Inputs {

	/** Configuration name for {@link #getExpr()}. */
	String EXPR = "expr";

	/**
	 * TL-Script predicate deciding whether the unit is shown.
	 *
	 * <p>
	 * The predicate sees the {@link #getInputs() inputs} as variables named after their channels,
	 * or by the names given with {@code as}. The unit is shown while it yields a value
	 * {@link SearchExpression#isTrue(Object) TL-Script takes as true}; {@code null}, {@code false},
	 * an empty collection and an empty string leave the unit out.
	 * </p>
	 */
	@Name(EXPR)
	@Mandatory
	@NonNullable
	@ScriptContextVariables(InputScript.Variables.class)
	Expr getExpr();

}
