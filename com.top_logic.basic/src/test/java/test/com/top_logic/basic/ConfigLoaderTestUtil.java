/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import test.com.top_logic.basic.module.ServiceStarter;
import test.com.top_logic.basic.util.WebdirFinder;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.MultiFileManager;
import com.top_logic.basic.MultiProperties;
import com.top_logic.basic.XMLProperties;
import com.top_logic.basic.XMLProperties.XMLPropertiesConfig;
import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.core.workspace.Workspace;
import com.top_logic.basic.io.RegExpFilenameFilter;
import com.top_logic.basic.module.ModuleException;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.tooling.ModuleLayoutConstants;
import com.top_logic.basic.util.Computation;
import com.top_logic.basic.util.ComputationEx2;


/**
 * A utility for tests that need to access the application configuration
 * in cases where configuration hasn't been loaded, yet.
 * This utility class can load the configuration, and unload it again afterwards.
 * 
 * The configuration loaded is the test configuration of the application,
 * as this utility is meant to be used by tests only.
 * 
 * @author     <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class ConfigLoaderTestUtil {
	
	/** @see #useTestConfiguration(boolean) */
	private boolean withTestConfig = true;

	private static final RegExpFilenameFilter TEST_CONFIG_FILENAME_FILTER =
		new RegExpFilenameFilter(".*\\.test\\.xml");

	private static File applicationRoot;
	
	/**
	 * Singleton {@link ConfigLoaderTestUtil} instance.
	 */
	public static final ConfigLoaderTestUtil INSTANCE = new ConfigLoaderTestUtil();
	
	private XMLPropertiesConfig previousConfig = null;
	
	private ServiceStarter _threadContextStarter = new ServiceStarter(ThreadContextManager.Module.INSTANCE);
	
	private boolean _active = false;
	
	private boolean failed = false;

	private File _metaConfFile;

	private File _metaConfCopy;
	
	private ConfigLoaderTestUtil() {
		/* Private singleton constructor */
	}
	
	/**
	 * The root directory of the current web application.
	 * 
	 * <p>
	 * If the current web application consists of multiple stacked modules, the
	 * directory of the top-level module is returned.
	 * </p>
	 */
	public static synchronized File getApplicationRoot() {
		if (applicationRoot == null) {
			applicationRoot = WebdirFinder.INSTANCE.findWebDir();
		}
		return applicationRoot;
	}
	
	/**
	 * The test configuration fragment that is loaded additionally to the configurations named in
	 * {@link ModuleLayoutConstants#META_CONF_RESOURCE}.
	 */
	public static Maybe<String> getTestConfiguration() {
		Maybe<File> testFolder = getTestWebapp();
		if (!testFolder.hasValue()) {
			return Maybe.none();
		}
		File testConfigDir = new File(testFolder.get(), ModuleLayoutConstants.CONF_PATH);
		if (!testConfigDir.exists()) {
			return Maybe.none();
		}
		File[] configFiles = testConfigDir.listFiles(TEST_CONFIG_FILENAME_FILTER);
		if (configFiles.length == 0) {
			return Maybe.none();
		}
		if (configFiles.length > 1) {
			throw new RuntimeException("Multiple test configurations found: " + Arrays.toString(configFiles));
		}
		return Maybe.toMaybe(configFiles[0].getName());
	}
	
	/**
	 * Returns the folder of the test web application.
	 */
	public static Maybe<File> getTestWebapp() {
		File webappDirDir = new File(getApplicationRoot(), ModuleLayoutTestConstants.PATH_TO_TEST_WEB_APPLICATION);
		if (!webappDirDir.exists()) {
			return Maybe.none();
		}
		return Maybe.toMaybe(webappDirDir);
	}

	/**
	 * Loads the configuration, runs the given {@link Computation}, and unloads the configuration
	 * again. If the computation fails, the configuration is unloaded and a RuntimeException is
	 * thrown.
	 */
	public <T, E1 extends Throwable, E2 extends Throwable> T runWithLoadedConfig(ComputationEx2<T, E1, E2> computation)
			throws E1, E2 {
		if (_active) {
			return computation.run();
		} else {
			loadConfig();
			try {
				return computation.run();
			} finally {
				unloadConfig();
			}
		}
	}

	private String message(Throwable error) {
		String message = error.getMessage();

		// E.g. NullPointerException has no message!
		return message != null ? message : error.getClass().getName();
	}
	
	/**
	 * Usage discouraged. If possible, please use {@link #runWithLoadedConfig(ComputationEx2)}
	 * instead.
	 * 
	 * Loads the configuration. Make sure, you call {@link #unloadConfig()}, if you call this
	 * method. (For example in a 'finally' block.)
	 */
	public synchronized void loadConfig() {
		if (failed) {
			String errorMessage = "Loading configuration has failed before."
				+ " It's unclear in what state the configuration currently is."
				+ " Therefore, loading it is no longer possible.";
			throw new IllegalStateException(errorMessage);
		}
		if (_active) {
			throw new IllegalStateException("The configuration is already loaded.");
		}
		try {
			_active = true;
			loadConfigUnprotected();
		} catch (Throwable error) {
			failed = true;
			String loggerMessage = "Loading configuration failed! Therefore: Unloading configuration. Error message: "
				+ message(error);
			Logger.error(loggerMessage, error, ConfigLoaderTestUtil.class);
			
			unloadConfig();
			
			String exceptionMessage = "Loading configuration failed! Therefore: Unloaded configuration. Error message: "
				+ message(error);
			throw new RuntimeException(exceptionMessage, error);
		}
	}
	
	/**
	 * Usage discouraged. If possible, please use {@link #runWithLoadedConfig(ComputationEx2)}
	 * instead.
	 * 
	 * Unloads the configuration after it has been loaded with {@link #loadConfig()}.
	 */
	public synchronized void unloadConfig() {
		try {
			if (!_active) {
				throw new IllegalStateException("Configuration should be unloaded, but isn't loaded!");
			}
			_active = false;
			unloadConfigUnprotected();
		} catch (Throwable error) {
			failed = true;
			String errorMessage = "Unloading configuration failed. Error message: " + message(error);
			throw new RuntimeException(errorMessage, error);
		}
	}
	
	private void loadConfigUnprotected() {
		setupFileManager();
		setupConfiguration();
	}
	
	private void setupConfiguration() {
		try {
			setupConnectionPoolRegistry();
			if (XMLProperties.exists()) {
				previousConfig = XMLProperties.Module.INSTANCE.config();
			}

			MultiProperties.restartWithConfig(null);

			_threadContextStarter.startService();
		}
		catch (Exception exception) {
			throw new RuntimeException(exception);
		}
	}

	private void setupConnectionPoolRegistry() throws IOException {
		// Update the metaConf.txt to be able to survive a restart of the configuration.
		List<String> additionalFiles = new ArrayList<>();
		addConfigFile(additionalFiles, "testingConnectionPool.xml");

		addDBConfigFile(additionalFiles, "com.mysql.cj.jdbc.MysqlConnectionPoolDataSource", "test-with-mysql.xml");
		addDBConfigFile(additionalFiles, "org.h2.jdbcx.JdbcDataSource", "test-with-h2.xml");
		addDBConfigFile(additionalFiles, "oracle.jdbc.pool.OracleConnectionPoolDataSource", "test-with-oracle.xml");
		addDBConfigFile(additionalFiles, "oracle.jdbc.pool.OracleConnectionPoolDataSource", "test-with-oracle12.xml");
		addDBConfigFile(additionalFiles, "oracle.jdbc.pool.OracleConnectionPoolDataSource", "test-with-oracle19.xml");
		addDBConfigFile(additionalFiles, "com.ibm.db2.jcc.DB2SimpleDataSource", "test-with-db2.xml");
		addDBConfigFile(additionalFiles, "com.microsoft.sqlserver.jdbc.SQLServerDataSource", "test-with-mssql.xml");
		addDBConfigFile(additionalFiles, "org.postgresql.ds.PGSimpleDataSource", "test-with-postgresql.xml");

		if (withTestConfig) {
			Maybe<String> testConfig = getTestConfiguration();
			if (testConfig.hasValue()) {
				additionalFiles.add(testConfig.get());
			}
		}

		addToMetaConf(additionalFiles);
	}

	private void addDBConfigFile(List<String> additionalFiles, String dataSourceClassName, String filename) {
		try {
			if (Class.forName(dataSourceClassName) != null) {
				addConfigFile(additionalFiles, filename);
			}
		} catch (ClassNotFoundException exception) {
			// Class not found. Skip.
		}
	}

	private void addConfigFile(List<String> additionalFiles, String name) {
		additionalFiles.add(name);
	}

	private void addToMetaConf(List<String> additionalFiles) throws IOException {
		File topMetaConf = FileManager.getInstance().getIDEFile(ModuleLayoutConstants.META_CONF_RESOURCE);
		File metaConfDir = topMetaConf.getParentFile();
		if (!metaConfDir.exists()) {
			// Ensure that storing metaConf.txt does not fail because of missing parent directory.
			metaConfDir.mkdirs();
		}
		_metaConfFile = new File(metaConfDir, ModuleLayoutConstants.META_CONF_NAME);
		_metaConfCopy = new File(metaConfDir, ModuleLayoutConstants.META_CONF_NAME + ".orig");
		if (_metaConfCopy.exists()) {
			throw handleExistingMetaConfCopy();
		}
		String charsetName = "ascii";
		if (_metaConfFile.exists()) {
			_metaConfFile.renameTo(_metaConfCopy);
		} else {
			// Only touch file.
			_metaConfCopy.createNewFile();
		}
		try (PrintWriter out =
			new PrintWriter(new OutputStreamWriter(new FileOutputStream(_metaConfFile), charsetName))) {
			try (BufferedReader in =
				new BufferedReader(new InputStreamReader(new FileInputStream(_metaConfCopy), charsetName))) {
				String line;
				while ((line = in.readLine()) != null) {
					if (!line.isEmpty()) {
						out.println(line);
					}
				}
			}
			for (String additionalFile : additionalFiles) {
				out.print(additionalFile);
				out.println();
			}
		}
	}

	private RuntimeException handleExistingMetaConfCopy() {
		StringBuilder msg = new StringBuilder();
		msg.append("There is a copy of the meta conf file: ");
		msg.append(_metaConfCopy.getAbsolutePath());
		msg.append(". Last setup may not did complete nornally. Current " + ModuleLayoutConstants.META_CONF_NAME + " (");
		msg.append(_metaConfFile.getAbsolutePath());
		msg.append(") may be corrupted.");
		throw new RuntimeException(msg.toString());
	}

	private void setupFileManager() {
		List<Path> webappPaths = Workspace.applicationModules();
		FileManager fileManager = MultiFileManager.createMultiFileManager(webappPaths);
		FileManager.setInstance(fileManager);
	}

	/**
	 * Whether the given web application is the
	 * {@link ModuleLayoutConstants#DEPLOY_LOCAL_FOLDER_NAME local} web application of the module.
	 */
	public static boolean isLocalWebApp(Path webappPath) {
		Path root = webappPath.getParent();
		if (root == null) {
			// Happens in a ZIP file system, where the webapp path is already the root of the
			// filesystem.
			return false;
		}
		return root.getFileName().toString().equals(ModuleLayoutConstants.DEPLOY_LOCAL_FOLDER_NAME)
			&& root.getParent().getFileName().toString().equals(ModuleLayoutConstants.DEPLOY_FOLDER_NAME);
	}

	/**
	 * Whether the given web application is the
	 * {@link ModuleLayoutConstants#DEPLOY_PRIVATE_FOLDER_NAME private} web application of the
	 * module.
	 */
	public static boolean isPrivateWebApp(Path webappPath) {
		Path root = webappPath.getParent();
		if (root == null) {
			// Happens in a ZIP file system, where the webapp path is already the root of the
			// filesystem.
			return false;
		}
		return root.getFileName().toString().equals(ModuleLayoutConstants.DEPLOY_PRIVATE_FOLDER_NAME)
			&& root.getParent().getFileName().toString().equals(ModuleLayoutConstants.DEPLOY_FOLDER_NAME);
	}

	private void unloadConfigUnprotected() {
		teardownXMLProps();
		teardownFileManager();
	}
	
	private void teardownXMLProps() {
		try {
			_threadContextStarter.stopService();
			if (previousConfig != null) {
				XMLProperties.restartXMLProperties(previousConfig);
			} else {
				ModuleUtil.INSTANCE.shutDown(XMLProperties.Module.INSTANCE);
			}
			teardownConnectionPoolRegistry();
		}
		catch (IllegalStateException exception) {
			throw new RuntimeException(exception);
		}
		catch (ModuleException exception) {
			throw new RuntimeException(exception);
		}
	}

	private void teardownConnectionPoolRegistry() {
		if (_metaConfFile != null) {
			boolean delete = _metaConfFile.delete();
			if (!delete) {
				throw new RuntimeException("Unable to delete " + _metaConfFile.getAbsolutePath() + " to reinstall copy "
					+ _metaConfCopy.getAbsolutePath());
			}
		}

		if (_metaConfCopy != null) {
			if (_metaConfCopy.length() == 0) {
				// Clean-up placeholder file.
				boolean ok = _metaConfCopy.delete();
				if (!ok) {
					throw new RuntimeException(
						"Unable to remove placeholder file " + _metaConfCopy.getAbsolutePath() + ".");
				}
			} else {
				boolean renameTo = _metaConfCopy.renameTo(_metaConfFile);
				if (!renameTo) {
					throw new RuntimeException("Unable to rename " + _metaConfCopy.getAbsolutePath() + " to "
						+ _metaConfFile.getAbsolutePath());
				}
			}
		}
	}

	private void teardownFileManager() {
		FileManager.setInstance(null);
	}

	/**
	 * Sets whether the test configuration of the project should also be loaded.
	 * 
	 * @return The configuration value active before this call.
	 */
	public boolean useTestConfiguration(boolean b) {
		boolean before = withTestConfig;
		this.withTestConfig = b;
		return before;
	}

}
