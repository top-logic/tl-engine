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
 * <p>
 * When the expression {@link #usesSecurity() uses security}, the enumeration delivers only the
 * instances the current user may read, see {@link SearchExpression#filterSecurity(Object)}. An
 * object the user must not read therefore reaches a secured script only through a reference from
 * another object, which delivers it unfiltered (like the user interface shows a referenced object
 * by its label); reading its attributes is denied by the attribute access.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class All extends SearchExpressionWithSecurity {

	private TLStructuredType _type;

	/**
	 * Creates a {@link All}.
	 * 
	 * @param type
	 *        See {@link #getInstanceType()}
	 * @param usesSecurity
	 *        See {@link #usesSecurity()}.
	 */
	All(TLStructuredType type, boolean usesSecurity) {
		super(usesSecurity);
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
		return all(this, _type, usesSecurity());
	}

	/**
	 * Retrieves all instances of the given type.
	 *
	 * <p>
	 * The classifiers of a {@link TLEnumeration} are model elements and are delivered unfiltered,
	 * consistent with {@link SearchExpression#filterSecurity(Object)}.
	 * </p>
	 *
	 * @param self
	 *        The expression retrieving the instances, for error reporting.
	 * @param type
	 *        The {@link TLClass} or {@link TLEnumeration} whose instances are retrieved.
	 * @param usesSecurity
	 *        Whether to deliver only the instances the current user may read.
	 */
	public static List<? extends TLObject> all(SearchExpression self, TLStructuredType type, boolean usesSecurity) {
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
				return usesSecurity ? filterReadable(result) : result;
			}

			case ENUMERATION: {
				return ((TLEnumeration) type).getClassifiers();
			}

			default: {
				throw new TopLogicException(I18NConstants.ERROR_NEITHER_CLASS_NOR_ENUM__TYPE_EXPR.fill(type, self));
			}
		}
	}

	/**
	 * Drops the instances from the given list that the current user must not read.
	 *
	 * @param instances
	 *        The instances of a {@link TLClass}.
	 * @return The readable instances.
	 */
	@SuppressWarnings("unchecked")
	static List<TLObject> filterReadable(List<TLObject> instances) {
		return (List<TLObject>) filterSecurity(instances);
	}

}
