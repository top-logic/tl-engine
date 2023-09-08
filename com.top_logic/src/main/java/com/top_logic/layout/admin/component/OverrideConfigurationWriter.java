/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.admin.component;

import java.io.Writer;
import java.util.Collection;

import javax.xml.stream.XMLStreamException;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationWriter;
import com.top_logic.basic.config.PropertyDescriptor;

/**
 * {@link ConfigurationWriter} adds an override attribute to all types of {@link ConfigurationItem}s
 * containing in a given set of types.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class OverrideConfigurationWriter extends ConfigurationWriter {

	/**
	 * Configuration override attribute name.
	 */
	private static final String CONFIG_OVERRIDE = "config:override";

	private Collection<Class<?>> _overriddenConfigInterfaces;

	/**
	 * Creates a {@link ConfigurationWriter} which adds override attributes to a set of
	 * {@link ConfigurationItem}s.
	 */
	public OverrideConfigurationWriter(Writer writer, Collection<Class<?>> configInterfaces) throws XMLStreamException {
		super(writer);

		_overriddenConfigInterfaces = configInterfaces;
	}

	@Override
	protected void writeItem(ConfigurationItem item, PropertyDescriptor property) throws XMLStreamException {
		if (_overriddenConfigInterfaces.contains(getConfigInterface(property))) {
			getXMLWriter().writeAttribute(CONFIG_OVERRIDE, "true");
		}

		super.writeItem(item, property);
	}

	private Class<?> getConfigInterface(PropertyDescriptor property) {
		return property.getDescriptor().getConfigurationInterface();
	}

}
