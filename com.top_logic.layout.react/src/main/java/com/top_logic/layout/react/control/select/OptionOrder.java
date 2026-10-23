/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.select;

import com.top_logic.basic.config.ExternallyNamed;

/**
 * The order the options of a select field are offered in.
 *
 * <p>
 * Whatever the {@link SelectDisplay shape} of the field, the options are offered one after the
 * other, and the order decides where an option is found. An order by label suits a long list that
 * is searched for a name the user knows. Options that have an order of their own - the days of a
 * week, the steps of a process, a list that starts with "none" - are offered in the order they are
 * given in, since sorting them by label would tear that order apart.
 * </p>
 *
 * @see ReactDropdownSelectControl
 */
public enum OptionOrder implements ExternallyNamed {

	/**
	 * Offer the options sorted alphabetically by their labels.
	 *
	 * <p>
	 * The order for a list of names without an order of their own - persons, countries, projects:
	 * the user finds an option where its name puts it.
	 * </p>
	 */
	BY_LABEL("by-label"),

	/**
	 * Offer the options in the order they are given in.
	 *
	 * <p>
	 * The order for options with an order of their own - the days of a week, the classifiers of an
	 * enumeration, the result of an expression computing the options: the options are read in the
	 * order that means something, rather than in the alphabetical order of their labels.
	 * </p>
	 */
	AS_GIVEN("as-given");

	private final String _externalName;

	private OptionOrder(String externalName) {
		_externalName = externalName;
	}

	@Override
	public String getExternalName() {
		return _externalName;
	}

}
