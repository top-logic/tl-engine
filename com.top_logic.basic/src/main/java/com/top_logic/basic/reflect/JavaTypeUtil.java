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
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
	 * Whether instances of the given implementation class can be used as values of the given
	 * (potentially parameterized) type.
	 *
	 * <p>
	 * The implementation class is viewed as the raw class of the expected type, with the type
	 * arguments it binds in its declaration, e.g. a class
	 * <code>LabelProvider implements Function&lt;Object, String&gt;</code> is viewed as
	 * <code>Function&lt;Object, String&gt;</code>. It is compatible, if this view is a subtype of
	 * the expected type by the rules of Java: type arguments are invariant, except for wildcards of
	 * the expected type, e.g. <code>Function&lt;Object, String&gt;</code> is compatible with
	 * <code>Function&lt;?, ? extends CharSequence&gt;</code> but not with
	 * <code>Function&lt;String, String&gt;</code>.
	 * </p>
	 *
	 * <p>
	 * A type argument that is not known does not restrict the compatibility:
	 * </p>
	 * <ul>
	 * <li>A type argument of the expected type that contains a type variable accepts every type
	 * argument of the implementation class.</li>
	 * <li>A type argument of the implementation class that contains one of its own type variables
	 * (of a generic implementation class) is compatible with every type argument of the expected
	 * type, since the type variable can be bound accordingly when instantiating the class.</li>
	 * <li>A raw expected type, and an implementation class implementing the raw class of the
	 * expected type, only require the implementation class to be assignable to the raw class.</li>
	 * </ul>
	 *
	 * <p>
	 * An expected wildcard or type variable is replaced by its upper bound.
	 * </p>
	 *
	 * @param implementationClass
	 *        The implementation class to check.
	 * @param expectedType
	 *        The type values must have.
	 * @return Whether the given class is assignable to the raw class of the expected type, and its
	 *         type arguments for that class are compatible with the ones of the expected type.
	 */
	public static boolean isCompatibleImplementation(Class<?> implementationClass, Type expectedType) {
		Type expected = upperBound(expectedType);
		Class<?> rawExpected = getRawClass(expected);
		if (!rawExpected.isAssignableFrom(implementationClass)) {
			return false;
		}
		if (!(expected instanceof ParameterizedType expectedParameterized)) {
			return true;
		}
		@SuppressWarnings({ "unchecked", "rawtypes" })
		Type supertype = TypeToken.of(genericClass(implementationClass)).getSupertype((Class) rawExpected).getType();
		if (!(supertype instanceof ParameterizedType supertypeParameterized)) {
			// Implements the raw class of the expected type.
			return true;
		}

		Type[] expectedArguments = expectedParameterized.getActualTypeArguments().clone();
		Type[] actualArguments = supertypeParameterized.getActualTypeArguments().clone();
		for (int n = 0; n < expectedArguments.length; n++) {
			if (containsTypeVariable(expectedArguments[n])) {
				// Unknown: Accepts every type argument.
				expectedArguments[n] = UNBOUNDED_WILDCARD;
				actualArguments[n] = eraseTypeVariables(actualArguments[n]);
			} else if (containsTypeVariable(actualArguments[n])) {
				// A type variable of the implementation class, bound when instantiating the class.
				actualArguments[n] = matchingArgument(expectedArguments[n]);
			}
		}
		return TypeToken.of(newParameterizedType(rawExpected, actualArguments))
			.isSubtypeOf(newParameterizedType(rawExpected, expectedArguments));
	}

	private static final WildcardType UNBOUNDED_WILDCARD = newWildcardType(new Type[] { Object.class }, NO_TYPES);

	/**
	 * The type argument that is contained in the given expected type argument.
	 */
	private static Type matchingArgument(Type expectedArgument) {
		if (expectedArgument instanceof WildcardType wildcard) {
			Type[] lowerBounds = wildcard.getLowerBounds();
			return lowerBounds.length > 0 ? lowerBounds[0] : wildcard.getUpperBounds()[0];
		}
		return expectedArgument;
	}

	/**
	 * The given type with its own type variables as type arguments, if it is generic.
	 */
	private static Type genericClass(Class<?> type) {
		TypeVariable<?>[] parameters = type.getTypeParameters();
		if (parameters.length == 0) {
			return type;
		}
		return newParameterizedType(type, parameters);
	}

	/**
	 * The given type, or its upper bound, if it is a wildcard or type variable.
	 */
	private static Type upperBound(Type type) {
		while (true) {
			if (type instanceof WildcardType wildcard) {
				type = wildcard.getUpperBounds()[0];
			} else if (type instanceof TypeVariable<?> variable) {
				type = variable.getBounds()[0];
			} else {
				return type;
			}
		}
	}

	/**
	 * Whether the given type contains a type variable.
	 */
	private static boolean containsTypeVariable(Type type) {
		return eraseTypeVariables(type) != type;
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
	 *         of the given type), only the most specific declaration is contained, see
	 *         {@link #isMoreSpecific(Method, Method)}.
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
				Method before = abstractMethods.get(signature);
				if (before == null || isMoreSpecific(method, before)) {
					// Note: Replacing the value keeps the position of the signature.
					abstractMethods.put(signature, method);
				}
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

	/**
	 * Whether the given method is a more specific declaration than another declaration with the
	 * same signature.
	 *
	 * <p>
	 * A declaration is more specific, if it has a more specific (covariant) return type, or if it
	 * has the same return type and is declared in a sub interface of the other declaration's
	 * interface, e.g. with a type argument bound to its return type.
	 * </p>
	 *
	 * @param method
	 *        A method declaration.
	 * @param other
	 *        Another declaration of a method with the same signature.
	 * @return Whether the given method describes the method more precisely than the other
	 *         declaration.
	 */
	public static boolean isMoreSpecific(Method method, Method other) {
		Class<?> returnType = method.getReturnType();
		Class<?> otherReturnType = other.getReturnType();
		if (returnType != otherReturnType) {
			return otherReturnType.isAssignableFrom(returnType);
		}
		Class<?> declaringClass = method.getDeclaringClass();
		Class<?> otherDeclaringClass = other.getDeclaringClass();
		return declaringClass != otherDeclaringClass && otherDeclaringClass.isAssignableFrom(declaringClass);
	}

	/**
	 * Whether the given method has the signature of a public method of {@link Object}, e.g. an
	 * abstract re-declaration of {@link Object#equals(Object)} in an interface.
	 */
	public static boolean isObjectMethod(Method method) {
		try {
			Object.class.getMethod(method.getName(), method.getParameterTypes());
			return true;
		} catch (NoSuchMethodException ex) {
			return false;
		}
	}

	/**
	 * The signature of the given method resolved in the context of the given type.
	 *
	 * <p>
	 * Two declarations of an interface method in different super interfaces of the given type have
	 * the same resolved signature, if the given type inherits them as one method, e.g. a method
	 * declared with a type variable parameter and its re-declaration with the type argument bound.
	 * </p>
	 *
	 * @param type
	 *        The type inheriting the given method.
	 * @param method
	 *        A method of the given type or one of its super types.
	 * @return The method name followed by the erasures of its parameter types resolved in the
	 *         context of the given type. Signatures are comparable with {@link Object#equals(Object)}.
	 */
	public static List<Object> getResolvedSignature(Class<?> type, Method method) {
		return resolvedSignature(TypeToken.of(type), method);
	}

	private static List<Object> resolvedSignature(TypeToken<?> context, Method method) {
		List<Object> signature = new ArrayList<>();
		signature.add(method.getName());
		for (Type parameterType : method.getGenericParameterTypes()) {
			signature.add(context.resolveType(parameterType).getRawType());
		}
		return signature;
	}

	/**
	 * The class the given type erases to.
	 *
	 * @param type
	 *        A {@link Class}, {@link ParameterizedType}, {@link GenericArrayType},
	 *        {@link TypeVariable} or {@link WildcardType}.
	 * @return The erasure of the given type: the raw type of a parameterized type, the first
	 *         (upper) bound of a type variable or wildcard.
	 */
	public static Class<?> getRawClass(Type type) {
		return TypeToken.of(type).getRawType();
	}

	/**
	 * Creates the parameterized type <code>rawType&lt;typeArguments&gt;</code>.
	 *
	 * <p>
	 * The owner type of the result is the class declaring the given raw type, like for a
	 * parameterized type obtained through reflection.
	 * </p>
	 *
	 * @param rawType
	 *        The generic class.
	 * @param typeArguments
	 *        The type arguments, one for each type parameter of the given class.
	 * @return The parameterized type.
	 * @throws IllegalArgumentException
	 *         If the number of type arguments does not match the type parameters of the given
	 *         class.
	 */
	public static ParameterizedType newParameterizedType(Class<?> rawType, Type... typeArguments) {
		int expected = rawType.getTypeParameters().length;
		if (typeArguments.length != expected) {
			throw new IllegalArgumentException("Type '" + rawType.getName() + "' expects " + expected
				+ " type arguments, got " + typeArguments.length + ".");
		}
		return new ParameterizedTypeImpl(rawType, rawType.getDeclaringClass(), typeArguments.clone());
	}

	/**
	 * Creates the wildcard type with the given bounds.
	 *
	 * @param upperBounds
	 *        The upper bounds, <code>{Object.class}</code> for an unbounded wildcard or a wildcard
	 *        with a lower bound.
	 * @param lowerBounds
	 *        The lower bounds, empty for a wildcard without lower bound.
	 * @return The wildcard type.
	 */
	public static WildcardType newWildcardType(Type[] upperBounds, Type[] lowerBounds) {
		return new WildcardTypeImpl(upperBounds.clone(), lowerBounds.clone());
	}

	/**
	 * Creates the array type with the given component type.
	 *
	 * @param componentType
	 *        The type of the elements of the array.
	 * @return The array {@link Class}, if the given component type is a {@link Class}, a
	 *         {@link GenericArrayType} otherwise.
	 */
	public static Type newArrayType(Type componentType) {
		if (componentType instanceof Class<?> componentClass) {
			return componentClass.arrayType();
		}
		return new GenericArrayTypeImpl(componentType);
	}

	/**
	 * Replaces all type variables in the given type with their erasure.
	 *
	 * <p>
	 * The result is a type that is fully described by classes, e.g. the type
	 * <code>Map&lt;K, List&lt;V&gt;&gt;</code> with unbounded type variables <code>K</code> and
	 * <code>V</code> is turned into <code>Map&lt;Object, List&lt;Object&gt;&gt;</code>.
	 * </p>
	 *
	 * @param type
	 *        The type to transform.
	 * @return The given type, if it contains no type variables, otherwise a copy of it with its type
	 *         variables replaced by their {@link #getRawClass(Type) erasure}.
	 */
	public static Type eraseTypeVariables(Type type) {
		if (type instanceof Class<?>) {
			return type;
		} else if (type instanceof TypeVariable<?> variable) {
			return getRawClass(variable);
		} else if (type instanceof ParameterizedType parameterized) {
			Type[] arguments = parameterized.getActualTypeArguments();
			Type[] erased = eraseTypeVariables(arguments);
			if (erased == arguments) {
				return type;
			}
			return newParameterizedType((Class<?>) parameterized.getRawType(), erased);
		} else if (type instanceof WildcardType wildcard) {
			Type[] upper = wildcard.getUpperBounds();
			Type[] lower = wildcard.getLowerBounds();
			Type[] erasedUpper = eraseTypeVariables(upper);
			Type[] erasedLower = eraseTypeVariables(lower);
			if (erasedUpper == upper && erasedLower == lower) {
				return type;
			}
			return newWildcardType(erasedUpper, erasedLower);
		} else if (type instanceof GenericArrayType array) {
			Type component = array.getGenericComponentType();
			Type erased = eraseTypeVariables(component);
			if (erased == component) {
				return type;
			}
			return newArrayType(erased);
		}
		throw new IllegalArgumentException("Type not expected: " + type);
	}

	private static Type[] eraseTypeVariables(Type[] types) {
		Type[] result = types;
		for (int n = 0; n < types.length; n++) {
			Type erased = eraseTypeVariables(types[n]);
			if (erased != types[n]) {
				if (result == types) {
					result = types.clone();
				}
				result[n] = erased;
			}
		}
		return result;
	}

	/**
	 * The source code notation of the given type, using binary names for classes.
	 *
	 * <p>
	 * Classes are written with their {@link Class#getName() binary name} (nested classes with
	 * <code>$</code>), arrays with <code>[]</code>, e.g.
	 * <code>java.util.Map$Entry&lt;java.lang.String, ? extends java.lang.Number&gt;[]</code>. A type
	 * variable is written as its {@link #getRawClass(Type) erasure}. The owner type of a
	 * parameterized nested class is not written.
	 * </p>
	 *
	 * @param type
	 *        The type to write.
	 * @return The notation of the given type.
	 */
	public static String toTypeName(Type type) {
		StringBuilder result = new StringBuilder();
		appendTypeName(result, type);
		return result.toString();
	}

	private static void appendTypeName(StringBuilder out, Type type) {
		if (type instanceof Class<?> cls) {
			if (cls.isArray()) {
				appendTypeName(out, cls.getComponentType());
				out.append("[]");
			} else {
				out.append(cls.getName());
			}
		} else if (type instanceof ParameterizedType parameterized) {
			appendTypeName(out, parameterized.getRawType());
			out.append('<');
			appendTypeNames(out, parameterized.getActualTypeArguments(), ", ");
			out.append('>');
		} else if (type instanceof WildcardType wildcard) {
			out.append('?');
			Type[] lower = wildcard.getLowerBounds();
			if (lower.length > 0) {
				out.append(" super ");
				appendTypeNames(out, lower, " & ");
			} else {
				Type[] upper = wildcard.getUpperBounds();
				if (upper.length > 0 && upper[0] != Object.class) {
					out.append(" extends ");
					appendTypeNames(out, upper, " & ");
				}
			}
		} else if (type instanceof GenericArrayType array) {
			appendTypeName(out, array.getGenericComponentType());
			out.append("[]");
		} else if (type instanceof TypeVariable<?> variable) {
			appendTypeName(out, getRawClass(variable));
		} else {
			throw new IllegalArgumentException("Type not expected: " + type);
		}
	}

	private static void appendTypeNames(StringBuilder out, Type[] types, String separator) {
		for (int n = 0; n < types.length; n++) {
			if (n > 0) {
				out.append(separator);
			}
			appendTypeName(out, types[n]);
		}
	}

	/**
	 * {@link ParameterizedType} created by {@link JavaTypeUtil#newParameterizedType(Class, Type...)}.
	 *
	 * <p>
	 * Equality and hash code follow the contract of the platform implementation, so that instances
	 * are equal to parameterized types obtained through reflection.
	 * </p>
	 */
	private static final class ParameterizedTypeImpl implements ParameterizedType {

		private final Class<?> _rawType;

		private final Type _ownerType;

		private final Type[] _arguments;

		ParameterizedTypeImpl(Class<?> rawType, Type ownerType, Type[] arguments) {
			_rawType = rawType;
			_ownerType = ownerType;
			_arguments = arguments;
		}

		@Override
		public Type[] getActualTypeArguments() {
			return _arguments.clone();
		}

		@Override
		public Type getRawType() {
			return _rawType;
		}

		@Override
		public Type getOwnerType() {
			return _ownerType;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof ParameterizedType other)) {
				return false;
			}
			return _rawType.equals(other.getRawType()) && Objects.equals(_ownerType, other.getOwnerType())
				&& Arrays.equals(_arguments, other.getActualTypeArguments());
		}

		@Override
		public int hashCode() {
			return Arrays.hashCode(_arguments) ^ Objects.hashCode(_ownerType) ^ _rawType.hashCode();
		}

		@Override
		public String toString() {
			return toTypeName(this);
		}
	}

	/**
	 * {@link WildcardType} created by {@link JavaTypeUtil#newWildcardType(Type[], Type[])}.
	 */
	private static final class WildcardTypeImpl implements WildcardType {

		private final Type[] _upperBounds;

		private final Type[] _lowerBounds;

		WildcardTypeImpl(Type[] upperBounds, Type[] lowerBounds) {
			_upperBounds = upperBounds;
			_lowerBounds = lowerBounds;
		}

		@Override
		public Type[] getUpperBounds() {
			return _upperBounds.clone();
		}

		@Override
		public Type[] getLowerBounds() {
			return _lowerBounds.clone();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof WildcardType other)) {
				return false;
			}
			return Arrays.equals(_upperBounds, other.getUpperBounds())
				&& Arrays.equals(_lowerBounds, other.getLowerBounds());
		}

		@Override
		public int hashCode() {
			return Arrays.hashCode(_upperBounds) ^ Arrays.hashCode(_lowerBounds);
		}

		@Override
		public String toString() {
			return toTypeName(this);
		}
	}

	/**
	 * {@link GenericArrayType} created by {@link JavaTypeUtil#newArrayType(Type)}.
	 */
	private static final class GenericArrayTypeImpl implements GenericArrayType {

		private final Type _componentType;

		GenericArrayTypeImpl(Type componentType) {
			_componentType = componentType;
		}

		@Override
		public Type getGenericComponentType() {
			return _componentType;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this) {
				return true;
			}
			return obj instanceof GenericArrayType other && _componentType.equals(other.getGenericComponentType());
		}

		@Override
		public int hashCode() {
			return _componentType.hashCode();
		}

		@Override
		public String toString() {
			return toTypeName(this);
		}
	}

}
