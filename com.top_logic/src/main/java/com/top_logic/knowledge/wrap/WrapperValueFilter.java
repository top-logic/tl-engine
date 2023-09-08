/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap;

import java.util.Collection;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.col.Filter;
import com.top_logic.util.Utils;

/**
 * Filter for values from a wrapper.
 *
 * This filter will check, if the filterValue from the given object is equal to
 * the filterValue defined in this instance.
 * 
 * For Collection valued attributes this semantic is blurred.
 *
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class WrapperValueFilter implements Filter<Object> {

	/** The attribute name to be requested via {@link Wrapper#getValue(String)}. */
	private String	attribute;

	/** The filterValue to accept. */
	private Object	filterValue;

	/**
	 * Create a new instance of this class.
	 *
	 * @param anAttribute
	 *            The name of the attribute to be checked, must not be
	 *            <code>null</code>..
	 * @param aValue
	 *            The filterValue to equal to, must not be <code>null</code>.
	 */
	public WrapperValueFilter(String anAttribute, Object aValue) {
		this.attribute = anAttribute;
		this.filterValue = aValue;
	}

	public String getAttributeName() {
		return attribute;
	}

	public Object getMatchValue() {
		return filterValue;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return (this.getClass().getName() + " [" + "attribute: " + this.attribute + ", filterValue: " + this.filterValue + ']');
	}

	@Override
	public boolean accept(Object anObject) {
		if (anObject instanceof Wrapper) {
			final Wrapper wrapper = (Wrapper) anObject;
			if (!wrapper.tValid()) {
				return false;
			}
			Object objectValue = Utils.getValueByPath(this.attribute, wrapper);
			// handle null:
			if (objectValue == null) {
			    return this.filterValue == null;
			}
			// handling for multi-value (list) attributes
			if (objectValue instanceof Collection) {
				if (this.filterValue instanceof Collection) {
					// multiple objectValues, multiple Filtervalues...
					Collection filterValues = (Collection) this.filterValue;
					return (CollectionUtil.containsAny((Collection) objectValue, filterValues));
				} 
				// multiple object values, single filterValue
				return ((Collection) objectValue).contains(this.filterValue);
			} 
			// handle single value attributes
			if (this.filterValue instanceof Collection) {
				// //single objectValue, multiple filterValues
				Collection filterValues = (Collection) this.filterValue;
				return (filterValues.contains(objectValue));
			} 
			// default case: single objectValue, single filterValue
			return (objectValue.equals(this.filterValue));
		}
		return (false);
	}

}
