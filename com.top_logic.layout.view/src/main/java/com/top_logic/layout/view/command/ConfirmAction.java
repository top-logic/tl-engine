/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.overlay.ConfirmDialogControl;
import com.top_logic.layout.react.control.overlay.DialogManager;
import com.top_logic.layout.view.channel.NamedInputs;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.ui.ScriptContextVariables;
import com.top_logic.util.Resources;

/**
 * Interruptible {@link ViewAction} that asks the user to confirm before the chain continues.
 *
 * <p>
 * Placed as a guard in a command chain - typically <em>after</em> {@code <store-form-state/>} (so it
 * sees the validated, applied form values) and <em>before</em> {@code <with-transaction>} (so no
 * transaction is held open during the dialog). The message is produced by a TL-Script expression
 * evaluated like {@link ExecuteScriptAction}: it uses the arguments of the chain and the values of
 * the configured {@link Config#getInputs() inputs} as variables. The expression returns the confirmation message (typically an i18n
 * literal such as {@code #('Replace?'@en, 'Ersetzen?'@de)}) to prompt, or {@code null}/empty to
 * proceed without asking - making the guard <em>conditional</em>.
 * </p>
 *
 * <p>
 * On confirm the chain {@link Continuation#resume(Object) resumes}; on decline it {@link Continuation#abort() aborts}, running the compensations of any
 * already-executed actions.
 * </p>
 *
 * <p>
 * The affirmative answer is drawn in the {@link Continuation#tone() tone of what follows}: before a
 * deletion it reads as destructive, and it stays the dialog's default on Enter.
 * </p>
 */
@InApp
public class ConfirmAction extends ArgumentsViewAction {

	/**
	 * Configuration for {@link ConfirmAction}.
	 */
	@TagName("confirm")
	public interface Config extends PolymorphicConfiguration<ConfirmAction>, NamedInputs {

		@Override
		@ClassDefault(ConfirmAction.class)
		Class<? extends ConfirmAction> getImplementationClass();

		/**
		 * TL-Script expression producing the confirmation message, or {@code null}/empty to skip the
		 * confirmation (proceed without asking).
		 *
		 * <p>
		 * Uses the arguments of the chain and the values of the {@link #getInputs() inputs} as
		 * variables named after them, without declaring them.
		 * </p>
		 */
		@Name("expr")
		@Mandatory
		@ScriptContextVariables(ActionScriptVariables.class)
		Expr getExpr();

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

	private final ActionScript _expr;

	private final ResKey _title;

	private final ResKey _confirmLabel;

	private final ResKey _cancelLabel;

	/**
	 * Creates a new {@link ConfirmAction} from configuration.
	 */
	@CalledByReflection
	public ConfirmAction(InstantiationContext context, Config config) {
		_expr = ActionScript.compile(config.getExpr(), ActionScope.of(context), config.getInputs());
		_title = config.getTitle();
		_confirmLabel = config.getConfirmLabel();
		_cancelLabel = config.getCancelLabel();
	}

	@Override
	public void execute(ReactContext context, ActionArguments arguments, Continuation continuation) {
		Object input = arguments.value();
		ResKey message = _expr.message(context, arguments);
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
			() -> continuation.abort(),
			continuation.tone());
	}

	/**
	 * Asks the user in a dialog.
	 */
	@Override
	public boolean changesViewState() {
		return true;
	}
}
