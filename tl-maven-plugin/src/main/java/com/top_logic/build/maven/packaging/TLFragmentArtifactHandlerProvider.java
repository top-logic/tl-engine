/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.build.maven.packaging;

import javax.inject.Named;
import javax.inject.Singleton;

/**
 * The artifact handler of the packaging {@link TLPackaging#FRAGMENT_ID} of TopLogic fragment modules.
 */
@Named(TLPackaging.FRAGMENT_ID)
@Singleton
public class TLFragmentArtifactHandlerProvider extends TLArtifactHandlerProvider {

	/**
	 * Creates a {@link TLFragmentArtifactHandlerProvider}.
	 */
	public TLFragmentArtifactHandlerProvider() {
		super(TLPackaging.FRAGMENT);
	}
}
