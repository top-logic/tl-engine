/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.misc;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import com.top_logic.basic.col.InlineMap;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;

/**
 * {@link DescendingConfigurationItemVisitor} that (recursively) replaces a
 * {@link ConfigurationItem} using a given replacement function.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ReplaceConfigurationVisitor extends DescendingConfigurationItemVisitor {

	private final Function<? super ConfigurationItem, ? extends ConfigurationItem> _replacement;

	/**
	 * Creates a new {@link ReplaceConfigurationVisitor}.
	 * 
	 * @param replacement
	 *        Replacement definition of the {@link ConfigurationItem}s in the visited configuration
	 *        tree. The replacement must not return <code>null</code>, input is also not
	 *        <code>null</code>. It must not modify the given configurations. If no change must
	 *        happen, the given argument configuration must be returned.
	 */
	public ReplaceConfigurationVisitor(Function<? super ConfigurationItem, ? extends ConfigurationItem> replacement) {
		_replacement = replacement;
	}

	/**
	 * Replaces {@link ConfigurationItem} values in the given {@link ConfigurationItem} according to
	 * the given replacement function.
	 * 
	 * <p>
	 * For the given item and all (recursively) referenced items the replacement function is called.
	 * When the replacement function returns for item "A" a different item "B", then "A" is replaced
	 * by "B".
	 * </p>
	 * 
	 * @param config
	 *        The configuration which must (recursively) be replaced.
	 */
	public ConfigurationItem replace(ConfigurationItem config) {
		if (config == null) {
			return config;
		}
		ConfigurationItem result = _replacement.apply(config);
		handleProperties(result);
		return result;
	}

	@Override
	protected void handleListProperty(ConfigurationItem config, PropertyDescriptor property, List<? extends ConfigurationItem> listValue) {
		PropertyDescriptor keyProperty = property.getKeyProperty();
		if (keyProperty == null) {
			ListIterator<ConfigurationItem> it = (ListIterator<ConfigurationItem>) listValue.listIterator();
			while (it.hasNext()) {
				ConfigurationItem value = it.next();
				ConfigurationItem inlined = replace(value);
				if (value != inlined) {
					if (inlined == null) {
						it.remove();
					} else {
						it.set(inlined);
					}
				}
			}
		} else {
			/* The key of this property may be changed. It is not good to change the key of an
			 * indexed property. Better create a copy of the list and store it to have a clear
			 * "key state" */
			List<ConfigurationItem> copy = modifyToNewList(listValue);
			if (copy != null) {
				config.update(property, copy);
			}

		}
	}

	private List<ConfigurationItem> modifyToNewList(List<? extends ConfigurationItem> value) {
		List<ConfigurationItem> modifiedList = null;
		for (int i = 0; i < value.size(); i++) {
			ConfigurationItem entry = value.get(i);
			ConfigurationItem replacedEntry = replace(entry);
			if (replacedEntry != entry && modifiedList == null) {
				modifiedList = new ArrayList<>(value.size());
				modifiedList.addAll(value.subList(0, i));
			}
			if (modifiedList != null) {
				if (replacedEntry != null) {
					modifiedList.add(replacedEntry);
				}
			}
		}
		return modifiedList;
	}

	@Override
	protected void handleArrayProperty(ConfigurationItem config, PropertyDescriptor property, Object[] arrayValue) {
		/* It is no problem to modify the array inline, because always a new array instance is
		 * returned. Otherwise the immutability could not be ensured. */
		boolean modified = false;

		int srcLength = arrayValue.length;
		int destLength = 0;
		for (int i = 0, size = srcLength; i < size; i++) {
			ConfigurationItem value = (ConfigurationItem) arrayValue[i];
			ConfigurationItem inlined = replace(value);
			if (value != inlined) {
				if (inlined != null) {
					arrayValue[destLength++] = inlined;
				}
				modified = true;
			} else {
				destLength++;
			}
		}
		if (modified) {
			if (destLength < srcLength) {
				Object shortened = Array.newInstance(arrayValue.getClass().getComponentType(), destLength);
				System.arraycopy(arrayValue, 0, shortened, 0, destLength);

				config.update(property, shortened);
			} else {
				config.update(property, arrayValue);
			}
		}
	}

	@Override
	protected void handleItemProperty(ConfigurationItem config, PropertyDescriptor property, ConfigurationItem itemValue) {
		ConfigurationItem inlined = replace(itemValue);
		if (itemValue != inlined) {
			config.update(property, inlined);
		}
	}

	@Override
	protected void handleMapProperty(ConfigurationItem config, PropertyDescriptor property,
			Map<Object, ConfigurationItem> mapValue) {
		/* Note: Changing the value of an entry is not reported to potential listeners. Therefore
		 * values must be set via "put". This must be done outside the loop to prevent a
		 * ConcurrentModificationException. */
		InlineMap<Object, ConfigurationItem> replacements = InlineMap.empty();
		List<Object> failedKeys = null;
		for (Entry<?, ? extends ConfigurationItem> entry : mapValue.entrySet()) {
			ConfigurationItem value = entry.getValue();
			ConfigurationItem inlined = replace(value);
			if (value != inlined) {
				if (inlined != null) {
					replacements = replacements.putValue(entry.getKey(), inlined);
				} else {
					if (failedKeys == null) {
						failedKeys = new ArrayList<>();
					}
					failedKeys.add(entry.getKey());
				}
			}
		}
		mapValue.putAll(replacements.toMap());
		if (failedKeys != null) {
			mapValue.keySet().removeAll(failedKeys);
		}
	}

	@Override
	protected void handlePlainProperty(ConfigurationItem config, PropertyDescriptor property) {
		// Value is not an Item or anything that refers to an item. Ignore.
	}

}
