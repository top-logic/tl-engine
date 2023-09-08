/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.expr.transform;

import static com.top_logic.knowledge.search.ExpressionFactory.*;

import com.top_logic.knowledge.search.InSet;
import com.top_logic.knowledge.search.Intersection;
import com.top_logic.knowledge.search.SetExpression;
import com.top_logic.knowledge.search.Substraction;

/**
 * Replaces {@link Intersection} and {@link Substraction} nodes with equivalent
 * {@link InSet} expressions.
 * 
 * <ul>
 * <li>intersection(s1, s2) to filter(s1, inSet(s2))</li>
 * <li>substraction(s1, s2) to filter(s1, not(inSet(s2)))</li>
 * </ul>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SetSimplification extends ExpressionTransformer<Void> {
	
	/**
	 * Singleton {@link SetSimplification} instance.
	 */
	public static final SetSimplification INSTANCE = new SetSimplification();
	
	private static final Void none = null;

	private SetSimplification() {
		// Singleton constructor.
	}
	
	/**
	 * Simplifies the given Expression
	 * 
	 * @see SetSimplification
	 */
	public static SetExpression simplifySets(SetExpression expr) {
		return expr.visitSetExpr(INSTANCE, none);
	}

	@Override
	protected SetExpression process(Intersection expr, SetExpression left, SetExpression right) {
		return filter(left, inSet(context(), right));
	}
	
	@Override
	protected SetExpression process(Substraction expr, SetExpression left, SetExpression right) {
		return filter(left, not(inSet(context(), right)));
	}


}
