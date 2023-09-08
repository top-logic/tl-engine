/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.processor;

import java.io.File;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.Protocol;
import com.top_logic.gui.Theme;

/**
 * Information about an application during layout processing.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface Application {

	/**
	 * The application name.
	 */
	String getName();

	/**
	 * The root folder from which the application is served (folder containing the WEB-INF
	 * subdirectory).
	 */
	File getWebappDir();

	/**
	 * The root folder of the module defining the application.
	 */
	File getModuleDir();

	/**
	 * The file containing the list of configuration files to read.
	 */
	String getMetaConfResource();

	/**
	 * The directory containing component configurations.
	 */
	File getLayoutDir();

	/**
	 * The Java source directory for tests.
	 */
	File getTestSourceDir();

	/**
	 * Creates a {@link LayoutResolver} for this application.
	 */
	default LayoutResolver createLayoutResolver(Protocol protocol, FileManager fileManager, Theme theme) {
		return new LayoutResolver(protocol, this, fileManager, theme);
	}

	/**
	 * Creates a {@link LayoutResolver} for this application with default {@link FileManager}.
	 * 
	 * @see #createLayoutResolver(Protocol, FileManager, Theme)
	 * @see FileManager#getInstance()
	 */
	default LayoutResolver createLayoutResolver(Protocol protocol, Theme theme) {
		return createLayoutResolver(protocol, FileManager.getInstance(), theme);
	}

}