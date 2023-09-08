/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.compile.eval;

import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.knowledge.search.Expression;
import com.top_logic.knowledge.search.ExpressionFactory;
import com.top_logic.model.ModelKind;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.search.expr.SearchExpression;

/**
 * {@link Value} that represents an expression that can be completely compiled to a database query.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CompiledExpression extends Value {

	private MetaObject _type;
	private final Expression _compiled;

	/**
	 * Creates a {@link CompiledExpression}.
	 *
	 * @param type
	 *        The table type of this compiled value.
	 * @param compiled
	 *        See {@link #compiled()}.
	 */
	public CompiledExpression(MetaObject type, Expression compiled) {
		_type = type;
		_compiled = compiled;
	}

	@Override
	public Value processEquals(SearchExpression orig, Value other) {
		if (!other.hasInterpretedPart()) {
			return new CompiledExpression(MOPrimitive.BOOLEAN, ExpressionFactory.eqBinary(_compiled, other.compiled()));
		}
		return new InterpretedExpression(orig);
	}

	@Override
	public Value processAccess(SearchExpression orig, TLStructuredTypePart part) {
		if (_type instanceof MOStructure) {
			MOStructure tableType = (MOStructure) _type;
			String partName = part.getName();
			MOAttribute attr = tableType.getAttributeOrNull(partName);
			if (attr != null) {
				if (!AttributeOperations.getStorageImplementation(part).isReadOnly()) {
					if (part.getModelKind() == ModelKind.PROPERTY) {
						return new CompiledExpression(attr.getMetaObject(),
							ExpressionFactory.attribute(_compiled, attr.getOwner().getName(), attr.getName()));
					}
					else if (part.getModelKind() == ModelKind.REFERENCE) {
						return new CompiledExpression(attr.getMetaObject(),
							ExpressionFactory.reference(_compiled, attr.getOwner().getName(), attr.getName()));
					}
				}
			}
		}
		return new InterpretedExpression(orig);
	}

	@Override
	public Value processAnd(SearchExpression orig, Value other) {
		if (other.hasInterpretedPart()) {
			return new CombinedAndValue(_compiled, other.interpreted());
		} else {
			return new CompiledExpression(MOPrimitive.BOOLEAN, ExpressionFactory.and(_compiled, other.compiled()));
		}
	}

	@Override
	public Value processOr(SearchExpression orig, Value other) {
		if (!other.hasInterpretedPart()) {
			return new CompiledExpression(MOPrimitive.BOOLEAN, ExpressionFactory.or(_compiled, other.compiled()));
		}
		return new InterpretedExpression(orig);
	}

	@Override
	public Value processNot(SearchExpression orig) {
		return new CompiledExpression(MOPrimitive.BOOLEAN, ExpressionFactory.not(_compiled));
	}

	@Override
	public boolean hasCompiledPart() {
		return true;
	}

	@Override
	public Expression compiled() {
		return _compiled;
	}

	@Override
	public boolean hasInterpretedPart() {
		return false;
	}

	@Override
	public SearchExpression interpreted() {
		throw new UnsupportedOperationException();
	}

}
