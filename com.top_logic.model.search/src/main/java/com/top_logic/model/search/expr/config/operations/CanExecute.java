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
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.security.ModelAccessRights;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.simple.CommandGroupRegistry;
import com.top_logic.util.TLContext;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link GenericMethod} reporting whether the current user may perform a custom business operation
 * (a {@link BoundCommandGroup} referenced by its id) on an object.
 *
 * <p>
 * Explicit, non-failing check for custom operations (e.g. {@code Approve}, {@code Cancel}): the
 * second argument names the operation by its command group id. Returns the outcome of the access
 * check as a boolean.
 * </p>
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CanExecute extends GenericMethod {

	/**
	 * Creates a {@link CanExecute}.
	 */
	CanExecute(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new CanExecute(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return TLModelUtil.findType(TypeSpec.BOOLEAN_TYPE);
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		TLObject object = asTLObject(arguments[0]);
		if (object == null) {
			return Boolean.FALSE;
		}
		String operationId = asString(arguments[1]);
		BoundCommandGroup group = CommandGroupRegistry.getInstance().getGroup(operationId);
		if (group == null) {
			throw new TopLogicException(
				I18NConstants.ERROR_UNKNOWN_OPERATION__NAME_EXPR.fill(operationId, this));
		}
		return Boolean.valueOf(ModelAccessRights.getInstance().isAllowed(TLContext.currentUser(), object, group));
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
	 * {@link MethodBuilder} creating the {@code canExecute} function.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<CanExecute> {

		private static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.mandatory("object")
			.mandatory("operation")
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
		public CanExecute build(Expr expr, SearchExpression[] args) throws ConfigurationException {
			return new CanExecute(getName(), args);
		}
	}
}
