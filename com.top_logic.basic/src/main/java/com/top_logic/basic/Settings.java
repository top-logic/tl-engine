/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

import java.io.File;
import java.io.IOException;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;

/**
 * Global application configuration.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class Settings extends ManagedClass {

	/**
	 * Configuration options for {@link Settings}.
	 */
	public interface Config extends ServiceConfiguration<Settings> {

		/**
		 * @see #getTempDir()
		 */
		String TEMP_DIR = "temp-dir";

		/**
		 * @see #getTmpImagePath()
		 */
		String TEMP_IMAGE_PATH = "temp-image-path";

		/**
		 * The temporary directory of the application.
		 * 
		 * <p>
		 * If a relative path is given, it is resolved relative to the Java temporary directory
		 * (given in the system property {@value #JAVA_IO_TMPDIR}).
		 * </p>
		 * 
		 * <p>
		 * If this option is empty, a unique directory is created within the Java temporary
		 * directory. It is highly recommended to keep this default setting.
		 * </p>
		 */
		@Name(TEMP_DIR)
		@Nullable
		String getTempDir();

		/**
		 * The resource directory where temporary images of the application are created for
		 * download.
		 * 
		 * <p>
		 * The value is a resource name (starting with a '/' character) that denotes the location of
		 * the temporary image folder relative to the root of web application.
		 * </p>
		 */
		@Name(TEMP_IMAGE_PATH)
		@StringDefault("/images/tmp")
		String getTmpImagePath();

	}

	private static final String JAVA_IO_TMPDIR = "java.io.tmpdir";

	private final File _tempDir;

	private final File _imgTempDir;

	private final String _imgTempPath;

	/**
	 * Called by the {@link TypedConfiguration} for starting the {@link Settings} service.
	 * <p>
	 * <b>Don't call directly.</b> Use {@link #getInstance()} instead.
	 * </p>
	 * 
	 * @param context
	 *        For error reporting and instantiation of dependent configured objects.
	 * @param config
	 *        The configuration for the new instance.
	 */
	@CalledByReflection
	public Settings(InstantiationContext context, Config config) {
		super(context, config);
		_tempDir = mkTempDir(config);
		_imgTempPath = config.getTmpImagePath();
		_imgTempDir = mkTempImageDir(config);

		Logger.info("Using temporary directory: " + _tempDir.getAbsolutePath(), Settings.class);
		Logger.info("Using temporary image directory: " + _imgTempDir.getAbsolutePath(), Settings.class);
	}

	/**
	 * The singleton {@link Settings} instance.
	 */
	public static Settings getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * Used for initializing {@link #getImageTempDir()}.
	 * <p>
	 * Hook for subclasses.
	 * </p>
	 * 
	 * @return Never null. Always {@link File#exists() exists}.
	 */
	protected File mkTempImageDir(Config config) {
		String path = config.getTmpImagePath();
		if (path.isEmpty()) {
			path = ".";
		}
		return mkDir(FileManager.getInstance().getIDEFile(path));
	}

	/**
	 * Used for initializing {@link #getTempDir()}.
	 * <p>
	 * Hook for subclasses. Should not throw exceptions but log them to the given {@link Protocol}.
	 * </p>
	 * 
	 * @return Never null. Always {@link File#exists() exists}.
	 */
	protected File mkTempDir(Config config) {
		try {
			String tempDir = config.getTempDir();
			if (StringServices.isEmpty(tempDir)) {
				return mkDefaultTempDir();
			}

			return mkDir(new File(getJavaTempDir(), tempDir));
		} catch (IOException ex) {
			Logger.error("Failed to create temporary directory: " + ex.getMessage(), ex, Settings.class);
			return getJavaTempDir();
		}
	}

	private File mkDefaultTempDir() throws IOException {
		File result = File.createTempFile("tl-temp", "");
		result.delete();
		return mkDir(result);
	}

	/**
	 * The directory in the system property "java.io.tmpdir".
	 * 
	 * @return Never null. Might not {@link File#exists() exist}.
	 */
	protected final File getJavaTempDir() {
		return new File(System.getProperty(JAVA_IO_TMPDIR));
	}

	/**
	 * The temp directory of the application.
	 * 
	 * @return Never null. Always {@link File#exists() exists} and {@link File#isDirectory() is a
	 *         directory}.
	 */
	public final File getTempDir() {
		// Note: Some cleanup task on the host system may delete the temporary directory (e.g. a
		// cron job deleting all contents of /tmp). Therefore, it is the easiest was to re-create it
		// whenever it is needed.
		return mkDir(_tempDir);
	}

	/**
	 * The image temp directory of the application.
	 * 
	 * @return Never null. Always {@link File#exists() exists} and {@link File#isDirectory() is a
	 *         directory}.
	 */
	public final File getImageTempDir() {
		return mkDir(_imgTempDir);
	}

	/**
	 * The name of temporary directory for generated images of the application.
	 */
	public final String getImageTempDirName() {
		return _imgTempPath;
	}

	/**
	 * Check if the given path is existing and a directory.
	 * 
	 * <p>
	 * If the given path does not exist, a directory is created at the specified location.
	 * </p>
	 */
	private File mkDir(File dir) {
		if (!dir.isDirectory()) {
			if (!dir.exists()) {
				Logger.info("Creating directory: '" + dir.getAbsolutePath() + "'", Settings.class);

				boolean success = dir.mkdirs();
				if (!success) {
					Logger.error("Failed to create directory: '" + dir.getAbsolutePath() + "'", Settings.class);
				}
			} else {
				Logger.error("Not a directory: '" + dir.getAbsolutePath() + "'", Settings.class);
			}
		}
		return dir;
	}

	/**
	 * {@link TypedRuntimeModule} for access to the {@link Settings}.
	 */
	public static final class Module extends TypedRuntimeModule<Settings> {

		/**
		 * Sole module instance.
		 */
		public static final Module INSTANCE = new Module();

		private Module() {
			// singleton
		}

		@Override
		public Class<Settings> getImplementation() {
			return Settings.class;
		}

	}

}
