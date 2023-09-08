/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model;

import com.top_logic.basic.Named;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.provider.MetaLabelProvider;

/**
 * {@link Named} {@link TLObject}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface TLNamed extends TLObject, Named {

	/**
	 * @see #getName()
	 */
	String NAME_ATTRIBUTE = "name";

	/**
	 * Duplicate constant for compatibility with legacy code.
	 * 
	 * @see #NAME_ATTRIBUTE
	 */
	String NAME = NAME_ATTRIBUTE;

	/**
	 * The name of this part.
	 */
	@Override
	@Name(NAME_ATTRIBUTE)
	default String getName() {
		Object name = tValueByName(NAME_ATTRIBUTE);
		if (name == null) {
			return null;
		}
		if (name instanceof String) {
			return (String) name;
		}

		return MetaLabelProvider.INSTANCE.getLabel(name);
    }

	/**
	 * Sets the {@link #getName()} property.
	 * 
	 * @param value
	 *        The new name to set.
	 */
	default void setName(String value) {
		tUpdateByName(NAME_ATTRIBUTE, value);
	}

}
