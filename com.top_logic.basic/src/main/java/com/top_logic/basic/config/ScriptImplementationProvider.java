/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.lang.reflect.Method;
import java.util.ServiceLoader;

import com.top_logic.basic.reflect.JavaTypeUtil;

/**
 * Service provider that implements interfaces with scripts in a typed configuration.
 *
 * <p>
 * A configuration property of type {@link PolymorphicConfiguration PolymorphicConfiguration&lt;F&gt;}
 * with an interface <code>F</code> is configured with a Java implementation of <code>F</code> by
 * naming its implementation class. A {@link ScriptImplementationProvider} additionally allows to
 * implement <code>F</code> with a script in the same configuration syntax:
 * </p>
 *
 * <ul>
 * <li>An attribute value or the text content of the property element is a script, unless it is a
 * {@link ScriptImplementations#isClassName(CharSequence) qualified Java class name}, which names an
 * implementation class as before. This short-hand is only available, if <code>F</code> is a
 * functional interface (has exactly one {@link JavaTypeUtil#getAbstractMethods(Class) abstract
 * method}), see {@link #parse(PropertyDescriptor, CharSequence)}.</li>
 * <li>A property element without an implementation class attribute whose property has no concrete
 * default implementation class is read into the configuration created by
 * {@link #createScriptBuilder(PropertyDescriptor)}.</li>
 * </ul>
 *
 * <p>
 * A script configuration that the provider {@link #isScript(PolymorphicConfiguration) recognizes}
 * is written back in its {@link #getSpecification(PolymorphicConfiguration) text form}. All other
 * configurations are written as without a provider.
 * </p>
 *
 * <p>
 * The provider is found through the {@link ServiceLoader} mechanism (a resource
 * <code>META-INF/services/com.top_logic.basic.config.ScriptImplementationProvider</code> naming the
 * implementation class), because configurations are read before any application service is
 * started. There is at most one provider. Without a provider, all configuration properties are
 * implemented in Java only.
 * </p>
 *
 * @see ScriptImplementations#getProvider()
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ScriptImplementationProvider {

	/**
	 * Whether the given property accepts script implementations.
	 *
	 * <p>
	 * The given property is a {@link PropertyKind#ITEM} property without an explicit format, whose
	 * {@link PropertyDescriptor#getType() type} is a {@link PolymorphicConfiguration} and whose
	 * {@link PropertyDescriptor#getInstanceType() instance type} is an interface. The provider
	 * decides based on the property type: each script configuration it creates for the property
	 * must be an instance of the property type. A provider therefore supports the plain
	 * {@link PolymorphicConfiguration} and only those sub interfaces its script configurations
	 * implement.
	 * </p>
	 *
	 * <p>
	 * The method is called while the {@link ConfigurationDescriptor} of the property is being built.
	 * It must decide from Java types alone and must not request any configuration descriptor.
	 * </p>
	 *
	 * @param property
	 *        The property to check.
	 * @return Whether script implementations are allowed as values of the given property.
	 */
	boolean supports(PropertyDescriptor property);

	/**
	 * Creates the configuration of the script implementation given in text form.
	 *
	 * <p>
	 * Only called for {@link #supports(PropertyDescriptor) supported} properties whose instance
	 * type is a functional interface, and never for text that is a
	 * {@link ScriptImplementations#isClassName(CharSequence) class name}.
	 * </p>
	 *
	 * @param property
	 *        The property the script is configured for. Its
	 *        {@link PropertyDescriptor#getGenericType() generic type} determines the interface to
	 *        implement, see {@link JavaTypeUtil#resolve(java.lang.reflect.Type, java.lang.reflect.Type)}.
	 * @param text
	 *        The script source.
	 * @return The configuration that creates the script implementation, an instance of the type of
	 *         the given property.
	 * @throws ConfigurationException
	 *         If the script is invalid.
	 */
	PolymorphicConfiguration<?> parse(PropertyDescriptor property, CharSequence text) throws ConfigurationException;

	/**
	 * Whether the given configuration is a script configuration of this provider that is written in
	 * {@link #getSpecification(PolymorphicConfiguration) text form}.
	 *
	 * @param config
	 *        A value of a {@link #supports(PropertyDescriptor) supported} property.
	 * @return Whether {@link #getSpecification(PolymorphicConfiguration)} is able to serialize the
	 *         given configuration.
	 */
	boolean isScript(PolymorphicConfiguration<?> config);

	/**
	 * The text form of the given script configuration.
	 *
	 * @param config
	 *        A configuration for which {@link #isScript(PolymorphicConfiguration)} holds.
	 * @return The script source that {@link #parse(PropertyDescriptor, CharSequence)} reads back
	 *         into an equal configuration.
	 */
	String getSpecification(PolymorphicConfiguration<?> config);

	/**
	 * Creates the builder for a script implementation configured in element form.
	 *
	 * <p>
	 * Called for a property element without implementation class attribute, if the property has no
	 * concrete default implementation class. The element's attributes and child elements are read
	 * into the returned builder, e.g. one child element per method of the implemented interface.
	 * Values the provider sets in the builder are part of the configuration and are written back.
	 * </p>
	 *
	 * @param property
	 *        A {@link #supports(PropertyDescriptor) supported} property.
	 * @return A builder for the script configuration, or <code>null</code>, if the provider has no
	 *         element form for the given property.
	 * @throws ConfigurationException
	 *         If creating the builder fails.
	 */
	ConfigBuilder createScriptBuilder(PropertyDescriptor property) throws ConfigurationException;

	/**
	 * Creates a new script implementation for the given property, as starting point for editing.
	 *
	 * <p>
	 * This is the value a configuration editor sets, when a script is chosen as implementation of
	 * the property. Each script expression is pre-filled with a skeleton that takes the arguments
	 * of its method and is completed afterwards. Reading a configuration never uses this method.
	 * All script configurations the
	 * provider creates for the same property have the
	 * {@link PolymorphicConfiguration#getImplementationClass() implementation class} of the
	 * returned configuration, and no Java implementation that is offered as option for the
	 * property has it.
	 * </p>
	 *
	 * @param property
	 *        A {@link #supports(PropertyDescriptor) supported} property.
	 * @return A new script configuration with skeleton expressions, an instance of the type of the
	 *         given property.
	 * @throws ConfigurationException
	 *         If the interface of the given property cannot be implemented by a script.
	 */
	PolymorphicConfiguration<?> createScriptConfig(PropertyDescriptor property) throws ConfigurationException;

	/**
	 * The method of the implemented interface that the given property of the given script
	 * configuration implements.
	 *
	 * @param script
	 *        A script configuration created by this provider.
	 * @param property
	 *        A property of the given script configuration.
	 * @return The implemented method, or <code>null</code>, if the given property is no script of
	 *         a method, or the given configuration is no script configuration of this provider.
	 */
	default Method getImplementedMethod(ConfigurationItem script, PropertyDescriptor property) {
		return null;
	}

}
