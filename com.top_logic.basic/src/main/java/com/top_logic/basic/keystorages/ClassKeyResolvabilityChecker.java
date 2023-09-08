/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.keystorages;

import com.top_logic.basic.DefaultTypeKeyProvider;
import com.top_logic.basic.config.ConfigurationException;

/**
 * {@link KeyStorageChecker} checking that all Java {@link Class}es used in keys can be resolved.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class ClassKeyResolvabilityChecker implements KeyStorageChecker {

	private static final String CLASS_NAME_PREFIX = DefaultTypeKeyProvider.CLASS_NAME_PREFIX;

	/** The instance of the {@link ClassKeyResolvabilityChecker}. */
	public static final ClassKeyResolvabilityChecker INSTANCE = new ClassKeyResolvabilityChecker();

	@Override
	public Exception check(String key, String value) {
		if (!key.startsWith(CLASS_NAME_PREFIX)) {
			return null;
		}
		int prefix = CLASS_NAME_PREFIX.length();
		return check(key.substring(prefix));
	}

	private Exception check(String className) {
		ClassNotFoundException problem = tryResolve(className);
		if (problem == null) {
			return null;
		}
		return createError(className, problem);
	}

	private Exception createError(String text, ClassNotFoundException problem) {
		String message = "The entry seems to be a class-name, but cannot be resolved."
			+ " Class: " + text + ". Cause: " + problem.getMessage();
		return new ConfigurationException(message, problem);
	}

	private ClassNotFoundException tryResolve(String className) {
		try {
			Class.forName(className);
			return null;
		} catch (ClassNotFoundException ex) {
			return ex;
		}
	}

}
