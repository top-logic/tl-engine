/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import static com.top_logic.model.search.expr.ExpressionUtil.*;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.visit.Visitor;
import com.top_logic.util.Utils;

/**
 * {@link BinaryOperation} representing the equality comparison of {@link #getLeft()} and
 * {@link #getRight()}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class IsEqual extends BinaryOperation implements BooleanExpression {

	/**
	 * Creates a {@link IsEqual}.
	 * 
	 * @param left
	 *        See {@link #getLeft()}.
	 * @param right
	 *        See {@link #getRight()}.
	 */
	IsEqual(SearchExpression left, SearchExpression right) {
		super(left, right);
	}

	@Override
	public <R, A> R visit(Visitor<R, A> visitor, A arg) {
		return visitor.visitEquals(this, arg);
	}

	@Override
	public Object internalEval(EvalContext definitions, Args args) {
		Object leftResult = getLeft().evalWith(definitions, args);
		Object rightResult = getRight().evalWith(definitions, args);

		return compute(leftResult, rightResult);
	}

	/**
	 * Computes the result based on concrete values.
	 */
	public final Object compute(Object leftResult, Object rightResult) {
		return Boolean.valueOf(equals(leftResult, rightResult));
	}

	private boolean equals(Object leftResult, Object rightResult) {
		if (isCollection(leftResult) || isCollection(rightResult)) {
			int leftSize = size(leftResult);
			int rightSize = size(rightResult);
			if (leftSize != rightSize) {
				return false;
			}
			if (leftSize == 0) {
				return true;
			}
			if (leftSize == 1) {
				return equals(singleElement(leftResult), singleElement(rightResult));
			}
			if (isSet(leftResult) && isSet(rightResult)) {
				return Utils.equals(leftResult, rightResult);
			} else if (isList(leftResult) && isList(rightResult)) {
				return equalsCollectionOfEqualLength(leftResult, rightResult);
			} else {
				return Utils.equals(leftResult, rightResult);
			}
		} else if (isNumber(leftResult) && isNumber(rightResult)) {
			Number leftNumber = normalize((Number) leftResult);
			Number rightNumber = normalize((Number) rightResult);
			if (isLong(leftNumber) && isLong(rightNumber)) {
				return leftNumber.longValue() == rightNumber.longValue();
			} else {
				/* Comparing float values is a stupid idea, but there is no precision given,
				 * therefore, do it anyway. Both might represent integer values, in which case the
				 * comparison will work as expected. */
				return leftNumber.doubleValue() == rightNumber.doubleValue();
			}
		} else if (isStringLike(leftResult) || isStringLike(rightResult)) {
			String leftString = asString(leftResult);
			String rightString = asString(rightResult);
			return leftString.equals(rightString);
		} else {
			return Utils.equals(leftResult, rightResult);
		}
	}

	private boolean equalsCollectionOfEqualLength(Object left, Object right) {
		Collection<?> leftCollection = (Collection<?>) left;
		Collection<?> rightCollection = (Collection<?>) right;
		for (Iterator<?> it1 = leftCollection.iterator(), it2 = rightCollection.iterator(); it1.hasNext();) {
			if (!equals(it1.next(), it2.next())) {
				return false;
			}
		}
		return true;
	}

	private static Object singleElement(Object value) {
		if (value instanceof Collection<?>) {
			return ((Collection<?>) value).iterator().next();
		} else {
			return value;
		}
	}

	private static int size(Object value) {
		if (value == null) {
			return 0;
		} else if (value instanceof Collection<?>) {
			return ((Collection<?>) value).size();
		} else {
			return 1;
		}
	}

	private static boolean isStringLike(Object value) {
		return value instanceof CharSequence;
	}

	private static boolean isCollection(Object value) {
		return value instanceof Collection<?>;
	}

	private static boolean isNumber(Object value) {
		return value instanceof Number;
	}

	private static boolean isLong(Number value) {
		return value instanceof Long;
	}

	private static boolean isSet(Object value) {
		return value instanceof Set<?>;
	}

	private static boolean isList(Object value) {
		return value instanceof List<?>;
	}

}