/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.build.maven.packaging;

import javax.inject.Provider;

import org.apache.maven.lifecycle.mapping.LifecycleMapping;

/**
 * Base class for the lifecycle mapping of a {@link TLPackaging}, which binds the same plugins to
 * the phases as the <code>jar</code> packaging of the running Maven.
 */
public abstract class JarLifecycleMappingProvider implements Provider<LifecycleMapping> {

	/**
	 * The role hint of the lifecycle mapping of the <code>jar</code> packaging.
	 */
	public static final String JAR = "jar";

	private final LifecycleMapping _jarMapping;

	/**
	 * Creates a {@link JarLifecycleMappingProvider}.
	 *
	 * @param jarMapping
	 *        The lifecycle mapping of the <code>jar</code> packaging.
	 */
	protected JarLifecycleMappingProvider(LifecycleMapping jarMapping) {
		_jarMapping = jarMapping;
	}

	@Override
	public LifecycleMapping get() {
		return _jarMapping;
	}
}
