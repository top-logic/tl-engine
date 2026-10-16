/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.react.temperature;

import com.top_logic.layout.LabelProvider;

/**
 * {@link LabelProvider} of a {@link Temperature}: its value in its display unit, e.g.
 * <code>68.0 °F</code>.
 *
 * <p>
 * Registered for the value class in the <code>LabelProviderService</code>, so that a table cell,
 * a filter, an export and every other place showing a value by its label shows the temperature in
 * the unit of the object it was read from.
 * </p>
 */
public class TemperatureLabelProvider implements LabelProvider {

	@Override
	public String getLabel(Object object) {
		if (object == null) {
			return null;
		}
		return ((Temperature) object).format();
	}

}
