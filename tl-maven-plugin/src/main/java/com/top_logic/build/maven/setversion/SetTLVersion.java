/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.build.maven.setversion;

import java.io.File;
import java.util.Collections;
import java.util.Properties;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;

/**
 * {@link AbstractMojo} to update the <code>tl.version</code> during release.
 */
@Mojo(name = "set-tl-version", requiresProject = true, threadSafe = true)
public class SetTLVersion extends AbstractMojo {

	@Parameter(defaultValue = "${basedir}/pom.xml")
	private File pom;

	@Parameter(defaultValue = "${project.version}")
	private String newVersion;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info("Setting tl.version to: " + newVersion);

		final Properties parameters = new Properties();
		parameters.setProperty("property", "tl.version");
		parameters.setProperty("newVersion", newVersion);

		final InvocationRequest invocationRequest = new DefaultInvocationRequest();
		invocationRequest.setPomFile(pom);
		invocationRequest.setGoals(Collections.singletonList("versions:update-property"));
		invocationRequest.setProperties(parameters);

		final Invoker invoker = new DefaultInvoker();
		try {
			invoker.execute(invocationRequest);
		} catch (MavenInvocationException ex) {
			throw new MojoFailureException("Update version failed: " + ex.getMessage(), ex);
		}
	}

}
