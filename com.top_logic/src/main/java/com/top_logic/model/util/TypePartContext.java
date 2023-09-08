/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.util;

import com.top_logic.model.ModelKind;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.TLTypePart;
import com.top_logic.model.annotate.TLAnnotation;

/**
 * {@link TLTypeContext} defined by an {@link TLTypePart attribute}.
 */
public class TypePartContext implements TLTypeContext {

	private TLStructuredTypePart _part;

	/**
	 * Creates a {@link TypePartContext}.
	 */
	public TypePartContext(TLStructuredTypePart part) {
		_part = part;
	}

	@Override
	public <T extends TLAnnotation> T getAnnotation(Class<T> annotationType) {
		T customResult = _part.getAnnotation(annotationType);
		if (customResult != null) {
			return customResult;
		}

		return getType().getAnnotation(annotationType);
	}

	@Override
	public TLType getType() {
		return _part.getType();
	}

	@Override
	public boolean isMultiple() {
		return _part.isMultiple();
	}

	@Override
	public boolean isMandatory() {
		return _part.isMandatory();
	}

	@Override
	public boolean isCompositionContext() {
		return _part.getModelKind() == ModelKind.REFERENCE && ((TLReference) _part).getEnd().isComposite();
	}

	@Override
	public TLTypePart getTypePart() {
		return _part;
	}

}
