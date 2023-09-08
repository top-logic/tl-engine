/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.build.maven.war.tasks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.top_logic.basic.core.workspace.ModuleLayoutConstants;
import com.top_logic.build.maven.war.TLAppWar;
import com.top_logic.build.maven.war.WarContext;

/**
 * {@link WarTask} for generating the metaConf for deployment.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class GenerateMetaConfTask extends AbstractWarTask {

	private static final String[] NO_DEPLOY_ASPECTS = new String[0];

	private String[] _deployAspects = NO_DEPLOY_ASPECTS;

	private String _deployDir = ModuleLayoutConstants.DEPLOY_FOLDER_NAME;

	private final String[] _excludePatterns;

	private final String[] _metaConfAddons;

	/**
	 * Creates a {@link GenerateMetaConfTask}.
	 * 
	 * @param metaConfAddons
	 *        See {@link TLAppWar#metaConfAddons}.
	 */
	public GenerateMetaConfTask(String[] excludePatterns, String[] metaConfAddons) {
		super();

		_excludePatterns = excludePatterns;
		_metaConfAddons = metaConfAddons;
	}

	/**
	 * Sets the base directory for deploy aspect folders, defaults to
	 * {@link ModuleLayoutConstants#DEPLOY_FOLDER_NAME}.
	 */
	public GenerateMetaConfTask setDeployDir(String deployDir) {
		_deployDir = deployDir;
		return this;
	}

	/**
	 * Sets the comma separated list of directory names of deploy aspect folders.
	 * 
	 * <p>
	 * The given path names are relative to the {@link #setDeployDir(String)}.
	 * </p>
	 */
	public void setDeployAspects(String commaSeparated) {
		if (commaSeparated.equals("-")) {
			// "-" is the representation for no deploy aspect in build.xml's
			_deployAspects = NO_DEPLOY_ASPECTS;
		} else {
			_deployAspects = commaSeparated.trim().split("\\s*,\\s*");
		}
	}

	@Override
	protected void doRun(WarContext context) throws IOException {
		Collection<String> configReferences = new LinkedHashSet<>();
		List<Path> resourcePath = context.getResourcePath();
		for (int n = resourcePath.size() - 1; n >= 0; n--) {
			Path root = resourcePath.get(n);
			Path metaConf = root.resolve(ModuleLayoutConstants.META_CONF_PATH);
			if (Files.exists(metaConf)) {
				try (BufferedReader reader =
					new BufferedReader(new InputStreamReader(Files.newInputStream(metaConf)))) {
					String line;
					while ((line = reader.readLine()) != null) {
						String reference = line.trim();
						if (reference.isEmpty() || reference.startsWith("#")) {
							continue;
						}
						if (configReferences.contains(reference)) {
							continue;
						}
						configReferences.add(reference);
					}
				}
			}
		}

		Pattern excludePattern = Pattern.compile(
			Arrays.asList(_excludePatterns).stream().map(p -> "(?:" + p + ")").collect(Collectors.joining("|")));

		Path target = context.getWebappDirectory().resolve(ModuleLayoutConstants.META_CONF_PATH);
		try (OutputStream out = Files.newOutputStream(target)) {
			try (Writer writer = new OutputStreamWriter(out)) {
				for (String reference : configReferences) {
					if (excludePattern.matcher(reference).matches()) {
						getLog().info("Skipping excluded configuration reference '" + reference + "'.");
						continue;
					}
					writer.write(reference);
					writer.write("\r\n");
				}

				for (String reference : _metaConfAddons) {
					writer.write(reference);
					writer.write("\r\n");
				}
			}
		}
	}

}
