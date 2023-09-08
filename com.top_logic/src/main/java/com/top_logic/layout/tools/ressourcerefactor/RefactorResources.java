/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tools.ressourcerefactor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.top_logic.basic.col.map.MultiMaps;
import com.top_logic.layout.tools.FileHandler;
import com.top_logic.layout.tools.FileUtil;
import com.top_logic.layout.tools.cleanup.AddResourcePrefix;
import com.top_logic.layout.tools.cleanup.ResourceMappingUtil;

/**
 * Applies resource key refactoring information to a given set of resource properties files.
 * 
 * @see AddResourcePrefix Creating resource key refactoring information by normalizing resource keys
 *      defined in layout XML files.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class RefactorResources implements FileHandler {

	/**
	 * Old prefix mapped to a set of potential new prefixes.
	 */
	private Map<String, Set<String>> _prefixMapping = new HashMap<>();

	private Map<String, String> _oldPrefixMapping = new HashMap<>();

	private List<String> _newPrefixes = new ArrayList<>();

	private List<String> _oldPrefixes;

	/**
	 * Main entry point of the {@link RefactorResources} tool.
	 * 
	 * @param args
	 *        {@link File#pathSeparator}-separated paths with resource files to apply the resource
	 *        key refactoring information to.
	 */
	public static void main(String[] args) throws Exception {
		RefactorResources tool = new RefactorResources();
		tool.init();
		FileUtil.handleFiles(args, tool);
	}

	private void init() throws IOException {
		loadPrefixMapping(loadProperties(ResourceMappingUtil.MAPPING_PROPERTIES));
		Properties allKeys = loadProperties("all-keys.properties");

		// Add specialized mapping for keys being requested by the application.
		for (Object key : allKeys.keySet()) {
			String newKey = (String) key;

			for (String newPrefix : _newPrefixes) {
				if (matches(newPrefix, newKey)) {
					String oldPrefix = _oldPrefixMapping.get(newPrefix);
					String oldKey = oldPrefix + newKey.substring(newPrefix.length());

					MultiMaps.add(_prefixMapping, oldKey, newKey);
					break;
				}
			}

			// Make sure that the value is preserved, even if it is used by some mapping (key is
			// duplicated).
			MultiMaps.add(_prefixMapping, newKey, newKey);
		}

		_oldPrefixes = new ArrayList<>(_prefixMapping.keySet());
		Collections.sort(_oldPrefixes);
	}

	private boolean matches(String prefix, String key) {
		if (prefix.endsWith(".")) {
			return key.startsWith(prefix);
		} else {
			return key.startsWith(prefix) && (key.length() == prefix.length() || key.charAt(prefix.length()) == '.');
		}
	}

	@Override
	public void handleFile(String fileName) throws Exception {
		Properties properties = loadProperties(fileName);

		boolean changed = false;
		for (String definedKey : keys(properties)) {
			for (int n = _oldPrefixes.size() - 1; n >= 0; n--) {
				String oldPrefix = _oldPrefixes.get(n);
				if (matches(oldPrefix, definedKey)) {
					String value = properties.getProperty(definedKey);

					// Note: Remove before adding new keys, since the new key may equal the old key.
					// Removing after adding would then remove the newly inserted (unchanged) key.
					properties.remove(definedKey);

					Set<String> newPrefixes = _prefixMapping.get(oldPrefix);
					for (String newPrefix : newPrefixes) {
						String newKey = newPrefix + definedKey.substring(oldPrefix.length());
						properties.setProperty(newKey, value);
					}

					if (newPrefixes.size() > 1) {
						System.err.println("WARNING: Multiple matches for key '" + definedKey + "': " +
							oldPrefix + " -> " + newPrefixes);
					}

					changed = true;

					// Prevent also applying mappings for prefixes of the current old prefix.
					break;
				}
			}
		}

		if (changed) {
			System.out.println("Updating: " + fileName);
			writeProperties(fileName, properties);
		}
	}

	private void writeProperties(String fileName, Properties properties) throws FileNotFoundException, IOException {
		FileOutputStream out = new FileOutputStream(new File(fileName));
		try {
			properties.store(out, null);
		} finally {
			out.close();
		}
	}

	private List<String> keys(Properties properties) {
		Set<Object> keySet = properties.keySet();
		ArrayList<String> result = new ArrayList<>(keySet.size());
		for (Object key : keySet) {
			result.add((String) key);
		}
		return result;
	}

	private Properties loadProperties(String fileName) throws FileNotFoundException, IOException {
		Properties properties = new Properties();
		File file = new File(fileName);
		if (file.exists()) {
			FileInputStream in = new FileInputStream(file);
			try {
				properties.load(in);
			} finally {
				in.close();
			}
		}
		return properties;
	}

	private void loadPrefixMapping(Properties properties) {
		for (Object key : properties.keySet()) {
			String newPrefix = (String) key;
			String oldPrefix = properties.getProperty(newPrefix);

			_newPrefixes.add(newPrefix);
			_oldPrefixMapping.put(newPrefix, oldPrefix);
			MultiMaps.add(_prefixMapping, oldPrefix, newPrefix);
		}
		Collections.sort(_newPrefixes);
	}
}
