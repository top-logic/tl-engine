/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.copy;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.config.ConfigBuilder;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.PropertyKind;
import com.top_logic.basic.config.TypedConfiguration;

/**
 * Deep-copies {@link ConfigurationItem}s.
 * <p>
 * {@link ConfigurationItem}s and {@link ConfiguredInstance}s are deep-copied, other values are not
 * copied.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class ConfigCopier {

	private enum BuildOptions {
		BUILD_ITEMS,
		RETURN_BUILDER;
	}

	private final InstantiationContext _instantiationContext;

	private final BuildOptions _buildOption;

	private final Map<ConfigurationItem, ConfigBuilder> _originalToCopy =
		new IdentityHashMap<>();

	private ConfigCopier(InstantiationContext instantiationContext, BuildOptions buildOption) {
		_instantiationContext = instantiationContext;
		_buildOption = buildOption;
	}

	/**
	 * Deep-copies the given {@link ConfigurationItem}.
	 * 
	 * @param original
	 *        Is not allowed to be null.
	 * @return Never null.
	 */
	public static <C extends ConfigurationItem> C copy(C original) {
		InstantiationContext instantiationContext = new DefaultInstantiationContext(ConfigCopier.class);
		C copy = copy(original, instantiationContext);
		check(instantiationContext, original);
		assert copy != null;
		return copy;
	}

	/**
	 * Deep-copies the given {@link ConfigurationItem}.
	 * <p>
	 * <b>Does not throw the errors logged to the {@link InstantiationContext}, but returns null in
	 * this case, instead of returning a broken {@link ConfigurationItem}.</b>
	 * </p>
	 * 
	 * @param original
	 *        Is not allowed to be null.
	 * @param instantiationContext
	 *        For instantiating {@link ConfigurationItem}s and <b>reporting errors</b>. <b>The
	 *        caller has to call {@link InstantiationContext#checkErrors()} to check for errors.</b>
	 *        This method only ensures that null is returned instead of a broken
	 *        {@link ConfigurationItem}, i.e. when the {@link InstantiationContext#hasErrors()}. Is
	 *        not allowed to be null.
	 * @return null, if and only if copying failed because the
	 *         {@link InstantiationContext#hasErrors()}.
	 */
	public static <C extends ConfigurationItem> C copy(C original, InstantiationContext instantiationContext) {
		ConfigCopier configCopier = new ConfigCopier(instantiationContext, BuildOptions.BUILD_ITEMS);
		// Cast is safe, as the original object is copied, and it is of type T.
		@SuppressWarnings("unchecked")
		C copy = (C) configCopier.copyItem(original);
		if (instantiationContext.hasErrors()) {
			return null;
		}
		return copy;
	}

	/**
	 * Creates a {@link ConfigBuilder} tree from the given {@link ConfigurationItem}.
	 * 
	 * @param original
	 *        Is not allowed to be null.
	 * @param instantiationContext
	 *        For instantiating {@link ConfigurationItem}s and <b>reporting errors</b>. <b>The
	 *        caller has to call {@link InstantiationContext#checkErrors()} to check for errors.</b>
	 *        This method only ensures that null is returned instead of a broken
	 *        {@link ConfigurationItem}, i.e. when the {@link InstantiationContext#hasErrors()}. Is
	 *        not allowed to be null.
	 * @return null, if copying failed because the {@link InstantiationContext#hasErrors()}.
	 */
	public static ConfigBuilder copyBuilder(ConfigurationItem original, InstantiationContext instantiationContext) {
		ConfigCopier configCopier = new ConfigCopier(instantiationContext, BuildOptions.RETURN_BUILDER);
		ConfigBuilder copy = (ConfigBuilder) configCopier.copyItem(original);
		if (instantiationContext.hasErrors()) {
			return null;
		}
		return copy;
	}

	/**
	 * Fills the values from the source to the sink by deep-copying them.
	 * <p>
	 * More precisely: Every property in the source where
	 * {@link ConfigurationItem#valueSet(PropertyDescriptor)} is true, is copied to the sink, if it
	 * has that property and {@link ConfigurationItem#valueSet(PropertyDescriptor)} is false there.
	 * </p>
	 * <p>
	 * <b>The caller has to call {@link InstantiationContext#checkErrors()} to check for errors.</b>
	 * </p>
	 * 
	 * @param source
	 *        Is not allowed to be null.
	 * @param sink
	 *        Is not allowed to be null.
	 * @param instantiationContext
	 *        For instantiating {@link ConfigurationItem}s and <b>reporting errors</b>. <b>The
	 *        caller has to call {@link InstantiationContext#checkErrors()} to check for errors.</b>
	 *        Is not allowed to be null.
	 */
	public static void fillDeepCopy(ConfigurationItem source, ConfigBuilder sink,
			InstantiationContext instantiationContext) {
		ConfigCopier configCopier = new ConfigCopier(instantiationContext, BuildOptions.RETURN_BUILDER);
		configCopier.fill(source, sink);
	}

	/**
	 * Copies all properties deep from the source configuration to destination configuration
	 * overriding potentially set values in destination configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to create instances in the copy of an attribute value.
	 * @param source
	 *        The source configuration.
	 * @param destination
	 *        The destination configuration.
	 */
	public static void copyContent(InstantiationContext context, ConfigurationItem source,
			ConfigurationItem destination) {
		copyContent(context, source, destination, false);
	}

	/**
	 * Copies all properties deep from the source configuration to destination configuration.
	 * 
	 * <p>
	 * {@link ConfigurationItem} in the source tree are copied deep before they are set to
	 * corresponding destination property.
	 * </p>
	 * 
	 * @param context
	 *        {@link InstantiationContext} to create instances in the copy of an attribute value.
	 * @param source
	 *        The source configuration.
	 * @param destination
	 *        The destination configuration.
	 * @param keepTargetValue
	 *        if <code>true</code>, then values that are
	 *        {@link ConfigurationItem#valueSet(PropertyDescriptor) explicitly set} in the
	 *        destination are not overridden.
	 */
	public static void copyContent(InstantiationContext context, ConfigurationItem source,
			ConfigurationItem destination, boolean keepTargetValue) {
		copyContent(context, source.descriptor().getProperties(), source, destination, keepTargetValue);
	}

	/**
	 * Copies the given properties properties deep from the source configuration to destination
	 * configuration.
	 * 
	 * <p>
	 * {@link ConfigurationItem} in the source tree are copied deep before they are set to
	 * corresponding destination property.
	 * </p>
	 * 
	 * @param context
	 *        {@link InstantiationContext} to create instances in the copy of an attribute value.
	 * @param properties
	 *        The properties to copy. The properties must be properties of both items, the source
	 *        item and the destination item.
	 * @param source
	 *        The source configuration.
	 * @param destination
	 *        The destination configuration.
	 * @param keepTargetValue
	 *        if <code>true</code>, then values that are
	 *        {@link ConfigurationItem#valueSet(PropertyDescriptor) explicitly set} in the
	 *        destination are not overridden.
	 */
	public static void copyContent(InstantiationContext context, Collection<? extends PropertyDescriptor> properties,
			ConfigurationItem source, ConfigurationItem destination, boolean keepTargetValue) {
		ConfigCopier configCopier = new ConfigCopier(context, BuildOptions.BUILD_ITEMS);

		for (PropertyDescriptor sourceProp : properties) {
			configCopier.fill(source, sourceProp, destination, keepTargetValue, true);
		}
	}

	private ConfigurationItem copyItem(ConfigurationItem original) {
		ConfigBuilder resultBuilder = createBuilder(original);
		if (_buildOption == BuildOptions.RETURN_BUILDER) {
			return resultBuilder;
		}
		return build(resultBuilder);
	}

	private ConfigBuilder createBuilder(ConfigurationItem original) {
		ConfigBuilder copy = getCopy(original);
		if (copy == null) {
			copy = createBuilderInternal(original);
			addCopy(original, copy);
		}
		return copy;
	}

	private ConfigBuilder createBuilderInternal(ConfigurationItem original) {
		ConfigBuilder resultBuilder = TypedConfiguration.createConfigBuilder(original.descriptor());
		/* Don't check the mandatory properties: Whatever state the original ConfigItem is in,
		 * "copy" just creates another one in exactly that state. */
		resultBuilder.disableChecks();
		fill(original, resultBuilder);
		resultBuilder.initLocation(original.location());
		return resultBuilder;
	}

	private void fill(ConfigurationItem sourceItem, ConfigBuilder sinkItem) {
		for (PropertyDescriptor sourceProperty : sourceItem.descriptor().getProperties()) {
			PropertyDescriptor sinkProperty = fill(sourceItem, sourceProperty, sinkItem, true, false);
			if (sinkProperty == null) {
				continue;
			}

			if (!isAtomic(sourceProperty.kind())) {
				transferValueSet(sourceProperty, sourceItem, sinkProperty, sinkItem);
			}
		}
	}

	private PropertyDescriptor fill(ConfigurationItem sourceItem, PropertyDescriptor sourceProperty,
			ConfigurationItem destinationItem, boolean keepTargetValue, boolean buildItem) {
		if (!sourceProperty.canHaveSetter()) {
			return null;
		}
		if ((sourceProperty.isMandatory() || isAtomic(sourceProperty.kind()))
			&& !sourceItem.valueSet(sourceProperty)) {
			return null;
		}
		ConfigurationDescriptor sinkDescriptor = destinationItem.descriptor();
		String propertyName = sourceProperty.getPropertyName();
		if (!sinkDescriptor.hasProperty(propertyName)) {
			// Property does not exist in the target descriptor
			return null;
		}
		PropertyDescriptor sinkProperty = sinkDescriptor.getProperty(propertyName);
		if (keepTargetValue && destinationItem.valueSet(sinkProperty)) {
			// Don't override values that have already been set, just fill in "missing" values.
			return null;
		}
		copy(sourceProperty, sourceItem, sinkProperty, destinationItem, buildItem);
		return sinkProperty;
	}

	private boolean isAtomic(PropertyKind kind) throws UnreachableAssertion {
		switch (kind) {
			case PLAIN:
			case COMPLEX:
			case DERIVED:
			case REF:
				return true;
			case ITEM:
			case ARRAY:
			case LIST:
			case MAP:
				return false;
		}
		throw new UnreachableAssertion("No such kind: " + kind);
	}

	private <C extends ConfigurationItem> void copy(
			PropertyDescriptor originalProperty, ConfigurationItem originalItem,
			PropertyDescriptor copyProperty, ConfigurationItem copyItem, boolean buildItem) {
		Object copiedValue;
		switch (originalProperty.kind()) {
			case PLAIN:
			case COMPLEX: {
				copiedValue = originalItem.value(originalProperty);
				break;
			}
			case ITEM: {
				copiedValue = copyObject(originalItem.value(originalProperty), buildItem);
				break;
			}
			case ARRAY: {
				copiedValue = copyArray(originalItem.value(originalProperty), buildItem);
				break;
			}
			case LIST: {
				copiedValue = copyList((List<?>) originalItem.value(originalProperty), buildItem);
				break;
			}
			case MAP: {
				copiedValue = copyMap((Map<?, ?>) originalItem.value(originalProperty), buildItem);
				break;
			}
			case REF: {
				copiedValue = copyObject(originalItem.value(originalProperty), buildItem);
				break;
			}
			case DERIVED: {
				// Derived properties are not copied, they will be calculated automatically.
				return;
			}
			default: {
				String message = "Unexpected property kind: " + originalProperty.kind();
				throw new UnreachableAssertion(message);
			}
		}
		copyItem.update(copyProperty, copiedValue);

	}

	private void transferValueSet(PropertyDescriptor originalProperty, ConfigurationItem originalItem,
			PropertyDescriptor copyProperty, ConfigBuilder copyItem) {
		if (!originalItem.valueSet(originalProperty)) {
			copyItem.resetValueSet(copyProperty);
		}
	}

	private Object copyArray(Object original, boolean buildItem) {
		Class<?> componentType = original.getClass().getComponentType();
		// Can copy inline, since a copy is created in getter and setter.
		for (int n = 0, length = Array.getLength(original); n < length; n++) {
			Object copiedValue = copyObject(Array.get(original, n), buildItem);
			if (!buildItem && copiedValue instanceof ConfigBuilder) {
				/* Copy may not be correct for the array type, e.g. the Array type is a special
				 * configuration interface. In this case the builder must be built. */
				ConfigBuilder builder = (ConfigBuilder) copiedValue;
				if (!componentType.isInstance(builder)) {
					copiedValue = build(builder);
				}
			}
			Array.set(original, n, copiedValue);
		}
		return original;
	}

	private List<Object> copyList(List<?> original, boolean buildItem) {
		List<Object> copy = new ArrayList<>(original.size());
		for (Object entry : original) {
			copy.add(copyObject(entry, buildItem));
		}
		return copy;
	}

	private Map<Object, Object> copyMap(Map<?, ?> original, boolean buildItem) {
		Map<Object, Object> copiedMap = new LinkedHashMap<>(original.size());
		for (Map.Entry<?, ?> originalEntry : original.entrySet()) {
			Object copiedKey = copyObject(originalEntry.getKey(), buildItem);
			Object copiedObject = copyObject(originalEntry.getValue(), buildItem);
			copiedMap.put(copiedKey, copiedObject);
		}
		return copiedMap;
	}

	/**
	 * Only {@link ConfigurationItem}s and {@link ConfiguredInstance}s are copied, everything else
	 * is returned directly.
	 */
	private Object copyObject(Object original, boolean buildItem) {
		if (original instanceof ConfigurationItem) {
			ConfigBuilder builder = createBuilder((ConfigurationItem) original);
			if (buildItem) {
				return build(builder);
			} else {
				return builder;
			}
		} else if (original instanceof ConfiguredInstance) {
			ConfiguredInstance<?> configuredInstance = (ConfiguredInstance<?>) original;
			ConfigBuilder builder = createBuilder(configuredInstance.getConfig());
			if (buildItem) {
				return build(builder);
			} else {
				return builder;
			}
		} else {
			return original;
		}
	}

	private ConfigurationItem build(ConfigBuilder builder) {
		return builder.createConfig(_instantiationContext);
	}

	private void addCopy(ConfigurationItem original, ConfigBuilder copy) {
		_originalToCopy.put(original, copy);
	}

	private ConfigBuilder getCopy(ConfigurationItem original) {
		return _originalToCopy.get(original);
	}

	private static void check(InstantiationContext protocol, ConfigurationItem original) {
		if (protocol.hasErrors()) {
			try {
				protocol.checkErrors();
			} catch (ConfigurationException ex) {
				throw new ConfigurationError("Copying failed. Cause: " + ex.getMessage() + " ConfigItem to be copied: "
					+ original, ex);
			}
		}
	}

}
