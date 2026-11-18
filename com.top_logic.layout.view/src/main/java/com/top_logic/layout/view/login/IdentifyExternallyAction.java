/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.login;

import com.top_logic.base.accesscontrol.IdentityVerificationHandler;
import com.top_logic.base.accesscontrol.IdentityVerifications;
import com.top_logic.base.accesscontrol.loginmethod.LoginMethod;
import com.top_logic.base.accesscontrol.loginmethod.LoginMethods;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Mandatory;
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
 * Interruptible {@link ViewAction} that has the user of the session sign in at an external
 * identity provider and hands the identity the provider confirms to a configured
 * {@link IdentityVerificationHandler}.
 *
 * <p>
 * The input of the action is the {@link LoginMethod#getId() ID} of the login method to sign in
 * with; it must {@link LoginMethod#canIdentify() identify} a user within a running session. The
 * browser opens the sign-in of that method in a window of its own while the chain waits in a
 * dialog. The request returning from the provider is no login: the identity it confirms goes to
 * the {@link Config#getHandler() handler}, together with the account of the session that ran the
 * command. When the handler accepts it, the dialog closes and the chain resumes with its input
 * unchanged; when the handler refuses it, or the sign-in took longer than the verification is
 * offered, the dialog closes, the reason is shown and the chain aborts - in the window the command
 * was run in, while the window of the sign-in closes itself. A cancelled dialog aborts the chain.
 * </p>
 *
 * <p>
 * Example: connect a further login with the session's account, after a fresh confirmation of the
 * session.
 * </p>
 *
 * <pre>
 * &lt;generic-command inputs="loginMethod"&gt;
 *   &lt;verify-identity/&gt;
 *   &lt;identify-externally handler="my.module.ConnectIdentity"/&gt;
 * &lt;/generic-command&gt;
 * </pre>
 *
 * <p>
 * The action fails closed: without a dialog manager, a session account, or a login method that can
 * identify the user, the chain aborts.
 * </p>
 *
 * @see LoginMethod#getIdentificationUrl(String)
 * @see IdentityVerifications
 */
@InApp
public class IdentifyExternallyAction extends InterruptibleViewAction {

	/**
	 * Configuration for {@link IdentifyExternallyAction}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends PolymorphicConfiguration<IdentifyExternallyAction> {

		/** Configuration tag of an {@link IdentifyExternallyAction}. */
		String TAG_NAME = "identify-externally";

		/** Configuration name for {@link #getHandler()}. */
		String HANDLER = "handler";

		/** Configuration name for {@link #getTitle()}. */
		String TITLE = "title";

		/** Configuration name for {@link #getMessage()}. */
		String MESSAGE = "message";

		@Override
		@ClassDefault(IdentifyExternallyAction.class)
		Class<? extends IdentifyExternallyAction> getImplementationClass();

		/**
		 * What the identity confirmed by the provider means for the account of the session.
		 *
		 * <p>
		 * The name of a class implementing {@link IdentityVerificationHandler}, with a public
		 * no-argument constructor or a static <code>INSTANCE</code> field.
		 * </p>
		 */
		@Name(HANDLER)
		@Mandatory
		@InstanceFormat
		IdentityVerificationHandler getHandler();

		/**
		 * The title of the dialog waiting for the sign-in.
		 *
		 * <p>
		 * Defaults to a generic title when not set.
		 * </p>
		 */
		@Name(TITLE)
		ResKey getTitle();

		/**
		 * The explanation in the dialog waiting for the sign-in, saying what the sign-in is for.
		 *
		 * <p>
		 * Defaults to a generic explanation when not set.
		 * </p>
		 */
		@Name(MESSAGE)
		ResKey getMessage();
	}

	private final IdentityVerificationHandler _handler;

	private final ResKey _title;

	private final ResKey _message;

	/**
	 * Creates a new {@link IdentifyExternallyAction} from configuration.
	 */
	@CalledByReflection
	public IdentifyExternallyAction(InstantiationContext context, Config config) {
		_handler = config.getHandler();
		_title = config.getTitle();
		_message = config.getMessage();
	}

	@Override
	public void execute(ReactContext context, Object input, Continuation continuation) {
		DialogManager dialogManager = context.getDialogManager();
		Person account = sessionAccount();
		if (dialogManager == null || account == null) {
			continuation.abort();
			return;
		}

		LoginMethod method = loginMethod(input);
		if (method == null) {
			ViewMessages.error(context, I18NConstants.ERROR_LOGIN_METHOD_NOT_IDENTIFYING);
			continuation.abort();
			return;
		}

		ChainAwaitingIdentity waiting = new ChainAwaitingIdentity(context, _handler, input, continuation,
			IdentifyExternallyAction.class, action -> ChainAwaitingIdentity.runInWindow(context, action));
		IdentityVerifications verifications = IdentityVerifications.forCurrentSession();
		String token = verifications.register(account, waiting);

		String url = method.getIdentificationUrl(token);
		if (url == null) {
			verifications.cancel(token);
			ViewMessages.error(context, I18NConstants.ERROR_LOGIN_METHOD_NOT_IDENTIFYING);
			continuation.abort();
			return;
		}

		Resources resources = Resources.getInstance();
		waiting.setClosePrompt(ReauthenticationPromptDialogControl.openDialog(context, dialogManager,
			resources.getString(_title != null ? _title : I18NConstants.IDENTIFY_EXTERNALLY_TITLE),
			resources.getString(_message != null ? _message : I18NConstants.IDENTIFY_EXTERNALLY_MESSAGE),
			// The provider's own label is the button: it names where the user signs in.
			resources.getString(method.getLabel()),
			url,
			() -> {
				verifications.cancel(token);
				continuation.abort();
			}));
	}

	/**
	 * The available login method with the given ID that can identify a user within a session, or
	 * {@code null} if there is none.
	 */
	private static LoginMethod loginMethod(Object id) {
		if (!(id instanceof String)) {
			return null;
		}
		for (LoginMethod method : LoginMethods.available(LoginMethods.all())) {
			if (method.getId().equals(id) && method.canIdentify()) {
				return method;
			}
		}
		return null;
	}

	/**
	 * The account of the running session, or {@code null} for the anonymous session.
	 */
	private static Person sessionAccount() {
		if (TLContext.isAnonymous()) {
			return null;
		}
		return TLContext.currentUser();
	}

	/**
	 * Opens the dialog waiting for the sign-in.
	 */
	@Override
	public boolean changesViewState() {
		return true;
	}

}
