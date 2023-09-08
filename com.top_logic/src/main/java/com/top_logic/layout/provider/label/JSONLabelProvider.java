/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.provider.label;

import java.io.IOError;
import java.io.IOException;
import java.io.StringWriter;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.json.JSON.DefaultValueAnalyzer;
import com.top_logic.basic.json.JSON.ValueAnalyzer;
import com.top_logic.layout.LabelProvider;

/**
 * {@link LabelProvider} that creates a serialised form for a JSON object.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class JSONLabelProvider extends AbstractConfiguredInstance<JSONLabelProvider.Config> implements LabelProvider {

	/**
	 * Typed configuration interface definition for {@link JSONLabelProvider}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	public interface Config extends PolymorphicConfiguration<JSONLabelProvider> {

		/**
		 * Whether the output is pretty formatted.
		 */
		boolean isPretty();

		/**
		 * Setter for {@link #isPretty()}.
		 */
		void setPretty(boolean value);

		/**
		 * The {@link ValueAnalyzer} that interprets the stored JSON string and reconstructs the
		 * JSON object.
		 */
		@ItemDefault(DefaultValueAnalyzer.class)
		@NonNullable
		PolymorphicConfiguration<ValueAnalyzer> getAnalyzer();

		/**
		 * Setter for {@link #getAnalyzer()}.
		 */
		void setAnalyzer(PolymorphicConfiguration<ValueAnalyzer> value);

	}

	private ValueAnalyzer _analyzer;

	/**
	 * Create a {@link JSONLabelProvider}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public JSONLabelProvider(InstantiationContext context, Config config) {
		super(context, config);
		_analyzer = context.getInstance(config.getAnalyzer());
	}

	@Override
	public String getLabel(Object object) {
		try (StringWriter out = new StringWriter()) {
			JSON.write(out, _analyzer, object, getConfig().isPretty());
			return out.toString();
		} catch (IOException ex) {
			throw new IOError(ex);
		}
	}

}

