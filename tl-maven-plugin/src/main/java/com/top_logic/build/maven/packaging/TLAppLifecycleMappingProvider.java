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
 * The lifecycle mapping of the packaging {@link TLPackaging#APP_ID} of TopLogic applications.
 */
@Named(TLPackaging.APP_ID)
@Singleton
public class TLAppLifecycleMappingProvider extends JarLifecycleMappingProvider {

	/**
	 * Creates a {@link TLAppLifecycleMappingProvider}.
	 */
	@Inject
	public TLAppLifecycleMappingProvider(@Named(JAR) LifecycleMapping jarMapping) {
		super(jarMapping);
	}
}
