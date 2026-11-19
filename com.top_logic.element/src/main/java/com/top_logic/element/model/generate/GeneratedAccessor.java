/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.generate;

import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;

/**
 * Signature of a Java access method the {@link InterfaceGenerator} generates for a
 * {@link TLStructuredTypePart}.
 *
 * <p>
 * Types are given by their erasure as fully qualified class names (or primitive type names, or
 * <code>void</code>). A type that is generated in the same run is not loadable; its
 * {@link #getValueType() model type} allows to determine its loadable super types.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class GeneratedAccessor {

	/** The return type name of a method without result. */
	public static final String VOID = void.class.getName();

	private final TLStructuredTypePart _part;

	private final String _name;

	private final String _returnType;

	private final String _elementType;

	private final boolean _elementWildcard;

	private final TLType _valueType;

	private final List<String> _parameterTypes;

	/**
	 * Creates a {@link GeneratedAccessor}.
	 *
	 * @param part
	 *        See {@link #getPart()}.
	 * @param name
	 *        See {@link #getName()}.
	 * @param returnType
	 *        See {@link #getReturnType()}.
	 * @param elementType
	 *        See {@link #getElementType()}.
	 * @param elementWildcard
	 *        See {@link #isElementWildcard()}.
	 * @param valueType
	 *        See {@link #getValueType()}.
	 * @param parameterTypes
	 *        See {@link #getParameterTypes()}.
	 */
	public GeneratedAccessor(TLStructuredTypePart part, String name, String returnType, String elementType,
			boolean elementWildcard, TLType valueType, List<String> parameterTypes) {
		_part = part;
		_name = name;
		_returnType = returnType;
		_elementType = elementType;
		_elementWildcard = elementWildcard;
		_valueType = valueType;
		_parameterTypes = List.copyOf(parameterTypes);
	}

	/**
	 * The part this method accesses.
	 */
	public TLStructuredTypePart getPart() {
		return _part;
	}

	/**
	 * The method name.
	 */
	public String getName() {
		return _name;
	}

	/**
	 * The erasure of the return type.
	 */
	public String getReturnType() {
		return _returnType;
	}

	/**
	 * The element type of a collection return type, <code>null</code> if the method does not
	 * return a collection.
	 */
	public String getElementType() {
		return _elementType;
	}

	/**
	 * Whether the type argument of a collection return type is a wildcard
	 * <code>? extends</code>{@link #getElementType()}.
	 */
	public boolean isElementWildcard() {
		return _elementWildcard;
	}

	/**
	 * The model type of a single return value, or of the elements of a collection return type,
	 * <code>null</code> if the method returns nothing.
	 */
	public TLType getValueType() {
		return _valueType;
	}

	/**
	 * The erasures of the parameter types.
	 */
	public List<String> getParameterTypes() {
		return _parameterTypes;
	}

	@Override
	public String toString() {
		return _returnType + " " + _name + "(" + _parameterTypes.stream().collect(Collectors.joining(", ")) + ")";
	}

}
