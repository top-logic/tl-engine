/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.remote.shared;

/**
 * {@link TypeNaming} based on the Java {@link Class} name.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ClassTypeNaming implements TypeNaming {

	/**
	 * Singleton {@link ClassTypeNaming} instance.
	 */
	public static final ClassTypeNaming INSTANCE = new ClassTypeNaming();

	private ClassTypeNaming() {
		// Singleton constructor.
	}

	@Override
	public String networkType(ObjectData obj) {
		return obj.handle().getClass().getName();
	}

}
