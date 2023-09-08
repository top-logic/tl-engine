/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

/**
 * The class {@link AbstractConfigurationValueBinding} is an abstract superclass
 * for {@link ConfigurationValueProvider}. This class can be extended to resolve
 * potential default behavior.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractConfigurationValueBinding<T> implements ConfigurationValueBinding<T> {
	
	@Override
	public boolean isLegalValue(Object value) {
		return true;
	}
	
	@Override
	public T defaultValue() {
		return null;
	}
	
	@Override
	public Object normalize(Object value) {
		return value;
	}

}
