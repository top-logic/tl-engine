/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.react.temperature;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.access.StorageMapping;
import com.top_logic.model.internal.AbstractConfiguredStorageMapping;

/**
 * {@link StorageMapping} of the model type <code>demo.temperature:Temperature</code>, storing a
 * {@link Temperature} as a number of degrees Celsius.
 *
 * <p>
 * The mapping knows no context: a value it reads from the database is displayed in degrees
 * Celsius. The unit of the object the value belongs to is attached by {@link TemperatureStorage}.
 * </p>
 *
 * <p>
 * Besides a {@link Temperature}, a plain {@link Number} is accepted as value and taken as degrees
 * Celsius, so that a script or an import may set a temperature without knowing the value class.
 * </p>
 */
public class TemperatureMapping extends AbstractConfiguredStorageMapping<TemperatureMapping.Config, Temperature> {

	/**
	 * Configuration options for {@link TemperatureMapping}.
	 */
	@TagName("temperature-mapping")
	public interface Config extends PolymorphicConfiguration<TemperatureMapping> {
		// No options.
	}

	/**
	 * Creates a {@link TemperatureMapping} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public TemperatureMapping(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public Class<Temperature> getApplicationType() {
		return Temperature.class;
	}

	@Override
	public Temperature getBusinessObject(Object storageObject) {
		if (storageObject == null) {
			return null;
		}
		return Temperature.ofCelsius(((Number) storageObject).doubleValue());
	}

	@Override
	public Object getStorageObject(Object businessObject) {
		if (businessObject == null) {
			return null;
		}
		if (businessObject instanceof Temperature temperature) {
			return Double.valueOf(temperature.getCelsius());
		}
		return Double.valueOf(((Number) businessObject).doubleValue());
	}

	@Override
	public boolean isCompatible(Object businessObject) {
		return businessObject == null || businessObject instanceof Temperature || businessObject instanceof Number;
	}

}
