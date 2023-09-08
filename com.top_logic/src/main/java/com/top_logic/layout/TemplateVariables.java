/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import com.top_logic.layout.basic.TemplateVariable;
import com.top_logic.layout.template.NoSuchPropertyException;
import com.top_logic.layout.template.WithProperties;

/**
 * Access to {@link Control} properties through the {@link WithProperties} interface.
 *
 * @see TemplateVariable
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface TemplateVariables extends WithProperties {

	/**
	 * Name of the {@link Control#getID()} {@link #getPropertyValue(String) property}.
	 */
	String ID = "id";

	@Override
	default Object getPropertyValue(String propertyName) throws NoSuchPropertyException {
		switch (propertyName) {
			case ID:
				return self().getID();
		}
		return WithProperties.super.getPropertyValue(propertyName);
	}

	/**
	 * The implementing {@link Control}.
	 */
	Control self();

}
