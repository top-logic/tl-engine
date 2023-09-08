/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.model.types.util;

import java.util.Comparator;

import com.top_logic.knowledge.wrap.WrapperNameComparator;
import com.top_logic.model.TLObject;

/**
 * A {@link Comparator} that compares {@link TLObject}s with {@link WrapperNameComparator} and
 * {@link DemoYearGroup}s by their years.
 * <p>
 * {@link DemoYearGroup} are "less" than {@link TLObject}. <code>null</code> values are "less" than
 * none-null values.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class DemoComparator implements Comparator<Object> {

	/** Singleton {@link DemoComparator} instance. */
	public static final DemoComparator INSTANCE = new DemoComparator();

	protected DemoComparator() {
		// singleton instance
	}

	@Override
	public int compare(Object left, Object right) {
		if ((left == null) && (right == null)) {
			return 0;
		}
		if (left == null) {
			return -1;
		}
		if (right == null) {
			return 1;
		}

		if ((left instanceof DemoYearGroup) && (right instanceof DemoYearGroup)) {
			DemoYearGroup leftYearGroup = (DemoYearGroup) left;
			DemoYearGroup rightYearGroup = (DemoYearGroup) right;
			if ((leftYearGroup.getYear() == null) && (rightYearGroup.getYear() == null)) {
				return 0;
			}
			if (leftYearGroup.getYear() == null) {
				return -1;
			}
			if (rightYearGroup.getYear() == null) {
				return 1;
			}
			return leftYearGroup.getYear().compareTo(rightYearGroup.getYear());
		}
		if (left instanceof DemoYearGroup) {
			return -1;
		}
		if (right instanceof DemoYearGroup) {
			return 1;
		}

		if ((left instanceof TLObject) && (right instanceof TLObject)) {
			return WrapperNameComparator.getInstance().compare((TLObject) left, (TLObject) right);
		}
		if (left instanceof TLObject) {
			return -1;
		}
		if (right instanceof TLObject) {
			return 1;
		}
		return 0;
	}

}
