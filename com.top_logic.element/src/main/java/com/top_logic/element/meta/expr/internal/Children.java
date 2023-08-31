/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.expr.internal;

import java.util.Collections;
import java.util.Set;

import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.element.meta.kbbased.filtergen.AttributeValueLocator;
import com.top_logic.element.meta.kbbased.filtergen.CustomSingleSourceValueLocator;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.model.TLObject;

/**
 * {@link AttributeValueLocator} that returns the
 * {@link StructuredElement#getChildren()} of a {@link StructuredElement}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@TagName("structure-children")
public final class Children extends CustomSingleSourceValueLocator {
	
	/**
	 * Singleton {@link Children} instance.
	 */
	public static final Children INSTANCE = new Children();

	private Children() {
		// Singleton constructor.
	}
	
	@Override
	public Object internalLocateAttributeValue(Object anObject) {
		return ((StructuredElement) anObject).getChildren();
	}

	@Override
	public Set<? extends TLObject> locateReferers(Object value) {
		return Collections.singleton(((StructuredElement) value).getParent());
	}
}