/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.action;

import java.util.LinkedHashSet;
import java.util.Set;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.exception.I18NFailure;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.scripting.action.ApplicationAction;
import com.top_logic.layout.scripting.runtime.ActionContext;

/**
 * {@link ApplicationActionOp} that triggers an inner action and expects that the action fails.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ExpectedFailureActionOp
		extends AbstractApplicationActionOp<ExpectedFailureActionOp.ExpectedFailureAction> {

	/**
	 * Configuration of an {@link ExpectedFailureActionOp}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface ExpectedFailureAction extends ApplicationAction {

		/**
		 * The {@link ApplicationAction} expected to fail.
		 */
		@Mandatory
		ApplicationAction getFailureAction();

		/**
		 * @see #getFailureAction()
		 */
		void setFailureAction(ApplicationAction value);

		/**
		 * A part of the failure message when the {@link #getFailureAction()} fails.
		 */
		String getExpectedFailureMessage();

		/**
		 * @see #getExpectedFailureMessage()
		 */
		void setExpectedFailureMessage(String value);

	}

	private final ApplicationActionOp<?> _failureAction;

	/**
	 * Ceates a new {@link ExpectedFailureActionOp}.
	 */
	public ExpectedFailureActionOp(InstantiationContext context, ExpectedFailureAction config) {
		super(context, config);
		_failureAction = context.getInstance(config.getFailureAction());
	}

	@Override
	protected Object processInternal(ActionContext context, Object argument) throws Throwable {
		String expectedFailureMsg = getConfig().getExpectedFailureMessage();
		try {
			_failureAction.process(context, argument);
		} catch (ApplicationAssertion ex) {
			Throwable problem = ex;
			Set<String> allMessages = new LinkedHashSet<>();
			while (problem != null) {
				if (problem instanceof I18NFailure) {
					ResKey errorKey = ((I18NFailure) problem).getErrorKey();
					String i18nFailure = context.getDisplayContext().getResources().getString(errorKey);
					if (i18nFailure.contains(expectedFailureMsg)) {
						return argument;
					}
					allMessages.add(i18nFailure);
				}
				String failureMsg = problem.getMessage();
				if (failureMsg.contains(expectedFailureMsg)) {
					return argument;
				}
				allMessages.add(failureMsg);
				problem = problem.getCause();
			}

			ApplicationAssertions.fail(getConfig(),
				"Expected failure contains '" + expectedFailureMsg + "', but was: " + String.join(" ", allMessages));
		}
		ApplicationAssertions.fail(getConfig(),
			"Expected failure containing '" + expectedFailureMsg + "', but action was successful.");
		return argument;
	}

}

