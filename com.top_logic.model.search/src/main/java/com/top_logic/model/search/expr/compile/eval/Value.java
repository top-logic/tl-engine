/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.compile.eval;

import java.util.function.Function;

import com.top_logic.dob.MetaObject;
import com.top_logic.knowledge.search.Expression;
import com.top_logic.knowledge.search.ExpressionFactory;
import com.top_logic.knowledge.service.db2.expr.visit.PolymorphicTypeComputation;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.search.expr.Access;
import com.top_logic.model.search.expr.And;
import com.top_logic.model.search.expr.IsEqual;
import com.top_logic.model.search.expr.Not;
import com.top_logic.model.search.expr.Or;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.compile.transform.FilterCompiler.Parameters;

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
	public abstract Value processEquals(SearchExpression orig, Value other);

	/**
	 * Interprets an {@link Access} expression on this this {@link Value}.
	 * 
	 * @param orig
	 *        The original {@link SearchExpression} representing the processed operation.
	 * @return The resulting {@link Value}.
	 */
	public abstract Value processAccess(SearchExpression orig, TLStructuredTypePart part);

	/**
	 * Interprets an {@link Not} expression on this this {@link Value}.
	 * 
	 * @param orig
	 *        The original {@link SearchExpression} representing the processed operation.
	 * @return The resulting {@link Value}.
	 */
	public abstract Value processNot(SearchExpression orig);

	/**
	 * Interprets an {@link Or} comparison of this and the other {@link Value}.
	 * 
	 * @param orig
	 *        The original {@link SearchExpression} representing the processed operation.
	 * @param other
	 *        The other {@link Value}.
	 * @return The resulting {@link Value}.
	 */
	public abstract Value processOr(SearchExpression orig, Value other);

	/**
	 * Interprets an {@link And} comparison of this and the other {@link Value}.
	 * 
	 * @param orig
	 *        The original {@link SearchExpression} representing the processed operation.
	 * @param other
	 *        The other {@link Value}.
	 * @return The resulting {@link Value}.
	 */
	public abstract Value processAnd(SearchExpression orig, Value other);

	/**
	 * Whether this {@link Value} has a {@link #compiled() compilation result}.
	 */
	public abstract boolean hasCompiledPart();

	/**
	 * The compilation result of this {@link Value}, if {@link #hasCompiledPart()}.
	 * 
	 * @return Mapping that creates the actual {@link Expression} based on the given
	 *         {@link Parameters} object.
	 */
	public abstract Function<Parameters, Expression> compiled();

	/**
	 * The database type of the {@link #compiled() compilation result}, if
	 * {@link #hasCompiledPart()}.
	 */
	public abstract MetaObject compiledType();

	/**
	 * Checks that the {@link #compiledType()} is compatible with the given {@link MetaObject} if
	 * possible.
	 * 
	 * <p>
	 * The value may adapt its own {@link #compiledType()} with respect to the argument type.
	 * </p>
	 * 
	 * @param type
	 *        The expected super type of {@link #compiledType()} for this {@link Value}.
	 * @return <code>true</code> if {@link #compiledType()} is compatible with the given type.
	 */
	public abstract boolean notifyExpectedCompiledType(MetaObject type);

	/**
	 * Whether this {@link Value} has an {@link #interpreted() interpretation result}.
	 */
	public abstract boolean hasInterpretedPart();

	/**
	 * The interpretation of this {@link Value}, if {@link #hasInterpretedPart()}.
	 */
	public abstract SearchExpression interpreted();

	/**
	 * Creates a {@link Value} from a literal value.
	 * 
	 * @param orig
	 *        The original {@link SearchExpression} representing the given literal value.
	 * @param literal
	 *        The literal value.
	 * @return A {@link Value} representing the literal.
	 */
	public static Value literal(SearchExpression orig, Object literal) {
		MetaObject literalType = PolymorphicTypeComputation.getLiteralType(literal);
		if (literalType == MetaObject.INVALID_TYPE) {
			return new InterpretedExpression(orig);
		}
		return new CompiledExpression(literalType, ExpressionFactory.literal(literal));
	}

}
