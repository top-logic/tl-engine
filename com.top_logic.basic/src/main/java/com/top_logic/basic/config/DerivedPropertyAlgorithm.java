/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.annotation.Derived;
import com.top_logic.basic.func.Function1;
import com.top_logic.basic.func.IGenericFunction;
import com.top_logic.basic.type.PrimitiveTypeUtil;

/**
 * Algorithm computing a value of a {@link Derived} property or some other value that is solely
 * derived from a single {@link ConfigurationItem} model.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class DerivedPropertyAlgorithm extends Function1<Object, ConfigurationItem> {

	/**
	 * Creates a {@link DerivedPropertyAlgorithm}.
	 * 
	 * @param property
	 *        The property the algorithms is defined on.
	 * @param function
	 *        See {@link #getFunction()}.
	 * @param argumentReferenceNames
	 *        See {@link #getPaths()}.
	 * @param argumentReferences
	 *        See {@link #getArgumentReferences()}
	 * @return The new algorithm.
	 */
	public static DerivedPropertyAlgorithm createAlgorithm(PropertyDescriptor property,
			IGenericFunction<?> function, NamePath[] argumentReferenceNames, PropertyPath[] argumentReferences) {
		return new DerivedPropertyAlgorithm(
			property, property.getType(), function, argumentReferenceNames, argumentReferences);
	}

	/**
	 * Variant of
	 * {@link #createAlgorithm(PropertyDescriptor, IGenericFunction, NamePath[], PropertyPath[])}
	 * where the result type of the function is explicit given. Use this variant, if the algorithm
	 * should not compute the value of the property.
	 */
	public static DerivedPropertyAlgorithm createAlgorithm(PropertyDescriptor property, Class<?> resultType,
			IGenericFunction<?> function, NamePath[] argumentReferenceNames, PropertyPath[] argumentReferences) {
		return new DerivedPropertyAlgorithm(property, resultType, function, argumentReferenceNames, argumentReferences);
	}

	private final Class<?> _resultType;

	private final IGenericFunction<?> _function;

	private final NamePath[] _paths;

	private final PropertyPath[] _argumentReferences;

	private final PropertyDescriptor _property;

	private DerivedPropertyAlgorithm(PropertyDescriptor property, Class<?> resultType, IGenericFunction<?> function,
			NamePath[] paths, PropertyPath[] argumentReferences) {
		_property = property;
		_resultType = resultType;
		_function = function;
		// Prevent aftereffects of invalid paths
		boolean pathValid = argumentReferences.length > 0;
		_paths = pathValid ? paths : new NamePath[0];
		_argumentReferences = argumentReferences;
	}

	/**
	 * The property, this {@link DerivedPropertyAlgorithm} is defined on.
	 */
	public PropertyDescriptor getProperty() {
		return _property;
	}

	/**
	 * The model type, this {@link DerivedPropertyAlgorithm} is defined on.
	 */
	public ConfigurationDescriptor getDescriptor() {
		return _property.getDescriptor();
	}

	/**
	 * The {@link IGenericFunction} that implements this {@link DerivedPropertyAlgorithm}.
	 */
	public IGenericFunction<?> getFunction() {
		return _function;
	}

	/**
	 * Property reference paths to the argument properties.
	 */
	public PropertyPath[] getArgumentReferences() {
		return _argumentReferences;
	}

	/**
	 * Names of properties in {@link #getArgumentReferences()}.
	 */
	public NamePath[] getPaths() {
		return _paths;
	}

	/**
	 * The {@link AlgorithmSpec} that created this {@link DerivedPropertyAlgorithm}.
	 */
	public AlgorithmSpec getSpec() {
		return AlgorithmSpec.create(getFunction(), getPaths());
	}

	@Override
	public Object apply(ConfigurationItem input) {
		try {
			Object[] args = getArgumentValues(input);
			Object result = _function.invoke(args);
			if (_resultType.isPrimitive()) {
				return implicitPrimitiveConversion(result);
			}
			return result;
		} catch (RuntimeException ex) {
			// Enable finding the problematic property form exception stack traces.
			throw new RuntimeException("Error computing value of derived property '" + _property + "'.", ex);
		}
	}

	private Object implicitPrimitiveConversion(Object result) {
		if (result == null) {
			return result;
		}
		Class<?> expectedObjectType = PrimitiveTypeUtil.asNonPrimitive(_resultType);
		if (expectedObjectType.isInstance(result)) {
			return result;
		}
		return implicitPrimitiveConversion(result, expectedObjectType);
	}

	private Object implicitPrimitiveConversion(Object result, Class<?> expectedType) {
		try {
			return PrimitiveTypeUtil.numberConversion(result, expectedType);
		} catch (RuntimeException ex) {
			throw new RuntimeException("Failed to convert value " + StringServices.getObjectDescription(result)
				+ " to type " + _resultType + ". Property: " + getProperty() + " Cause: " + ex.getMessage(), ex);
		}
	}

	private Object[] getArgumentValues(ConfigurationItem self) {
		int argCnt = _argumentReferences.length;
		Object[] args = new Object[argCnt];
		for (int arg = 0; arg < argCnt; arg++) {
			PropertyPath propertyPath = _argumentReferences[arg];
			Object argumentValue;
			if (propertyPath.size() == 0) {
				/* This "absurd" path can be used to force the typed configuration treating an ITEM
				 * property as derived. */
				argumentValue = self;
			} else {
				argumentValue = getArgumentValue(self, propertyPath, 0);
			}
			args[arg] = argumentValue;
		}
		return args;
	}

	private Object getArgumentValue(ConfigurationItem self, PropertyPath path, int step) {
		PropertyDescriptor staticProperty = path.get(step);
		PropertyDescriptor runtimeProperty = getRuntimeProperty(self, staticProperty);
		Object value;
		if (runtimeProperty == null) {
			return null;
		} else if (self.valueSet(runtimeProperty) || !runtimeProperty.isMandatory()) {
			value = self.value(runtimeProperty);
		} else {
			/* When the property is mandatory and not set, the value is null. See: #18097, comment 4
			 * and 5. */
			return null;
		}
		if (step == path.size() - 1) {
			return value;
		}
		switch (runtimeProperty.kind()) {
			case ITEM: {
				if (value == null) {
					return null;
				} else {
					return getArgumentValue((ConfigurationItem) value, path, step + 1);
				}
			}
			case LIST: {
				return nextStep(((List<?>) value).stream(), path, step);
			}
			case MAP: {
				return nextStep(((Map<?, ?>) value).values().stream(), path, step);
			}
			case ARRAY: {
				return nextStep(Arrays.stream((Object[]) value), path, step);
			}
			case DERIVED:
				if (value == null) {
					return null;
				}
				if (value instanceof Collection) {
					return nextStep(((Collection<?>) value).stream(), path, step);
				} else if (value instanceof Map) {
					return nextStep(((Map<?, ?>) value).values().stream(), path, step);
				} else if (value.getClass().isArray()) {
					return nextStep(Arrays.stream((Object[]) value), path, step);
				} else {
					return getArgumentValue((ConfigurationItem) value, path, step + 1);
				}
			default:
				throw new IllegalArgumentException();
		}

	}

	private List<Object> nextStep(Stream<?> stream, PropertyPath path, int step) {
		return stream
			.map(ConfigurationItem.class::cast)
			.map(v -> getArgumentValue(v, path, step + 1))
			.filter(Objects::nonNull).collect(Collectors.toList());
	}

	private PropertyDescriptor getRuntimeProperty(ConfigurationItem config, PropertyDescriptor property) {
		ConfigurationDescriptor runtimeDescriptor = config.descriptor();
		return runtimeDescriptor.getProperty(property.getPropertyName());
	}

}