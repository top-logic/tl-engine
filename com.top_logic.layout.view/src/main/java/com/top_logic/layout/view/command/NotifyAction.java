/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.overlay.DialogManager;
import com.top_logic.layout.react.control.overlay.MessageDialogControl;
import com.top_logic.layout.view.ViewMessages;
import com.top_logic.layout.view.channel.Inputs;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.util.Resources;

/**
 * Action that tells the user something - why what they asked for was refused, or what has happened
 * - and either lets the chain continue or ends it after the notice.
 *
 * <p>
 * The message is produced by a TL-Script expression evaluated like {@link ExecuteScriptAction}: the
 * configured input channel values are passed as leading positional arguments, followed by the
 * chain's current input value as the last argument. The expression returns the message (typically
 * an i18n literal such as {@code #('Nothing selected.'@en, 'Nichts ausgewählt.'@de)}), or
 * {@code null}/empty when there is nothing to say - which makes the notice
 * <em>conditional</em> and passes the chain's value on untouched.
 * </p>
 *
 * <p>
 * The notice appears as a passing message beside the user's work, or as a dialog they acknowledge;
 * its severity decides how a passing message is marked and which title a dialog carries. A dialog
 * holds the chain until it is closed.
 * </p>
 *
 * @implNote A dialog suspends the chain: {@link Continuation#resume(Object)} or
 *           {@link Continuation#abort()} is called from the dialog's close handler, so the outcome
 *           is decided exactly once, whichever way the dialog was left. Without a
 *           {@link ReactContext#getDialogManager() dialog manager} - a chain running headless -
 *           there is nothing to open a dialog in, so the message is reported the passing way and
 *           the chain continues synchronously.
 */
@InApp
public class NotifyAction extends InterruptibleViewAction {

	/**
	 * How serious what the user is told is.
	 */
	public enum Kind implements ExternallyNamed {

		/** Something has happened that the user asked for. */
		INFO("info"),

		/** Something the user should be aware of, without it having stopped them. */
		WARNING("warning"),

		/** Something the user asked for did not happen. */
		ERROR("error");

		private final String _externalName;

		private Kind(String externalName) {
			_externalName = externalName;
		}

		@Override
		public String getExternalName() {
			return _externalName;
		}

		/**
		 * Reports the given message beside the user's work, marked with this severity.
		 */
		void report(ReactContext context, ResKey message) {
			switch (this) {
				case INFO -> ViewMessages.info(context, message);
				case WARNING -> ViewMessages.warning(context, message);
				case ERROR -> ViewMessages.error(context, message);
			}
		}

		/**
		 * The title a dialog of this severity carries when none is configured.
		 */
		ResKey defaultTitle() {
			return switch (this) {
				case INFO -> I18NConstants.NOTIFY_TITLE_INFO;
				case WARNING -> I18NConstants.NOTIFY_TITLE_WARNING;
				case ERROR -> I18NConstants.NOTIFY_TITLE_ERROR;
			};
		}
	}

	/**
	 * Where the user reads what they are told.
	 */
	public enum Display implements ExternallyNamed {

		/**
		 * A message passing by beside the user's work, which they need not act on.
		 */
		SNACKBAR("snackbar"),

		/**
		 * A dialog that stays until the user acknowledges it, and holds the chain while it is open.
		 */
		DIALOG("dialog");

		private final String _externalName;

		private Display(String externalName) {
			_externalName = externalName;
		}

		@Override
		public String getExternalName() {
			return _externalName;
		}
	}

	/**
	 * Configuration for {@link NotifyAction}.
	 */
	@TagName("notify")
	public interface Config extends PolymorphicConfiguration<NotifyAction>, Inputs {

		@Override
		@ClassDefault(NotifyAction.class)
		Class<? extends NotifyAction> getImplementationClass();

		/**
		 * TL-Script expression producing the message, or {@code null}/empty to say nothing (the
		 * chain continues untouched).
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
		 * How serious the message is.
		 *
		 * <p>
		 * Decides how a {@link Display#SNACKBAR} message is marked, and which title a
		 * {@link Display#DIALOG} carries when none is configured.
		 * </p>
		 */
		@Name("kind")
		Kind getKind();

		/**
		 * Where the user reads the message.
		 */
		@Name("display")
		Display getDisplay();

		/**
		 * Whether the chain ends with the notice instead of continuing with its value unchanged.
		 *
		 * <p>
		 * An ending chain runs the compensations of the actions before it and reports nothing
		 * beyond the message itself. A message there was nothing to say ends no chain.
		 * </p>
		 */
		@Name("stop")
		boolean isStop();

		/**
		 * The title of a {@link Display#DIALOG}.
		 *
		 * <p>
		 * Defaults to a title matching the {@link #getKind() severity} when not set.
		 * </p>
		 */
		@Name("title")
		ResKey getTitle();
	}

	private final ActionScript _expr;

	private final Kind _kind;

	private final Display _display;

	private final boolean _stop;

	private final ResKey _title;

	/**
	 * Creates a new {@link NotifyAction} from configuration.
	 */
	@CalledByReflection
	public NotifyAction(InstantiationContext context, Config config) {
		this(ActionScript.compile(config.getExpr(), config.getInputs()), config.getKind(), config.getDisplay(),
			config.isStop(), config.getTitle());
	}

	/**
	 * Creates a new {@link NotifyAction}.
	 *
	 * @param expr
	 *        The function producing the message, or {@code null}/empty for nothing to say.
	 * @param kind
	 *        How serious the message is.
	 * @param display
	 *        Where the user reads the message.
	 * @param stop
	 *        Whether the chain ends with the notice.
	 * @param title
	 *        The dialog title, or {@code null} for the one matching the severity.
	 */
	public NotifyAction(ActionScript expr, Kind kind, Display display, boolean stop, ResKey title) {
		_expr = expr;
		_kind = kind;
		_display = display;
		_stop = stop;
		_title = title;
	}

	@Override
	public void execute(ReactContext context, Object input, Continuation continuation) {
		ResKey message = _expr.message(context, input);
		if (message == null) {
			// Nothing to say leaves the chain as it was, whether or not it would have ended here.
			continuation.resume(input);
			return;
		}

		if (_display == Display.DIALOG) {
			DialogManager dialogManager = context.getDialogManager();
			if (dialogManager != null) {
				openDialog(context, dialogManager, message, input, continuation);
				return;
			}
			// No dialog possible (e.g. headless) - the message is still worth showing the other way.
		}

		_kind.report(context, message);
		proceed(input, continuation);
	}

	private void openDialog(ReactContext context, DialogManager dialogManager, ResKey message, Object input,
			Continuation continuation) {
		Resources resources = Resources.getInstance();
		String title = resources.getString(_title != null ? _title : _kind.defaultTitle());
		MessageDialogControl.openDialog(context, dialogManager, title, resources.getString(message), null,
			() -> proceed(input, continuation));
	}

	private void proceed(Object input, Continuation continuation) {
		if (_stop) {
			continuation.abort();
		} else {
			continuation.resume(input);
		}
	}
}
