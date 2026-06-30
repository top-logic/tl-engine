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
import com.top_logic.element.meta.StorageImplementation;
import com.top_logic.element.meta.kbbased.storage.ColumnStorage;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.search.expr.Access;
import com.top_logic.model.search.expr.And;
import com.top_logic.model.search.expr.IsEqual;
import com.top_logic.model.search.expr.Not;
import com.top_logic.model.search.expr.Or;

/**
 * {@link Value} that represents an expression that can be completely compiled to a database query.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class CompiledExpression extends CompiledValue {

	private MetaObject _type;

	/**
	 * Creates a {@link CompiledExpression}.
	 *
	 * @param type
	 *        The table type of this compiled value.
	 */
	public CompiledExpression(MetaObject type) {
		if (type == MOPrimitive.INVALID_TYPE) {
			throw new IllegalArgumentException();
		}
		_type = type;
	}

	@Override
	public Value processEquals(IsEqual orig, Value other) {
		if (!other.hasInterpretedPart()) {
			CompiledValue otherCompiled = other.compiled();
			if (!otherCompiled.notifyExpectedCompiledType(_type)) {
				return new InterpretedExpression(orig);
			}
			return new CompiledEquals(this, otherCompiled);
		} else if (other instanceof NullLiteral) {
			return new CompiledIsNull(this);
		}
		return new InterpretedExpression(orig);
	}

	@Override
	public Value processAccess(Access orig, TLStructuredTypePart part) {
		if (_type instanceof MOStructure) {
			MOStructure tableType = (MOStructure) _type;

			StorageImplementation storageImplementation = AttributeOperations.getStorageImplementation(part);
			if (storageImplementation instanceof ColumnStorage columnStorage) {
				MOAttribute attr = tableType.getAttributeOrNull(columnStorage.getStorageAttribute());
				if (attr != null) {
					switch (part.getModelKind()) {
						case PROPERTY:
						case REFERENCE:
							return new CompiledAttributeAccess(part, attr, this);
						default:
							break;
					}
				}
			}
		}
		return new InterpretedExpression(orig);
	}

	@Override
	public Value processAnd(And orig, Value other) {
		if (_type != MOPrimitive.BOOLEAN) {
			return new InterpretedExpression(orig);
		}
		CompiledValue compiledAnd;
		CompiledValue otherCompiled = other.compiled();
		if (otherCompiled != null) {
			if (!otherCompiled.notifyExpectedCompiledType(MOPrimitive.BOOLEAN)) {
				return new InterpretedExpression(orig);
			}
			compiledAnd = new CompiledAnd(compiled(), otherCompiled);
		} else {
			compiledAnd = compiled();
		}

		if (other.hasInterpretedPart()) {
			return new CombinedAndValue(compiledAnd, other.interpreted());
		} else {
			return compiledAnd;
		}
	}

	@Override
	public Value processOr(Or orig, Value other) {
		if (_type != MOPrimitive.BOOLEAN) {
			return new InterpretedExpression(orig);
		}
		if (!other.hasInterpretedPart()) {
			CompiledValue otherCompiled = other.compiled();
			if (!otherCompiled.notifyExpectedCompiledType(MOPrimitive.BOOLEAN)) {
				return new InterpretedExpression(orig);
			}
			return new CompiledOr(this, otherCompiled);
		}
		return new InterpretedExpression(orig);
	}

	@Override
	public Value processNot(Not orig) {
		return new CompiledNot(this);
	}

	@Override
	public boolean notifyExpectedCompiledType(MetaObject type) {
		return _type.isSubtypeOf(type);
	}

	@Override
	public MetaObject compiledType() {
		return _type;
	}

}
