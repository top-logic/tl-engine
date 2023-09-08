/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.util;

import java.util.Comparator;

import com.top_logic.layout.form.FormMember;

/**
 * @author     <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class FieldLabelComparator implements Comparator {

    public FieldLabelComparator() {
        super();
    }

    @Override
	public int compare(Object o1, Object o2) {
        return ((FormMember) o1).getLabel().compareTo(((FormMember) o2).getLabel());
    }

    public static FieldLabelComparator getComparator() {
        return new FieldLabelComparator();
    }
}
