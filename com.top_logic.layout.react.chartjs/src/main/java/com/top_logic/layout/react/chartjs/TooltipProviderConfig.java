/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.chartjs;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.model.search.expr.config.dom.Expr;

/**
 * Configuration for a named tooltip provider.
 *
 * <p>
 * The {@link #getName() name} is referenced from TL-Script dataset output via the
 * {@code tooltip} key. When a tooltip is requested, the {@link #getExpr() expression}
 * is evaluated with the metadata TL object and returns an HTML string.
 * </p>
 */
public interface TooltipProviderConfig extends ConfigurationItem {

	/** Configuration name for {@link #getName()}. */
	String NAME = "name";

	/** Configuration name for {@link #getExpr()}. */
	String EXPR = "expr";

	/**
	 * The tooltip name, referenced from TL-Script dataset {@code tooltip} key.
	 */
	@Mandatory
	@Name(NAME)
	String getName();

	/**
	 * TL-Script expression that receives the metadata TL object and returns an HTML string.
	 */
	@Mandatory
	@Name(EXPR)
	Expr getExpr();
}
