/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.resource.analyzer;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.core.workspace.ModuleLayoutConstants;
import com.top_logic.basic.util.I18NBundleSPI;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.tools.resources.ResourceFile;
import com.top_logic.util.DefaultResourcesModule;
import com.top_logic.util.Resources;

/**
 * {@link ResourcesModule} that dumps missing keys and all resolved keys to the workspace.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class IDEResources extends DefaultResourcesModule {

	/**
	 * Configuration options for {@link IDEResources}.
	 */
	public interface Config extends ResourcesModule.Config {

		/**
		 * Whether to dump all used keys during application shutdown.
		 * 
		 * @see #getAllKeysFile()
		 */
		@Name("dump-all-keys")
		boolean getDumpAllKeys();

		/**
		 * The file to dump missing keys to.
		 */
		@StringDefault("src/" + ModuleLayoutConstants.MAIN_ASPECT + "/" + ModuleLayoutConstants.WEBAPP_LOCAL_DIR_NAME
				+ "/" + ModuleLayoutConstants.CONF_PATH + "/resources/missing.properties")
		@Name("missing-keys-file")
		String getMissingKeysFile();

		/**
		 * The file to dump all resolved keys to.
		 * 
		 * @see #getDumpAllKeys()
		 */
		@Name("all-keys-file")
		@StringDefault("all-keys.properties")
		String getAllKeysFile();
	}

	private Map<String, String> _allKeys = new ConcurrentHashMap<>();

	private final boolean _dumpAll;

	/**
	 * Creates a {@link IDEResources} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public IDEResources(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
		_dumpAll = config.getDumpAllKeys();
	}

	@Override
	protected I18NBundleSPI createBundle(Locale locale, Map<String, String> bundle, I18NBundleSPI fallback) {
		return new Bundle(this, locale, bundle, fallback);
	}

	void handleResolve(String key) {
		if (_dumpAll) {
			_allKeys.put(key, key);
		}
	}

	@Override
	protected void shutDown() {
		super.shutDown();

		addMappings(_allKeys, new File(config().getAllKeysFile()));
	}

	@Override
	protected void newUnknownKey(I18NBundleSPI bundle, ResKey key) {
		super.newUnknownKey(bundle, key);

		addMappings(Collections.singletonMap(key.getKey(), ""), new File(config().getMissingKeysFile()));
	}

	private Config config() {
		return (Config) getConfiguration();
	}

	private void addMappings(Map<String, String> newMappings, File file) {
		if (!newMappings.isEmpty()) {
			try {
				save(file, newMappings);
			} catch (IOException ex) {
				Logger.error("Cannot update key mapping: " + file, ex, ResourcesModule.class);
			}
		}
	}

	private void save(File file, Map<String, String> newMappings) throws IOException {
		File dir = file.getParentFile();
		if (!dir.exists()) {
			dir.mkdirs();
		}
		boolean changed = false;
		ResourceFile resourceFile = new ResourceFile(file);
		for (String key : newMappings.keySet()) {
			if (resourceFile.getProperty(key) != null) {
				continue;
			}
			resourceFile.setProperty(key, newMappings.get(key));
			changed = true;
		}
		if (changed) {
			resourceFile.save();
		}
	}

	static class Bundle extends Resources {

		protected Bundle(ResourcesModule owner, Locale locale, Map<String, String> bundle, I18NBundleSPI fallback) {
			super(owner, locale, bundle, fallback);
		}

		@Override
		public String lookup(String key, boolean withFallbackBundle) {
			((IDEResources) owner()).handleResolve(key);

			return super.lookup(key, withFallbackBundle);
		}

	}

}
