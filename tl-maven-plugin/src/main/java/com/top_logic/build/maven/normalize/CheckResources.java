/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.build.maven.normalize;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

import com.top_logic.tools.resources.ResourceFile;

/**
 * Maven goal for checking resource bundles for consistency.
 * 
 * <p>
 * A resource bundle is consistent, if all files for individual languages are in normal form and all
 * files contain translations for all keys.
 * </p>
 */
@Mojo(name = "check-resources")
public class CheckResources extends AbstractResourcesMojo {

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		File target = getResourceDir();
		if (!target.exists()) {
			getLog().info("No resources: " + target);
			return;
		}

		if (target.isFile()) {
			checkFile(target);
		} else {
			checkAll(target);
		}
	}

	private void checkFile(File target) {
		Map<String, List<File>> bundles = listBundles(target.getParentFile());
		bundles.keySet().retainAll(Collections.singleton(getBaseName(target)));
		checkBundles(bundles);
	}

	private void checkAll(File dir) {
		checkBundles(listBundles(dir));
	}

	private Map<String, List<File>> listBundles(File dir) {
		Map<String, List<File>> bundles =
			Arrays.asList(dir.listFiles(f -> f.getName().endsWith(".properties")))
				.stream()
				.collect(Collectors.groupingBy(this::getBaseName));
		return bundles;
	}

	private void checkBundles(Map<String, List<File>> bundles) {
		boolean ok = true;
		for (List<File> files : bundles.values()) {
			List<ResourceFile> resourceFiles = files.stream().map(ResourceFile::new).collect(Collectors.toList());

			Set<String> allKeys = new HashSet<>();
			resourceFiles.forEach(r -> allKeys.addAll(r.getKeys()));

			for (ResourceFile resourceFile : resourceFiles) {
				if (!resourceFile.getKeys().equals(allKeys)) {
					Set<String> missing = new HashSet<>(allKeys);
					missing.removeAll(resourceFile.getKeys());

					error("Missing resource keys in '" + resourceFile.getFile() + "': "
						+ missing.stream().collect(Collectors.joining(", ")));
					ok = false;
				}

				if (!resourceFile.isNormalized()) {
					error("Not normalized: " + resourceFile.getFile());
					ok = false;
				}
			}
		}

		if (ok) {
			getLog().info(
				(bundles.keySet().size() > 1 ? "Resource bundles are consistent: " : "Resource bundle is consistent: ")
					+ bundles.keySet().stream().sorted().collect(Collectors.joining(", ")));
		}
	}

	private void error(String msg) {
		getLog().error("Resource check: " + msg);
	}

	private String getBaseName(File file) {
		String name = file.getName();
		int langIndex = name.indexOf('_');
		int dotIndex = name.lastIndexOf('.');
		return name.substring(0, langIndex < 0 ? dotIndex : langIndex);
	}

}
