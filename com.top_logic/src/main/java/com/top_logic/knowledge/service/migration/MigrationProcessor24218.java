/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.migration;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.Log;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.util.ResourcesModule;

/**
 * {@link MigrationProcessor} for Ticket #24218.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class MigrationProcessor24218 implements MigrationProcessor {

	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		// Resources are started together with the XMLProperties.
		ResourcesModule resources = ResourcesModule.getInstance();
		File storage = FileManager.getInstance().getIDEFile(resources.getDynamicStorage());
		if (storage == null || !storage.exists() || !storage.isDirectory()) {
			log.info(resources.getDynamicStorage() + " is not an existing directory.");
			return;
		}
		List<String> bundleNames = resources.getBundleNames();
		if (bundleNames.size() == 0) {
			log.info("No resource bundles given,");
			return;
		}
		
		StringBuilder pattern = new StringBuilder();
		pattern.append('(');
		pattern.append(Pattern.quote(bundleNames.get(0)));
		for (int i = 1; i < bundleNames.size(); i++) {
			pattern.append('|');
			pattern.append(Pattern.quote(bundleNames.get(i)));
		}
		pattern.append(')');
		pattern.append("_(de|en)_(?:DE|EN)_dynamic\\.properties");
		Pattern fileNamePattern = Pattern.compile(pattern.toString());
		
		File[] oldFormatFiles;
		try {
			oldFormatFiles = FileUtilities.listFiles(storage, new FilenameFilter() {

				@Override
				public boolean accept(File dir, String name) {
					return fileNamePattern.matcher(name).matches();
				}
			});
		} catch (IOException ex1) {
			return;
		}
		if (oldFormatFiles.length == 0) {
			log.info("No files to rename.");
			return;
		}
		for (File oldFile : oldFormatFiles) {
			Matcher matcher = fileNamePattern.matcher(oldFile.getName());
			matcher.matches();
			String bundle = matcher.group(1);
			String language = matcher.group(2);
			File newFile = new File(oldFile.getParent(), bundle + "_" + language + "__dynamic.properties");
			if (newFile.exists()) {
				log.info("File " + newFile + " already exists.");
				continue;
			}
			boolean success = oldFile.renameTo(newFile);
			if (success) {
				log.info("Rename " + oldFile + " to " + newFile + ".");
			} else {
				log.error("Unable to rename " + oldFile + " to " + newFile + ".");
			}
		}
	}

}

