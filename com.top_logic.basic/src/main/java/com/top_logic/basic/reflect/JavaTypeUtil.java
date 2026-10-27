/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.reflect;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.reflect.TypeToken;

import com.top_logic.basic.UnreachableAssertion;

/**
 * Utility for finding type bounds of generic super types and for resolving generic types in the
 * context of a concrete parameterization.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class JavaTypeUtil {

	private static final Type[] NO_TYPES = {};

	/**
	 * Resolves the upper bound of a type variable in a super type of a concrete implementation
	 * type.
	 *
	 * @param type
	 *        The concrete implementation type.
	 * @param base
	 *        Some super type of the given type.
	 * @param typeParameterIndex
	 *        Index of the type parameter of the given base type to resolve an upper bound for.
	 * @return The upper bound of the specified type variable in the given base type of the given
	 *         concrete implementation type.
	 */
	public static Class<?> getTypeBound(Class<?> type, Class<?> base, int typeParameterIndex) {
		return getTypeBound(type, base, typeParameterIndex, NO_TYPES, new HashMap<>());
	}

	private static Class<?> getTypeBound(Class<?> type, Class<?> base, int typeParameterIndex, Type[] typeArguments,
			Map<TypeVariable<?>, Type> binding) {
		if (type == base) {
			if (typeArguments.length > typeParameterIndex) {
				return asClass(binding, typeArguments[typeParameterIndex]);
			} else {
				return asClass(binding, base.getTypeParameters()[typeParameterIndex].getBounds()[0]);
			}
		}

		TypeVariable<?>[] typeParameters = type.getTypeParameters();

		addBinding(binding, typeParameters, typeArguments);

		Type superType = type.getGenericSuperclass();
		if (superType != null) {
			Class<?> superResult = descend(superType, base, typeParameterIndex, binding);
			if (superResult != null) {
				return superResult;
			}
		}


		for (Type intf : type.getGenericInterfaces()) {
			Class<?> intfResult = descend(intf, base, typeParameterIndex, binding);
			if (intfResult != null) {
				return intfResult;
			}
		}

		removeBinding(binding, typeParameters);

		// Not found.
		return null;
	}

	private static void removeBinding(Map<TypeVariable<?>, Type> binding, TypeVariable<?>[] typeParameters) {
		for (TypeVariable<?> var : typeParameters) {
			binding.remove(var);
		}
	}

	private static void addBinding(Map<TypeVariable<?>, Type> binding, TypeVariable<?>[] typeParameters,
			Type[] typeArguments) {
		for (int n = 0, cnt = Math.min(typeParameters.length, typeArguments.length); n < cnt; n++) {
			binding.put(typeParameters[n], typeArguments[n]);
		}
	}

	private static Class<?> descend(Type superType, Class<?> base, int typeParameterIndex,
			Map<TypeVariable<?>, Type> binding) {
		if (superType instanceof Class<?>) {
			return getTypeBound((Class<?>) superType, base, typeParameterIndex);
		} else {
			ParameterizedType parametrizedType = (ParameterizedType) superType;
			Type[] actualArguments = parametrizedType.getActualTypeArguments();

			return getTypeBound(asClass(binding, parametrizedType), base, typeParameterIndex, actualArguments, binding);
		}
	}

	private static Class<?> asClass(Map<TypeVariable<?>, Type> binding, Type type) {
		if (type instanceof Class<?>) {
			return (Class<?>) type;
		} else if (type instanceof ParameterizedType) {
			return asClass(binding, ((ParameterizedType) type).getRawType());
		} else if (type instanceof TypeVariable<?>) {
			TypeVariable<?> var = (TypeVariable<?>) type;
			Type bound = binding.get(var);
			if (bound != null) {
				return asClass(binding, bound);
			} else {
				return asClass(binding, var.getBounds()[0]);
			}
		} else if (type instanceof WildcardType) {
			return asClass(binding, ((WildcardType) type).getUpperBounds()[0]);
		} else if (type instanceof GenericArrayType) {
			Class<?> componentClass = asClass(binding, ((GenericArrayType) type).getGenericComponentType());
			return toArray(componentClass);
		}
		throw new UnreachableAssertion("Type not expected: " + type);
	}

	private static Class<?> toArray(Class<?> componentClass) {
		StringBuilder name = new StringBuilder();
		name.append('[');
		while (componentClass.isArray()) {
			name.append('[');
			componentClass = componentClass.getComponentType();
		}
		name.append('L');
		name.append(componentClass.getName());
		name.append(';');
		try {
			return Class.forName(name.toString());
		} catch (ClassNotFoundException ex) {
			throw new UnreachableAssertion("Component class already loaded.", ex);
		}
	}


	/**
	 * Resolves the given type in the context of a (potentially parameterized) type.
	 *
	 * <p>
	 * Type variables of the context type and of all its super types are replaced by the arguments
	 * they are bound to in the context. Parameterized types stay parameterized, e.g. resolving the
	 * return type of {@link java.util.function.Function#apply(Object)} in the context
	 * <code>UnaryOperator&lt;List&lt;String&gt;&gt;</code> results in
	 * <code>List&lt;String&gt;</code>. A type variable that is not bound in the context is kept.
	 * </p>
	 *
	 * @param context
	 *        The type providing the bindings of type variables, e.g. the
	 *        {@link java.lang.reflect.Method#getGenericReturnType() generic return type} of a
	 *        property getter.
	 * @param type
	 *        The type to resolve, e.g. a type variable of a super type of the context, or the
	 *        generic parameter or return type of a method declared in a super type of the context.
	 * @return The given type with all type variables bound in the context replaced.
	 */
	public static Type resolve(Type context, Type type) {
		return TypeToken.of(context).resolveType(type).getType();
	}

	/**
	 * The abstract methods of the given interface, including the ones inherited from its super
	 * interfaces.
	 *
	 * <p>
	 * Default and static methods are not abstract. Abstract re-declarations of the public methods of
	 * {@link Object} (such as {@link Object#equals(Object)}) are not counted, because every
	 * implementation inherits them. A method that is declared in several super interfaces, or that
	 * is re-declared with a type argument bound in a sub interface, is reported once. An interface
	 * with exactly one abstract method is a functional interface.
	 * </p>
	 *
	 * @param type
	 *        The interface to analyze.
	 * @return The abstract methods of the given type in declaration order of
	 *         {@link Class#getMethods()}. Of methods with the same signature (resolved in the context
	 *         of the given type), only the first one is contained.
	 */
	public static List<Method> getAbstractMethods(Class<?> type) {
		Map<List<Object>, Method> abstractMethods = new LinkedHashMap<>();
		Map<List<Object>, Boolean> implemented = new HashMap<>();
		TypeToken<?> context = TypeToken.of(type);
		for (Method method : type.getMethods()) {
			if (method.isSynthetic() || method.isBridge() || Modifier.isStatic(method.getModifiers())) {
				continue;
			}
			if (isObjectMethod(method)) {
				continue;
			}
			List<Object> signature = resolvedSignature(context, method);
			if (Modifier.isAbstract(method.getModifiers())) {
				abstractMethods.putIfAbsent(signature, method);
			} else {
				implemented.put(signature, Boolean.TRUE);
			}
		}
		List<Method> result = new ArrayList<>();
		for (Map.Entry<List<Object>, Method> entry : abstractMethods.entrySet()) {
			if (!implemented.containsKey(entry.getKey())) {
				result.add(entry.getValue());
			}
		}
		return result;
	}

	private static boolean isObjectMethod(Method method) {
		try {
			Object.class.getMethod(method.getName(), method.getParameterTypes());
			return true;
		} catch (NoSuchMethodException ex) {
			return false;
		}
	}

	private static List<Object> resolvedSignature(TypeToken<?> context, Method method) {
		List<Object> signature = new ArrayList<>();
		signature.add(method.getName());
		for (Type parameterType : method.getGenericParameterTypes()) {
			signature.add(context.resolveType(parameterType).getRawType());
		}
		return signature;
	}

}
