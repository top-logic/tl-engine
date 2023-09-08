/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.module;

import java.util.Collection;

import com.top_logic.basic.Configuration;
import com.top_logic.basic.Configuration.IterableConfiguration;
import com.top_logic.basic.XMLProperties;
import com.top_logic.basic.config.ConfigurationException;

/**
 * {@link BasicRuntimeModule} which additionally reads the configuration for the
 * implementation from the properties.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class RuntimeModule<M extends ManagedClass> extends BasicRuntimeModule<M> {

	protected RuntimeModule() {
	}

	protected abstract M newImplementationInstance(IterableConfiguration config) throws ModuleException, ConfigurationException;

	/**
	 * Return value must contain the service module of {@link XMLProperties} or
	 * a dependent of it to ensure that reloading {@link XMLProperties} will
	 * reload this service.
	 * 
	 * @see com.top_logic.basic.module.BasicRuntimeModule#getDependencies()
	 */
	@Override
	public abstract Collection<? extends Class<? extends BasicRuntimeModule<?>>> getDependencies();

	@Override
	protected final M newImplementationInstance() throws ModuleException {
		try {
			return newImplementationInstance(getServiceConfiguration());
		} catch (ConfigurationException ex) {
			throw new ModuleException("Unable to create instance for '" + getImplementation().getName() + "' due to configuration problems.", ex, this.getImplementation());
		}
	}

	/**
	 * Can not create the configuration during construction as it accesses
	 * {@link #getImplementation()} which may not be initialized at that time,
	 * e.g. {@link MultiRuntimeModule}
	 */
	protected IterableConfiguration getServiceConfiguration() {
		IterableConfiguration instanceConfig;
		String configurationSection = getConfigurationSection();
		if (configurationSection != null) {
			instanceConfig = Configuration.getConfigurationByName(configurationSection);
		} else {
			instanceConfig = Configuration.getConfiguration(getImplementation());
		}
		return instanceConfig;
	}

	/**
	 * Returns the section form which the configuration is read. May be
	 * <code>null</code>. In this case the section is determined from the
	 * {@link #getImplementation() implementation class}.
	 */
	protected String getConfigurationSection() {
		return null;
	}

	@Override
	protected void addToString(StringBuilder result) {
		super.addToString(result);
		String configSection = getConfigurationSection();
		if (configSection != null) {
			result.append(",configSection:'");
			result.append(configSection);
			result.append('\'');
		}
	}

}
