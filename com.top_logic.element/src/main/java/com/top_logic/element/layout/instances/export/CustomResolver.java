/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.instances.export;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.model.TLClass;
import com.top_logic.model.instance.exporter.XMLInstanceExporter;
import com.top_logic.model.instance.importer.resolver.InstanceResolver;
import com.top_logic.model.util.TLModelPartRef;

/**
 * Specification of an object resolver that should be used exclusively for this export.
 * 
 * <p>
 * Note: In general, it is better to annotate the resolver to the referenced type, since a local
 * resolver must also be present for the corresponding import functionality to allow resolving the
 * reference. Therefore, this customization should only be used, if modifying the model is not an
 * option.
 * </p>
 */
public class CustomResolver extends AbstractConfiguredInstance<CustomResolver.Config<?>>
		implements ExportCustomization {

	/**
	 * Configuration options for {@link CustomResolver}.
	 */
	public interface Config<I extends CustomResolver> extends PolymorphicConfiguration<I> {
		/**
		 * The type to use the resolver for.
		 */
		@Mandatory
		TLModelPartRef getType();

		/**
		 * The resolver implementation for the specified {@link #getType()}.
		 */
		@Mandatory
		@Options(fun = AllInAppImplementations.class)
		PolymorphicConfiguration<? extends InstanceResolver> getResolver();
	}

	private final TLClass _type;
	private final InstanceResolver _resolver;

	/**
	 * Creates a {@link CustomResolver} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public CustomResolver(InstantiationContext context, Config<?> config) throws ConfigurationException {
		super(context, config);
		_type = config.getType().resolveClass();
		_resolver = context.getInstance(config.getResolver());
	}

	@Override
	public void perform(XMLInstanceExporter exporter) {
		exporter.addResolver(_type, _resolver);
	}

}
