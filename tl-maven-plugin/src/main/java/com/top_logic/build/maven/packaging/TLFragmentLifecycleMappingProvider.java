/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.build.maven.packaging;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.lifecycle.mapping.LifecycleMapping;

/**
 * The lifecycle mapping of the packaging {@link TLPackaging#FRAGMENT_ID} of TopLogic fragment modules.
 */
@Named(TLPackaging.FRAGMENT_ID)
@Singleton
public class TLFragmentLifecycleMappingProvider extends JarLifecycleMappingProvider {

	/**
	 * Creates a {@link TLFragmentLifecycleMappingProvider}.
	 */
	@Inject
	public TLFragmentLifecycleMappingProvider(@Named(JAR) LifecycleMapping jarMapping) {
		super(jarMapping);
	}
}
