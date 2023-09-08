/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.remote.shared;

/**
 * {@link TypeNaming} based on the Java super {@link Class} common to both the server and the client.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CommonSuperClassNaming implements TypeNaming {

	private static final String COMMON_PACKAGE_PREFIX = "com.top_logic.graph.common.";

	/** The instance of the {@link CommonSuperClassNaming}. */
	public static final CommonSuperClassNaming INSTANCE = new CommonSuperClassNaming();

	@Override
	public String networkType(ObjectData obj) {
		Object handle = obj.handle();
		if (handle instanceof Enum<?>) {
			Enum<?> literal = (Enum<?>) handle;
			return literal.getDeclaringClass().getName() + "#" + literal.name();
		}
		Class<?> handleType = handle.getClass();
		Class<?> commonType = getCommonType(handleType);
		return commonType.getName();
	}

	/**
	 * Finds the most specific {@link Class#getSuperclass() super class} that is common to both the
	 * client and the server.
	 */
	private Class<?> getCommonType(Class<?> type) {
		if (isCommonType(type)) {
			return type;
		}
		if (type.getSuperclass() == null) {
			throw new IllegalArgumentException("There is no common super type.");
		}
		return getCommonType(type.getSuperclass());
	}

	private boolean isCommonType(Class<?> type) {
		return type.getCanonicalName().startsWith(COMMON_PACKAGE_PREFIX);
	}

}
