/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate.ui;

import com.top_logic.layout.form.control.CalendarMarker;
import com.top_logic.layout.form.model.ComplexField;
import com.top_logic.model.TLObject;

/**
 * A {@link MarkerFactory} configures and returns a {@link CalendarMarker}.
 * 
 * @author <a href="mailto:Diana.Pankratz@top-logic.com">Diana Pankratz</a>
 * 
 * @see CalendarMarker
 */
public interface MarkerFactory {

	/**
	 * Defines the {@link CalendarMarker}.
	 * @param object
	 *        The object containing the date attribute.
	 * @param field
	 *        The date field.
	 * 
	 * @return The configured {@link CalendarMarker}.
	 */
	CalendarMarker getCalendarMarker(TLObject object, ComplexField field);
}