/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.module.ModuleException;
import com.top_logic.basic.tooling.ModuleLayoutConstants;

/**
 * Extension to the XMLProperties, which provides a functionality to
 * use more than one XML specification for the system.
 *
 * <p>
 * When you are accessing an attribute, the class tries to find this
 * attribute in the special configuration, if it's not there, it'll
 * check the default configuration. When you are requesting the list
 * of properties for a key, the class will collect all properties from
 * the default configuration and will afterwards overwrite the values
 * with those found in the special configuration.</p>
 * 
 * <p>For usage of this class you can use the {@link #restartWithConfig(BinaryData) pushInstance()} 
 * method, which prepares the original 
 * {@link com.top_logic.basic.XMLProperties} to be used in a extended way.</p>
 * 
 * <p>A possible way to start usage is provided by the following code
 * snippet:</p>
 * 
 * <pre>
 * ...
 * XMLProperties.getInstance();
 * MultiProperties.pushInstance("webinf://conf/myProps.xml");
 * ..
 * </pre>
 * 
 * <p>Now the XMLProperties will always use this class instead of the 
 * original one. The first call is for instanciating the original values,
 * the second call replaces the singleton within the application.</p>
 * 
 * <p>
 * Changing Values when <code>MultiProperties</code> are used will
 * only change the topmost file. 
 * This will usually save some defaults from
 * the "lower" file(s) in the toplevel file. (In general changing
 * Files when <code>MultiProperties</code> are used is a bad Idea 
 * and not really tested).
 * </p>
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 * @author    <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class MultiProperties extends XMLProperties {

	private static final String TYPED_CONFIG_SUFFIX = ".config.xml";

	/** Original XMLProperties we are overriding. */
    private XMLProperties fallback;

	/**
	 * Creates a {@link MultiProperties}.
	 *
	 * @param aFallback
	 *        The original configuration that is overridden by the new instance.
	 * @param config
	 *        See {@link XMLProperties#XMLProperties(BinaryContent)}
	 */
    public MultiProperties (XMLProperties aFallback, BinaryContent config) throws IOException {
		super(aFallback, config);
    	fallback = aFallback;
    	fallback.stopCaching();
    }

    /** Provide a reasonable String for debugging */
    @Override
	public String toString() {
        if (fallback != null) // just in case
            return fallback.toString() + '\n'
                   + super.toString(); 
        
        return super.toString();
    }

    /**
     * Push an additional XML-Properties file defined by aVersion and optionally by aContext
     */
	static void pushSystemProperty(XMLPropertiesConfig config) throws IOException {
		XMLProperties.Setting setting = Setting.getSetting(config, Setting.CONFIG_FILE);

		if (setting != null) {
			File configFile = new File(setting.getValue());
    
            if (!configFile.exists()) {
				System.err.println("Configuration '" + configFile.getAbsolutePath() + "' not found, will be ignored!");
			} else {
            	System.out.println();
                System.out.println("*******************************************************************************************");
                System.out.println(setting);
                System.out.println("*******************************************************************************************");
				System.out.println();
				if (configFile.isFile()) {
					BinaryData config1 = BinaryDataFactory.createBinaryData(configFile);
					FileManager resolver = FileManager.getInstance();
					config.addConfiguration(resolver, config1.getName(), config1);
				} else {
					addAdditionalContentFolder(config, configFile);
				}
            }
        }
    }

	private static void addAdditionalContentFolder(XMLPropertiesConfig config, File configDir) throws IOException {
		File metaConf = new File(configDir, ModuleLayoutConstants.META_CONF_NAME);
		if (!metaConf.exists()) {
			System.err.println("No '" + ModuleLayoutConstants.META_CONF_NAME + "' file found in configuration folder '"
				+ configDir.getAbsolutePath() + "', will be ignored!");
			return;
		}
		FileManager resolver = new FileManagerOverlay(new DefaultFileManager(configDir) {
			@Override
			protected String toPath(String resourceName) {
				String prefix = ModuleLayoutConstants.CONF_RESOURCE_PREFIX;
				if (!resourceName.startsWith(prefix)) {
					return "NO_SUCH_FILE";
				}

				String suffix = resourceName.substring(prefix.length());
				return super.toPath(suffix);
			}
		}, FileManager.getInstance());

		config.loadMetaConf(resolver, BinaryDataFactory.createBinaryData(metaConf));
	}

	static XMLProperties createNewXMLProperties(XMLProperties previous, BinaryContent config) throws IOException {
		if (previous == null) { // no previous XMLProperties, thats fine
		    return new XMLProperties(config);
		}
		else {
		    return new MultiProperties(previous, config);
		}
	}

	/**
	 * Restarts the configuration with the given configuration content used as overlay to the
	 * existing configuration.
	 */
	public static void restartWithConfig(BinaryData additionalConfig) throws IOException, ModuleException {
		XMLProperties.restartXMLProperties(
			additionalConfig == null ? origConfig() : MultiProperties.pushConfig(origConfig(), additionalConfig));
	}

	/**
	 * Restarts the configuration with the given configuration content used as overlay to the
	 * existing configuration.
	 */
	public static void restartWithConfigs(BinaryContent untypedConfig,
			BinaryContent typedConfig) throws ModuleException {
		XMLProperties.restartXMLProperties(MultiProperties.pushConfigs(origConfig(), untypedConfig, typedConfig));
	}

	/**
	 * Restarts the configuration with the given configuration content used as overlay to the
	 * existing configuration.
	 */
	public static void restartWithConfigs(BinaryContent[] untypedConfig,
			BinaryContent[] typedConfig) throws ModuleException {
		XMLProperties.restartXMLProperties(MultiProperties.pushConfigs(origConfig(), untypedConfig, typedConfig));
	}

	private static XMLPropertiesConfig origConfig() {
		XMLPropertiesConfig config;
		if (exists()) {
			config = Module.INSTANCE.config();
		} else {
			config = new XMLPropertiesConfig();
		}
		return config;
	}

	/**
	 * Take the given filenames as name for new Properties preceding previously used ones. Should
	 * only be done during start-up.
	 * 
	 * @param configFiles
	 *        {@link File}s that are used to load the additional XMLProperties.
	 * 
	 * @return XMLProperties based on the given files falling back to eventually preceding ones.
	 * 
	 * @throws IOException
	 *         iff {@link BinaryContent#getStream()} of the given configurations throws some
	 */
	public static XMLPropertiesConfig pushConfig(XMLPropertiesConfig origConfig, BinaryData... configFiles)
			throws IOException {
		BinaryContent[] untypedConfigs = new BinaryContent[configFiles.length];
		BinaryContent[] typedConfigs = new BinaryContent[configFiles.length];
		for (int i = 0; i < configFiles.length; i++) {
			untypedConfigs[i] = configFiles[i];
			typedConfigs[i] = FileManager.getInstance().getDataOrNull(typedConfigName(configFiles[i].getName()));
		}
		return pushConfigs(origConfig, untypedConfigs, typedConfigs);
	}

	/**
	 * @see #pushConfigs(XMLProperties.XMLPropertiesConfig, BinaryContent[], BinaryContent[])
	 */
	public static XMLPropertiesConfig pushConfigs(XMLPropertiesConfig origConfig, BinaryContent untypedConfig,
			BinaryContent typedConfig) {
		return pushConfigs(origConfig, new BinaryContent[] { untypedConfig }, new BinaryContent[] { typedConfig });
	}

	/**
	 * Take the given {@link BinaryContent} as additional content for the new {@link XMLProperties}
	 * based on former {@link XMLProperties}.
	 * 
	 * <p>
	 * The number of untyped configurations must match the number of typed configurations.
	 * </p>
	 * 
	 * @param untypedConfig
	 *        Additional {@link BinaryContent} for the legacy untyped configuration. Must not be
	 *        <code>null</code>, but may contain <code>null</code>.
	 * @param typedConfig
	 *        Additional {@link BinaryContent} for the typed configuration. Must not be
	 *        <code>null</code>, but may contain <code>null</code>.
	 * 
	 * @return XMLProperties based on the given file falling back to eventually preceding ones
	 */
	public static XMLPropertiesConfig pushConfigs(XMLPropertiesConfig origConfig, BinaryContent[] untypedConfig,
			BinaryContent[] typedConfig) {
		final XMLPropertiesConfig newConfig = origConfig.clone();
		if (untypedConfig.length != typedConfig.length) {
			StringBuilder argMismatch = new StringBuilder();
			argMismatch.append("Number of untyped config (");
			argMismatch.append(untypedConfig.length);
			argMismatch.append(") does not match number of typed config (");
			argMismatch.append(typedConfig.length);
			argMismatch.append("). Untyped configs: ");
			argMismatch.append(Arrays.toString(untypedConfig));
			argMismatch.append(", Typed configs: ");
			argMismatch.append(Arrays.toString(typedConfig));
			throw new IllegalArgumentException(argMismatch.toString());
		}
		for (int i = 0; i < typedConfig.length; i++) {
			newConfig.pushAdditionalContent(untypedConfig[i], typedConfig[i]);
		}
		return newConfig;
	}

	/**
	 * The name of the typed configuration file for an untyped configuration name.
	 */
	public static String typedConfigName(String configName) {
		return configName.substring(0, configName.length() - 4) + TYPED_CONFIG_SUFFIX;
	}

	/**
	 * Whether the given resource is a typed configuration.
	 */
	public static boolean isTypedConfigName(String resource) {
		return resource.endsWith(TYPED_CONFIG_SUFFIX);
	}
}

