/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.model.annotations;

import java.util.Calendar;
import java.util.Date;

import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.layout.form.control.CalendarMarker;
import com.top_logic.layout.form.control.DefaultCalendarMarker;
import com.top_logic.layout.form.model.ComplexField;
import com.top_logic.model.TLObject;
import com.top_logic.model.annotate.ui.MarkerFactory;

/**
 * Demo implementation of the interface {@link MarkerFactory}
 * <p>
 * Creates the {@link MarkerFactory} implementation.
 * </p>
 *
 * @author <a href="mailto:Diana.Pankratz@top-logic.com">Diana Pankratz</a>
 */
public class DemoMarkerFactory implements MarkerFactory {

	/** CSS class for marking dates green. */
	public static final String MARKED_GREEN = "markedGreen";

	/** CSS class for marking dates red. */
	public static final String MARKED_RED = "markedRed";

	/**
	 * Initializes a {@link CalendarMarker} based on a time range.
	 * <p>
	 * Start and end of the range are defined by two date fields in the parent object.
	 * </p>
	 * @param object
	 *        Object containing the the date attribute.
	 * @param field
	 *        The date field.
	 * 
	 * @return The implementation of the {@link CalendarMarker}.
	 * 
	 * @see MarkerFactory#getCalendarMarker(TLObject, ComplexField)
	 */
	@Override
	public CalendarMarker getCalendarMarker(TLObject object, ComplexField field) {
		if (object == null) {
			return new DefaultCalendarMarker(null, null, false, "", "", false);
		}
		StructuredElement child = (StructuredElement) object;
		StructuredElement parent = child.getParent();

		Calendar lowerBoundCalendar = null;
		Date lowerBoundDate = (Date) parent.getValue("date");
		if(lowerBoundDate != null){
			lowerBoundCalendar = CalendarUtil.createCalendar(lowerBoundDate);
		}
		
		Calendar upperBoundCalendar = null;
		Date upperBoundDate = (Date) parent.getValue("date2");
		if (upperBoundDate != null) {
			upperBoundCalendar = CalendarUtil.createCalendar(upperBoundDate);
		}

		return new DefaultCalendarMarker(lowerBoundCalendar, upperBoundCalendar, false, MARKED_GREEN, MARKED_RED, false);
	}

}