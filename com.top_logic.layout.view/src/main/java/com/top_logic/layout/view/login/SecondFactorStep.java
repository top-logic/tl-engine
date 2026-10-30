/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.login;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.knowledge.wrap.person.MfaRequirement;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.util.error.TopLogicException;

/**
 * What the React login does with an account whose password has been checked: complete the login,
 * confirm the account with one of its {@link SecondFactor}s, or set one up.
 *
 * <p>
 * {@link #decide(Person, List)} chooses among the methods:
 * </p>
 * <ul>
 * <li>Methods the account {@link SecondFactor#isEnrolled(Person) has set up}: the account confirms
 * itself with one of them ({@link Kind#VERIFY}), whatever its {@link MfaRequirement} says.</li>
 * <li>None set up, and the account is {@link MfaRequirement#REQUIRED required} to use a second
 * factor: it sets up one of the methods it {@link SecondFactor#canEnroll(Person) may set up}
 * ({@link Kind#ENROLL}). Without such a method the login is refused.</li>
 * <li>Otherwise the password alone completes the login ({@link Kind#COMPLETE}).</li>
 * </ul>
 *
 * <p>
 * {@link #perform(ReactContext, Person)} carries the decision out. A single method opens its step
 * directly; several are offered in the {@link #CHOICE_VIEW choice dialog}, which lists them through
 * {@link com.top_logic.layout.view.element.SecondFactorChoiceElement}.
 * </p>
 */
public final class SecondFactorStep {

	/**
	 * Dialog view (relative to {@link com.top_logic.layout.view.ViewLoader#VIEW_BASE_PATH}) in which
	 * the user chooses among several second-factor methods.
	 */
	public static final String CHOICE_VIEW = "second-factor-choice.view.xml";

	/**
	 * What a {@link SecondFactorStep} does.
	 */
	public enum Kind {

		/** The password alone completes the login. */
		COMPLETE,

		/** The account confirms itself with one of the {@link SecondFactorStep#getMethods() methods}. */
		VERIFY,

		/** The account sets up one of the {@link SecondFactorStep#getMethods() methods}. */
		ENROLL;
	}

	private static final SecondFactorStep COMPLETE = new SecondFactorStep(Kind.COMPLETE, Collections.emptyList());

	private final Kind _kind;

	private final List<SecondFactor> _methods;

	private SecondFactorStep(Kind kind, List<SecondFactor> methods) {
		_kind = kind;
		_methods = methods;
	}

	/**
	 * What this step does.
	 */
	public Kind getKind() {
		return _kind;
	}

	/**
	 * The methods to choose from, in configuration order; empty for {@link Kind#COMPLETE}.
	 */
	public List<SecondFactor> getMethods() {
		return _methods;
	}

	/**
	 * Carries this step out for the given account.
	 *
	 * <p>
	 * Completes the login, opens the step of the only method, or replaces the current dialog with the
	 * {@link #CHOICE_VIEW choice dialog}, which receives the account on its
	 * {@link LoginAction#ACCOUNT_CHANNEL} channel.
	 * </p>
	 *
	 * @param context
	 *        The context of the dialog the login is in.
	 * @param account
	 *        The account whose password has been checked.
	 */
	public void perform(ReactContext context, Person account) {
		switch (_kind) {
			case COMPLETE:
				LoginAction.completeLogin(context, account.getName());
				return;
			case VERIFY:
			case ENROLL:
				if (_methods.size() == 1) {
					start(context, account, _methods.get(0));
				} else {
					LoginAction.replaceDialog(context, CHOICE_VIEW, Map.of(LoginAction.ACCOUNT_CHANNEL, account));
				}
				return;
		}
		throw new IllegalStateException("Unknown step: " + _kind);
	}

	/**
	 * Opens the step of the given method for the given account: its verification for
	 * {@link Kind#VERIFY}, its set-up for {@link Kind#ENROLL}.
	 *
	 * @param context
	 *        The context of the dialog the login is in.
	 * @param account
	 *        The account whose password has been checked.
	 * @param method
	 *        One of the {@link #getMethods() methods} of this step.
	 */
	public void start(ReactContext context, Person account, SecondFactor method) {
		switch (_kind) {
			case VERIFY:
				method.verify(context, account);
				return;
			case ENROLL:
				method.enroll(context, account);
				return;
			case COMPLETE:
				break;
		}
		throw new IllegalStateException("No method to start for step: " + _kind);
	}

	/**
	 * Decides what the login does with the given account, using the {@link #configured()} methods.
	 *
	 * @see #decide(Person, List)
	 */
	public static SecondFactorStep decide(Person account) {
		return decide(account, configured());
	}

	/**
	 * Decides what the login does with the given account, see the class description.
	 *
	 * @param account
	 *        The account whose password has been checked.
	 * @param methods
	 *        The methods to choose from.
	 * @return The step to take, never <code>null</code>.
	 * @throws TopLogicException
	 *         If the account is required to use a second factor but has none set up and may set up
	 *         none of the methods.
	 */
	public static SecondFactorStep decide(Person account, List<? extends SecondFactor> methods) {
		List<SecondFactor> enrolled = new ArrayList<>();
		for (SecondFactor method : methods) {
			if (method.isEnrolled(account)) {
				enrolled.add(method);
			}
		}
		if (!enrolled.isEmpty()) {
			return new SecondFactorStep(Kind.VERIFY, Collections.unmodifiableList(enrolled));
		}
		if (account.getMFARequirement() != MfaRequirement.REQUIRED) {
			return COMPLETE;
		}

		List<SecondFactor> enrollable = new ArrayList<>();
		for (SecondFactor method : methods) {
			if (method.canEnroll(account)) {
				enrollable.add(method);
			}
		}
		if (enrollable.isEmpty()) {
			throw new TopLogicException(I18NConstants.ERROR_NO_SECOND_FACTOR__USER.fill(account.getName()));
		}
		return new SecondFactorStep(Kind.ENROLL, Collections.unmodifiableList(enrollable));
	}

	/**
	 * The methods listed in the {@link SecondFactorsConfig}, in configuration order.
	 */
	public static List<SecondFactor> configured() {
		SecondFactorsConfig config = ApplicationConfig.getInstance().getConfig(SecondFactorsConfig.class);
		DefaultInstantiationContext context = new DefaultInstantiationContext(SecondFactorStep.class);
		List<SecondFactor> result = new ArrayList<>(config.getMethods().size());
		for (SecondFactor.Config<?> methodConfig : config.getMethods()) {
			SecondFactor method = context.getInstance(methodConfig);
			if (method != null) {
				result.add(method);
			}
		}
		return result;
	}

}
