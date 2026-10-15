/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.build.maven.war;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.DefaultModelReader;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import com.top_logic.basic.core.workspace.DependencyResolver;
import com.top_logic.build.maven.resolve.EngineResolver;

/**
 * The TopLogic modules an application consists of, ordered by their dependencies, together with
 * the web fragments of the modules the application depends on.
 *
 * <p>
 * The {@link #getBuildOrder() build order} lists the most basic module first and the application
 * itself last. The {@link #resourceStack(List) resource stack} of the application is the reverse:
 * its first entry is the layer with the highest priority, i.e. a resource found there overrides the
 * same resource in all later entries.
 * </p>
 *
 * <p>
 * The archives opened while building the resource stack stay open until this stack is
 * {@link #close() closed}.
 * </p>
 */
public class AppModuleStack implements Closeable {

	private final String _appId;

	private final List<String> _buildOrder;

	private final Map<String, Artifact> _fragmentById;

	private final List<FileSystem> _archives = new ArrayList<>();

	private AppModuleStack(String appId, List<String> buildOrder, Map<String, Artifact> fragmentById) {
		_appId = appId;
		_buildOrder = buildOrder;
		_fragmentById = fragmentById;
	}

	/**
	 * Resolves the {@link AppModuleStack} of the given application project.
	 *
	 * <p>
	 * Each dependency of type <code>jar</code> that is a TopLogic web component (see
	 * {@link EngineResolver#isWebComponent(File)}) is a module of the application. Its web fragment
	 * and its project model are resolved through Maven.
	 * </p>
	 *
	 * @param project
	 *        The application project.
	 * @param resolveHelper
	 *        The {@link ResolveHelper} to resolve web fragments and project models with.
	 * @param log
	 *        The {@link Log} to report problems to.
	 * @param fragmentsRequired
	 *        Whether a web fragment that cannot be resolved is an error. Otherwise, the module is
	 *        left out of the {@link #resourceStack(List) resource stack} with a warning.
	 * @throws MojoExecutionException
	 *         If a required web fragment cannot be resolved.
	 * @throws MojoRuntimeException
	 *         If the project model of a module cannot be read.
	 */
	public static AppModuleStack resolve(MavenProject project, ResolveHelper resolveHelper, Log log,
			boolean fragmentsRequired) throws MojoExecutionException {
		DefaultModelReader modelReader = new DefaultModelReader();
		List<Module> modules = new ArrayList<>();
		for (Artifact dependency : project.getArtifacts()) {
			if (!"jar".equals(dependency.getType())) {
				continue;
			}
			File file = dependency.getFile();
			if (file == null) {
				log.info("Missing artifact: " + dependency);
				continue;
			}
			try {
				if (!EngineResolver.isWebComponent(file)) {
					continue;
				}
			} catch (IOException ex) {
				log.error("Failed to access '" + file + "': " + ex.getMessage(), ex);
				continue;
			}

			Artifact webFragment;
			try {
				webFragment = resolveHelper.resolveWebFragment(dependency);
			} catch (MojoExecutionException ex) {
				if (fragmentsRequired) {
					throw ex;
				}
				log.warn("Skipping web fragment of '" + dependency + "': " + ex.getMessage());
				webFragment = null;
			}
			Artifact pom = resolveHelper.resolve(dependency, "pom", null);
			try {
				modules.add(new Module(modelReader.read(pom.getFile(), null), webFragment));
			} catch (IOException ex) {
				throw new MojoRuntimeException("Failed to access project model of '" + dependency + "'", ex);
			}
		}
		return create(project.getModel(), modules);
	}

	/**
	 * Creates an {@link AppModuleStack} from the given project models.
	 *
	 * @param appModel
	 *        The project model of the application.
	 * @param modules
	 *        The modules the application depends on, in the order of the project's dependency
	 *        artifacts.
	 */
	public static AppModuleStack create(Model appModel, List<Module> modules) {
		DependencyResolver resolver = new DependencyResolver();
		String appId = resolver.enter(appModel, false);
		Map<String, Artifact> fragmentById = new HashMap<>();
		for (Module module : modules) {
			String id = resolver.enter(module.model(), false);
			if (module.fragment() != null) {
				fragmentById.put(id, module.fragment());
			}
		}
		return new AppModuleStack(appId, resolver.createBuildOrder(), fragmentById);
	}

	/**
	 * The ID of the application module in the {@link #getBuildOrder() build order}.
	 */
	public String getAppId() {
		return _appId;
	}

	/**
	 * The IDs of all modules, the most basic module first.
	 *
	 * <p>
	 * A module of the build order depends only on modules listed before it. The list also contains
	 * the {@link #getAppId() application} and modules without a {@link #getFragment(String) web
	 * fragment}.
	 * </p>
	 */
	public List<String> getBuildOrder() {
		return _buildOrder;
	}

	/**
	 * The web fragment of the module with the given ID.
	 *
	 * @return The web fragment, or <code>null</code> for the {@link #getAppId() application}, for
	 *         a module whose web fragment was not resolved, and for an ID that names no TopLogic
	 *         module.
	 */
	public Artifact getFragment(String id) {
		return _fragmentById.get(id);
	}

	/**
	 * The resource stack of the application, highest priority first.
	 *
	 * @param appLayer
	 *        The resource roots of the application itself, highest priority first. They take the
	 *        position of the {@link #getAppId() application} in the stack.
	 * @return The given application layer followed by the root directories of the web fragments,
	 *         from the most dependent module to the most basic one.
	 */
	public List<Path> resourceStack(List<Path> appLayer) throws IOException {
		List<Path> result = new ArrayList<>();
		for (ListIterator<String> it = _buildOrder.listIterator(_buildOrder.size()); it.hasPrevious();) {
			String id = it.previous();
			if (id.equals(_appId)) {
				result.addAll(appLayer);
			} else {
				Artifact fragment = _fragmentById.get(id);
				if (fragment != null) {
					result.addAll(openArchive(fragment.getFile().toPath()));
				}
			}
		}
		return result;
	}

	/**
	 * Opens the given archive as file system that stays open until this stack is
	 * {@link #close() closed}.
	 *
	 * @return The root directories of the archive.
	 */
	public List<Path> openArchive(Path archive) throws IOException {
		FileSystem fileSystem = FileSystems.newFileSystem(archive);
		_archives.add(fileSystem);

		List<Path> roots = new ArrayList<>();
		fileSystem.getRootDirectories().forEach(roots::add);
		return roots;
	}

	/**
	 * Closes all archives opened by {@link #resourceStack(List)} and {@link #openArchive(Path)}.
	 */
	@Override
	public void close() throws IOException {
		IOException problem = null;
		for (FileSystem archive : _archives) {
			try {
				archive.close();
			} catch (IOException ex) {
				if (problem == null) {
					problem = ex;
				} else {
					problem.addSuppressed(ex);
				}
			}
		}
		_archives.clear();
		if (problem != null) {
			throw problem;
		}
	}

	/**
	 * A TopLogic module the application depends on.
	 *
	 * @param model
	 *        The project model of the module.
	 * @param fragment
	 *        The web fragment of the module, or <code>null</code> if it was not resolved.
	 */
	public record Module(Model model, Artifact fragment) {
		// Pure data.
	}

}
