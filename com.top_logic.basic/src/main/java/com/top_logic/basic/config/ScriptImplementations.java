/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;

import com.top_logic.basic.annotation.FrameworkInternal;

/**
 * Access to the {@link ScriptImplementationProvider} and to the decisions the configuration
 * framework bases on it.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class ScriptImplementations {

	/**
	 * Separator of the segments of a qualified class name.
	 */
	private static final char PACKAGE_SEPARATOR = '.';

	/**
	 * Character that a class name segment does not start with, although Java allows it.
	 *
	 * <p>
	 * Scripts refer to variables with this prefix, so that a variable access with a property name
	 * is not mistaken as class name.
	 * </p>
	 */
	private static final char SCRIPT_VARIABLE_PREFIX = '$';

	private static volatile ScriptImplementationProvider _installed;

	/**
	 * Lazy holder of the provider found through the {@link ServiceLoader}.
	 */
	private static final class Holder {
		static final ScriptImplementationProvider PROVIDER = lookup(ScriptImplementations.class.getClassLoader());
	}

	private ScriptImplementations() {
		// Utility class.
	}

	/**
	 * The {@link ScriptImplementationProvider} of the application.
	 *
	 * @return The provider registered through the {@link ServiceLoader} mechanism, or the one set
	 *         with {@link #installProvider(ScriptImplementationProvider)}, or <code>null</code>, if
	 *         there is none.
	 */
	public static ScriptImplementationProvider getProvider() {
		ScriptImplementationProvider installed = _installed;
		if (installed != null) {
			return installed;
		}
		return Holder.PROVIDER;
	}

	/**
	 * Looks up the {@link ScriptImplementationProvider} registered through the
	 * {@link ServiceLoader} mechanism.
	 *
	 * @param classLoader
	 *        The class loader to search service registrations in.
	 * @return The registered provider, or <code>null</code>, if there is none.
	 * @throws IllegalStateException
	 *         If more than one provider is registered.
	 */
	public static ScriptImplementationProvider lookup(ClassLoader classLoader) {
		Iterator<ScriptImplementationProvider> providers =
			ServiceLoader.load(ScriptImplementationProvider.class, classLoader).iterator();
		if (!providers.hasNext()) {
			return null;
		}
		ScriptImplementationProvider result = providers.next();
		if (providers.hasNext()) {
			List<String> names = new ArrayList<>();
			names.add(result.getClass().getName());
			while (providers.hasNext()) {
				names.add(providers.next().getClass().getName());
			}
			throw new IllegalStateException(
				"At most one " + ScriptImplementationProvider.class.getName() + " may be registered, found: " + names);
		}
		return result;
	}

	/**
	 * Installs a provider that takes precedence over the one found by the {@link ServiceLoader}.
	 *
	 * <p>
	 * Only {@link ConfigurationDescriptor}s that are created afterwards are affected, since a
	 * descriptor decides about script implementations of its properties when it is built.
	 * </p>
	 *
	 * @param provider
	 *        The provider to use, or <code>null</code> to use the one found by the
	 *        {@link ServiceLoader} again.
	 * @return The provider installed before, or <code>null</code>, if there was none.
	 */
	@FrameworkInternal
	public static ScriptImplementationProvider installProvider(ScriptImplementationProvider provider) {
		ScriptImplementationProvider before = _installed;
		_installed = provider;
		return before;
	}

	/**
	 * Whether the given property accepts script implementations.
	 *
	 * @param property
	 *        The property to check, may be <code>null</code>.
	 * @return Whether the {@link #getProvider() provider} {@link ScriptImplementationProvider#supports(PropertyDescriptor)
	 *         supports} the given property.
	 */
	public static boolean acceptsScript(PropertyDescriptor property) {
		return property instanceof PropertyDescriptorImpl && ((PropertyDescriptorImpl) property).acceptsScript();
	}

	/**
	 * Creates the builder for a script implementation of the given property configured in element
	 * form, i.e. by an element (or JSON object) without implementation class.
	 *
	 * @param property
	 *        The property whose value is read, may be <code>null</code>.
	 * @param defaultDescriptor
	 *        The descriptor that an element without implementation class is read into, if it is
	 *        no script implementation.
	 * @return The builder created by the {@link ScriptImplementationProvider}, or
	 *         <code>null</code>, if the given property does not accept scripts in element form, or
	 *         has a concrete default implementation class that the element configures.
	 * @throws ConfigurationException
	 *         If creating the builder fails, e.g. because the implemented interface cannot be
	 *         implemented by a script.
	 *
	 * @see ScriptImplementationProvider#createScriptBuilder(PropertyDescriptor)
	 */
	public static ConfigBuilder createScriptBuilder(PropertyDescriptor property,
			ConfigurationDescriptor defaultDescriptor) throws ConfigurationException {
		if (!acceptsScript(property)) {
			return null;
		}
		PropertyDescriptor classProperty = defaultDescriptor.getProperty(PolymorphicConfiguration.IMPLEMENTATION_CLASS_NAME);
		if (classProperty == null) {
			return null;
		}
		Class<?> implementationClass = (Class<?>) classProperty.getDefaultValue();
		if (implementationClass != null && !implementationClass.isInterface()
			&& DefaultConfigConstructorScheme.getFactory(implementationClass).isConcrete()) {
			return null;
		}
		return getProvider().createScriptBuilder(property);
	}

	/**
	 * Creates an empty script implementation for the given property.
	 *
	 * @param property
	 *        The property to create a value for, may be <code>null</code>.
	 * @return The script configuration created by the {@link ScriptImplementationProvider}, or
	 *         <code>null</code>, if the given property does not {@link #acceptsScript(PropertyDescriptor)
	 *         accept scripts}.
	 * @throws ConfigurationException
	 *         If the interface of the given property cannot be implemented by a script.
	 *
	 * @see ScriptImplementationProvider#createScriptConfig(PropertyDescriptor)
	 */
	public static PolymorphicConfiguration<?> createScriptConfig(PropertyDescriptor property)
			throws ConfigurationException {
		if (!acceptsScript(property)) {
			return null;
		}
		return getProvider().createScriptConfig(property);
	}

	/**
	 * Whether the given value of the given property is written as element without implementation
	 * class and without configuration interface annotation.
	 *
	 * <p>
	 * This is the case, if reading such an element creates a configuration of the same type with
	 * the same implementation class, i.e. if the value has the descriptor of the
	 * {@link #createScriptBuilder(PropertyDescriptor, ConfigurationDescriptor) script builder} of
	 * the property, its implementation class is the default of that descriptor, and it has the
	 * values the provider sets in the builder.
	 * </p>
	 *
	 * @param property
	 *        The property whose value is written.
	 * @param value
	 *        The value to write.
	 * @return Whether the type of the given value is implied by the property.
	 */
	public static boolean isImpliedScriptElement(PropertyDescriptor property, ConfigurationItem value) {
		if (!(value instanceof PolymorphicConfiguration<?>) || !acceptsScript(property)) {
			return false;
		}
		ConfigBuilder builder;
		try {
			builder = createScriptBuilder(property, property.getDefaultDescriptor());
		} catch (ConfigurationException ex) {
			return false;
		}
		if (builder == null) {
			return false;
		}
		ConfigurationDescriptor descriptor = value.descriptor();
		if (builder.descriptor() != descriptor) {
			return false;
		}
		for (PropertyDescriptor builderProperty : descriptor.getProperties()) {
			if (builder.valueSet(builderProperty)
				&& !Objects.equals(builder.value(builderProperty), value.value(builderProperty))) {
				return false;
			}
		}
		PropertyDescriptor classProperty = descriptor.getProperty(PolymorphicConfiguration.IMPLEMENTATION_CLASS_NAME);
		return Objects.equals(value.value(classProperty), classProperty.getDefaultValue());
	}

	/**
	 * Whether the given configuration text is the qualified name of an implementation class.
	 *
	 * <p>
	 * A class name consists of at least two Java identifiers separated by dots, the last of them
	 * possibly naming a nested class with <code>$</code>, e.g. <code>a.b.C</code> or
	 * <code>a.b.C$D</code>. A segment does not start with <code>$</code>. Surrounding white space
	 * is ignored. All other texts of a property that {@link #acceptsScript(PropertyDescriptor)
	 * accepts scripts} are script sources.
	 * </p>
	 *
	 * @param text
	 *        The configured text.
	 * @return Whether the text names a class.
	 */
	public static boolean isClassName(CharSequence text) {
		String name = text.toString().trim();
		int length = name.length();
		int segments = 0;
		int start = 0;
		while (start <= length) {
			int end = name.indexOf(PACKAGE_SEPARATOR, start);
			if (end < 0) {
				end = length;
			}
			if (!isClassNameSegment(name, start, end)) {
				return false;
			}
			segments++;
			start = end + 1;
		}
		return segments > 1;
	}

	private static boolean isClassNameSegment(String name, int start, int end) {
		if (start >= end) {
			return false;
		}
		char first = name.charAt(start);
		if (first == SCRIPT_VARIABLE_PREFIX || !Character.isJavaIdentifierStart(first)) {
			return false;
		}
		for (int n = start + 1; n < end; n++) {
			if (!Character.isJavaIdentifierPart(name.charAt(n))) {
				return false;
			}
		}
		return true;
	}

}
