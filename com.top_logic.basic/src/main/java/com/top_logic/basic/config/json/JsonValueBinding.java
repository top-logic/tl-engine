/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.json;

import java.io.IOException;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationValueCheck;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.common.json.gstream.JsonReader;
import com.top_logic.common.json.gstream.JsonWriter;

/**
 * Algorithm to serialise the value of a configuration {@link PropertyDescriptor property} in Json
 * format.
 * 
 * @see JsonBinding
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface JsonValueBinding<T> extends ConfigurationValueCheck<T> {

	/**
	 * Reads a configuration item from the given reader.
	 * 
	 * @param property
	 *        The {@link PropertyDescriptor} currently reading.
	 * @param in
	 *        the reader to read the value from.
	 * @param baseValue
	 *        The inherited value from the base configuration. May be <code>null</code> when there
	 *        is no base value.
	 * 
	 * @return the parsed configuration value.
	 * 
	 * @throws IOException
	 *         If the configuration file is not well-formed or does not conform to the schema
	 *         established by this binding.
	 */
	T loadConfigItem(PropertyDescriptor property, JsonReader in, T baseValue) throws IOException, ConfigurationException;

	/**
	 * Marshals the given value to the given writer.
	 * 
	 * @param property
	 *        The {@link PropertyDescriptor} currently writing.
	 * @param out
	 *        The writer to write the given configuration value to.
	 * @param item
	 *        The configuration value to write.
	 */
	void saveConfigItem(PropertyDescriptor property, JsonWriter out, T item) throws IOException;

}

