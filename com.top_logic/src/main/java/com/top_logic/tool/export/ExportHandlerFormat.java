/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.export;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationValueProvider;

/**
 * {@link AbstractConfigurationValueProvider} parsing a comma separated list of {@link ExportHandler
 * export handler names} to a mapping from the name of the handler to the handler itself.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ExportHandlerFormat extends AbstractConfigurationValueProvider<Map<String, ExportHandler>> {

	/** Singleton instance of {@link ExportHandlerFormat} */
	public static final ConfigurationValueProvider<Map<String, ExportHandler>> INSTANCE = new ExportHandlerFormat();
	
	private ExportHandlerFormat() {
		super(Map.class);
	}

	@Override
	protected String getSpecificationNonNull(Map<String, ExportHandler> configValue) {
		return StringServices.toString(configValue.keySet(), ",");
	}

	@Override
	protected Map<String, ExportHandler> getValueEmpty(String propertyName) throws ConfigurationException {
		return defaultValue();
	}

	@Override
	protected Map<String, ExportHandler> getValueNonEmpty(String propertyName, CharSequence propertyValue)
			throws ConfigurationException {
		// convert a csl into a list of ExportHandlers
		// use a LinkedHashMap to retain the order as configured.
		Map<String, ExportHandler> theMap = new LinkedHashMap<>();
		ExportHandlerRegistry reg = ExportHandlerRegistry.getInstance();
		for (String theID : StringServices.toList(propertyValue, ',')) {
			theMap.put(theID, reg.getHandler(theID));
		}
		return theMap;
	}
	
	@Override
	public boolean isLegalValue(Object value) {
		return value != null;
	}
	
	@Override
	public Map<String, ExportHandler> defaultValue() {
		return new HashMap<>();
	}

	@Override
	public Object normalize(Object value) {
		if (value == null) {
			return new HashMap<String, ExportHandler>();
		}
		return super.normalize(value);
	}
}
