/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;


/**
 * {@link Comparable} that compares objects directly implementing
 * {@link DisplayValue}.
 * 
 * @see AbstractLocalizableComparator for building comparators that use certain
 *      {@link DisplayValue} aspects of objects to compare.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultLocalizableComparator extends AbstractLocalizableComparator {

	public DefaultLocalizableComparator(DisplayContext context) {
		super(context);
	}

	@Override
	protected final DisplayValue getLocalizable(Object object) {
		return (DisplayValue) object;
	}

}
