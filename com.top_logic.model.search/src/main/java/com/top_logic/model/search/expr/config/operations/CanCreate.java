/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations;

import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.element.meta.TypeSpec;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.security.ModelAccessRights;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.TLContext;

/**
 * {@link GenericMethod} reporting whether the current user may create a new child object in the
 * given composition attribute of the given parent instance.
 *
 * <p>
 * Explicit, non-failing counterpart to the implicit create check: returns the outcome of
 * {@link ModelAccessRights#isAllowedCreate(com.top_logic.knowledge.wrap.person.Person, TLObject, TLStructuredTypePart)}
 * as a boolean.
 * </p>
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CanCreate extends GenericMethod {

	/**
	 * Creates a {@link CanCreate}.
	 */
	CanCreate(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new CanCreate(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return TLModelUtil.findType(TypeSpec.BOOLEAN_TYPE);
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		// Note: The parent may be null for a top-level creation.
		TLObject parent = asTLObject(arguments[0]);
		TLStructuredTypePart compositionAttribute = asTypePart(this, arguments[1]);
		return Boolean.valueOf(
			ModelAccessRights.getInstance().isAllowedCreate(TLContext.currentUser(), parent, compositionAttribute));
	}

	/**
	 * The permission check depends on the current user and the live security configuration and can
	 * therefore not be evaluated at compile time.
	 */
	@Override
	public boolean canEvaluateAtCompileTime(Object[] arguments) {
		return false;
	}

	/**
	 * {@link MethodBuilder} creating the {@code canCreate} function.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<CanCreate> {

		private static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.mandatory("parent")
			.mandatory("reference")
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
		public CanCreate build(Expr expr, SearchExpression[] args) throws ConfigurationException {
			return new CanCreate(getName(), args);
		}
	}
}
