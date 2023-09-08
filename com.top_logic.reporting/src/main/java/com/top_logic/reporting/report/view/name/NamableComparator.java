/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.view.name;

import java.util.Comparator;

/**
 * A {@link NamableComparator} compares {@link Namable}s. This comparator is
 * <code>null</code> safe.
 *
 * @author    <a href=mailto:tdi@top-logic.com>tdi</a>
 */
public class NamableComparator implements Comparator {

	/** Please use this static instance. */
	public static final Comparator INSTANCE = new NamableComparator();

	@Override
	public int compare(Object o1, Object o2) {
		String name1 = ((Namable) o1).getName();
		String name2 = ((Namable) o2).getName();

		if (name1 != null && name2 != null) {
			return name1.compareTo(name2);
		} else if (name1 == null && name2 != null) {
			return -1;
		} else if (name1 != null && name2 == null) {
			return 1;
		} else {
			return 0;
		}
	}

}
