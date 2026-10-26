/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.build.maven.packaging;

import javax.inject.Provider;

import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;

/**
 * Base class for the artifact handler of a {@link TLPackaging}.
 *
 * <p>
 * The main artifact is a Java jar on the class path, as for the <code>jar</code> packaging, so
 * that dependent projects reference it by a dependency without type.
 * </p>
 */
public abstract class TLArtifactHandlerProvider implements Provider<ArtifactHandler> {

	private final ArtifactHandler _handler;

	/**
	 * Creates a {@link TLArtifactHandlerProvider}.
	 *
	 * @param packaging
	 *        The packaging whose artifacts are handled.
	 */
	protected TLArtifactHandlerProvider(TLPackaging packaging) {
		// The packaging of the handler defaults to its type.
		DefaultArtifactHandler handler = new DefaultArtifactHandler(packaging.getId());
		handler.setExtension(TLPackaging.ARTIFACT_EXTENSION);
		handler.setLanguage(TLPackaging.ARTIFACT_LANGUAGE);
		handler.setAddedToClasspath(true);
		_handler = handler;
	}

	@Override
	public ArtifactHandler get() {
		return _handler;
	}
}
