/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.top_logic.basic.Log;
import com.top_logic.basic.config.annotation.GlobalKey;

/**
 * The items of a configuration tree that carry a {@link GlobalKey} property.
 * 
 * <p>
 * The {@link ConfigurationReader} resolves a {@link ConfigurationSchemaConstants#CONFIG_REF
 * reference} among these items and checks that their keys are unique in the whole tree.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
final class GlobalKeys {

	/**
	 * Callback for every item with a {@link GlobalKey} property that has a key value.
	 */
	interface ItemVisitor {

		/**
		 * Visits an item with a key value.
		 * 
		 * @param owner
		 *        The item whose property holds the visited item.
		 * @param property
		 *        The property of the owner holding the item: of kind {@link PropertyKind#ITEM},
		 *        {@link PropertyKind#LIST}, {@link PropertyKind#ARRAY} or {@link PropertyKind#MAP}.
		 * @param item
		 *        The item.
		 * @param keyProperty
		 *        The {@link GlobalKey} property of the item, as declared by the item's type.
		 * @param key
		 *        The key value of the item, never anonymous.
		 */
		void visit(ConfigurationItem owner, PropertyDescriptor property, ConfigurationItem item,
				PropertyDescriptor keyProperty, Object key);

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

	private static final PropertyDescriptor[] NO_PROPERTIES = {};

	private static final ConcurrentHashMap<ConfigurationDescriptor, PropertyDescriptor[]> GLOBAL_KEY_PROPERTIES =
		new ConcurrentHashMap<>();

	private GlobalKeys() {
		// Static utility.
	}

	/**
	 * Whether the given property is a {@link GlobalKey}.
	 */
	static boolean isGlobalKey(PropertyDescriptor property) {
		return property.getAnnotation(GlobalKey.class) != null;
	}

	/**
	 * The {@link GlobalKey} properties of items of the given type.
	 */
	static PropertyDescriptor[] globalKeyProperties(ConfigurationDescriptor descriptor) {
		return GLOBAL_KEY_PROPERTIES.computeIfAbsent(descriptor, type -> {
			List<PropertyDescriptor> result = new ArrayList<>();
			for (PropertyDescriptor property : type.getProperties()) {
				if (isGlobalKey(property)) {
					result.add(property);
				}
			}
			return result.isEmpty() ? NO_PROPERTIES : result.toArray(new PropertyDescriptor[0]);
		});
	}

	/**
	 * Whether items of the given type carry a {@link GlobalKey} property.
	 */
	static boolean hasGlobalKeys(ConfigurationDescriptor descriptor) {
		return globalKeyProperties(descriptor).length > 0;
	}

	/**
	 * Visits every item with a key value of a {@link GlobalKey} property in the tree below the
	 * given item, in document order.
	 * 
	 * @param root
	 *        The item whose contents are searched; the item itself is visited only as an owner.
	 * @param override
	 *        The current contents of lists being read, see {@link ListOverride}.
	 * @param visitor
	 *        The callback for every item.
	 */
	static void visitItems(ConfigurationItem root, ListOverride override, ItemVisitor visitor) {
		for (PropertyDescriptor property : root.descriptor().getProperties()) {
			switch (property.kind()) {
				case ITEM: {
					visitValue(root, property, root.value(property), override, visitor);
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
					for (Object entry : entries) {
						visitValue(root, property, entry, override, visitor);
					}
					break;
				}
				case MAP: {
					Map<?, ?> map = (Map<?, ?>) root.value(property);
					if (map == null) {
						break;
					}
					for (Object value : map.values()) {
						visitValue(root, property, value, override, visitor);
					}
					break;
				}
				default: {
					break;
				}
			}
		}
	}

	private static void visitValue(ConfigurationItem owner, PropertyDescriptor property, Object value,
			ListOverride override, ItemVisitor visitor) {
		if (!(value instanceof ConfigurationItem)) {
			return;
		}
		ConfigurationItem item = (ConfigurationItem) value;
		for (PropertyDescriptor keyProperty : globalKeyProperties(item.descriptor())) {
			Object key = item.value(keyProperty);
			if (!ConfigUtil.isAnonymousKey(key)) {
				visitor.visit(owner, property, item, keyProperty, key);
			}
		}
		visitItems(item, override, visitor);
	}

	/**
	 * The key value of the given item for the given key property of a list's element type.
	 */
	static Object keyOf(ConfigurationItem item, PropertyDescriptor keyProperty) {
		PropertyDescriptor itemKeyProperty = item.descriptor().getProperty(keyProperty.getPropertyName());
		if (itemKeyProperty == null) {
			return null;
		}
		return item.value(itemKeyProperty);
	}

	/**
	 * Reports every key value that two items with a {@link GlobalKey} property in the tree below
	 * the given item share.
	 */
	static void checkUnique(Log log, ConfigurationItem root) {
		Map<Object, ConfigurationItem> seen = new HashMap<>();
		visitItems(root, (owner, property) -> null, (owner, property, item, keyProperty, key) -> {
			ConfigurationItem first = seen.putIfAbsent(key, item);
			if (first != null) {
				log.error("Duplicate value '" + key + "' of the key property '" + keyProperty.getPropertyName()
					+ "', which is unique in the whole configuration: " + first.location() + " and "
					+ item.location() + ".");
			}
		});
	}

}
