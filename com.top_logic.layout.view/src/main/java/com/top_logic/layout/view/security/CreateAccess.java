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
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.ui.ScriptContextVariables;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.execution.ExecutableState;

/**
 * {@link AccessCheck} of the creation of an object a command performs.
 *
 * <p>
 * The creation is decided by the model access rights: the user needs the
 * {@link SimpleBoundCommandGroup#CREATE creation} right on the {@link Config#getType() type} in the
 * context the object is created in and, when it is added to a {@link Config#getReference()
 * reference} of that context, the right to write that reference.
 * </p>
 *
 * <p>
 * The context is the value the {@link Config#getIn() in} expression yields from the
 * {@link CommandArguments arguments} of the command, or, without an expression, the object of the
 * innermost enclosing UI unit with an {@link AccessControl}. Without both, or for a unit checked on
 * the security root, the object is created without context, decided against the security root.
 * </p>
 *
 * <p>
 * The decision for the current user, see {@link ModelAccessPolicy#onCreate(TLClass,
 * TLStructuredTypePart, Object)}: hidden when the user can never create the type; disabled when
 * there is no context ({@code null} or an empty collection); disabled with the refusal as reason
 * when the creation is refused in a context (in a collection, every context must allow it).
 * </p>
 *
 * <pre>
 * &lt;access&gt;
 *   &lt;create type="demo.tickets:Comment" in="$ticket" reference="demo.tickets:Ticket#comments"/&gt;
 * &lt;/access&gt;
 * </pre>
 *
 * @see IntrinsicAccessRules#creation(TLModelPartRef, com.top_logic.layout.view.channel.ChannelRef,
 *      String)
 */
public class CreateAccess implements AccessCheck {

	/**
	 * Configuration of a {@link CreateAccess}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends AccessCheck.Config<CreateAccess> {

		/** Tag name of a {@link CreateAccess} in the access checks of a command. */
		String TAG_NAME = "create";

		/** Configuration name for {@link #getType()}. */
		String TYPE = "type";

		/** Configuration name for {@link #getIn()}. */
		String IN = "in";

		/** Configuration name for {@link #getReference()}. */
		String REFERENCE = "reference";

		@Override
		@ClassDefault(CreateAccess.class)
		Class<? extends CreateAccess> getImplementationClass();

		/**
		 * The qualified name of the type of the created object.
		 */
		@Name(TYPE)
		@Mandatory
		TLModelPartRef getType();

		/**
		 * TL-Script expression computing the context(s) the object is created in.
		 *
		 * <p>
		 * The expression sees the arguments of the command as variables, like the scripts of its
		 * actions, and is evaluated without the access rights of the current user. It yields a
		 * single object, a collection of objects (the creation must be allowed in every one), or
		 * {@code null} / an empty collection: then there is no context, and the command is
		 * disabled.
		 * </p>
		 *
		 * <p>
		 * When unset, the context is the object of the enclosing UI unit with an access control;
		 * without one, the object is created without context, decided against the security root.
		 * </p>
		 */
		@Name(IN)
		@Nullable
		@ScriptContextVariables(ActionScriptVariables.class)
		Expr getIn();

		/**
		 * The qualified name of the composition reference of the context the created object is
		 * added to, e.g. {@code demo.tickets:Ticket#comments}.
		 *
		 * <p>
		 * Creating an object there additionally requires the right to write this reference of the
		 * context. The {@link #getType() type} must be compatible with the type of the reference.
		 * </p>
		 */
		@Name(REFERENCE)
		@Nullable
		TLModelPartRef getReference();
	}

	private final TLModelPartRef _typeRef;

	private final TLModelPartRef _referenceRef;

	private final ActionScope _scope;

	private final ActionScript _in;

	private final String _owner;

	private SecuredObject _unit;

	/**
	 * Creates a {@link CreateAccess} from configuration.
	 */
	@CalledByReflection
	public CreateAccess(InstantiationContext context, Config config) {
		_owner = "the access creation at " + config.location();
		_typeRef = config.getType();
		_referenceRef = config.getReference();
		_scope = ActionScope.of(context);
		_in = config.getIn() == null ? null : ActionScript.compileWithoutSecurity(config.getIn(), _scope);
		checkTypes(context);
	}

	private void checkTypes(InstantiationContext context) {
		TLClass type;
		try {
			type = asClass(_typeRef.resolveType());
		} catch (RuntimeException ex) {
			context.error("The type '" + _typeRef.qualifiedName() + "' of " + _owner + " does not exist.", ex);
			return;
		}
		if (type == null) {
			context.error("The type '" + _typeRef.qualifiedName() + "' of " + _owner + " is no class.");
			return;
		}
		if (_referenceRef == null) {
			return;
		}
		TLModelPart part;
		try {
			part = _referenceRef.resolvePart();
		} catch (RuntimeException ex) {
			context.error("The reference '" + _referenceRef.qualifiedName() + "' of " + _owner + " does not exist.",
				ex);
			return;
		}
		if (!(part instanceof TLReference reference) || !reference.isComposite()) {
			context.error("The reference '" + _referenceRef.qualifiedName() + "' of " + _owner
				+ " is no composition reference.");
			return;
		}
		if (!TLModelUtil.isCompatibleType(reference.getType(), type)) {
			context.error("The type '" + _typeRef.qualifiedName() + "' of " + _owner
				+ " is not compatible with the type of the reference '" + _referenceRef.qualifiedName() + "'.");
		}
	}

	private static TLClass asClass(TLType type) {
		return type instanceof TLClass clazz ? clazz : null;
	}

	@Override
	public void bind(InstantiationContext problems, ViewContext context) {
		_unit = _in == null && context != null ? SecuredObject.of(context) : null;
		if (_in == null && _referenceRef != null && (_unit == null || _unit.isSecurityRoot())) {
			problems.error("The reference '" + _referenceRef.qualifiedName() + "' of " + _owner
				+ " requires a context: an '" + Config.IN + "' expression or an enclosing unit with an object.");
		}
	}

	/**
	 * Follows the inputs of the enclosing unit whose object is the context; the inputs of the
	 * command itself, which the context expression reads, are followed by the command.
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
		TLClass type = asClass(_typeRef.resolveType());
		TLStructuredTypePart reference =
			_referenceRef == null ? null : (TLStructuredTypePart) _referenceRef.resolvePart();
		if (type == null) {
			return ExecutableState.NOT_EXEC_HIDDEN;
		}
		if (_in != null) {
			return ModelAccessPolicy.onCreate(type, reference, _in.execute(null, arguments));
		}
		if (_unit == null || _unit.isSecurityRoot()) {
			return ModelAccessPolicy.onType(SimpleBoundCommandGroup.CREATE, type);
		}
		return ModelAccessPolicy.onCreate(type, reference, _unit.getValue());
	}

}
