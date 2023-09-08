/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.action;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.top_logic.basic.Settings;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.scripting.action.ActionUtil;
import com.top_logic.layout.scripting.action.ApplicationAction;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;
import com.top_logic.util.Utils;
import com.top_logic.util.error.TopLogicException;

/**
 * Helper methods for {@link ApplicationActionOp} implementations that check
 * assertions.
 * 
 * <p>
 * In case of failure, {@link ApplicationAssertion} is thrown. 
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ApplicationAssertions {

	/**
	 * Assert that the given {@link HandlerResult} represents a success.
	 */
	public static void assertSuccess(ApplicationAction context, HandlerResult result) {
		if (result.isSuccess() || result.isSuspended()) {
			return;
		}
		TopLogicException exception = result.getException();
		if (exception != null) {
			fail(context, "Command failed.", exception);
		} else {
			List<ResKey> encodedErrors = result.getEncodedErrors();
			assertFalse(context, "HandlerResult is not a success but has neither an exception nor any errors set: "
				+ result,
				encodedErrors.isEmpty());
			fail(context, internationalize(encodedErrors));
		}
	}

	private static String internationalize(List<ResKey> encodedErrors) {
		Resources resources = Resources.getInstance();

		StringBuilder result = new StringBuilder();
		for (ResKey messageKey : encodedErrors) {
			if (result.length() > 0) {
				result.append(" ");
			}
			result.append(resources.decodeMessageFromKeyWithEncodedArguments(messageKey));
		}

		return result.toString();
	}

	public static void assertFalse(ConfigurationItem context, String message, boolean value) {
		if (value) {
			throw createFailure(context, message);
		}
	}

	public static void assertTrue(ConfigurationItem context, String message, boolean value) {
		if (! value) {
			throw createFailure(context, message);
		}
	}

	/**
	 * Null-save check for equality with {@link Object#equals(Object)}.
	 * 
	 * <p>
	 * The reported error message consists of the given message + " Expected: 'xxx'; Actual: 'yyy'"
	 * </p>
	 * 
	 * @param context
	 *        The failed {@link ApplicationAction}.
	 * @param message
	 *        Should neither be <code>null</code> nor empty! (But can be.)
	 * @param expected
	 *        Is allowed to be <code>null</code>.
	 * @param actual
	 *        Is allowed to be <code>null</code>.
	 */
	public static void assertEquals(ConfigurationItem context, String message, Object expected, Object actual) {
		if (!Utils.equals(expected, actual)) {
			throw createFailure(context, message + " Expected: " + StringServices.getObjectDescription(expected) + "; Actual: "
				+ StringServices.getObjectDescription(actual));
		}
	}

	/**
	 * Null-save check for equality with {@link Object#equals(Object)}.
	 * 
	 * <p>
	 * The reported error message consists of the given message + " Expected: 'xxx'; Actual: 'yyy'"
	 * </p>
	 * 
	 * <p>
	 * Moreover the content of the binary data dare stored in file system for port mortem analysis.
	 * The contents are stored in "&lt;applicationRoot&gt;/ApplicationAssertions/&lt;timestamp&gt;".
	 * </p>
	 * 
	 * @param context
	 *        The failed {@link ApplicationAction}.
	 * @param message
	 *        Should neither be <code>null</code> nor empty! (But can be.)
	 * @param expected
	 *        Is allowed to be <code>null</code>.
	 * @param actual
	 *        Is allowed to be <code>null</code>.
	 */
	public static void assertEquals(ConfigurationItem context, String message, BinaryData expected, BinaryData actual)
			throws IOException {
		if (!Utils.equals(expected, actual)) {
			File tempDir = Settings.getInstance().getTempDir();
			File baseFolder = new File(tempDir, ApplicationAssertions.class.getSimpleName());
			baseFolder.mkdirs();

			File checkFolder = newTestSpecificFolder(baseFolder);
			if (checkFolder != null) {
				writeData(checkFolder, expected, "expected_");
				writeData(checkFolder, actual, "actual_");
			}

			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append(message);
			stringBuilder.append(" Expected: ");
			stringBuilder.append(StringServices.getObjectDescription(expected));
			stringBuilder.append("; Actual: ");
			stringBuilder.append(StringServices.getObjectDescription(actual));
			if (checkFolder != null) {
				stringBuilder.append(" The contents are stored in ");
				stringBuilder.append(checkFolder.getCanonicalPath());
			}
			throw createFailure(context, stringBuilder.toString());
		}
	}

	/**
	 * Check that the given map is equal to the expected one.
	 * 
	 * <p>
	 * In contrast to {@link #assertEquals(ConfigurationItem, String, Object, Object)}, a detailed
	 * failure message is generated, if the maps doe not match at some position.
	 * </p>
	 * 
	 * @param context
	 *        The failed {@link ApplicationAction}.
	 * @param message
	 *        Should neither be <code>null</code> nor empty! (But can be.)
	 * @param expected
	 *        Is allowed to be <code>null</code>.
	 * @param actual
	 *        Is allowed to be <code>null</code>.
	 */
	public static void assertMapEquals(ConfigurationItem context, String message, Map<?, ?> expected, Map<?, ?> actual) {
		if (actual == null || expected == null) {
			assertEquals(context, message, expected, actual);
			return;
		}

		if (actual.equals(expected)) {
			return;
		}
	
		StringBuilder buffer = new StringBuilder(message);
		HashMap<Object, Object> actualMap = new HashMap<>(actual);
		for (Entry<?, ?> expectedEntry : expected.entrySet()) {
			Object expectedKey = expectedEntry.getKey();
			Object expectedValue = expectedEntry.getValue();
			if (actualMap.containsKey(expectedKey)) {
				Object actualValue = actualMap.remove(expectedKey);
				if (!Utils.equals(expectedValue, actualValue)) {
					buffer.append(" Missmatch for key '" + expectedKey + "', expected '" + expectedValue
						+ "', but found '" + actualValue + "'.");
				}
			} else {
				buffer.append(" Missing entry (" + expectedKey + ", " + expectedValue + ").");
			}
		}
		for (Entry<?, ?> actualEntry : actualMap.entrySet()) {
			Object actualKey = actualEntry.getKey();
			Object actualValue = actualEntry.getValue();
	
			buffer.append(" Unexpected entry (" + actualKey + ", " + actualValue + ").");
		}
		throw createFailure(context, buffer.toString());
	}

	private static File newTestSpecificFolder(File baseFolder) {
		int retry = 5;
		while (retry-- > 0) {
			File checkFolder = new File(baseFolder, "" + System.currentTimeMillis());
			if (checkFolder.mkdir()) {
				return checkFolder;
			}
			// Folder already exists. Create different one.
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ex) {
				// ignore.
			}
		}
		return null;
	}

	private static void writeData(File checkFolder, BinaryData expected, String string) throws IOException {
		FileOutputStream out = new FileOutputStream(new File(checkFolder, string + expected.getName()));
		try {
			InputStream in = expected.getStream();
			try {
				StreamUtilities.copyStreamContents(in, out);
			} finally {
				in.close();
			}
		} finally {
			out.close();
		}
	}

	public static void assertInstanceOf(ConfigurationItem context, String message, Class<?> expectedClass, Object actual) {
		if (!expectedClass.isInstance(actual)) {
			throw createFailure(context, message + " Expected instance of: " + expectedClass.getName() + "; Actual : "
				+ StringServices.getObjectDescription(actual));
		}
	}

	public static void assertNotNull(ConfigurationItem context, String message, Object value) {
		if (value == null) {
			throw createFailure(context, message);
		}
	}
	
	public static void assertNull(ConfigurationItem context, String message, Object value) {
		if (value != null) {
			throw createFailure(context, message);
		}
	}

	public static RuntimeException fail(ConfigurationItem context, String message) {
		throw createFailure(context, message);
	}

	public static RuntimeException fail(ConfigurationItem context, String message, Throwable cause) {
		throw createFailure(context, withCause(message, cause), cause);
	}
	
	private static String withCause(String message, Throwable cause) {
		if (cause == null) {
			return message;
		}
		return withoutDot(message) + ": " + message(cause);
	}

	private static String withoutDot(String message) {
		if (message.endsWith(".")) {
			return message.substring(0, message.length() - 1);
		}
		return message;
	}

	/**
	 * The message of the given {@link Throwable}, or it's class name, if no message is available.
	 */
	private static String message(Throwable ex) {
		String message = ex.getMessage();
		return message == null ? ex.getClass().getName() : message;
	}

	private static RuntimeException createFailure(ConfigurationItem context, String message) {
		return createFailure(context, message, null);
	}
	
	private static RuntimeException createFailure(ConfigurationItem context, String message, Throwable cause) {
		if (cause == null) {
			// Make sure not to initialize the cause, to be able to append the root cause in the
			// test executor.
			return new ApplicationAssertion(getCommentedMessage(context, message));
		} else {
			return new ApplicationAssertion(getCommentedMessage(context, message), cause);
		}
	}

	/**
	 * The given failure messages enhanced with the given action context.
	 * 
	 * @param context
	 *        The failed action.
	 * @param message
	 *        The detail failure message to enhance with the context.
	 * @return The enhanced message.
	 */
	public static String getCommentedMessage(ConfigurationItem context, String message) {
		if (context == null) {
			return message;
		}

		StringBuilder result = new StringBuilder();
		result.append(message);
	
		if (context instanceof ApplicationAction) {
			result.append("\nAction: ");
			result.append(ActionUtil.actionName((ApplicationAction) context));
		}
	
		result.append("\nAt ");
		result.append(context.location());
	
		return result.toString();
	}

}
