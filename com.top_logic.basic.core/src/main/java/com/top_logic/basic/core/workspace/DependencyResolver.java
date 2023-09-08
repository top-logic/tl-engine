/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.core.workspace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;

/**
 * Algorithm creating a build path order from the dependencies of a Maven project.
 * 
 * @see #createBuildOrder()
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DependencyResolver {

	private Map<String, List<String>> _dependenciesById = new LinkedHashMap<>();

	private Map<String, String> _versionById = new LinkedHashMap<>();

	/**
	 * Enters a project model with its dependencies.
	 * 
	 * @param projectModel
	 *        The {@link Model} to add with its dependencies.
	 * @param isTest
	 *        Whether the model represents a "test" model.
	 * @return An ID for the given project, see {@link #createBuildOrder()}.
	 */
	public String enter(Model projectModel, boolean isTest) {
		// 
		// A  <- Test(A)
		// |
		// |------+
		// |      |
		// v      v
		// B <- Test(b)
		// |
		// |------+
		// |      |
		// v      v
		// C <- Test(C)
		// 
		String id = id(projectModel, isTest);
		List<String> dependencies =
			projectModel.getDependencies().stream().map(d -> id(d, true)).collect(Collectors.toList());
		if (isTest) {
			// Test depends on all tests of all dependencies (if they exist) and on the productive
			// part of the project.
			dependencies.add(id(projectModel, false));
		} else {
			// Productive part of a project depends on all tests of all dependencies (if they exist)
			// and on all productive parts of its dependencies. This ensures that a test resource of
			// a dependency must not overwrite productive resources of usages of that dependency.
			dependencies = projectModel.getDependencies().stream().map(d -> id(d)).collect(Collectors.toList());
		}

		String version = projectModel.getModelVersion();
		String versionClash = _versionById.put(id, version);
		if (versionClash != null && !versionClash.equals(version)) {
			throw new IllegalArgumentException(
				"Duplicate dependency: " + id + ":" + version + " vs. " + id + ":" + versionClash);
		}

		List<String> clash = _dependenciesById.put(id, dependencies);
		if (clash != null && !clash.equals(dependencies)) {
			// Join dependencies. This might happend in IDE mode, where dependencies from the plugin
			// and application are merged.
			HashSet<String> sum = new LinkedHashSet<>(clash);
			sum.addAll(dependencies);
			_dependenciesById.put(id, new ArrayList<>(sum));
		}
		return id;
	}

	private String id(Dependency dependency) {
		return id(dependency.getGroupId(), dependency.getArtifactId());
	}

	private String id(Dependency projectModel, boolean isTest) {
		return id(groupId(projectModel), projectModel.getArtifactId()) + (isTest ? ":tests" : "");
	}

	private String id(Model projectModel, boolean isTest) {
		return id(groupId(projectModel), projectModel.getArtifactId()) + (isTest ? ":tests" : "");
	}

	private String groupId(Dependency projectModel) {
		String groupId = projectModel.getGroupId();
		return groupId;
	}

	private String groupId(Model projectModel) {
		String groupId = projectModel.getGroupId();
		if (groupId != null) {
			return groupId;
		}
		return projectModel.getParent().getGroupId();
	}

	private String id(String groupId, String artifactId) {
		return groupId + ":" + artifactId;
	}

	/**
	 * A valid build path order of all projects added with calls to {@link #enter(Model, boolean)} before.
	 * 
	 * @return A list of IDs in build path order. In build path order, the ID of a project A has a
	 *         smaller index than the ID of a project B, if project B depends on project A.
	 */
	public List<String> createBuildOrder() {
		List<String> orderedIds = new ArrayList<>();
		Set<String> resolved = new HashSet<>();
		List<String> dependencyIds = new ArrayList<>(_dependenciesById.keySet());
		Collections.reverse(dependencyIds);
		for (String id : dependencyIds) {
			resolve(resolved, orderedIds, id);
		}
		return orderedIds;
	}

	private void resolve(Set<String> resolved, List<String> orderedIds, String id) {
		if (!resolved.add(id)) {
			return;
		}

		List<String> dependencies = _dependenciesById.get(id);
		if (dependencies != null) {
			// Since the order is built in reverse top-sort order, low-priority dependencies must
			// come first.
			for (int n = dependencies.size() - 1; n >= 0; n--) {
				resolve(resolved, orderedIds, dependencies.get(n));
			}
		}

		orderedIds.add(id);
	}

}
