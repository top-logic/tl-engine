/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import static com.top_logic.basic.type.PrimitiveTypeUtil.*;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;

import com.top_logic.basic.Protocol;
import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.func.Function1;
import com.top_logic.basic.func.Function2;
import com.top_logic.basic.func.Function3;
import com.top_logic.basic.func.Identity;

/**
 * Searches for type errors in derived property paths.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class AlgorithmTypeChecker {

	private static final Class<?>[] FUNCTION_N_CLASSES = { Function1.class, Function2.class, Function3.class };

	private final Protocol _protocol;

	private final PropertyDescriptor _derivedProperty;

	private final Class<?> _functionClass;

	private final Class<?> _resultType;

	AlgorithmTypeChecker(
			Protocol protocol, PropertyDescriptor derivedProperty, Class<?> functionClass, Class<?> resultType) {
		_protocol = protocol;
		_derivedProperty = derivedProperty;
		_functionClass = functionClass;
		_resultType = resultType;
	}

	private Protocol getProtocol() {
		return _protocol;
	}

	private PropertyDescriptor getDerivedProperty() {
		return _derivedProperty;
	}

	private Class<?> getResultType() {
		return _resultType;
	}

	private Class<?> getFunctionClass() {
		return _functionClass;
	}

	/**
	 * Checks if the function result is compatible to the derived property type.
	 * <p>
	 * If an error is found, it is reported to the {@link Protocol}.
	 * </p>
	 * 
	 * @return true if an error is found
	 */
	boolean checkFunctionResult() {
		Maybe<Type> functionResultType = findResultTypeParameter();
		if (!functionResultType.hasValue()) {
			// Unable to check anything.
			return false;
		}
		Type functionResult = asNonPrimitive(functionResultType.get());
		Class<?> derivedProperty = asNonPrimitive(getResultType());
		Maybe<Boolean> isFunctionResultTypeSubtype = isSubtypeOf(functionResult, derivedProperty);
		if (!isFunctionResultTypeSubtype.hasValue()) {
			return false;
		}
		if (!isFunctionResultTypeSubtype.get()) {
			error("Wrong result type. Expected type: " + getResultType()
				+ ", Actual Type: " + functionResultType.get());
			return true;
		}
		return false;
	}

	/**
	 * Checks if the function argument with the given index is compatible to what the path for that
	 * argument delivers.
	 * <p>
	 * If an error is found, it is reported to the {@link #getProtocol()}.
	 * </p>
	 * 
	 * @return true if an error is found
	 */
	boolean checkFunctionArgument(PropertyDescriptor[] path, int argumentIndex) {
		if (path.length > 0) {
			int lastStep = path.length - 1;
			PropertyDescriptor sourceProperty = path[lastStep];
			return checkFunctionArgument(sourceProperty, argumentIndex);
		} else {
			return checkFunctionArgument(_derivedProperty.getDescriptor().getConfigurationInterface(), argumentIndex);
		}
	}

	private boolean checkFunctionArgument(PropertyDescriptor sourceProperty, int argumentIndex) {
		return checkFunctionArgument(sourceProperty.getType(), argumentIndex);
	}

	private boolean checkFunctionArgument(Class<?> sourceType, int argumentIndex) {
		if (!isFunctionN()) {
			// Unable to check anything.
			return false;
		}
		Type derivedType;
		if (isLikeIdentityFunction(argumentIndex)) {
			derivedType = getResultType();
			if (isIdentityFunction() && isPrimitive(derivedType) && !isPrimitive(sourceType)) {
				error("Wrong argument types. Expected the primitive type " + derivedType
					+ " but got the non-primitive type " + sourceType + ".");
				return true;
			}
			if (isPrimitive(derivedType) && isPrimitive(sourceType)) {
				if (isCompatiblePrimitive(sourceType, (Class<?>) derivedType)) {
					return false;
				}
				error("Wrong argument types. Expected the type " + derivedType
					+ " but got the type " + sourceType + ".");
				return true;
			}
		} else {
			Maybe<Type> typeParameter = findTypeParameter(argumentIndex);
			if (!typeParameter.hasValue()) {
				// Unable to check anything.
				return false;
			}
			derivedType = typeParameter.get();
		}
		return hasTypeErrors(derivedType, sourceType);
	}

	private boolean isLikeIdentityFunction(int argumentIndex) {
		Maybe<Type> outputType = findResultTypeParameter();
		if ((!outputType.hasValue()) || !(outputType.get() instanceof TypeVariable)) {
			return false;
		}
		Maybe<Type> inputType = findTypeParameter(argumentIndex);
		if ((!inputType.hasValue()) || !(inputType.get() instanceof TypeVariable)) {
			return false;
		}
		TypeVariable<?> outputTypeVariable = (TypeVariable<?>) outputType.get();
		TypeVariable<?> inputTypeVariable = (TypeVariable<?>) inputType.get();
		if (!isSameVariable(outputTypeVariable, inputTypeVariable)) {
			return false;
		}
		if (!Arrays.equals(outputTypeVariable.getBounds(), new Type[] { Object.class })) {
			return false;
		}
		return true;
	}

	private boolean isPrimitive(Type type) {
		if (!(type instanceof Class)) {
			return false;
		}
		Class<?> classType = (Class<?>) type;
		return classType.isPrimitive();
	}

	private boolean isIdentityFunction() {
		return getFunctionClass().equals(Identity.class);
	}

	private boolean isSameVariable(TypeVariable<?> subTypeVariable, TypeVariable<?> superTypeVariable) {
		return subTypeVariable.getName().equals(superTypeVariable.getName())
			&& subTypeVariable.getGenericDeclaration().equals(superTypeVariable.getGenericDeclaration());
	}

	private boolean isFunctionN() {
		for (Class<?> functionNClass : FUNCTION_N_CLASSES) {
			if (functionNClass.isAssignableFrom(getFunctionClass())) {
				return true;
			}
		}
		return false;
	}

	private Maybe<Type> findResultTypeParameter() {
		Maybe<Type[]> typeParameters = findTypeParameters();
		if (!typeParameters.hasValue()) {
			return Maybe.none();
		}
		return Maybe.some(typeParameters.get()[0]);
	}

	private Maybe<Type> findTypeParameter(int parameterIndex) {
		Maybe<Type[]> typeParameters = findTypeParameters();
		if (!typeParameters.hasValue()) {
			return Maybe.none();
		}
		// Type parameter 0 is the return type. Therefore '+1'
		return Maybe.some(typeParameters.get()[parameterIndex + 1]);
	}

	private Maybe<Type[]> findTypeParameters() {
		if (!equalsWellKnownType(getFunctionClass().getSuperclass())) {
			return Maybe.none();
		}
		ParameterizedType functionN = (ParameterizedType) getFunctionClass().getGenericSuperclass();
		return Maybe.some(functionN.getActualTypeArguments());
	}

	private boolean equalsWellKnownType(Class<?> type) {
		for (Class<?> functionNClass : FUNCTION_N_CLASSES) {
			if (functionNClass.equals(type)) {
				return true;
			}
		}
		return false;
	}

	private boolean hasTypeErrors(Type derivedType, Class<?> sourceType) {
		Maybe<Boolean> isSourceTypeSubtype = isSubtypeOf(asNonPrimitive(sourceType), asNonPrimitive(derivedType));
		if (!isSourceTypeSubtype.hasValue()) {
			// Unknown, therefore not error
			return false;
		}
		if (!isSourceTypeSubtype.get()) {
			error("Wrong argument types. Expected type: " + derivedType + ", Actual Type: " + sourceType);
			return true;
		}
		return false;
	}

	/**
	 * Returns {@link Maybe#none()} if it cannot decide it.
	 */
	private Maybe<Boolean> isSubtypeOf(Type subType, Type superType) {
		if (Object.class.equals(superType)) {
			return Maybe.some(true);
		}
		if ((subType instanceof Class) && (superType instanceof Class)) {
			Class<?> subClass = (Class<?>) subType;
			Class<?> superClass = (Class<?>) superType;
			return Maybe.some(superClass.isAssignableFrom(subClass));
		}
		if (subType instanceof Class) {
			return internalIsSubtypeOf((Class<?>) subType, superType);
		}
		if (superType instanceof Class) {
			return internalIsSubtypeOf(subType, (Class<?>) superType);
		}
		return Maybe.none();
	}

	/**
	 * Returns {@link Maybe#none()} if it cannot decide it.
	 */
	private Maybe<Boolean> internalIsSubtypeOf(Class<?> subClass, Type superType) {
		if (superType instanceof GenericArrayType) {
			if (!subClass.isArray()) {
				return Maybe.some(false);
			}
			return Maybe.none();
		}
		if (superType instanceof ParameterizedType) {
			ParameterizedType parameterizedSuperType = (ParameterizedType) superType;
			if (!(parameterizedSuperType.getRawType() instanceof Class)) {
				return Maybe.none();
			}
			Class<?> rawSuperClass = (Class<?>) parameterizedSuperType.getRawType();
			if (!rawSuperClass.isAssignableFrom(subClass)) {
				return Maybe.some(false);
			}
			// Cannot check the type parameters
			return Maybe.none();
		}
		return Maybe.none();
	}

	/**
	 * Returns {@link Maybe#none()} if it cannot decide it.
	 */
	private Maybe<Boolean> internalIsSubtypeOf(Type subType, Class<?> superClass) {
		if (subType instanceof GenericArrayType) {
			if (!superClass.isArray()) {
				return Maybe.some(false);
			}
			return Maybe.none();
		}
		if (subType instanceof ParameterizedType) {
			ParameterizedType parameterizedSubType = (ParameterizedType) subType;
			if (!(parameterizedSubType.getRawType() instanceof Class)) {
				return Maybe.none();
			}
			Class<?> rawSubClass = (Class<?>) parameterizedSubType.getRawType();
			if (!superClass.isAssignableFrom(rawSubClass)) {
				return Maybe.some(false);
			}
			// Cannot check the type parameters
			return Maybe.none();
		}
		return Maybe.none();
	}

	private void error(String message) {
		Class<?> configInterface = getDerivedProperty().getDescriptor().getConfigurationInterface();
		String property = "Property: " + configInterface.getName() + "." + getDerivedProperty().getPropertyName();
		String fullMessage = message.endsWith(".") ? message + " " + property : message + ". " + property;
		getProtocol().error(fullMessage, new ClassCastException(fullMessage));
	}

}
