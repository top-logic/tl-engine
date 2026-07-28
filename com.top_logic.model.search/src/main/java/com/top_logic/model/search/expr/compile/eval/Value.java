/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.compile.eval;

import com.top_logic.dob.MetaObject;
import com.top_logic.knowledge.service.db2.expr.visit.PolymorphicTypeComputation;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.search.expr.Access;
import com.top_logic.model.search.expr.And;
import com.top_logic.model.search.expr.CompareOp;
import com.top_logic.model.search.expr.IsEqual;
import com.top_logic.model.search.expr.Literal;
import com.top_logic.model.search.expr.Not;
import com.top_logic.model.search.expr.Or;
import com.top_logic.model.search.expr.SearchExpression;

/**
 * Building block of a {@link SearchExpression} interpreter that separates expressions into those
 * that can be compiled to a database query and those that must be interpreted in memory.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class Value {

	/**
	 * Interprets an {@link IsEqual} comparison of this and the other {@link Value}.
	 * 
	 * @param orig
	 *        The original {@link SearchExpression} representing the processed operation.
	 * @param other
	 *        The other {@link Value}.
	 * @return The resulting {@link Value}.
	 */
	public abstract Value processEquals(IsEqual orig, Value other);

	/**
	 * Interprets an {@link Access} expression on this this {@link Value}.
	 * 
	 * @param orig
	 *        The original {@link SearchExpression} representing the processed operation.
	 * @return The resulting {@link Value}.
	 */
	public abstract Value processAccess(Access orig, TLStructuredTypePart part);

	/**
	 * Interprets an {@link Not} expression on this this {@link Value}.
	 * 
	 * @param orig
	 *        The original {@link SearchExpression} representing the processed operation.
	 * @return The resulting {@link Value}.
	 */
	public abstract Value processNot(Not orig);

	/**
	 * Interprets an {@link Or} comparison of this and the other {@link Value}.
	 * 
	 * @param orig
	 *        The original {@link SearchExpression} representing the processed operation.
	 * @param other
	 *        The other {@link Value}.
	 * @return The resulting {@link Value}.
	 */
	public abstract Value processOr(Or orig, Value other);

	/**
	 * Interprets an {@link And} comparison of this and the other {@link Value}.
	 * 
	 * @param orig
	 *        The original {@link SearchExpression} representing the processed operation.
	 * @param other
	 *        The other {@link Value}.
	 * @return The resulting {@link Value}.
	 */
	public abstract Value processAnd(And orig, Value other);

	/**
	 * Interprets a {@link CompareOp} order comparison ({@code <}, {@code <=}, {@code >}, {@code >=})
	 * of this and the other {@link Value}.
	 *
	 * @param orig
	 *        The original {@link SearchExpression} representing the processed operation.
	 * @param other
	 *        The other {@link Value}.
	 * @return The resulting {@link Value}.
	 */
	public abstract Value processCompareOp(CompareOp orig, Value other);

	/**
	 * Whether this {@link Value} has a {@link #compiled() compilation result}.
	 */
	public final boolean hasCompiledPart() {
		return compiled() != null;
	}

	/**
	 * Compiled part of this {@link Value}. May be <code>null</code>.
	 */
	public abstract CompiledValue compiled();

	/**
	 * Whether this {@link Value} has an {@link #interpreted() interpretation result}.
	 */
	public final boolean hasInterpretedPart() {
		return interpreted() != null;
	}

	/**
	 * The interpretation of this {@link Value}. May be <code>null</code>.
	 */
	public abstract SearchExpression interpreted();

	/**
	 * Creates a {@link Value} from a literal value.
	 * 
	 * @param orig
	 *        The original {@link SearchExpression} representing the given literal value.
	 * @param literalValue
	 *        The literal value.
	 * @return A {@link Value} representing the literal.
	 */
	public static Value literal(Literal orig, Object literalValue) {
		if (literalValue == null) {
			// Null literal is not allowed in the KB.
			return new NullLiteral(orig);
		}
		MetaObject literalType = PolymorphicTypeComputation.getLiteralType(literalValue);
		if (literalType == MetaObject.INVALID_TYPE) {
			return new InterpretedExpression(orig);
		}
		return new CompiledLiteral(literalType, literalValue);
	}

}
