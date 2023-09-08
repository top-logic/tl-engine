/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import com.top_logic.element.meta.kbbased.WrapperMetaAttributeUtil;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.model.TLStructuredTypePart;

/**
 * Compare validity states of attributed objects.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class ValidityStateComparator implements Comparator {

    /** Direction of compare (ascending or descending). */
    private boolean asc;

    /** Small cache because sorting is expensive. */
    private Map cache;

    private TLStructuredTypePart attr;

    /** 
     * Create a new instance of this class.
     */
    public ValidityStateComparator(boolean isAsc) {
        this(null, isAsc, true);
    }
    
    /** 
     * Create a new instance of this class.
     */
    public ValidityStateComparator(TLStructuredTypePart anAttr, boolean isAsc) {
        this(anAttr, isAsc, true);
    }

    /** 
     * Create a new instance of this class.
     */
    public ValidityStateComparator(TLStructuredTypePart anAttr, boolean isAsc, boolean useCache) {
        this.attr  = anAttr;
        this.asc   = isAsc;
        this.cache = useCache ? new HashMap() : null;
    }

    /** 
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
	public int compare(Object o1, Object o2) {
        return ((asc ? 1 : -1) * (this.getState(o1) - this.getState(o2)));
    }

    public void setMetaAttribute(TLStructuredTypePart aMA) {
        if (aMA != this.attr) {
            this.attr = aMA;
            this.resetCache();
        }
    }

    /** 
     * Reset the internal cache.
     */
    public void resetCache() {
        if (this.cache != null) {
            this.cache.clear();
        }
    }

    /** 
     * Return the state for comparement (may be taken from cache).
     * 
     * This method will check, if there is a value in the cache. If it's not
     * in the cache {@link #getStateWithoutCache(Object)} will be called and 
     * the result is stored in the cache.
     * 
     * @param    anObject    The attributed to be checked.
     * @return   The state (red = 1; yellow = 2; green = 3; white = 4).
     * @see      #getStateWithoutCache(Object)
     */
    protected int getState(Object anObject) {
        if (anObject instanceof Wrapper) {
            int theResult = 0;

            if (this.cache != null) {
                Object theCache = this.cache.get(anObject);

                if (!(theCache instanceof Integer)) {
                    theResult = this.getStateWithoutCache(anObject);

					this.cache.put(anObject, Integer.valueOf(theResult));
                }
                else {
                    theResult = ((Integer) theCache).intValue();
                }
            }
            else {
                theResult = this.getStateWithoutCache(anObject);
            }

            return theResult;
        }
        else {
            return (4);
        }
    }

    /** 
     * Calculate the state for comparement.
     * 
     * @param    anObject    The attributed to be checked.
     * @return   The state (red = 1; yellow = 2; green = 3; white = 4).
     */
    protected int getStateWithoutCache(Object anObject) {
        int    theResult;
        Object theCache = WrapperMetaAttributeUtil.getValidityState((Wrapper) anObject, this.attr);

        if (MetaElementUtil.STATE_RED.equals(theCache)) {
            theResult = 1;
        }
        else if (MetaElementUtil.STATE_YELLOW.equals(theCache)) {
            theResult = 2;
        }
        else if (MetaElementUtil.STATE_GREEN.equals(theCache)) {
            theResult = 3;
        }
        else {
            theResult = 4;
        }

        return (theResult);
    }
}