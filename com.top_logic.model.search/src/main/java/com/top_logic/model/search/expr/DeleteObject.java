/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import java.util.Collection;
import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.SingleArgMethodBuilder;
import com.top_logic.model.security.ModelAccessRights;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.util.TLContext;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link SearchExpression} deleting objects.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DeleteObject extends GenericMethodWithSecurity implements WithFlatMapSemantics<Void> {

	/**
	 * Creates a {@link DeleteObject}.
	 */
	DeleteObject(String name, SearchExpression[] arguments, boolean usesSecurity) {
		super(name, arguments, usesSecurity);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new DeleteObject(getName(), arguments, usesSecurity());
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return null;
	}

	@Override
	public boolean isSideEffectFree() {
		return false;
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		return evalPotentialFlatMap(definitions, arguments[0], null);
	}

	@Override
	public Object evalFlatMap(EvalContext definitions, Collection<?> base, Void param) {
		deleteAll(base, usesSecurity());
		return null;
	}

	@Override
	public Object evalDirect(EvalContext definitions, Object singletonValue, Void param) {
		TLObject obj = asTLObject(singletonValue);
		if (obj != null) {
			delete(obj, usesSecurity());
		}
		return null;
	}

	/**
	 * Deletes the given object or collection of objects the way the TL-Script function
	 * {@code delete()} does.
	 *
	 * @param value
	 *        A {@link TLObject}, or a collection of values of which the {@link TLObject}s are
	 *        deleted. Other values are ignored.
	 * @param withSecurity
	 *        Whether the current user must hold the {@link SimpleBoundCommandGroup#DELETE delete}
	 *        right on each deleted object. A refusal is reported before anything is deleted.
	 * @throws TopLogicException
	 *         With {@link I18NConstants#DELETE_PERMISSION_DENIED__OBJECT}, when the user may not
	 *         delete one of the objects.
	 */
	public static void delete(Object value, boolean withSecurity) {
		if (value instanceof Collection<?> collection) {
			deleteAll(collection, withSecurity);
		} else if (value instanceof TLObject obj) {
			delete(obj, withSecurity);
		}
	}

	private static void delete(TLObject obj, boolean withSecurity) {
		if (withSecurity) {
			checkDeletePermission(obj);
		}
		obj.tDelete();
	}

	private static void deleteAll(Collection<?> base, boolean withSecurity) {
		List<TLObject> objects = base.stream()
			.filter(TLObject.class::isInstance)
			.map(TLObject.class::cast)
			.toList();
		if (withSecurity) {
			objects.forEach(DeleteObject::checkDeletePermission);
		}
		KBUtils.deleteAll(objects);
	}

	/**
	 * Checks that the current user may delete the given object.
	 *
	 * @throws TopLogicException
	 *         With {@link I18NConstants#DELETE_PERMISSION_DENIED__OBJECT}, when the user may not
	 *         delete it.
	 */
	public static void checkDeletePermission(TLObject obj) {
		if (!ModelAccessRights.getInstance().isAllowed(TLContext.currentUser(), obj,
			SimpleBoundCommandGroup.DELETE)) {
			throw new TopLogicException(I18NConstants.DELETE_PERMISSION_DENIED__OBJECT.fill(obj));
		}
	}

	/**
	 * Builder creating a {@link DeleteObject} expression.
	 */
	public static class Builder extends SingleArgMethodBuilder<DeleteObject> {
		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		protected DeleteObject internalBuild(Expr expr, SearchExpression argument, SearchExpression[] allArgs)
				throws ConfigurationException {
			return new DeleteObject(getName(), allArgs, true);
		}
	}

}
