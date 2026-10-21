/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate.util;

import java.util.List;

import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.TLValueNormalizer;

/**
 * {@link ValueNormalizer} applying a sequence of normalizers, each one to the result of the one
 * before.
 *
 * <p>
 * This is the normalizer of an attribute whose {@link TLValueNormalizer} annotation lists more
 * than one normalizer.
 * </p>
 *
 * @see ValueNormalizer#annotated(com.top_logic.model.annotate.AnnotationLookup)
 */
public class ValueNormalizerChain implements ValueNormalizer {

	private final List<ValueNormalizer> _normalizers;

	/**
	 * Creates a {@link ValueNormalizerChain}.
	 *
	 * @param normalizers
	 *        The normalizers to apply, in the order they are applied.
	 */
	public ValueNormalizerChain(List<? extends ValueNormalizer> normalizers) {
		_normalizers = List.copyOf(normalizers);
	}

	@Override
	public Object normalize(TLObject object, TLStructuredTypePart attribute, Object value) {
		Object result = value;
		for (ValueNormalizer normalizer : _normalizers) {
			result = normalizer.normalize(object, attribute, result);
		}
		return result;
	}

}
