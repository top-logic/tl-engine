/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.build.maven.war.tasks;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;

import com.top_logic.build.maven.war.WarContext;

/**
 * {@link WarTask} that copies the contents of the web application being currently built to the
 * {@link #getWebappDirectory()}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 * 
 * @see FragmentOverlay
 */
public class AppOverlay extends FragmentOverlay {

	private MavenProject _project;

	private String[] _metaConfExcludes;

	private String[] _metaConfAddons;

	/**
	 * Creates an {@link AppOverlay}.
	 */
	public AppOverlay(MavenProject project, Artifact appFragment, String[] metaConfExcludes, String[] metaConfAddons) {
		super(appFragment);
		_project = project;
		_metaConfExcludes = metaConfExcludes;
		_metaConfAddons = metaConfAddons;
	}
	
	@Override
	protected void doRun(WarContext context) throws IOException {
		super.doRun(context);

		getLog().info("Creating final web app.");
		copyJarDependencies();

		new GenerateMetaConfTask(_metaConfExcludes, _metaConfAddons).run(context);
	}

	private void copyJarDependencies() throws IOException {
		Path libPath = getWebappDirectory().resolve("WEB-INF/lib");
		Files.createDirectories(libPath);
		
		List<Artifact> artifacts =
			_project.getArtifacts().stream()
				.filter(artifact -> "jar".equals(artifact.getType()))
				.filter(artifact -> !"provided".equals(artifact.getScope()))
				.collect(Collectors.toList());

		artifacts.add(_project.getArtifact());
		
		Map<String, Long> duplicates = artifacts.stream()
			.collect(Collectors.groupingBy(Artifact::getArtifactId, Collectors.counting()));
		
		for (Artifact artifact : artifacts) {
			String targetBaseName = artifact.getArtifactId();
			if (duplicates.get(targetBaseName) > 1) {
				targetBaseName = artifact.getGroupId() + "-" + artifact.getArtifactId();
			}
			
			Path targetPath = libPath.resolve(targetBaseName + ".jar");
			Path artifactJar = artifact.getFile().toPath();

			getLog().info("Adding JAR " + targetPath + " from " + artifactJar);
			Files.copy(artifactJar, targetPath);
		}
	}

}
