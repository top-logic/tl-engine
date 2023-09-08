/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.storage;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.element.meta.AttributeException;
import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;

/**
 * {@link ForeignKeyStorage} that is read-only.
 * 
 * <p>
 * This is a special storage which fetches the value from a reference attribute, but does not
 * support setting a value.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@InApp(false)
public class ReadOnlyForeignKeyStorage<C extends ReadOnlyForeignKeyStorage.Config<?>> extends ForeignKeyStorage<C> {

	/**
	 * Creates a new {@link ReadOnlyForeignKeyStorage}.
	 */
	public ReadOnlyForeignKeyStorage(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	public boolean isReadOnly() {
		return true;
	}

	@Override
	protected void internalSetAttributeValue(TLObject object, TLStructuredTypePart attribute, Object value)
			throws NoSuchAttributeException, IllegalArgumentException, AttributeException {
		throw new AttributeException("Setting of values is forbidden for read-only attribute '" + attribute + "'.");
	}

	@Override
	public void checkUpdate(AttributeUpdate update) {
		return;
	}

	@Override
	public Object getUpdateValue(AttributeUpdate update)
			throws NoSuchAttributeException, IllegalArgumentException, AttributeException {
		TLStructuredTypePart attribute = update.getAttribute();
		throw new AttributeException("Read-only attribute '" + attribute + "' can't be updated.");
	}

	@Override
	public void update(AttributeUpdate update)
			throws AttributeException {
		// Ignore.
	}


}

