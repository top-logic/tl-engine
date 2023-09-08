/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.top_logic.basic.Protocol;
import com.top_logic.basic.config.NamePath.PathStep;
import com.top_logic.basic.config.annotation.Derived;

/**
 * Temporary data extracted from {@link Derived} annotations for creating an
 * {@link AlgorithmDependency}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
class DependencyBuilder {

	/**
	 * Creates a {@link DependencyBuilder}.
	 * 
	 * @param paths Property paths to all dependency properties.
	 */
	public static DependencyBuilder create(NamePath[] paths) {
		DependencyBuilder result = new DependencyBuilder();
		for (NamePath path : paths) {
			result.addPath(path);
		}
		return result;
	}

	private final Map<PathStep, DependencyBuilder> _data = new LinkedHashMap<>();

	private DependencyBuilder() {
		// Reduce visibility.
	}

	private void addPath(NamePath propertyNames) {
		DependencyBuilder node = this;
		for (PathStep propertyName : propertyNames.toArray()) {
			node = node.add(propertyName);
		}
	}

	private DependencyBuilder add(PathStep propertyRef) {
		DependencyBuilder next = _data.get(propertyRef);
		if (next == null) {
			next = new DependencyBuilder();
			_data.put(propertyRef, next);
		}
		return next;
	}

	/**
	 * Creates the actual dependency implementations.
	 * 
	 * @param protocol
	 *        The error log.
	 * @param descriptor
	 *        The model type, the dependencies should be applied to.
	 * @return The created dependency implementations. Never null.
	 */
	public List<PropertyDependency> createDependencies(Protocol protocol, ConfigurationDescriptor descriptor) {
		Set<Entry<PathStep, DependencyBuilder>> dependencySrc = _data.entrySet();
		List<PropertyDependency> dependencies = new ArrayList<>(dependencySrc.size());
		for (Entry<PathStep, DependencyBuilder> entry : dependencySrc) {
			PathStep propertyRef = entry.getKey();
			PropertyDescriptor pathProperty = propertyRef.resolve(descriptor);
			if (pathProperty == null) {
				protocol.error("There is no property '" + propertyRef + "' in configuration interface '"
					+ descriptor.getConfigurationInterface().getName()
					+ "' referenced from derived property algorithm dependencies.");
				continue;
			}

			DependencyBuilder nextDependency = entry.getValue();
			dependencies.add(nextDependency.buildPropertyDependency(protocol, pathProperty));
		}
		return dependencies;
	}

	private PropertyDependency buildPropertyDependency(Protocol protocol, PropertyDescriptor property) {
		return new PropertyDependency(property, createNextDependencies(protocol, property));
	}

	private List<PropertyDependency> createNextDependencies(Protocol protocol, PropertyDescriptor property) {
		if (hasDependencies()) {
			return createDependencies(protocol, property.getValueDescriptor());
		} else {
			return Collections.emptyList();
		}
	}

	private boolean hasDependencies() {
		return !_data.isEmpty();
	}

}
