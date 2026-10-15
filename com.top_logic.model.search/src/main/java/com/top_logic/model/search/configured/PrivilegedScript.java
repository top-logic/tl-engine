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
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.form.values.edit.annotation.PropertyEditor;
import com.top_logic.layout.form.values.edit.editor.PlainEditor;
import com.top_logic.model.search.constraint.CheckExprWithAdditionalParams;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.ui.ScriptContextVariables;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.CommandGroupReference;

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
		 * may discover. A result that is no single object denies the call.
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

}
