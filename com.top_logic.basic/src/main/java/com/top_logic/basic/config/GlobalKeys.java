/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.Log;
import com.top_logic.basic.config.annotation.GlobalKey;

/**
 * The entries of lists keyed by a {@link GlobalKey} property in a configuration tree.
 * 
 * <p>
 * The {@link ConfigurationReader} resolves a {@link ConfigurationSchemaConstants#CONFIG_REF
 * reference} among these entries and checks that their keys are unique in the whole tree.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
final class GlobalKeys {

	/**
	 * Callback for every entry of a list keyed by a {@link GlobalKey} property.
	 */
	interface EntryVisitor {

		/**
		 * Visits an entry with a key value.
		 * 
		 * @param owner
		 *        The item whose list property holds the entry.
		 * @param listProperty
		 *        The list property of the owner holding the entry.
		 * @param keyProperty
		 *        The key property of the list, as declared by the element type of the list.
		 * @param entry
		 *        The entry.
		 * @param key
		 *        The key value of the entry, never anonymous.
		 */
		void visit(ConfigurationItem owner, PropertyDescriptor listProperty, PropertyDescriptor keyProperty,
				ConfigurationItem entry, Object key);

	}

	/**
	 * The current contents of a list property that is being read, taking the place of the value
	 * stored in the owner.
	 */
	interface ListOverride {

		/**
		 * The entries to visit for the given list property of the given owner, <code>null</code>
		 * to visit the value stored in the owner.
		 */
		List<?> entriesOf(ConfigurationItem owner, PropertyDescriptor listProperty);

	}

	private GlobalKeys() {
		// Static utility.
	}

	/**
	 * Whether the given key property is a {@link GlobalKey}.
	 */
	static boolean isGlobalKey(PropertyDescriptor keyProperty) {
		return keyProperty.getAnnotation(GlobalKey.class) != null;
	}

	/**
	 * Visits every entry with a key value of every list keyed by a {@link GlobalKey} property in
	 * the tree below the given item, in document order.
	 * 
	 * @param root
	 *        The item whose contents are searched; the item itself is visited only as an owner.
	 * @param override
	 *        The current contents of lists being read, see {@link ListOverride}.
	 * @param visitor
	 *        The callback for every entry.
	 */
	static void visitEntries(ConfigurationItem root, ListOverride override, EntryVisitor visitor) {
		for (PropertyDescriptor property : root.descriptor().getProperties()) {
			switch (property.kind()) {
				case ITEM: {
					Object value = root.value(property);
					if (value instanceof ConfigurationItem) {
						visitEntries((ConfigurationItem) value, override, visitor);
					}
					break;
				}
				case LIST:
				case ARRAY: {
					List<?> entries = override.entriesOf(root, property);
					if (entries == null) {
						Object value = root.value(property);
						entries = property.kind() == PropertyKind.ARRAY
							? PropertyDescriptorImpl.arrayAsList(value)
							: (List<?>) value;
					}
					if (entries == null) {
						break;
					}
					PropertyDescriptor keyProperty = property.getKeyProperty();
					boolean global = keyProperty != null && isGlobalKey(keyProperty);
					for (Object entry : entries) {
						if (!(entry instanceof ConfigurationItem)) {
							continue;
						}
						ConfigurationItem entryItem = (ConfigurationItem) entry;
						if (global) {
							Object key = keyOf(entryItem, keyProperty);
							if (!ConfigUtil.isAnonymousKey(key)) {
								visitor.visit(root, property, keyProperty, entryItem, key);
							}
						}
						visitEntries(entryItem, override, visitor);
					}
					break;
				}
				case MAP: {
					Map<?, ?> map = (Map<?, ?>) root.value(property);
					if (map == null) {
						break;
					}
					for (Object value : map.values()) {
						if (value instanceof ConfigurationItem) {
							visitEntries((ConfigurationItem) value, override, visitor);
						}
					}
					break;
				}
				default: {
					break;
				}
			}
		}
	}

	/**
	 * The key value of the given entry for the given key property of the list's element type.
	 */
	static Object keyOf(ConfigurationItem entry, PropertyDescriptor keyProperty) {
		PropertyDescriptor entryKeyProperty = entry.descriptor().getProperty(keyProperty.getPropertyName());
		if (entryKeyProperty == null) {
			return null;
		}
		return entry.value(entryKeyProperty);
	}

	/**
	 * Reports every key value that two entries of lists keyed by a {@link GlobalKey} property in
	 * the tree below the given item share.
	 */
	static void checkUnique(Log log, ConfigurationItem root) {
		Map<Object, ConfigurationItem> seen = new HashMap<>();
		visitEntries(root, (owner, property) -> null, (owner, listProperty, keyProperty, entry, key) -> {
			ConfigurationItem first = seen.putIfAbsent(key, entry);
			if (first != null) {
				log.error("Duplicate value '" + key + "' of the key property '" + keyProperty.getPropertyName()
					+ "', which is unique in the whole configuration: entry " + first.location() + " and entry "
					+ entry.location() + ".");
			}
		});
	}

}
