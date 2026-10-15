/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.autoconf;

import java.io.Writer;
import java.util.Collection;
import java.util.Collections;

import javax.xml.stream.XMLStreamException;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationWriter;
import com.top_logic.basic.config.PropertyDescriptor;

/**
 * {@link ConfigurationWriter} adds an override attribute to all types of {@link ConfigurationItem}s
 * containing in a given set of types.
 *
 * <p>
 * Additionally, every written element holding an entry of one of the given replaced entry types is
 * marked with the override attribute, see
 * {@link #OverrideConfigurationWriter(Writer, Collection, Collection)}. Read onto a base
 * configuration, such an entry replaces the entry of the same key instead of being merged into it,
 * e.g. an entry of a keyed list, whose own lists would otherwise be appended to the ones of the
 * base entry.
 * </p>
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class OverrideConfigurationWriter extends ConfigurationWriter {

	/**
	 * Configuration override attribute name.
	 */
	private static final String CONFIG_OVERRIDE = "config:override";

	private Collection<Class<?>> _overriddenConfigInterfaces;

	private Collection<Class<?>> _replacedEntryInterfaces;

	/**
	 * Creates a {@link ConfigurationWriter} which adds override attributes to a set of
	 * {@link ConfigurationItem}s.
	 */
	public OverrideConfigurationWriter(Writer writer, Collection<Class<?>> configInterfaces) throws XMLStreamException {
		this(writer, configInterfaces, Collections.emptySet());
	}

	/**
	 * Creates a {@link ConfigurationWriter} which adds override attributes to a set of
	 * {@link ConfigurationItem}s and to the elements of the entries of the given types.
	 *
	 * @param writer
	 *        The writer to write to.
	 * @param configInterfaces
	 *        The configuration types to mark with the override attribute, see
	 *        {@link #OverrideConfigurationWriter(Writer, Collection)}.
	 * @param replacedEntryInterfaces
	 *        The configuration types whose entries replace the entry of the same key of the base
	 *        configuration. Every written element holding an entry of one of these types or of a
	 *        sub type is marked with the override attribute.
	 */
	public OverrideConfigurationWriter(Writer writer, Collection<Class<?>> configInterfaces,
			Collection<Class<?>> replacedEntryInterfaces) throws XMLStreamException {
		super(writer);

		_overriddenConfigInterfaces = configInterfaces;
		_replacedEntryInterfaces = replacedEntryInterfaces;
	}

	@Override
	protected void writeContent(String tagName, ConfigurationDescriptor staticType, ConfigurationItem item)
			throws XMLStreamException {
		if (isReplacedEntry(item)) {
			getXMLWriter().writeAttribute(CONFIG_OVERRIDE, "true");
		}

		super.writeContent(tagName, staticType, item);
	}

	private boolean isReplacedEntry(ConfigurationItem item) {
		Class<?> configInterface = item.descriptor().getConfigurationInterface();
		for (Class<?> replaced : _replacedEntryInterfaces) {
			if (replaced.isAssignableFrom(configInterface)) {
				return true;
			}
		}
		return false;
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
