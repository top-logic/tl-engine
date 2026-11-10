/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.selfservice;

import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import com.top_logic.base.accesscontrol.LoginFailuresModule;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.layout.view.login.LoginAction;
import com.top_logic.model.TLObject;
import com.top_logic.security.selfservice.invitation.SelfRegistration;
import com.top_logic.security.selfservice.invitation.VerificationCode;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link ViewAction} of the {@link #REGISTER_VIEW registration dialog}: it requests a
 * {@link VerificationCode} for the entered e-mail address from the {@link SelfRegistration} and
 * opens the code dialog in which the visitor enters it (see {@link CheckVerificationCodeAction}).
 *
 * <p>
 * The input is the transient {@link #REGISTRATION_TYPE} the dialog edits. The solution of the
 * captcha, if the self-registration requires one, comes from the channel named by
 * {@link Config#getCaptcha()}. A solution is accepted once only, so the action clears that channel
 * after every request, which gives the captcha a new challenge.
 * </p>
 *
 * <p>
 * The visitor is told the same in every case, whether the address has an account or not, so the
 * dialog does not reveal which addresses have one. A refusal - registration switched off, captcha
 * not solved, invalid address, a request that follows the previous one too soon - is reported and
 * leaves the dialog open.
 * </p>
 *
 * <p>
 * The code dialog carries the address as {@link RegistrationRequest}, which creates the account.
 * </p>
 *
 * @implNote The code is requested by {@link SelfRegistration#requestCode(String, String, String)}
 *           with the network address of the client of the request, as the login counts failed
 *           attempts by it (see {@link LoginFailuresModule}). The account is created by
 *           {@link SelfRegistration#register(String, String, String, char[])}.
 */
@InApp
public class RequestRegistrationCodeAction implements ViewAction {

	/** Dialog view (relative to {@link ViewLoader#VIEW_BASE_PATH}) for entering the address. */
	public static final String REGISTER_VIEW = "register.view.xml";

	/** Name of the transient model type holding the input of the registration dialog. */
	public static final String REGISTRATION_TYPE = "tl.login:RegistrationRequest";

	/** Attribute of {@link #REGISTRATION_TYPE} holding the e-mail address. */
	public static final String EMAIL_ATTR = "email";

	/** Default name of the channel holding the solution of the captcha. */
	public static final String CAPTCHA_CHANNEL = "captcha";

	/**
	 * Configuration for {@link RequestRegistrationCodeAction}.
	 */
	@TagName("request-registration-code")
	public interface Config extends PolymorphicConfiguration<RequestRegistrationCodeAction> {

		/** Configuration name for {@link #getCaptcha()}. */
		String CAPTCHA = "captcha";

		@Override
		@ClassDefault(RequestRegistrationCodeAction.class)
		Class<? extends RequestRegistrationCodeAction> getImplementationClass();

		/**
		 * The name of the channel of the view holding the solution of the captcha, the channel the
		 * captcha element writes to.
		 */
		@Name(CAPTCHA)
		@StringDefault(CAPTCHA_CHANNEL)
		String getCaptcha();
	}

	private final String _captchaChannel;

	/**
	 * Creates a new {@link RequestRegistrationCodeAction} from configuration.
	 */
	@CalledByReflection
	public RequestRegistrationCodeAction(InstantiationContext context, Config config) {
		_captchaChannel = config.getCaptcha();
	}

	@Override
	public Object execute(ReactContext context, Object input) {
		if (!(input instanceof TLObject)) {
			return input;
		}
		String email = SelfServiceSupport.stringValue((TLObject) input, EMAIL_ATTR);
		if (SelfServiceSupport.isEmpty(email) || email.isBlank()) {
			throw new TopLogicException(I18NConstants.EMAIL_EMPTY);
		}
		email = email.strip();

		Object captcha = SelfServiceSupport.channel(context, _captchaChannel);
		VerificationCode code;
		try {
			code = SelfRegistration.getInstance().requestCode(email,
				captcha == null ? null : captcha.toString(), clientAddress());
		} finally {
			if (captcha != null) {
				SelfServiceSupport.clearChannel(context, _captchaChannel);
			}
		}

		CheckVerificationCodeAction.openCodeDialog(context, new RegistrationRequest(email), code);
		InfoService.showInfo(com.top_logic.security.selfservice.I18NConstants.REGISTRATION_CODE_SENT__EMAIL.fill(email));
		return input;
	}

	private static String clientAddress() {
		DisplayContext displayContext = DefaultDisplayContext.getDisplayContext();
		HttpServletRequest request = displayContext == null ? null : displayContext.asRequest();
		return request == null ? null : request.getRemoteAddr();
	}

	/**
	 * Opens the registration dialog in place of the top dialog.
	 *
	 * @param context
	 *        The context of the action.
	 * @param email
	 *        The address to fill in, <code>null</code> for none.
	 */
	public static void openRegistrationDialog(ReactContext context, String email) {
		TLObject registration = SelfServiceSupport.newTransient(REGISTRATION_TYPE);
		registration.tUpdateByName(EMAIL_ATTR, email);

		Map<String, Object> channels = new LinkedHashMap<>();
		channels.put(LoginAction.MODEL_CHANNEL, registration);
		SelfServiceSupport.replaceTopDialog(context, REGISTER_VIEW, channels);
	}

	/**
	 * Opens the code dialog.
	 */
	@Override
	public boolean changesViewState() {
		return true;
	}
}
