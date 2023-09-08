/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Comparator;

import com.top_logic.basic.ArrayUtil;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.col.TupleFactory.Tuple;

/**
 * This class breaks a sequence of text and numbers into its parts to be able to use numeric
 * ordering for embedded numbers.
 * 
 * <p>
 * The string is split into strings and integers composed from the digits. This can be used to sort
 * strings via the {@link MappingSorter}. In addition this class can be used as {@link Comparator}
 * for strings.
 * </p>
 * 
 * @author <a href="mailto:kha@top-logic.com">kha</a>
 */
public class StringDigitMapping implements Mapping, Comparator {

//    private static final Object INFINITY = Integer5.valueOf(Integer.MAX_VALUE);
    
	public static StringDigitMapping INSTANCE = new StringDigitMapping();
    
    protected StringDigitMapping() { 
        // allow subclasses but enforce usage of INSTANCE.
    }
    
    /**
     * @see com.top_logic.basic.col.Mapping#map(java.lang.Object)
     */
    @Override
	public Object map(Object input) {
        return getKey((CharSequence) input);
    }

	public Tuple getKey(CharSequence inputSequence) {
		return TupleFactory.newTuple(slice(inputSequence));
	}
    
    /**
	 * Slices the text/digit parts of the given input sequence into an array with
	 * CharSequence/Integers entries.
	 * 
	 * @return The sliced input sequence starting with a string. If the input sequence starts with a
	 *         digit, the first entry in the resulting array is the empty string.
	 */
    public Object[] slice(CharSequence aSeq) {
        if (aSeq == null) {
            return ArrayUtil.EMPTY_ARRAY;
        }
        int len = aSeq.length();
        if (len == 0) {
            return ArrayUtil.EMPTY_ARRAY;
        }
        ArrayList tmp = new ArrayList(4 + (len >> 4)); // divide by 16
        int     accu   = 0;
        char    currentChar = aSeq.charAt(0);
        boolean isDigit = isDecimalDigit(currentChar); 
        int     lastPos = 0;
        if (isDigit) {
            accu = (currentChar - '0');
            // Ensure that all entries of the resulting composite key start something comparable to a string.
            tmp.add("");
        }
        for (int pos = 1; pos < len; pos++) {
            currentChar = aSeq.charAt(pos);
            boolean newState = isDecimalDigit(currentChar); 
            if (newState != isDigit) {
                if (isDigit) {
                    tmp.add(Integer.valueOf(accu));
                } else {
                    tmp.add(aSeq.subSequence(lastPos, pos));
                    accu = 0; // newState is digit
                }
                lastPos = pos;
                isDigit = newState;
            }
            if (isDigit) {
                accu = 10 * accu + (currentChar - '0');
            }
        }
        if (isDigit) {
            tmp.add(Integer.valueOf(accu));
        } else {
            tmp.add(aSeq.subSequence(lastPos, len));
        }
        return tmp.toArray();
    }
    
    final static boolean isDecimalDigit(char c) {
        return (c >= '0') && (c <= '9');
    }

    /**
     * Compare two CharSequence with embedded digits.
     */
    @Override
	public int compare(Object o1, Object o2) {
        return compare((CharSequence) o1, (CharSequence) o2);
        /* Equivalent to
        RobustComposite key1 = new RobustComposite(slice((CharSequence) o1));
        RobustComposite key2 = new RobustComposite(slice((CharSequence) o2));
        return key1.compareTo(key2);
        */
    }
    
    /** Slice the Sting/digit parts of aSeq into and Object[] with CharSequence/Integers */
    public int compare(CharSequence seq1, CharSequence seq2) {
        int len1 = seq1 != null ? seq1.length() : 0;
        int len2 = seq2 != null ? seq2.length() : 0;
        if (len1 == 0) {
            return len2 == 0 ? 0 : -1;
        }
        if (len2 == 0) {
            return 1;
        }
        char    c1       = seq1.charAt(0);
        char    c2       = seq2.charAt(0);
        boolean isDigit  = isDecimalDigit(c1); 
        boolean isDigit2 = isDecimalDigit(c2); 
        if (c1 != c2 && (isDigit ^ isDigit2)) {
            if (isDigit2) {
                if (isDigit) {
                    throw new UnreachableAssertion("bot digits?");
                }
                return 1; // prefer numbers, first.
            }
            if (isDigit) {
                return -1; // prefer numbers, first.
            }
            return c1 - c2; // Is correct for both characters
        }
        
        ParsePosition pp1 = new ParsePosition(0);
        ParsePosition pp2 = new ParsePosition(0);
        int     result  = 0;
        do {
            if (isDigit) {
                Integer nextDigit1 = nextDigits(pp1,seq1, len1);
                Integer nextDigit2 = nextDigits(pp2,seq2, len2);
                result = ArrayUtil.compareObjects(nextDigit1, nextDigit2);
                isDigit = false;
            } else {
                String nextSeq1 = nextNonDigits(pp1,seq1, len1);
                String nextSeq2 = nextNonDigits(pp2,seq2, len2);
                result = ArrayUtil.compareObjects(nextSeq1, nextSeq2);
                isDigit = true;
            }
        } while (result == 0 && pp1.getIndex() <= len1 && pp2.getIndex() <= len2);

        return result;
    }
    
    private static Integer nextDigits(ParsePosition aPos, CharSequence aSeq, int len) {
        Integer result = null;
        int     pos    = aPos.getIndex();
        if (pos <= len) {
            int     accu   = 0;
    
            for (; pos < len; pos++) {
                char    c       = aSeq.charAt(pos);
                boolean isDigit = isDecimalDigit(c); 
                if (!isDigit) {
                    break;
                }
                accu = 10 * accu + (c - '0');
            }
            result = Integer.valueOf(accu);
        }
        aPos.setIndex(pos);
        return result;
    }

    private static String nextNonDigits(ParsePosition aPos, CharSequence aSeq, int len) {
        String result = null;
        int    pos    = aPos.getIndex() + 1;
        if (pos <= len) {
            for (; pos < len; pos++) {
                char    c       = aSeq.charAt(pos);
                boolean isDigit = isDecimalDigit(c); 
                if (isDigit) {
                    break;
                }
            }
            result = aSeq.subSequence(aPos.getIndex(), pos).toString();
        }
        aPos.setIndex(pos);
        return result;
    }

}

