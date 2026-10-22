/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.chartjs;

import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.config.container.ConfigPart;
import com.top_logic.layout.view.channel.InputScript;
import com.top_logic.layout.view.channel.Supplies;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.ui.ScriptContextVariables;

/**
 * Configuration for a named tooltip provider.
 *
 * <p>
 * The {@link #getName() name} is referenced from TL-Script dataset output via the
 * {@code tooltip} key. When a tooltip is requested, the {@link #getExpr() expression}
 * is evaluated for the metadata object of the data point and returns an HTML string.
 * </p>
 */
public interface TooltipProviderConfig extends ConfigPart {

	/** Configuration name for {@link #getName()}. */
	String NAME = "name";

	/** Configuration name for {@link #getExpr()}. */
	String EXPR = "expr";

	/** Configuration name for {@link #getAs()}. */
	String AS = "as";

	/** Name of the variable holding the data point's metadata object when no other name is given. */
	String DEFAULT_AS = ClickHandlerConfig.DEFAULT_AS;

	/**
	 * The tooltip name, referenced from TL-Script dataset {@code tooltip} key.
	 */
	@Mandatory
	@Name(NAME)
	String getName();

	/**
	 * TL-Script expression computing the HTML of the tooltip of a data point.
	 *
	 * <p>
	 * The expression sees the values of the {@link ChartElement.Config#getInputs() inputs} of the
	 * chart as variables named after their channels, or by their {@code as} names, and the metadata
	 * object of the data point as the variable named by {@link #getAs() as}:
	 * </p>
	 *
	 * <pre>
	 * toString($element.size()) + ' transactions'
	 * </pre>
	 */
	@Mandatory
	@Name(EXPR)
	@Supplies(value = DEFAULT_AS, renamedBy = AS)
	@ScriptContextVariables(InputScript.Variables.class)
	Expr getExpr();

	/**
	 * The name of the variable holding the metadata object of the data point in the
	 * {@link #getExpr() expression}.
	 *
	 * <p>
	 * An input of the chart of the same name is a configuration error and is renamed with
	 * <code>as</code>.
	 * </p>
	 */
	@Name(AS)
	@StringDefault(DEFAULT_AS)
	String getAs();
}
