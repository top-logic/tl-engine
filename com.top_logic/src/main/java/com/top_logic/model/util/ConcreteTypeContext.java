/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.util;

import com.top_logic.model.TLType;
import com.top_logic.model.TLTypePart;
import com.top_logic.model.annotate.TLAnnotation;

/**
 * {@link TLTypeContext} solely defined by a concrete {@link TLType}.
 */
public class ConcreteTypeContext implements TLTypeContext {

	private TLType _type;

	/**
	 * Creates a {@link ConcreteTypeContext}.
	 */
	public ConcreteTypeContext(TLType type) {
		_type = type;
	}

	@Override
	public <T extends TLAnnotation> T getAnnotation(Class<T> annotationInterface) {
		return _type.getAnnotation(annotationInterface);
	}

	@Override
	public TLType getType() {
		return _type;
	}

	@Override
	public boolean isMultiple() {
		return false;
	}

	@Override
	public boolean isMandatory() {
		return false;
	}

	@Override
	public boolean isCompositionContext() {
		return false;
	}

	@Override
	public TLTypePart getTypePart() {
		return null;
	}

}