/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.selfservice;

import java.util.LinkedHashMap;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.layout.view.login.LoginAction;
import com.top_logic.model.TLObject;
import com.top_logic.security.selfservice.invitation.InvitationModule;
import com.top_logic.security.selfservice.invitation.VerificationCode;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link ViewAction} of the code dialog of an {@link AccountRequest}: it checks the entered code
 * against the {@link VerificationCode} that was mailed and replaces the dialog with the
 * {@link #CREATE_ACCOUNT_VIEW dialog creating the account}.
 *
 * <p>
 * The code dialog ({@link #CODE_VIEW}) edits a transient {@link #CODE_ENTRY_TYPE}. It carries the
 * request on the channel {@link AccountRequest#REQUEST_CHANNEL} and the code on the channel
 * {@link AccountRequest#VERIFICATION_CHANNEL}.
 * </p>
 *
 * <p>
 * The account dialog edits a transient {@link #ACCOUNT_CREATION_TYPE} whose user name is the
 * e-mail address of the request. It receives the request on the channel
 * {@link AccountRequest#REQUEST_CHANNEL} and, on the channel {@link #PASSWORDS_CHANNEL}, whether
 * the person chooses a password; {@link CreateOwnAccountAction} creates the account.
 * </p>
 *
 * @implNote The code dialog is opened by
 *           {@link #openCodeDialog(ReactContext, AccountRequest, VerificationCode)}.
 */
@InApp
public class CheckVerificationCodeAction implements ViewAction {

	/** Dialog view (relative to {@link ViewLoader#VIEW_BASE_PATH}) for entering the code. */
	public static final String CODE_VIEW = "verification-code.view.xml";

	/** Name of the transient model type holding the entered code. */
	public static final String CODE_ENTRY_TYPE = "tl.login:OtpEntry";

	/** Attribute of {@link #CODE_ENTRY_TYPE} holding the entered code. */
	public static final String CODE_ATTR = "code";

	/** Dialog view (relative to {@link ViewLoader#VIEW_BASE_PATH}) creating the account. */
	public static final String CREATE_ACCOUNT_VIEW = "create-account.view.xml";

	/** Name of the transient model type holding the input of the account dialog. */
	public static final String ACCOUNT_CREATION_TYPE = "tl.login:AccountCreation";

	/** Attribute of {@link #ACCOUNT_CREATION_TYPE} holding the user name of the new account. */
	public static final String USER_NAME_ATTR = "userName";

	/**
	 * Dialog channel telling whether the account dialog asks for a password: whether the
	 * {@link InvitationModule#authenticationDevice() authentication device} of the new account
	 * manages passwords.
	 */
	public static final String PASSWORDS_CHANNEL = "passwords";

	/**
	 * Configuration for {@link CheckVerificationCodeAction}.
	 */
	@TagName("check-verification-code")
	public interface Config extends PolymorphicConfiguration<CheckVerificationCodeAction> {

		@Override
		@ClassDefault(CheckVerificationCodeAction.class)
		Class<? extends CheckVerificationCodeAction> getImplementationClass();
	}

	/**
	 * Creates a new {@link CheckVerificationCodeAction} from configuration.
	 */
	@CalledByReflection
	public CheckVerificationCodeAction(InstantiationContext context, Config config) {
		// No configuration needed.
	}

	@Override
	public Object execute(ReactContext context, Object input) {
		if (!(input instanceof TLObject)) {
			return input;
		}
		Object requestValue = SelfServiceSupport.channel(context, AccountRequest.REQUEST_CHANNEL);
		Object codeValue = SelfServiceSupport.channel(context, AccountRequest.VERIFICATION_CHANNEL);
		if (!(requestValue instanceof AccountRequest) || !(codeValue instanceof VerificationCode)) {
			throw new TopLogicException(
				com.top_logic.security.selfservice.I18NConstants.ERROR_VERIFICATION_CODE_EXPIRED);
		}
		AccountRequest request = (AccountRequest) requestValue;
		((VerificationCode) codeValue).check(SelfServiceSupport.stringValue((TLObject) input, CODE_ATTR));
		request.checkOpen();

		openAccountDialog(context, request);
		return input;
	}

	/**
	 * Opens the code dialog in place of the top dialog.
	 *
	 * @param context
	 *        The context of the action that has mailed the code.
	 * @param request
	 *        The request the code proves the address of.
	 * @param code
	 *        The code that was mailed.
	 */
	public static void openCodeDialog(ReactContext context, AccountRequest request, VerificationCode code) {
		Map<String, Object> channels = new LinkedHashMap<>();
		channels.put(LoginAction.MODEL_CHANNEL, SelfServiceSupport.newTransient(CODE_ENTRY_TYPE));
		channels.put(AccountRequest.REQUEST_CHANNEL, request);
		channels.put(AccountRequest.VERIFICATION_CHANNEL, code);
		SelfServiceSupport.replaceTopDialog(context, CODE_VIEW, channels);
	}

	private static void openAccountDialog(ReactContext context, AccountRequest request) {
		TLObject account = SelfServiceSupport.newTransient(ACCOUNT_CREATION_TYPE);
		account.tUpdateByName(USER_NAME_ATTR, request.getEmail());

		Map<String, Object> channels = new LinkedHashMap<>();
		channels.put(LoginAction.MODEL_CHANNEL, account);
		channels.put(AccountRequest.REQUEST_CHANNEL, request);
		channels.put(PASSWORDS_CHANNEL,
			Boolean.valueOf(InvitationModule.getInstance().authenticationDevice().allowPwdChange()));
		SelfServiceSupport.replaceTopDialog(context, CREATE_ACCOUNT_VIEW, channels);
	}

	/**
	 * Replaces the code dialog with the account dialog.
	 */
	@Override
	public boolean changesViewState() {
		return true;
	}
}
