/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.storage;

import java.util.Objects;

import com.top_logic.basic.col.Sink;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.meta.AbstractStorageBase;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.util.model.check.AbstractInstanceCheck;
import com.top_logic.util.model.check.InstanceCheck;

/**
 * {@link InstanceCheck} checking that mandatory attributes are not filled with empty values.
 * 
 * @see TLStructuredTypePart#isMandatory()
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class GenericMandatoryCheck extends AbstractInstanceCheck {

	private final AbstractStorageBase<?> _storage;

	/** 
	 * Creates a {@link GenericMandatoryCheck}.
	 */
	public GenericMandatoryCheck(TLStructuredTypePart attribute, AbstractStorageBase<?> storage) {
		super(attribute);
		_storage = Objects.requireNonNull(storage);
	}

	@Override
	protected void internalCheck(Sink<ResKey> problems, TLObject object) {
		Object value = getValue(object);
		if (_storage.isEmpty(value)) {
			problems.add(withLocation(object, I18NConstants.ERROR_EMPTY_VALUE_IN_MANDATORY_ATTRIBUTE));
		}
	}
}