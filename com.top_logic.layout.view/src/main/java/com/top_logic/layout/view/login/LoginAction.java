/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.login;

import com.top_logic.layout.view.command.WithValueArgument;
import java.util.Arrays;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.base.accesscontrol.Login;
import com.top_logic.base.accesscontrol.Login.InMaintenanceModeException;
import com.top_logic.base.accesscontrol.LoginFailuresModule;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.ViewLoader;
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
 * Before the session starts, the account runs the {@link LoginStep}s of the {@link LoginStepsConfig}
 * in a {@link LoginFlow}, each composed as its own dialog view, with the account carried on the
 * {@link #ACCOUNT_CHANNEL} channel: by default an expired password forces a
 * {@link ChangePasswordStep change-password dialog}, and the {@link SecondFactorLoginStep second
 * factor} is confirmed or set up. The user is logged in only once the last step is finished.
 * </p>
 *
 * @implNote The steps and the login run in {@link LoginFlow}, which this action marks with
 *           {@link ChangePasswordStep#PASSWORD_EXPIRED} for an expired password. The password checks are
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

		LoginFlow flow = new LoginFlow(account);
		flow.set(ChangePasswordStep.PASSWORD_EXPIRED, Boolean.valueOf(passwordChangeRequired));
		flow.begin(context);
		return input;
	}

	/**
	 * A fresh transient model for a one-time-code form, whether it verifies an existing secret or
	 * confirms a newly generated one.
	 */
	static TLObject newOtpEntry() {
		return newTransient(OTP_ENTRY_TYPE);
	}

	/**
	 * A fresh transient object of the given model type, e.g. the model of a login-step form.
	 */
	static TLObject newTransient(String typeName) {
		TLClass type = (TLClass) TLModelUtil.findType(typeName);
		return TransientObjectFactory.INSTANCE.createObject(type);
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
