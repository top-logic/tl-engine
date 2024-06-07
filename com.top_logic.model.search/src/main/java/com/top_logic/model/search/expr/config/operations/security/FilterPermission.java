/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations.security;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.ArgumentDescriptor;
import com.top_logic.model.search.expr.config.operations.string.Translate;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.util.TLContext;
import com.top_logic.util.error.TopLogicException;

/**
 * Generic method filtering {@link TLObject}s such that a given user has one of a given role on the
 * object.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class FilterPermission extends GenericMethod {

	/**
	 * Creates a new {@link FilterPermission}.
	 */
	protected FilterPermission(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new FilterPermission(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return null;
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		Set<BoundObject> objects = objectsToFilter(arguments[0], getArguments()[0]);
		if (objects.isEmpty()) {
			return Collections.emptyList();
		}
		Set<BoundedRole> roles = asRoles(arguments[1], getArguments()[1]);
		Person account = asAccount(arguments[2], getArguments()[2]);
		return AccessManager.getInstance().getAllowedBusinessObjects(account, roles, objects);
	}

	private Set<BoundedRole> asRoles(Object values, SearchExpression expr) {
		return asCollection(values)
			.stream()
			.map(v -> asRole(v, expr))
			.collect(Collectors.toSet());
	}

	private BoundedRole asRole(Object value, SearchExpression expr) {
		if (value instanceof BoundedRole) {
			return (BoundedRole) value;
		}
		BoundedRole role = BoundedRole.getRoleByName(asString(value));
		if (role != null) {
			return role;
		}
		throw new TopLogicException(I18NConstants.ERROR_NOT_A_ROLE__VAL_EXPR.fill(value, expr));
	}

	private Person asAccount(Object value, SearchExpression expr) {
		if (value instanceof String) {
			Person account = Person.byName((String) value);
			if (account == null) {
				throw new TopLogicException(I18NConstants.ERROR_NO_SUCH_ACCOUNT__VAL_EXPR.fill(value, expr));
			}
			return account;
		}
		Person account = (Person) value;
		if (account == null) {
			account = TLContext.getContext().getPerson();
		}
		return account;
	}

	private Set<BoundObject> objectsToFilter(Object values, SearchExpression expr) {
		return asCollection(values)
			.stream()
			.map(item -> asTLObjectNotNull(BoundObject.class, expr, item))
			.collect(Collectors.toSet());
	}

	/**
	 * {@link AbstractSimpleMethodBuilder} creating {@link FilterPermission}.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<FilterPermission> {

		/** Description of parameters for a {@link Translate}. */
		public static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.mandatory("objects")
			.mandatory("roles")
			.optional("account")
			.build();

		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public ArgumentDescriptor descriptor() {
			return DESCRIPTOR;
		}

		@Override
		public FilterPermission build(Expr expr, SearchExpression[] args)
				throws ConfigurationException {
			return new FilterPermission(getConfig().getName(), args);
		}

	}

}
