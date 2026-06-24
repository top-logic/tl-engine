/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.SingleArgMethodBuilder;
import com.top_logic.model.security.ModelAccessRights;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.util.TLContext;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link SearchExpression} creating a new object of a given {@link TLClass} type.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DeleteObject extends GenericMethod implements WithFlatMapSemantics<Void> {

	/**
	 * Creates a {@link DeleteObject}.
	 */
	DeleteObject(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new DeleteObject(getName(), arguments);
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
		List<TLObject> forbidden;
		Consumer<TLObject> collectForbidden;
		if (definitions.usesSecurity()) {
			ModelAccessRights accessRights = ModelAccessRights.getInstance();
			Person user = TLContext.currentUser();
			forbidden = new ArrayList<>();
			collectForbidden = object -> {
				if (!accessRights.isAllowed(user, object, SimpleBoundCommandGroup.DELETE)) {
					forbidden.add(object);
				}
			};
		} else {
			forbidden = Collections.emptyList();
			collectForbidden = object -> {
				// ignore
			};
		}
		List<TLObject> objects = base.stream()
			.filter(TLObject.class::isInstance)
			.map(TLObject.class::cast)
			.peek(collectForbidden)
			.toList();
		
		if (!forbidden.isEmpty()) {
			throw new TopLogicException(I18NConstants.DELETE_PERMISSION_DENIED__OBJECT.fill(forbidden.get(0)));
		}

		KBUtils.deleteAll(objects);
		return null;
	}

	@Override
	public Object evalDirect(EvalContext definitions, Object singletonValue, Void param) {
		TLObject obj = asTLObject(singletonValue);
		if (obj != null) {
			if (definitions.usesSecurity()) {
				if (!ModelAccessRights.getInstance().isAllowed(obj, SimpleBoundCommandGroup.DELETE)) {
					throw new TopLogicException(I18NConstants.DELETE_PERMISSION_DENIED__OBJECT.fill(obj));
				}
			}
			obj.tDelete();
		}
		return null;
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
			return new DeleteObject(getName(), allArgs);
		}
	}

}
