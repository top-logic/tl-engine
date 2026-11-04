/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.login;

import java.util.LinkedHashMap;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.ReactContext;

/**
 * {@link LoginStep} forcing the change of an expired password in the change-password dialog.
 *
 * <p>
 * Applies when the password the account logged in with has expired or does not satisfy the
 * password policy any more. The step is finished by {@link ChangePasswordApplyAction} once the new
 * password is stored.
 * </p>
 *
 * @implNote Whether the password has expired is known only to the login entry that checked it,
 *           which marks the {@link LoginFlow} with {@link #PASSWORD_EXPIRED}. The dialog is
 *           {@link LoginAction#CHANGE_PASSWORD_VIEW}.
 */
public class ChangePasswordStep extends AbstractConfiguredInstance<ChangePasswordStep.Config<?>>
		implements LoginStep {

	/**
	 * Property of a {@link LoginFlow} telling that the password the account authenticated with has
	 * expired or does not satisfy the password policy any more.
	 */
	public static final Property<Boolean> PASSWORD_EXPIRED =
		TypedAnnotatable.property(Boolean.class, "passwordExpired", Boolean.FALSE);

	/**
	 * Configuration of a {@link ChangePasswordStep}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config<I extends ChangePasswordStep> extends LoginStep.Config<I> {

		/** Configuration tag of a {@link ChangePasswordStep}. */
		String TAG_NAME = "change-password-step";

		@Override
		@ClassDefault(ChangePasswordStep.class)
		Class<? extends I> getImplementationClass();
	}

	/**
	 * Creates a {@link ChangePasswordStep} from configuration.
	 */
	@CalledByReflection
	public ChangePasswordStep(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public boolean appliesTo(LoginFlow flow) {
		return flow.get(PASSWORD_EXPIRED).booleanValue();
	}

	/**
	 * Opens the change-password dialog with the account on its {@link LoginAction#ACCOUNT_CHANNEL}
	 * channel and a fresh transient {@link LoginAction#PASSWORD_CHANGE_TYPE} model on its
	 * {@link LoginAction#MODEL_CHANNEL} channel.
	 */
	@Override
	public void start(ReactContext context, LoginFlow flow) {
		Map<String, Object> channels = new LinkedHashMap<>();
		channels.put(LoginAction.MODEL_CHANNEL, LoginAction.newTransient(LoginAction.PASSWORD_CHANGE_TYPE));
		channels.put(LoginAction.ACCOUNT_CHANNEL, flow.getAccount());
		LoginFlow.replaceDialog(context, LoginAction.CHANGE_PASSWORD_VIEW, channels);
	}

}
