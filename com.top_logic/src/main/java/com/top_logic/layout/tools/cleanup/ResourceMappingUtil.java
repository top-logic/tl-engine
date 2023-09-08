/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tools.cleanup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

/**
 * Utilities for storing resource key mapping files.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ResourceMappingUtil {

	/**
	 * File name where to store resource refactoring information.
	 */
	public static final String MAPPING_PROPERTIES = "webapp/WEB-INF/conf/resources/resource.mapping.properties";

	/**
	 * Adds the given mapping to the `prefix-mapping.properties` file.
	 * 
	 * @param newMappings
	 *        Mapping of legacy key or prefix to new key(s) or prefix(es).
	 */
	public static void storeMappings(Map<String, Set<String>> newMappings) throws FileNotFoundException, IOException {
		if (newMappings.isEmpty()) {
			return;
		}
		
		File file = new File(MAPPING_PROPERTIES);
	
		Properties mapping = new Properties();
		if (file.exists()) {
			FileInputStream in = new FileInputStream(file);
			try {
				mapping.load(in);
			} finally {
				in.close();
			}
		} else {
			file.getParentFile().mkdirs();
		}
	
		FileOutputStream out = new FileOutputStream(file);
		try {
			ResourceMappingUtil.putMappings(mapping, newMappings);
			mapping.store(out, null);
		} finally {
			out.close();
		}
	}

	private static void putMappings(Properties mapping, Map<String, Set<String>> newMappings) {
		for (Entry<String, Set<String>> entry : newMappings.entrySet()) {
			String oldPrefix = entry.getKey();
			for (String newPrefix : entry.getValue()) {
				mapping.put(newPrefix, oldPrefix);
			}
		}
	}

}
