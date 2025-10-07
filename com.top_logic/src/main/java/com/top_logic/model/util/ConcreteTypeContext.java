/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.util;

import com.top_logic.basic.tools.NameBuilder;
import com.top_logic.model.TLType;
import com.top_logic.model.TLTypePart;
import com.top_logic.model.annotate.TLAnnotation;

/**
 * {@link TLTypeContext} solely defined by a concrete {@link TLType}.
 */
public class ConcreteTypeContext implements TLTypeContext {

	private final TLType _type;

	private final boolean _mandatory;

	private final boolean _multiple;

	/**
	 * Creates a {@link ConcreteTypeContext}.
	 */
	public ConcreteTypeContext(TLType type, boolean mandatory, boolean multiple) {
		_type = type;
		_mandatory = mandatory;
		_multiple = multiple;
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
		return _multiple;
	}

	@Override
	public boolean isMandatory() {
		return _mandatory;
	}

	@Override
	public boolean isCompositionContext() {
		return false;
	}

	@Override
	public TLTypePart getTypePart() {
		return null;
	}

	@Override
	public String toString() {
		return NameBuilder.buildName(this, TLModelUtil.qualifiedName(_type));
	}

}