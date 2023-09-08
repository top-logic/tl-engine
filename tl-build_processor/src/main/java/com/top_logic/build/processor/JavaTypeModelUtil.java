/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.build.processor;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.UnionType;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * Utility for finding type bounds of generic super types.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class JavaTypeModelUtil {

	private static final List<TypeMirror> NO_TYPES = Collections.emptyList();

	private final Elements _elementUtils;
	private final Types _typeUtils;

	/**
	 * Creates a {@link JavaTypeModelUtil}.
	 */
	public JavaTypeModelUtil(Types typeUtils, Elements elementUtils) {
		_elementUtils = elementUtils;
		_typeUtils = typeUtils;
	}

	/**
	 * Resolves the upper bound of a type variable in a super type of a concrete implementation
	 * type.
	 *
	 * @param type
	 *        The concrete implementation type.
	 * @param base
	 *        Super type of the given type for which to resolve the binding of a type variable in
	 *        the context of the given type.
	 * @param typeParameterIndex
	 *        Index of the type variable of the base type to resolve an upper bound for.
	 * @return The upper bound of the specified type variable in the given base type of the given
	 *         concrete implementation type.
	 */
	public TypeMirror getTypeBound(TypeElement type, TypeElement base, int typeParameterIndex) {
		return getTypeBound(type, base, typeParameterIndex, NO_TYPES, new HashMap<>());
	}

	private TypeMirror getTypeBound(TypeElement type, TypeElement base, int typeParameterIndex,
			List<? extends TypeMirror> typeArguments, Map<TypeVariable, TypeMirror> binding) {
		if (_typeUtils.isSameType(type.asType(), base.asType())) {
			if (typeParameterIndex < typeArguments.size()) {
				return resolve(binding, typeArguments.get(typeParameterIndex));
			} else {
				return resolve(binding, base.getTypeParameters().get(typeParameterIndex).getBounds().get(0));
			}
		}
		if (!_typeUtils.isSubtype(raw(type.asType()), raw(base.asType()))) {
			// No chance to find the base type in this part of the tree.
			return null;
		}

		List<? extends TypeParameterElement> typeParameters = type.getTypeParameters();

		addBinding(binding, typeParameters, typeArguments);

		TypeMirror superType = type.getSuperclass();
		if (superType != null && superType.getKind() == TypeKind.DECLARED) {
			TypeMirror superResult = descend(superType, base, typeParameterIndex, binding);
			if (superResult != null) {
				return superResult;
			}
		}


		for (TypeMirror intf : type.getInterfaces()) {
			TypeMirror intfResult = descend(intf, base, typeParameterIndex, binding);
			if (intfResult != null) {
				return intfResult;
			}
		}

		removeBinding(binding, typeParameters);

		// Not found.
		return null;
	}

	private TypeMirror raw(TypeMirror type) {
		return _typeUtils.erasure(type);
	}

	private TypeMirror descend(TypeMirror superType, TypeElement base, int typeParameterIndex,
			Map<TypeVariable, TypeMirror> binding) {
		List<? extends TypeMirror> typeArguments = ((DeclaredType) superType).getTypeArguments();
		TypeElement superTypeElement = (TypeElement) _typeUtils.asElement(superType);
		return getTypeBound(superTypeElement, base, typeParameterIndex, typeArguments, binding);
	}

	private void removeBinding(Map<TypeVariable, TypeMirror> binding,
			List<? extends TypeParameterElement> typeParameters) {
		for (TypeParameterElement var : typeParameters) {
			binding.remove(var.asType());
		}
	}

	private void addBinding(Map<TypeVariable, TypeMirror> binding,
			List<? extends TypeParameterElement> typeParameters,
			List<? extends TypeMirror> typeArguments) {
		for (int n = 0, cnt = Math.min(typeParameters.size(), typeArguments.size()); n < cnt; n++) {
			TypeParameterElement typeVar = typeParameters.get(n);
			TypeMirror typeArg = n < typeArguments.size() ? typeArguments.get(n) : firstBound(typeVar);
			binding.put((TypeVariable) typeVar.asType(), typeArg);
		}
	}

	private TypeMirror firstBound(TypeParameterElement typeVar) {
		List<? extends TypeMirror> bounds = typeVar.getBounds();
		return bounds.size() > 0 ? bounds.get(0) : _elementUtils.getTypeElement("java.lang.Object").asType();
	}

	private TypeMirror resolve(Map<TypeVariable, TypeMirror> binding, TypeMirror type) {
		switch (type.getKind()) {
			case TYPEVAR:
				TypeVariable var = (TypeVariable) type;
				TypeMirror bound = binding.get(var);
				if (bound != null) {
					return resolve(binding, bound);
				} else {
					return resolve(binding, var.getUpperBound());
				}
			case WILDCARD:
				return resolve(binding, ((WildcardType) type).getExtendsBound());
			case UNION:
				return ((UnionType) type).getAlternatives().get(0);
			case INTERSECTION:
				return ((IntersectionType) type).getBounds().get(0);
			default:
				return type;
		}
	}

}
