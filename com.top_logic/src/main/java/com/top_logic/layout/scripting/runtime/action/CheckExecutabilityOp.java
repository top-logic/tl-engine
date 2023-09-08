/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.action;

import static com.top_logic.layout.scripting.runtime.action.ApplicationAssertions.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.scripting.action.CheckExecutability;
import com.top_logic.layout.scripting.action.ReasonKeyAssertion;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.mig.html.layout.CommandDispatcher;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutableState;

/**
 * {@link ApplicationActionOp} that executes a given command and checks for a potential
 * noExecutability reason.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CheckExecutabilityOp extends AbstractCommandActionOp<CheckExecutability> {

	/**
	 * Creates a {@link CheckExecutabilityOp} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public CheckExecutabilityOp(InstantiationContext context, CheckExecutability config) {
		super(context, config);
		checkOnlyOneErrorType(config);
	}

	private static void checkOnlyOneErrorType(CheckExecutability config) {
		boolean hasError = hasReasonKey(config);
		hasError &= hasReasonKeys(config);
		if (hasError) {
			throw new ConfigurationError("Must not have more than one errors: reason-key, reason-keys, contained-keys");
		}
		hasError &= hasContainedKeys(config);
		if (hasError) {
			throw new ConfigurationError("Must not have more than one errors: reason-key, reason-keys, contained-keys");
		}
	}

	private static boolean hasContainedKeys(ReasonKeyAssertion config) {
		return !config.getContainedKeys().isEmpty();
	}

	private static boolean hasReasonKeys(ReasonKeyAssertion config) {
		return !config.getReasonKeys().isEmpty();
	}

	private static boolean hasReasonKey(ReasonKeyAssertion config) {
		return !StringServices.isEmpty(config.getReasonKey());
	}

	@Override
	protected Object process(ActionContext context, LayoutComponent component, Object argument) {
		CommandHandler command = getCommandHandler(context, component);
		Map<String, Object> arguments = getArguments(context, command, component);
		DisplayContext executionContext = context.getDisplayContext();

		ExecutableState state = command.isExecutable(component, arguments);
		if (state.isExecutable() && noErrorExpected()) {
			// This is OK, do not actually execute the command.
		} else {
			// Go ahead and check the result. If the command reports not to be executable, the
			// executable state could be checked without actually triggering the command. But a
			// command that reports to be executable must be executed to allow checking potential
			// errors that are reported only during command execution.
			// reports to be executable, but will produce an error, if it's actually executed.
			checkExecutionResult(executionContext, component, command, arguments);
		}

		return argument;
	}

	private void checkExecutionResult(DisplayContext executionContext, LayoutComponent component,
			CommandHandler command, Map<String, Object> arguments) {
		HandlerResult result =
			CommandDispatcher.getInstance().dispatchCommand(command, CommandDispatcher.approved(executionContext),
				component, arguments);
		checkResult(result);
	}

	@Override
	protected void checkResult(HandlerResult result) {
		if (noErrorExpected()) {
			super.checkResult(result);
		} else {
			checkErrors(getReasonKeys(result), config);
		}
	}

	/**
	 * Checks that the actual errors match the given config.
	 */
	public static void checkErrors(List<String> actualErrors, ReasonKeyAssertion config) {
		if (hasReasonKey(config)) {
			assertExactlyOneError(actualErrors, config);
			assertExpectedError(actualErrors, config);
			return;
		}
		if (hasReasonKeys(config)) {
			assertExpectedErrors(actualErrors, config);
			return;
		}
		if (hasContainedKeys(config)) {
			assertContainsExpectedErrors(actualErrors, config);
			return;
		}
	}

	private static void assertContainsExpectedErrors(List<String> encodedErrors, ReasonKeyAssertion config) {
		Set<String> expectedReasonKeys = new HashSet<>(config.getContainedKeys());
		expectedReasonKeys.removeAll(encodedErrors);
		if (!expectedReasonKeys.isEmpty()) {
			ApplicationAssertions.fail(config, "Expected all errors! Missing: '" + expectedReasonKeys + "'");
		}
	}

	private static void assertExpectedErrors(List<String> actualErrorMessages, ReasonKeyAssertion config) {
		Set<String> expectedReasonKeys = config.getReasonKeys();
		if (!expectedReasonKeys.equals(CollectionUtil.toSet(actualErrorMessages))) {
			ApplicationAssertions.fail(config, "Expected different errors! Expected: '" + expectedReasonKeys
				+ "'; Actual: '" + actualErrorMessages + "'");
		}
	}

	private boolean noErrorExpected() {
		return !hasReasonKey(config) && !hasReasonKeys(config) && !hasContainedKeys(config);
	}

	private static void assertExactlyOneError(List<String> actualEncodedErrors, ReasonKeyAssertion config) {
		String expectedReasonKey = config.getReasonKey();

		String actualErrorMessages = actualEncodedErrors.toString();
		int numerOfErrors = actualEncodedErrors.size();
		String failureMessageWrongNumberOfErrors = "Expected exactly one error but got " + numerOfErrors
			+ " errors! Expected error: '" + expectedReasonKey + "'; Actual errors: '" + actualErrorMessages + "'";
		assertEquals(config, failureMessageWrongNumberOfErrors, 1, numerOfErrors);
	}

	private static void assertExpectedError(List<String> encodedErrors, ReasonKeyAssertion config) {
		String expectedReasonKey = config.getReasonKey();
		String actualErrorMessage = encodedErrors.get(0);
		String failureMessageWrongError = "Expected a different error! Expected: '" + expectedReasonKey
			+ "'; Actual: '" + actualErrorMessage + "'";
		assertEquals(config, failureMessageWrongError, expectedReasonKey, actualErrorMessage);
	}

	private List<String> getReasonKeys(HandlerResult result) {
		List<ResKey> actualEncodedErrors = result.getEncodedErrors();

		I18NRuntimeException exception = result.getException();
		if (exception != null) {
			actualEncodedErrors = new ArrayList<>(actualEncodedErrors);
			ResKey reasonKey = exception.getErrorKey();
			actualEncodedErrors.add(reasonKey);
		}
		return keys(actualEncodedErrors);
	}

	private List<String> keys(List<ResKey> keys) {
		ArrayList<String> result = new ArrayList<>(keys.size());
		for (ResKey key : keys) {
			result.add(key.direct().plain().toString());
		}
		return result;
	}

}
