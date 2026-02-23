/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react;

import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.model.TLStructuredTypePart;

/**
 * Registry that maps TopLogic model types to React component modules.
 *
 * <p>
 * This allows model-driven table columns and form fields to resolve which React component should be
 * used for a given type part.
 * </p>
 */
public class ReactComponentRegistry extends ConfiguredManagedClass<ReactComponentRegistry.Config> {

	/**
	 * Configuration for {@link ReactComponentRegistry}.
	 */
	public interface Config extends ConfiguredManagedClass.Config<ReactComponentRegistry> {

		/**
		 * Mappings from type names to React module names.
		 */
		@Key(TypeMapping.TYPE)
		Map<String, TypeMapping> getTypeMappings();

	}

	/**
	 * Maps a TopLogic type name to a React module.
	 */
	public interface TypeMapping extends ConfigurationItem {

		/** Property name for {@link #getType()}. */
		String TYPE = "type";

		/**
		 * The qualified TopLogic type name (e.g. "tl.core:String").
		 */
		@Name(TYPE)
		@Mandatory
		String getType();

		/**
		 * The React module identifier to use for rendering (e.g. "TLTextInput").
		 */
		@Name("module")
		@Mandatory
		String getModule();

		/**
		 * Optional configuration to pass to the React component.
		 */
		@Name("config")
		String getConfig();

	}

	/**
	 * Creates a new {@link ReactComponentRegistry}.
	 */
	@CalledByReflection
	public ReactComponentRegistry(InstantiationContext context, Config config) {
		super(context, config);
	}

	/**
	 * Returns the React module for the cell control of the given type part.
	 *
	 * @param typePart
	 *        The model type part.
	 * @return The React module identifier, or {@code null} if no mapping is configured.
	 */
	public String getCellModule(TLStructuredTypePart typePart) {
		TypeMapping mapping = getMapping(typePart);
		return mapping != null ? mapping.getModule() : null;
	}

	/**
	 * Returns the configuration for the cell control of the given type part.
	 *
	 * @param typePart
	 *        The model type part.
	 * @return The configuration string, or {@code null} if no mapping or configuration is set.
	 */
	public String getCellConfig(TLStructuredTypePart typePart) {
		TypeMapping mapping = getMapping(typePart);
		return mapping != null ? mapping.getConfig() : null;
	}

	private TypeMapping getMapping(TLStructuredTypePart typePart) {
		String typeName = typePart.getType().toString();
		return getConfig().getTypeMappings().get(typeName);
	}

	/**
	 * Module class for {@link ReactComponentRegistry}.
	 */
	public static final class Module extends TypedRuntimeModule<ReactComponentRegistry> {

		/** Singleton instance. */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton.
		}

		@Override
		public Class<ReactComponentRegistry> getImplementation() {
			return ReactComponentRegistry.class;
		}

	}

}
