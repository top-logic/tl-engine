/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.fieldprovider;

import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.element.meta.form.AbstractFieldProvider;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.FormFactory;

/**
 * {@link AbstractFieldProvider} to create a display display value field.
 * 
 * @see FormFactory#newDisplayField(String, Object, Renderer)
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ValueDisplayFieldProvider extends AbstractFieldProvider
		implements ConfiguredInstance<ValueDisplayFieldProvider.Config> {

	/**
	 * Typed configuration interface definition for {@link ValueDisplayFieldProvider}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	public interface Config extends PolymorphicConfiguration<ValueDisplayFieldProvider> {

		/**
		 * Configuration of the renderer which is used to render the value of the field.
		 */
		@Mandatory
		PolymorphicConfiguration<Renderer<?>> getRenderer();
	}

	private Config _config;

	private Renderer<?> _renderer;

	/**
	 * Create a {@link ValueDisplayFieldProvider}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public ValueDisplayFieldProvider(InstantiationContext context, Config config) {
		_config = config;
		_renderer = context.getInstance(config.getRenderer());
	}

	@Override
	public Config getConfig() {
		return _config;
	}

	@Override
	public FormMember createFormField(EditContext editContext, String fieldName) {
		return FormFactory.newDisplayField(fieldName, null, _renderer);
	}


}

