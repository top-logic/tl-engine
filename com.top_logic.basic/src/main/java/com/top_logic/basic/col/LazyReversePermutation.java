/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * A Permutation that will build a reverse Index ond demand.
 * 
 * This will allow fast reverse lookups, but (re-)creating them
 * will expensive, once.
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class LazyReversePermutation extends Permutation {

    /** 
     * The Reverse mapping is recreated lazily whenever needed.
     * 
     * assert reverseMap.get(permuteArray(i)) == i must hold. 
     */
    protected HashMap<Integer, Integer> reverseMap;

    /** 
     * Creates a {@link LazyReversePermutation}.
     * 
     * @param aBaseSize size of some underlying List.
     */
    public LazyReversePermutation(int aBaseSize) {
        super(aBaseSize);
    }
    
    /**
     * Create a Permutation from a {@link Permutation#store()}d String.
     */
    public LazyReversePermutation(int aBaseSize, String aString) {
        super(aBaseSize, aString);
    }



    /**
     * @see com.top_logic.basic.col.Permutation#reverse(int)
     */
    @Override
	public int reverse(int aIndex) {
        if (reverseMap == null) {
            rebuildReverse();
        }
        return super.reverse(aIndex);
    }
    
    /**
     * Reset reverseMap before permuting.
     */
    @Override
	public void permute(int aIndexOne, int aIndexTwo) {
        reverseMap = null;
        super.permute(aIndexOne, aIndexTwo);
    }

    /**
     * Reset reverseMap before sorting.
     */
    @Override
	public <T> void sort(List<T> aBase, Comparator<? super T> aComparator) {
        reverseMap = null;
        super.sort(aBase, aComparator);
    }
    
    /**
     * Reset reverseMap before filtering.
     */
    @Override
	public <T> void filter(List<T> aBase, Filter<? super T> aFilter) {
        reverseMap = null;
        super.filter(aBase, aFilter);
    }

    
    /** 
     * Recreate reverse Map from permuteArray
     */
    protected void rebuildReverse() {
        int                         theSize  = permuteArray.size();
        ArrayList<Integer>          permList = permuteArray;
        HashMap<Integer,Integer>    rMap     = new HashMap<>(theSize);
        for (int i=0; i< theSize; i++) {
            rMap.put(permList.get(i), i);
        }
        reverseMap = rMap;
    }

}

