/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.login;

import com.top_logic.layout.view.command.WithValueArgument;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.base.accesscontrol.Login;
import com.top_logic.base.accesscontrol.Login.InMaintenanceModeException;
import com.top_logic.base.accesscontrol.LoginFailuresModule;
import com.top_logic.base.accesscontrol.SessionService;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.knowledge.wrap.person.MfaRequirement;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.overlay.DialogManager;
import com.top_logic.layout.react.control.overlay.DialogResult;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.command.OpenDialogAction;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.impl.TransientObjectFactory;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link ViewAction} that authenticates the credentials held by its input object and logs the
 * resulting user in within the current browser window.
 *
 * <p>
 * Expects its input to be a (transient) object carrying {@code name} and {@code password}
 * attributes, e.g. the model of the login {@code <form>} as the result of an enclosing {@code <store-form-state>}. On
 * success the user is logged in; on failure a {@link TopLogicException} is raised so the form shows
 * the error.
 * </p>
 *
 * <p>
 * Before the login, follow-up steps may be interposed (each composed as its own dialog view,
 * with the account carried on the {@link #ACCOUNT_CHANNEL} channel):
 * </p>
 * <ul>
 * <li>An expired password forces a {@link #CHANGE_PASSWORD_VIEW change-password dialog}
 * ({@link ChangePasswordApplyAction}).</li>
 * <li>An account with a {@link SecondFactor} set up confirms itself with it, e.g. in the
 * {@link #OTP_VIEW OTP-verification dialog} of the {@link TotpSecondFactor}.</li>
 * <li>An account for which MFA is {@link MfaRequirement#REQUIRED required} but that has no second
 * factor set up yet sets one up, e.g. in the {@link #MFA_ENROLL_VIEW enrollment dialog} of the
 * {@link TotpSecondFactor}.</li>
 * </ul>
 *
 * <p>
 * The user is logged in only once the final step completes the login.
 * </p>
 *
 * @implNote The login is performed by {@link #completeLogin}; the second-factor step is decided by
 *           {@link SecondFactorStep}, see {@link #proceedAfterPassword}. The password checks are
 *           counted by the {@link LoginFailuresModule} per account and client address
 *           ({@link LoginFailuresModule#accountKey(String, jakarta.servlet.ServletRequest)}),
 *           together with the logins of the classic UI and the identity confirmations of
 *           {@link VerifyIdentityAction}: after repeated wrong passwords, further logins are refused
 *           for a growing time without the password being checked.
 */
@InApp
public class LoginAction implements ViewAction {

	/** Name of the transient model type holding the change-password form input. */
	public static final String PASSWORD_CHANGE_TYPE = "tl.login:PasswordChange";

	/** Attribute of {@link #PASSWORD_CHANGE_TYPE} holding the newly chosen password. */
	public static final String NEW_PASSWORD_ATTR = "newPassword";

	/** Attribute of {@link #PASSWORD_CHANGE_TYPE} holding the repetition of the new password. */
	public static final String NEW_PASSWORD_CONFIRM_ATTR = "newPasswordConfirm";

	/** Dialog view (relative to {@link ViewLoader#VIEW_BASE_PATH}) for the forced password change. */
	public static final String CHANGE_PASSWORD_VIEW = "change-password.view.xml";

	/** Dialog channel carrying the account whose (expired) password is being changed. */
	public static final String ACCOUNT_CHANNEL = "account";

	/** Dialog view for the OTP-verification step (account has an existing MFA secret). */
	public static final String OTP_VIEW = "otp.view.xml";

	/** Dialog view for the MFA-enrollment step (MFA required, no secret set yet). */
	public static final String MFA_ENROLL_VIEW = "mfa-enroll.view.xml";

	/** Dialog channel carrying the transient model a login-step form edits. */
	public static final String MODEL_CHANNEL = "model";

	/** Dialog channel carrying the TOTP secret being verified (existing or freshly generated). */
	public static final String SECRET_CHANNEL = "secret";

	/** Dialog channel carrying the QR-code image shown during MFA enrollment. */
	public static final String QR_CHANNEL = "qr";

	/**
	 * Dialog channel carrying the freshly generated TOTP secret as plain text, for entering it into
	 * an authenticator app by hand during MFA enrollment.
	 */
	public static final String SECRET_TEXT_CHANNEL = "secret-text";

	/** Name of the transient model type holding the OTP form input. */
	public static final String OTP_ENTRY_TYPE = "tl.login:OtpEntry";

	/**
	 * Configuration for {@link LoginAction}.
	 */
	@TagName("login")
	public interface Config extends PolymorphicConfiguration<LoginAction>, WithValueArgument {

		@Override
		@ClassDefault(LoginAction.class)
		Class<? extends LoginAction> getImplementationClass();
	}

	/**
	 * Creates a new {@link LoginAction} from configuration.
	 */
	@CalledByReflection
	public LoginAction(InstantiationContext context, Config config) {
		// No configuration needed.
	}

	@Override
	public Object execute(ReactContext context, Object input) {
		if (!(input instanceof TLObject)) {
			return input;
		}
		TLObject credentials = (TLObject) input;
		String userName = asString(credentials.tValueByName("name"));
		String password = asString(credentials.tValueByName("password"));
		if (isEmpty(userName) || isEmpty(password)) {
			throw new TopLogicException(I18NConstants.LOGIN_MISSING_CREDENTIALS);
		}

		DisplayContext displayContext = DefaultDisplayContext.getDisplayContext();
		char[] decryptedPassword = password.toCharArray();
		Person account;
		boolean passwordChangeRequired;
		LoginFailuresModule<?> loginFailures = LoginFailuresModule.getInstance();
		String accountKey = LoginFailuresModule.accountKey(userName, displayContext.asRequest());
		try {
			// Refuses with the too-many-attempts message while the account is backing off.
			loginFailures.checkRetry(accountKey,
				com.top_logic.layout.component.configuration.I18NConstants.ERROR_TOO_MANY_LOGIN_ATTEMPS__TIMEOUT);

			boolean success;
			try {
				success = Login.getInstance()
					.checkUserPassword(userName, decryptedPassword, displayContext.asRequest(),
						displayContext.asResponse());
			} catch (InMaintenanceModeException ex) {
				Logger.info("Login attempt for '" + userName + "' rejected: maintenance mode active.",
					LoginAction.class);
				throw new TopLogicException(Login.getMaintenanceMessage(userName), ex);
			} catch (Exception ex) {
				Logger.info("Login attempt for '" + userName + "' rejected: " + ex.getMessage(), LoginAction.class);
				throw new TopLogicException(I18NConstants.LOGIN_FAILED, ex);
			}

			if (!loginFailures.recordAttempt(accountKey, success)) {
				throw new TopLogicException(I18NConstants.LOGIN_FAILED);
			}

			account = Person.byName(userName);
			passwordChangeRequired = !Login.isPasswordValidAndNotExpired(decryptedPassword, account);
		} finally {
			Arrays.fill(decryptedPassword, (char) 0);
		}

		if (passwordChangeRequired) {
			// Hold back the login and force a password change first.
			openChangePasswordDialog(context, account);
			return input;
		}

		proceedAfterPassword(context, account);
		return input;
	}

	/**
	 * Continues the login flow after the password has been verified (and, when it was expired, a new
	 * one applied): confirms the account with a second factor, sets one up, or completes the login,
	 * as {@link SecondFactorStep#decide(Person)} decides for the configured {@link SecondFactor}s.
	 *
	 * @param context
	 *        The context of the dialog the login is in.
	 * @param account
	 *        The account whose password has been checked.
	 */
	public static void proceedAfterPassword(ReactContext context, Person account) {
		SecondFactorStep.decide(account).perform(context, account);
	}

	/**
	 * Replaces the current (login) dialog with the forced change-password dialog, transferring the
	 * authenticated account on its {@link #ACCOUNT_CHANNEL} channel and a fresh transient
	 * {@link #PASSWORD_CHANGE_TYPE} model on its {@code "model"} channel.
	 */
	private static void openChangePasswordDialog(ReactContext context, Person account) {
		Map<String, Object> channels = new LinkedHashMap<>();
		channels.put(MODEL_CHANNEL, newTransient(PASSWORD_CHANGE_TYPE));
		channels.put(ACCOUNT_CHANNEL, account);
		replaceDialog(context, CHANGE_PASSWORD_VIEW, channels);
	}

	/**
	 * Closes the current top dialog and opens the given view as a modal dialog seeded with the given
	 * channel values: the way a step of the login takes over from the previous one.
	 *
	 * <p>
	 * The dialog does not close on a click on the backdrop, so that an accidental click does not
	 * abort the login.
	 * </p>
	 *
	 * @param context
	 *        The context of the dialog the login is in.
	 * @param view
	 *        The dialog view, relative to {@link ViewLoader#VIEW_BASE_PATH}.
	 * @param channels
	 *        The values of the dialog's channels, by channel name, e.g. the account on the
	 *        {@link #ACCOUNT_CHANNEL} channel.
	 */
	public static void replaceDialog(ReactContext context, String view, Map<String, ?> channels) {
		DialogManager dialogManager = context.getDialogManager();
		if (dialogManager != null) {
			dialogManager.closeTopDialog(DialogResult.cancelled());
		}
		// Do not close on a backdrop click: an accidental click outside must not silently abort the
		// login flow (change-password / OTP / MFA-enrollment step).
		OpenDialogAction.openDialog(context, ViewLoader.VIEW_BASE_PATH + view, false, channels,
			Collections.emptyList());
	}

	/**
	 * A fresh transient model for a one-time-code form, whether it verifies an existing secret or
	 * confirms a newly generated one.
	 */
	static TLObject newOtpEntry() {
		return newTransient(OTP_ENTRY_TYPE);
	}

	private static TLObject newTransient(String typeName) {
		TLClass type = (TLClass) TLModelUtil.findType(typeName);
		return TransientObjectFactory.INSTANCE.createObject(type);
	}

	/**
	 * Completes a successful authentication: logs the user in within the current browser window,
	 * closes the top dialog, and rebuilds the window for the user.
	 *
	 * <p>
	 * The login applies to the window it happens in, see
	 * {@link SessionService#loginUserInWindow(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse, Person)}:
	 * the session is kept, other windows of the session keep their user, and a window opened later
	 * starts with the user logged in here. The changed session ID reaches the browser with the
	 * answer to the current command. The window's tree, built for the previous user, is discarded
	 * and the window is reloaded ({@link ReactWindowRegistry#rebuildWindow(String)}), which renders
	 * it anew for the user.
	 * </p>
	 *
	 * <p>
	 * Re-checks the maintenance mode before the login: the follow-up steps (second-factor
	 * verification, forced password change) can complete long after the password check, so
	 * maintenance mode may have been activated in between.
	 * </p>
	 *
	 * <p>
	 * A {@link SecondFactor} calls this once the account has proven itself.
	 * </p>
	 *
	 * @param context
	 *        The context of the dialog the login is in.
	 * @param userName
	 *        The name of the account to log in.
	 */
	public static void completeLogin(ReactContext context, String userName) {
		Person account = Person.byName(userName);
		try {
			Login.getInstance().checkAllowedGroups(account);
		} catch (InMaintenanceModeException ex) {
			Logger.info("Login completion for '" + userName + "' rejected: maintenance mode active.",
				LoginAction.class);
			throw new TopLogicException(Login.getMaintenanceMessage(userName), ex);
		}

		DisplayContext displayContext = DefaultDisplayContext.getDisplayContext();
		SessionService.getInstance().loginUserInWindow(displayContext.asRequest(), displayContext.asResponse(),
			account);

		DialogManager dialogManager = context.getDialogManager();
		if (dialogManager != null) {
			dialogManager.closeTopDialog(DialogResult.cancelled());
		}
		context.getWindowRegistry().rebuildWindow(context.getWindowName());
	}

	private static String asString(Object value) {
		return value == null ? null : value.toString();
	}

	private static boolean isEmpty(String value) {
		return value == null || value.isEmpty();
	}

	/**
	 * Logs the authenticated user in or opens a dialog.
	 */
	@Override
	public boolean changesViewState() {
		return true;
	}
}
