/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.col.LazyTypedAnnotatable;
import com.top_logic.basic.config.AlgorithmDependency;
import com.top_logic.basic.config.AlgorithmSpec;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.DerivedPropertyAlgorithm;
import com.top_logic.basic.config.NamePath;
import com.top_logic.basic.config.NamePath.PathStep;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.PropertyPath;
import com.top_logic.basic.func.Function1;

/**
 * Custom property created programmatically on top of a {@link ConfigurationItem} model.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DerivedProperty<T> extends LazyTypedAnnotatable {

	private final Class<T> _type;

	private final DerivedPropertyAlgorithm _algorithm;

	private final AlgorithmDependency _dependency;

	/**
	 * Creates a {@link DerivedProperty}.
	 * <p>
	 * Both parameters are not allowed to be null.
	 * </p>
	 */
	public DerivedProperty(Class<T> type, DerivedPropertyAlgorithm algorithm, AlgorithmDependency dependency) {
		checkParams(type, algorithm, dependency); // Fail early
		_type = type;
		_algorithm = algorithm;
		_dependency = dependency;
	}

	private void checkParams(Class<T> type, DerivedPropertyAlgorithm algorithm, AlgorithmDependency dependency) {
		if (type == null) {
			throw new NullPointerException("Type is not allowed to be null.");
		}
		if (algorithm == null) {
			throw new NullPointerException("Algorithm is not allowed to be null.");
		}
		if (dependency == null) {
			throw new NullPointerException("Dependency is not allowed to be null.");
		}
	}

	/** Never null. */
	public DerivedPropertyAlgorithm getAlgorithm() {
		return _algorithm;
	}

	/**
	 * Get the {@link Value} of this property from the given model.
	 * 
	 * @return Never null, but may represent null.
	 * 
	 * @see #get(ConfigurationItem)
	 */
	public Value<T> getValue(ConfigurationItem model) {
		return new DerivedValue<>(model, _algorithm, _dependency.createUpdater());
	}

	/**
	 * Get the raw value of this property from the given model.
	 * 
	 * @return May be null.
	 * 
	 * @see #getValue(ConfigurationItem)
	 */
	public T get(ConfigurationItem model) {
		return (T) _algorithm.apply(model);
	}

	/**
	 * <ul>
	 * <li><code>true</code>: None of the parameters is <code>null</code>.</li>
	 * <li><code>false</code>: One of the parameters is <code>null</code>, for example because a
	 * property on the path to it is <code>null</code>.</li>
	 * <li><code>null</code>: None of the last steps in the parameter paths is mandatory.</li>
	 * </ul>
	 */
	public DerivedProperty<Boolean> getValidity() {
		PropertyPath[] argumentReferences = _algorithm.getArgumentReferences();
		List<PropertyPath> mandatoryPaths = new ArrayList<>(argumentReferences.length);
		for (PropertyPath path : argumentReferences) {
			if (path.size() > 0) {
				if (path.get(path.size() - 1).isMandatory()) {
					mandatoryPaths.add(path);
				}
			}
		}

		if (mandatoryPaths.size() == 0) {
			return null;
		}

		final PropertyPath[] validityReferences =
			mandatoryPaths.toArray(new PropertyPath[mandatoryPaths.size()]);

		Function1<Boolean, ConfigurationItem> isValid = new Function1<>() {
			@Override
			public Boolean apply(ConfigurationItem arg) {
				for (PropertyPath path : validityReferences) {
					Object value = arg;
					for (PropertyDescriptor ref : path.toArray()) {
						ConfigurationItem config = ref.getConfigurationAccess().getConfig(value);
						if (config == null) {
							return Boolean.FALSE;
						}
						value =
							ref.isMandatory() ? (config.valueSet(ref) ? config.value(ref) : null) : config.value(ref);
						if (value == null) {
							return Boolean.FALSE;
						}
					}
				}
				return Boolean.TRUE;
			}
		};
		PropertyDescriptor property = getAlgorithm().getProperty();
		Protocol protocol = new LogProtocol(DerivedProperty.class);
		PropertyPath[] self = self(getAlgorithm().getDescriptor());
		DerivedPropertyAlgorithm algorithm =
			DerivedPropertyAlgorithm.createAlgorithm(property, _type, isValid, toSpec(self), self);
		AlgorithmDependency dependency =
			AlgorithmDependency.createDependency(protocol, property.getDescriptor(), toSpec(validityReferences));
		protocol.checkErrors();

		return new DerivedProperty<>(Boolean.class, algorithm, dependency);
	}

	private PropertyPath[] self(ConfigurationDescriptor selfDescriptor) {
		return new PropertyPath[] { new PropertyPath(selfDescriptor) };
	}

	private static NamePath[] toSpec(PropertyPath[] refs) {
		NamePath[] paths = new NamePath[refs.length];
		for (int arg = 0, refsLength = refs.length; arg < refsLength; arg++) {
			PropertyPath ref = refs[arg];
			PathStep[] path = new PathStep[ref.size()];
			for (int step = 0, refLength = ref.size(); step < refLength; step++) {
				PropertyDescriptor property = ref.get(step);

				ConfigurationDescriptor base = step == 0 ? ref.getStart() : ref.get(step - 1).getValueDescriptor();
				PropertyDescriptor dynamicProperty = base.getProperty(property.getPropertyName());
				boolean baseHasProperty =
					dynamicProperty != null && dynamicProperty.identifier() == property.identifier();
				Class<?> type = baseHasProperty ? null : property.getDescriptor().getConfigurationInterface();
				path[step] = new PathStep(type, property.getPropertyName());
			}
			paths[arg] = new NamePath(path);
		}
		return paths;
	}

	public static <T> DerivedProperty<T> createDerivedProperty(Class<T> type, ConfigurationDescriptor descriptor,
			AlgorithmSpec spec, PropertyDescriptor property, String location) {
		LogProtocol protocol = new LogProtocol(descriptor.getConfigurationInterface());
		DerivedPropertyAlgorithm algorithm = spec.createAlgorithm(protocol, property, location, type);
		AlgorithmDependency dependency = spec.createDependency(protocol, descriptor);
	
		protocol.checkErrors();
	
		return new DerivedProperty<>(type, algorithm, dependency);
	}

}
