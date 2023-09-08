/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.generate;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utility for printing Java generic reflection types.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TypeUtil {

	/**
	 * The Java source representation of the given type.
	 */
	public static String getTypeName(Type type) {
		if (type instanceof Class<?>) {
			Class<?> classType = (Class<?>) type;
			if (classType.isArray()) {
				return getTypeName(classType.getComponentType()) + "[]";
			} else {
				return classType.getSimpleName();
			}
		} else if (type instanceof GenericArrayType) {
			GenericArrayType arrayType = (GenericArrayType) type;
			return getTypeName(arrayType.getGenericComponentType()) + "[]";
		} else if (type instanceof ParameterizedType) {
			ParameterizedType parameterizedType = (ParameterizedType) type;
			return getTypeOwnerPrefix(parameterizedType.getOwnerType()) + getTypeName(parameterizedType.getRawType()) + getTypeArgumentsExpr(parameterizedType.getActualTypeArguments());
		} else if (type instanceof TypeVariable<?>) {
			TypeVariable<?> typeVar = (TypeVariable<?>) type;
			return typeVar.getName() + getUpperBoundsExpr(typeVar.getBounds());
		} else if (type instanceof WildcardType) {
			WildcardType wildcard = (WildcardType) type;
			return "?" + getLowerBoundsExpr(wildcard.getLowerBounds()) + getUpperBoundsExpr(wildcard.getUpperBounds());
		} else {
			throw new UnsupportedOperationException("Unknown type kind: " + type);
		}
	}

	private static CharSequence getUpperBoundsExpr(Type[] bounds) {
		return getBoundsExpr(" extends ", bounds);
	}

	private static CharSequence getLowerBoundsExpr(Type[] bounds) {
		return getBoundsExpr(" super ", bounds);
	}
	
	private static CharSequence getBoundsExpr(String keyword, Type[] bounds) {
		StringBuilder result = new StringBuilder();
		for (Type bound : bounds) {
			if (bound == Object.class) {
				continue;
			}
			
			if (result.length() == 0) {
				result.append(keyword);
			} else {
				result.append(" & ");
			}
			
			result.append(getTypeName(bound));
		}
		return result;
	}

	private static CharSequence getTypeArgumentsExpr(Type[] typeArguments) {
		if (typeArguments.length == 0) {
			return "";
		}
		
		StringBuilder result = new StringBuilder();
		result.append('<');
		for (int n = 0, cnt = typeArguments.length; n < cnt; n++) {
			if (n > 0) {
				result.append(", ");
			}
			result.append(getTypeName(typeArguments[n]));
		}
		result.append('>');
		return result;
	}

	private static String getTypeOwnerPrefix(Type ownerType) {
		if (ownerType == null) {
			return "";
		} else {
			return getTypeName(ownerType) + ".";
		}
	}

	/**
	 * Find a binding of a type parameter in a given class.
	 * 
	 * @param startClass
	 *        The class in which to resolve a binding for a type parameter.
	 * @param parameterizedClass
	 *        The class that declares the type parameter. Must be a subclass of the start class.
	 * @param parameterIndex
	 *        The index of the type parameter to resolve.
	 * @return An upper bound to the type parameter.
	 */
	public static Class<?> findTypeBound(Class<?> startClass, Class<?> parameterizedClass, int parameterIndex) {
		List<Class<?>> inheritanceChain = new ArrayList<>();
		
		Class<?> anchestor = startClass;
		while (true) {
			if (anchestor == parameterizedClass) {
				break;
			}
			
			inheritanceChain.add(anchestor);
	
			anchestor = anchestor.getSuperclass();
			if (anchestor == Object.class) {
				throw new IllegalArgumentException("Class '" + parameterizedClass.getName()
					+ "' is not an anchestor class of '" + startClass.getName() + "'.");
			}
		}
		
		Class<?> resolveTarget = parameterizedClass;
		int resolveParameterIndex = parameterIndex;
		for (int n = inheritanceChain.size() - 1; n >= 0; n--) {
			// subClass is a direct extension of resolveTarget. Therefore, resolveTarget is the
			// super class of subClass.
			Class<?> subClass = inheritanceChain.get(n);
	
			Type superType = subClass.getGenericSuperclass();
			if (superType instanceof ParameterizedType) {
				Type binding = ((ParameterizedType) superType).getActualTypeArguments()[resolveParameterIndex];
				if (binding instanceof TypeVariable<?>) {
					resolveTarget = subClass;
					resolveParameterIndex = Arrays.asList(subClass.getTypeParameters()).indexOf(binding);
				} else if (binding instanceof ParameterizedType) {
					return getRawType(binding);
				} else {
					return (Class<?>) binding;
				}
			} else {
				break;
			}
		}
		TypeVariable<?> resolveParameter = resolveTarget.getTypeParameters()[resolveParameterIndex];
		Type firstBound = resolveParameter.getBounds()[0];
		return getRawType(firstBound);
	}

	private static Class<?> getRawType(Type type) {
		if (type instanceof TypeVariable<?>) {
			return getRawType(((TypeVariable<?>) type).getBounds()[0]);
		} else if (type instanceof ParameterizedType) {
			return getRawType(((ParameterizedType) type).getRawType());
		} else {
			return (Class<?>) type;
		}
	}


}
