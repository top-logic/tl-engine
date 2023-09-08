/*
 * SPDX-FileCopyrightText: 2003 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap;

import java.util.Arrays;
import java.util.Comparator;

import com.top_logic.basic.TLID;
import com.top_logic.basic.col.ComparableComparator;
import com.top_logic.model.TLObject;

/**
 * Convenient class to use the Wrappers with DOAttributeComparators
 *
 * Note: this comparator imposes orderings that are inconsistent with equals,
 *       in case one of the passed wrappers is invalid 0 is returned.       
 * 
 * @see com.top_logic.dob.filt.DOAttributeComparator
 * @author  <a href="mailto:kha@top-logic.com">kha</a>
 */
public class WrapperComparator implements Comparator<TLObject> {

	/** Constant indicating ascending order */
	public static final boolean ASCENDING = true;

	/** Constant indicating descending order */
	public static final boolean DESCENDING = false;

	/**
	 * {@link Comparator} of {@link TLObject}s creating a stable order based on the internal object
	 * IDs.
	 */
	public static final Comparator<TLObject> ID_COMPARATOR = new Comparator<>() {

		@Override
		public int compare(TLObject o1, TLObject o2) {
			TLID id1 = o1.tHandle().getObjectName();
			TLID id2 = o2.tHandle().getObjectName();
			return id1.compareTo(id2);
		}

    };

	private String[] _attributes;

	private boolean[] _ascending;

    /**
	 * Creates a {@link WrapperComparator}.
	 */
	public WrapperComparator(String[] attrNames, boolean[] ascending) {
		_attributes = attrNames;
		_ascending = ascending;
	}

	/**
	 * Creates a {@link WrapperComparator}.
	 */
	public WrapperComparator(String... attrNames) {
		this(attrNames, new boolean[attrNames.length]);
		Arrays.fill(_ascending, true);
	}

	/** 
     * Create a Comparator using a single Attribute name.
     */
    public WrapperComparator(String attributeName) {
		this(new String[] { attributeName });
    }

    /** 
     * Create a stable Comparator using a single Attribute name.
     */
    public WrapperComparator(String attributeName, boolean ascending) {
		this(new String[] { attributeName }, new boolean[] { ascending });
    }

    /** 
     * Create a Comparator using two Attribute names.
     */
    public WrapperComparator(String att1, String att2) {
		this(new String[] { att1, att2 });
    }

    /** 
     * Create a stable Comparator using two Attribute names.
     */
    public WrapperComparator(String att1, String att2, boolean ascending) {
		this(new String[] { att1, att2 }, new boolean[] { ascending, ascending });
    }

    @Override
	public int compare(TLObject o1, TLObject o2) {
		if (!o1.tValid()) {
			return o2.tValid() ? 1 : 0;
		}
		if (!o2.tValid()) {
			return -1;
		}
		int len = _attributes.length;
		for (int i = 0; i < len; i++) {
			int result = compareAttributes(i, o1, o2, _attributes[i]);
			if (result != 0) {
				return _ascending[i] ? result : -result; // done we are ....
            }
        }
        return 0;
    }

    /** 
     * Compare given Wrappers by given Attribute.
     * 
     * @param pos allows subclasses to switch to Sub-Compares.
     */
	protected int compareAttributes(int pos, TLObject w1, TLObject w2, String attribute) {
		Object val1 = w1.tValueByName(attribute);
		Object val2 = w2.tValueByName(attribute);
        
        return compareValues(val1, val2);
    }

	/**
	 * Compares two corresponding attribute values.
	 */
	protected int compareValues(Object val1, Object val2) {
		return ComparableComparator.INSTANCE.compare(val1, val2);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + java.util.Arrays.hashCode(_ascending);
		result = prime * result + java.util.Arrays.hashCode(_attributes);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WrapperComparator other = (WrapperComparator) obj;
		if (!java.util.Arrays.equals(_ascending, other._ascending))
			return false;
		if (!java.util.Arrays.equals(_attributes, other._attributes))
			return false;
		return true;
	}

}
