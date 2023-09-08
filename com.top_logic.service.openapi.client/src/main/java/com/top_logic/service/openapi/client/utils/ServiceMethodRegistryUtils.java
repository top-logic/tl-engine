/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.client.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.XmlDateTimeFormat;
import com.top_logic.basic.json.JSON;
import com.top_logic.model.search.expr.ToString;
import com.top_logic.service.openapi.client.registry.ServiceMethodRegistry;

/**
 * TUtilities for {@link ServiceMethodRegistry}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ServiceMethodRegistryUtils {

	/**
	 * Determines the configuration of the {@link ServiceMethodRegistry}.
	 * 
	 * <p>
	 * This method can also be called when the {@link ServiceMethodRegistry} is not currently
	 * started.
	 * </p>
	 */
	public static ServiceMethodRegistry.Config<?> serviceMethodRegistryConfig() {
		if (ServiceMethodRegistry.Module.INSTANCE.isActive()) {
			return ServiceMethodRegistry.getInstance().getConfig();
		} else {
			try {
				return (ServiceMethodRegistry.Config<?>) ApplicationConfig.getInstance()
					.getServiceConfiguration(ServiceMethodRegistry.class);
			} catch (ConfigurationException ex) {
				throw new ConfigurationError(ex);
			}
		}
	}

	/**
	 * Serializes the given value as {@link String} argument to call the external API.
	 * 
	 * @param value
	 *        The value to deliver to external API.
	 * 
	 * @return String representation of the value to use as parameter argument.
	 */
	public static String serializeArgument(Object value) {
		String stringValue;
		if (value instanceof Date) {
			// Guess Open API type "date" or "date-time"
			stringValue = XmlDateTimeFormat.INSTANCE.format(value);
		} else if (value instanceof Calendar) {
			// Guess Open API type "date" or "date-time"
			stringValue = XmlDateTimeFormat.INSTANCE.format(((Calendar) value).getTime());
		} else if (value instanceof Map) {
			// Guess Open API type "object"
			stringValue = JSON.toString(value);
		} else if (value instanceof Number) {
			stringValue = ((Number) value).toString();
		} else if (value instanceof Boolean) {
			stringValue = ((Boolean) value).toString();
		} else {
			stringValue = ToString.toString(value);
		}
		return stringValue;
	}

}

