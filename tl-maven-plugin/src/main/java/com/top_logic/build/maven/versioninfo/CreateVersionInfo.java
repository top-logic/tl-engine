/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.build.maven.versioninfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Contributor;
import org.apache.maven.model.License;
import org.apache.maven.model.Model;
import org.apache.maven.model.Organization;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingResult;

import com.google.gson.stream.JsonWriter;

import com.top_logic.build.maven.war.MojoRuntimeException;

/**
 * Maven goal to generate a version and dependency overview file during packaging.
 */
@Mojo(name = "create-version-info", requiresProject = true, threadSafe = true, defaultPhase = LifecyclePhase.PREPARE_PACKAGE, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class CreateVersionInfo extends AbstractMojo {

	@Parameter(defaultValue = "${project}", readonly = true)
	private MavenProject project;

	@Parameter(defaultValue = "${project.build.directory}/version-info/license/version.json")
	private File versionFile;

	@Parameter(defaultValue = "")
	private String excludedScopes;

	@Parameter(defaultValue = "${session}", readonly = true, required = true)
	private MavenSession session;

	@Parameter(defaultValue = "${project.remoteArtifactRepositories}", readonly = true, required = true)
	private List<ArtifactRepository> pomRemoteRepositories;

	@Parameter
	private String buildQualifier;

	@Component
	private ProjectBuilder mavenProjectBuilder;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		Set<String> excludedScopeSet = excludedScopes == null ? Collections.emptySet()
			: Arrays.asList(excludedScopes.split(",")).stream().map(s -> s.trim()).collect(Collectors.toSet());

		File versionDir = versionFile.getParentFile();
		if (!versionDir.exists()) {
			versionDir.mkdirs();
		}

		ProjectBuildingRequest projectBuildingRequest =
			new DefaultProjectBuildingRequest(session.getProjectBuildingRequest())
				.setValidationLevel(ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL)
				// We already have the relevant part of the dependency tree
				// Re-resolving risks including e.g. excluded artifacts
				.setResolveDependencies(false)
				// We don't care about plugin licensing
				.setProcessPlugins(false)
				.setRemoteRepositories(pomRemoteRepositories);

		try (JsonWriter json = new JsonWriter(new OutputStreamWriter(new FileOutputStream(versionFile), "utf-8"))) {
			json.setIndent("\t");
			Model pom = project.getModel();
			beginModel(json, pom);
			{
				writeString(json, "buildQualifier", buildQualifier);

				Set<Artifact> dependencies = project.getArtifacts();
				if (!dependencies.isEmpty()) {
					json.name("dependencies");
					json.beginArray();
					for (Artifact dependency : dependencies) {
						if (excludedScopeSet.contains(dependency.getScope())) {
							continue;
						}

						try {
							ProjectBuildingResult result = mavenProjectBuilder.build(dependency, true, projectBuildingRequest);
							MavenProject dependencyProject = result.getProject();
							if (dependencyProject == null) {
								getLog().error(
									"Cannot resolve POM of dependency '" + dependency + "': " + result.getProblems());
								writeArtifact(json, dependency);
							} else {
								Model dependencyModel = dependencyProject.getModel();
								writeModel(json, dependencyModel);
							}
						} catch (ProjectBuildingException ex) {
							getLog().error(
								"Error while resolving POM of dependency '" + dependency + "': " + ex.getMessage());
							writeArtifact(json, dependency);
						}
					}
					json.endArray();
				}
			}
			endModel(json);
		} catch (IOException ex) {
			throw new MojoRuntimeException("Cannot create version information: " + ex.getMessage(), ex);
		}
	}

	private void writeArtifact(JsonWriter json, Artifact pom) throws IOException {
		json.beginObject();
		{
			writeString(json, "groupId", pom.getGroupId());
			writeString(json, "artifactId", pom.getArtifactId());
			writeString(json, "version", pom.getVersion());
		}
		json.endObject();
	}

	private void writeModel(JsonWriter json, Model pom) throws IOException {
		beginModel(json, pom);
		endModel(json);
	}

	private void beginModel(JsonWriter json, Model pom) throws IOException {
		json.beginObject();
		{
			writeString(json, "groupId", pom.getGroupId());
			writeString(json, "artifactId", pom.getArtifactId());
			writeString(json, "version", pom.getVersion());

			writeString(json, "name", pom.getName());
			writeString(json, "description", pom.getDescription());
			writeString(json, "url", pom.getUrl());
			writeString(json, "inceptionYear", pom.getInceptionYear());
			writeOrganization(json, "organization", pom.getOrganization());
			writeLicenses(json, "licenses", pom.getLicenses());
			writeContributors(json, "contributors", pom.getContributors());
			writeContributors(json, "developers", pom.getDevelopers());
		}
	}

	private void endModel(JsonWriter json) throws IOException {
		json.endObject();
	}

	private void writeLicenses(JsonWriter json, String name, List<License> licenses) throws IOException {
		if (!licenses.isEmpty()) {
			json.name(name);
			json.beginArray();
			{
				for (License license : licenses) {
					writeLicense(json, license);
				}
			}
			json.endArray();
		}
	}

	private void writeOrganization(JsonWriter json, String name, Organization organization) throws IOException {
		if (organization != null) {
			json.name(name);
			{
				json.beginObject();
				{
					writeString(json, "name", organization.getName());
					writeString(json, "url", organization.getUrl());
				}
				endModel(json);
			}
		}
	}

	private void writeLicense(JsonWriter json, License license) throws IOException {
		json.beginObject();
		{
			writeString(json, "name", license.getName());
			writeString(json, "url", license.getUrl());
			writeString(json, "comments", license.getComments());
		}
		endModel(json);
	}

	private void writeContributors(JsonWriter json, String name, List<? extends Contributor> developers) throws IOException {
		if (!developers.isEmpty()) {
			json.name(name);
			json.beginArray();
			for (Contributor contributor : developers) {
				writeContributor(json, contributor);
			}
			json.endArray();
		}
	}

	private void writeContributor(JsonWriter json, Contributor contributor) throws IOException {
		json.beginObject();
		{
			writeString(json, "name", contributor.getName());
			writeString(json, "email", contributor.getEmail());
			writeString(json, "url", contributor.getUrl());
			writeString(json, "organization", contributor.getOrganization());
			writeString(json, "organizationUrl", contributor.getOrganizationUrl());
		}
		json.endObject();
	}

	private void writeString(JsonWriter json, String name, String value) throws IOException {
		if (value != null) {
			json.name(name);
			json.value(value);
		}
	}

}
