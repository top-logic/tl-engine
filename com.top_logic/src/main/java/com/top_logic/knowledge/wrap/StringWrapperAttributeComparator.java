/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

import com.top_logic.dob.filt.DOStringAttributeComparator;
import com.top_logic.model.TLObject;
import com.top_logic.util.TLContext;

/**
 * Compare String Attributes using a correct collator.
 * 
 * @author <a href="mailto:kha@top-logic.com">kha</a>
 */
public class StringWrapperAttributeComparator implements Comparator<TLObject> {

	/** The DatatObjects are compared according to these attributes. */
	private final String[] _attributes;

	/** The DatatObjects are compared ascending / Descending according to these values */
	private final boolean[] _ascendings;

	private final Comparator<? super String> _collator;

    /** 
     * Create a Comparator using an Arry of Attribute names.
     *
     * @param ascending use ASCENDING or DESCENDING order per entry.
     */
	public StringWrapperAttributeComparator(String attrNames[], boolean ascending[], Locale locale) {
		_attributes = attrNames;
		_ascendings = ascending;
		_collator = Collator.getInstance(locale);
    }
    
    /**
     * Stable compare of given String Attributes using the current Locale.
     * 
     * @param attr1 First  Attribute to sort by.
     * @param attr2 Second Attribute to sort by.
     * @param ascending sort ascending or descending 
     */ 
	public StringWrapperAttributeComparator(String attr1, String attr2, boolean ascending) {
		this(new String[] { attr1, attr2 }, new boolean[] { ascending, ascending }, TLContext.getLocale());
    }

    /**
     * Compare the given String Attribute using the current Locale.
     * 
     * @param attrName Attribute to sort by.
     */
    public StringWrapperAttributeComparator(String attrName) {
		this(attrName, true);
    }

    /**
     * Stable compare the given String Attribute using the current Locale.
     * 
     * @param attrName Attribute to sort by.
     * @param ascending sort ascending or descending 
     */ 
	public StringWrapperAttributeComparator(String attrName, boolean ascending) {
		this(new String[] { attrName }, new boolean[] { ascending }, TLContext.getLocale());
    }

    /** Extract the (Flex-) Attributes and do the compare.
     */
    @Override
	public int compare(TLObject o1, TLObject o2) {
    	if (o1 == null) {
    		if (o2 == null) {
    			return 0;
    		}
    		
    		return -1;
    	}
    	
    	if (o2 == null) {
    		return 1;
    	}
    	
		if (!o1.tValid()) {
			return o2.tValid() ? 1 : 0;
		}
		if (!o2.tValid()) {
			return -1;
		}
		int len = _attributes.length;
		for (int i = 0; i < len; i++) {
			String attribute = _attributes[i];
			Object val1 = o1.tValueByName(attribute);
			Object val2 = o2.tValueByName(attribute);
            
			int result = DOStringAttributeComparator.compareToString(_collator, val1, val2);
			if (result != 0) {
				return _ascendings[i] ? result : -result; // done we are ....
            }
        }
		return 0;
    }

}
