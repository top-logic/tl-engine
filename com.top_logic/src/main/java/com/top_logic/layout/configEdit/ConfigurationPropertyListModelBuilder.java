/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.configEdit;

import java.util.Collection;
import java.util.Collections;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.customization.CustomizationContainer.PropertyCustomizationConfig;
import com.top_logic.mig.html.ListModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link ListModelBuilder} that creates entries by accessing a {@link PropertyDescriptor} of a
 * {@link ConfigurationItem} model.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ConfigurationPropertyListModelBuilder<C extends ConfigurationPropertyListModelBuilder.Config<?>>
		extends AbstractConfiguredInstance<C> implements ListModelBuilder {

	/**
	 * Configuration options for {@link ConfigurationPropertyListModelBuilder}.
	 */
	public interface Config<I extends ConfigurationPropertyListModelBuilder<?>> extends PolymorphicConfiguration<I> {

		/**
		 * @see #getProperty()
		 */
		String PROPERTY = "property";

		/**
		 * The property to access for generating the contents.
		 */
		@Name(PROPERTY)
		@Format(PropertyCustomizationConfig.PropertyReferenceFormat.class)
		String getProperty();

	}

	private PropertyDescriptor _property;

	/**
	 * Creates a {@link ConfigurationPropertyListModelBuilder} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ConfigurationPropertyListModelBuilder(InstantiationContext context, C config) throws ConfigurationException {
		super(context, config);
		_property =
			PropertyCustomizationConfig.PropertyReferenceFormat.parseReference(Config.PROPERTY, config.getProperty());
	}

	@Override
	public Collection<?> getModel(Object businessModel, LayoutComponent aComponent) {
		if (businessModel == null) {
			return Collections.emptyList();
		}
		ConfigurationItem model = (ConfigurationItem) businessModel;
		return (Collection<?>) model.value(_property);
	}

	@Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
		return aModel == null || _property.getDescriptor().getConfigurationInterface().isInstance(aModel);
	}

	@Override
	public boolean supportsListElement(LayoutComponent contextComponent, Object listElement) {
		return _property.getElementType().isInstance(listElement);
	}

	@Override
	public Object retrieveModelFromListElement(LayoutComponent contextComponent, Object listElement) {
		return null;
	}

}
