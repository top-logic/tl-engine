/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.top_logic.basic.Log;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.col.MapUtil;
import com.top_logic.basic.config.customization.AnnotationCustomizations;
import com.top_logic.basic.config.internal.ItemFactory;
import com.top_logic.basic.util.ResKey;

/**
 * {@link AbstractConfigurationDescriptor} this is constructed directly.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@FrameworkInternal
public class DeclarativeConfigDescriptor extends AbstractConfigurationDescriptor {

	private Map<Class<? extends Annotation>, Annotation> _annotations;

	private final Map<String, PropertyDescriptorImpl> _properties = MapUtil.newLinkedMap(16);

	private String _resPrefix = StringServices.EMPTY_STRING;

	private final ConfigurationDescriptor[] _superDescriptors;

	private Map<Method, MethodImplementation> _methodImplementations;

	private Map<Method, PropertyDescriptor> _propsByMethod = Collections.emptyMap();

	private final Location _location;

	/**
	 * Creates a new {@link DeclarativeConfigDescriptor}.
	 * 
	 * @param annotations
	 *        See {@link #getAnnotation(AnnotationCustomizations, Class)}.
	 * @param superDescriptors
	 *        See {@link #getSuperDescriptors()}.
	 * @param location
	 *        Location where this {@link ConfigurationDescriptor} is defined.
	 */
	public DeclarativeConfigDescriptor(Map<Class<? extends Annotation>, Annotation> annotations,
			ConfigurationDescriptor[] superDescriptors, Location location) {
		super(ConfigurationItem.class);
		_annotations = annotations;
		_superDescriptors = superDescriptors;
		_methodImplementations = getMethodImplementations(superDescriptors);
		_location = location;
	}

	@Override
	public <T extends Annotation> T getAnnotation(AnnotationCustomizations customizations, Class<T> annotationType) {
		@SuppressWarnings("unchecked")
		T localAnnotation = (T) _annotations.get(annotationType);
		if (localAnnotation != null) {
			return localAnnotation;
		}
		return super.getAnnotation(customizations, annotationType);
	}

	private static Map<Method, MethodImplementation> getMethodImplementations(
			ConfigurationDescriptor[] superDescriptors) {
		Map<Method, MethodImplementation> methodImplementations;
		if (superDescriptors.length == 0) {
			methodImplementations = Collections.emptyMap();
		} else {
			methodImplementations = new HashMap<>();
			for (ConfigurationDescriptor superDescriptor : superDescriptors) {
				methodImplementations
					.putAll(((AbstractConfigurationDescriptor) superDescriptor).getMethodImplementations());
			}
		}
		return methodImplementations;
	}

	/**
	 * Adds the given {@link PropertyDescriptor} to this {@link DeclarativeConfigDescriptor}.
	 * 
	 * @param protocol
	 *        Protocol to log errors to.
	 * @param descriptor
	 *        The property to add.
	 */
	public void addPropertyDescriptor(Protocol protocol, PropertyDescriptorImpl descriptor) {
		checkNotFrozen();

		String propertyName = descriptor.getPropertyName();
		PropertyDescriptorImpl clash = _properties.put(propertyName, descriptor);
		if (clash != null) {
			_properties.put(propertyName, clash);
			protocol.error("Descriptor '" + this + "' already has a property with name '" + propertyName + "': " + clash
				+ " vs. " + descriptor);
		}
	}

	@Override
	protected void internalFreeze(Protocol protocol) {
		super.internalFreeze(protocol);
		initPropertiesByMethod(protocol);
	}

	private void initPropertiesByMethod(Log log) {
		if (getSuperDescriptors().length == 0) {
			return;
		}
		HashMap<Method, PropertyDescriptor> tmp = new HashMap<>();
		for (ConfigurationDescriptor superDescriptor : getSuperDescriptors()) {
			tmp.putAll(((AbstractConfigurationDescriptor) superDescriptor).getPropertiesByMethod());
		}
		
		for (Iterator<Entry<Method, PropertyDescriptor>> entries = tmp.entrySet().iterator(); entries.hasNext();) {
			// Replace foreign property by own property.
			Entry<Method, PropertyDescriptor> entry = entries.next();
			PropertyDescriptor property = entry.getValue();
			String propertyName = property.getPropertyName();
			PropertyDescriptorImpl ownProperty = _properties.get(propertyName);
			if (ownProperty != null) {
				entry.setValue(ownProperty);
			} else {
				entries.remove();
				log.error("SuperDescriptor '" + property.getDescriptor() + "' + declares property '" + property
					+ "' which does not exist in '" + this + "'.");
			}
		}
		_propsByMethod = tmp;
	}

	@Override
	public Collection<? extends PropertyDescriptorImpl> getProperties() {
		return _properties.values();
	}

	@Override
	public PropertyDescriptor[] getPropertiesOrdered() {
		Collection<? extends PropertyDescriptor> actuallySortedProperties = getProperties();
		return actuallySortedProperties.toArray(new PropertyDescriptor[actuallySortedProperties.size()]);
	}

	@Override
	public ConfigurationDescriptor[] getSuperDescriptors() {
		return _superDescriptors;
	}

	@Override
	public PropertyDescriptor getProperty(String name) {
		return _properties.get(name);
	}

	@Override
	public ItemFactory factory() {
		return new GenericConfigFactory(this);
	}

	/**
	 * Sets the resource prefix to internationalise the names for the {@link PropertyDescriptor}s.
	 * 
	 * @param resPrefix
	 *        Must not be <code>null</code>. If not empty it must ends with '.'. If empty the keys
	 *        are computed from {@link #getConfigurationInterface()}.
	 * 
	 * @see #getPropertyLabel(String, String)
	 */
	public void setResPrefix(String resPrefix) {
		if (resPrefix.length() > 0 && resPrefix.charAt(resPrefix.length() - 1) != '.') {
			throw new IllegalArgumentException("Prefix '" + resPrefix + "' must end with '.'");
		}
		_resPrefix = resPrefix;
	}

	/**
	 * Returns the resource prefix that is used to internationalise the names of the
	 *         properties. May be empty. In this case the label is derived from
	 *         {@link #getConfigurationInterface()}.
	 */
	public String getResPrefix() {
		return _resPrefix;
	}

	@Override
	public ResKey getPropertyLabel(String property, String keySuffix) {
		if (getResPrefix().isEmpty()) {
			return super.getPropertyLabel(property, keySuffix);
		}
		if (keySuffix == null) {
			return ResKey.legacy(getResPrefix() + property);
		} else {
			return ResKey.legacy(getResPrefix() + property + keySuffix);
		}
	}

	@Override
	protected Class<?>[] getInterfacesToImplement() {
		ConfigurationDescriptor[] superDescriptors = getSuperDescriptors();
		if (superDescriptors.length == 0) {
			return super.getInterfacesToImplement();
		}
		Class<?> configInterface = getConfigurationInterface();
		ArrayList<Class<?>> interfaces = new ArrayList<>();
		interfaces.add(configInterface);
		if (!ConfigurationItem.class.isAssignableFrom(configInterface)) {
			interfaces.add(ConfigurationItem.class);
		}
		for (ConfigurationDescriptor superDescriptor : getSuperDescriptors()) {
			for (Class<?> intfClass : ((AbstractConfigurationDescriptor) superDescriptor).getInterfacesToImplement()) {
				if (interfaces.contains(intfClass)) {
					/* Due to restrictions on the interfaces array to create ProxyClass, no two
					 * identical classes must be contained. */
					continue;
				}
				interfaces.add(intfClass);
			}
		}
		return interfaces.toArray(new Class[interfaces.size()]);
	}

	@Override
	protected Map<Method, MethodImplementation> getMethodImplementations() {
		return _methodImplementations;
	}

	@Override
	public Map<Method, PropertyDescriptor> getPropertiesByMethod() {
		return _propsByMethod;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + '(' + _location + ')';
	}

}

