/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * {@link ViewCommandConfirmation} that decides - and phrases - the confirmation via a TL-Script
 * expression.
 *
 * <p>
 * The expression is evaluated like {@link ExecuteScriptAction}: the current values of the configured
 * {@link Config#getInputs() input} channels are passed as leading positional arguments (in
 * declaration order), followed by the command's current input value as the last argument. This lets
 * a confirmation inspect more than the command input alone (e.g. a container channel plus the object
 * about to be created).
 * </p>
 *
 * <p>
 * The expression returns the confirmation message - typically an i18n literal such as
 * {@code #('Replace?'@en, 'Ersetzen?'@de)} - to ask before executing, or {@code null} to proceed
 * without asking. This makes the confirmation <em>conditional</em>: e.g. ask only when an element
 * with the same name already exists.
 * </p>
 */
public class ScriptViewConfirmation implements ViewCommandConfirmation {

	/**
	 * Configuration for {@link ScriptViewConfirmation}.
	 */
	@TagName("script-confirmation")
	public interface Config extends ViewCommandConfirmation.Config {

		/** Configuration name for {@link #getInputs()}. */
		String INPUTS = "inputs";

		@Override
		@ClassDefault(ScriptViewConfirmation.class)
		Class<? extends ViewCommandConfirmation> getImplementationClass();

		/**
		 * TL-Script expression producing the confirmation message, or {@code null}/empty to skip the
		 * confirmation.
		 *
		 * <p>
		 * Called with the {@link #getInputs() input} channel values as leading positional arguments
		 * (in declaration order), followed by the command's current input value as the last argument.
		 * With no inputs, called with the command's current input value as the single argument.
		 * </p>
		 */
		@Name("expr")
		@Mandatory
		Expr getExpr();

		/**
		 * References to {@link ViewChannel}s whose current values become leading positional arguments
		 * to {@link #getExpr()} (before the command's current input value).
		 */
		@Name(INPUTS)
		@ListBinding(format = ChannelRefFormat.class, tag = "input", attribute = "channel")
		List<ChannelRef> getInputs();
	}

	private final QueryExecutor _executor;

	private final List<ChannelRef> _inputRefs;

	/**
	 * Creates a new {@link ScriptViewConfirmation} from configuration.
	 */
	@CalledByReflection
	public ScriptViewConfirmation(InstantiationContext context, Config config) {
		_executor = QueryExecutor.compile(config.getExpr());
		_inputRefs = config.getInputs();
	}

	@Override
	public ResKey getConfirmation(ReactContext context, ResKey commandLabel, Object input) {
		Object result;
		if (_inputRefs.isEmpty()) {
			result = _executor.execute(input);
		} else {
			ViewContext viewContext = (ViewContext) context;
			Object[] args = new Object[_inputRefs.size() + 1];
			int i = 0;
			for (ChannelRef ref : _inputRefs) {
				ViewChannel channel = viewContext.resolveChannel(ref);
				args[i++] = channel.get();
			}
			args[i] = input;
			result = _executor.execute(args);
		}

		if (result == null) {
			return null;
		}
		if (result instanceof ResKey) {
			return (ResKey) result;
		}
		String text = result.toString();
		return text.isEmpty() ? null : ResKey.text(text);
	}
}
