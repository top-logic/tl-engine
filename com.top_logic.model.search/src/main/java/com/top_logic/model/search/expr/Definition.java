/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import com.top_logic.basic.NamedConstant;
import com.top_logic.model.search.expr.interpreter.SearchExpressionPart;

/**
 * {@link SearchExpressionPart} that can be referenced from other parts of a search.
 * 
 * @see Var
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface Definition extends SearchExpressionPart {

	/**
	 * The ID of the bound variable.
	 */
	NamedConstant getKey();

}
