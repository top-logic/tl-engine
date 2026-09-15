/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.login;

import java.util.function.Predicate;

import com.top_logic.base.accesscontrol.Login.LoginDeniedException;
import com.top_logic.base.accesscontrol.LoginCredentials;
import com.top_logic.base.security.device.interfaces.AuthenticationDevice;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.overlay.DialogManager;
import com.top_logic.layout.view.ViewMessages;
import com.top_logic.layout.view.command.Continuation;
import com.top_logic.layout.view.command.InterruptibleViewAction;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContext;

/**
 * Interruptible {@link ViewAction} that lets the chain continue only after the user has re-entered
 * the password of the account they are logged in with.
 *
 * <p>
 * The guard for a step whose consequences outlive the session that took it - handing a deputy the
 * right to act in the user's name, changing the password itself - where the question is not what
 * the session may do but whether the person at the keyboard is the account holder. Placed in a
 * command chain like {@code <confirm>}, typically after {@code <store-form-state/>} and before the
 * action it protects, so that no transaction is held open while the dialog is on screen.
 * </p>
 *
 * <p>
 * The account whose password is asked for is always the session's own; nothing the view passes in
 * can name another one. On the entered password the chain
 * {@link Continuation#resume(Object) resumes} with its input unchanged; a cancelled dialog
 * {@link Continuation#abort() aborts} it, running the compensations of the actions already
 * executed.
 * </p>
 *
 * <p>
 * The guard fails closed. Where it cannot ask - no session account, the anonymous account, an
 * account whose {@link AuthenticationDevice} is unknown, or a headless context without a dialog
 * manager - the chain aborts rather than passing, and a password the device does not accept is a
 * password not confirmed, whether it was mistyped or the device authenticates elsewhere and accepts
 * none.
 * </p>
 *
 * @implNote The password is checked by the account's own
 *           {@link AuthenticationDevice#authentify(LoginCredentials)}, the same check a login
 *           makes, so an application that authenticates against a directory verifies against that
 *           directory here as well. The check is handed to
 *           {@link PasswordPromptDialogControl#openDialog} as a predicate, which keeps the dialog
 *           open on a rejected password and reports only the accepted one.
 */
@InApp
public class VerifyPasswordAction extends InterruptibleViewAction {

	/**
	 * Configuration for {@link VerifyPasswordAction}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends PolymorphicConfiguration<VerifyPasswordAction> {

		/** Configuration tag of a {@link VerifyPasswordAction}. */
		String TAG_NAME = "verify-password";

		/** Configuration name for {@link #getTitle()}. */
		String TITLE = "title";

		/** Configuration name for {@link #getMessage()}. */
		String MESSAGE = "message";

		@Override
		@ClassDefault(VerifyPasswordAction.class)
		Class<? extends VerifyPasswordAction> getImplementationClass();

		/**
		 * The title of the dialog asking for the password.
		 *
		 * <p>
		 * Defaults to a generic title when not set.
		 * </p>
		 */
		@Name(TITLE)
		ResKey getTitle();

		/**
		 * The explanation shown above the input, saying what the password is asked for.
		 *
		 * <p>
		 * Worth setting wherever the action being guarded is not obvious from the dialog the user
		 * is standing in. Defaults to a generic request when not set.
		 * </p>
		 */
		@Name(MESSAGE)
		ResKey getMessage();
	}

	private final ResKey _title;

	private final ResKey _message;

	/**
	 * Creates a new {@link VerifyPasswordAction} from configuration.
	 */
	@CalledByReflection
	public VerifyPasswordAction(InstantiationContext context, Config config) {
		_title = config.getTitle();
		_message = config.getMessage();
	}

	@Override
	public void execute(ReactContext context, Object input, Continuation continuation) {
		Predicate<char[]> verifier = createVerifier();
		if (verifier == null) {
			// Nothing to check the password against - asking for one would promise a confirmation
			// that cannot happen.
			ViewMessages.error(context, I18NConstants.ERROR_PASSWORD_NOT_VERIFIABLE);
			continuation.abort();
			return;
		}

		DialogManager dialogManager = context.getDialogManager();
		if (dialogManager == null) {
			// Without a dialog the user cannot be asked, and an unasked guard must not let the chain
			// through.
			continuation.abort();
			return;
		}

		openPrompt(context, dialogManager,
			_title != null ? _title : I18NConstants.VERIFY_PASSWORD_TITLE,
			_message != null ? _message : I18NConstants.VERIFY_PASSWORD_MESSAGE,
			verifier,
			() -> continuation.resume(input),
			() -> continuation.abort());
	}

	/**
	 * The check a password entered in the prompt has to pass, or {@code null} when the session's
	 * account has no password that can be checked.
	 *
	 * <p>
	 * Reads the account from the session rather than from the chain, so that what is confirmed is
	 * always the identity of the user at the keyboard.
	 * </p>
	 *
	 * @return The check, called with the characters the user entered; {@code null} to abort the
	 *         chain as unverifiable.
	 */
	protected Predicate<char[]> createVerifier() {
		if (TLContext.isAnonymous()) {
			return null;
		}
		Person account = TLContext.currentUser();
		if (account == null) {
			return null;
		}
		AuthenticationDevice device = account.getAuthenticationDevice();
		if (device == null) {
			return null;
		}
		return password -> authenticates(device, account, password);
	}

	/**
	 * Opens the dialog asking for the password.
	 *
	 * @param context
	 *        The React context.
	 * @param dialogManager
	 *        The dialog manager to open the dialog with.
	 * @param title
	 *        The dialog title, the configured one or this action's own.
	 * @param message
	 *        The explanation shown above the input, the configured one or this action's own.
	 * @param verifier
	 *        The check produced by {@link #createVerifier()}.
	 * @param onVerified
	 *        Continues the chain.
	 * @param onCancel
	 *        Aborts the chain.
	 */
	protected void openPrompt(ReactContext context, DialogManager dialogManager, ResKey title, ResKey message,
			Predicate<char[]> verifier, Runnable onVerified, Runnable onCancel) {
		Resources resources = Resources.getInstance();
		PasswordPromptDialogControl.openDialog(context, dialogManager,
			resources.getString(title), resources.getString(message),
			resources.getString(I18NConstants.VERIFY_PASSWORD_FIELD_LABEL),
			verifier, onVerified, onCancel);
	}

	/**
	 * Whether the given password authenticates the given account against its device.
	 *
	 * <p>
	 * An empty password is rejected without troubling the device, and a device that refuses to look
	 * at the account at all counts as a rejection: every path but an accepted password ends in
	 * {@code false}.
	 * </p>
	 */
	private static boolean authenticates(AuthenticationDevice device, Person account, char[] password) {
		if (password == null || password.length == 0) {
			return false;
		}
		try {
			return device.authentify(LoginCredentials.fromUserAndPassword(account, password));
		} catch (LoginDeniedException ex) {
			return false;
		}
	}

}
