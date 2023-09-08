/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.util;

import com.top_logic.model.TLType;
import com.top_logic.model.TLTypePart;
import com.top_logic.model.annotate.AnnotationLookup;
import com.top_logic.model.annotate.TLAnnotation;

/**
 * {@link TLTypeContext} with custom annotations.
 */
public class AnnotatedTypeContext implements TLTypeContext {

	private TLTypeContext _lead;

	private AnnotationLookup _annotations;

	/**
	 * Creates a {@link AnnotatedTypeContext}.
	 */
	public AnnotatedTypeContext(TLTypeContext lead, AnnotationLookup annotations) {
		_lead = lead;
		_annotations = annotations;
	}

	@Override
	public <T extends TLAnnotation> T getAnnotation(Class<T> annotationType) {
		T custom = _annotations.getAnnotation(annotationType);
		if (custom != null) {
			return custom;
		}

		return _lead.getAnnotation(annotationType);
	}

	@Override
	public TLType getType() {
		return _lead.getType();
	}

	@Override
	public boolean isMultiple() {
		return _lead.isMultiple();
	}

	@Override
	public boolean isMandatory() {
		return _lead.isMandatory();
	}

	@Override
	public boolean isCompositionContext() {
		return _lead.isCompositionContext();
	}

	@Override
	public TLTypePart getTypePart() {
		return _lead.getTypePart();
	}

}
