/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.util.Calendar;

/**
 * A {@link CalendarMarker} defines CSS-Classes for specific fields in a {@link CalendarControl}
 * date picker.
 * @author <a href="mailto:Diana.Pankratz@top-logic.com">Diana Pankratz</a>
 */
@FunctionalInterface
public interface CalendarMarker {

	/**
	 * The specific CSS-Class for disabled {@link CalendarControl} fields.
	 */
	String DISABLED = "disabled";

	/**
	 * Gets the necessary CSS-Classes for a {@link CalendarControl} field to mark the currently set
	 * date.
	 * <p>
	 * The CSS-Class {@value CalendarControl#DISABLED_MARKER} not only defines a CSS-Style but also
	 * disables the date in the {@link CalendarControl}. In this way its not possible to pick this
	 * date in the date picker.
	 * </p>
	 * 
	 * @param calStart
	 *        The start time of the {@link CalendarControl} date that should be marked. Is never
	 *        null.
	 * @param calEnd
	 *        The end time of the {@link CalendarControl} date that should be marked. Is never null.
	 * @return String of all CSS-Classes separated by space. Null or the empty string mean: No
	 *         special CSS-Class.
	 */
	String getCss(Calendar calStart, Calendar calEnd);

}
