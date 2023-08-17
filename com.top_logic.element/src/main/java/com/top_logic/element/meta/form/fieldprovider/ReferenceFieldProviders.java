/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.fieldprovider;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.form.FieldProvider;

/**
 * {@link ConfiguredManagedClass} that handles creation of {@link FieldProvider} for reference
 * attributes.
 * 
 * @see ReferenceFieldProvider
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ReferenceFieldProviders extends ConfiguredManagedClass<ReferenceFieldProviders.Config> {

	/**
	 * Configuration for {@link ReferenceFieldProviders}.
	 */
	public interface Config extends ConfiguredManagedClass.Config<ReferenceFieldProviders> {

		/**
		 * {@link FieldProvider} that is used for composition attributes.
		 */
		@ImplementationClassDefault(CompositionFieldProvider.class)
		@ItemDefault
		PolymorphicConfiguration<? extends FieldProvider> getCompositionFieldProvider();

		/**
		 * {@link FieldProvider} that is used for an ordinary reference valued attribute.
		 */
		@ImplementationClassDefault(WrapperFieldProvider.class)
		@ItemDefault
		PolymorphicConfiguration<? extends FieldProvider> getDefaultReferenceFieldProvider();

	}

	private final FieldProvider _composition;

	private final FieldProvider _default;

	/**
	 * Creates a new {@link ReferenceFieldProviders}.
	 */
	public ReferenceFieldProviders(InstantiationContext context, Config config) {
		super(context, config);
		_composition = context.getInstance(config.getCompositionFieldProvider());
		_default = context.getInstance(config.getDefaultReferenceFieldProvider());
	}
	
	/**
	 * Determines the {@link FieldProvider} to use for the given edit context.
	 */
	public FieldProvider delegate(EditContext editContext) {
		if (editContext.isComposition()) {
			return _composition;
		}
		return _default;
	}

	/**
	 * Module for instantiation of the {@link ReferenceFieldProviders}.
	 */
	public static class Module extends TypedRuntimeModule<ReferenceFieldProviders> {

		/** Singleton for this module. */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton constructor.
		}

		@Override
		public Class<ReferenceFieldProviders> getImplementation() {
			return ReferenceFieldProviders.class;
		}

	}

}

