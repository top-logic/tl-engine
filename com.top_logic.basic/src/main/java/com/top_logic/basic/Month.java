/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

import com.top_logic.basic.config.CommaSeparatedEnum;
import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.basic.config.ExternallyNamed;

/**
 * Representation of the months in a year.
 * 
 * @since 5.7.3
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public enum Month implements ExternallyNamed {

	/** Representation of January */
	JANUARY("January"),

	/** Representation of February */
	FEBRUARY("February"),

	/** Representation of March */
	MARCH("March"),

	/** Representation of April */
	APRIL("April"),

	/** Representation of May */
	MAY("May"),

	/** Representation of June */
	JUNE("June"),

	/** Representation of July */
	JULY("July"),

	/** Representation of August */
	AUGUST("August"),

	/** Representation of September */
	SEPTEMBER("September"),

	/** Representation of October */
	OCTOBER("October"),

	/** Representation of November */
	NOVEMBER("November"),

	/** Representation of December */
	DECEMBER("December"),

	/**
	 * Value of the month indicating the thirteenth month of the year. Although
	 * <code>GregorianCalendar</code> does not use this value, lunar calendars
	 * do.
	 */
	UNDECIMBER("Undecimber"),

	;

	/**
	 * {@link ConfigurationValueProvider} to process comma separated lists of {@link Month}s.
	 * 
	 * @since 5.7.3
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class CommaSeparatedMonths extends CommaSeparatedEnum<Month> {

		/** Sole {@link CommaSeparatedEnum} instance. */
		public static final CommaSeparatedMonths INSTANCE = new CommaSeparatedMonths();

		private CommaSeparatedMonths() {
			super(Month.class);
		}
	}

	private final String _externalName;

	private Month(String externalName) {
		this._externalName = externalName;
	}

	@Override
	public String getExternalName() {
		return _externalName;
	}

	@Override
	public String toString() {
		return getExternalName();
	}

}
