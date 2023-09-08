/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.fieldprovider.format;

import java.text.Format;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.json.JSON.DefaultValueAnalyzer;
import com.top_logic.basic.json.JSON.DefaultValueFactory;
import com.top_logic.basic.json.JSON.ValueAnalyzer;
import com.top_logic.basic.json.JSON.ValueFactory;
import com.top_logic.basic.json.JSONFormat;

/**
 * {@link FormatProvider} creating {@link JSONFormat}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class JSONFormatProvider extends AbstractConfiguredInstance<PolymorphicConfiguration<?>>
		implements FormatProvider {

	/**
	 * Typed configuration interface definition for {@link JSONFormatProvider}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	public interface Config extends PolymorphicConfiguration<JSONFormatProvider> {

		/**
		 * The {@link ValueAnalyzer} that interprets the stored JSON string and reconstructs the
		 * JSON object.
		 */
		@NonNullable
		@ItemDefault(DefaultValueAnalyzer.class)
		PolymorphicConfiguration<ValueAnalyzer> getAnalyzer();

		/**
		 * The {@link ValueFactory} that creates the actual string representation of the stored JSON
		 * element.
		 */
		@NonNullable
		@ItemDefault(DefaultValueFactory.class)
		PolymorphicConfiguration<ValueFactory> getFactory();

	}

	private ValueAnalyzer _analyzer;

	private ValueFactory _factory;

	/**
	 * Create a {@link JSONFormatProvider}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public JSONFormatProvider(InstantiationContext context, Config config) {
		super(context, config);
		_analyzer = context.getInstance(config.getAnalyzer());
		_factory = context.getInstance(config.getFactory());
	}

	@Override
	public Format createFormat() throws ConfigurationException {
		return new JSONFormat().setFactoryAndAnalyzer(_factory, _analyzer);
	}

}

