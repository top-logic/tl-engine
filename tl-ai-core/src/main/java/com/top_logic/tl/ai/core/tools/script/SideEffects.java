/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.script;

import java.util.List;

import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.Throw;
import com.top_logic.model.search.expr.ToSystemCalendar;
import com.top_logic.model.search.expr.ToUserCalendar;
import com.top_logic.model.search.expr.Update;
import com.top_logic.model.search.expr.compile.transform.HasSideEffects;
import com.top_logic.model.search.expr.visit.DefaultDescendingVisitor;

/**
 * What a script does besides computing its value, read from its compiled expression.
 *
 * <p>
 * The tools that read with a script - {@code query} and {@code present} - take a script without an
 * effect only, and run it without anybody being asked.
 * </p>
 *
 * <p>
 * <b>Why not the engine's {@link HasSideEffects}.</b> It answers for the outermost call of a
 * function only and does not look into the function's arguments, so {@code toString($x.delete())}
 * or {@code all(`m:T`).foreach(x -> $x.delete()).firstElement()} pass it as free of effects. The
 * compiler it was written for loses an optimization at worst; a read-only tool would run a function
 * with an effect outside the knowledge base - which nothing undoes - without anybody being asked.
 * This class looks at every part of the expression.
 * </p>
 */
final class SideEffects {

	private SideEffects() {
		// Static utility.
	}

	/**
	 * Whether the expression has any effect: a write to the knowledge base, or a function that acts
	 * outside it.
	 */
	static boolean any(SearchExpression expression) {
		return Boolean.TRUE.equals(expression.visit(AnyEffect.INSTANCE, null));
	}

	/**
	 * Whether calling the given function has an effect at all.
	 *
	 * <p>
	 * The engine marks a few functions as not free of side effects that have none that lasts: a
	 * calendar conversion returns a new object (the mark keeps the compiler from sharing it), and
	 * {@code throw} ends the evaluation. A date question needs the conversions, and treating them as
	 * effects would refuse it.
	 * </p>
	 */
	private static boolean hasEffect(GenericMethod function) {
		if (function.isSideEffectFree()) {
			return false;
		}
		return !(function instanceof ToUserCalendar || function instanceof ToSystemCalendar
			|| function instanceof Throw);
	}

	/** Whether there is an effect anywhere in the expression. */
	private static final class AnyEffect extends DefaultDescendingVisitor<Boolean, Void> {

		static final AnyEffect INSTANCE = new AnyEffect();

		@Override
		protected Boolean composeUpdate(Update expr, Void arg, Boolean selfResult, Boolean valueResult) {
			return Boolean.TRUE;
		}

		@Override
		protected Boolean composeGenericMethod(GenericMethod expr, Void arg, List<Boolean> argumentsResult) {
			return Boolean.valueOf(hasEffect(expr) || Boolean.TRUE.equals(combine(argumentsResult)));
		}

		@Override
		protected Boolean combine(Boolean result1, Boolean result2) {
			return Boolean.valueOf(Boolean.TRUE.equals(result1) || Boolean.TRUE.equals(result2));
		}

		@Override
		protected Boolean none() {
			return Boolean.FALSE;
		}
	}


}
