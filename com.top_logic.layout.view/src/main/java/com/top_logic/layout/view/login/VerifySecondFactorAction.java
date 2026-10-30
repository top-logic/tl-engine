/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.login;

import com.top_logic.base.accesscontrol.LoginFailuresModule;
import com.top_logic.base.accesscontrol.RetryDeniedException;
import com.top_logic.base.security.util.Password;
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
import com.top_logic.layout.view.command.Continuation;
import com.top_logic.layout.view.command.InterruptibleViewAction;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.layout.view.login.PasswordPromptDialogControl.EntryKind;
import com.top_logic.layout.view.login.PasswordPromptDialogControl.Verifier;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContext;

/**
 * Interruptible {@link ViewAction} that lets the chain continue only after the user has entered a
 * current one-time code of the second factor their account is set up with.
 *
 * <p>
 * The guard for a step that changes or removes the second factor itself - setting up another
 * authenticator app, switching multi-factor authentication off: whoever takes that step must hold
 * the device the second factor lives on, not only a session someone left logged in. Placed in a
 * command chain like {@code <confirm>}, before the action it protects, so that no transaction is
 * held open while the dialog is on screen.
 * </p>
 *
 * <p>
 * The account whose code is asked for is always the session's own; nothing the view passes in can
 * name another one. The code is checked against the secret stored on that account. On a valid code
 * the chain {@link Continuation#resume(Object) resumes} with its input unchanged; a cancelled dialog
 * {@link Continuation#abort() aborts} it, running the compensations of the actions already
 * executed. A wrong code keeps the dialog open with the reason under the input.
 * </p>
 *
 * <p>
 * An account without a stored secret has no second factor to prove, and the chain continues at
 * once: there is no device whose holder could be told apart from the session, and what the guard
 * protects - the stored secret - does not exist. Setting up a first second factor is protected by
 * the setup itself, which stores a secret only once a code generated from it has been entered. A
 * view that wants the person at the keyboard confirmed in that case as well combines this guard
 * with {@link VerifyIdentityAction}.
 * </p>
 *
 * <p>
 * Otherwise the guard fails closed. Where it cannot ask - no session account, the anonymous account,
 * or a headless context without a dialog manager - the chain aborts rather than passing.
 * </p>
 *
 * @implNote The code is checked by {@link MfaSupport#isValidCode(Password, String)}, the check of
 *           the one-time code at the login, guarded by
 *           {@link LoginFailuresModule#attempt(String, com.top_logic.basic.util.ResKey1, LoginFailuresModule.Attempt)}
 *           under the key of the secret ({@link LoginFailuresModule#secretKey(Password)}), so that
 *           codes guessed in this prompt and at the login count together. After repeated wrong
 *           codes, the prompt refuses further codes for a growing time without checking them, and
 *           says so under the input. The prompt is a {@link PasswordPromptDialogControl} with a
 *           plain {@link EntryKind#ONE_TIME_CODE code input}.
 */
@InApp
public class VerifySecondFactorAction extends InterruptibleViewAction {

	/**
	 * Configuration for {@link VerifySecondFactorAction}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends PolymorphicConfiguration<VerifySecondFactorAction> {

		/** Configuration tag of a {@link VerifySecondFactorAction}. */
		String TAG_NAME = "verify-second-factor";

		/** Configuration name for {@link #getTitle()}. */
		String TITLE = "title";

		/** Configuration name for {@link #getMessage()}. */
		String MESSAGE = "message";

		@Override
		@ClassDefault(VerifySecondFactorAction.class)
		Class<? extends VerifySecondFactorAction> getImplementationClass();

		/**
		 * The title of the dialog asking for the code.
		 *
		 * <p>
		 * Defaults to a generic title when not set.
		 * </p>
		 */
		@Name(TITLE)
		ResKey getTitle();

		/**
		 * The explanation shown above the code input, saying what the code is asked for.
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
	 * Creates a new {@link VerifySecondFactorAction} from configuration.
	 */
	@CalledByReflection
	public VerifySecondFactorAction(InstantiationContext context, Config config) {
		_title = config.getTitle();
		_message = config.getMessage();
	}

	@Override
	public void execute(ReactContext context, Object input, Continuation continuation) {
		Person account = sessionAccount();
		if (account == null) {
			// No account to ask a code of: an unasked guard must not let the chain through.
			continuation.abort();
			return;
		}

		Password secret = account.getMFASecret();
		if (secret == null) {
			// No second factor to prove.
			continuation.resume(input);
			return;
		}

		DialogManager dialogManager = context.getDialogManager();
		if (dialogManager == null) {
			// Without a dialog the user cannot be asked.
			continuation.abort();
			return;
		}

		openPrompt(context, dialogManager,
			_title != null ? _title : I18NConstants.VERIFY_SECOND_FACTOR_TITLE,
			_message != null ? _message : I18NConstants.VERIFY_SECOND_FACTOR_MESSAGE,
			code -> verify(secret, code),
			() -> continuation.resume(input),
			() -> continuation.abort());
	}

	/**
	 * The account of the running session, or {@code null} for a session that belongs to no account.
	 *
	 * <p>
	 * Read from the session rather than from the chain, so that the code asked for is always one of
	 * the user at the keyboard.
	 * </p>
	 */
	protected Person sessionAccount() {
		if (TLContext.isAnonymous()) {
			return null;
		}
		return TLContext.currentUser();
	}

	/**
	 * Checks a code entered in the prompt.
	 *
	 * <p>
	 * An empty code is rejected without counting as an attempt. Every other code is checked under
	 * the back-off of the secret: while it is backing off, the code is refused with the time to wait
	 * and not checked.
	 * </p>
	 *
	 * @return {@code null} for an accepted code, otherwise the reason of the rejection.
	 */
	private ResKey verify(Password secret, char[] entered) {
		if (entered == null || entered.length == 0) {
			return I18NConstants.MFA_INVALID_CODE;
		}
		String code = new String(entered);
		try {
			boolean valid = LoginFailuresModule.getInstance().attempt(LoginFailuresModule.secretKey(secret),
				com.top_logic.layout.component.configuration.I18NConstants.CHECK_OTP_TOO_MANY_ATTEMPTS__TIMEOUT,
				() -> isValidCode(secret, code));
			return valid ? null : I18NConstants.MFA_INVALID_CODE;
		} catch (RetryDeniedException ex) {
			return ex.getErrorKey();
		}
	}

	/**
	 * Whether the given code is a current one-time code of the given secret.
	 *
	 * @see MfaSupport#isValidCode(Password, String)
	 */
	protected boolean isValidCode(Password secret, String code) {
		return MfaSupport.isValidCode(secret, code);
	}

	/**
	 * Opens the dialog asking for the code.
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
	 *        The check of the entered code against the stored secret, under the back-off of that
	 *        secret.
	 * @param onVerified
	 *        Continues the chain.
	 * @param onCancel
	 *        Aborts the chain.
	 */
	protected void openPrompt(ReactContext context, DialogManager dialogManager, ResKey title, ResKey message,
			Verifier verifier, Runnable onVerified, Runnable onCancel) {
		Resources resources = Resources.getInstance();
		PasswordPromptDialogControl.openDialog(context, dialogManager,
			resources.getString(title), resources.getString(message),
			resources.getString(I18NConstants.VERIFY_SECOND_FACTOR_FIELD_LABEL),
			EntryKind.ONE_TIME_CODE, verifier, onVerified, onCancel);
	}

	/**
	 * Opens the dialog asking for the code.
	 */
	@Override
	public boolean changesViewState() {
		return true;
	}
}
