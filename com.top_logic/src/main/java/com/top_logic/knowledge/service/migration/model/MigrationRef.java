/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.migration.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.knowledge.service.migration.MigrationConfig;
import com.top_logic.knowledge.service.migration.Version;

/**
 * Information about a migration to be performed.
 */
public class MigrationRef {
	private final VersionID _id;

	private MigrationConfig _config;

	private Map<String, MigrationRef> _dependencyByModule = new HashMap<>();

	private Map<String, String> _reasonByModule = new HashMap<>();

	private List<Dependency> _reverseDependencies = new ArrayList<>();

	private MigrationRef _successor;

	private int _order;

	/**
	 * Creates a {@link MigrationRef}.
	 *
	 * @param id
	 *        The version identifier.
	 */
	public MigrationRef(VersionID id) {
		_id = id;
	}

	/**
	 * Lookup or create a migration reference for the given version.
	 *
	 * @param migrations
	 *        All known migrations.
	 * @param version
	 *        The version.
	 * @return An existing reference from the given pool, or a new uninitialized reference.
	 */
	public static MigrationRef forVersion(Map<VersionID, MigrationRef> migrations, Version version) {
		return migrations.computeIfAbsent(VersionID.of(version), id -> new MigrationRef(id));
	}

	/**
	 * Initializes this reference with the actual migration configuration.
	 */
	public void initConfig(MigrationConfig config) {
		assert _config == null;
		_config = config;
	}

	private void addDependency(Dependency dependency) {
		addDependency(dependency.getMigration());
		_reasonByModule.put(dependency.getMigration().getModule(), dependency.getReason());
	}

	/**
	 * Adds a dependency that must be executed before this migation.
	 */
	public void addDependency(MigrationRef dependency) {
		MigrationRef clash = updateDependency(dependency);
		assert clash == null : "Non-unique dependency for '" + getId() + "' in module '" + dependency.getModule()
			+ "': " + dependency.getId() + " vs. " + clash.getId();
	}

	/**
	 * Replaces a potentially existing dependency with a new one.
	 */
	public MigrationRef updateDependency(MigrationRef dependency) {
		return _dependencyByModule.put(dependency.getModule(), dependency);
	}

	/**
	 * The migration in the same module, this one is based on, <code>null</code> for the initial
	 * version of a module.
	 */
	public MigrationRef getPredecessor() {
		return _dependencyByModule.get(_id.getModule());
	}

	/**
	 * All direct dependencies that must be executed before this migration.
	 */
	public Collection<MigrationRef> getDependencies() {
		return _dependencyByModule.values();
	}

	/**
	 * All direct dependencies indexed by module.
	 */
	public Map<String, MigrationRef> getDependencyByModule() {
		return _dependencyByModule;
	}

	/**
	 * The name of the module this migration must be run in.
	 */
	public String getModule() {
		return _id.getModule();
	}

	/**
	 * The migration in the given module, this one is based on. <code>null</code> means that the
	 * module of this migration does not depend on the given module or there is no migration in the
	 * given module.
	 */
	public MigrationRef getDependency(String module) {
		return _dependencyByModule.get(module);
	}

	/**
	 * The reason for a dependency to the given module.
	 */
	public String getReason(String module) {
		return _reasonByModule.get(module);
	}

	/**
	 * Additional dependencies introduced during dependency analysis.
	 */
	public void addSyntheticDependency(MigrationRef predecessor, String reason) {
		assert predecessor != null;
		_reverseDependencies.add(new Dependency(predecessor, reason));
	}

	/**
	 * Flushes synthetic dependencies into the regular dependencies.
	 */
	public void updateDependencies() {
		for (Dependency dependency : _reverseDependencies) {
			addDependency(dependency);
		}
	}

	/**
	 * The migration configuration.
	 */
	public MigrationConfig getConfig() {
		return _config;
	}

	@Override
	public String toString() {
		return _id.toString();
	}

	private VersionID getId() {
		return _id;
	}

	/**
	 * Initializes the {@link #getSuccessor()} relation.
	 */
	public void initSucessor() {
		MigrationRef predecessor = getPredecessor();
		if (predecessor != null) {
			predecessor.initSuccessor(this);
		}
	}

	/**
	 * The successor in the same module.
	 */
	public MigrationRef getSuccessor() {
		return _successor;
	}

	private void initSuccessor(MigrationRef successor) {
		if (_successor != null) {
			throw new IllegalArgumentException(
				"Non-unique module-local successor for migration '" + getId() + "': " + _successor.getId()
					+ " vs. " + successor.getId());
		}
		_successor = successor;
	}

	/**
	 * The migration that has no {@link #getSuccessor()}
	 */
	public MigrationRef findLatest() {
		MigrationRef result = this;
		while (true) {
			MigrationRef successor = result.getSuccessor();
			if (successor == null) {
				break;
			}
			result = successor;
		}
		return result;
	}

	/**
	 * Initialized module-local order index (latest is zero, older ones have lower index).
	 * 
	 * <p>
	 * Must be called on the latest one.
	 * </p>
	 */
	public void initLocalOrder() {
		int order = 0;

		MigrationRef ancestor = this;
		while (ancestor != null) {
			ancestor._order = order--;
			ancestor = ancestor.getPredecessor();
		}
	}

	/**
	 * Whether this one is newer than the other one.
	 * 
	 * <p>
	 * Requires {@link #initLocalOrder()} to be called.
	 * </p>
	 */
	public boolean isNewerThan(MigrationRef dependency) {
		assert getModule().equals(dependency.getModule()) : "Must only compare module-local order.";

		return _order > dependency._order;
	}
}

