/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.storage.mappings;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.json.JSON.DefaultValueAnalyzer;
import com.top_logic.basic.json.JSON.DefaultValueFactory;
import com.top_logic.basic.json.JSON.ParseException;
import com.top_logic.basic.json.JSON.ValueAnalyzer;
import com.top_logic.basic.json.JSON.ValueFactory;
import com.top_logic.model.internal.AbstractConfiguredStorageMapping;

/**
 * {@link AbstractConfiguredStorageMapping} storing JSON elements into a string column.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class JSONStorageMapping extends AbstractConfiguredStorageMapping<JSONStorageMapping.Config, Object> {

	/**
	 * Typed configuration interface definition for {@link JSONStorageMapping}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	public interface Config extends PolymorphicConfiguration<JSONStorageMapping> {

		/** 
		 * The {@link ValueAnalyzer} that interprets the stored JSON string and reconstructs the JSON object. 
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

	private final ValueAnalyzer _analyzer;

	private final ValueFactory _factory;

	/**
	 * Create a {@link JSONStorageMapping}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public JSONStorageMapping(InstantiationContext context, Config config) {
		super(context, config);
		_analyzer = context.getInstance(config.getAnalyzer());
		_factory = context.getInstance(config.getFactory());
	}

	@Override
	public Class<? extends Object> getApplicationType() {
		return Object.class;
	}

	@Override
	public Object getBusinessObject(Object aStorageObject) {
		if (aStorageObject == null) {
			return null;
		}
		try {
			return JSON.fromString((CharSequence) aStorageObject, _factory);
		} catch (ParseException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public Object getStorageObject(Object aBusinessObject) {
		if (aBusinessObject == null) {
			return null;
		}
		return JSON.toString(_analyzer, aBusinessObject);
	}

	@Override
	public boolean isCompatible(Object businessObject) {
		if (businessObject == null) {
			return true;
		}
		return _analyzer.getType(businessObject) != ValueAnalyzer.UNSUPPORTED_TYPE;
	}

}

