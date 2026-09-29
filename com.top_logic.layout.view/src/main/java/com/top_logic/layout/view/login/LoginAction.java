/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.login;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.top_logic.base.accesscontrol.Login;
import com.top_logic.base.accesscontrol.Login.InMaintenanceModeException;
import com.top_logic.base.security.util.Password;
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
import com.top_logic.layout.react.protocol.JSSnipplet;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.command.OpenDialogAction;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.impl.TransientObjectFactory;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link ViewAction} that authenticates the credentials held by its input object and switches the
 * session to the resulting user.
 *
 * <p>
 * Expects its input to be a (transient) object carrying {@code name} and {@code password}
 * attributes, e.g. the model of the login {@code <form>} after {@code <store-form-state/>}. On
 * success the actual session swap is deferred to the reload via {@link PendingSessionAction}; on
 * failure a {@link TopLogicException} is raised so the form shows the error.
 * </p>
 *
 * <p>
 * Before the session swap, follow-up steps may be interposed (each composed as its own dialog view,
 * with the account carried on the {@link #ACCOUNT_CHANNEL} channel):
 * </p>
 * <ul>
 * <li>An expired password forces a {@link #CHANGE_PASSWORD_VIEW change-password dialog}
 * ({@link ChangePasswordApplyAction}).</li>
 * <li>An account with a TOTP secret must pass an {@link #OTP_VIEW OTP-verification dialog}
 * ({@link VerifyOtpAction}).</li>
 * <li>An account for which MFA is {@link MfaRequirement#REQUIRED required} but has no secret yet is
 * sent through an {@link #MFA_ENROLL_VIEW enrollment dialog} that generates a secret, shows its QR
 * code and confirms it via {@link VerifyOtpAction}.</li>
 * </ul>
 *
 * <p>
 * The actual session swap (via {@link PendingSessionAction}) happens only once the final step
 * completes the login.
 * </p>
 *
 * @implNote The deferred session swap is triggered by {@link #completeLogin}; the per-step MFA
 *           branching is in {@link #proceedAfterPassword}.
 */
public class LoginAction implements ViewAction {

	/** Name of the transient model type holding the change-password form input. */
	public static final String PASSWORD_CHANGE_TYPE = "tl.login:PasswordChange";

	/** Dialog view (relative to {@link ViewLoader#VIEW_BASE_PATH}) for the forced password change. */
	public static final String CHANGE_PASSWORD_VIEW = "change-password.view.xml";

	/** Dialog channel carrying the account whose (expired) password is being changed. */
	public static final String ACCOUNT_CHANNEL = "account";

	/** Dialog view for the OTP-verification step (account has an existing MFA secret). */
	public static final String OTP_VIEW = "otp.view.xml";

	/** Dialog view for the MFA-enrollment step (MFA required, no secret set yet). */
	public static final String MFA_ENROLL_VIEW = "mfa-enroll.view.xml";

	/** Dialog channel carrying the TOTP secret being verified (existing or freshly generated). */
	public static final String SECRET_CHANNEL = "secret";

	/** Dialog channel carrying the QR-code image shown during MFA enrollment. */
	public static final String QR_CHANNEL = "qr";

	/** Name of the transient model type holding the OTP form input. */
	public static final String OTP_ENTRY_TYPE = "tl.login:OtpEntry";

	/**
	 * Configuration for {@link LoginAction}.
	 */
	@TagName("login")
	public interface Config extends PolymorphicConfiguration<LoginAction> {

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
		try {
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

			if (!success) {
				throw new TopLogicException(I18NConstants.LOGIN_FAILED);
			}

			account = Person.byName(userName);
			passwordChangeRequired = !Login.isPasswordValidAndNotExpired(decryptedPassword, account);
		} finally {
			Arrays.fill(decryptedPassword, (char) 0);
		}

		if (passwordChangeRequired) {
			// Hold back the session swap and force a password change first.
			openChangePasswordDialog(context, account);
			return input;
		}

		proceedAfterPassword(context, account);
		return input;
	}

	/**
	 * Continues the login flow after the password has been verified (and, when it was expired, a new
	 * one applied): opens the {@link #OTP_VIEW OTP-verification dialog} if the account has a TOTP
	 * secret, opens the {@link #MFA_ENROLL_VIEW enrollment dialog} if MFA is
	 * {@link MfaRequirement#REQUIRED required} but no secret is set yet, and otherwise completes the
	 * login.
	 */
	static void proceedAfterPassword(ReactContext context, Person account) {
		Password secret = account.getMFASecret();
		if (secret != null) {
			openOtpDialog(context, account, secret);
		} else if (account.getMFARequirement() == MfaRequirement.REQUIRED) {
			openMfaEnrollDialog(context, account);
		} else {
			completeLogin(context, account.getName());
		}
	}

	/**
	 * Replaces the current (login) dialog with the forced change-password dialog, transferring the
	 * authenticated account on its {@link #ACCOUNT_CHANNEL} channel and a fresh transient
	 * {@link #PASSWORD_CHANGE_TYPE} model on its {@code "model"} channel.
	 */
	private static void openChangePasswordDialog(ReactContext context, Person account) {
		Map<String, Object> channels = new LinkedHashMap<>();
		channels.put("model", newTransient(PASSWORD_CHANGE_TYPE));
		channels.put(ACCOUNT_CHANNEL, account);
		replaceDialog(context, CHANGE_PASSWORD_VIEW, channels);
	}

	/**
	 * Replaces the current (login) dialog with the OTP-verification dialog, transferring the account
	 * and its existing TOTP secret on the {@link #ACCOUNT_CHANNEL} / {@link #SECRET_CHANNEL} channels.
	 */
	private static void openOtpDialog(ReactContext context, Person account, Password secret) {
		Map<String, Object> channels = new LinkedHashMap<>();
		channels.put("model", newTransient(OTP_ENTRY_TYPE));
		channels.put(ACCOUNT_CHANNEL, account);
		channels.put(SECRET_CHANNEL, secret);
		replaceDialog(context, OTP_VIEW, channels);
	}

	/**
	 * Replaces the current (login) dialog with the MFA-enrollment dialog, generating a fresh secret
	 * and its QR code and transferring them on the {@link #SECRET_CHANNEL} / {@link #QR_CHANNEL}
	 * channels (the secret is persisted only once {@link VerifyOtpAction} confirms a valid code).
	 */
	private static void openMfaEnrollDialog(ReactContext context, Person account) {
		Password secret = MfaSupport.generateSecret();
		Map<String, Object> channels = new LinkedHashMap<>();
		channels.put("model", newTransient(OTP_ENTRY_TYPE));
		channels.put(ACCOUNT_CHANNEL, account);
		channels.put(SECRET_CHANNEL, secret);
		channels.put(QR_CHANNEL, MfaSupport.createQrCode(account, secret));
		replaceDialog(context, MFA_ENROLL_VIEW, channels);
	}

	/**
	 * Closes the current top dialog and opens the given view as a modal dialog seeded with the given
	 * channel values.
	 */
	private static void replaceDialog(ReactContext context, String view, Map<String, ?> channels) {
		DialogManager dialogManager = context.getDialogManager();
		if (dialogManager != null) {
			dialogManager.closeTopDialog(DialogResult.cancelled());
		}
		// Do not close on a backdrop click: an accidental click outside must not silently abort the
		// login flow (change-password / OTP / MFA-enrollment step).
		OpenDialogAction.openDialog(context, ViewLoader.VIEW_BASE_PATH + view, false, channels,
			Collections.emptyList());
	}

	private static TLObject newTransient(String typeName) {
		TLClass type = (TLClass) TLModelUtil.findType(typeName);
		return TransientObjectFactory.INSTANCE.createObject(type);
	}

	/**
	 * Completes a successful authentication: records the deferred login on the HTTP session, closes
	 * the top dialog, and triggers the client reload that lets {@link PendingSessionAction} perform
	 * the actual session swap.
	 *
	 * <p>
	 * Re-checks the maintenance mode before recording the login: the follow-up steps (OTP
	 * verification, forced password change) can complete long after the password check, so
	 * maintenance mode may have been activated in between.
	 * </p>
	 */
	static void completeLogin(ReactContext context, String userName) {
		try {
			Login.getInstance().checkAllowedGroups(Person.byName(userName));
		} catch (InMaintenanceModeException ex) {
			Logger.info("Login completion for '" + userName + "' rejected: maintenance mode active.",
				LoginAction.class);
			throw new TopLogicException(Login.getMaintenanceMessage(userName), ex);
		}

		DisplayContext displayContext = DefaultDisplayContext.getDisplayContext();
		PendingSessionAction.requestLogin(displayContext.asRequest().getSession(), userName);

		DialogManager dialogManager = context.getDialogManager();
		if (dialogManager != null) {
			dialogManager.closeTopDialog(DialogResult.cancelled());
		}
		context.getSSEQueue().enqueue(JSSnipplet.create().setCode("window.location.reload();"));
	}

	private static String asString(Object value) {
		return value == null ? null : value.toString();
	}

	private static boolean isEmpty(String value) {
		return value == null || value.isEmpty();
	}

}
