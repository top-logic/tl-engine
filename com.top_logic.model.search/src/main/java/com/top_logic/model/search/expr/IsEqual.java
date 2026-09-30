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

import com.top_logic.basic.util.WithEmptiness;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.model.TLClassifier;
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
	public Boolean compute(Object leftResult, Object rightResult) {
		return Boolean.valueOf(equals(leftResult, rightResult));
	}

	/**
	 * Checks whether the both elements are treated as equal in TL-Script.
	 * 
	 * @param left
	 *        Left element of the equality check. May be <code>null</code>.
	 * @param right
	 *        Right element of the equality check. May be <code>null</code>.
	 */
	public static boolean equals(Object left, Object right) {
		// An "empty" value (e.g. an empty HTML text) is indistinguishable from null and the empty
		// string in TL-Script, keeping equality consistent with isEmpty().
		left = normalizeEmpty(left);
		right = normalizeEmpty(right);

		if (isCollection(left) || isCollection(right)) {
			int leftSize = size(left);
			int rightSize = size(right);
			if (leftSize != rightSize) {
				return false;
			}
			if (leftSize == 0) {
				return true;
			}
			if (leftSize == 1) {
				return equals(singleElement(left), singleElement(right));
			}
			if (isSet(left) && isSet(right)) {
				return Utils.equals(left, right);
			} else if (isList(left) && isList(right)) {
				return equalsCollectionOfEqualLength(left, right);
			} else {
				return Utils.equals(left, right);
			}
		} else if (isNumber(left) && isNumber(right)) {
			Number leftNumber = normalize((Number) left);
			Number rightNumber = normalize((Number) right);
			if (isLong(leftNumber) && isLong(rightNumber)) {
				return leftNumber.longValue() == rightNumber.longValue();
			} else {
				/* Comparing float values is a stupid idea, but there is no precision given,
				 * therefore, do it anyway. Both might represent integer values, in which case the
				 * comparison will work as expected. */
				return leftNumber.doubleValue() == rightNumber.doubleValue();
			}
		} else if (isStringLike(left) || isStringLike(right)) {
			String leftString = asString(left);
			String rightString = asString(right);
			return leftString.equals(rightString);
		} else if (left instanceof TLClassifier && right instanceof TLClassifier) {
			// Historic objects refer to historic classifiers, but when comparing classifiers, they
			// are compared without version, since the model must only be used in current and
			// classifiers are part of the data and the model.

			TLClassifier leftClassifier = (TLClassifier) left;
			TLClassifier rightClassifier = (TLClassifier) right;

			return WrapperHistoryUtils.equalsUnversioned(leftClassifier, rightClassifier);
		} else {
			return Utils.equals(left, right);
		}
	}

	private static boolean equalsCollectionOfEqualLength(Object left, Object right) {
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

	/**
	 * Maps a {@link WithEmptiness} value that {@link WithEmptiness#isEmpty() reports itself empty} to
	 * <code>null</code>, so that it compares equal to <code>null</code> and the empty string. All
	 * other values are returned unchanged.
	 */
	private static Object normalizeEmpty(Object value) {
		if (value instanceof WithEmptiness emptiness && emptiness.isEmpty()) {
			return null;
		}
		return value;
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