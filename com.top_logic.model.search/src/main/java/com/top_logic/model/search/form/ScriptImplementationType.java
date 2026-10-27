/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.form;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationDescriptorConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.MethodProperty;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.PropertyDescriptorConfig;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.format.JavaTypeFormat;
import com.top_logic.basic.reflect.JavaTypeUtil;
import com.top_logic.basic.util.ResKey;
import com.top_logic.model.search.expr.config.dom.Expr;

/**
 * The configuration type of TL-Script implementations of a Java interface.
 *
 * <p>
 * A TL-Script implementation of an interface <code>F</code> is configured with one property per
 * method of <code>F</code>, named after the method in hyphenated form:
 * </p>
 *
 * <pre>
 * &lt;builder&gt;
 *     &lt;get-model&gt;...&lt;/get-model&gt;
 *     &lt;supports-model&gt;...&lt;/supports-model&gt;
 * &lt;/builder&gt;
 * </pre>
 *
 * <p>
 * A method without parameters whose return type has a plain value representation (e.g.
 * <code>boolean</code>, {@link String}, an enumeration, see
 * {@link MethodProperty#getValueType(Type)}) is implemented by a value property of that type,
 * e.g. <code>&lt;provider is-computed-in-transaction="true" .../&gt;</code>. All other methods are
 * implemented by a TL-Script {@link Expr expression}.
 * </p>
 *
 * <p>
 * The {@link #getDescriptor() descriptor} of this configuration is created from the methods of
 * <code>F</code> (there is no Java configuration interface declaring the method properties). It
 * {@link ConfigurationDescriptor#getSuperDescriptors() extends} {@link ScriptImplementation.Config}
 * and is created once per {@link #getImplementedType() implemented type}, i.e. per interface
 * <code>F</code> with the type arguments of the configuration property, e.g.
 * <code>Mapping&lt;Object, String&gt;</code>. The implemented type is the default value of the
 * {@link WithImplementedType#IMPLEMENTED_TYPE implemented type} property and therefore not part of
 * the configuration text.
 * </p>
 *
 * <p>
 * The method properties follow the rules of {@link MethodProperty}:
 * </p>
 *
 * <ul>
 * <li>An abstract method of <code>F</code> is a mandatory property, a missing one is reported as
 * configuration error. This also holds for a value method with a primitive return type, its
 * property has no implicit default.</li>
 * <li>A default method of <code>F</code> is an optional property. If it is given, the script or
 * value overrides the Java implementation of the default method.</li>
 * <li>An interface cannot be implemented by TL-Script, if one of its abstract methods is overloaded
 * or generic, or if the property name of a method clashes with the one of another method or with
 * the {@link PolymorphicConfiguration#IMPLEMENTATION_CLASS_NAME implementation class} or
 * {@link WithImplementedType#IMPLEMENTED_TYPE implemented type} properties. Overloaded or generic
 * default methods are no properties and keep their Java implementation.</li>
 * </ul>
 *
 * <p>
 * A method property is labeled with the resource <code>&lt;F&gt;.&lt;property-name&gt;</code> (the
 * qualified name of <code>F</code> with nested class names separated by dots), like a property of a
 * configuration interface, falling back to its property name.
 * </p>
 *
 * @see TLScriptImplementationProvider
 * @see ScriptImplementation
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class ScriptImplementationType {

	/**
	 * Property names of {@link ScriptImplementation.Config} that methods must not use.
	 */
	private static final Set<String> RESERVED_NAMES = Set.of(
		PolymorphicConfiguration.IMPLEMENTATION_CLASS_NAME,
		WithImplementedType.IMPLEMENTED_TYPE,
		ConfigurationItem.CONFIGURATION_INTERFACE_NAME);

	/**
	 * Name of the property {@link ConfigurationDescriptorConfig#getResPrefix()}.
	 */
	private static final String RES_PREFIX = "res-prefix";

	/**
	 * Name of the property {@link ConfigurationDescriptorConfig#getProperties()}.
	 */
	private static final String PROPERTIES = "properties";

	/**
	 * Name of the property {@link PropertyDescriptorConfig#getAnnotations()}.
	 */
	private static final String ANNOTATIONS = "annotations";

	/**
	 * Name of the annotation property holding the value of {@link FormattedDefault#value()}.
	 */
	private static final String VALUE = "value";

	private static final Map<Type, ScriptImplementationType> BY_TYPE = new ConcurrentHashMap<>();

	private static final Map<ConfigurationDescriptor, ScriptImplementationType> BY_DESCRIPTOR =
		new ConcurrentHashMap<>();

	private final Type _implementedType;

	private final Class<?> _implementedInterface;

	private final List<MethodProperty> _methods;

	private final MethodProperty _functionalMethod;

	private final ConfigurationDescriptor _descriptor;

	private final Map<MethodProperty, PropertyDescriptor> _properties;

	private final Map<MethodProperty, Class<?>> _valueTypes;

	private ScriptImplementationType(Type implementedType, Class<?> implementedInterface,
			List<MethodProperty> methods, Map<MethodProperty, Class<?>> valueTypes,
			ConfigurationDescriptor descriptor) {
		_implementedType = implementedType;
		_implementedInterface = implementedInterface;
		_methods = methods;
		_valueTypes = valueTypes;
		_descriptor = descriptor;

		Map<MethodProperty, PropertyDescriptor> properties = new HashMap<>();
		MethodProperty functionalMethod = null;
		int abstractCount = 0;
		for (MethodProperty method : methods) {
			properties.put(method, descriptor.getProperty(method.getPropertyName()));
			if (method.isAbstract()) {
				abstractCount++;
				functionalMethod = method;
			}
		}
		_properties = properties;
		_functionalMethod = abstractCount == 1 ? functionalMethod : null;
	}

	/**
	 * The script implementation type of the given interface.
	 *
	 * @param implementedType
	 *        The implemented interface, possibly parameterized, without type variables and
	 *        wildcards, see {@link TLScriptImplementationProvider#implementedType(PropertyDescriptor)}.
	 * @return The script implementation type, created on first request.
	 * @throws ConfigurationException
	 *         If the given interface cannot be implemented by TL-Script.
	 */
	public static ScriptImplementationType of(Type implementedType) throws ConfigurationException {
		ScriptImplementationType result = BY_TYPE.get(implementedType);
		if (result == null) {
			ScriptImplementationType created = create(implementedType);
			result = BY_TYPE.putIfAbsent(implementedType, created);
			if (result == null) {
				BY_DESCRIPTOR.put(created.getDescriptor(), created);
				result = created;
			}
		}
		return result;
	}

	/**
	 * The script implementation type with the given descriptor.
	 *
	 * @param descriptor
	 *        The descriptor of a configuration.
	 * @return The script implementation type whose {@link #getDescriptor()} is the given one, or
	 *         <code>null</code>, if the given descriptor is no descriptor of a script
	 *         implementation type.
	 */
	public static ScriptImplementationType forDescriptor(ConfigurationDescriptor descriptor) {
		return BY_DESCRIPTOR.get(descriptor);
	}

	private static ScriptImplementationType create(Type implementedType) throws ConfigurationException {
		Class<?> implementedInterface = JavaTypeUtil.getRawClass(implementedType);
		if (!implementedInterface.isInterface()) {
			throw new IllegalArgumentException(
				"Script implementation requires an interface, got: " + implementedType.getTypeName());
		}
		List<MethodProperty> methods = MethodProperty.getMethodProperties(implementedInterface, RESERVED_NAMES);

		ConfigurationDescriptorConfig config = TypedConfiguration.newConfigItem(ConfigurationDescriptorConfig.class);
		set(config, ConfigurationDescriptorConfig.EXTENSION_NAME, new Class<?>[] { ScriptImplementation.Config.class });
		set(config, ConfigurationDescriptorConfig.INTERFACE, ScriptImplementation.Config.class);
		set(config, RES_PREFIX, implementedInterface.getName().replace('$', '.') + ".");

		Map<String, PropertyDescriptorConfig> properties = new LinkedHashMap<>();
		Map<MethodProperty, Class<?>> valueTypes = new HashMap<>();
		for (MethodProperty method : methods) {
			PropertyDescriptorConfig property = newProperty(method.getPropertyName());
			Class<?> valueType = method.getValueType(implementedType);
			if (valueType != null) {
				valueTypes.put(method, valueType);
				set(property, PropertyDescriptorConfig.TYPE, valueType);
			} else {
				set(property, PropertyDescriptorConfig.TYPE, Expr.class);
			}
			set(property, PropertyDescriptorConfig.LABEL, ResKey.text(method.getPropertyName()));
			if (method.isAbstract()) {
				set(property, ANNOTATIONS, annotations(TypedConfiguration.newAnnotationItem(Mandatory.class)));
			}
			properties.put(property.getName(), property);
		}

		PropertyDescriptorConfig implementedTypeProperty = newProperty(WithImplementedType.IMPLEMENTED_TYPE);
		FormattedDefault implementedTypeDefault = TypedConfiguration.newAnnotationItem(FormattedDefault.class);
		set((ConfigurationItem) implementedTypeDefault, VALUE,
			JavaTypeFormat.INSTANCE.getSpecification(implementedType));
		set(implementedTypeProperty, ANNOTATIONS, annotations(implementedTypeDefault));
		properties.put(implementedTypeProperty.getName(), implementedTypeProperty);

		set(config, PROPERTIES, properties);

		BufferingProtocol protocol = new BufferingProtocol();
		ConfigurationDescriptor descriptor = TypedConfiguration.getConfigurationDescriptor(protocol, config);
		if (protocol.hasErrors()) {
			throw new ConfigurationException("Cannot create the script configuration of '"
				+ implementedType.getTypeName() + "': " + protocol.getErrors());
		}
		return new ScriptImplementationType(implementedType, implementedInterface, methods, valueTypes, descriptor);
	}

	private static PropertyDescriptorConfig newProperty(String name) {
		PropertyDescriptorConfig result = TypedConfiguration.newConfigItem(PropertyDescriptorConfig.class);
		result.setName(name);
		return result;
	}

	private static Map<Class<? extends Annotation>, Annotation> annotations(Annotation annotation) {
		Map<Class<? extends Annotation>, Annotation> result = new LinkedHashMap<>();
		result.put(annotation.annotationType(), annotation);
		return result;
	}

	private static void set(ConfigurationItem item, String property, Object value) {
		item.update(item.descriptor().getProperty(property), value);
	}

	/**
	 * The implemented interface with the type arguments of the property the script is configured
	 * for.
	 */
	public Type getImplementedType() {
		return _implementedType;
	}

	/**
	 * The raw class of {@link #getImplementedType()}.
	 */
	public Class<?> getImplementedInterface() {
		return _implementedInterface;
	}

	/**
	 * The methods implemented by properties of {@link #getDescriptor()}.
	 *
	 * @see #getProperty(MethodProperty)
	 */
	public List<MethodProperty> getMethods() {
		return _methods;
	}

	/**
	 * The single abstract method of a functional interface.
	 *
	 * @return The abstract method, or <code>null</code>, if the {@link #getImplementedInterface()
	 *         implemented interface} has not exactly one abstract method.
	 */
	public MethodProperty getFunctionalMethod() {
		return _functionalMethod;
	}

	/**
	 * The descriptor of the script configurations, an extension of
	 * {@link ScriptImplementation.Config}.
	 */
	public ConfigurationDescriptor getDescriptor() {
		return _descriptor;
	}

	/**
	 * The property of {@link #getDescriptor()} implementing the given method.
	 *
	 * @param method
	 *        One of {@link #getMethods()}.
	 * @return The property of type {@link Expr}, or of the {@link #getValueType(MethodProperty)
	 *         value type} of a method implemented by a value.
	 */
	public PropertyDescriptor getProperty(MethodProperty method) {
		return _properties.get(method);
	}

	/**
	 * The type of the value implementing the given method.
	 *
	 * @param method
	 *        One of {@link #getMethods()}.
	 * @return The type of the value property implementing the given method, or <code>null</code>,
	 *         if the method is implemented by a TL-Script expression.
	 *
	 * @see MethodProperty#getValueType(Type)
	 */
	public Class<?> getValueType(MethodProperty method) {
		return _valueTypes.get(method);
	}

	/**
	 * Whether the given method is implemented by a value instead of a TL-Script expression.
	 *
	 * @param method
	 *        One of {@link #getMethods()}.
	 */
	public boolean isValue(MethodProperty method) {
		return _valueTypes.containsKey(method);
	}

	/**
	 * Creates an empty script configuration of this type.
	 *
	 * @return A configuration without method expressions.
	 */
	public ScriptImplementation.Config<?> newConfig() {
		return (ScriptImplementation.Config<?>) _descriptor.factory().createNew();
	}

	/**
	 * Whether the given configuration only implements the {@link #getFunctionalMethod()
	 * functional method} of the implemented interface.
	 *
	 * <p>
	 * Such a configuration is fully described by the expression (or value) of the functional
	 * method.
	 * </p>
	 *
	 * @param config
	 *        A configuration with the {@link #getDescriptor() descriptor} of this type.
	 */
	public boolean isFunctional(ConfigurationItem config) {
		if (_functionalMethod == null) {
			return false;
		}
		for (MethodProperty method : _methods) {
			if (method != _functionalMethod && config.valueSet(getProperty(method))) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "(" + _implementedType.getTypeName() + ")";
	}

}
