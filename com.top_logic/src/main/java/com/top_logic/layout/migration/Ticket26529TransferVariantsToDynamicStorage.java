/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.migration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.top_logic.basic.Log;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.basic.util.ResourcesModule.Config;
import com.top_logic.knowledge.service.migration.MigrationProcessor;

/**
 * Migration pseudo-processor that upgrades the {@link ResourcesModule} dynamic storage.
 */
public class Ticket26529TransferVariantsToDynamicStorage implements MigrationProcessor {

	private static final String BASE = "[^_]+";

	private static final String LANG = "\\w\\w";

	private static final String COUNTRY = "[^_\\.]*";

	private static final String VARIANT = "dynamic";

	private static Pattern NAME_PATTERN =
		Pattern.compile("(" + BASE + ")_((" + LANG + ")(?:_(" + COUNTRY + ")(?:_(" + VARIANT + "))))"
				+ Pattern.quote(ResourcesModule.EXT));

	@Override
	public void doMigration(Log log, PooledConnection connection) {
		try {
			ResourcesModule.Config config =
				(Config) ApplicationConfig.getInstance().getServiceConfiguration(ResourcesModule.class);

			File dir = new File(config.getDynamicStorage());
			if (dir.exists()) {
				File[] files = dir.listFiles();
				if (files != null) {
					Map<String, Properties> resources = new HashMap<>();
					Set<File> oldFiles = new HashSet<>();
					
					for (File file : files) {
						String fileName = file.getName();
						Matcher matcher = NAME_PATTERN.matcher(fileName);
						if (!matcher.matches()) {
							continue;
						}

						oldFiles.add(file);
						String lang = matcher.group(3);
						Properties properties = resources.computeIfAbsent(lang, x -> new Properties());

						try {
							try (InputStream in = new FileInputStream(file)) {
								properties.load(in);
							}
						} catch (IOException ex) {
							log.info("Cannot read legacy resources from '" + file + "': " + ex.getMessage(), Log.WARN);
						}
					}
					
					for (Entry<String, Properties> entry : resources.entrySet()) {
						File newFile =
							new File(dir, ResourcesModule.DYNAMIC_BUNDLE + "_" + entry.getKey() + ResourcesModule.EXT);

						// In case of name clash.
						oldFiles.remove(newFile);

						try {
							try (OutputStream out = new FileOutputStream(newFile)) {
								entry.getValue().store(out, null);
							}
						} catch (IOException ex) {
							log.info("Cannot write new dynamic resources '" + newFile + "': " + ex.getMessage(),
								Log.WARN);
						}
					}

					// Clean up.
					for (File oldFile : oldFiles) {
						oldFile.delete();
					}
				}
			} else {
				log.info("No dynamic storge found, skipping upgrade.");
			}
		} catch (ConfigurationException ex) {
			log.info("Cannot upgrade dynamic resources: " + ex.getMessage(), Log.WARN);
		}
	}


}
