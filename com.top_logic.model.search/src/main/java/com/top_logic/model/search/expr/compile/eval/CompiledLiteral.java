/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.model.search.expr.compile.eval;

import com.top_logic.dob.MetaObject;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.knowledge.search.Expression;
import com.top_logic.knowledge.search.ExpressionFactory;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.expr.CompareOp;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.IsEqual;

/**
 * {@link CompiledExpression} representing a {@link ExpressionFactory#literal(Object)}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CompiledLiteral extends CompiledExpression {

	private Object _literal;

	/**
	 * Creates a new {@link CompiledLiteral}.
	 * 
	 * @param type
	 *        Type of the literal.
	 * @param literal
	 *        The literal value.
	 */
	public CompiledLiteral(MetaObject type, Object literal) {
		super(type);
		_literal = literal;
	}

	@Override
	public Expression buildExpression(EvalContext context) {
		return ExpressionFactory.literal(_literal);
	}

	@Override
	public Object eval(TLObject item, EvalContext context) {
		return _literal;
	}

	@Override
	public Value processEquals(IsEqual orig, Value other) {
		if (!(other instanceof CompiledLiteral)) {
			// equals is symmetric. This literal may hold a double by automatic type conversion
			// actual a integer was meant.
			return other.processEquals(orig, this);
		}
		return super.processEquals(orig, other);
	}

	@Override
	public Value processCompareOp(CompareOp orig, Value other) {
		if (!other.hasInterpretedPart()) {
			CompiledValue otherCompiled = other.compiled();
			if (!this.notifyExpectedCompiledType(otherCompiled.compiledType())) {
				return new InterpretedExpression(orig);
			}
			if (!CompiledCompareOp.supportsCompiledCompare(compiledType())) {
				// Ordering of non-numeric/temporal types (e.g. strings) would use the database
				// collation, which may differ from the in-memory comparison. Keep it interpreted.
				return new InterpretedExpression(orig);
			}
			return new CompiledCompareOp(this, otherCompiled, orig.getKind());
		} else if (other instanceof NullLiteral) {
			// Comparison with null always leads to null
			return other;
		}
		return new InterpretedExpression(orig);
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * In addition to the plain subtype check, this may <em>adapt</em> the numeric literal to the
	 * expected type and {@link #updateCompiledType(MetaObject) update} its {@link #compiledType()}.
	 * This reverses the automatic {@link Double} normalization that TL-Script applies to numeric
	 * literals (a literal {@code 1000} is a {@link Double}), so that a comparison against a typed
	 * database column (e.g. an {@code Integer} column) can be delegated to the database. The literal
	 * is converted to the boxed type matching the target column type, so that the knowledge base
	 * builds a compatible SQL comparison.
	 * </p>
	 *
	 * <p>
	 * The adaptation only happens when it is <em>value-preserving</em>: to an integer type only for
	 * an integral value within the target range, to {@link Float} only when the value is exactly
	 * representable as a {@code float}. Otherwise the literal is left unchanged and {@code false} is
	 * returned so that the comparison stays in the interpreted evaluation. Because both the
	 * interpreted evaluation and SQL compare numbers by value, the value-preserving adaptation keeps
	 * both paths consistent.
	 * </p>
	 *
	 * <p>
	 * Note: This is a mutating operation. It is expected to be called at most once per literal (as
	 * the operand of a single comparison) while its {@link Value} tree is being compiled.
	 * </p>
	 */
	@Override
	public boolean notifyExpectedCompiledType(MetaObject type) {
		if (super.notifyExpectedCompiledType(type)) {
			return true;
		}
		if (!(_literal instanceof Number) || !(type instanceof MOPrimitive)) {
			return false;
		}
		Object adapted = adaptNumber(((Number) _literal).doubleValue(), (MOPrimitive) type);
		if (adapted == null) {
			return false;
		}
		_literal = adapted;
		updateCompiledType(type);
		return true;
	}

	/**
	 * Value-preserving conversion of the given numeric value to the boxed type matching the given
	 * target column type, or {@code null} if the value cannot be represented without loss.
	 */
	private static Object adaptNumber(double value, MOPrimitive targetType) {
		switch (targetType.getDefaultSQLType()) {
			case BYTE:
				return isIntegral(value, Byte.MIN_VALUE, Byte.MAX_VALUE) ? Byte.valueOf((byte) value) : null;
			case SHORT:
				return isIntegral(value, Short.MIN_VALUE, Short.MAX_VALUE) ? Short.valueOf((short) value) : null;
			case INT:
				return isIntegral(value, Integer.MIN_VALUE, Integer.MAX_VALUE) ? Integer.valueOf((int) value) : null;
			case LONG:
				return isIntegral(value, Long.MIN_VALUE, Long.MAX_VALUE) ? Long.valueOf((long) value) : null;
			case FLOAT:
				// A range check is not sufficient here: a double within the float range is generally
				// not representable as a float, so narrowing would lose precision and the compiled
				// comparison would differ from the interpreted one. Only accept values that survive
				// the round-trip through float unchanged.
				@SuppressWarnings("cast")
				double roundTrip = (double) (float) value;
				return roundTrip == value ? Float.valueOf((float) value) : null;
			case DOUBLE:
				return Double.valueOf(value);
			default:
				return null;
		}
	}

	private static boolean isIntegral(double value, long min, long max) {
		return value == Math.floor(value) && value >= min && value <= max;
	}

	@Override
	public boolean needsEvalContext() {
		return false;
	}

}

