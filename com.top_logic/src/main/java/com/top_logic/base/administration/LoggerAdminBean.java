/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.administration;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import com.top_logic.basic.AliasManager;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.core.workspace.ModuleLayoutConstants;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.io.binary.ByteArrayStream;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ModuleException;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.basic.module.TypedRuntimeModule;

/** 
 * Utility functions around the Logging framework.
 * 
 * TODO KHA This works only with Logger4, redesign for LoggerJ 
 * 
 * @author  Frank Mausz
 */
public final class LoggerAdminBean extends ConfiguredManagedClass<LoggerAdminBean.Config> {

	/**
	 * Configuration options for {@link LoggerAdminBean}.
	 */
	public interface Config extends ConfiguredManagedClass.Config<LoggerAdminBean> {

		/**
		 * The file system directory, where {@link #getLoggingConfig() logging configurations} are
		 * searched.
		 * 
		 * <p>
		 * If nothing is specified, the logging configuration is expected to be in the
		 * <code>{@value #CONF_PATH}</code> path within the web application.
		 * </p>
		 */
		@Name("config-dir")
		@Nullable
		String getConfigDir();

		/**
		 * The logging configuration file in {@link #getConfigDir()} to load by default.
		 */
		@Name("logging-config")
		@StringDefault("default.xml")
		String getLoggingConfig();

		/**
		 * @see #getLoggingConfig()
		 */
		void setLoggingConfig(String propertiesFile);

		/**
		 * The directory to create log files in.
		 */
		@Name("log-dir")
		@Mandatory
		String getLogDir();

	}

	/**
	 * The path relative to the web application, where logging configuration are searched for, if
	 * {@link Config#getConfigDir()} is not specified.
	 */
	private static final String CONF_PATH = ModuleLayoutConstants.CONF_RESOURCE_PREFIX + "/logging/";

	/**
	 * Creates a {@link LoggerAdminBean} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public LoggerAdminBean(InstantiationContext context, Config config) {
		super(context, config);

		configure();
	}
	
	/**
	 * Looks up all logging configuration file names in the configuration directory.
	 */
	public List<String> getExistingConfigFiles() {
		try {
			String customConfigDir = getConfig().getConfigDir();
			if (customConfigDir == null) {
				return confFilesFromDefaultDir();
			} else {
				return confFilesFromCustomDir(customConfigDir);
			}
		} catch (Exception ex) {
			Logger.error("Cannot list logging config files.", ex, LoggerAdminBean.class);
			return Collections.emptyList();
		}
   }

	private List<String> confFilesFromCustomDir(String customConfigDir) throws IOException {
		File configDir = new File(customConfigDir);
		if (!configDir.exists()) {
			Logger.warn("Configuration directory '" + configDir.getAbsolutePath() + "' does not exist.",
				LoggerAdminBean.class);
			return Collections.emptyList();
		}
		if (!configDir.isDirectory()) {
			Logger.warn("Configuration directory '" + configDir.getAbsolutePath() + "' is not a directory.",
				LoggerAdminBean.class);
			return Collections.emptyList();
		}
		String[] configFiles = FileUtilities.list(configDir, new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return isPotentialLogConfiguration(name);
			}
		});
		Arrays.sort(configFiles);
		return Arrays.asList(configFiles);
	}

	private List<String> confFilesFromDefaultDir() {
		List<String> existingConfigFiles = new ArrayList<>();
		FileManager fm = FileManager.getInstance();
		for (String logConfFile : fm.getResourcePaths(CONF_PATH)) {
			if (fm.isDirectory(logConfFile)) {
				continue;
			}
			if (isPotentialLogConfiguration(logConfFile)) {
				existingConfigFiles.add(logConfFile.substring(CONF_PATH.length()));
			}
		}
		Collections.sort(existingConfigFiles);
		return existingConfigFiles;
	}

	boolean isPotentialLogConfiguration(String logConfFile) {
		return logConfFile.endsWith(".xml") || logConfFile.endsWith(".properties");
	}

	/**
	 * Configure the logging with default properties.
	 */
	private void configure() {
		try {
			URL configFile = openConfigFile();
			if (configFile == null) {
				return;
			}
			// Create a new URL that transforms the content by using the AliasManager.
			configFile = new URL(null, configFile.toExternalForm(), new AliasedURLStreamHandler(configFile));

			Logger.configure(configFile);

			Logger.info("Logging configuration successfully loaded from " + configFile + ".", LoggerAdminBean.class);
		} catch (IOException ex) {
			Logger.error("Error loading logging configuration.", ex, LoggerAdminBean.class);
		} catch (URISyntaxException ex) {
			Logger.error("Error loading logging configuration.", ex, LoggerAdminBean.class);
		}
	}

	private URL openConfigFile() throws IOException, URISyntaxException {
		Config config = getConfig();
		String loggingConfig = config.getLoggingConfig();
		if (loggingConfig.isEmpty()) {
			Logger.info("No logging configuration given.", LoggerAdminBean.class);
			return null;
		}
		String customConfigDir = config.getConfigDir();
		if (customConfigDir != null) {
			File configFile = new File(customConfigDir, loggingConfig);
			if (configFile.exists()) {
				Logger.info("Start loading logging configuration from '" + configFile.getAbsolutePath() + "'.",
					LoggerAdminBean.class);
				return configFile.toURI().toURL();
			} else {
				Logger.error("Configuration file '" + configFile.getAbsolutePath() + "' does not exist.",
					LoggerAdminBean.class);
				return null;
			}
		} else {
			String configPath = CONF_PATH + loggingConfig;
			Logger.info("Start loading logging configuration from path '" + configPath + "'.",
				LoggerAdminBean.class);
			return FileManager.getInstance().getResourceUrl(configPath);
		}
	}

	/**
	 * Get the base directory for log files of this instance.
	 *
	 * @return the base directory; never null
	 */
	public File getLogDir() {
		return new File(getConfig().getLogDir());
   }
   
	/**
	 * Singleton reference to {@link LoggerAdminBean}.
	 */
	public static final class Module extends TypedRuntimeModule<LoggerAdminBean> {

		/**
		 * Singleton {@link LoggerAdminBean.Module} instance.
		 */
    	public static final LoggerAdminBean.Module INSTANCE = new LoggerAdminBean.Module();
    	
    	private String propertiesFile = null;
		
		@Override
		public Class<LoggerAdminBean> getImplementation() {
			return LoggerAdminBean.class;
		}

		@Override
		protected LoggerAdminBean createImplementation(InstantiationContext context,
				ServiceConfiguration<? extends LoggerAdminBean> configuration) {
			if (propertiesFile != null) {
				configuration = TypedConfiguration.copy(configuration);
				((Config) configuration).setLoggingConfig(propertiesFile);
			}
			
			return super.createImplementation(context, configuration);
		}

		String configureWithProperties(String newPropertiesFile) {
			String oldPropertiesFile = propertiesFile;
			propertiesFile = newPropertiesFile;
			return oldPropertiesFile;
		}
    	
    }
    
	/**
	 * Configures the Logger with the properties form the given file and starts
	 * the module.
	 * 
	 * @see Properties#load(InputStream)
	 * 
	 * @param propertiesFile
	 *        the name of the file to use relative to the configuration
	 *        directory
	 * 
	 * @throws ModuleException
	 *         iff restarting this module or a dependent failed
	 */
    public static final void configureWithProperties(final String propertiesFile) throws IllegalArgumentException, ModuleException {
    	final Module loggerModule = Module.INSTANCE;
    	if (loggerModule.isActive()) {
    		ModuleUtil.INSTANCE.restart(loggerModule, new Runnable() {
				
				@Override
				public void run() {
					loggerModule.configureWithProperties(propertiesFile);
				}
			});
    	} else {
    		loggerModule.configureWithProperties(propertiesFile);
    		ModuleUtil.INSTANCE.startUp(loggerModule);
    	}
    	
    }

	/**
	 * {@link URLStreamHandler} that replaces the content read from the original URL by using the
	 * {@link AliasManager}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class AliasedURLStreamHandler extends URLStreamHandler {

		private URL _baseURL;

		private static class AliasedURLConnection extends URLConnection {

			private final URLConnection _baseConnection;

			AliasedURLConnection(URL url, URLConnection baseConnection) {
				super(url);
				_baseConnection = baseConnection;
			}

			@Override
			public void connect() throws IOException {
				_baseConnection.connect();
			}

			@Override
			public InputStream getInputStream() throws IOException {
				Charset charset = contentEncoding();
				String string = baseContentAsString(charset);
				String replaced = AliasManager.getInstance().replace(string);
				return transformedByteContent(replaced, charset);

			}

			private InputStream transformedByteContent(String content, Charset charset) throws IOException {
				try (ByteArrayStream byteOut = new ByteArrayStream();
						Writer w = new OutputStreamWriter(byteOut, charset)) {
					w.write(content);
					w.flush();
					return byteOut.getStream();
				}
			}

			private String baseContentAsString(Charset charset) throws IOException {
				StringWriter out = new StringWriter();
				try (InputStream rawInput = _baseConnection.getInputStream();
						Reader r = new InputStreamReader(rawInput, charset)) {
					StreamUtilities.copyReaderWriterContents(r, out);
				}
				return out.toString();
			}

			private Charset contentEncoding() {
				String externalForm = url.toExternalForm();
				Charset charset;
				if (externalForm.endsWith(".properties")) {
					// Properties files are always ISO-8859-1 encoded
					charset = StreamUtilities.ISO_8859_1;
				} else {
					charset = StringServices.CHARSET_UTF_8;
				}
				return charset;
			}

		}

		/**
		 * Creates a new {@link AliasedURLStreamHandler}.
		 * 
		 * @param baseURL
		 *        Base URL to get original content from.
		 */
		public AliasedURLStreamHandler(URL baseURL) {
			_baseURL = baseURL;
		}

		@Override
		protected URLConnection openConnection(URL u) throws IOException {
			return new AliasedURLConnection(u, _baseURL.openConnection());
		}

	}

}
