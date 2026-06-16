/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.selfservice;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.top_logic.base.mail.MailSenderService;
import com.top_logic.base.security.device.interfaces.AuthenticationDevice;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.encryption.SecureRandomService;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.overlay.DialogManager;
import com.top_logic.layout.react.control.overlay.DialogResult;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.command.OpenDialogAction;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.impl.TransientObjectFactory;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.security.selfservice.invitation.InvitationModule;
import com.top_logic.util.Resources;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link ViewAction} that starts the self-service password reset: it generates a verification code,
 * emails it to the identified account, and opens the {@link #RESET_PASSWORD_VIEW reset-password
 * dialog} (carrying the account, the expected code and its creation time on dialog channels).
 *
 * <p>
 * To avoid disclosing whether a user name exists, the behaviour is identical regardless of whether
 * the account was found: a code is always generated and the reset dialog always opens; the mail is
 * sent only when a matching, password-changeable account with an email address exists.
 * </p>
 *
 * @implNote When {@link MailSenderService#isConfigured() mail is not configured} (e.g. a dev/demo
 *           setup) the code cannot be emailed; it is logged instead so the flow stays testable. In a
 *           properly configured deployment the code is only ever sent by email.
 */
public class RequestPasswordResetAction implements ViewAction {

	/** Dialog view (relative to {@link ViewLoader#VIEW_BASE_PATH}) for the password-reset step. */
	public static final String RESET_PASSWORD_VIEW = "reset-password.view.xml";

	/** Name of the transient model type holding the reset form input. */
	public static final String PASSWORD_RESET_TYPE = "tl.login:PasswordReset";

	/** Dialog channel carrying the account whose password is being reset. */
	public static final String ACCOUNT_CHANNEL = "account";

	/** Dialog channel carrying the expected verification code. */
	public static final String EXPECTED_CODE_CHANNEL = "expectedCode";

	/** Dialog channel carrying the code's creation timestamp (epoch millis, {@link Long}). */
	public static final String CREATED_AT_CHANNEL = "createdAt";

	/**
	 * Configuration for {@link RequestPasswordResetAction}.
	 */
	@TagName("request-password-reset")
	public interface Config extends PolymorphicConfiguration<RequestPasswordResetAction> {

		@Override
		@ClassDefault(RequestPasswordResetAction.class)
		Class<? extends RequestPasswordResetAction> getImplementationClass();
	}

	/**
	 * Creates a new {@link RequestPasswordResetAction} from configuration.
	 */
	@CalledByReflection
	public RequestPasswordResetAction(InstantiationContext context, Config config) {
		// No configuration needed.
	}

	@Override
	public Object execute(ReactContext context, Object input) {
		if (!(input instanceof TLObject)) {
			return input;
		}
		String userName = asString(((TLObject) input).tValueByName("username"));
		if (isEmpty(userName)) {
			throw new TopLogicException(I18NConstants.RESET_MISSING_USERNAME);
		}

		String code = newCode();
		Person account = Person.byName(userName);
		deliverCode(userName, account, code);

		openResetDialog(context, account, code);
		InfoService.showInfo(I18NConstants.RESET_CODE_SENT);
		return input;
	}

	private static void deliverCode(String userName, Person account, String code) {
		if (MailSenderService.isConfigured()) {
			sendResetMail(account, code);
		} else {
			// Dev / no-mail fallback: surface the code in the server log so the flow stays testable
			// without an SMTP setup. In a configured deployment the code is sent by email only and
			// never logged.
			Logger.info("Mail not configured; password reset code for '" + userName + "' is " + code
				+ " (dev fallback)", RequestPasswordResetAction.class);
		}
	}

	private static void sendResetMail(Person account, String code) {
		if (account == null) {
			return;
		}
		AuthenticationDevice device = account.getAuthenticationDevice();
		if (device == null || !device.allowPwdChange()) {
			return;
		}
		String email = account.getUser() != null ? account.getUser().getEMail() : null;
		if (isEmpty(email)) {
			return;
		}
		String application = Resources.getInstance().getString(com.top_logic.layout.I18NConstants.APPLICATION_TITLE);
		InvitationModule.getInstance().getResetPasswordMail().execute(email, application, code);
	}

	private static void openResetDialog(ReactContext context, Person account, String code) {
		DialogManager dialogManager = context.getDialogManager();
		if (dialogManager != null) {
			dialogManager.closeTopDialog(DialogResult.cancelled());
		}
		Map<String, Object> channels = new LinkedHashMap<>();
		channels.put("model", newReset());
		channels.put(ACCOUNT_CHANNEL, account);
		channels.put(EXPECTED_CODE_CHANNEL, code);
		channels.put(CREATED_AT_CHANNEL, Long.valueOf(System.currentTimeMillis()));
		// Do not close on a backdrop click: an accidental click outside must not abort the reset.
		OpenDialogAction.openDialog(context, ViewLoader.VIEW_BASE_PATH + RESET_PASSWORD_VIEW, false, channels,
			Collections.emptyList());
	}

	private static TLObject newReset() {
		TLClass type = (TLClass) TLModelUtil.findType(PASSWORD_RESET_TYPE);
		return TransientObjectFactory.INSTANCE.createObject(type);
	}

	private static String newCode() {
		int size = InvitationModule.getInstance().getConfig().getVerificationCodeSize();
		long upperBound = (long) Math.pow(10, size);
		long value = SecureRandomService.getInstance().getRandom().nextLong(0, upperBound);
		return String.format("%0" + size + "d", Long.valueOf(value));
	}

	private static String asString(Object value) {
		return value == null ? null : value.toString();
	}

	private static boolean isEmpty(String value) {
		return value == null || value.isEmpty();
	}

}
