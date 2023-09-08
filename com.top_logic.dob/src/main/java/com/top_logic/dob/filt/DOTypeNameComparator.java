/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.filt;

import java.util.Comparator;

import com.top_logic.dob.DataObject;

/**
 * Compare DataObjects by (MetaObject) Type Name.
 * 
 * @author <a href="mailto:kha@top-logic.com">kha </a>
 */
public class DOTypeNameComparator implements Comparator<DataObject> {

	public static final DOTypeNameComparator SINGLETON = new DOTypeNameComparator();

    /**
     * Private to enforce usage of {@link #SINGLETON}.
     */
    private DOTypeNameComparator() {
        // enforce usage of SINGLETON
    }
    
	/**
	 * Compare to DataObjects by Type Name.
	 */
	@Override
	public int compare(DataObject o1, DataObject o2) {
		String name1 = o1.tTable().getName();
		String name2 = o2.tTable().getName();
		return name1.compareTo(name2);
	}

}

