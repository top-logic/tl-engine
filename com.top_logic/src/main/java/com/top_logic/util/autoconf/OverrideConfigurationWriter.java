/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.autoconf;

import java.io.Writer;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.top_logic.basic.NamedConstant;
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
 * <p>
 * Finally, removal entries can be written for keyed lists, see
 * {@link #OverrideConfigurationWriter(Writer, Collection, Collection, Map)}: read onto a base
 * configuration, such an entry removes the entry of the same key of the base configuration.
 * </p>
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class OverrideConfigurationWriter extends ConfigurationWriter {

	/**
	 * Configuration override attribute name.
	 */
	private static final String CONFIG_OVERRIDE = "config:override";

	/**
	 * Name of the attribute stating the operation of a list entry.
	 */
	private static final String CONFIG_OPERATION = "config:operation";

	/**
	 * Value of {@link #CONFIG_OPERATION} for an entry removing the entry of the same key.
	 */
	private static final String OPERATION_REMOVE = "remove";

	private Collection<Class<?>> _overriddenConfigInterfaces;

	private Collection<Class<?>> _replacedEntryInterfaces;

	private Map<NamedConstant, Collection<?>> _removedEntries;

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
		this(writer, configInterfaces, replacedEntryInterfaces, Collections.emptyMap());
	}

	/**
	 * Creates a {@link ConfigurationWriter} which adds override attributes to a set of
	 * {@link ConfigurationItem}s and to the elements of the entries of the given types, and writes
	 * removal entries into the given lists.
	 *
	 * @param writer
	 *        The writer to write to.
	 * @param configInterfaces
	 *        The configuration types to mark with the override attribute, see
	 *        {@link #OverrideConfigurationWriter(Writer, Collection)}.
	 * @param replacedEntryInterfaces
	 *        The configuration types whose entries replace the entry of the same key of the base
	 *        configuration, see {@link #OverrideConfigurationWriter(Writer, Collection, Collection)}.
	 * @param removedEntries
	 *        The keys of the entries to remove from the base configuration, by the keyed list
	 *        property they are removed from. Each list must be a
	 *        {@link PropertyDescriptor#isDefaultContainer() default container}. The removal entries
	 *        are written after the entries of the list, so the list must be written: its value
	 *        must be set, even if it is empty.
	 */
	public OverrideConfigurationWriter(Writer writer, Collection<Class<?>> configInterfaces,
			Collection<Class<?>> replacedEntryInterfaces,
			Map<PropertyDescriptor, ? extends Collection<?>> removedEntries) throws XMLStreamException {
		super(writer);

		_overriddenConfigInterfaces = configInterfaces;
		_replacedEntryInterfaces = replacedEntryInterfaces;
		_removedEntries = new HashMap<>();
		for (Map.Entry<PropertyDescriptor, ? extends Collection<?>> entry : removedEntries.entrySet()) {
			PropertyDescriptor list = entry.getKey();
			if (!list.isDefaultContainer() || list.getKeyProperty() == null) {
				throw new IllegalArgumentException(
					"Removal entries can only be written into a keyed list that is a default container: " + list);
			}
			_removedEntries.put(list.identifier(), entry.getValue());
		}
	}

	@Override
	protected void writeList(ConfigurationItem item, PropertyDescriptor property) throws XMLStreamException {
		super.writeList(item, property);

		Collection<?> removedKeys = _removedEntries.get(property.identifier());
		if (removedKeys == null) {
			return;
		}
		// The list is a default container, see the constructor: its entries are written directly
		// into the element of the item, which is still open.
		String entryTag = property.getElementName(property.getDefaultDescriptor());
		String keyAttribute = property.getKeyProperty().getPropertyName();
		XMLStreamWriter out = getXMLWriter();
		for (Object key : removedKeys) {
			out.writeEmptyElement(entryTag);
			out.writeAttribute(keyAttribute, String.valueOf(key));
			out.writeAttribute(CONFIG_OPERATION, OPERATION_REMOVE);
		}
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
