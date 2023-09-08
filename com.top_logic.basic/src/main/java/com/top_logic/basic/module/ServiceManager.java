/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.module;

import java.util.Collection;

import com.top_logic.basic.Configuration.IterableConfiguration;
import com.top_logic.basic.config.ConfigurationException;

/**
 * The class {@link ServiceManager} is used in
 * {@link MultiRuntimeModule} and manages the inner services, i.e. it knows how
 * to determine dependencies and how to create new instances.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ServiceManager<M extends ManagedClass> {

	/**
	 * creates a new instance of the service from the given configuration.
	 */
	public M createImplementation(IterableConfiguration config) throws ModuleException, ConfigurationException;

	/**
	 * Determines the dependencies of the service configured with the given
	 * configuration.
	 */
	public Collection<? extends Class<? extends BasicRuntimeModule<?>>> getDependencies(IterableConfiguration config) throws ConfigurationException;

}
