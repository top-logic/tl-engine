/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate.ui;

import java.awt.Color;

import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;

/**
 * {@link ValueColorProvider} taking the color of a value from the model annotations.
 *
 * <p>
 * An enumeration literal is colored by the {@link TLColor} annotation on the literal itself. An
 * object is colored by the attribute the {@link TLColorAttribute} annotation of its type names,
 * which holds either a color value or an enumeration literal that carries a {@link TLColor}. A
 * color value is its own color. Everything else has no color.
 * </p>
 *
 * @see TLColor
 * @see TLColorAttribute
 */
public class AnnotationValueColorProvider implements ValueColorProvider {

	/** Singleton {@link AnnotationValueColorProvider} instance. */
	public static final AnnotationValueColorProvider INSTANCE = new AnnotationValueColorProvider();

	/**
	 * Creates an {@link AnnotationValueColorProvider}.
	 *
	 * @see #INSTANCE
	 */
	protected AnnotationValueColorProvider() {
		super();
	}

	@Override
	public ValueColor colorOf(Object value) {
		if (value instanceof TLClassifier classifier) {
			return annotatedColor(classifier);
		}
		if (value instanceof TLObject object) {
			return attributeColor(object);
		}
		return literalColor(value);
	}

	/**
	 * The color the given object holds in the attribute its type annotates as color attribute.
	 */
	private ValueColor attributeColor(TLObject object) {
		TLStructuredType type = object.tType();
		if (type == null) {
			return null;
		}
		TLColorAttribute annotation = type.getAnnotation(TLColorAttribute.class);
		if (annotation == null) {
			return null;
		}
		TLStructuredTypePart attribute = type.getPart(annotation.getName());
		if (attribute == null) {
			return null;
		}
		Object color = object.tValue(attribute);
		if (color instanceof TLClassifier classifier) {
			return annotatedColor(classifier);
		}
		return literalColor(color);
	}

	/**
	 * The color annotated to the given enumeration literal.
	 */
	private ValueColor annotatedColor(TLClassifier classifier) {
		return ValueColor.of(classifier.getAnnotation(TLColor.class));
	}

	/**
	 * The color a value is itself, if it is a color value.
	 */
	private ValueColor literalColor(Object value) {
		return value instanceof Color color ? ValueColor.color(color) : null;
	}

}
