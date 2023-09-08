/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import java.util.ArrayList;

import com.top_logic.basic.col.CloseableIterator;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLInstanceAccess;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.visit.Visitor;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link SearchExpression} conceptually looking up all instances of a certain {@link #getInstanceType()}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class All extends SearchExpression {

	private TLStructuredType _type;

	/**
	 * Creates a {@link All}.
	 * 
	 * @param type
	 *        See {@link #getInstanceType()}
	 */
	All(TLStructuredType type) {
		_type = type;
	}

	/**
	 * The model {@link TLStructuredType} to load instances from.
	 */
	public TLStructuredType getInstanceType() {
		return _type;
	}

	/**
	 * @see #getInstanceType()
	 */
	public void setClassType(TLStructuredType classType) {
		_type = classType;
	}

	@Override
	public <R, A> R visit(Visitor<R, A> visitor, A arg) {
		return visitor.visitAll(this, arg);
	}

	@Override
	public Object internalEval(EvalContext definitions, Args args) {
		switch (_type.getModelKind()) {
			case CLASS: {
				ArrayList<Object> result = new ArrayList<>();
				try (CloseableIterator<?> instances =
					_type.getModel().getQuery(TLInstanceAccess.class).getAllInstances((TLClass) _type)) {
					while (instances.hasNext()) {
						Object instance = instances.next();
						result.add(instance);
					}
				}
				return result;
			}

			case ENUMERATION: {
				return ((TLEnumeration) _type).getClassifiers();
			}

			default: {
				throw new TopLogicException(I18NConstants.ERROR_NEITHER_CLASS_NOR_ENUM__TYPE_EXPR.fill(_type, this));
			}
		}
	}

}