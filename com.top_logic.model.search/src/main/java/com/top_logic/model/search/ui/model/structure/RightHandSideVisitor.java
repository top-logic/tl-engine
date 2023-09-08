/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model.structure;

import com.top_logic.model.search.ui.model.NavigationValue;
import com.top_logic.model.search.ui.model.QueryValue;
import com.top_logic.model.search.ui.model.SubQuery;
import com.top_logic.model.search.ui.model.literal.LiteralObjectSet;
import com.top_logic.model.search.ui.model.literal.LiteralObjectValue;
import com.top_logic.model.search.ui.model.literal.LiteralPrimitiveValue;

/**
 * Visitor interface for {@link RightHandSide} parts.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface RightHandSideVisitor<R, A> {

	/**
	 * Visit case for {@link LiteralObjectValue}.
	 */
	R visitLiteralObjectValue(LiteralObjectValue value, A arg);

	/**
	 * Visit case for {@link LiteralObjectSet}.
	 */
	R visitLiteralObjectSet(LiteralObjectSet value, A arg);

	/**
	 * Visit case for {@link LiteralPrimitiveValue}.
	 */
	R visitLiteralPrimitiveValue(LiteralPrimitiveValue value, A arg);

	/**
	 * Visit case for {@link NavigationValue}.
	 */
	R visitNavigationValue(NavigationValue value, A arg);

	/**
	 * Visit case for {@link QueryValue}.
	 */
	R visitQueryValue(QueryValue value, A arg);

	/**
	 * Visit case for {@link SubQuery}.
	 */
	R visitSubQuery(SubQuery value, A arg);

}
