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
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.security.ModelAccessRights;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.util.TLContext;

/**
 * {@link GenericMethod} reporting whether the current user may perform a {@link BoundCommandGroup
 * command group} (configured on the {@link Builder}) on a single attribute of an object.
 *
 * <p>
 * Attribute-level counterpart of {@link CanAccess}. This function never fails and never filters
 * silently, but returns the outcome of the attribute-level access check as a boolean, so that a
 * script can branch explicitly (e.g. before a {@code set(...)} that would otherwise throw when the
 * attribute must not be written).
 * </p>
 *
 * <p>
 * The checked command group is a configuration option of the {@link Builder}. Additional
 * {@code canXxxAttribute} functions can therefore be added by a plain {@code <method>} registration
 * with a different {@code command-group}, without a new implementation class. The command group
 * serves as the {@link #getId() expression id} so that the search compiler materializes each
 * registration with its own builder.
 * </p>
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CanAccessAttribute extends GenericMethod {

	/**
	 * Description of the (object, attribute) arguments of the {@link CanAccessAttribute} functions.
	 */
	static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
		.mandatory("object")
		.mandatory("attribute")
		.build();

	private final BoundCommandGroup _commandGroup;

	/**
	 * Creates a {@link CanAccessAttribute}.
	 */
	CanAccessAttribute(String name, BoundCommandGroup commandGroup, SearchExpression[] arguments) {
		super(name, arguments);
		_commandGroup = commandGroup;
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new CanAccessAttribute(getName(), _commandGroup, arguments);
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
		TLStructuredTypePart part = asTypePart(this, arguments[1]);
		return Boolean.valueOf(
			ModelAccessRights.getInstance().isAllowed(TLContext.currentUser(), object, part, _commandGroup));
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
		return List.of(CanAccessAttribute.class, _commandGroup.getID());
	}

	/**
	 * {@link MethodBuilder} creating a {@link CanAccessAttribute} for a configured
	 * {@link BoundCommandGroup}.
	 */
	public static final class Builder extends AbstractMethodBuilder<Builder.Config, CanAccessAttribute> {

		/**
		 * Configuration options for {@link CanAccessAttribute.Builder}.
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
		public CanAccessAttribute build(Expr expr, SearchExpression[] args) throws ConfigurationException {
			// Note: The command group is resolved lazily (not in the constructor): the builder is
			// instantiated at service startup, before the CommandGroupRegistry is available.
			BoundCommandGroup group = getConfig().getCommandGroup().resolve();
			if (group == null) {
				throw error(I18NConstants.ERROR_UNKNOWN_OPERATION__NAME_EXPR
					.fill(getConfig().getCommandGroup().id(), toString(expr)));
			}
			return new CanAccessAttribute(getName(), group, args);
		}

		@Override
		public Object getId() {
			return List.of(CanAccessAttribute.class, getConfig().getCommandGroup().id());
		}
	}
}
