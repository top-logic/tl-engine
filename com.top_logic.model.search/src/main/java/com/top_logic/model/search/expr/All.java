/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.col.CloseableIterator;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLInstanceAccess;
import com.top_logic.model.TLObject;
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
		return all(this, _type);
	}

	/**
	 * Retrieves all instances of the given type.
	 */
	public static List<? extends TLObject> all(SearchExpression self, TLStructuredType type) {
		switch (type.getModelKind()) {
			case CLASS: {
				ArrayList<TLObject> result = new ArrayList<>();
				try (CloseableIterator<TLObject> instances =
					type.getModel().getQuery(TLInstanceAccess.class).getAllInstances((TLClass) type)) {
					while (instances.hasNext()) {
						TLObject instance = instances.next();
						result.add(instance);
					}
				}
				return result;
			}

			case ENUMERATION: {
				return ((TLEnumeration) type).getClassifiers();
			}

			default: {
				throw new TopLogicException(I18NConstants.ERROR_NEITHER_CLASS_NOR_ENUM__TYPE_EXPR.fill(type, self));
			}
		}
	}

}