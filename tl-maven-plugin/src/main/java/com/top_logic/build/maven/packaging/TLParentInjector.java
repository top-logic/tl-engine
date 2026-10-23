/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.build.maven.packaging;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.RepositoryUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Build;
import org.apache.maven.model.Extension;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginManagement;
import org.apache.maven.model.Profile;
import org.apache.maven.model.ReportPlugin;
import org.apache.maven.model.building.DefaultModelBuildingRequest;
import org.apache.maven.model.building.ModelBuilder;
import org.apache.maven.model.building.ModelBuildingException;
import org.apache.maven.model.building.ModelBuildingResult;
import org.apache.maven.model.building.ModelProcessor;
import org.apache.maven.project.DefaultModelBuildingListener;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingHelper;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.ProjectModelResolver;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RequestTrace;
import org.eclipse.aether.impl.RemoteRepositoryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builds projects with a {@link TLPackaging} with the build configuration of the TopLogic parent
 * POM of their packaging, without requiring them to inherit from it.
 *
 * <p>
 * After all projects are read, each project with a {@link TLPackaging} is rebuilt by the model
 * builder as if the TopLogic parent POM of its packaging (of the version of this plugin) were the
 * parent of the top-most POM of the project's own parent chain (see {@link ParentSplicer}). The
 * project's own POM and its own parents therefore override the injected configuration, as with
 * inheritance.
 * </p>
 *
 * <p>
 * From the rebuilt model, the project takes the build section (plugins, plugin management,
 * resources), the properties, the dependencies, the repositories and the reporting section, and
 * the profiles activated in the injected POMs. The identity and metadata of the project (group,
 * artifact, version, name, URL, organization, licenses, SCM, distribution management, modules)
 * stay as declared by the project's own chain. The injected POMs contribute no
 * <code>dependencyManagement</code>: the project imports the BOM <code>tl-bom</code> itself,
 * because Maven checks the dependency versions while reading the projects, before this
 * participant runs.
 * </p>
 *
 * <p>
 * A project whose parent chain already contains a TopLogic parent POM is left unchanged.
 * </p>
 */
@Named(TLParentInjector.NAME)
@Singleton
public class TLParentInjector extends AbstractMavenLifecycleParticipant {

	/**
	 * The name of this component.
	 */
	public static final String NAME = "tl-parent-injector";

	/**
	 * Key of the project context value recording the injected parent of a project.
	 */
	public static final String INJECTED_PARENT = TLParentInjector.class.getName() + ".injectedParent";

	/**
	 * The artifact ID of this plugin.
	 */
	public static final String PLUGIN_ARTIFACT_ID = "tl-maven-plugin";

	/**
	 * The resource with the Maven coordinates of this plugin.
	 */
	public static final String PLUGIN_POM_PROPERTIES =
		"/META-INF/maven/" + TLPackaging.TL_GROUP_ID + "/" + PLUGIN_ARTIFACT_ID + "/pom.properties";

	/**
	 * The property of {@link #PLUGIN_POM_PROPERTIES} holding the version.
	 */
	public static final String VERSION_PROPERTY = "version";

	/**
	 * The ID of the injected profiles that are not declared in a POM.
	 *
	 * @see MavenProject#setInjectedProfileIds(String, List)
	 */
	public static final String EXTERNAL_PROFILES = "external";

	/**
	 * The group ID of the Eclipse m2e lifecycle mapping pseudo plugin.
	 */
	public static final String M2E_GROUP_ID = "org.eclipse.m2e";

	/**
	 * The artifact ID of the Eclipse m2e lifecycle mapping pseudo plugin.
	 */
	public static final String M2E_ARTIFACT_ID = "lifecycle-mapping";

	/**
	 * The version of the Eclipse m2e lifecycle mapping pseudo plugin that m2e accepts.
	 */
	public static final String M2E_VERSION = "1.0.0";

	/**
	 * Configuration element of the m2e lifecycle mapping plugin.
	 */
	public static final String M2E_LIFECYCLE_MAPPING_METADATA = "lifecycleMappingMetadata";

	/**
	 * Child element of {@link #M2E_LIFECYCLE_MAPPING_METADATA} with the plugin execution mappings.
	 */
	public static final String M2E_PLUGIN_EXECUTIONS = "pluginExecutions";

	/**
	 * The configuration element of a plugin.
	 */
	public static final String CONFIGURATION = "configuration";

	private static final Logger LOG = LoggerFactory.getLogger(TLParentInjector.class);

	private final ModelBuilder _modelBuilder;

	private final ModelProcessor _modelProcessor;

	private final ProjectBuildingHelper _projectBuildingHelper;

	private final RepositorySystem _repositorySystem;

	private final RemoteRepositoryManager _remoteRepositoryManager;

	private final org.apache.maven.repository.RepositorySystem _artifactFactory;

	/**
	 * Creates a {@link TLParentInjector}.
	 */
	@Inject
	public TLParentInjector(ModelBuilder modelBuilder, ModelProcessor modelProcessor,
			ProjectBuildingHelper projectBuildingHelper, RepositorySystem repositorySystem,
			RemoteRepositoryManager remoteRepositoryManager,
			org.apache.maven.repository.RepositorySystem artifactFactory) {
		_modelBuilder = modelBuilder;
		_modelProcessor = modelProcessor;
		_projectBuildingHelper = projectBuildingHelper;
		_repositorySystem = repositorySystem;
		_remoteRepositoryManager = remoteRepositoryManager;
		_artifactFactory = artifactFactory;
	}

	@Override
	public void afterProjectsRead(MavenSession session) throws MavenExecutionException {
		String version = null;
		for (MavenProject project : session.getProjects()) {
			TLPackaging packaging = TLPackaging.fromId(project.getPackaging());
			if (packaging == null) {
				continue;
			}
			if (project.getContextValue(INJECTED_PARENT) != null) {
				continue;
			}
			if (!usesThisExtension(project)) {
				continue;
			}
			if (inheritsTLParent(project)) {
				LOG.debug("{} inherits from a TopLogic parent, nothing to inject.", project.getId());
				continue;
			}
			if (version == null) {
				version = ownVersion();
			}
			inject(session, project, packaging, version);
		}
	}

	/**
	 * Whether the given project declares the plugin of this participant as build extension.
	 *
	 * <p>
	 * Maven imports the realm of each build extension of a project into the project realm.
	 * Another version of this plugin brings its own participant.
	 * </p>
	 */
	private boolean usesThisExtension(MavenProject project) {
		ClassRealm realm = project.getClassRealm();
		if (realm == null) {
			return false;
		}
		ClassLoader ownRealm = getClass().getClassLoader();
		return realm == ownRealm || realm.getImportRealms().contains(ownRealm);
	}

	private static boolean inheritsTLParent(MavenProject project) {
		for (MavenProject parent = project.getParent(); parent != null; parent = parent.getParent()) {
			if (TLPackaging.isTLParent(parent.getGroupId(), parent.getArtifactId())) {
				return true;
			}
		}
		return false;
	}

	private String ownVersion() throws MavenExecutionException {
		try (InputStream in = TLParentInjector.class.getResourceAsStream(PLUGIN_POM_PROPERTIES)) {
			if (in == null) {
				throw new MavenExecutionException("Missing resource '" + PLUGIN_POM_PROPERTIES + "'.",
					(Throwable) null);
			}
			Properties properties = new Properties();
			properties.load(in);
			return properties.getProperty(VERSION_PROPERTY);
		} catch (IOException ex) {
			throw new MavenExecutionException("Cannot read '" + PLUGIN_POM_PROPERTIES + "'.", ex);
		}
	}

	private void inject(MavenSession session, MavenProject project, TLPackaging packaging, String version)
			throws MavenExecutionException {
		Parent tlParent = new Parent();
		tlParent.setGroupId(TLPackaging.TL_GROUP_ID);
		tlParent.setArtifactId(packaging.getParentArtifactId());
		tlParent.setVersion(version);
		// Resolve from the workspace or repository, never relative to the top-most POM.
		tlParent.setRelativePath("");

		LOG.info("Building {} with the configuration of {}:{}:{} as top-most parent.", project.getId(),
			tlParent.getGroupId(), tlParent.getArtifactId(), tlParent.getVersion());

		ProjectBuildingRequest projectRequest = project.getProjectBuildingRequest() != null
			? new DefaultProjectBuildingRequest(project.getProjectBuildingRequest())
			: new DefaultProjectBuildingRequest(session.getProjectBuildingRequest());

		ParentSplicer splicer = new ParentSplicer(tlParent, _modelProcessor);

		DefaultModelBuildingRequest request = new DefaultModelBuildingRequest();
		request.setPomFile(project.getFile());
		request.setModelSource(new SplicingModelSource(project.getFile(), splicer));
		request.setValidationLevel(projectRequest.getValidationLevel());
		request.setProcessPlugins(projectRequest.isProcessPlugins());
		request.setProfiles(projectRequest.getProfiles());
		request.setActiveProfileIds(projectRequest.getActiveProfileIds());
		request.setInactiveProfileIds(projectRequest.getInactiveProfileIds());
		request.setSystemProperties(projectRequest.getSystemProperties());
		request.setUserProperties(projectRequest.getUserProperties());
		request.setBuildStartTime(projectRequest.getBuildStartTime());
		request.setLocationTracking(true);
		request.setTwoPhaseBuilding(true);

		RequestTrace trace = RequestTrace.newChild(null, projectRequest).newChild(request);
		ProjectModelResolver resolver = new ProjectModelResolver(projectRequest.getRepositorySession(), trace,
			_repositorySystem, _remoteRepositoryManager,
			RepositoryUtils.toRepos(projectRequest.getRemoteRepositories()), projectRequest.getRepositoryMerging(),
			null);
		request.setModelResolver(new SplicingModelResolver(resolver, splicer));
		request.setModelBuildingListener(
			new DefaultModelBuildingListener(project, _projectBuildingHelper, projectRequest));

		ModelBuildingResult result;
		try {
			result = _modelBuilder.build(request);
			splicer.finish();
			result = _modelBuilder.build(request, result);
		} catch (ModelBuildingException ex) {
			throw new MavenExecutionException(
				"Cannot build " + project.getId() + " with the configuration of " + modelId(tlParent) + ": "
					+ ex.getMessage(),
				ex);
		}
		if (!splicer.isSpliced()) {
			throw new MavenExecutionException(
				"Cannot inject " + modelId(tlParent) + " into the parent chain of " + project.getId() + ".",
				project.getFile());
		}

		apply(project, result);
		addM2eLifecycleMapping(project, result, tlParent);
		project.setContextValue(INJECTED_PARENT, modelId(tlParent));
	}

	private void apply(MavenProject project, ModelBuildingResult result) {
		Model effective = result.getEffectiveModel();
		Model model = project.getModel();
		model.setBuild(effective.getBuild());
		model.setProperties(effective.getProperties());
		model.setDependencies(effective.getDependencies());
		model.setRepositories(effective.getRepositories());
		model.setPluginRepositories(effective.getPluginRepositories());
		model.setReporting(effective.getReporting());

		List<String> modelIds = result.getModelIds();
		List<Profile> activeProfiles = new ArrayList<>(result.getActivePomProfiles(modelIds.get(0)));
		activeProfiles.addAll(result.getActiveExternalProfiles());
		project.setActiveProfiles(activeProfiles);
		project.setInjectedProfileIds(EXTERNAL_PROFILES, profileIds(result.getActiveExternalProfiles()));
		for (String modelId : modelIds) {
			project.setInjectedProfileIds(modelId, profileIds(result.getActivePomProfiles(modelId)));
		}

		Set<Artifact> pluginArtifacts = new HashSet<>();
		for (Plugin plugin : project.getBuildPlugins()) {
			Artifact artifact = _artifactFactory.createPluginArtifact(plugin);
			if (artifact != null) {
				pluginArtifacts.add(artifact);
			}
		}
		project.setPluginArtifacts(pluginArtifacts);

		Set<Artifact> reportArtifacts = new HashSet<>();
		for (ReportPlugin report : project.getReportPlugins()) {
			Plugin plugin = new Plugin();
			plugin.setGroupId(report.getGroupId());
			plugin.setArtifactId(report.getArtifactId());
			plugin.setVersion(report.getVersion());
			Artifact artifact = _artifactFactory.createPluginArtifact(plugin);
			if (artifact != null) {
				reportArtifacts.add(artifact);
			}
		}
		project.setReportArtifacts(reportArtifacts);

		Set<Artifact> extensionArtifacts = new HashSet<>();
		for (Extension extension : project.getBuildExtensions()) {
			Artifact artifact = _artifactFactory.createArtifact(extension.getGroupId(), extension.getArtifactId(),
				extension.getVersion(), null, TLPackaging.ARTIFACT_EXTENSION);
			if (artifact != null) {
				extensionArtifacts.add(artifact);
			}
		}
		project.setExtensionArtifacts(extensionArtifacts);
	}

	private static List<String> profileIds(List<Profile> profiles) {
		List<String> result = new ArrayList<>(profiles.size());
		for (Profile profile : profiles) {
			result.add(profile.getId());
		}
		return result;
	}

	/**
	 * Makes the m2e lifecycle mappings of the injected POMs visible to Eclipse m2e.
	 *
	 * <p>
	 * m2e reads the lifecycle mappings from the original models of the project and its parent
	 * projects, which do not include the injected POMs. Their plugin execution mappings are
	 * therefore added to the original model of the project.
	 * </p>
	 */
	private static void addM2eLifecycleMapping(MavenProject project, ModelBuildingResult result,
			Parent tlParent) {
		List<Xpp3Dom> executions = new ArrayList<>();
		boolean injected = false;
		for (String modelId : result.getModelIds()) {
			if (modelId.equals(modelId(tlParent))) {
				injected = true;
			}
			if (!injected || modelId.isEmpty()) {
				continue;
			}
			Xpp3Dom injectedExecutions = m2eExecutions(result.getRawModel(modelId).getBuild());
			if (injectedExecutions != null) {
				for (Xpp3Dom execution : injectedExecutions.getChildren()) {
					executions.add(execution);
				}
			}
		}
		if (executions.isEmpty()) {
			return;
		}

		Model original = project.getOriginalModel();
		Build build = original.getBuild();
		if (build == null) {
			build = new Build();
			original.setBuild(build);
		}
		PluginManagement pluginManagement = build.getPluginManagement();
		if (pluginManagement == null) {
			pluginManagement = new PluginManagement();
			build.setPluginManagement(pluginManagement);
		}
		Plugin m2e = pluginManagement.getPluginsAsMap().get(M2E_GROUP_ID + ":" + M2E_ARTIFACT_ID);
		if (m2e == null) {
			m2e = new Plugin();
			m2e.setGroupId(M2E_GROUP_ID);
			m2e.setArtifactId(M2E_ARTIFACT_ID);
			m2e.setVersion(M2E_VERSION);
			pluginManagement.addPlugin(m2e);
		}
		Xpp3Dom configuration = (Xpp3Dom) m2e.getConfiguration();
		if (configuration == null) {
			configuration = new Xpp3Dom(CONFIGURATION);
			m2e.setConfiguration(configuration);
		}
		Xpp3Dom metadata = child(configuration, M2E_LIFECYCLE_MAPPING_METADATA);
		Xpp3Dom target = child(metadata, M2E_PLUGIN_EXECUTIONS);

		Set<String> present = new HashSet<>();
		for (Xpp3Dom execution : target.getChildren()) {
			present.add(execution.toString());
		}
		int added = 0;
		for (Xpp3Dom execution : executions) {
			if (present.add(execution.toString())) {
				target.addChild(new Xpp3Dom(execution));
				added++;
			}
		}
		LOG.debug("Added {} m2e plugin execution mappings of the injected POMs to {}.", added, project.getId());
	}

	/**
	 * The ID of the given parent in {@link ModelBuildingResult#getModelIds()}.
	 */
	private static String modelId(Parent parent) {
		return parent.getGroupId() + ":" + parent.getArtifactId() + ":" + parent.getVersion();
	}

	private static Xpp3Dom m2eExecutions(Build build) {
		if (build == null || build.getPluginManagement() == null) {
			return null;
		}
		Plugin m2e = build.getPluginManagement().getPluginsAsMap().get(M2E_GROUP_ID + ":" + M2E_ARTIFACT_ID);
		if (m2e == null || !(m2e.getConfiguration() instanceof Xpp3Dom)) {
			return null;
		}
		Xpp3Dom metadata = ((Xpp3Dom) m2e.getConfiguration()).getChild(M2E_LIFECYCLE_MAPPING_METADATA);
		return metadata == null ? null : metadata.getChild(M2E_PLUGIN_EXECUTIONS);
	}

	private static Xpp3Dom child(Xpp3Dom parent, String name) {
		Xpp3Dom result = parent.getChild(name);
		if (result == null) {
			result = new Xpp3Dom(name);
			parent.addChild(result);
		}
		return result;
	}

}
