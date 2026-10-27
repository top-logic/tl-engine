/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.top_logic.basic.config.annotation.Binding;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.format.BuiltInFormats;
import com.top_logic.basic.reflect.JavaTypeUtil;

/**
 * A method of an interface that a configuration implements with one configuration property per
 * method, e.g. a script implementation of a configured interface.
 *
 * <p>
 * The {@link #getPropertyName() property name} is the method name in hyphenated form, see
 * {@link ConfigurationDescriptorBuilder#getDefaultConfigurationName(String)}:
 * <code>supportsModel</code> becomes <code>supports-model</code>, <code>getModel</code> becomes
 * <code>get-model</code>.
 * </p>
 *
 * <p>
 * The methods of an interface that are implemented through properties are computed by
 * {@link #getMethodProperties(Class, Collection)}:
 * </p>
 *
 * <ul>
 * <li>Every {@link JavaTypeUtil#getAbstractMethods(Class) abstract method} is a property, which
 * must be set to implement the interface.</li>
 * <li>Every default method is an optional property. If it is not set, the Java implementation of
 * the default method applies.</li>
 * <li>Static and private methods and the methods of {@link Object} are no properties.</li>
 * <li>Several declarations of a method in the interface hierarchy (re-declarations with a
 * covariant return type or bound type arguments) are one property, described by its
 * {@link JavaTypeUtil#isMoreSpecific(Method, Method) most specific} declaration.</li>
 * </ul>
 *
 * <p>
 * A method without parameters whose (resolved) return type has a plain value representation in the
 * typed configuration is implemented by a value property of that type, see
 * {@link #getValueType(Type)}. All other methods are implemented by the configuration of an
 * implementing provider, e.g. a script.
 * </p>
 *
 * <p>
 * An interface cannot be implemented through properties (the analysis fails with a
 * {@link ConfigurationException}), if
 * </p>
 *
 * <ul>
 * <li>one of its abstract methods is overloaded, i.e. another method has the same name, or one of
 * its abstract methods is generic (declares type parameters), or</li>
 * <li>methods with different names have the same property name (e.g. <code>getUrl</code> and
 * <code>getURL</code>), or</li>
 * <li>the property name of a method is reserved for another property of the implementing
 * configuration.</li>
 * </ul>
 *
 * <p>
 * Overloaded and generic default methods are no properties, they keep their Java implementation.
 * Therefore, e.g. {@link java.util.Comparator} with its overloaded generic default methods
 * <code>thenComparing</code> can be implemented through its single abstract method
 * <code>compare</code>.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class MethodProperty {

	private final String _propertyName;

	private final Method _method;

	private final boolean _abstract;

	private final List<Object> _signature;

	private MethodProperty(String propertyName, Method method, boolean isAbstract, List<Object> signature) {
		_propertyName = propertyName;
		_method = method;
		_abstract = isAbstract;
		_signature = signature;
	}

	/**
	 * The name of the configuration property implementing {@link #getMethod()}.
	 */
	public String getPropertyName() {
		return _propertyName;
	}

	/**
	 * The most specific declaration of the implemented method.
	 *
	 * <p>
	 * Its return type is the most specific type all declarations of the method guarantee.
	 * </p>
	 */
	public Method getMethod() {
		return _method;
	}

	/**
	 * Whether the method is abstract in the analyzed interface, so that its property must be set.
	 *
	 * <p>
	 * Otherwise, the method has a default implementation that applies, if the property is not set.
	 * </p>
	 */
	public boolean isAbstract() {
		return _abstract;
	}

	/**
	 * The signature of the method in the analyzed interface.
	 *
	 * <p>
	 * All declarations of the method in the interface hierarchy, and therefore all methods a
	 * dynamic proxy may receive for it, have this signature.
	 * </p>
	 *
	 * @see JavaTypeUtil#getResolvedSignature(Class, Method)
	 */
	public List<Object> getSignature() {
		return _signature;
	}

	/**
	 * The type of the plain value implementing this method.
	 *
	 * <p>
	 * A method is implemented by a value, if it has no parameters, and its return type, resolved in
	 * the context of the implemented type, is a {@link #isValueType(Class) value type}. The
	 * implementation returns the configured value.
	 * </p>
	 *
	 * @param implementedType
	 *        The implemented interface, possibly parameterized, that determines the return type of
	 *        a method declared with a type variable. May be <code>null</code>, then a type
	 *        variable return type is not resolved.
	 * @return The value type (a primitive type for a method returning a primitive), or
	 *         <code>null</code>, if the method is not implemented by a value.
	 */
	public Class<?> getValueType(Type implementedType) {
		if (_method.getParameterCount() > 0) {
			return null;
		}
		Type returnType = _method.getGenericReturnType();
		if (implementedType != null) {
			returnType = JavaTypeUtil.resolve(implementedType, returnType);
		}
		Class<?> rawType;
		if (returnType instanceof Class<?> type) {
			rawType = type;
		} else if (returnType instanceof ParameterizedType parameterized) {
			rawType = (Class<?>) parameterized.getRawType();
		} else {
			// A type variable or generic array.
			return null;
		}
		return isValueType(rawType) ? rawType : null;
	}

	/**
	 * Whether a property of the given type holds a plain value in the typed configuration.
	 *
	 * <p>
	 * These are the types the configuration framework represents with a built-in format
	 * ({@link BuiltInFormats#isPrimitive(Class)}: primitives and their wrappers, {@link String},
	 * enumerations, {@link Class}, {@link java.util.Date}, and arrays of those), and other types
	 * that are no {@link ConfigurationItem}s but declare their {@link Format} or {@link Binding}
	 * themselves, e.g. {@link com.top_logic.basic.util.ResKey}. A property of such a type gets its
	 * format without annotation of the property.
	 * </p>
	 *
	 * @param type
	 *        The property type.
	 * @return Whether the given type is a value type.
	 */
	public static boolean isValueType(Class<?> type) {
		if (type == void.class || ConfigurationItem.class.isAssignableFrom(type)) {
			return false;
		}
		if (BuiltInFormats.isPrimitive(type)) {
			return true;
		}
		return type.isAnnotationPresent(Format.class) || type.isAnnotationPresent(Binding.class);
	}

	@Override
	public String toString() {
		return _propertyName + "(" + _method + ")";
	}

	/**
	 * The methods of the given interface that are implemented through configuration properties.
	 *
	 * @param type
	 *        The interface to implement.
	 * @param reservedNames
	 *        Names of other properties of the implementing configuration, which must not be used
	 *        for methods.
	 * @return The method properties, first the {@link #isAbstract() abstract} ones, then the ones
	 *         of default methods, each in declaration order: the methods declared in the given
	 *         interface before the ones of its super interfaces.
	 * @throws ConfigurationException
	 *         If the given interface cannot be implemented through properties, see
	 *         {@link MethodProperty}.
	 */
	public static List<MethodProperty> getMethodProperties(Class<?> type, Collection<String> reservedNames)
			throws ConfigurationException {
		if (!type.isInterface()) {
			throw new IllegalArgumentException("Not an interface: " + type.getName());
		}

		Map<List<Object>, Method> abstractMethods = new LinkedHashMap<>();
		for (Method method : JavaTypeUtil.getAbstractMethods(type)) {
			abstractMethods.put(JavaTypeUtil.getResolvedSignature(type, method), method);
		}

		Map<List<Object>, Method> methods = new LinkedHashMap<>();
		for (Class<?> declaringInterface : hierarchy(type)) {
			for (Method method : declaringInterface.getDeclaredMethods()) {
				if (!isInstanceMethod(method) || JavaTypeUtil.isObjectMethod(method)) {
					continue;
				}
				List<Object> signature = JavaTypeUtil.getResolvedSignature(type, method);
				Method before = methods.get(signature);
				if (before == null || JavaTypeUtil.isMoreSpecific(method, before)) {
					methods.put(signature, method);
				}
			}
		}

		Map<String, List<MethodProperty>> byName = new LinkedHashMap<>();
		for (Map.Entry<List<Object>, Method> entry : methods.entrySet()) {
			List<Object> signature = entry.getKey();
			Method abstractMethod = abstractMethods.get(signature);
			boolean isAbstract = abstractMethod != null;
			Method method = isAbstract ? abstractMethod : entry.getValue();
			String propertyName = ConfigurationDescriptorBuilder.getDefaultConfigurationName(method.getName());
			byName.computeIfAbsent(propertyName, x -> new ArrayList<>())
				.add(new MethodProperty(propertyName, method, isAbstract, signature));
		}

		List<MethodProperty> abstractProperties = new ArrayList<>();
		List<MethodProperty> defaultProperties = new ArrayList<>();
		for (Map.Entry<String, List<MethodProperty>> entry : byName.entrySet()) {
			String propertyName = entry.getKey();
			List<MethodProperty> group = entry.getValue();
			MethodProperty property = checkGroup(type, propertyName, group);
			if (property == null) {
				continue;
			}
			if (reservedNames.contains(propertyName)) {
				throw new ConfigurationException(
					I18NConstants.ERROR_RESERVED_METHOD_PROPERTY__INTERFACE_PROPERTY_METHOD.fill(type.getName(),
						propertyName, property.getMethod().getName()));
			}
			if (property.isAbstract()) {
				abstractProperties.add(property);
			} else {
				defaultProperties.add(property);
			}
		}
		abstractProperties.addAll(defaultProperties);
		return abstractProperties;
	}

	/**
	 * Checks the methods with the same property name.
	 *
	 * @return The property for the given methods, or <code>null</code>, if the methods are
	 *         overloaded or generic default methods that are not implemented through a property.
	 */
	private static MethodProperty checkGroup(Class<?> type, String propertyName, List<MethodProperty> group)
			throws ConfigurationException {
		Set<String> methodNames = new HashSet<>();
		for (MethodProperty property : group) {
			methodNames.add(property.getMethod().getName());
		}
		if (methodNames.size() > 1) {
			String methods = group.stream()
				.map(property -> property.getMethod().getName())
				.distinct()
				.collect(Collectors.joining(", "));
			throw new ConfigurationException(
				I18NConstants.ERROR_METHOD_PROPERTY_CLASH__INTERFACE_PROPERTY_METHODS.fill(type.getName(),
					propertyName, methods));
		}
		if (group.size() > 1) {
			for (MethodProperty property : group) {
				if (property.isAbstract()) {
					throw new ConfigurationException(
						I18NConstants.ERROR_OVERLOADED_SCRIPT_METHOD__INTERFACE_METHOD.fill(type.getName(),
							property.getMethod().toGenericString()));
				}
			}
			// Overloaded default methods keep their Java implementation.
			return null;
		}
		MethodProperty property = group.get(0);
		if (property.getMethod().getTypeParameters().length > 0) {
			if (property.isAbstract()) {
				throw new ConfigurationException(
					I18NConstants.ERROR_GENERIC_SCRIPT_METHOD__INTERFACE_METHOD.fill(type.getName(),
						property.getMethod().toGenericString()));
			}
			// Generic default methods keep their Java implementation.
			return null;
		}
		return property;
	}

	private static boolean isInstanceMethod(Method method) {
		int modifiers = method.getModifiers();
		return !method.isSynthetic() && !method.isBridge() && !Modifier.isStatic(modifiers)
			&& Modifier.isPublic(modifiers);
	}

	/**
	 * The given interface and all its super interfaces, each sub interface before its super
	 * interfaces (in the order of their declaration).
	 */
	private static List<Class<?>> hierarchy(Class<?> type) {
		List<Class<?>> result = new ArrayList<>();
		addHierarchy(result, new HashSet<>(), type);
		return result;
	}

	private static void addHierarchy(List<Class<?>> result, Set<Class<?>> seen, Class<?> type) {
		if (!seen.add(type)) {
			return;
		}
		result.add(type);
		for (Class<?> superInterface : type.getInterfaces()) {
			addHierarchy(result, seen, superInterface);
		}
	}

}
