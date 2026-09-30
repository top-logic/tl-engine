/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
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
import com.top_logic.tool.boundsec.CommandGroupReference;

/**
 * {@link GenericMethod} reporting whether the current user may perform a {@link BoundCommandGroup
 * command group} (configured on the {@link Builder}) on an object.
 *
 * <p>
 * In contrast to the implicit security enforcement of e.g. {@code delete(...)} or {@code set(...)},
 * this function never fails and never filters silently: it simply returns the outcome of the access
 * check as a boolean. A script can thus branch explicitly on missing rights instead of running into
 * a {@link com.top_logic.util.error.TopLogicException} or receiving a filtered result.
 * </p>
 *
 * <p>
 * The checked command group is a configuration option of the {@link Builder}. Additional
 * {@code canXxx} functions (e.g. for a custom operation like {@code Approve}) can therefore be
 * added by a plain {@code <method>} registration with a different {@code command-group}, without a
 * new implementation class.
 * </p>
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CanAccess extends GenericMethod {

	/**
	 * Description of the single object argument of the {@link CanAccess} functions.
	 */
	static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
		.mandatory("object")
		.build();

	private final BoundCommandGroup _commandGroup;

	/**
	 * Creates a {@link CanAccess}.
	 */
	CanAccess(String name, BoundCommandGroup commandGroup, SearchExpression[] arguments) {
		super(name, arguments);
		_commandGroup = commandGroup;
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new CanAccess(getName(), _commandGroup, arguments);
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
		return Boolean.valueOf(ModelAccessRights.getInstance().isAllowed(object, _commandGroup));
	}

	/**
	 * The permission check depends on the current user and the live security configuration and can
	 * therefore not be evaluated at compile time.
	 */
	@Override
	public boolean canEvaluateAtCompileTime(Object[] arguments) {
		return false;
	}

	@Override
	public Object getId() {
		// Identity is the (expression class, command group) pair: the function name is irrelevant to
		// the behaviour, the command group alone would clash with other function families reusing the
		// same command group, and the class alone would clash across command groups.
		return List.of(CanAccess.class, _commandGroup.getID());
	}

	/**
	 * {@link MethodBuilder} creating a {@link CanAccess} for a configured {@link BoundCommandGroup}.
	 */
	public static final class Builder extends AbstractMethodBuilder<Builder.Config, CanAccess> {

		/**
		 * Configuration options for {@link CanAccess.Builder}.
		 */
		public interface Config extends AbstractMethodBuilder.Config<Builder> {
			/**
			 * The command group whose access is checked by the created function.
			 */
			@Mandatory
			@Name("command-group")
			CommandGroupReference getCommandGroup();
		}

		/**
		 * Creates a {@link Builder}.
		 */
		@CalledByReflection
		public Builder(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		public ArgumentDescriptor descriptor() {
			return DESCRIPTOR;
		}

		@Override
		public CanAccess build(Expr expr, SearchExpression[] args) throws ConfigurationException {
			// Note: The command group is resolved lazily (not in the constructor): the builder is
			// instantiated at service startup, before the CommandGroupRegistry is available.
			BoundCommandGroup group = getConfig().getCommandGroup().resolve();
			if (group == null) {
				throw error(I18NConstants.ERROR_UNKNOWN_OPERATION__NAME_EXPR
					.fill(getConfig().getCommandGroup().id(), toString(expr)));
			}
			return new CanAccess(getName(), group, args);
		}

		@Override
		public Object getId() {
			return List.of(CanAccess.class, getConfig().getCommandGroup().id());
		}
	}
}
