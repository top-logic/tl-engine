/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.form;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.model.search.expr.config.dom.Expr;

/**
 * Configuration of an {@link Expr}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface WithExpression extends ConfigurationItem {


	/** Configuration name of {@link #getExpression()}. */
	String EXPRESSION = "expression";

	/**
	 * The configured {@link Expr}.
	 */
	@Mandatory
	@Name(EXPRESSION)
	Expr getExpression();

	/**
	 * Setter for {@link #getExpression()}.
	 */
	void setExpression(Expr expr);

}
