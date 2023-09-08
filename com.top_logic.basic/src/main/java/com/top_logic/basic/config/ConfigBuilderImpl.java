/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;

import com.top_logic.basic.Log;
import com.top_logic.basic.NamedConstant;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.tools.NameBuilder;

/**
 * Internal {@link ConfigBuilder} implementation.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@FrameworkInternal
class ConfigBuilderImpl implements ConfigBuilder {

	private final ConfigurationDescriptor configurationDescriptor;
	private final HashMap<NamedConstant, Object> values;
	private Location _location = LocationImpl.NONE;

	private ConfigurationItem _result = null;

	/** @see #disableChecks() */
	private boolean _checksEnabled = true;

	/**
	 * @see TypedConfiguration#createConfigBuilder(ConfigurationDescriptor)
	 *      Instantiating a {@link ConfigBuilder}.
	 */
	/*package protected*/ ConfigBuilderImpl(ConfigurationDescriptor configurationDescriptor) {
		this.configurationDescriptor = configurationDescriptor;
		this.values = new HashMap<>();
	}

	/**
	 * Internal access to the internal data structure for copying values into the final item.
	 */
	Map<NamedConstant, Object> values() {
		return values;
	}

	@Override
	public ConfigurationDescriptor descriptor() {
		return this.configurationDescriptor;
	}

	@Override
	public void disableChecks() {
		_checksEnabled = false;
	}

	/** @see #disableChecks() */
	private boolean areChecksEnabled() {
		return _checksEnabled;
	}

	@Override
	public boolean valueSet(PropertyDescriptor property) {
		return values.containsKey(property.getValueSetIdentifier());
	}
	
	@Override
	public Object value(PropertyDescriptor property) {
		property = MutableConfigItem.checkLegalProperty(descriptor(), property);
		if (isConfigInterfaceProperty(property)) {
			return configurationDescriptor.getConfigurationInterface();
		}
		NamedConstant identifier = property.identifier();
		if (!values.containsKey(identifier)) {
			if (property.isMandatory()) {
				// Allow copying values from a builder through standard interfaces.
				return property.getDefaultValue();
			}
			DerivedPropertyAlgorithm algorithm = property.getAlgorithm();
			if (algorithm != null) {
				return algorithm.apply(this);
			}
			// Lazily create default value.
			Object defaultValue = property.getDefaultValue();
			values.put(identifier, defaultValue);
			return defaultValue;
		}
		return values.get(identifier);
	}
	
	@Override
	public Object update(PropertyDescriptor property, Object value) {
		checkNoResult();
		checkNotConfigInterfaceProperty(property);
		if (property.getDescriptor() != configurationDescriptor) {
			throw new IllegalArgumentException("Property of wrong descriptor.");
		}
		NamedConstant valueSetId = property.getValueSetIdentifier();
		values.put(valueSetId, Boolean.TRUE);
		return values.put(property.identifier(), value);
	}
	
	@Override
	public void initValue(PropertyDescriptor property, Object value) {
		checkNoResult();
		checkNotConfigInterfaceProperty(property);
		if (values.containsKey(property.identifier())) {
			throw new IllegalStateException("Property value of property '" + property.getPropertyName() + "' must be initialized only once.");
		}
		update(property, value);
	}

	private void checkNotConfigInterfaceProperty(PropertyDescriptor property) {
		if (isConfigInterfaceProperty(property)) {
			throw new IllegalArgumentException(
				"Property '" + ConfigurationItem.CONFIGURATION_INTERFACE_NAME + "' can not be changed.");
		}
	}

	private boolean isConfigInterfaceProperty(PropertyDescriptor property) {
		return property.getPropertyName().equals(ConfigurationItem.CONFIGURATION_INTERFACE_NAME);
	}

	@Override
	public void reset(PropertyDescriptor property) {
		values.remove(property.identifier());
		resetValueSet(property);
	}

	@Override
	public final void resetValueSet(PropertyDescriptor property) {
		values.remove(property.getValueSetIdentifier());
	}

	@Override
	public void initLocation(Location location) {
		checkNoResult();
		this._location = location;
	}
	
	@Override
	public Location location() {
		return _location;
	}
	
	@Override
	public final ConfigurationItem createConfig(InstantiationContext instantiationContext) {
		if (_result == null) {
			if (areChecksEnabled()) {
				// Note: Locally checking is sufficient, since the whole tree is transformed to
				// configuration items and checks are done at each level of instantiation.
				checkLocal(instantiationContext);
			}
			createInnerConfigs(instantiationContext);
			try {
				_result = instantiateConfigItem(instantiationContext);
			} catch (ConfigurationException ex) {
				instantiationContext.error("Instantiating configuration failed.", ex);
			}
		}
		return _result;
	}

	private ConfigurationItem instantiateConfigItem(InstantiationContext instantiationContext)
			throws ConfigurationException {
		try {
			return configurationDescriptor.factory().createCopy(this);
		} catch (RuntimeException ex) {
			instantiationContext.error(ex.getMessage(), ex);
			return null;
		}
	}

	private void createInnerConfigs(InstantiationContext instantiationContext) {
		for (PropertyDescriptor property : configurationDescriptor.getProperties()) {
			if (!values.containsKey(property.identifier())) {
				continue;
			}
			createInnerConfigs(property, instantiationContext);
		}
	}

	private void createInnerConfigs(PropertyDescriptor property, InstantiationContext instantiationContext) {
		switch (property.kind()) {
			case PLAIN:
			case COMPLEX: {
				// Nothing to do, as there are no ConfigBuilders here, only pure values.
				return;
			}
			case DERIVED: {
				// Derived properties are built when their source position is built.
				return;
			}
			case REF:
			case ITEM: {
				createPropertyKindItem(property, instantiationContext);
				return;
			}
			case ARRAY: {
				createArray(property, instantiationContext);
				return;
			}
			case LIST: {
				createPropertyKindList(property, instantiationContext);
				return;
			}
			case MAP: {
				createPropertyKindMap(property, instantiationContext);
				return;
			}
			default: {
				throw new UnreachableAssertion("Unknown PropertyKind: " + property.kind());
			}
		}
	}

	private void createPropertyKindItem(PropertyDescriptor property, InstantiationContext instantiationContext) {
		Object value = values.get(property.identifier());
		assert property.isInstanceValued() || ((value == null) || (value instanceof ConfigurationItem));
		if (value instanceof ConfigBuilder) {
			ConfigBuilder innerBuilder = (ConfigBuilder) value;
			ConfigurationItem innerConfig = innerBuilder.createConfig(instantiationContext);
			Object innerValue = property.getConfigurationAccess().createValue(instantiationContext, innerConfig);
			assert (innerValue != null) || instantiationContext.hasErrors();
			values.put(property.identifier(), innerValue);
		}
	}

	private void createArray(PropertyDescriptor property, InstantiationContext instantiationContext) {
		Object rawValue = values.get(property.identifier());
		if (rawValue == null) {
			return;
		}
		List<Object> list =
			instantiateCollection(property, instantiationContext, PropertyDescriptorImpl.arrayAsList(rawValue));
		values.put(property.identifier(), PropertyDescriptorImpl.listAsArray(property, list));
	}

	private void createPropertyKindList(PropertyDescriptor property, InstantiationContext instantiationContext) {
		Object rawValue = values.get(property.identifier());
		if (rawValue == null) {
			return;
		}
		instantiateCollection(property, instantiationContext, rawValue);
	}

	private List<Object> instantiateCollection(PropertyDescriptor property, InstantiationContext instantiationContext,
			Object rawValue) {
		List<Object> valueList = (List<Object>) rawValue;
		for (ListIterator<Object> iterator = valueList.listIterator(); iterator.hasNext();) {
			Object entry = iterator.next();
			if (entry instanceof ConfigBuilder) {
				ConfigBuilder innerBuilder = (ConfigBuilder) entry;
				ConfigurationItem innerConfig = innerBuilder.createConfig(instantiationContext);
				Object innerValue = property.getConfigurationAccess().createValue(instantiationContext, innerConfig);
				assert (innerValue != null) || instantiationContext.hasErrors();
				if (innerValue != null || property.isNullable()) {
					iterator.set(innerValue);
				} else {
					iterator.remove();
				}
			}
		}
		return valueList;
	}

	private void createPropertyKindMap(PropertyDescriptor property, InstantiationContext instantiationContext) {
		Object rawValue = values.get(property.identifier());
		if (rawValue == null) {
			return;
		}
		Map<Object, Object> valueMap = (Map<Object, Object>) rawValue;
		Iterator<Entry<Object, Object>> entries = valueMap.entrySet().iterator();
		while (entries.hasNext()) {
			Entry<Object, Object> entry = entries.next();
			assert !(entry.getKey() instanceof ConfigurationItem);
			if (entry.getValue() instanceof ConfigBuilder) {
				ConfigBuilder innerBuilder = (ConfigBuilder) entry.getValue();
				ConfigurationItem innerConfig = innerBuilder.createConfig(instantiationContext);
				Object innerValue = property.getConfigurationAccess().createValue(instantiationContext, innerConfig);
				assert (innerValue != null) || instantiationContext.hasErrors();
				if (innerValue != null || property.isNullable()) {
					entry.setValue(innerValue);
				} else {
					entries.remove();
				}
			}
		}
		return;
	}

	@Override
	public boolean addConfigurationListener(PropertyDescriptor property, ConfigurationListener listener) {
		throw new UnsupportedOperationException("Builders cannot be observed.");
	}

	@Override
	public boolean removeConfigurationListener(PropertyDescriptor property, ConfigurationListener listener) {
		throw new UnsupportedOperationException("Builders cannot be observed.");
	}

	@Override
	public void check(Log protocol) {
		for (PropertyDescriptor property : descriptor().getProperties()) {
			property.checkMandatory(protocol, this);
			for (ConfigurationItem childConfig : ConfigUtil.getChildConfigs(this, property)) {
				childConfig.check(protocol);
			}
		}
	}

	/**
	 * Checks properties of this item without recursing to contents.
	 */
	private void checkLocal(Log protocol) {
		for (PropertyDescriptor property : descriptor().getProperties()) {
			property.checkMandatory(protocol, this);
		}
	}

	@Override
	public Class<?> getConfigurationInterface() {
		return descriptor().getConfigurationInterface();
	}

	@Override
	public Unimplementable unimplementable() {
		return null;
	}

	private void checkNoResult() {
		if (_result != null) {
			throw new IllegalStateException("Modification is no longer allowed, as the result has already been built.");
		}
	}

	@Override
	public String toString() {
		return new NameBuilder(this)
			.add("config-interface", configurationDescriptor.getConfigurationInterface().getName())
			.add("location", _location.toString())
			.add("values", values)
			.add("config-created?", Boolean.toString(_result != null))
			.buildName();
	}

}
