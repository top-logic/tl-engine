/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.overlay.ConfirmDialogControl;
import com.top_logic.layout.react.control.overlay.DialogManager;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.util.Resources;

/**
 * Interruptible {@link ViewAction} that asks the user to confirm before the chain continues.
 *
 * <p>
 * Placed as a guard in a command chain - typically <em>after</em> {@code <store-form-state/>} (so it
 * sees the validated, applied form values) and <em>before</em> {@code <with-transaction>} (so no
 * transaction is held open during the dialog). The message is produced by a TL-Script expression
 * evaluated like {@link ExecuteScriptAction}: the configured {@link Config#getInputs() input}
 * channel values are passed as leading positional arguments, followed by the chain's current input
 * value as the last argument. The expression returns the confirmation message (typically an i18n
 * literal such as {@code #('Replace?'@en, 'Ersetzen?'@de)}) to prompt, or {@code null}/empty to
 * proceed without asking - making the guard <em>conditional</em>.
 * </p>
 *
 * <p>
 * On confirm the chain {@link Continuation#resume(Object) resumes} (the input passes through
 * unchanged); on decline it {@link Continuation#abort() aborts}, running the compensations of any
 * already-executed actions.
 * </p>
 */
public class ConfirmAction extends InterruptibleViewAction {

	/**
	 * Configuration for {@link ConfirmAction}.
	 */
	@TagName("confirm")
	public interface Config extends PolymorphicConfiguration<ConfirmAction> {

		/** Configuration name for {@link #getInputs()}. */
		String INPUTS = "inputs";

		@Override
		@ClassDefault(ConfirmAction.class)
		Class<? extends ConfirmAction> getImplementationClass();

		/**
		 * TL-Script expression producing the confirmation message, or {@code null}/empty to skip the
		 * confirmation (proceed without asking).
		 *
		 * <p>
		 * Called with the {@link #getInputs() input} channel values as leading positional arguments
		 * (in declaration order), followed by the chain's current input value as the last argument.
		 * </p>
		 */
		@Name("expr")
		@Mandatory
		Expr getExpr();

		/**
		 * References to {@link ViewChannel}s whose current values become leading positional arguments
		 * to {@link #getExpr()} (before the chain's current input value).
		 */
		@Name(INPUTS)
		@ListBinding(format = ChannelRefFormat.class, tag = "input", attribute = "channel")
		List<ChannelRef> getInputs();

		/**
		 * The confirmation dialog title.
		 *
		 * <p>
		 * Defaults to a generic confirmation title when not set.
		 * </p>
		 */
		@Name("title")
		ResKey getTitle();

		/**
		 * The label of the affirmative (confirm) button.
		 *
		 * <p>
		 * Defaults to the standard "Yes" label when not set.
		 * </p>
		 */
		@Name("confirm-label")
		ResKey getConfirmLabel();

		/**
		 * The label of the declining (cancel) button.
		 *
		 * <p>
		 * Defaults to the standard "No" label when not set.
		 * </p>
		 */
		@Name("cancel-label")
		ResKey getCancelLabel();
	}

	private final QueryExecutor _expr;

	private final List<ChannelRef> _inputRefs;

	private final ResKey _title;

	private final ResKey _confirmLabel;

	private final ResKey _cancelLabel;

	/**
	 * Creates a new {@link ConfirmAction} from configuration.
	 */
	@CalledByReflection
	public ConfirmAction(InstantiationContext context, Config config) {
		_expr = QueryExecutor.compile(config.getExpr());
		_inputRefs = config.getInputs();
		_title = config.getTitle();
		_confirmLabel = config.getConfirmLabel();
		_cancelLabel = config.getCancelLabel();
	}

	@Override
	public void execute(ReactContext context, Object input, Continuation continuation) {
		ResKey message = evaluateMessage(context, input);
		if (message == null) {
			continuation.resume(input);
			return;
		}

		DialogManager dialogManager = context.getDialogManager();
		if (dialogManager == null) {
			// No dialog possible (e.g. headless) - proceed rather than dead-ending the chain.
			continuation.resume(input);
			return;
		}

		Resources resources = Resources.getInstance();
		String title = resources.getString(_title != null ? _title : I18NConstants.CONFIRM_TITLE);
		String confirmLabel = _confirmLabel == null ? null : resources.getString(_confirmLabel);
		String cancelLabel = _cancelLabel == null ? null : resources.getString(_cancelLabel);
		ConfirmDialogControl.openDialog(context, dialogManager,
			title,
			resources.getString(message),
			confirmLabel,
			cancelLabel,
			() -> continuation.resume(input),
			() -> continuation.abort());
	}

	private ResKey evaluateMessage(ReactContext context, Object input) {
		Object result;
		if (_inputRefs.isEmpty()) {
			result = _expr.execute(input);
		} else {
			ViewContext viewContext = (ViewContext) context;
			Object[] args = new Object[_inputRefs.size() + 1];
			int i = 0;
			for (ChannelRef ref : _inputRefs) {
				ViewChannel channel = viewContext.resolveChannel(ref);
				args[i++] = channel.get();
			}
			args[i] = input;
			result = _expr.execute(args);
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
