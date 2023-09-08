/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.MapBinding;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.ModuleException;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.util.Utils;

/**
 * {@link SystemProperties} reads a section with system properties from the
 * configuration and transforms the read properties to
 * {@link System#getProperties() system properties}.
 * 
 * No system properties are set in top-logic, but some examples are provided.
 * This may not work with a Security manager, as this may break global security.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SystemProperties extends ManagedClass {
	
	/**
	 * Configuration for system properties.
	 * 
	 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
	 */
	public interface Config extends ServiceConfiguration<SystemProperties> {
		/**
		 * System properties.
		 */
		@MapBinding(tag = "system-property")
		Map<String, String> getSystemProperties();
	}

	private final Map<String, String> _systemProperties;

	/**
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        Configuration for {@link SystemProperties}.
	 * @throws ModuleException
	 *         System property is already set.
	 */
	public SystemProperties(InstantiationContext context, Config config) throws ModuleException {
		super(context, config);
		_systemProperties = config.getSystemProperties();

	}

	@Override
	protected void startUp() {
		if (!_systemProperties.isEmpty()) {
			Set<String> installedProps = new HashSet<>();
			for (String key : _systemProperties.keySet()) {
				if (System.getProperty(key) == null) {
					System.setProperty(key, _systemProperties.get(key));
					installedProps.add(key);
					Logger.info("System property '" + key + "' successfully installed.", SystemProperties.class);
				} else {
					throw handleClash(key, installedProps);
				}
			}
		}
		super.startUp();
	}

	private IllegalStateException handleClash(String key, Set<String> installedProps) {
		for (String prop : installedProps) {
			System.clearProperty(prop);
		}
		throw new IllegalStateException("There is already a system property with key '" + key + "'");
	}

	@Override
	protected void shutDown() {
		for (String key : _systemProperties.keySet()) {
			if (Utils.equals(_systemProperties.get(key), System.getProperty(key))) {
				System.clearProperty(key);
			} else {
				Logger.warn("Property '" + key + "' was modified concurrently. Property is not reset.",
					SystemProperties.class);
			}
		}
		super.shutDown();
	}

	/**
	 * Module for {@link SystemProperties}.
	 * 
	 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
	 */
	public static final class Module extends TypedRuntimeModule<SystemProperties> {

		/**
		 * Module Instance
		 */
		public static final SystemProperties.Module INSTANCE = new SystemProperties.Module();

		@Override
		public Class<SystemProperties> getImplementation() {
			return SystemProperties.class;
		}
	}

}
