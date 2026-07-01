/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.model.search.expr.compile.eval;

import static com.top_logic.knowledge.search.ExpressionFactory.*;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.knowledge.search.Expression;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.expr.CompareKind;
import com.top_logic.model.search.expr.CompareOp;
import com.top_logic.model.search.expr.EvalContext;

/**
 * {@link CompiledExpression} representing an order comparison ({@code <}, {@code <=}, {@code >},
 * {@code >=}) of two {@link CompiledValue}s that is evaluated in the database query.
 *
 * <p>
 * This is only used for numeric and temporal types (see
 * {@link #supportsCompiledCompare(MetaObject)}). Ordering of other types (in particular
 * {@link String}s) would use the database collation, which may differ from the in-memory comparison,
 * and is therefore kept in the interpreted evaluation.
 * </p>
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CompiledCompareOp extends CompiledExpression {

	private final CompiledValue _left;

	private final CompiledValue _right;

	private final CompareKind _compareKind;

	/**
	 * Creates a new {@link CompiledCompareOp}.
	 */
	public CompiledCompareOp(CompiledValue left, CompiledValue right, CompareKind compareKind) {
		super(MOPrimitive.BOOLEAN);
		_left = left;
		_right = right;
		_compareKind = compareKind;
	}

	/**
	 * Whether a compare operation ({@code <}, {@code <=}, {@code >}, {@code >=}) on the given compiled
	 * type may be evaluated in the database.
	 *
	 * <p>
	 * Only numeric and temporal types are supported. Ordering of other types (in particular
	 * {@link String}s) would use the database collation, which may differ from the in-memory
	 * comparison; such comparisons are therefore kept in the interpreted evaluation to keep both
	 * paths consistent.
	 * </p>
	 */
	public static boolean supportsCompiledCompare(MetaObject type) {
		if (!(type instanceof MOPrimitive)) {
			return false;
		}
		switch (((MOPrimitive) type).getDefaultSQLType()) {
			case BYTE:
			case SHORT:
			case INT:
			case LONG:
			case FLOAT:
			case DOUBLE:
			case DECIMAL:
			case DATE:
			case TIME:
			case DATETIME:
				return true;
			default:
				return false;
		}
	}

	@Override
	public Expression buildExpression(EvalContext context) throws CompiledValue.IncompatibleTypes {
		Expression left = _left.buildExpression(context);
		Expression right = _right.buildExpression(context);
		switch (_compareKind) {
			case GE:
				return ge(left, right);
			case GT:
				return gt(left, right);
			case LE:
				return le(left, right);
			case LT:
				return lt(left, right);
		}
		throw new UnreachableAssertion("Uncovered: " + _compareKind);
	}

	@Override
	public Object eval(TLObject item, EvalContext context) {
		Object left = _left.eval(item, context);
		Object right = _right.eval(item, context);
		return CompareOp.compare(left, right, _compareKind);
	}

	@Override
	public boolean needsEvalContext() {
		return _left.needsEvalContext() || _right.needsEvalContext();
	}

}

