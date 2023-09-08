/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.build.maven.war;

import java.nio.file.Path;
import java.util.List;

import org.apache.maven.plugin.logging.Log;

/**
 * Context information for all tasks that cooperate building a TopLogic application WAR.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class WarContext {

	private Log _log;

	private Path _webappDirectory;

	private List<Path> _resourcePath;

	/**
	 * Creates a {@link WarContext}.
	 */
	public WarContext(Log log, Path webappDirectory, List<Path> resourcePath) {
		_log = log;
		_webappDirectory = webappDirectory;
		_resourcePath = resourcePath;
	}

	/**
	 * The build log.
	 */
	public Log getLog() {
		return _log;
	}

	/**
	 * The target directory, where the expanded web application is built before achiving.
	 */
	public Path getWebappDirectory() {
		return _webappDirectory;
	}

	/**
	 * The resource path of the modular source application.
	 */
	public List<Path> getResourcePath() {
		return _resourcePath;
	}

}
