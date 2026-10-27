/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.security;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.listener.Registration;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.command.ActionArguments;
import com.top_logic.layout.view.command.ActionScope;
import com.top_logic.layout.view.command.ActionScript;
import com.top_logic.layout.view.command.ActionScriptVariables;
import com.top_logic.layout.view.command.CommandArguments;
import com.top_logic.model.TLClass;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.ui.ScriptContextVariables;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.execution.ExecutableState;

/**
 * {@link AccessCheck} of the operation a command performs on an object.
 *
 * <p>
 * The checked object is the value the {@link Config#getObject() object} expression yields from the
 * {@link CommandArguments arguments} of the command. Without an expression, it is the object of the
 * innermost enclosing UI unit with an {@link AccessControl} (see {@link SecuredObject#of(ViewContext)}),
 * and without such a unit, or for a unit checked on the security root, the security root.
 * </p>
 *
 * <p>
 * The decision for the current user:
 * </p>
 * <ul>
 * <li>Hidden: the user can never hold the operation - no role can hold it on the checked type (see
 * {@link ModelAccessPolicy#isOffered(BoundCommandGroup, TLClass)}), or, on the security root, it is
 * not allowed there.</li>
 * <li>Disabled: there is no object to check ({@code null} or an empty collection), or the operation
 * is refused on a checked object, giving the refusal as reason. An object that is no instance of the
 * checked type is refused.</li>
 * <li>Otherwise, as the {@link com.top_logic.tool.execution.service.CommandApprovalService} decides
 * about the operation on every checked object (see
 * {@link ModelAccessPolicy#approval(BoundCommandGroup, com.top_logic.model.TLObject)}).</li>
 * </ul>
 *
 * <pre>
 * &lt;access&gt;
 *   &lt;operation name="Approve" object="$milestones.map(m -&gt; $m.container())"
 *     type="tl.demo.projectManagement:ProjectScope"/&gt;
 * &lt;/access&gt;
 * </pre>
 */
public class OperationAccess implements AccessCheck {

	/**
	 * Configuration of an {@link OperationAccess}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends AccessCheck.Config<OperationAccess> {

		/** Tag name of an {@link OperationAccess} in the access checks of a command. */
		String TAG_NAME = "operation";

		/** Configuration name for {@link #getName()}. */
		String NAME = "name";

		/** Configuration name for {@link #getObject()}. */
		String OBJECT = "object";

		/** Configuration name for {@link #getType()}. */
		String TYPE = "type";

		@Override
		@ClassDefault(OperationAccess.class)
		Class<? extends OperationAccess> getImplementationClass();

		/**
		 * The operation the command performs.
		 *
		 * <p>
		 * The name of a command group: a built-in operation such as {@code Write} or {@code Delete},
		 * or an operation declared for the checked type in the {@code <security-config>}. The
		 * grants deciding the operation are those of the model access rights.
		 * </p>
		 */
		@Name(NAME)
		@Mandatory
		CommandGroupReference getName();

		/**
		 * TL-Script expression computing the object(s) the {@link #getName() operation} is checked
		 * on.
		 *
		 * <p>
		 * The expression sees the arguments of the command as variables, like the scripts of its
		 * actions. It yields a single object, a collection of objects (the operation must be allowed
		 * on every element), or {@code null} / an empty collection: then there is no object, and
		 * the command is disabled. The expression is evaluated without the access rights of the
		 * current user, so that an object the user may not see is refused rather than dropped.
		 * </p>
		 *
		 * <p>
		 * When unset, the operation is checked on the object of the enclosing UI unit with an access
		 * control, or on the security root.
		 * </p>
		 */
		@Name(OBJECT)
		@Nullable
		@ScriptContextVariables(ActionScriptVariables.class)
		Expr getObject();

		/**
		 * The qualified name of the type of the {@link #getObject() checked object(s)}.
		 *
		 * <p>
		 * Mandatory with an {@link #getObject() object}. The {@link #getName() operation} must apply
		 * to this type. An object that is no instance of the type is refused.
		 * </p>
		 */
		@Name(TYPE)
		@Nullable
		TLModelPartRef getType();
	}

	private final OperationCheck _check;

	private final ActionScope _scope;

	private final ActionScript _object;

	private final boolean _valid;

	private SecuredObject _unit;

	/**
	 * Creates an {@link OperationAccess} from configuration.
	 */
	@CalledByReflection
	public OperationAccess(InstantiationContext context, Config config) {
		_scope = ActionScope.of(context);
		_check = new OperationCheck("the access operation '" + config.getName().id() + "' at " + config.location(),
			config.getName(), config.getType());
		Expr object = config.getObject();
		_valid = _check.checkDeclaration(context, object != null, Config.OBJECT, Config.TYPE);
		_object = object == null ? null : ActionScript.compileWithoutSecurity(object, _scope);
	}

	@Override
	public void bind(InstantiationContext problems, ViewContext context) {
		_unit = _object == null && context != null ? SecuredObject.of(context) : null;
		if (_valid) {
			_check.checkApplies(problems, _unit);
		}
	}

	/**
	 * Follows the inputs of the enclosing unit whose object is checked; the inputs of the command
	 * itself, which the object expression reads, are followed by the command.
	 */
	@Override
	public Runnable observe(Runnable revalidate) {
		if (_unit == null || _unit.isSecurityRoot()) {
			return () -> {
				// Nothing observed.
			};
		}
		Registration registration = _unit.observe(revalidate);
		return registration::dispose;
	}

	@Override
	public ExecutableState isExecutable(Object input) {
		return isExecutableFor(_scope.arguments(input));
	}

	@Override
	public ExecutableState isExecutableFor(ActionArguments arguments) {
		BoundCommandGroup operation = _check.operation();
		if (operation == null) {
			return ExecutableState.NOT_EXEC_HIDDEN;
		}
		if (_object != null) {
			TLClass type = _check.type();
			if (type == null) {
				return ExecutableState.NOT_EXEC_HIDDEN;
			}
			return _check.state(operation, type, _object.execute(null, arguments));
		}
		if (_unit == null || _unit.isSecurityRoot()) {
			return OperationCheck.rootState(operation);
		}
		return _check.state(operation, _unit.getType(), _unit.getValue());
	}

}
