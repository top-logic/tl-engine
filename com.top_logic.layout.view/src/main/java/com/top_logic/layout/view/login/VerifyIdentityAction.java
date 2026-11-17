/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.login;

import java.util.List;
import java.util.function.Predicate;

import jakarta.servlet.ServletRequest;

import com.top_logic.base.accesscontrol.IdentityVerificationHandler;
import com.top_logic.base.accesscontrol.IdentityVerifications;
import com.top_logic.base.accesscontrol.Login.LoginDeniedException;
import com.top_logic.base.accesscontrol.LoginCredentials;
import com.top_logic.base.accesscontrol.LoginFailuresModule;
import com.top_logic.base.accesscontrol.RetryDeniedException;
import com.top_logic.base.accesscontrol.loginmethod.LoginMethod;
import com.top_logic.base.accesscontrol.loginmethod.LoginMethods;
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
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.overlay.DialogManager;
import com.top_logic.layout.view.ViewMessages;
import com.top_logic.layout.view.command.Continuation;
import com.top_logic.layout.view.command.InterruptibleViewAction;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.layout.view.login.PasswordPromptDialogControl.Verifier;
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
 * provider's authentication once more and returns to the application, and closes itself again.
 * The answer is given in the window the command was run in: the dialog the user left behind closes
 * once the confirmation has arrived, and a sign-in as another account, or one that took longer than
 * the confirmation is offered, aborts the chain with its reason shown there. Every other session is confirmed with the
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
 *           session, and the request coming back from the provider completes the entry, whose
 *           {@link ChainAwaitingIdentity} handler runs on that request's thread. It carries the
 *           rest of the chain into the window waiting for it and reports a failure of that chain
 *           there, exactly as a command failing in that window is reported; the request that
 *           brought the confirmation sees none of it and answers for the identity alone. The password is checked by
 *           {@link AuthenticationDevice#authentify(LoginCredentials)}, the same check a login makes,
 *           guarded by {@link LoginFailuresModule#attempt(String, com.top_logic.basic.util.ResKey1, LoginFailuresModule.Attempt)}
 *           and handed to {@link PasswordPromptDialogControl#openDialog} as a
 *           {@link PasswordPromptDialogControl.Verifier}, which keeps the dialog open on a rejected
 *           password and reports only the accepted one. The passwords entered in the prompt are
 *           counted by the {@link LoginFailuresModule} under the same key as the logins of the
 *           account from the same client address
 *           ({@link LoginFailuresModule#accountKey(String, jakarta.servlet.ServletRequest)}), so
 *           that guesses in the prompt and at the login count together. After repeated wrong
 *           passwords, the prompt refuses further passwords for a growing time without asking the
 *           device, and says so under the input. A prompt answered without a request at hand counts
 *           under the account name alone.
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

		Verifier verifier = createVerifier();
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
		ChainAwaitingIdentity waiting = new ChainAwaitingIdentity(context,
			IdentityVerificationHandler.reauthentication(), input, continuation, VerifyIdentityAction.class,
			action -> runInWindow(context, action));
		String token = registerVerification(waiting);
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
			waiting.setClosePrompt(openWaitingPrompt(context, dialogManager,
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
	 * @param waiting
	 *        The suspended chain, which takes the answer of the identity provider on the thread of
	 *        the request that brings it.
	 * @return The token addressing the announcement, to be carried to the identity provider and
	 *         back, or {@code null} when there is no session account whose return could be awaited.
	 */
	protected String registerVerification(ChainAwaitingIdentity waiting) {
		Person account = sessionAccount();
		if (account == null) {
			return null;
		}
		return IdentityVerifications.forCurrentSession().register(account, waiting);
	}

	/**
	 * Withdraws the announcement made by {@link #registerVerification(ChainAwaitingIdentity)} under the given
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
	 * The verifier of the prompt: the {@link #createPasswordCheck() password check} under the
	 * back-off of the {@link #attemptKey() account}, or {@code null} when there is no password that
	 * can be checked.
	 */
	private Verifier createVerifier() {
		Predicate<char[]> check = createPasswordCheck();
		if (check == null) {
			return null;
		}
		return password -> verify(check, password);
	}

	/**
	 * Checks a password entered in the prompt.
	 *
	 * <p>
	 * An empty password is rejected without troubling the device and without counting as an
	 * attempt. Every other password is checked under the back-off of the {@link #attemptKey()}: while
	 * it is backing off, the password is refused with the time to wait and the check is not run.
	 * </p>
	 *
	 * @return {@code null} for an accepted password, otherwise the reason of the rejection.
	 */
	private ResKey verify(Predicate<char[]> check, char[] password) {
		if (password == null || password.length == 0) {
			return I18NConstants.ERROR_WRONG_PASSWORD;
		}
		String key = attemptKey();
		if (key == null) {
			// No account to count the attempt for: nothing is confirmed.
			return I18NConstants.ERROR_WRONG_PASSWORD;
		}
		try {
			boolean accepted = LoginFailuresModule.getInstance().attempt(key,
				com.top_logic.layout.component.configuration.I18NConstants.ERROR_TOO_MANY_LOGIN_ATTEMPS__TIMEOUT,
				() -> check.test(password));
			return accepted ? null : I18NConstants.ERROR_WRONG_PASSWORD;
		} catch (RetryDeniedException ex) {
			return ex.getErrorKey();
		}
	}

	/**
	 * The key under which the passwords entered in the prompt are counted, or {@code null} when the
	 * session belongs to no account.
	 *
	 * <p>
	 * Determined for each entered password: the session's account and the client address of the
	 * request answering the prompt, the key a login of that account from that address is counted
	 * under, see {@link LoginFailuresModule#accountKey(String, jakarta.servlet.ServletRequest)}.
	 * </p>
	 */
	protected String attemptKey() {
		Person account = sessionAccount();
		if (account == null) {
			return null;
		}
		ServletRequest request =
			DefaultDisplayContext.hasDisplayContext() ? DefaultDisplayContext.getDisplayContext().asRequest() : null;
		return LoginFailuresModule.accountKey(account.getName(), request);
	}

	/**
	 * The check a password entered in the prompt has to pass, or {@code null} when the session's
	 * account has no password that can be checked.
	 *
	 * <p>
	 * The check is the account's own; counting the attempts and refusing them while backing off is
	 * done around it, see {@link #attemptKey()}.
	 * </p>
	 *
	 * @return The check, called with the (non-empty) characters the user entered; {@code null} to
	 *         abort the chain as unverifiable.
	 */
	protected Predicate<char[]> createPasswordCheck() {
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
	 *        The check of the entered password, the {@link #createPasswordCheck() password check}
	 *        under the back-off of the {@link #attemptKey() account}.
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
	 * Runs the given action in the window of the given context, from whatever thread is calling.
	 *
	 * @implNote The step that needs a browser, see
	 *           {@link ChainAwaitingIdentity#runInWindow(ReactContext, Runnable)}.
	 */
	protected void runInWindow(ReactContext context, Runnable action) {
		ChainAwaitingIdentity.runInWindow(context, action);
	}

	/**
	 * Whether the given password authenticates the given account against its device.
	 *
	 * <p>
	 * A device that refuses to look at the account at all counts as a rejection: every path but an
	 * accepted password ends in {@code false}.
	 * </p>
	 */
	private static boolean authenticates(AuthenticationDevice device, Person account, char[] password) {
		try {
			return device.authentify(LoginCredentials.fromUserAndPassword(account, password));
		} catch (LoginDeniedException ex) {
			return false;
		}
	}

	/**
	 * Opens the dialog asking for the identity.
	 */
	@Override
	public boolean changesViewState() {
		return true;
	}
}
