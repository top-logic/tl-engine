/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.util.Arrays;

import com.top_logic.basic.Protocol;
import com.top_logic.basic.config.NamePath.PathStep;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.func.GenericFunction;
import com.top_logic.basic.func.IGenericFunction;

/**
 * Values creating a {@link DerivedPropertyAlgorithm}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class AlgorithmSpec {

	private final IGenericFunction<?> _function;

	private final NamePath[] _paths;

	/**
	 * Creates a {@link AlgorithmSpec}.
	 * 
	 * @param function
	 *        See {@link #getFunction()}.
	 * @param refs
	 *        Annotated {@link #getPaths()}.
	 * @return Never null.
	 * @throws IllegalArgumentException
	 *         If argument count does not match.
	 */
	public static AlgorithmSpec create(IGenericFunction<?> function, Ref[] refs) throws IllegalArgumentException {
		return create(function, createPaths(refs));
	}

	/**
	 * Creates a {@link AlgorithmSpec}.
	 * 
	 * @param function
	 *        See {@link #getFunction()}.
	 * @param paths
	 *        See {@link #getPaths()}.
	 * @return Never null.
	 * @throws IllegalArgumentException
	 *         If argument count does not match.
	 */
	public static AlgorithmSpec create(IGenericFunction<?> function, NamePath[] paths) throws IllegalArgumentException {
		checkArgumentCount(function, paths);
		return internalCreate(function, paths);
	}

	private static void checkArgumentCount(IGenericFunction<?> function, NamePath[] paths) {
		if (function instanceof GenericFunction<?>) {
			GenericFunction<?> gFun = (GenericFunction<?>) function;
			if (isArgumentCountWrong(gFun, paths)) {
				throw new IllegalArgumentException(
					"Mismatch of argument count. Function '" + function.getClass().getName()
						+ "' expects " + (gFun.hasVarArgs() ? "at least " : "") + gFun.getArgumentCount()
						+ " arguments, but got " + paths.length + ".");
			}
		}
	}

	private static boolean isArgumentCountWrong(GenericFunction<?> function, NamePath[] paths) {
		if (function.hasVarArgs()) {
			return paths.length < function.getArgumentCount();
		}
		return paths.length != function.getArgumentCount();
	}

	private static AlgorithmSpec internalCreate(IGenericFunction<?> function, NamePath[] paths) {
		return new AlgorithmSpec(function, paths);
	}

	private AlgorithmSpec(IGenericFunction<?> function, NamePath[] paths) {
		_function = function;
		_paths = paths;
	}

	/**
	 * The implementation function.
	 */
	public IGenericFunction<?> getFunction() {
		return _function;
	}

	/**
	 * Property reference paths evaluated for filling the arguments of {@link #getFunction()}.
	 */
	public NamePath[] getPaths() {
		return _paths;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}

		if (obj instanceof AlgorithmSpec) {
			return equalsAlgorithmSpec((AlgorithmSpec) obj);
		}

		return false;
	}

	private boolean equalsAlgorithmSpec(AlgorithmSpec other) {
		return equalsFunction(getFunction(), other.getFunction()) && equalsPaths(getPaths(), other.getPaths());
	}

	private static boolean equalsFunction(IGenericFunction<?> f1, IGenericFunction<?> f2) {
		return f1.getClass() == f2.getClass();
	}

	@Override
	public int hashCode() {
		return hashCode(getFunction()) + hashCode(getPaths());
	}

	private static int hashCode(IGenericFunction<?> function) {
		return function.getClass().hashCode();
	}

	private static int hashCode(NamePath[] paths) {
		return Arrays.hashCode(paths);
	}

	private static boolean equalsPaths(NamePath[] paths1, NamePath[] paths2) {
		return Arrays.equals(paths1, paths2);
	}

	/**
	 * Creates a {@link DerivedPropertyAlgorithm} that works on {@link ConfigurationItem}s of the
	 * given {@link ConfigurationDescriptor}.
	 * 
	 * @param protocol
	 *        The error log.
	 * @param property
	 *        The property whose algorithm this is.
	 * @param location
	 *        Description of the algorithm definition for error messages.
	 * @return The created {@link DerivedPropertyAlgorithm}.
	 */
	public DerivedPropertyAlgorithm createAlgorithm(Protocol protocol, PropertyDescriptor property, String location) {
		return createAlgorithm(protocol, property, location, property.getType());
	}

	/**
	 * A variant of {@link #createAlgorithm(Protocol, PropertyDescriptor, String)} where the result
	 * type of the algorithm is explicitly given. Use this, if the algorithm does not compute the
	 * value of the property.
	 */
	public DerivedPropertyAlgorithm createAlgorithm(
			Protocol protocol, PropertyDescriptor property, String location, Class<?> resultType) {
		PropertyPath[] argumentReferences = createArgumentReferences(protocol, property, location, resultType);
		return DerivedPropertyAlgorithm.createAlgorithm(property, resultType, _function, _paths, argumentReferences);
	}

	/**
	 * Creates a corresponding {@link AlgorithmDependency}.
	 * 
	 * @see #createAlgorithm(Protocol, PropertyDescriptor, String)
	 */
	public AlgorithmDependency createDependency(Protocol protocol, ConfigurationDescriptor descriptor) {
		return AlgorithmDependency.createDependency(protocol, descriptor, _paths);
	}

	private PropertyPath[] createArgumentReferences(
			Protocol protocol, PropertyDescriptor baseProperty, String location, Class<?> resultType) {
		AlgorithmTypeChecker typeChecker =
			new AlgorithmTypeChecker(protocol, baseProperty, _function.getClass(), resultType);
		if (typeChecker.checkFunctionResult()) {
			// Path is invalid. Prevent aftereffects.
			return new PropertyPath[0];
		}
		int argCount = _paths.length;
		final PropertyPath[] argumentReferences = new PropertyPath[argCount];
		for (int arg = 0; arg < argCount; arg++) {
			NamePath propertyNames = _paths[arg];
			PropertyDescriptor[] path = buildPath(protocol, baseProperty.getDescriptor(), propertyNames, location);
			if (path == null) {
				// Path is invalid. Prevent aftereffects.
				return new PropertyPath[0];
			}
			if (typeChecker.checkFunctionArgument(path, arg)) {
				// Path is invalid. Prevent aftereffects.
				return new PropertyPath[0];
			}
			argumentReferences[arg] = new PropertyPath(baseProperty.getDescriptor(), path);
		}
		return argumentReferences;
	}

	private PropertyDescriptor[] buildPath(Protocol protocol, ConfigurationDescriptor baseDescriptor,
			NamePath propertyPath, String location) {
		int pathLength = propertyPath.size();
		PropertyDescriptor[] path = new PropertyDescriptor[pathLength];
		for (int step = 0; step < pathLength; step++) {
			PathStep stepConfig = propertyPath.get(step);

			ConfigurationDescriptor stepDescriptor;
			Class<?> explicitType = stepConfig.getConfigType();
			if (explicitType != null) {
				// Explicitly given - like a type cast in the path.
				stepDescriptor = TypedConfiguration.getConfigurationDescriptor(explicitType);
			} else if (step == 0) {
				stepDescriptor = baseDescriptor;
			} else {
				PropertyDescriptor previousProperty = path[step - 1];
				stepDescriptor = previousProperty.getValueDescriptor();
				if (stepDescriptor == null) {
					errorNoConfigItemProperty(protocol, baseDescriptor, previousProperty, location);
					// Note: An empty path is a valid path and cannot be taken as indicator for an error.
					return null;
				}
			}
			String stepPropertyName = stepConfig.getPropertyName();
			PropertyDescriptor stepProperty = stepDescriptor.getProperty(stepPropertyName);
			if (stepProperty == null) {
				errorUndefinedProperty(protocol, baseDescriptor, stepDescriptor, stepPropertyName, location);
				// Note: An empty path is a valid path and cannot be taken as indicator for an error.
				return null;
			}
			path[step] = stepProperty;
		}
		return path;
	}

	private void errorNoConfigItemProperty(Protocol protocol, ConfigurationDescriptor baseDescriptor,
			PropertyDescriptor property, String location) {

		String message = "Property does not store ConfigItems and can therefore not be navigated through. Property '"
			+ property.getPropertyName() + "' in '" + getConfigName(property.getDescriptor())
			+ "' referenced by " + location + " in '" + getConfigName(baseDescriptor) + "'.";
		protocol.error(message);
	}

	private void errorUndefinedProperty(Protocol protocol, ConfigurationDescriptor baseDescriptor,
			ConfigurationDescriptor stepDescriptor, String stepPropertyName, String location) {

		String message = "Undefined property '" + stepPropertyName + "' in '" + getConfigName(stepDescriptor)
			+ "' referenced by " + location + " in '" + getConfigName(baseDescriptor) + "'.";
		protocol.error(message);
	}

	private String getConfigName(ConfigurationDescriptor stepDescriptor) {
		return stepDescriptor.getConfigurationInterface().getName();
	}

	static NamePath[] createPaths(Ref[] pathAnnotations) {
		int argCount = pathAnnotations.length;
		final NamePath[] paths = new NamePath[argCount];
		for (int arg = 0; arg < argCount; arg++) {
			Ref ref = pathAnnotations[arg];
			paths[arg] = NamePath.path(ref);
		}
		return paths;
	}

}
