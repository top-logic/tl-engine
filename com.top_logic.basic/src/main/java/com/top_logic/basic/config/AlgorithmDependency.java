/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.Protocol;

/**
 * Dependency tree of a derived property.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class AlgorithmDependency {

	private final List<PropertyDependency> _dependencies;

	private AlgorithmDependency(List<PropertyDependency> dependencies) {
		_dependencies = dependencies;
	}

	/**
	 * Creates the {@link Updater} that is handles listener management.
	 */
	public Updater createUpdater() {
		if (_dependencies.size() == 1) {
			return _dependencies.get(0).createUpdater();
		} else {
			List<Updater> updaters = new ArrayList<>(_dependencies.size());
			for (PropertyDependency dependency : _dependencies) {
				updaters.add(dependency.createUpdater());
			}
			return new CombinedUpdater(updaters);
		}
	}

	/**
	 * Storage optimized variant of {@link #createUpdater()}.
	 * {@link Updater#install(ConfigurationItem, ConfigurationListener)}, but cannot be reverted,
	 * since there is no possibility to uninstall.
	 */
	void installFinally(ConfigurationItem item, ConfigurationListener updateListener) {
		for (PropertyDependency dependency : _dependencies) {
			dependency.createUpdater().install(item, updateListener);
		}
	}

	/**
	 * Creates an {@link AlgorithmDependency} for the given {@link ConfigurationDescriptor} that
	 * monitors the given reference paths.
	 * 
	 * @param protocol
	 *        Error log.
	 * @param descriptor
	 *        The model type.
	 * @param paths
	 *        The observed paths of {@link ConfigurationItem} properties.
	 * @return The created dependency manager.
	 */
	public static AlgorithmDependency createDependency(Protocol protocol, ConfigurationDescriptor descriptor,
			NamePath[] paths) {
		DependencyBuilder dependencyBuilder = DependencyBuilder.create(paths);
		List<PropertyDependency> dependencies = dependencyBuilder.createDependencies(protocol, descriptor);
		return new AlgorithmDependency(dependencies);
	}

}
