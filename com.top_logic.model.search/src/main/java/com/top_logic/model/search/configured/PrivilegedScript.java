/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.configured;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.form.values.edit.annotation.PropertyEditor;
import com.top_logic.layout.form.values.edit.editor.PlainEditor;
import com.top_logic.model.TLFormObjectBase;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.constraint.CheckExprWithAdditionalParams;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.search.ui.ScriptContextVariables;
import com.top_logic.model.security.CheckedObjects;
import com.top_logic.model.security.CheckedObjects.Decision;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.util.TLContext;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link ConfiguredScript} that is authorized as a whole.
 *
 * <p>
 * A call from a secured evaluation checks the {@link Config#getCommandGroup() command group} once
 * on the {@link Config#getSecurityObject() security object} computed from the arguments, runs the
 * implementation without security, and filters the result for the Discover right. A call from an
 * unsecured evaluation is not checked. See the developer article
 * <code>access-privileged-scripts</code>.
 * </p>
 */
public class PrivilegedScript extends ConfiguredScript {

	/**
	 * Configuration of a {@link PrivilegedScript}.
	 */
	@TagName(Config.TAG_NAME)
	@DisplayOrder({
		Config.NAME_ATTRIBUTE,
		Config.LABEL,
		Config.DESCRIPTION,
		Config.PARAMETERS,
		Config.IMPLEMENTATION,
		Config.COMMAND_GROUP,
		Config.SECURITY_OBJECT,
		Config.ON_DENIED,
	})
	public interface Config extends ConfiguredScript.Config {

		/** Tag name of a {@link PrivilegedScript} in the configured scripts. */
		String TAG_NAME = "privileged-script";

		/** Configuration name of {@link #getSecurityObject()}. */
		String SECURITY_OBJECT = "security-object";

		/** Configuration name of {@link #getCommandGroup()}. */
		String COMMAND_GROUP = "command-group";

		/** Configuration name of {@link #getOnDenied()}. */
		String ON_DENIED = "on-denied";

		@Override
		@ClassDefault(PrivilegedScript.class)
		Class<? extends ConfiguredScript> getImplementationClass();

		/**
		 * The object on which the {@link #getCommandGroup() command group} is checked.
		 *
		 * <p>
		 * The expression accesses the {@link #getParameters() parameters} as variables. It is
		 * evaluated without security; the arguments of a secured call contain only objects the user
		 * may discover. The result is a single object or a collection of objects; for a collection,
		 * the command group must be allowed on every element. <code>null</code> and an empty
		 * collection deny the call. An edit overlay of a form is checked as the object it edits. An
		 * element that is no stored object (e.g. a create overlay, a transient object, or one
		 * created in the current transaction) denies the whole call.
		 * </p>
		 */
		@Mandatory
		@PropertyEditor(PlainEditor.class)
		@Constraint(value = CheckExprWithAdditionalParams.class, args = { @Ref(PARAMETERS) })
		@ScriptContextVariables(ScriptParameterVariables.class)
		@Name(SECURITY_OBJECT)
		Expr getSecurityObject();

		/**
		 * The operation the current user must hold on the {@link #getSecurityObject() security
		 * object} to call the function.
		 */
		@Mandatory
		@Name(COMMAND_GROUP)
		CommandGroupReference getCommandGroup();

		/**
		 * The result of a call the current user is not allowed to make.
		 *
		 * <p>
		 * The expression accesses the {@link #getParameters() parameters} as variables and is
		 * evaluated with the security of the user. It may deliver a replacement value (e.g.
		 * <code>null</code>) or fail with a message of its own (<code>throw(...)</code>). Without
		 * it, a denied call fails with a generic message.
		 * </p>
		 */
		@Nullable
		@PropertyEditor(PlainEditor.class)
		@Constraint(value = CheckExprWithAdditionalParams.class, args = { @Ref(PARAMETERS) })
		@ScriptContextVariables(ScriptParameterVariables.class)
		@Name(ON_DENIED)
		Expr getOnDenied();

	}

	private final BoundCommandGroup _commandGroup;

	private volatile QueryExecutor _securityObject;

	private volatile QueryExecutor _onDenied;

	/**
	 * Creates a {@link PrivilegedScript}.
	 */
	public PrivilegedScript(InstantiationContext context, Config config) {
		super(context, config);
		_commandGroup = config.getCommandGroup().resolve();
		if (_commandGroup == null) {
			context.error(I18NConstants.ERROR_UNKNOWN_COMMAND_GROUP__GROUP_NAME
				.fill(config.getCommandGroup().id(), config.getName()).toString());
		}
	}

	@Override
	public Config getConfig() {
		return (Config) super.getConfig();
	}

	/**
	 * The resolved {@link Config#getCommandGroup() command group}.
	 */
	public BoundCommandGroup getCommandGroup() {
		return _commandGroup;
	}

	/**
	 * Whether the current user may call this function with the given arguments.
	 *
	 * @see #check(Object[])
	 */
	public boolean isAllowed(Object[] arguments) {
		return check(arguments).isAllowed();
	}

	/**
	 * Decides whether the current user may call this function with the given arguments.
	 *
	 * <p>
	 * The {@link Config#getSecurityObject() security object} is computed without security, in an
	 * evaluation context of its own. It is a single object or a collection; for a collection, the
	 * user must hold the {@link Config#getCommandGroup() command group} on every element. An edit
	 * overlay of a form ({@link TLFormObjectBase} that is not {@link TLFormObjectBase#isCreate()
	 * create}) is checked as the object it {@link TLFormObjectBase#getEditedObject() edits}.
	 * <code>null</code> and an empty collection deny the call. An element that is no stored
	 * {@link TLObject} denies the whole call: a value that is no {@link TLObject}, a create
	 * overlay, a transient object, and an object created in the current transaction. The access
	 * check passes every such object (it has no roles yet), so accepting it would let a caller
	 * authorize the call with an object of its own making.
	 * </p>
	 *
	 * <p>
	 * The check is made on the persistent object. A security object expression that navigates from
	 * an overlay argument (e.g. <code>$m.get(...)</code>) reads its not yet stored values, and so does
	 * any navigation while a {@link com.top_logic.model.form.TracingScope} is open on the current
	 * thread (a traced computation of a form answers every attribute read from the form's overlays).
	 * A security object expression should therefore start from the argument itself or navigate along
	 * references the user can not change in the form.
	 * </p>
	 *
	 * @param arguments
	 *        The arguments of the call, in the order of the {@link Config#getParameters()
	 *        parameters}.
	 * @return The decision, see {@link CheckedObjects#checkStored(Person, Object, BoundCommandGroup)}.
	 */
	public Decision check(Object[] arguments) {
		Object securityObject = securityObjectExecutor().executeIntermediate(arguments);
		return CheckedObjects.checkStored(TLContext.currentUser(), securityObject, _commandGroup);
	}

	/**
	 * The result of a call the current user is not {@link #isAllowed(Object[]) allowed} to make.
	 *
	 * @param arguments
	 *        The arguments of the call.
	 * @param decision
	 *        The {@link #check(Object[]) decision} that denied the call.
	 * @return The result of {@link Config#getOnDenied()}, evaluated with security.
	 * @throws TopLogicException
	 *         if no {@link Config#getOnDenied()} is configured, with a message naming the reason of
	 *         the given decision.
	 */
	public Object denied(Object[] arguments, Decision decision) {
		Expr onDenied = getConfig().getOnDenied();
		if (onDenied == null) {
			throw new TopLogicException(deniedMessage(decision));
		}
		QueryExecutor executor = _onDenied;
		if (executor == null) {
			executor = QueryExecutor.compile(withParameters(onDenied));
			_onDenied = executor;
		}
		return executor.executeIntermediate(arguments);
	}

	private ResKey deniedMessage(Decision decision) {
		String name = getConfig().getName();
		switch (decision.getOutcome()) {
			case NO_OBJECT:
				return I18NConstants.ERROR_CALL_NO_SECURITY_OBJECT__NAME.fill(name);
			case INVALID:
				return I18NConstants.ERROR_CALL_INVALID_SECURITY_OBJECT__NAME_OBJECT.fill(name,
					decision.getElement());
			case DENIED:
				return I18NConstants.ERROR_CALL_NOT_ALLOWED__NAME_OPERATION_OBJECT.fill(name,
					com.top_logic.tool.boundsec.simple.I18NConstants.COMMAND_GROUP_NAMES.key(_commandGroup.getID()),
					decision.getElement());
			case ALLOWED:
				break;
		}
		throw new IllegalArgumentException("Call is not denied: " + decision);
	}

	private QueryExecutor securityObjectExecutor() {
		QueryExecutor executor = _securityObject;
		if (executor == null) {
			executor = QueryExecutor.compile(withParameters(getConfig().getSecurityObject()));
			executor.disableSecurity();
			_securityObject = executor;
		}
		return executor;
	}

}
