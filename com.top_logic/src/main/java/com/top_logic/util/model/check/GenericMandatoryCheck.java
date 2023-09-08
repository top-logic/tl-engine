/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.model.check;

import java.util.Collection;

import com.top_logic.basic.col.Sink;
import com.top_logic.basic.util.ResKey;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;

/**
 * {@link InstanceCheck} checking that mandatory attributes are not filled with empty values.
 * 
 * @see TLStructuredTypePart#isMandatory()
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class GenericMandatoryCheck extends AbstractInstanceCheck {

	/** 
	 * Creates a {@link GenericMandatoryCheck}.
	 */
	public GenericMandatoryCheck(TLStructuredTypePart attribute) {
		super(attribute);
	}

	@Override
	protected void internalCheck(Sink<ResKey> problems, TLObject object) {
		Object value = getValue(object);
		if (value == null || (value instanceof Collection<?> && ((Collection<?>) value).isEmpty())
			|| (value instanceof CharSequence && ((CharSequence) value).length() == 0)) {
			problems.add(withLocation(object, I18NConstants.ERROR_EMPTY_VALUE_IN_MANDATORY_ATTRIBUTE));
		}
	}
}