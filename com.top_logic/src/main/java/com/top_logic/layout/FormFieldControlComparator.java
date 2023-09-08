/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import java.util.Comparator;

import com.top_logic.basic.col.ComparableComparator;
import com.top_logic.layout.form.control.AbstractFormFieldControlBase;
import com.top_logic.util.FormFieldHelper;

/**
 * The FormFieldControlComparator compares {@link AbstractFormFieldControlBase}s 
 * by the values of their attached FormFields. Therefore it uses a given Comparator or 
 * the {@link ComparableComparator} as fall back.
 * 
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class FormFieldControlComparator implements Comparator<Object> {

	public static final FormFieldControlComparator INSTANCE = new FormFieldControlComparator();
	
    private Comparator<Object> comparator;
    private int descending;
    
    
    /**
     * Creates a comparator using the {@link ComparableComparator}.
     * Sort in ascending order.
     */
    public FormFieldControlComparator() {
        this(ComparableComparator.INSTANCE, false);
    }
    
    /**
     * Creates a comparator using the {@link ComparableComparator}.
     * 
     * @param descending if true, sort order will be descending
     */
    public FormFieldControlComparator(boolean descending) {
        this(ComparableComparator.INSTANCE, descending);
    }
    
    /**
     * Creates a comparator, sorting in ascending order.
     * 
     * @param aComparator the comparator to compare the values of the attached
     *                    form fields.
     */
    public FormFieldControlComparator(Comparator<Object> aComparator) {
        this(aComparator, false);
    }
    
    /**
     * Creates a comparator
     * 
     * @param aComparator the comparator to compare the values of the attached
     *                    form fields.
     * @param descending  if true, sort order will be descending
     */
    public FormFieldControlComparator(Comparator<Object> aComparator, boolean descending) {
        this.comparator = aComparator;
        this.descending = descending ? -1 : 1;
    }
    
    @Override
	public int compare(Object o1, Object o2) {
    	return this.descending * comparator.compare(FormFieldHelper.getProperValue(o1), FormFieldHelper.getProperValue(o2));
    }

}
