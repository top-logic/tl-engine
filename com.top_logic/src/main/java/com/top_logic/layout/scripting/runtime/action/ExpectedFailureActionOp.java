/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.action;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

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
		 * The message that must occur in the failure message when the {@link #getFailureAction()}
		 * fails.
		 *
		 * <p>
		 * By default, the value is matched as a literal substring of the failure message. If
		 * {@link #isRegexpMode()} is set, the value is instead interpreted as a regular expression
		 * that must be {@link java.util.regex.Matcher#find() found} somewhere in the failure message;
		 * in that case regex metacharacters (e.g. <code>. ( ) [ ] { } * + ? \ ^ $ |</code>) must be
		 * escaped to match literally. When empty, any failure is accepted.
		 * </p>
		 */
		String getExpectedFailureMessage();

		/**
		 * @see #getExpectedFailureMessage()
		 */
		void setExpectedFailureMessage(String value);

		/**
		 * Whether {@link #getExpectedFailureMessage()} is interpreted as a regular expression.
		 *
		 * <p>
		 * If not set (the default), {@link #getExpectedFailureMessage()} is matched as a literal
		 * substring of the failure message. If set, it is interpreted as a regular expression.
		 * </p>
		 */
		boolean isRegexpMode();

		/**
		 * @see #isRegexpMode()
		 */
		void setRegexpMode(boolean value);

	}

	private final ApplicationActionOp<?> _failureAction;

	private final String _expectedFailureMessage;

	private final Pattern _expectedFailurePattern;

	/**
	 * Ceates a new {@link ExpectedFailureActionOp}.
	 */
	public ExpectedFailureActionOp(InstantiationContext context, ExpectedFailureAction config) {
		super(context, config);
		_failureAction = context.getInstance(config.getFailureAction());
		_expectedFailureMessage = config.getExpectedFailureMessage() == null ? "" : config.getExpectedFailureMessage();
		_expectedFailurePattern = config.isRegexpMode() ? compilePattern(context, _expectedFailureMessage) : null;
	}

	private static Pattern compilePattern(InstantiationContext context, String regex) {
		try {
			return Pattern.compile(regex);
		} catch (PatternSyntaxException ex) {
			context.error("Invalid regular expression '" + regex + "' for 'expected-failure-message'.", ex);
			// Degrade to a literal match so that the action stays usable if the (already reported)
			// configuration error is ignored.
			return Pattern.compile(Pattern.quote(regex));
		}
	}

	@Override
	protected Object processInternal(ActionContext context, Object argument) throws Throwable {
		try {
			_failureAction.process(context, argument);
		} catch (ApplicationAssertion ex) {
			Throwable problem = ex;
			Set<String> allMessages = new LinkedHashSet<>();
			while (problem != null) {
				if (problem instanceof I18NFailure) {
					ResKey errorKey = ((I18NFailure) problem).getErrorKey();
					String i18nFailure = context.getDisplayContext().getResources().getString(errorKey);
					if (matches(i18nFailure)) {
						return argument;
					}
					allMessages.add(i18nFailure);
				}
				String failureMsg = problem.getMessage();
				if (matches(failureMsg)) {
					return argument;
				}
				if (failureMsg != null) {
					allMessages.add(failureMsg);
				}
				problem = problem.getCause();
			}

			ApplicationAssertions.fail(getConfig(),
				"Expected failure matching '" + _expectedFailureMessage + "', but was: "
					+ String.join(" ", allMessages));
		}
		ApplicationAssertions.fail(getConfig(),
			"Expected failure matching '" + _expectedFailureMessage + "', but action was successful.");
		return argument;
	}

	private boolean matches(String message) {
		if (message == null) {
			return false;
		}
		if (_expectedFailurePattern != null) {
			return _expectedFailurePattern.matcher(message).find();
		}
		return message.contains(_expectedFailureMessage);
	}

}

