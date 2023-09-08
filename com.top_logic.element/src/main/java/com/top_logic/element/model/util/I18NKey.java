/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.util;

import com.top_logic.basic.util.ResKey;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLNamedPart;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.util.TLModelI18N;
import com.top_logic.model.util.TLModelNamingConvention;
import com.top_logic.model.visit.DefaultTLModelVisitor;

/**
 * Helper class to find the I18N key for a given {@link TLModelPart}.
 * 
 * @see I18NKey#getKey(TLModelPart)
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class I18NKey extends DefaultTLModelVisitor<ResKey, Void> {

	/** Singleton {@link I18NKey} instance. */
	public static final I18NKey INSTANCE = new I18NKey();

	private I18NKey() {
		// singleton instance
	}

	/**
	 * Returns the I18N key for the given {@link TLModelPart}.
	 * 
	 * @return may be <code>null</code> if no I18N could be found.
	 */
	public static ResKey getKey(TLModelPart part) {
		return part.visit(INSTANCE, none);
	}

	@Override
	protected ResKey visitStructuredTypePart(TLStructuredTypePart model, Void arg) {
		return TLModelI18N.getI18NKey(model);
	}

	@Override
	protected ResKey visitStructuredType(TLStructuredType model, Void arg) {
		return TLModelNamingConvention.getTypeLabelKey(model);
	}

	@Override
	protected ResKey visitNamedPart(TLNamedPart model, Void arg) {
		return I18NConstants.MODEL_PART.key(model.getName());
	}

}
