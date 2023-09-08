/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.keystorages;

import java.util.Collections;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;

/**
 * Registry for {@link KeyStorageChecker}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class KeyStorageCheckerRegistry extends ConfiguredManagedClass<KeyStorageCheckerRegistry.Config> {

	/**
	 * {@link com.top_logic.basic.module.ManagedClass.ServiceConfiguration} for the
	 * {@link KeyStorageCheckerRegistry}.
	 */
	public interface Config extends ConfiguredManagedClass.Config<KeyStorageCheckerRegistry> {

		/**
		 * The {@link List} of {@link KeyStorageChecker}s.
		 */
		@InstanceFormat
		List<KeyStorageChecker> getCheckers();

	}

	private final List<KeyStorageChecker> _checkers;

	/**
	 * Called by the {@link TypedConfiguration} for creating a {@link KeyStorageCheckerRegistry}.
	 * <p>
	 * <b>Don't call directly.</b> Use
	 * {@link InstantiationContext#getInstance(PolymorphicConfiguration)} instead.
	 * </p>
	 * 
	 * @param context
	 *        For error reporting and instantiation of dependent configured objects.
	 * @param config
	 *        The configuration for the new instance.
	 */
	@CalledByReflection
	public KeyStorageCheckerRegistry(InstantiationContext context, Config config) {
		super(context, config);
		_checkers = Collections.unmodifiableList(getConfig().getCheckers());
	}

	/**
	 * The configured {@link KeyStorageChecker}.
	 * <p>
	 * The returned {@link List} is unmodifiable.
	 * </p>
	 */
	public List<KeyStorageChecker> getCheckers() {
		return _checkers;
	}

	/**
	 * The singleton {@link KeyStorageCheckerRegistry} instance.
	 */
	public static KeyStorageCheckerRegistry getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * {@link TypedRuntimeModule} of the {@link KeyStorageCheckerRegistry}.
	 */
	public static final class Module extends TypedRuntimeModule<KeyStorageCheckerRegistry> {

		/**
		 * Singleton module instance.
		 */
		public static final Module INSTANCE = new Module();

		private Module() {
			// singleton
		}

		@Override
		public Class<KeyStorageCheckerRegistry> getImplementation() {
			return KeyStorageCheckerRegistry.class;
		}

	}

}
