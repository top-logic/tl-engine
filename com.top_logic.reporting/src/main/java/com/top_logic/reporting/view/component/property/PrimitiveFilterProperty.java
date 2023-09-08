/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.view.component.property;

import com.top_logic.tool.boundsec.BoundComponent;

/**
 * {@link FilterProperty} for primitive data types.
 * 
 * @author <a href="mailto:kbu@top-logic.com">kbu</a>
 */
public abstract class PrimitiveFilterProperty extends FilterProperty {

	/**
	 * Creates a new {@link PrimitiveFilterProperty}.
	 */
	public PrimitiveFilterProperty(String name, Object initialValue, BoundComponent aComponent) {
		super(name, initialValue, aComponent);
	}

	@Override
	protected Object getValueForPersonalConfiguration(Object fieldValue) {
		return fieldValue;
	}

	@Override
	protected Object getValueFromPersonalConfiguration(Object confValue) {
		return confValue;
	}

}
