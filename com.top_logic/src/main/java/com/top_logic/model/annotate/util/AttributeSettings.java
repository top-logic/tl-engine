/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate.util;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.annotate.TLAnnotation;

/**
 * Configured settings for {@link TLStructuredTypePart} and {@link TLStructuredType}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class AttributeSettings extends ConfiguredManagedClass<AttributeSettings.Config> {

	/**
	 * Configuration options for {@link AttributeSettings}.
	 */
	public interface Config extends ConfiguredManagedClass.Config<AttributeSettings> {

		// No configuration given

	}

	/**
	 * Creates a new {@link AttributeSettings}.
	 */
	public AttributeSettings(InstantiationContext context, Config config) {
		super(context, config);
	}

	/**
	 * The {@link AttributeSettings} service.
	 */
	public static AttributeSettings getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * Singleton holder for the {@link AttributeSettings}.
	 */
	public static final class Module extends TypedRuntimeModule<AttributeSettings> {

		/**
		 * Singleton {@link AttributeSettings.Module} instance.
		 */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton constructor.
		}

		@Override
		public Class<AttributeSettings> getImplementation() {
			return AttributeSettings.class;
		}
	}

	/**
	 * Configured annotation for the given {@link TLStructuredTypePart} of the given annotation
	 * type.
	 * 
	 * @param annotationInterface
	 *        Class of the annotation that is desired.
	 * @param part
	 *        {@link TLStructuredTypePart} to get configured annotation from.
	 * 
	 * @return The configured annotation for the given part. May be <code>null</code>.
	 */
	public <T extends TLAnnotation> T getConfiguredPartAnnotation(Class<T> annotationInterface,
			TLStructuredTypePart part) {
		return null;
	}

	/**
	 * Configured annotation for the given {@link TLType} of the given annotation type.
	 * 
	 * @param annotationInterface
	 *        Class of the annotation that is desired.
	 * @param type
	 *        {@link TLType} to get configured annotation from.
	 * 
	 * @return The configured annotation for the given type. May be <code>null</code>.
	 */
	public <T extends TLAnnotation> T getConfiguredTypeAnnotation(Class<T> annotationInterface, TLType type) {
		return null;
	}

}

