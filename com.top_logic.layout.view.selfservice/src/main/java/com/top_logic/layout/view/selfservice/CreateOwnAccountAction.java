/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.selfservice;

import java.util.Arrays;
import java.util.Objects;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.model.TLObject;
import com.top_logic.security.selfservice.invitation.InvitationModule;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link ViewAction} of the account dialog of an {@link AccountRequest}: it creates the account and
 * lets the request continue, e.g. log the new user in.
 *
 * <p>
 * The input is the {@link CheckVerificationCodeAction#ACCOUNT_CREATION_TYPE} the dialog edits, the
 * request comes from the {@link AccountRequest#REQUEST_CHANNEL} channel. A refusal - missing
 * surname, passwords that differ or violate the password rules, a request that can no longer be
 * continued, a callback refusing the account - is reported and leaves the dialog open.
 * </p>
 *
 * @implNote The account is created by {@link AccountRequest#createAccount(String, String, char[])},
 *           what follows is {@link AccountRequest#accountCreated(ReactContext, Person)}.
 */
@InApp
public class CreateOwnAccountAction implements ViewAction {

	/** Attribute of {@link CheckVerificationCodeAction#ACCOUNT_CREATION_TYPE} holding the surname. */
	public static final String SURNAME_ATTR = "surname";

	/** Attribute of {@link CheckVerificationCodeAction#ACCOUNT_CREATION_TYPE} holding the given name. */
	public static final String GIVEN_NAME_ATTR = "givenName";

	/** Attribute of {@link CheckVerificationCodeAction#ACCOUNT_CREATION_TYPE} holding the password. */
	public static final String NEW_PASSWORD_ATTR = "newPassword";

	/**
	 * Attribute of {@link CheckVerificationCodeAction#ACCOUNT_CREATION_TYPE} holding the repetition
	 * of the password.
	 */
	public static final String NEW_PASSWORD_CONFIRM_ATTR = "newPasswordConfirm";

	/**
	 * Configuration for {@link CreateOwnAccountAction}.
	 */
	@TagName("create-own-account")
	public interface Config extends PolymorphicConfiguration<CreateOwnAccountAction> {

		@Override
		@ClassDefault(CreateOwnAccountAction.class)
		Class<? extends CreateOwnAccountAction> getImplementationClass();
	}

	/**
	 * Creates a new {@link CreateOwnAccountAction} from configuration.
	 */
	@CalledByReflection
	public CreateOwnAccountAction(InstantiationContext context, Config config) {
		// No configuration needed.
	}

	@Override
	public Object execute(ReactContext context, Object input) {
		if (!(input instanceof TLObject)) {
			return input;
		}
		Object requestValue = SelfServiceSupport.channel(context, AccountRequest.REQUEST_CHANNEL);
		if (!(requestValue instanceof AccountRequest)) {
			throw new TopLogicException(
				com.top_logic.security.selfservice.I18NConstants.ERROR_VERIFICATION_CODE_EXPIRED);
		}
		AccountRequest request = (AccountRequest) requestValue;
		TLObject form = (TLObject) input;

		String surname = SelfServiceSupport.stringValue(form, SURNAME_ATTR);
		if (StringServices.isEmpty(surname)) {
			throw new TopLogicException(I18NConstants.SURNAME_EMPTY);
		}
		String givenName = SelfServiceSupport.stringValue(form, GIVEN_NAME_ATTR);

		char[] password = null;
		if (InvitationModule.getInstance().authenticationDevice().allowPwdChange()) {
			String newPassword = SelfServiceSupport.stringValue(form, NEW_PASSWORD_ATTR);
			String newPasswordConfirm = SelfServiceSupport.stringValue(form, NEW_PASSWORD_CONFIRM_ATTR);
			if (!Objects.equals(newPassword, newPasswordConfirm)) {
				throw new TopLogicException(I18NConstants.PASSWORD_MISMATCH);
			}
			password = StringServices.nonNull(newPassword).toCharArray();
		}

		Person account;
		try {
			account = request.createAccount(surname.trim(), trimmed(givenName), password);
		} finally {
			if (password != null) {
				Arrays.fill(password, (char) 0);
			}
		}

		request.accountCreated(context, account);
		return input;
	}

	private static String trimmed(String value) {
		return value == null ? null : value.trim();
	}

	/**
	 * Logs the new user in, opens the dialog of a login step, or closes the dialog.
	 */
	@Override
	public boolean changesViewState() {
		return true;
	}
}
