/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.scriptable;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.External;
import com.top_logic.basic.config.annotation.Factory;

/**
 * Configuration with methods that are no property getters.
 */
public interface PropertyRule extends ConfigurationItem {

	/**
	 * The value of a property.
	 */
	String getPlainValue();

	/**
	 * A value computed from {@link #getPlainValue()}.
	 */
	default String getComputedValue() {
		return getPlainValue();
	}

	/**
	 * A factory method.
	 */
	@Factory
	PropertyRule getCreated();

	/**
	 * An externally implemented method.
	 */
	@External
	String getExternalValue();

}
