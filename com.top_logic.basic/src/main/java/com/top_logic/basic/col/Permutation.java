/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.top_logic.basic.StringServices;

/**
 * Efficient Implementation of an integer based, partial permutation.
 * 
 * This implies some List this Permutation is applied to. The
 * List however ist <em>not</em> part of this object.
 * 
 * TODO c) Use this class in TableViewModel.
 * 
 * assert reverse(map(k)) = k holds for all k < size
 * 
 * @author <a href="mailto:kha@top-logic.com">kha</a>
 */
public class Permutation {

    
    /** Return this for invalid mappings */
	public static final int INVALID = -1;

    private static final int MAX_NUMER_LEN = 10; // Integer.toString(Integer.MAX_VALUE).length();

    /** Assume Filters in Average will reduce size to 1/FILTER_RATIO */
    private static final int FILTER_RATIO = 16;

    /** Maximum Size of Permutation */
	protected int baseSize;

    /** 
     * This describes the forward part of the permutation.
     * 
     * Every Integer must be contained in permuteArray exactly one.
     * When no filters are applied all values from [0..baseSize} must
     * be contained.
     * 
     * permuteArray.size() is always LE baseSize.
     */
	protected ArrayList<Integer> permuteArray;
	
	/**
	 * Allow Subclasses to handle CTor as they like it
	 */
	protected Permutation() {
	    // empty here
	}
	
	/**
	 * Create a new Identity Permutation with given baseSize.
	 */
	public Permutation(int baseSize) {
	    this.baseSize = baseSize;
	    permuteArray = identity(baseSize);
	}
	
    /**
     * Create a Permutation from a {@link #store()}d String.
     */
    public Permutation(int baseSize, String aString) {
        this.baseSize = baseSize;
        
        if (aString != null) {
            int       len       = aString.length();
            ArrayList<Integer> theResult = new ArrayList<>(len >>> 2);
            char[]    tmp       = new char[MAX_NUMER_LEN];  
            int  i = 0;     // index into aString
            char c = 0;
            while (i < len)  {
                int  j = 1; // index into tmp
                tmp[0] = c = aString.charAt(i++);
                while (i < len && j < MAX_NUMER_LEN) {
                    if (c == ',') {
                        j--;
                        break;
                    }
                    tmp[j++] = c = aString.charAt(i++);
                }
                String s = new String(tmp, 0, j);
                theResult.add(Integer.valueOf(Integer.parseInt(s, 10)));
            }
            theResult.trimToSize();
            this.permuteArray = theResult;
        } else {
            this.permuteArray = new ArrayList<>(0);
        }
    }

	/**
	 * Creates an identity permutation of the given size.
	 * 
	 * @param aSize
	 *        The size of the permutation.
	 * @return An identity permutation of the requested size.
	 */
    public static final ArrayList<Integer> identity(int aSize) {
	    ArrayList<Integer> result = new ArrayList<>(aSize);
        for (int i = 0; i < aSize; i++) {
            result.add(i, i);
        }
        return result;
	}
	
    /**
     * Take given inList and reset it to an Identy-List of given size.
     */
    public static final void resetIdentity(ArrayList<Integer> inList, int size) {
        inList.clear();
        inList.ensureCapacity(size);
        for (int i = 0; i < size; i++) {
            inList.add(i, i);
        }
        inList.trimToSize();
    }

    /**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
	    return this.getClass().getSimpleName() + "#" + getSize();
	}

	/** return mapped value of i */
	public int map(int index) {
	    return permuteArray.get(index);
	}

	/** return reverse mapped value of j */
	public int reverse(int index) {
	    return permuteArray.indexOf(index);
	}

	/** Permute the two given indexes */
	public void permute(int indexOne, int indexTwo) {
		Integer intOne = permuteArray.get(indexOne);
		Integer intTwo = permuteArray.get(indexTwo);

		permuteArray.set(indexOne, intTwo);
		permuteArray.set(indexTwo, intOne);
	}

	/** Return a read only View of aBase via this Permutation */
	public <T> List<T> map(List<T> aBase) {
	    return new PermuteList<>(aBase);
	}
	
	/** Rest permutation to identity */
	public void reset() {
	    int size = baseSize;
	    permuteArray.clear();
	    permuteArray.ensureCapacity(size);
        for (int i = 0; i < size; i++) {
            permuteArray.add(i, i);
        }
	}

	/**
	 * After this call applying the permutation to aBase will result in a sort
	 * according to aComparator.
	 * 
	 * @param aBase
	 *            will not be modified.
	 */
	public <T> void sort(List<T> aBase, Comparator<? super T> aComparator) {
		PermutateComparator<T> theComp = new PermutateComparator<>(aBase, aComparator);
		Collections.sort(permuteArray, theComp);
	}
	
	static final int calcFilteredSize(int aBase) {
	    if (aBase < (FILTER_RATIO * FILTER_RATIO)) {
	        return 16;
	    }
	    return aBase / FILTER_RATIO;
	}

    /**
     * Reduce Permutation to all values in aBase matched by Filter.
     * 
     * @param aBase  will not be modified.
     */
    public <T> void filter(List<T> aBase, Filter<? super T> aFilter) {
        
        int size = permuteArray.size();
        ArrayList<Integer> tmp = new ArrayList<>(calcFilteredSize(size));
        for (int i = 0; i < size; i++) {
            int index = permuteArray.get(i);
            if (aFilter.accept(aBase.get(index))) {
                tmp.add(index);
            }
        }
        permuteArray = tmp;
    }
    
    /**
     * Generate a String representation what can be used to recreate me.
     */
    public String store() {
        return StringServices.toString(permuteArray, ",");
    }

    /** true when Permutation was reduced to 0 */
    public boolean isEmpty() {
        return permuteArray.isEmpty();
    }

    public int getBaseSize() {
        return baseSize;
    }

    public int getSize() {
        return permuteArray.size();
    }

    /**
	 * {@link Comparator} for rows of the outer TableViewModel's
	 * underlying application model
	 * 
	 * <p>
	 * A TableViewModel.RowComparator instance compares two rows of the
	 * underlying TableViewModel#getApplicationModel() application model
	 * of its outer TableViewModel} by fetching the values at a fixed
	 * column from the rows (given as {@link Integer} indices) and applying a
	 * custom value {@link Comparator} to these values.
	 * </p>
	 */
	private final class PermutateComparator<T> implements Comparator<Integer> {

        /** The List the original values are fetched from in permuted order. */
        private final List<T> baseList;

        /** The actual Comparator doing the work */
        private final Comparator<? super T> comparator;


		PermutateComparator(List<T> aList, Comparator<? super T> aComparator) {
            this.baseList   = aList;
			this.comparator = aComparator;
		}
		
		/**
		 * Implements {@link Comparator#compare(Object, Object)} to compare two
		 * rows of the TableViewModel's underlying application model.
		 * 
		 * @param row1
		 *            An {@link Integer} object representin a row index into the
		 *            underlying application model.
		 * @param row2
		 *            An {@link Integer} object representin a row index into the
		 *            underlying application model.
		 * 
		 * @see Comparator#compare(Object, Object)
		 */
		@Override
		public int compare(Integer row1, Integer row2) {
			return comparator.compare(baseList.get(row1), baseList.get(row2));
		}
	}

	/**
	 * This class provides a Read only view to some base list using the Permutation.
	 * 
	 * @author    <a href=mailto:dna@top-logic.com>dna</a>
	 */
	public class PermuteList<T> extends AbstractList<T> {

	    List<T>        baseList;

		/**
		 * By creating a new PermuteList we need the BaseList and the Permutation that will be applied to it.
		 * 
		 */
		public PermuteList(List<T> aList) {
			this.baseList    = aList;
		}

		@Override
		public T get(int index) {
			return this.baseList.get(map(index));
		}

        @Override
		public int size() {
			return Permutation.this.permuteArray.size();
		}

		// Unsupported Operations
        @Override
		public boolean add(T o) {
			throw new UnsupportedOperationException(
					"This Method isnt implemented in this class");
		}

        @Override
		public void add(int i, T o) {
			throw new UnsupportedOperationException(
					"This Method isnt implemented in this class");
		}

        @Override
        public boolean addAll(Collection<? extends T> aC) {
            throw new UnsupportedOperationException(
                "This Method isnt implemented in this class");
        }

        @Override
		public void clear() {
			throw new UnsupportedOperationException(
					"This Method isnt implemented in this class");
		}

        @Override
		public T remove(int index) {
			throw new UnsupportedOperationException(
					"This Method isnt implemented in this class");
		}

        @Override
		protected void removeRange(int fromIndex, int toIndex) {
			throw new UnsupportedOperationException(
					"This Method isnt implemented in this class");
		}

        @Override
		public int lastIndexOf(Object o) {
			throw new UnsupportedOperationException(
					"This Method isnt implemented in this class");
		}

        @Override
		public int indexOf(Object o) {
			throw new UnsupportedOperationException(
					"This Method isnt implemented in this class");
		}
	}
}
