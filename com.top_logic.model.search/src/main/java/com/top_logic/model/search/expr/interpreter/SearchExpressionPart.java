/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.interpreter;

import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.model.search.expr.SearchExpression;

/**
 * Part of a {@link SearchExpression}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface SearchExpressionPart extends TypedAnnotatable {
	// Pure marker interface.
}
