/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.top_logic.basic.config.annotation.Binding;

/**
 * Binding for configuration values that is not described by a
 * {@link ConfigurationDescriptor}.
 * 
 * @see Binding Annotating a {@link ConfigurationValueBinding}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ConfigurationValueBinding<T> extends ConfigurationValueCheck<T> {

	// TODO: Provide schema information about the accepted contents?

	/**
	 * Reads a configuration item from the given reader.
	 * 
	 * <p>
	 * When this method is called, the given reader is positioned at the start tag of the
	 * configuration element that describes the configuration value to load.
	 * </p>
	 * 
	 * <p>
	 * If the method returns normally, the given reader must be positioned at the corresponding end
	 * tag of the element, on whose start tag the reader was positioned when this method was called.
	 * </p>
	 * 
	 * @param in
	 *        the reader to read the value from.
	 * @param baseValue
	 *        The inherited value from the base configuration.
	 * @return the parsed configuration value.
	 * 
	 * @throws XMLStreamException
	 *         If the configuration file is not well-formed or does not conform to the schema
	 *         established by this binding.
	 * @throws ConfigurationException
	 *         TODO Is that a good signature? The problem is that parsing must stop after such an
	 *         exception, because no guarantee can be given where the reader is positioned, if the
	 *         method returns abnormally.
	 */
	T loadConfigItem(XMLStreamReader in, T baseValue) throws XMLStreamException, ConfigurationException;

	/**
	 * Marshals the given value to the given writer.
	 * 
	 * <p>
	 * When this method is called, the start tag representing the value has already been written to
	 * the given writer. The method implementation may write attributes to that start tag and/or
	 * additional element contents. After this method returns normally, the writer must be in a
	 * position, where the corresponding closing tag can be written.
	 * </p>
	 * 
	 * @param out
	 *        The writer to write the given configuration value to.
	 * @param item
	 *        The configuration value to write.
	 * @throws XMLStreamException
	 *         If writing to the given writer fails.
	 */
	void saveConfigItem(XMLStreamWriter out, T item) throws XMLStreamException;

}
