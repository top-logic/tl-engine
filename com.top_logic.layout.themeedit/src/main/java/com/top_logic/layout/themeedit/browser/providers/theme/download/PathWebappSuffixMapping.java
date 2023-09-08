/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.themeedit.browser.providers.theme.download;

import java.nio.file.Path;
import java.util.function.Function;

import com.top_logic.basic.tooling.ModuleLayoutConstants;

/**
 * Maps a given path to a subpath beginning from webapp.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class PathWebappSuffixMapping implements Function<Path, String> {

	/**
	 * Singleton {@link PathWebappSuffixMapping} instance.
	 */
	public static PathWebappSuffixMapping INSTANCE = new PathWebappSuffixMapping();

	private PathWebappSuffixMapping() {
		// Singleton.
	}

	@Override
	public String apply(Path path) {
		String pathString = path.toString();
		int webappIndex = pathString.indexOf(ModuleLayoutConstants.WEBAPP_LOCAL_DIR_NAME);

		return webappIndex != -1 ? pathString.substring(webappIndex) : pathString;
	}

}
