/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.model.types.util;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.demo.model.types.DemoTypesA;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.form.overlay.TLFormObject;
import com.top_logic.element.meta.kbbased.filtergen.AbstractAttributedValueFilter;
import com.top_logic.element.meta.kbbased.filtergen.AttributedValueFilter;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.model.TLStructuredTypePart;

/**
 * {@link AttributedValueFilter} that checks, whether the new value is a child
 * of the value of another attribute.
 * 
 * <p>
 * This is a demo of a filter that implements a dependency on another attribute
 * of the same object.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Deprecated
public class ChildOfDependency1Filter extends AbstractAttributedValueFilter {

	@Override
	public boolean accept(Object newValue, EditContext editContext) {
		if (newValue == null) {
			return true;
		}
		if (!(newValue instanceof StructuredElement)) {
			return false;
		}
		
		TLFormObject overlay = editContext.getOverlay();
		TLStructuredTypePart otherAttribute = editContext.getType().getPartOrFail(DemoTypesA.DEPENDENCY1_ATTR);
		Object expectedParent = CollectionUtil.getSingleValueFrom(overlay.tValue(otherAttribute));
		if (expectedParent == null) {
			return false;
		}
		
		StructuredElement node = (StructuredElement) newValue;
		while (node != null) {
			if (node == expectedParent) {
				return true;
			}
			node = node.getParent();
		}
		return false;
	}

}
