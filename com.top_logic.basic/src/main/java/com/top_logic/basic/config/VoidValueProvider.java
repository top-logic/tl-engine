/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.basic.config.annotation.MapBinding;

/**
 * Implementation of {@link ConfigurationValueBinding} and
 * {@link ConfigurationValueProvider} to be able to set a default class in
 * {@link ListBinding} and {@link MapBinding}.
 * 
 * @see ListBinding
 * @see MapBinding
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class VoidValueProvider implements ConfigurationValueBinding<Object>, ConfigurationValueProvider<Object> {

	private VoidValueProvider() {
		// no instance of it
	}

	@Override
	public boolean isLegalValue(Object value) {
		throw new UnsupportedOperationException("Not usable");
	}

	@Override
	public Object defaultValue() {
		throw new UnsupportedOperationException("Not usable");
	}

	@Override
	public Class<?> getValueType() {
		throw new UnsupportedOperationException("Not usable");
	}

	@Override
	public Object getValue(String propertyName, CharSequence propertyValue) throws ConfigurationException {
		throw new UnsupportedOperationException("Not usable");
	}

	@Override
	public String getSpecification(Object configValue) {
		throw new UnsupportedOperationException("Not usable");
	}

	@Override
	public Object loadConfigItem(XMLStreamReader in, Object baseValue) throws XMLStreamException, ConfigurationException {
		throw new UnsupportedOperationException("Not usable");
	}

	@Override
	public void saveConfigItem(XMLStreamWriter out, Object item) throws XMLStreamException {
		throw new UnsupportedOperationException("Not usable");
	}

	@Override
	public Object normalize(Object value) {
		throw new UnsupportedOperationException("Not usable");
	}

	@Override
	public Unimplementable unimplementable() {
		return null;
	}

}
