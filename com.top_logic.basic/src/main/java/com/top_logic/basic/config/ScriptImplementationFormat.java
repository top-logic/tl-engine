/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

/**
 * Format of a {@link PolymorphicConfiguration} property that is implemented either by a Java class
 * or by a script.
 *
 * <p>
 * A {@link ScriptImplementations#isClassName(CharSequence) class name} is read into the
 * configuration of that implementation class, any other text is a script that the
 * {@link ScriptImplementationProvider} parses. Only script configurations are written in text
 * form, configurations of Java implementations are written as elements.
 * </p>
 *
 * <p>
 * The format is not declared but created for each property that
 * {@link ScriptImplementations#acceptsScript(PropertyDescriptor) accepts scripts} and implements a
 * functional interface, see {@link PropertyDescriptorImpl}.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
final class ScriptImplementationFormat extends AbstractConfigurationValueProvider<PolymorphicConfiguration<?>> {

	private final PropertyDescriptor _property;

	private final ScriptImplementationProvider _provider;

	/**
	 * Creates a {@link ScriptImplementationFormat}.
	 *
	 * @param property
	 *        The property whose values are parsed.
	 * @param provider
	 *        The provider that {@link ScriptImplementationProvider#supports(PropertyDescriptor)
	 *        supports} the given property.
	 */
	ScriptImplementationFormat(PropertyDescriptor property, ScriptImplementationProvider provider) {
		super(PolymorphicConfiguration.class);
		_property = property;
		_provider = provider;
	}

	/**
	 * Whether the given configuration text is not handled by a {@link ScriptImplementationFormat}
	 * but by the implementation class handling of the configuration reader.
	 *
	 * @param format
	 *        The format of a property.
	 * @param text
	 *        The configured text.
	 * @return Whether the given format is a {@link ScriptImplementationFormat} and the given text
	 *         is empty or a class name.
	 */
	static boolean isImplementationClass(ConfigurationValueProvider<?> format, CharSequence text) {
		return format instanceof ScriptImplementationFormat
			&& (text.toString().trim().isEmpty() || ScriptImplementations.isClassName(text));
	}

	@Override
	protected PolymorphicConfiguration<?> getValueNonEmpty(String propertyName, CharSequence propertyValue)
			throws ConfigurationException {
		if (ScriptImplementations.isClassName(propertyValue)) {
			return javaImplementation(propertyValue.toString().trim());
		}
		return _provider.parse(_property, propertyValue);
	}

	private PolymorphicConfiguration<?> javaImplementation(String className) throws ConfigurationException {
		ConfigurationDescriptor expectedDescriptor = _property.getValueDescriptor();
		PropertyDescriptor classProperty =
			expectedDescriptor.getProperty(PolymorphicConfiguration.IMPLEMENTATION_CLASS_NAME);
		Class<?> implementationClass =
			(Class<?>) classProperty.getValueProvider().getValue(classProperty.getPropertyName(), className);
		return TypedConfiguration.createConfigItemForImplementationClass(implementationClass, expectedDescriptor);
	}

	@Override
	public boolean isLegalValue(Object value) {
		return value instanceof PolymorphicConfiguration<?> && _provider.isScript((PolymorphicConfiguration<?>) value);
	}

	@Override
	protected String getSpecificationNonNull(PolymorphicConfiguration<?> configValue) {
		return _provider.getSpecification(configValue);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "(" + _property + ")";
	}

}
