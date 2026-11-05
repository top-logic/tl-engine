/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.login;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.knowledge.wrap.person.MfaRequirement;
import com.top_logic.layout.react.ReactContext;

/**
 * {@link LoginStep} confirming the account with one of its {@link SecondFactor}s, or setting one up
 * for an account that is {@link MfaRequirement#REQUIRED required} to use one.
 *
 * <p>
 * Applies unless the account may log in without a second factor, or the login has proven the
 * account with more than a password already - a passwordless passkey login proves more than a
 * password and a second factor together. The dialogs of the methods finish the step once the
 * account has proven itself.
 * </p>
 *
 * @implNote The decision is {@link SecondFactorStep#decide(com.top_logic.knowledge.wrap.person.Person)};
 *           a login entry that has proven the account marks the {@link LoginFlow} with
 *           {@link #PROVEN}. The dialogs finish the step through
 *           {@link LoginFlow#finish(ReactContext, Class, com.top_logic.knowledge.wrap.person.Person)}
 *           with this class.
 */
public class SecondFactorLoginStep extends AbstractConfiguredInstance<SecondFactorLoginStep.Config<?>>
		implements LoginStep {

	/**
	 * Property of a {@link LoginFlow} telling that the login entry has proven the account with more
	 * than a password already, so that no second factor is asked for.
	 */
	public static final Property<Boolean> PROVEN =
		TypedAnnotatable.property(Boolean.class, "secondFactorProven", Boolean.FALSE);

	/**
	 * Configuration of a {@link SecondFactorLoginStep}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config<I extends SecondFactorLoginStep> extends LoginStep.Config<I> {

		/** Configuration tag of a {@link SecondFactorLoginStep}. */
		String TAG_NAME = "second-factor-step";

		@Override
		@ClassDefault(SecondFactorLoginStep.class)
		Class<? extends I> getImplementationClass();
	}

	/**
	 * Creates a {@link SecondFactorLoginStep} from configuration.
	 */
	@CalledByReflection
	public SecondFactorLoginStep(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	/**
	 * Whether a second factor is to be confirmed or set up.
	 *
	 * @throws com.top_logic.util.error.TopLogicException
	 *         If the account is required to use a second factor but may set up none, which refuses
	 *         the login.
	 */
	@Override
	public boolean appliesTo(LoginFlow flow) {
		if (flow.get(PROVEN).booleanValue()) {
			return false;
		}
		return decide(flow).getKind() != SecondFactorStep.Kind.COMPLETE;
	}

	@Override
	public void start(ReactContext context, LoginFlow flow) {
		decide(flow).perform(context, flow.getAccount());
	}

	/**
	 * The decision for the account of the given flow.
	 */
	protected SecondFactorStep decide(LoginFlow flow) {
		return SecondFactorStep.decide(flow.getAccount());
	}

}
