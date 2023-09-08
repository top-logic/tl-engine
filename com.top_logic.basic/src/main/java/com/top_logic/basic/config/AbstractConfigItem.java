/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import static com.top_logic.basic.shared.collection.CollectionUtilShared.*;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import com.top_logic.basic.Log;
import com.top_logic.basic.NamedConstant;
import com.top_logic.basic.config.container.ConfigPart;
import com.top_logic.basic.config.container.ConfigPartUtilInternal;

/**
 * Abstract super class for reflective implementations of
 * {@link ConfigurationItem} described by some {@link ConfigurationDescriptor}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
abstract class AbstractConfigItem extends ReflectiveConfigItem implements Change {

	/**
	 * The entry in {@link #values} that stores the {@link #container()}, if this is a
	 * {@link ConfigPart}.
	 */
	private static final NamedConstant CONTAINER_IDENTIFIER = new NamedConstant("container()");

	private static final NamedConstant CONFIG_INTERFACE_ID =
		TypedConfiguration.getConfigurationDescriptor(ConfigurationItem.class)
			.getProperty(ConfigurationItem.CONFIGURATION_INTERFACE_NAME).identifier();

	private static final NamedConstant ANNOTATION_TYPE_ID =
		TypedConfiguration.getConfigurationDescriptor(Annotation.class)
			.getProperty(ConfigurationItem.ANNOTATION_TYPE).identifier();

	private final AbstractConfigurationDescriptor type;

	private final Map<NamedConstant, Object> values;

	private ConfigurationListener globalListener;

	public AbstractConfigItem(AbstractConfigurationDescriptor type, Map<NamedConstant, Object> values) {
		super();
		checkDescriptorFrozen(type);
		this.type = type;
		this.values = values;
	}

	private void checkDescriptorFrozen(AbstractConfigurationDescriptor descriptor) {
		if (!descriptor.isFrozen()) {
			throw new IllegalStateException("Creating " + descriptor.getConfigurationInterface().getName()
				+ " instance not possible. Its " + ConfigurationDescriptor.class.getSimpleName()
				+ " is not finished, yet.");
		}
	}

	/**
	 * Checks the initial values and initializes their properties.
	 * <p>
	 * More precisely: Removes all values from {@link AbstractConfigItem#values} and re-inserts them
	 * via {@link ConfigurationItem#update(PropertyDescriptor, Object)}. This way, all the property
	 * value check and update code is executed.
	 * </p>
	 */
	void initValues() {
		Map<NamedConstant, Object> uncheckedValues = new HashMap<>(values);
		values.clear();
		insertValues(uncheckedValues);
	}

	private void insertValues(Map<NamedConstant, Object> newValues) {
		for (PropertyDescriptor property : descriptor().getProperties()) {
			NamedConstant identifier = property.identifier();
			if (newValues.containsKey(identifier)) {
				update(property, newValues.remove(identifier));
			}
			// Reset the valueSet marker, if not marked explicitly set in the new values.
			NamedConstant valueSetIdentifier = property.getValueSetIdentifier();
			if (newValues.remove(valueSetIdentifier) == null) {
				values.remove(valueSetIdentifier);
			}
		}
		if (!newValues.isEmpty()) {
			throw new IllegalArgumentException(descriptor().getConfigurationInterface().getName()
				+ " has no such properties, but got inital values for them: " + newValues);
		}
	}

	protected final void markSet(PropertyDescriptor property) {
		values.put(property.getValueSetIdentifier(), Boolean.TRUE);
	}

	@Override
	public AbstractConfigurationDescriptor descriptor() {
		return type;
	}

	@Override
	public boolean valueSet(PropertyDescriptor property) {
		/* Only 'true' is explicitly stored. (Memory optimization) */
		return values.containsKey(property.getValueSetIdentifier());
	}

	/**
	 * Checks whether a value is present in the internal value map for the given property.
	 * 
	 * <p>
	 * In case the property is of {@link PropertyDescriptor#kind() kind} {@link PropertyKind#LIST}
	 * or {@link PropertyKind#MAP}, the property is not treated as set if the {@link List} or
	 * {@link Map} is empty.
	 * </p>
	 * 
	 * <p>
	 * This is not a technical simplification and not a restriction, because the default value is
	 * empty.
	 * </p>
	 */
	protected final boolean directlySet(PropertyDescriptor property) {
		NamedConstant identifier = property.identifier();
		switch (property.kind()) {
			case LIST: {
				Object propertyValue = values.get(identifier);
				return propertyValue != null && !((List<?>) propertyValue).isEmpty();
			}
			case MAP: {
				Object propertyValue = values.get(identifier);
				return propertyValue != null && !((Map<?, ?>) propertyValue).isEmpty();
			}
			case ARRAY:
				// No special handling, array values are set directly.
			default: {
				return values.containsKey(identifier);
			}
		}
	}
	
	protected final Object updateDirectly(PropertyDescriptorImpl property, Object newValue) {
		if (isConfigInterfaceProperty(property)) {
			throw new IllegalArgumentException(property + " is immutable.");
		}
		switch (property.kind()) {
			case ARRAY:
				// Copy value to avoid silent modifications to the shared array.
				newValue = PropertyDescriptorImpl.copyArray(property, newValue);
				break;
			case LIST: {
				if (!(newValue instanceof PropertyList)) {
					newValue = wrapWithPropertyList(property, (List<?>) newValue);
				}
				break;
			}
			case MAP: {
				if (!(newValue instanceof PropertyMap)) {
					newValue = wrapWithPropertyMap(property, (Map<?, ?>) newValue);
				}
				break;
			}
			default: {
				break;
			}
		}
		return this.values.put(property.identifier(), newValue);
	}

	protected PropertyList<Object> wrapWithPropertyList(PropertyDescriptorImpl property, Collection<?> value) {
		return new PropertyList<>(this, property, nonNull(value));
	}

	protected PropertyMap<Object, Object> wrapWithPropertyMap(PropertyDescriptorImpl property, Map<?, ?> value) {
		return new PropertyMap<>(this, property, nonNull(value));
	}

	Change onChange(PropertyDescriptor property) {
		assert property != null : "Only non null properties may change their values.";
		ConfigurationListener selectiveListener = getListener(property);
		if (globalListener == null && selectiveListener == null) {
			return this;
		}
		return new ChangeEvent(this, property, combine(globalListener, selectiveListener));
	}

	private ConfigurationListener combine(ConfigurationListener l1, ConfigurationListener l2) {
		ConfigurationListener receivers;
		if (l1 == null) {
			receivers = l2;
		}
		else if (l2 == null) {
			receivers = l1;
		}
		else {
			receivers = new MultiCast(l1, l2);
		}
		return receivers;
	}

	/**
	 * Returns the value which was present in the local value map for the given
	 * property.
	 * 
	 * No check whether the property belongs to the {@link #descriptor()}
	 * happens.
	 */
	protected final Object directValue(PropertyDescriptor property) {
		if (isConfigInterfaceProperty(property)) {
			// is implemented directly
			return getConfigurationInterface();
		}
		if (property.hasContainerAnnotation()) {
			return container();
		}
		Object result = values.get(property.identifier());
		switch (property.kind()) {
			case ARRAY: {
				if (result != null) {
					// Copy to avoid modification of internal value
					result = PropertyDescriptorImpl.copyArray(property, result);
				}
				break;
			}
			default: {
				break;
			}
		}
		return result;
	}

	@Override
	public final Object value(PropertyDescriptor property) {
		PropertyDescriptorImpl propertyImpl = (PropertyDescriptorImpl) checkLegalProperty(descriptor(), property);
		return internalValue(propertyImpl);
	}

	protected abstract Object internalValue(PropertyDescriptorImpl property);

	@Override
	public final Object update(PropertyDescriptor property, Object value) {
		PropertyDescriptorImpl propertyImpl = (PropertyDescriptorImpl) checkLegalProperty(descriptor(), property);
		return internalUpdate(propertyImpl, value);
	}

	/**
	 * Internal setter invoked by typed access through the typed interface.
	 * 
	 * @param property
	 *        See {@link #update(PropertyDescriptor, Object)}.
	 * @param value
	 *        See {@link #update(PropertyDescriptor, Object)}.
	 * 
	 * @return See {@link #update(PropertyDescriptor, Object)}.
	 */
	protected abstract Object internalUpdate(PropertyDescriptorImpl property, Object value);

	@Override
	public final void reset(PropertyDescriptor property) {
		PropertyDescriptorImpl propertyImpl = (PropertyDescriptorImpl) checkLegalProperty(descriptor(), property);
		internalReset(propertyImpl);
	}

	protected void internalReset(PropertyDescriptorImpl property) {
		Object oldValue = value(property);
		values.remove(property.identifier());
		values.remove(property.getValueSetIdentifier());
		Object newValue = value(property);
		onChange(property).update(property, oldValue, newValue);
	}

	@Override
	public ConfigurationItem container() {
		return (ConfigurationItem) values.get(CONTAINER_IDENTIFIER);
	}

	@Override
	void updateContainer(ConfigurationItem newContainer) {
		assert values.get(CONTAINER_IDENTIFIER) == null || newContainer == null : "Either init or reset container.";
		Object oldContainer = values.put(CONTAINER_IDENTIFIER, newContainer);
		notifyContainerPropertiesListeners(oldContainer, newContainer);
	}

	private void notifyContainerPropertiesListeners(Object oldContainer, Object newContainer) {
		// It is possible that multiple container properties exist,
		// therefore keep searching after the first container property.
		for (PropertyDescriptorImpl property : descriptor().getProperties()) {
			if (property.hasContainerAnnotation()) {
				onChange(property).update(property, oldContainer, newContainer);
			}
		}
	}

	@Override
	public boolean addConfigurationListener(PropertyDescriptor property, ConfigurationListener listener) {
		if (property != null) {
			PropertyDescriptor ownProperty = getLocalProperty(descriptor(), property);
			if (ownProperty == null) {
				// Foreign property. No changes.
				return false;
			}
			property = ownProperty;
		} else {
			// global listener
		}
		ConfigurationListener clash = setListener(property, listener);
		if (clash == null) {
			// The first listener.
			return true;
		}
		else if (clash instanceof MultiCast) {
			setListener(property, clash);
			return ((MultiCast) clash).add(listener);
		}
		else {
			if (clash == listener) {
				// Not added again.
				return false;
			} else {
				// The second listener.
				setListener(property, new MultiCast(clash, listener));
				return true;
			}
		}
	}

	@Override
	public boolean removeConfigurationListener(PropertyDescriptor property, ConfigurationListener listener) {
		if (property != null) {
			PropertyDescriptor ownProperty = getLocalProperty(descriptor(), property);
			if (ownProperty == null) {
				// Foreign property. No changes.
				return false;
			}
			property = ownProperty;
		} else {
			// global listener
		}
		ConfigurationListener existing = getListener(property);
		if (existing == null) {
			// No listeners at all.
			return false;
		}
		else if (existing instanceof MultiCast) {
			MultiCast listeners = (MultiCast) existing;
			if (listeners.size() == 2) {
				// Decompose into simple listener.
				ConfigurationListener first = listeners.get(0);
				ConfigurationListener second = listeners.get(1);
				if (first == listener) {
					setListener(property, second);
					return true;
				}
				else if (second == listener) {
					setListener(property, first);
					return true;
				}
				else {
					// Unchanged.
					return false;
				}
			} else {
				return listeners.remove(listener);
			}
		}
		else {
			if (existing == listener) {
				// Removed.
				removeListener(property);
				return true;
			} else {
				return false;
			}
		}
	}

	private ConfigurationListener getListener(PropertyDescriptor property) {
		if (property == null) {
			return globalListener;
		} else {
			return (ConfigurationListener) values.get(property.getListenerIdentifier());
		}
	}

	private ConfigurationListener setListener(PropertyDescriptor property, ConfigurationListener listener) {
		ConfigurationListener before;
		if (property == null) {
			before = globalListener;
			globalListener = listener;
		} else {
			before = (ConfigurationListener) values.put(property.getListenerIdentifier(), listener);
		}
		return before;
	}

	private void removeListener(PropertyDescriptor property) {
		if (property == null) {
			globalListener = null;
		} else {
			values.remove(property.getListenerIdentifier());
		}
	}

	@Override
	public void check(Log protocol) {
		for (PropertyDescriptorImpl property : descriptor().getProperties()) {
			property.checkMandatory(protocol, this);
			for (ConfigurationItem childConfig : ConfigUtil.getChildConfigs(this, property)) {
				childConfig.check(protocol);
			}
		}
	}

	/**
	 * Checks whether it is legal to ask the given {@link ConfigurationItem} with the given
	 * {@link PropertyDescriptor}, that is if the {@link PropertyDescriptor} is known by the
	 * descriptor or one of its {@link ConfigurationDescriptor#getSuperDescriptors() super
	 * descriptors}.
	 * 
	 * @return The {@link PropertyDescriptor} of the given {@link ConfigurationDescriptor}. It may
	 *         differ from the given one because it may be the property of a super or sub
	 *         descriptor.
	 */
	static PropertyDescriptor checkLegalProperty(ConfigurationDescriptor descriptor,
			PropertyDescriptor property) {
		if (property == null) {
			throw new NoSuchElementException("No such property 'null'.");
		}
		PropertyDescriptor myPropertyDescr = getLocalProperty(descriptor, property);
		if (myPropertyDescr == null) {
			throw new NoSuchElementException("No such property '" + property.getPropertyName() + "' in '"
				+ descriptor.getConfigurationInterface().getName() + "'.");
		}
		return myPropertyDescr;
	}

	/**
	 * Gets the "local" version of the given {@link PropertyDescriptor}.
	 * 
	 * <p>
	 * If the given {@link PropertyDescriptor} represents the a property of the given
	 * {@link ConfigurationItem} but actually belongs to a sub or super configuration, then the
	 * corresponding {@link PropertyDescriptor} of the {@link ConfigurationDescriptor descriptor} of
	 * the given configuration is returned. Otherwise <code>null</code>.
	 * </p>
	 * 
	 * @param descriptor
	 *        The {@link ConfigurationDescriptor} to get the correct descriptor from.
	 * @return May be <code>null</code>, if the given {@link PropertyDescriptor} does neither belong
	 *         to a sub nor a super configuration interface.
	 */
	static PropertyDescriptor getLocalProperty(ConfigurationDescriptor descriptor, PropertyDescriptor property) {
		PropertyDescriptor myPropertyDescr = descriptor.getProperty(property.getPropertyName());
		if (myPropertyDescr != null && myPropertyDescr.identifier() != property.identifier()) {
			myPropertyDescr = null;
		}
		return myPropertyDescr;
	}

	private boolean isConfigInterfaceProperty(PropertyDescriptor property) {
		NamedConstant id = property.identifier();
		return id == CONFIG_INTERFACE_ID || id == ANNOTATION_TYPE_ID;
	}

	@Override
	public void update(PropertyDescriptor property, Object oldValue, Object newValue) {
		// Ignore.
	}

	@Override
	public void add(PropertyDescriptor property, int index, Object element) {
		markSet(property);
		ConfigPartUtilInternal.initContainer(element, getInterface());
	}

	@Override
	public void remove(PropertyDescriptor property, int index, Object element) {
		markSet(property);
		ConfigPartUtilInternal.clearContainer(element);
	}

}
