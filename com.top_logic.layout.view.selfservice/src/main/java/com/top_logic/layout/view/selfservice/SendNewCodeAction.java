/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.selfservice;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.security.selfservice.invitation.VerificationCode;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link ViewAction} of the code dialog of an {@link AccountRequest} asking for a new
 * {@link VerificationCode}.
 *
 * <p>
 * The request on the {@link AccountRequest#REQUEST_CHANNEL} channel decides what happens: an
 * invitation mails a new code to its address, a registration leads back to the dialog requesting
 * the code, since a new code needs a new captcha.
 * </p>
 *
 * @implNote Calls {@link AccountRequest#sendNewCode(ReactContext)}.
 */
@InApp
public class SendNewCodeAction implements ViewAction {

	/**
	 * Configuration for {@link SendNewCodeAction}.
	 */
	@TagName("send-new-code")
	public interface Config extends PolymorphicConfiguration<SendNewCodeAction> {

		@Override
		@ClassDefault(SendNewCodeAction.class)
		Class<? extends SendNewCodeAction> getImplementationClass();
	}

	/**
	 * Creates a new {@link SendNewCodeAction} from configuration.
	 */
	@CalledByReflection
	public SendNewCodeAction(InstantiationContext context, Config config) {
		// No configuration needed.
	}

	@Override
	public Object execute(ReactContext context, Object input) {
		Object requestValue = SelfServiceSupport.channel(context, AccountRequest.REQUEST_CHANNEL);
		if (!(requestValue instanceof AccountRequest)) {
			throw new TopLogicException(
				com.top_logic.security.selfservice.I18NConstants.ERROR_VERIFICATION_CODE_EXPIRED);
		}
		((AccountRequest) requestValue).sendNewCode(context);
		return input;
	}

	/**
	 * Replaces the code dialog.
	 */
	@Override
	public boolean changesViewState() {
		return true;
	}
}
