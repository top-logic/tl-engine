/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap;

import java.text.Collator;
import java.util.Comparator;

import com.top_logic.basic.Logger;
import com.top_logic.basic.col.NullSafeComparator;
import com.top_logic.dob.filt.DOStringAttributeComparator;
import com.top_logic.knowledge.wrap.exceptions.WrapperRuntimeException;
import com.top_logic.model.TLNamed;
import com.top_logic.model.TLObject;
import com.top_logic.util.TLContext;

/**
 * Compares {@link TLNamed} instances using their {@link TLNamed#getName()}.
 * 
 * Useful because when wrappers not (exclusively)
 * use their name attribute for getName().
 * 
 * @author    <a href="mailto:kbu@top-logic.com">Karsten Buch</a>
 */
public class WrapperNameComparator implements Comparator<TLObject> {

	private final boolean _ascending;

	private final Comparator<? super String> _collator;

	/**
	 * Create a WrapperNameComparator using {@link TLContext#getLocale()}
	 * 
	 * @param ascending
	 *        true if sorting should be ascending
	 * 
	 * @see #getInstance()
	 */
	public WrapperNameComparator(boolean ascending) {
		_ascending = ascending;
		_collator = Collator.getInstance(TLContext.getLocale());
	}

	/**
	 * Create a WrapperNameComparator using {@link TLContext#getLocale()}
	 */
	public WrapperNameComparator() {
		this (true);
	}
    
    /** Extract the Names and do the compare.
     */
    @Override
	public int compare(TLObject o1, TLObject o2) {
		if (!o1.tValid()) {
			return o2.tValid() ? 1 : 0;
        }
		if (!o2.tValid()) {
            return -1;
        }
        try {
			String name1 = o1 instanceof TLNamed ? ((TLNamed) o1).getName() : null;
			String name2 = o2 instanceof TLNamed ? ((TLNamed) o2).getName() : null;
            
			int result = DOStringAttributeComparator.compareToString(_collator, name1, name2);
            if (result != 0) {
				return _ascending ? result : -result; // done we are ....
            }
        } catch (WrapperRuntimeException exp) {
            Logger.info("compare failed", exp, this);
        }        
		return 0;
    }

	/** Compares descending. */
	public static WrapperNameComparator getDescendingInstance() {
		return new WrapperNameComparator(/* ascending */false);
	}

	/** Compares ascending. */
	public static WrapperNameComparator getAscendingInstance() {
		return new WrapperNameComparator(/* ascending */ true);
	}

	/**
	 * Same as {@link #getAscendingInstance()}
	 */
	public static WrapperNameComparator getInstance() {
		return getAscendingInstance();
	}

	/**
	 * Null-safe variant of {@link #getInstance()}.
	 */
	public static Comparator<TLObject> getInstanceNullsafe() {
		return new NullSafeComparator<>(getInstance(), true);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (_ascending ? 1231 : 1237);
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
		WrapperNameComparator other = (WrapperNameComparator) obj;
		if (_ascending != other._ascending)
			return false;
		return true;
	}

}