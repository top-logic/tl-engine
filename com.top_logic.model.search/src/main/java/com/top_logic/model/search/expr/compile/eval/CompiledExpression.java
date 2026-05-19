/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.compile.eval;

import java.util.function.Function;

import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.StorageImplementation;
import com.top_logic.element.meta.kbbased.storage.ColumnStorage;
import com.top_logic.knowledge.search.Expression;
import com.top_logic.knowledge.search.ExpressionFactory;
import com.top_logic.knowledge.search.InternalExpressionFactory;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.compile.transform.FilterCompiler.Parameters;

/**
 * {@link Value} that represents an expression that can be completely compiled to a database query.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CompiledExpression extends Value {

	private MetaObject _type;

	private final Function<Parameters, Expression> _compiled;

	/**
	 * Creates a {@link CompiledExpression} whose compiled expression has no
	 * {@link ExpressionFactory#param(String) parameters}.
	 *
	 * @param type
	 *        The table type of this compiled value.
	 * @param compiled
	 *        See {@link #compiled()}.
	 */
	public CompiledExpression(MetaObject type, Expression compiled) {
		this(type, params -> compiled);
	}

	/**
	 * Creates a {@link CompiledExpression}.
	 *
	 * @param type
	 *        The table type of this compiled value.
	 * @param compiled
	 *        See {@link #compiled()}.
	 */
	public CompiledExpression(MetaObject type, Function<Parameters, Expression> compiled) {
		if (type == MOPrimitive.INVALID_TYPE) {
			throw new IllegalArgumentException();
		}
		_type = type;
		_compiled = compiled;
	}

	@Override
	public Value processEquals(SearchExpression orig, Value other) {
		if (!other.hasInterpretedPart()) {
			if (!other.notifyExpectedCompiledType(_type)) {
				return new InterpretedExpression(orig);
			}
			return new CompiledExpression(MOPrimitive.BOOLEAN,
				params -> ExpressionFactory.eqBinary(_compiled.apply(params), other.compiled().apply(params)));
		} else if (other instanceof NullLiteral) {
			return new CompiledExpression(MOPrimitive.BOOLEAN,
				params -> ExpressionFactory.isNull(compiled().apply(params)));
		}
		return new InterpretedExpression(orig);
	}

	@Override
	public Value processAccess(SearchExpression orig, TLStructuredTypePart part) {
		if (_type instanceof MOStructure) {
			MOStructure tableType = (MOStructure) _type;

			StorageImplementation storageImplementation = AttributeOperations.getStorageImplementation(part);
			if (!storageImplementation.isReadOnly()) {
				if (storageImplementation instanceof ColumnStorage columnStorage) {
					MOAttribute attr = tableType.getAttributeOrNull(columnStorage.getStorageAttribute());
					if (attr != null) {
						switch (part.getModelKind()) {
							case PROPERTY:
								return new CompiledExpression(attr.getMetaObject(),
									params -> InternalExpressionFactory.attributeTyped(_compiled.apply(params),
										attr));
							case REFERENCE:
								return new CompiledExpression(attr.getMetaObject(),
									params -> InternalExpressionFactory.referenceTyped(_compiled.apply(params),
										(MOReference) attr));
							default:
								break;
						}
					}
				}
			}
		}
		return new InterpretedExpression(orig);
	}

	@Override
	public Value processAnd(SearchExpression orig, Value other) {
		if (_type != MOPrimitive.BOOLEAN) {
			return new InterpretedExpression(orig);
		}
		Function<Parameters, Expression> compiledAnd;
		if (other.hasCompiledPart()) {
			if (!other.notifyExpectedCompiledType(MOPrimitive.BOOLEAN)) {
				return new InterpretedExpression(orig);
			}
			compiledAnd = params -> ExpressionFactory.and(compiled().apply(params), other.compiled().apply(params));
		} else {
			compiledAnd = compiled();
		}

		if (other.hasInterpretedPart()) {
			return new CombinedAndValue(compiledAnd, other.interpreted());
		} else {
			return new CompiledExpression(MOPrimitive.BOOLEAN, compiledAnd);
		}
	}

	@Override
	public Value processOr(SearchExpression orig, Value other) {
		if (_type != MOPrimitive.BOOLEAN) {
			return new InterpretedExpression(orig);
		}
		if (!other.hasInterpretedPart()) {
			if (!other.notifyExpectedCompiledType(MOPrimitive.BOOLEAN)) {
				return new InterpretedExpression(orig);
			}
			return new CompiledExpression(MOPrimitive.BOOLEAN,
				params -> ExpressionFactory.or(compiled().apply(params), other.compiled().apply(params)));
		}
		return new InterpretedExpression(orig);
	}

	@Override
	public Value processNot(SearchExpression orig) {
		return new CompiledExpression(MOPrimitive.BOOLEAN, params -> ExpressionFactory.not(compiled().apply(params)));
	}

	@Override
	public boolean hasCompiledPart() {
		return true;
	}

	@Override
	public boolean notifyExpectedCompiledType(MetaObject type) {
		return _type.isSubtypeOf(type);
	}

	@Override
	public Function<Parameters, Expression> compiled() {
		return _compiled;
	}

	@Override
	public MetaObject compiledType() {
		return _type;
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
