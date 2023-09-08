/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.searching;

import java.util.Arrays;

/** 
 * The search attribute is part of the condition a search engine has
 * to notify, when creating a search result.
 * 
 * Depending on the kind of search attribute, the engine can generate
 * additional attributes to the 
 * {@link com.top_logic.knowledge.searching.SearchResult search result},
 * e.g. the relevance of the result can be identified by a search engine
 * when the user asks for this.
 *
 * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class SearchAttribute extends SearchResultAttribute
                             implements Cloneable {

    /** The ranking of the hit. */
    public static final SearchAttribute RANKING = new SearchAttribute(
                        "ranking",
                        new Float[] {
                            Float.valueOf(0.0f), Float.valueOf(0.1f), Float.valueOf(0.2f),
                            Float.valueOf(0.3f), Float.valueOf(0.4f), Float.valueOf(0.5f),
                            Float.valueOf(0.6f), Float.valueOf(0.7f), Float.valueOf(0.8f),
                            Float.valueOf(0.9f), Float.valueOf(1.0f)});

    /** Specifies, if a description should be delivered. */
    public static final SearchAttribute DESCRIPTION = new SearchAttribute(
                        "description",
                        new Boolean[] {Boolean.TRUE, Boolean.FALSE});

    /** Specifies, if a profile should be used for search. */
    public static final SearchAttribute PROFILES = new SearchAttribute(
                        "profiles",
                        new Boolean[] {Boolean.TRUE, Boolean.FALSE});

    /** The possible range of the value. */    
    private Object[] range;

    /**
     * Constructor for search attribute.
     * 
     * The default value for this attribute will be the first element in
     * the range, the attribute can act within.
     * 
     * @param    aKey          The key of the attribute.
     * @param    someRanges    The range the value can be.
     * @throws   NullPointerException             If the given range is null.
     * @throws   ArrayIndexOutOfBoundsException   If the given range is empty.
     * @throws   IllegalArgumentException         If the key is empty or
     *                                            <code>null</code>.
     */
    public SearchAttribute(String aKey, Object[] someRanges)
                                           throws IllegalArgumentException,
                                                  NullPointerException,
                                                  ArrayIndexOutOfBoundsException {
        this(aKey, someRanges[0], someRanges);
    }

    /**
     * Constructor for search attribute.
     * 
     * @param    aKey          The key of the attribute.
     * @param    aValue        The default value of the attribute.
     * @param    someRanges    The range the value can be.
     * @throws   IllegalArgumentException    If one of the parameters is empty
     *                                       or <code>null</code>.
     */
    public SearchAttribute(String aKey, Object aValue, Object[] someRanges)
                                            throws IllegalArgumentException {
        super(aKey, aValue);

        if ((someRanges == null) || (someRanges.length == 0)) {
            throw new IllegalArgumentException("Range is null or empty!");
        }

        this.range = someRanges;
    }

    /**
     * Return the string representation of this instance for debugging.
     * 
     * @return    The string representation for debugging.
     */
    @Override
	public String toString() {
        return (this.getClass().getName() + " ["
                        + "key: " + this.getKey()
                        + ", value: " + this.value
                        + ", range: " + Arrays.asList(this.range)
                        + ']');
    }

    /**
     * Set the value of this attribute.
     * 
     * The given value has to be one of the values defined in the range
     * of this instance. If it's not the method call will be ignored.
     * 
     * @param    aValue    The value to be set.
     * @return   <code>true</code>, if the value is within the range.
     */
    public boolean setValue(Object aValue) {
        boolean found = false;
        
        if (aValue instanceof Comparable) {
            found = (Arrays.binarySearch(this.range, aValue) >= 0);
        }
        else {
            for (int i = 0; i < this.range.length; i++) {
                if (this.range[i].equals(aValue)) {
                    found = true;
                    break;
                }
            }
        }
        
        if (found) {
            this.value = aValue;
        }

        return (found);
    }
    
    /**
     * Return the range of values for this attribute.
     * 
     * @return    The range of values for this attribute
     */
    public Object[] getRange() {
        return this.range;
    }
    
    /**
     * @see java.lang.Object#equals(Object)
     */
    @Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
        if (obj instanceof SearchAttribute) {
            SearchAttribute attribute = (SearchAttribute) obj;
            if (attribute.getKey().equals(this.getKey())) {
                return true;
            }
        }
        return false;
    }
    
	@Override
	public int hashCode() {
		final int prime = 91957;
		int result = 46584;
		result = prime * result + getKey().hashCode();
		return result;
	}

    /**
     * @see java.lang.Object#clone()
     */
    @Override
	public Object clone() throws CloneNotSupportedException {
        SearchResultAttribute result = (SearchResultAttribute) super.clone();
        return result;
    }
}
