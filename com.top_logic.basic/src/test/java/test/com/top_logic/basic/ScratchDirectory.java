/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.Environment;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.core.workspace.ModuleLayoutConstants;
import com.top_logic.basic.tooling.Workspace;

/**
 * Module-local directory for temporary files written by tests.
 *
 * <p>
 * The directory is configured by the system property {@link #PROPERTY} and defaults to
 * {@value #DEFAULT} (relative to the module directory, which is the working directory of a test
 * run). Test runs that execute concurrently in the same module directory use different scratch
 * directories to not interfere with each other.
 * </p>
 *
 * <p>
 * When a scratch directory other than the default is configured, the storage directory of an
 * application started by a test (the variable {@link #STORAGE_PATH_VARIABLE}) defaults to the
 * sub-directory {@value #STORAGE_DIR} of the scratch directory, see
 * {@link #applyStorageDefault()}. An explicitly set {@link #STORAGE_PATH_VARIABLE} takes
 * precedence.
 * </p>
 *
 * <p>
 * Additionally, a writable web application overlay (see {@link #OVERLAY_DIR}) is placed in front of
 * the resource path of the application under test, see {@link #withOverlay(List)}. It becomes the
 * top-level web application ({@link Workspace#topLevelWebapp()}) that receives the files the
 * application creates in its workspace (generated style sheets and scripts, exported layouts,
 * models, and configuration fragments), so that test runs in the same module directory do not
 * modify the module's sources and do not see each other's files.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ScratchDirectory {

	/**
	 * System property configuring the scratch directory.
	 */
	public static final String PROPERTY = "TestAll.scratchDir";

	/**
	 * Default scratch directory, relative to the module directory.
	 */
	public static final String DEFAULT = "tmp";

	/**
	 * System property or environment variable defining the storage directory of the application
	 * under test.
	 */
	public static final String STORAGE_PATH_VARIABLE = "tl_storage_path";

	/**
	 * Name of the application storage directory within a configured scratch directory.
	 */
	public static final String STORAGE_DIR = "app-data";

	/**
	 * Name of the module-like directory within a configured scratch directory that holds the
	 * writable web application overlay.
	 * 
	 * <p>
	 * The overlay web application is the directory {@link ModuleLayoutConstants#WEBAPP_DIR} within
	 * this directory, so that the overlay has the layout of a module.
	 * </p>
	 */
	public static final String OVERLAY_DIR = "overlay";

	/**
	 * The scratch directory for temporary test files.
	 */
	public static File get() {
		return new File(path());
	}

	/**
	 * The file or directory with the given path within the scratch directory.
	 */
	public static File get(String path) {
		return new File(get(), path);
	}

	/**
	 * Whether the scratch directory is the {@link #DEFAULT}.
	 */
	public static boolean isDefault() {
		return DEFAULT.equals(path());
	}

	private static String path() {
		String value = System.getProperty(PROPERTY);
		if (value == null || value.isBlank()) {
			return DEFAULT;
		}
		return value.trim();
	}

	/**
	 * The resource path of the application under test with the writable overlay web application
	 * in front, if a scratch directory other than the {@link #DEFAULT} is configured.
	 * 
	 * <p>
	 * The overlay directory is created, since a {@link FileManager} only considers existing
	 * directories as top-level web application.
	 * </p>
	 * 
	 * @param resourcePath
	 *        The resource path of the application.
	 * @return The given resource path, if the default scratch directory is used, a new list with
	 *         the overlay web application in front otherwise.
	 */
	public static List<Path> withOverlay(List<Path> resourcePath) {
		if (isDefault()) {
			return resourcePath;
		}
		File overlay = new File(get(OVERLAY_DIR), ModuleLayoutConstants.WEBAPP_DIR).getAbsoluteFile();
		overlay.mkdirs();
		List<Path> result = new ArrayList<>(resourcePath.size() + 1);
		result.add(overlay.toPath());
		result.addAll(resourcePath);
		return result;
	}

	/**
	 * Sets the system property {@link #STORAGE_PATH_VARIABLE} to {@link #STORAGE_DIR} within the
	 * scratch directory, if a scratch directory other than the {@link #DEFAULT} is configured and
	 * {@link #STORAGE_PATH_VARIABLE} is set neither as system property nor as environment
	 * variable.
	 *
	 * <p>
	 * Must be called before the application configuration is loaded.
	 * </p>
	 */
	public static void applyStorageDefault() {
		if (isDefault()) {
			return;
		}
		if (Environment.getSystemPropertyOrEnvironmentVariable(STORAGE_PATH_VARIABLE, null) != null) {
			return;
		}
		System.setProperty(STORAGE_PATH_VARIABLE, get(STORAGE_DIR).getAbsolutePath());
	}

}
