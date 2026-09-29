/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.selfservice;

import java.util.Arrays;

import com.top_logic.base.security.device.interfaces.AuthenticationDevice;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.overlay.DialogManager;
import com.top_logic.layout.react.control.overlay.DialogResult;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.model.TLObject;
import com.top_logic.security.selfservice.invitation.InvitationModule;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link ViewAction} that completes the self-service password reset: it verifies the code entered in
 * the reset dialog against the one {@link RequestPasswordResetAction} generated (and within its
 * validity), then applies the new password to the account.
 *
 * <p>
 * On success the dialog closes and the user is told they can log in with the new password - the
 * reset does <em>not</em> log the user in (unlike the regular login follow-up steps).
 * </p>
 *
 * @implNote Reads the expected code and its creation time from the
 *           {@link RequestPasswordResetAction#EXPECTED_CODE_CHANNEL} /
 *           {@link RequestPasswordResetAction#CREATED_AT_CHANNEL} channels; the code validity comes
 *           from {@link InvitationModule}. The password is applied via
 *           {@link AuthenticationDevice#setPassword(Person, char[])}.
 */
public class ApplyPasswordResetAction implements ViewAction {

	/**
	 * Configuration for {@link ApplyPasswordResetAction}.
	 */
	@TagName("apply-password-reset")
	public interface Config extends PolymorphicConfiguration<ApplyPasswordResetAction> {

		@Override
		@ClassDefault(ApplyPasswordResetAction.class)
		Class<? extends ApplyPasswordResetAction> getImplementationClass();
	}

	/**
	 * Creates a new {@link ApplyPasswordResetAction} from configuration.
	 */
	@CalledByReflection
	public ApplyPasswordResetAction(InstantiationContext context, Config config) {
		// No configuration needed.
	}

	@Override
	public Object execute(ReactContext context, Object input) {
		if (!(input instanceof TLObject)) {
			return input;
		}
		TLObject form = (TLObject) input;
		String enteredCode = asString(form.tValueByName("code"));
		String expectedCode = asString(channel(context, RequestPasswordResetAction.EXPECTED_CODE_CHANNEL));
		Long createdAt = (Long) channel(context, RequestPasswordResetAction.CREATED_AT_CHANNEL);

		if (!isValidCode(enteredCode, expectedCode, createdAt)) {
			throw new TopLogicException(I18NConstants.RESET_CODE_INVALID);
		}

		String newPassword = asString(form.tValueByName("newPassword"));
		String newPasswordConfirm = asString(form.tValueByName("newPasswordConfirm"));
		if (!equalPasswords(newPassword, newPasswordConfirm)) {
			throw new TopLogicException(I18NConstants.PASSWORD_MISMATCH);
		}
		if (isEmpty(newPassword)) {
			throw new TopLogicException(I18NConstants.PASSWORD_EMPTY);
		}

		Object accountValue = channel(context, RequestPasswordResetAction.ACCOUNT_CHANNEL);
		if (!(accountValue instanceof Person)) {
			throw new TopLogicException(I18NConstants.RESET_PASSWORD_FAILED);
		}
		Person account = (Person) accountValue;

		char[] password = newPassword.toCharArray();
		try {
			AuthenticationDevice device = account.getAuthenticationDevice();
			if (device == null || !device.allowPwdChange()) {
				throw new TopLogicException(I18NConstants.RESET_PASSWORD_FAILED);
			}
			try (Transaction tx = account.tKnowledgeBase()
				.beginTransaction(I18NConstants.RESET_PASSWORD__USER.fill(account.getName()))) {
				device.setPassword(account, password);
				tx.commit();
			}
		} finally {
			Arrays.fill(password, (char) 0);
		}

		DialogManager dialogManager = context.getDialogManager();
		if (dialogManager != null) {
			dialogManager.closeTopDialog(DialogResult.cancelled());
		}
		InfoService.showInfo(I18NConstants.RESET_PASSWORD_SUCCESS);
		return input;
	}

	private static boolean isValidCode(String enteredCode, String expectedCode, Long createdAt) {
		if (isEmpty(enteredCode) || isEmpty(expectedCode) || createdAt == null) {
			return false;
		}
		long validity = InvitationModule.getInstance().getConfig().getCodeValidity();
		if (System.currentTimeMillis() > createdAt.longValue() + validity) {
			return false;
		}
		return expectedCode.equals(enteredCode);
	}

	private static Object channel(ReactContext context, String name) {
		if (!(context instanceof ViewContext)) {
			return null;
		}
		ViewContext viewContext = (ViewContext) context;
		if (!viewContext.hasChannel(name)) {
			return null;
		}
		return viewContext.resolveChannel(new ChannelRef(name)).get();
	}

	private static boolean equalPasswords(String a, String b) {
		return a == null ? b == null : a.equals(b);
	}

	private static String asString(Object value) {
		return value == null ? null : value.toString();
	}

	private static boolean isEmpty(String value) {
		return value == null || value.isEmpty();
	}

}
