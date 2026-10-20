/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate.util;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.layout.form.values.edit.InAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.AnnotationLookup;
import com.top_logic.model.annotate.TLValueNormalizer;

/**
 * Algorithm bringing a value entered for an attribute into its canonical form, before it is stored.
 *
 * <p>
 * A normalizer is {@link TLValueNormalizer annotated} to a model attribute. Typical normalizations
 * are trimming white space from a text, or parsing a rule text and printing it in its canonical
 * notation.
 * </p>
 *
 * <p>
 * A value the normalizer cannot canonicalize (e.g. a text that cannot be parsed) is returned
 * unchanged. A normalizer does not report invalid entries, this is the task of the attribute's
 * {@link ConstraintCheck constraints}.
 * </p>
 *
 * <p>
 * Only implementations annotated {@link InApp} are offered for selection in the model editor.
 * </p>
 *
 * @see TLValueNormalizer
 */
@Options(fun = InAppImplementations.class)
public interface ValueNormalizer {

	/**
	 * Computes the canonical form of an entered value.
	 *
	 * @param object
	 *        The edited object. In a form, this is the form's overlay of the object, so that the
	 *        other values entered in the same form can be read. <code>null</code>, if the value
	 *        belongs to an input of a view that is not bound to an attribute.
	 * @param attribute
	 *        The attribute that is annotated with this normalizer. <code>null</code>, if the value
	 *        belongs to an input of a view that is not bound to an attribute.
	 * @param value
	 *        The entered value.
	 * @return The canonical form of the given value, or the given value itself, if it cannot be
	 *         canonicalized.
	 */
	Object normalize(TLObject object, TLStructuredTypePart attribute, Object value);

	/**
	 * The normalizer {@link TLValueNormalizer annotated} in the given annotations.
	 *
	 * @param annotations
	 *        The annotations of an attribute, or of an input that is not bound to an attribute.
	 * @return A new instance of the annotated normalizer, or {@code null} if no normalizer is
	 *         annotated.
	 */
	static ValueNormalizer annotated(AnnotationLookup annotations) {
		TLValueNormalizer annotation = annotations.getAnnotation(TLValueNormalizer.class);
		if (annotation == null) {
			return null;
		}
		return TypedConfigUtil.createInstance(annotation.getNormalizer());
	}

}
