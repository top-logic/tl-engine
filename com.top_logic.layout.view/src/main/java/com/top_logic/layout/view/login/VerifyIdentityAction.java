/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.login;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import com.top_logic.base.accesscontrol.IdentityVerifications;
import com.top_logic.base.accesscontrol.Login.LoginDeniedException;
import com.top_logic.base.accesscontrol.LoginCredentials;
import com.top_logic.base.accesscontrol.loginmethod.LoginMethod;
import com.top_logic.base.accesscontrol.loginmethod.LoginMethods;
import com.top_logic.base.security.device.interfaces.AuthenticationDevice;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.CommandErrors;
import com.top_logic.layout.react.control.ErrorSink;
import com.top_logic.layout.react.control.overlay.DialogManager;
import com.top_logic.layout.react.scripting.ReactWindowReplay;
import com.top_logic.layout.view.ViewMessages;
import com.top_logic.layout.view.command.Continuation;
import com.top_logic.layout.view.command.InterruptibleViewAction;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContext;

/**
 * Interruptible {@link ViewAction} that lets the chain continue only after the user has proven
 * again that they are the holder of the account the session belongs to.
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
 * The proof is given the way the session was established. A session established through an external
 * identity provider is confirmed there: the browser opens a window of its own, walks through the
 * provider's authentication once more and returns to the application, and the dialog the user left
 * behind closes itself once the confirmation has arrived. Every other session is confirmed with the
 * password of its own account, which the account's {@link AuthenticationDevice} checks.
 * </p>
 *
 * <p>
 * The account to confirm is always the session's own; nothing the view passes in can name another
 * one. On the confirmation the chain {@link Continuation#resume(Object) resumes} with its input
 * unchanged; a cancelled dialog {@link Continuation#abort() aborts} it, running the compensations of
 * the actions already executed.
 * </p>
 *
 * <p>
 * The guard fails closed. Where it cannot ask - no session account, the anonymous account, an
 * account that neither an identity provider nor an {@link AuthenticationDevice} can confirm, or a
 * headless context without a dialog manager - the chain aborts rather than passing, and a password
 * the device does not accept is a password not confirmed, whether it was mistyped or the device
 * authenticates elsewhere and accepts none.
 * </p>
 *
 * @implNote The external round trip meets the waiting chain in
 *           {@link IdentityVerifications#forCurrentSession()}: the guard registers what it expects
 *           and hands the token to
 *           {@link LoginMethod#getReauthenticationUrl(String) the login method} that established the
 *           session, and the request coming back from the provider completes the entry, which runs
 *           {@link #confirmed(ReactContext, Runnable, Object, Continuation)} on that request's
 *           thread. That method carries the rest of the chain into the window waiting for it and
 *           reports a failure of that chain there, through
 *           {@link CommandErrors#failure(Throwable, String, Class)} and
 *           {@link CommandErrors#show(ErrorSink, HandlerResult)}, exactly as a command failing in
 *           that window is reported; the request that brought the confirmation sees none of it and
 *           answers for the identity alone. The password is checked by
 *           {@link AuthenticationDevice#authentify(LoginCredentials)}, the same check a login makes,
 *           handed to {@link PasswordPromptDialogControl#openDialog} as a predicate, which keeps the
 *           dialog open on a rejected password and reports only the accepted one.
 */
@InApp
public class VerifyIdentityAction extends InterruptibleViewAction {

	/**
	 * Configuration for {@link VerifyIdentityAction}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends PolymorphicConfiguration<VerifyIdentityAction> {

		/** Configuration tag of a {@link VerifyIdentityAction}. */
		String TAG_NAME = "verify-identity";

		/** Configuration name for {@link #getTitle()}. */
		String TITLE = "title";

		/** Configuration name for {@link #getMessage()}. */
		String MESSAGE = "message";

		@Override
		@ClassDefault(VerifyIdentityAction.class)
		Class<? extends VerifyIdentityAction> getImplementationClass();

		/**
		 * The title of the dialog asking for the confirmation.
		 *
		 * <p>
		 * Defaults to a generic title when not set.
		 * </p>
		 */
		@Name(TITLE)
		ResKey getTitle();

		/**
		 * The explanation shown above the password input, saying what the password is asked for.
		 *
		 * <p>
		 * Worth setting wherever the action being guarded is not obvious from the dialog the user
		 * is standing in. Defaults to a generic request when not set. A session confirmed at an
		 * external identity provider is not asked for a password and states its own message.
		 * </p>
		 */
		@Name(MESSAGE)
		ResKey getMessage();
	}

	/**
	 * Names the operation a failure is reported for: the command chain taken up again after the
	 * identity was confirmed.
	 */
	private static final String RESUME_DESCRIPTION = "Continuing the command chain after the identity confirmation";

	private final ResKey _title;

	private final ResKey _message;

	/**
	 * Creates a new {@link VerifyIdentityAction} from configuration.
	 */
	@CalledByReflection
	public VerifyIdentityAction(InstantiationContext context, Config config) {
		_title = config.getTitle();
		_message = config.getMessage();
	}

	@Override
	public void execute(ReactContext context, Object input, Continuation continuation) {
		DialogManager dialogManager = context.getDialogManager();
		if (dialogManager == null) {
			// Without a dialog the user cannot be asked, and an unasked guard must not let the chain
			// through.
			continuation.abort();
			return;
		}

		if (verifyExternally(context, dialogManager, input, continuation)) {
			return;
		}

		Predicate<char[]> verifier = createVerifier();
		if (verifier == null) {
			// Nothing to check the password against - asking for one would promise a confirmation
			// that cannot happen.
			ViewMessages.error(context, I18NConstants.ERROR_PASSWORD_NOT_VERIFIABLE);
			continuation.abort();
			return;
		}

		openPrompt(context, dialogManager,
			title(I18NConstants.VERIFY_PASSWORD_TITLE),
			_message != null ? _message : I18NConstants.VERIFY_PASSWORD_MESSAGE,
			verifier,
			() -> continuation.resume(input),
			() -> continuation.abort());
	}

	/**
	 * Asks the identity provider that established this session to authenticate its user again, and
	 * puts the chain on hold while that happens.
	 *
	 * @return Whether the confirmation is being asked for externally. The chain is then suspended
	 *         and continues (or aborts) from the prompt this opened; a {@code false} leaves it
	 *         untouched for the password path.
	 */
	private boolean verifyExternally(ReactContext context, DialogManager dialogManager, Object input,
			Continuation continuation) {
		// The prompt does not exist yet when the confirmation is registered, and the registered
		// continuation is what closes it; it is run by a later request, never before this method has
		// returned.
		AtomicReference<Runnable> prompt = new AtomicReference<>();
		String token = registerVerification(() -> confirmed(context, prompt.get(), input, continuation));
		if (token == null) {
			return false;
		}

		for (LoginMethod method : loginMethods()) {
			String url = method.getReauthenticationUrl(token);
			if (url == null) {
				// A method that only knows how to start a fresh login, or one that did not establish
				// this session.
				continue;
			}
			prompt.set(openWaitingPrompt(context, dialogManager,
				title(I18NConstants.REAUTHENTICATION_TITLE),
				I18NConstants.REAUTHENTICATION_MESSAGE,
				// The provider's own label is the button: it names where the user signs in.
				method.getLabel(),
				url,
				() -> {
					cancelVerification(token);
					continuation.abort();
				}));
			return true;
		}

		// No provider answers for this session: the confirmation will not arrive from outside.
		cancelVerification(token);
		return false;
	}

	/**
	 * The title of the dialog: the configured one, or the given default of the path that is taken.
	 */
	private ResKey title(ResKey defaultTitle) {
		return _title != null ? _title : defaultTitle;
	}

	/**
	 * Announces to the session that its account is about to authenticate again.
	 *
	 * @param onVerified
	 *        Continues the chain. Run on the thread of the request that brings the confirmation.
	 * @return The token addressing the announcement, to be carried to the identity provider and
	 *         back, or {@code null} when there is no session account whose return could be awaited.
	 */
	protected String registerVerification(Runnable onVerified) {
		Person account = sessionAccount();
		if (account == null) {
			return null;
		}
		return IdentityVerifications.forCurrentSession().register(account, onVerified);
	}

	/**
	 * Withdraws the announcement made by {@link #registerVerification(Runnable)} under the given
	 * token, because the confirmation is no longer awaited.
	 */
	protected void cancelVerification(String token) {
		IdentityVerifications.forCurrentSession().cancel(token);
	}

	/**
	 * The login methods asked whether they can authenticate the user of this session again.
	 *
	 * <p>
	 * The methods the application offers, in configured order; the first one that answers a URL is
	 * the one the user is sent to.
	 * </p>
	 */
	protected List<? extends LoginMethod> loginMethods() {
		return LoginMethods.all();
	}

	/**
	 * The account of the running session, or {@code null} for a session that belongs to no account.
	 *
	 * <p>
	 * Read from the session rather than from the chain, so that what is confirmed is always the
	 * identity of the user at the keyboard.
	 * </p>
	 */
	protected Person sessionAccount() {
		if (TLContext.isAnonymous()) {
			return null;
		}
		return TLContext.currentUser();
	}

	/**
	 * The check a password entered in the prompt has to pass, or {@code null} when the session's
	 * account has no password that can be checked.
	 *
	 * @return The check, called with the characters the user entered; {@code null} to abort the
	 *         chain as unverifiable.
	 */
	protected Predicate<char[]> createVerifier() {
		Person account = sessionAccount();
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
	 * Opens the dialog that sends the user to the identity provider and waits for them to come
	 * back.
	 *
	 * @param context
	 *        The React context.
	 * @param dialogManager
	 *        The dialog manager to open the dialog with.
	 * @param title
	 *        The dialog title, the configured one or this action's own.
	 * @param message
	 *        The explanation of what the sign-in confirms and how the dialog ends.
	 * @param buttonLabel
	 *        The label of the button opening the sign-in, the login method's own.
	 * @param reauthenticationUrl
	 *        Where the provider authenticates the user again, from
	 *        {@link LoginMethod#getReauthenticationUrl(String)}.
	 * @param onCancel
	 *        Withdraws the awaited confirmation and aborts the chain.
	 * @return Closes the prompt once the confirmation has arrived, see
	 *         {@link ReauthenticationPromptDialogControl#openDialog}.
	 */
	protected Runnable openWaitingPrompt(ReactContext context, DialogManager dialogManager, ResKey title,
			ResKey message, ResKey buttonLabel, String reauthenticationUrl, Runnable onCancel) {
		Resources resources = Resources.getInstance();
		return ReauthenticationPromptDialogControl.openDialog(context, dialogManager,
			resources.getString(title), resources.getString(message), resources.getString(buttonLabel),
			reauthenticationUrl, onCancel);
	}

	/**
	 * Takes the chain up again in the window it was suspended in, once the identity provider has
	 * confirmed the user.
	 *
	 * <p>
	 * Runs on the thread of the request that came back from the provider - in another browser window
	 * of the same session - so the dialog that is closed and everything the resumed chain displays
	 * belong to a window this thread is not serving.
	 * </p>
	 *
	 * <p>
	 * What the resumed chain throws is shown in that window as the failure of a command is shown,
	 * and nothing leaves this method: the identity was confirmed whatever the chain then makes of
	 * it, and the request bringing the confirmation reports on the confirmation only.
	 * </p>
	 *
	 * @param context
	 *        The React context of the window the chain was suspended in.
	 * @param closePrompt
	 *        Closes the waiting dialog of that window, as {@link #openWaitingPrompt} returned it.
	 * @param input
	 *        The value the chain continues with, unchanged.
	 * @param continuation
	 *        The suspended chain.
	 */
	protected void confirmed(ReactContext context, Runnable closePrompt, Object input, Continuation continuation) {
		try {
			runInWindow(context, () -> {
				if (closePrompt != null) {
					closePrompt.run();
				}
				resume(context, input, continuation);
			});
		} catch (Throwable ex) {
			// The request running this answers for the identity, not for the chain: whatever could
			// not be carried into the window is written to the log and left there.
			Logger.error("The confirmed identity could not be carried into window '" + context.getWindowName()
				+ "'.", ex, VerifyIdentityAction.class);
		}
	}

	/**
	 * Continues the chain, reporting what it throws in the window the chain belongs to.
	 *
	 * <p>
	 * The failure travels the way a failed command travels: an internal error is logged as one and
	 * shown generically, a user-level failure carries its own message into the window's snackbar,
	 * see {@link CommandErrors#failure(Throwable, String, Class)}.
	 * </p>
	 */
	private void resume(ReactContext context, Object input, Continuation continuation) {
		try {
			continuation.resume(input);
		} catch (Throwable ex) {
			HandlerResult failure = CommandErrors.failure(ex, RESUME_DESCRIPTION, VerifyIdentityAction.class);
			CommandErrors.show(context.getErrorSink(), failure);
		}
	}

	/**
	 * Runs the given action in the window of the given context, from whatever thread is calling.
	 *
	 * @implNote The step that needs a browser: the action is run in the window's sub-session and as
	 *           one interaction of the session, so that its display updates reach that window's
	 *           page and its texts are resolved in that window's language. A window whose page is
	 *           gone is not acted on.
	 */
	protected void runInWindow(ReactContext context, Runnable action) {
		String windowName = context.getWindowName();
		if (!ReactWindowReplay.inWindow(context.getWindowRegistry(), windowName, action)) {
			Logger.warn("Window '" + windowName + "' is gone, the command chain waiting in it does not continue.",
				VerifyIdentityAction.class);
		}
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
