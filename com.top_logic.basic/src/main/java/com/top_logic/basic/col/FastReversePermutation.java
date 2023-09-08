/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A Permutation that will keep its reverse Index in sync.
 * 
 * This will allow fast reverse lookups, but keeping it in sync
 * will be expensive.
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class FastReversePermutation extends Permutation {

    /** 
     * The Reverse mapping kept up to date, here.
     * 
     * assert reverseMap.get(permuteArray(i)) == i must hold. 
     */
    protected HashMap<Integer, Integer> reverseMap;

    /** 
     * Creates a {@link FastReversePermutation}.
     */
    public FastReversePermutation(int aBaseSize) {
        ArrayList<Integer>          actualAL  = new ArrayList<>       (aBaseSize);
        HashMap<Integer, Integer>   actualMap = new HashMap<>(aBaseSize);
        for (int i = 0; i < aBaseSize; i++) {
            Integer intObj = i;
            actualAL.add(intObj);
            actualMap.put(intObj, intObj);
        }
        this.permuteArray = actualAL;
        this.reverseMap   = actualMap;
        this.baseSize     = aBaseSize;
    }

    /**
     * Create a Permutation from a {@link #store()}d String.
     */
    public FastReversePermutation(int aBaseSize, String aString) {
        super(aBaseSize, aString);
        
        int                         theSize  = permuteArray.size();
        ArrayList<Integer>          permList = permuteArray;
        HashMap<Integer, Integer>   rMap     = new HashMap<>(theSize);
        for (int i=0; i< theSize; i++) {
            rMap.put(permList.get(i), i);
        }
        reverseMap = rMap;

    }

    /**
     * @see com.top_logic.basic.col.Permutation#reset()
     */
    @Override
	public void reset() {
        int theSize = this.baseSize;
        ArrayList<Integer>          actualAL  = new ArrayList<>(theSize);
        HashMap<Integer, Integer>   actualMap = new HashMap<>  (theSize);
        for (int i = 0; i < theSize; i++) {
            Integer intObj = Integer.valueOf(i);
            actualAL.add(intObj);
            actualMap.put(intObj, intObj);
        }
        this.permuteArray = actualAL;
        this.reverseMap   = actualMap;
    }
    
    /** return reverse mapped value of j */
    @Override
	public int reverse(int index) {
        Integer intObj = Integer.valueOf(index);
        Integer mapped = reverseMap.get(intObj);
        if(mapped == null){
            return INVALID;
        }
        return mapped.intValue();
    }
    
    @Override
	public void permute(int indexOne, int indexTwo) {
        Integer intOne = permuteArray.get(indexOne);
        Integer intTwo = permuteArray.get(indexTwo);

        permuteArray.set(indexOne, intTwo);
        permuteArray.set(indexTwo, intOne);

        reverseMap.put(intOne, indexTwo);
        reverseMap.put(intTwo, indexOne);
    }
    
    /**
     * Reduce Permutation to all values in aBase matched by Filter.
     * 
     * @param aBase  will not be modified.
     */
    @Override
	public <T> void filter(List<T> aBase, Filter<? super T> aFilter) {
        
        int size = permuteArray.size();
        reverseMap.clear();
        ArrayList<Integer> tmp = new ArrayList<>(calcFilteredSize(size));
        for (int i = 0; i < size; i++) {
            Integer index = permuteArray.get(i);
            if (aFilter.accept(aBase.get(index.intValue()))) {
                reverseMap.put(i, tmp.size());
                tmp.add(index);
            }
        }
        permuteArray = tmp;
        assert getSize() == reverseMap.size();
    }



}

