/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

import static java.util.Arrays.*;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Proxy;
import java.text.DateFormat;
import java.text.Format;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Random;
import java.util.RandomAccess;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.UUID;

import com.top_logic.basic.charsize.CharSizeMap;
import com.top_logic.basic.charsize.FontCharSizeMap;
import com.top_logic.basic.charsize.ProportionalCharSizeMap;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.shared.string.StringServicesShared;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.basic.util.RegExpUtil;
import com.top_logic.basic.util.ResKey;

/**
 * Class implementing extended String operations.
 *
 * @author  <a href="kha@top-logic.com">Klaus Halfmann</a>
 */
public abstract class StringServices extends StringServicesShared {

	/**
	 * The unicode <a href="https://en.wikipedia.org/wiki/Non-breaking_space">narrow no-break
	 * space</a> character.
	 */
	public static final char NARROW_NO_BREAK_SPACE = '\u202F';

	/** Instance of an empty string */
    public static final String EMPTY_STRING = "";
    
    /** Instance of a line break string "\n" */
    public static final String LINE_BREAK = "\n";

    /** Instance of a line break '\n' */
    public static final char LINE_BREAK_CHAR = '\n';

    /** Instance of a tabulator '\t' */
    public static final char TAB_CHAR = '\t';
    
    /** Instance of a blank ' ' */
    public static final char BLANK_CHAR = ' ';
    
    /** Array of hex digit for some fast conversions */
    private final static char HEX_DIGITS[] = {
        '0', '1', '2', '3', '4', '5', '6', '7',
        '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };

    public static final int START_POSITION_HEAD = 0;
    public static final int START_POSITION_TAIL = 1;
    
    /** Constant to fetch a CTor bases on a String Parameter */
    static final Class<?>[] STRING_PARAM = new Class[] { String.class };
 
    /** 
     * Used to generate strings with a number of spaces.  
     * 
     * @see #getSpaces(int)
     */
    private static StringBuilder spaces = new StringBuilder("                              ");

    public static final String VOCALS = "aeiou";
    public static final String KONSONANTS = "bcdfghjklmnpqrstvwxyz";
    public static final String NUMERALS = "0123456789";

    public static final String GERMAN_CHARS = "äöüß";
    public static final String SPECIAL_CHARS = "+-*/_=.";

    public static final String LITERALS = VOCALS + KONSONANTS;
    public static final String ALPHANUMERIC = VOCALS + KONSONANTS + NUMERALS;

    /**
     * Parse a String containing a boolean, "true" and "yes" will mapped to true.
     * Case is ignored, null and empty strings are mapped to false.
     * #author kha
     *
     * @param   aString     a String like "t", "TRUE", "yes" , "Y"
     * @return  true        when string looks like "true"
     *
     * @see Boolean#parseBoolean(String)
     * @see Boolean#valueOf(String)
     * @see com.top_logic.basic.util.Utils#getBooleanValue(Object)
     * @see com.top_logic.basic.util.Utils#getbooleanValue(Object)
     */
    public static boolean parseBoolean (String aString) {
        if (aString == null || aString.length() == 0) {
            return false;
        }
        char c = Character.toUpperCase(aString.charAt(0));
        return c == 'T' || c == 'Y';
    }

    /**
     * If the value equals (ignore case) "true" => true
     * If the value equals (ignore case) "false" => false
     * Else: Maybe.none()
     * 
     * @param value Is allowed to be <code>null</code>.
     * @return Never <code>null</code>.
     */
	public static Maybe<Boolean> parseBooleanStrict(String value) {
		if (value == null) {
			return Maybe.none();
		}
		if (value.equalsIgnoreCase("true")) {
			return Maybe.some(true);
		}
		if (value.equalsIgnoreCase("false")) {
			return Maybe.some(false);
		}
		return Maybe.none();
	}

    /** 
     * Remove all \n and \r from a CharSequence.
     *
     * (When used with larger strings you should consider using
     *  some stream-based implementation)
     */
    public static String stripNewlines(CharSequence input)
    {
        int l = input.length();
        StringBuilder result = new StringBuilder(input.length());
        for (int i=0; i < l; i++) {
            char c = input.charAt(i);
            if (c != '\n' && c != '\r') {
                result.append(c);
            }
        }
        return result.toString();
    }
 

    /**
     * Convert the given string to an array of strings.
     *
     * This method expects the comma to be the separator for different strings.
     *
     * @param    aString    The string to be converted.
     * @return   The array of strings.
     */
    public static String [] toArray (CharSequence aString) {
        return (toArray (aString, ','));
    }

    /**
     * Convert the given string to an array of strings.
     *
     * The resulting Strings will be trim()med.
     * 
     * Think about using {@link #toArray(CharSequence, char)} which is much faster.
     *
     * @param    aString       The string to be converted.
     * @param    aSeparator    The separators to be used for splitting.
     * @return   The array of trimmed strings, empty array for null input.
     */
    public static String [] toArray (String aString, String aSeparator) {
        String [] theResult;

        if (aString != null) {
            int             thePos    = 0;
            StringTokenizer theToken  = new StringTokenizer (aString,
                                                             aSeparator);

            theResult = new String [theToken.countTokens ()];

            while (theToken.hasMoreTokens ()) {
                theResult [thePos++] = (theToken.nextToken ().trim ());
            }
        }
        else {
            theResult = new String [0];
        }

        return (theResult);
    }

    /** 
     * Convert a collection of objects to an array of strings by using the given formatter.
     * 
     * @param    someObjects    The objects to be converted to string, may be <code>null</code>.
     * @param    aFormat        The formatter to convert the given objects, must not be <code>null</code>.
     * @return   The requested string array, never null.
     */
    public static String[] toArray(Collection<?> someObjects, Format aFormat) {
        if (someObjects == null) {
            return new String[0];
        }
        else {
            String[] theResult = new String[someObjects.size()];
            int      thePos    = 0;

            for (Iterator<?> theIt = someObjects.iterator(); theIt.hasNext();) {
                theResult[thePos] = aFormat.format(theIt.next());
                thePos++;
            }

            return theResult;
        }
    }

    /**
     * Convert the given string to an array of strings.
     *
     * The resulting Strings will be trim()med. Empty results
     * (e.g. ",  ,, ," will be suppressed. Instead of an empty
     * List null will be returned, if aString is null.
     *
     * @param    aString       The string to be converted.
     * @param    aSeparator    The separator to be used for splitting
     * @return   The array of trimmed strings or null.
     */
    public static String [] toArray (CharSequence aString, char aSeparator) {

        List<String> theResult = toList(aString, aSeparator);
        if (theResult != null) {
            int size = theResult.size();
            if (size > 0) {
                return theResult.toArray(new String[size]);
            }
        }

        return null;
    }
    
    /**
     * Return a string representation for an array of strings.
     * 
     * The returned representation will have the form: {'val1','val2'}.
     * If there are any <code>null</code> values, they will not have
     * quotes e.g {'val1',null}.
     * 
     * @param    anArray    The array to be represented.
     * @return   The string representation of the given array.
     */
    public static String toString(Object[] anArray) {

		if (anArray == null) {
			return "{}";
		}
		int len = anArray.length;
		if (len == 0) {
			return "{}";
		}

		StringBuilder buffer = new StringBuilder(len << 5); // * 32
		buffer.append('{');

		Object o = anArray[0];
		if (o != null) {
			buffer.append('\'').append(o).append('\'');
		} else
			buffer.append("null");

		for (int thePos = 1; thePos < len; thePos++) {
			o = anArray[thePos];
			if (o != null) {
				buffer.append(",'").append(o).append('\'');
			} else
				buffer.append(",null");
		}

		return buffer.append('}').toString();
	}
    
    /**
	 * Creates a string which is the concatenation of the objects in the given array. If the array
	 * is empty then the empty string is returned.
	 * 
	 * @param array
	 *        The array to be represented.
	 * @param separator
	 *        used to separate the strings.
	 * @return The string representation of the given array.
	 * 
	 * @throws NullPointerException
	 *         If the given array is <code>null</code>.
	 */
    public static String toString(Object[] array, char separator) {
		return toString(array, 0, array.length, separator);
	}

	/**
	 * Creates a string which is the concatenation of the objects in the given array, from
	 * <code>fromIndex</code>, inclusive, to <code>toIndex</code>, exclusive.
	 * 
	 * @param array
	 *        The array to be represented.
	 * @param fromIndex
	 *        The index of the first element, inclusive, to contain in the result string.
	 * @param toIndex
	 *        The index of the last element, exclusive, to contain in the result string.
	 * @param separator
	 *        used to separate the strings.
	 * @return The string representation of the given array.
	 * 
	 * @throws NullPointerException
	 *         If the given array is <code>null</code>.
	 */
	public static String toString(Object[] array, int fromIndex, int toIndex, char separator) {
		if (toIndex < fromIndex) {
			throw new IllegalArgumentException("toIndex must not be smaller than fromIndex");
		}
		if (toIndex == fromIndex)
			return EMPTY_STRING;
		StringBuilder sb = new StringBuilder((toIndex - fromIndex) * 10);

		sb.append(array[fromIndex]);
		for (int i = fromIndex + 1; i < toIndex; i++) {
			sb.append(separator);
			sb.append(array[i]);
        }
        return sb.toString();
    }

    /**
	 * Creates a string which is the concatenation of the objects in the array. If the array is
	 * empty then the empty string is returned.
	 *
	 * @param array
	 *        The array to be represented.
	 * @param separator
	 *        Used to separate the strings.
	 *        
	 * @return The string representation of the given array.
	 * 
	 * @throws NullPointerException
	 *         If the given array is <code>null</code>.
	 */
	public static String toString(Object[] array, String separator) {
		return toString(array, 0, array.length, separator, separator);
    }

	/**
	 * Creates a string which is the concatenation of the objects in the array, from
	 * <code>fromIndex</code>, inclusive, to <code>toIndex</code>, exclusive.
	 *
	 * @param fromIndex
	 *        The index of the first element, inclusive, to contain in the result string.
	 * @param toIndex
	 *        The index of the last element, exclusive, to contain in the result string.
	 * @param separator
	 *        Used to separate the strings.
	 *        
	 * @return The string representation of the given array.
	 * 
	 * @throws NullPointerException
	 *         If the given array is <code>null</code>.
	 */
	public static String toString(Object[] array, int fromIndex, int toIndex, String separator) {
		return toString(array, fromIndex, toIndex, separator, separator);
	}

    /**
	 * Creates a string which is the concatenation of the objects in the array. If the array is
	 * empty then the empty string is returned.
	 *
	 * @param array
	 *        The array to be represented.
	 * @param separator
	 *        Used to separate the elements.
	 * @param lastSeparator
	 *        Used to separate the last element.
	 *        
	 * @return The string representation of the given array.
	 * 
	 * @throws NullPointerException
	 *         If the given array is <code>null</code>.
	 */
	public static String toString(Object[] array, String separator, String lastSeparator) {
		return toString(array, 0, array.length, separator, lastSeparator);
    }

	/**
	 * Creates a string which is the concatenation of the objects in the array, from
	 * <code>fromIndex</code>, inclusive, to <code>toIndex</code>, exclusive.
	 *
	 * @param fromIndex
	 *        The index of the first element, inclusive, to contain in the result string.
	 * @param toIndex
	 *        The index of the last element, exclusive, to contain in the result string.
	 * @param separator
	 *        Used to separate the strings.
	 * @param lastSeparator
	 *        Used to separate the last element.
	 *        
	 * @return The string representation of the given array.
	 * 
	 * @throws NullPointerException
	 *         If the given array is <code>null</code>.
	 */
	public static String toString(Object[] array, int fromIndex, int toIndex, String separator,
			String lastSeparator) {
		if (toIndex < fromIndex) {
			throw new IllegalArgumentException("fromIndex must not be smaller than toIndex");
		}
		if (toIndex == fromIndex)
			return EMPTY_STRING;
		if (toIndex == fromIndex + 1) {
			return String.valueOf(array[fromIndex]);
		}
		StringBuilder sb = new StringBuilder((toIndex - fromIndex) * 10);

		sb.append(array[fromIndex]);
		toIndex--;
		for (int i = fromIndex + 1; i < toIndex; i++) {
			sb.append(separator);
			sb.append(array[i]);
		}
		sb.append(lastSeparator);
		sb.append(array[toIndex]);

		return sb.toString();
	}

    /**
     * A synonym for {@link #toString(Object[], char)}. 
     * 
     * Languages like python or ruby define a join method.
     */
    public static String join(Object[] anArray, char separator){
        return toString(anArray,separator);
    }
    
    /**
     * Same as  anArray[0].toString + anArray[1].toString + ...
     * 
     * Languages like python or ruby define a join method.
     */
    public static String join(Object[] anArray){
        int len = anArray != null ? anArray.length : 0;
        if (len == 0) {
            return EMPTY_STRING;
        }
        StringBuilder theBuffer = new StringBuilder(len << 5); // * 32
        for (int thePos = 0; thePos < len; thePos++) {
            Object o = anArray[thePos];
            if (o != null) {
                theBuffer.append(o);
            }
        }

        return theBuffer.toString();
    }

 
    
	/**
	 * Like {@link #join(Object[], char)} but with {@link List} objects.
	 */
	public static String join(Collection<?> values, String separator) {
		StringBuilder result = new StringBuilder();
		for (Iterator<?> it = values.iterator(); it.hasNext(); ) {
			result.append(it.next());
			if (it.hasNext()) {
				result.append(separator);
			}
		}
		return result.toString();
	}
	
    /**
     * Like toArray(), but does not suppress empty results and instead creates empty Strings.
     */
    public static String[] split(CharSequence aString,char aSeparator){
        List<String> theResult = toListAllowEmpty(aString, aSeparator);
        return theResult.toArray(new String[theResult.size()]);
    }
    
    private final static char[] DIGIT_TENS = {
		'0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
		'1', '1', '1', '1', '1', '1', '1', '1', '1', '1',
		'2', '2', '2', '2', '2', '2', '2', '2', '2', '2',
		'3', '3', '3', '3', '3', '3', '3', '3', '3', '3',
		'4', '4', '4', '4', '4', '4', '4', '4', '4', '4',
		'5', '5', '5', '5', '5', '5', '5', '5', '5', '5',
		'6', '6', '6', '6', '6', '6', '6', '6', '6', '6',
		'7', '7', '7', '7', '7', '7', '7', '7', '7', '7',
		'8', '8', '8', '8', '8', '8', '8', '8', '8', '8',
		'9', '9', '9', '9', '9', '9', '9', '9', '9', '9',
	};

	private final static char[] DIGIT_ONES = {
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
	};

	private static final int[] INT_DIVISORS = { 100000000, 1000000, 10000, 100, 1 };

	private static final int INT_DIVISORS_LENGTH = INT_DIVISORS.length;



	/**
	 * Allocation-free append the given decimal value to the given writer.
	 * 
	 * @param out
	 *        The writer to write to.
	 * @param value
	 *        The number to write.
	 * @throws IOException
	 *         If output fails.
	 */
	public static void append(Appendable out, int value) throws IOException {
		if (value < 0) {
			if (value == Integer.MIN_VALUE) {
				out.append("-2147483648");
				return;
			}
	
			out.append('-');
			value = -value;
		}
	
		appendPositiveInt(out, value);
	}

	private static void appendPositiveInt(Appendable out, int value) throws IOException {
		int n = 0;
		while (n < INT_DIVISORS_LENGTH) {
			int divisor = INT_DIVISORS[n++];
			int twoDigits = value / divisor;
	
			if (twoDigits != 0) {
				value -= divisor * twoDigits;
	
				char tensDigit = DIGIT_TENS[twoDigits];
				if (tensDigit != '0') {
					out.append(tensDigit);
				}
	
				out.append(DIGIT_ONES[twoDigits]);
				if (n == INT_DIVISORS_LENGTH) {
					return;
				}
	
				break;
			}
		}
	
		if (n < INT_DIVISORS_LENGTH) {
			appendIntTail(out, value, n);
		} else {
			out.append('0');
		}
	}

	private static void appendIntTail(Appendable out, int value, int n) throws IOException {
		do {
			int divisor = INT_DIVISORS[n++];
			int twoDigits = value / divisor;
			value -= divisor * twoDigits;
	
			out.append(DIGIT_TENS[twoDigits]);
			out.append(DIGIT_ONES[twoDigits]);
		} while (n < INT_DIVISORS_LENGTH);
	}



	private static final long[] LONG_DIVISORS = {
		1000000000000000000L,
		10000000000000000L,
		100000000000000L,
		1000000000000L,
		10000000000L,
		100000000L
	};

	private static final int LONG_DIVISORS_LENGTH = LONG_DIVISORS.length;



	/**
	 * Allocation-free append the given decimal value to the given writer.
	 * 
	 * @param out
	 *        The writer to write to.
	 * @param value
	 *        The number to write.
	 * @throws IOException
	 *         If output fails.
	 */
	public static void append(Appendable out, long value) throws IOException {
		if (value < 0) {
			if (value == Long.MIN_VALUE) {
				out.append("-9223372036854775808");
				return;
			}
	
			out.append('-');
			value = -value;
		}
	
		if (value <= Integer.MAX_VALUE) {
			appendPositiveInt(out, (int) value);
			return;
		}
	
		int n = 0;
		while (n < LONG_DIVISORS_LENGTH) {
			long divisor = LONG_DIVISORS[n++];
			int twoDigits = (int) (value / divisor);
	
			if (twoDigits != 0) {
				value -= divisor * twoDigits;
	
				char tensDigit = DIGIT_TENS[twoDigits];
				if (tensDigit != '0') {
					out.append(tensDigit);
				}
	
				out.append(DIGIT_ONES[twoDigits]);
				if (n == LONG_DIVISORS_LENGTH) {
					appendIntTail(out, (int) value, 1);
					return;
				}
	
				break;
			}
		}
	
		if (n < LONG_DIVISORS_LENGTH) {
			do {
				long divisor = LONG_DIVISORS[n++];
				int twoDigits = (int) (value / divisor);
				value -= divisor * twoDigits;
	
				out.append(DIGIT_TENS[twoDigits]);
				out.append(DIGIT_ONES[twoDigits]);
			} while (n < LONG_DIVISORS_LENGTH);
	
			appendIntTail(out, (int) value, 1);
		} else {
			// Still potentially null.
			appendPositiveInt(out, (int) value);
		}
	}

    /**
     * Converts the given String to a String, which consists of the
     * hexadecimal number for each charachter.
     * <p>
     * This will fail for very large Strings (longer than Integer.MAX_VALUE/4),
     * since the returned String is four times the size of the input.
     * </p>
     *
     * @param aString   to convert
     * @param start     where will conversion start,
     * @param end       where will conversion end.
     * @param into      result will be appended to given StringBuilder.
     *
     */
    public static void toHexString(CharSequence aString, int start, int end, StringBuilder into){
        for (int i = start; i < end; i++) {  
            char c  = aString.charAt(i);
            int hi = c >>> 8;   
            int lo = c & 0xff; 
            into.append(HEX_DIGITS[hi >> 4]);
            into.append(HEX_DIGITS[hi & 0x0f]);
            into.append(HEX_DIGITS[lo >> 4]);
            into.append(HEX_DIGITS[lo & 0x0f]);
        }
    }

    /**
     * Converts the given String to a String, which consists of the
     * hexadecimal number for each charachter.
     * <p>
     * This will fail for very large Strings (longer than Integer.MAX_VALUE/4),
     * since the returned String is four times the size of the input.
     * </p>
     *
     * @param aString to convert
     * @return the hexadecimal form of the String
     *
     */
    public static String toHexString(CharSequence aString){
        int len = aString.length();
        StringBuilder buf = new StringBuilder(len << 2);
        toHexString(aString,0, len, buf);
        return buf.toString();
    }
    
    /**
     * Converts the given String to a String, which consists of the
     * hexadecimal number for each charachter.
     * <p>
     * This will fail for very large Strings (longer than Integer.MAX_VALUE/4),
     * since the returned String is four times the size of the input.
     * </p>
     *
     * @param  aBytes   The String to convert
     * @return the hexadecimal form of the String
     *
     */
    public static String toHexString(byte[] aBytes){
        int len = aBytes.length;
        StringBuilder buf = new StringBuilder(len << 1);
        toHexString(aBytes,0, len, buf);
        return buf.toString();
    }

    /**
     * Converts the given bytes to a String, which consists of a
     * hexadecimal number for each byte.
     *
     * @param aBytes    The values to convert
     * @param start     where will conversion start,
     * @param end       where will conversion end.
     * @param into      result will be appended to given StringBuilder.
     *
     */
    public static void toHexString(byte[] aBytes, int start, int end, StringBuilder into){
        for (int i = start; i < end; i++) {  
            int b = aBytes[i];
            into.append(HEX_DIGITS[(b >>> 4) & 0x0f]);
            into.append(HEX_DIGITS[ b        & 0x0f]);
        }
    }

    /**
     * Converts the given String, 
     * assuming to contain hexadecimal numbers,
     * to a String containing the charachters, 
     * belonging to the hexadecimal numbers. 
     * If one String part is not parseable,
     * returning the given String itself.
     * 
     * @param aString to convert 
     * @return the hexadecimal form of the String
     * 
     */
    public static String hexStringToString(String aString) 
        throws NumberFormatException {
        int          len = aString.length();        
        StringBuilder buf = new StringBuilder(len >>> 2);
        for (int i = 0; i < len; ) {          
           buf.append(hexToChar(aString, i, i+=4));
        }
        
        return buf.toString();
    }
    
    /**
     * Gets the char belonging to the given hexadecimal code.
     * Gets the given String itself, if 
     * is null, 
     * is the empty String or 
     * is not parseable.
     * 
     * @param aHexCode the hexadecimal code to get a char for.
     * @param start index into aHexCode where conversion should start
     * @param end   index into aHexCode where conversion should end
     * @return the char belonging to the hexadecimal code.
     */
    static private char hexToChar(CharSequence aHexCode, int start, int end) 
        throws NumberFormatException {
        char result = 0;
        for (int i= start; i < end; i++) {
            result <<= 4;
            int hex = 0xFFFF & aHexCode.charAt(i);
            if (hex >= '0' && hex <='9') {
                result |= (hex - '0');
            }
            else if (hex >= 'a' && hex <='f') {
                result |= (hex - 'a' + 10);
            } // allow for A-F, too
            else if (hex >= 'A' && hex <='F') {
                result |= hex - 'A' + 10;
            }
            else
                throw new NumberFormatException("Not a hex Number '" 
                    + ((char) hex) + "'");
        }
        return result;
    }
    
    /**
     * Converts the given String, assuming it contains hexadecimal numbers,
     * to bytes.
     * 
     * @param   aString to convert 
     * @return  the hexadecimal form of the String
     * 
     */
    public static byte[] hexStringToBytes(String aString) 
        throws NumberFormatException {
        int          len = aString.length();        
        byte[]       buf = new byte[len >>> 1];
        for (int i=0, j=0; i < len; i+=2,j++) {          
            buf[j] = hexToByte(aString, i);
        }
        
        return buf;
    }

    /**
     * Gets the bytes for the given hexadecimal characters.
     * 
     * Will always convert excatly two Characters.
     * 
     * @param   aHexCode    the CharSequence to extract the hex-chars from
     * @param   index       index into aHexCode where conversion should start
     * @return  the byte for the code.
     */
    static private byte hexToByte(CharSequence aHexCode, int index) 
        throws NumberFormatException {
        byte result = 0;
        int  end    = index + 2;
        for (int i= index; i < end; i++) {
            result <<= 4;
            int hex = 0xFFFF & aHexCode.charAt(i);
            if (hex >= '0' && hex <='9') {
                result |= (hex - '0');
            }
            else if (hex >= 'a' && hex <='f') {
                result |= (hex - 'a' + 10);
            } // allow for A-F, too
            else if (hex >= 'A' && hex <='F') {
                result |= hex - 'A' + 10;
            }
            else
                throw new NumberFormatException("Not a hex Number '" 
                    + ((char) hex) + "'");
        }
        return result;
    }
    
    
    /**
     * Same as {@link #toList(CharSequence, char)} but never return null.
     *
     * @param    aString       The string to be converted.
     * @param    aSeparator    The separator to be used for splitting
     * @return   A list of trimmed strings, never null.
     */
    public static List<String> toNonNullList (CharSequence aString, char aSeparator) {
        List<String> theResult = toList(aString, aSeparator);
        return theResult == null ? new ArrayList<>() : theResult;
    }
    
    
    /**
     * Convert the given string to a List of strings.
     *
     * The resulting Strings will be trimmed. Empty results
     * (e.g. ",  ,, ," will be suppressed. Instead of an empty
     * List null will be returned, if aString is null.
     *
     * @param    aString       The string to be converted.
     * @param    aSeparator    The separator to be used for splitting
     * @return   A list of trimmed strings or null.
     */
    public static List<String> toList (CharSequence aString, char aSeparator) {

        ArrayList<String> theResult = null;
        if (aString != null) {
            int    len       = aString.length();
                   theResult = new ArrayList<>(len >>> 2);
            addStringsToCollection(aString, aSeparator, theResult);
        }
        return theResult;
    }
    
    /** 
     * Convert the given string to a Set of strings.
     *
     * The resulting Strings will be trimmed. Empty results
     * (e.g. ",  ,, ," will be suppressed. Instead of an empty
     * List null will be returned, if aString is null.
     *
     * @param    aString       The string to be converted.
     * @param    aSeparator    The separator to be used for splitting.
     * @return   A set of trimmed strings or null.
     */
    public static Set<String> toSet(CharSequence aString, char aSeparator) {
        
        HashSet<String> theResult = null;
        if (aString != null) {
            int    len       = aString.length();
                   theResult = new HashSet<>(len >>> 2);
            addStringsToCollection(aString, aSeparator, theResult);
        }
        return theResult;
    }
    
    /**
     * Like toList, but creates empty elements. 
     * 
     * (E. g., toListAllowEmpty("::",':') will create three empty strings)
     * Strings are NOT trimmed. A null string will produce an empty lists
     */
    public static List<String> toListAllowEmpty (CharSequence aString, char aSeparator) {
        if (aString == null) 
            return new ArrayList<>();

        int           len       = aString.length();
        List<String>  theResult = new ArrayList<>(len >>> 2);
        StringBuilder theBuff   = new StringBuilder();
        for (int i=0; i<len; i++) {
            char c = aString.charAt(i);
            if (aSeparator == c) {
                theResult.add(theBuff.toString());
                theBuff.setLength(0);
                continue;
            }
            theBuff.append(c);
        }
        theResult.add(theBuff.toString());
        return theResult;
    }
    
    /** 
     * Splits the given string around the given separator and adds the resulting
     * substrings to the provided collection.
     * If either the string or the collection are null the method will do nothing.
     * 
     * @param    aString       The string to be converted.
     * @param    aSeparator    The separator to be used for splitting.
     * @param    aCollection   The collection to add the strings to.
     * @return   The collection passed in after adding the trimmed strings. The
     * collection remains unchanged if the string is null, empty or contains no
     * tokens. Null will returned if null was passed in as the collection.
     */
    public static Collection<String> addStringsToCollection(CharSequence aString, 
                                                    char aSeparator, 
                                                    Collection<String> aCollection) {
        if (aString != null && aCollection != null) {
            int    len       = aString.length();
            char[] tmp       = new char[len];
            int  i = 0;    // index into aString
            char c = 0;
            while (i < len)  {
                // Skip Blanks at start of String
                while (i < len) {
                    c = aString.charAt(i);
					if (c == aSeparator || !Character.isWhitespace(c)) {
                        break;
                    }
                    i++;
                }
                if (i >= len) { // only spaces till end of String
                    break;
                }
    
                if (c == aSeparator) {
                     // String started with separator e.g, ",aaa,bb,,c"
                    i++;
                    continue;
                }
                int  j = 0; // index into tmp
                tmp[0] = c;
                i++;
                while (i < len) {
                    c = aString.charAt(i);
                    if (c == aSeparator) {
                        break;
                    }
                    j++;
                    i++;
                    tmp[j] = c;
                }
                // forget trailing blanks
				while (Character.isWhitespace(tmp[j])) {
                    j--;
                }
                i++;    // Skip the Separator char
                aCollection.add(new String(tmp,0, j+1));
            }
        }
        return aCollection;
    }

    /**
     * Checks whether the given string contains characters which are contained by the given
     * pattern. So the pattern string defines the search characters.
     *
     * @param aSource
     *        the string to check
     * @param aPattern
     *        The characters to search
     * @return <code>true</code>, if the first string contains characters contained by
     *         the pattern string, <code>false</code> otherwise.
     */
    public static boolean containsChar(String aSource, String aPattern) {
        if (isEmpty(aSource) || isEmpty(aPattern)) {
            return false;
        }
        for (int i = 0, length = aPattern.length(); i < length; i++) {
            if (aSource.indexOf(aPattern.charAt(i)) >= 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether the given string contains only characters which are contained by the
     * given pattern. So the pattern string defines the allowed characters.
     *
     * @param aSource
     *            the string to check
     * @param allowedCharacters
     *            the allowed characters
     * @return <code>true</code>, if the first string contains only characters contained
     *         by the pattern string, <code>false</code> otherwise.
     */
    public static boolean containsOnlyChars(String aSource, String allowedCharacters) {
        if (isEmpty(aSource)) {
            return true;
        }
        if (isEmpty(allowedCharacters)) {
            return false;
        }
        for (int i = 0, length = aSource.length(); i < length; i++) {
            if (allowedCharacters.indexOf(aSource.charAt(i)) < 0) {
                return false;
            }
        }
        return true;
    }

	/**
	 * Counts the number of occurrences of the given character in the given string.
	 * 
	 * @param str
	 *        The string to analyze.
	 * @param ch
	 *        The character to count.
	 *
	 * @return The number of occurrences of the given character in the given string.
	 */
	public static int count(String str, char ch) {
		if (isEmpty(str)) {
			return 0;
		}

		int result = 0;
		int index = 0;
		while (true) {
			int next = str.indexOf(ch, index);
			if (next < 0) {
				return result;
			}
			result++;
			index = next + 1;
		}
	}

    /**
     * Counts, how many times the given pattern string occurs in the given source string.
     *
     * @param aSource
     *        the string to search for patterns
     * @param aPattern
     *        the string to search for
     * @return the amount of the given pattern that occur in the given source string
     */
    public static int count(String aSource, String aPattern) {
        return count(aSource, aPattern, 0);
    }

    /**
     * Counts, how many times the given pattern string occurs in the given source string.
     *
     * @param aSource
     *        the string to search for patterns
     * @param aPattern
     *        the string to search for
     * @param fromIndex
     *        the index of aSource from which to begin search
     * @return the amount of the given pattern that occur in the given source string
     */
    public static int count(String aSource, String aPattern, int fromIndex) {
        if (isEmpty(aPattern)) throw new IllegalArgumentException("aPattern must not be empty!");
        if (isEmpty(aSource)) return 0;
        int ammount = 0;
        int index = aSource.indexOf(aPattern, fromIndex);
        while (index >= 0) {
            ammount++;
            index = aSource.indexOf(aPattern, index + aPattern.length());
        }
        return ammount;
    }

	/**
	 * First index of <code>target</code> within <code>source</code>.
	 * 
	 * <p>
	 * This is the generalization of {@link String#indexOf(String)} to {@link CharSequence}.
	 * </p>
	 * 
	 * @param source
	 *        The characters to search in.
	 * @param target
	 *        The characters to search for.
	 * 
	 * @return The position relative to <code>sourceOffset</code> of the first match, or
	 *         <code>-1</code> if not found.
	 * 
	 * @see String#indexOf(String)
	 */
	public static int indexOf(CharSequence source, String target) {
		if (source instanceof String) {
			return ((String) source).indexOf(target);
		} else {
			return indexOf(source, target, 0);
		}
	}

	/**
	 * First index of <code>target</code> within <code>source</code>.
	 * 
	 * <p>
	 * This is the generalization of {@link String#indexOf(String)} to {@link CharSequence}.
	 * </p>
	 * 
	 * @param source
	 *        The characters to search in.
	 * @param target
	 *        The characters to search for.
	 * 
	 * @return The position relative to <code>sourceOffset</code> of the first match, or
	 *         <code>-1</code> if not found.
	 * 
	 * @see String#indexOf(String)
	 */
	public static int indexOf(char[] source, char[] target) {
		return indexOf(source, target, 0);
	}
	
	/**
	 * Index of <code>target</code> within <code>source</code> when starting the search at
	 * <code>fromIndex</code>.
	 * 
	 * <p>
	 * This is the generalization of {@link String#indexOf(String, int)} to {@link CharSequence}.
	 * </p>
	 * 
	 * @param source
	 *        The characters to search in.
	 * @param target
	 *        The characters to search for.
	 * @param fromIndex
	 *        The first index in the source to consider for the match.
	 * 
	 * @return The position relative to <code>sourceOffset</code> of the first match, or
	 *         <code>-1</code> if not found.
	 * 
	 * @see String#indexOf(String, int)
	 */
	public static int indexOf(CharSequence source, String target, int fromIndex) {
		if (source instanceof String) {
			return ((String) source).indexOf(target, fromIndex);
		} else {
			return indexOf(source, 0, source.length(), target, 0, target.length(), fromIndex);
		}
	}

	/**
	 * Index of <code>target</code> within <code>source</code> when starting the search at
	 * <code>fromIndex</code>.
	 * 
	 * <p>
	 * This is the generalization of {@link String#indexOf(String, int)} to {@link CharSequence}.
	 * </p>
	 * 
	 * @param source
	 *        The characters to search in.
	 * @param target
	 *        The characters to search for.
	 * @param fromIndex
	 *        The first index in the source to consider for the match.
	 * 
	 * @return The position relative to <code>sourceOffset</code> of the first match, or
	 *         <code>-1</code> if not found.
	 * 
	 * @see String#indexOf(String, int)
	 */
	public static int indexOf(char[] source, char[] target, int fromIndex) {
		return indexOf(source, 0, source.length, target, 0, target.length, fromIndex);
	}

	/**
	 * Searches within the range <code>[sourceOffset, sourceOffset + sourceCount)</code> of
	 * <code>source</code> for an occurrence of the character range
	 * <code>[targetOffset, targetOffset + targetCount)</code> of <code>target</code> starting at
	 * the position <code>fromIndex</code> (relative to <code>sourceOffset</code>).
	 * 
	 * @param source
	 *        The characters to search in.
	 * @param sourceOffset
	 *        The index of the first character to consider for a potential match.
	 * @param sourceCount
	 *        The number of characters starting at <code>sourceOffset</code> to consider for a
	 *        potential match.
	 * @param target
	 *        The characters to search for.
	 * @param targetOffset
	 *        The index of the first character to search for.
	 * @param targetCount
	 *        The number of characters in <code>target</code> to try to match agains
	 *        <code>source</code>.
	 * @param fromIndex
	 *        The first index in the source characters (the part of the source sequence described by
	 *        the source offset and source length) to consider for the match.
	 * 
	 * @return The position relative to <code>sourceOffset</code> of the first match, or
	 *         <code>-1</code> if not found.
	 */
	public static int indexOf(CharSequence source, int sourceOffset, int sourceCount,
			CharSequence target, int targetOffset, int targetCount, int fromIndex) {
		if (fromIndex >= sourceCount) {
			return (targetCount == 0 ? sourceCount : -1);
		}
		if (fromIndex < 0) {
			fromIndex = 0;
		}
		if (targetCount == 0) {
			return fromIndex;
		}
	
		char firstChar = target.charAt(targetOffset);
		int sourceStartMax = sourceOffset + (sourceCount - targetCount);
	
		for (int sourceStartPos = sourceOffset + fromIndex; sourceStartPos <= sourceStartMax; sourceStartPos++) {
			// Look for first character.
			if (source.charAt(sourceStartPos) != firstChar) {
				while (++sourceStartPos <= sourceStartMax && source.charAt(sourceStartPos) != firstChar) {
					// Just increment position.
				}
			}
	
			// Found first character, now look at the rest of target.
			if (sourceStartPos <= sourceStartMax) {
				int sourcePos = sourceStartPos + 1;
				int sourceStop = sourcePos + targetCount - 1;
				for (int targetPos = targetOffset + 1; sourcePos < sourceStop
					&& source.charAt(sourcePos) == target.charAt(targetPos); sourcePos++, targetPos++) {
					// Just increment position.
				}
	
				if (sourcePos == sourceStop) {
					// Found whole string.
					return sourceStartPos - sourceOffset;
				}
			}
		}
		return -1;
	}

	/**
	 * Searches within the range <code>[sourceOffset, sourceOffset + sourceCount)</code> of
	 * <code>source</code> for an occurrence of the character range
	 * <code>[targetOffset, targetOffset + targetCount)</code> of <code>target</code> starting at
	 * the position <code>fromIndex</code> (relative to <code>sourceOffset</code>).
	 * 
	 * @param source
	 *        The characters to search in.
	 * @param sourceOffset
	 *        The index of the first character to consider for a potential match.
	 * @param sourceCount
	 *        The number of characters starting at <code>sourceOffset</code> to consider for a
	 *        potential match.
	 * @param target
	 *        The characters to search for.
	 * @param targetOffset
	 *        The index of the first character to search for.
	 * @param targetCount
	 *        The number of characters in <code>target</code> to try to match agains
	 *        <code>source</code>.
	 * @param fromIndex
	 *        The first index in the source to consider for the match.
	 * 
	 * @return The position relative to <code>sourceOffset</code> of the first match, or
	 *         <code>-1</code> if not found.
	 */
	public static int indexOf(char[] source, int sourceOffset, int sourceCount,
			char[] target, int targetOffset, int targetCount, int fromIndex) {
		if (fromIndex >= sourceCount) {
			return (targetCount == 0 ? sourceCount : -1);
		}
		if (fromIndex < 0) {
			fromIndex = 0;
		}
		if (targetCount == 0) {
			return fromIndex;
		}

		char firstChar = target[targetOffset];
		int sourceStartMax = sourceOffset + (sourceCount - targetCount);

		for (int sourceStartPos = sourceOffset + fromIndex; sourceStartPos <= sourceStartMax; sourceStartPos++) {
			// Look for first character.
			if (source[sourceStartPos] != firstChar) {
				while (++sourceStartPos <= sourceStartMax && source[sourceStartPos] != firstChar) {
					// Just increment position.
				}
			}

			// Found first character, now look at the rest of target.
			if (sourceStartPos <= sourceStartMax) {
				int sourcePos = sourceStartPos + 1;
				int sourceStop = sourcePos + targetCount - 1;
				for (int targetPos = targetOffset + 1; sourcePos < sourceStop
					&& source[sourcePos] == target[targetPos]; sourcePos++, targetPos++) {
					// Just increment position.
				}

				if (sourcePos == sourceStop) {
					// Found whole string.
					return sourceStartPos - sourceOffset;
				}
			}
		}
		return -1;
	}

    /**
     * Escape a String according to standard Java conventions.
     *
     * @param str must not be null.
     */
    public static String escape(String str) {
        int len = str.length();

        StringBuilder retval = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char ch = str.charAt(i);
           switch (ch)
           {
              case 0 :
                 retval.append("\\0");
                 continue;
              case '\b':
                 retval.append("\\b");
                 continue;
              case '\t':
                 retval.append("\\t");
                 continue;
              case '\n':
                 retval.append("\\n");
                 continue;
              case '\f':
                 retval.append("\\f");
                 continue;
              case '\r':
                 retval.append("\\r");
                 continue;
              case '\"':
                 retval.append("\\\"");
                 continue;
              case '\'':
                 retval.append("\\\'");
                 continue;
              case '\\':
                 retval.append("\\\\");
                 continue;
              default: // use Unicode escape if non-ASCII
                 if ((ch = str.charAt(i)) < 0x20 || ch > 0x7e) {
                    String s = "0000" + Integer.toString(ch, 16);
                    retval.append("\\u" + s.substring(s.length() - 4, s.length()));
                 } else {
                    retval.append(ch);
                 }
           }
        }
        return retval.toString();
    }

    /**
     * Actual worker function for {@link #unEscape(String)}.
     *
     * @param len   expected length of Result.
     * @param from  position in String where encoding starts.
     * @param to    position in String where encoding ends.
     *
     * #author   kha
     */
    private static String unEscape(String str, int len, int from, int to) {
        StringBuilder retval = new StringBuilder(len);
        for (int i = from; i < to; ) {
            char ch = str.charAt(i++);
            if (ch != '\\') {
                retval.append(ch);
            }
            else {
                ch = str.charAt(i++);
                switch (ch)
                {
                case '0' :
                     retval.append('\0'); continue;
                case 'b':
                    retval.append('\b'); continue;
                case 't':
                    retval.append('\t'); continue;
                case 'n':
                    retval.append('\n'); continue;
                case 'f':
                    retval.append('\f'); continue;
                case 'r':
                    retval.append('\r'); continue;
                case '"':
                    retval.append('"');  continue;
                case '\'':
                    retval.append('\''); continue;
                case '\\':
                    retval.append('\\'); continue;
                case 'u':   // E.G. \u0123
                    String hex = str.substring(i, i+4);
                    i += 4;
                    retval.append((char) Integer.parseInt(hex, 16));
                    continue;
                default: // Invalid Escape, ignore it
                    retval.append('\\');
                    retval.append(ch);
                 }
            }
        }
        return retval.toString();
    }

    /**
     * Unescapes the given char of the given String.
     * replace each double anUnEscapeChar with anUnEscapeChar
     * and
     * replace each single aCharToBeUnescaped with anUnEscapeChar
     *
     * @param aString            to escape charachters, must not be null.
     * @param aCharToBeUnescaped which will be unescaped.
     * @return the unescaped String.
     */
    public static String unescape(String aString, char aCharToBeUnescaped){
        if(aString.indexOf(aCharToBeUnescaped) == -1 ){
            return aString;
        }
        int len = aString.length();
        StringBuilder buf = new StringBuilder(len);
        int i = 0;
        while (i < len) {
            char c = aString.charAt(i);
            if(aCharToBeUnescaped == c && i+1 < len) {
                char cc = aString.charAt(i+1);
                buf.append(cc);
                i += 2;
            }
            else {
                buf.append(c);
                i++;
            }
        }

        return buf.toString();
    }

    /**
     * Gets the first index of aChar in aString,
     * which is not escaped with anEscapeChar.
     *
     * @param aString to get the first index of aChar.
     * @param aChar to get the first index of it in aString,
     *              which is not escaped
     * @param anEscapeChar the escape char
     * @return the first index of the not escaped aChar in aString
     */
    public static int getFirstIndexOfNotEscapedChar(CharSequence aString,
                                                    char aChar,
                                                    char anEscapeChar) {
        int  len = aString.length();
        for(int i = 0; i < len; i++) {
            if(aString.charAt(i) == aChar) {
                if(i == 0 || aString.charAt(i - 1) != anEscapeChar){
                    return i;
                }
            }
        }

        return -1;
    }

    /**
     * Encode a String according to standard Java conventions.
     *
     * #author   kha
     */
    public static final String unEscape(String str) {
        int len = str.length();
        return unEscape(str, len, 0, len);
    }

    /**
     * Encode a String and remove Quotes around it.
     *
     * In fact the first and last character are ignored.
     *
     * #author   kha
     */
    public static final String unQuoteAndEscape(String str) {
        int len = str.length();
        return unEscape(str, len-2, 1, len-1);
    }

    /**
	 * This method cuts a string from the beginning to the given length if the
	 * string is longer.
	 * 
	 * @param aString
	 *            A string to cut. Null permitted.
	 * @param aLength
	 *            A length (> 0).
	 */
    public static String cutString(String aString, int aLength) {
		if (aString != null && aString.length() > aLength) {
			return aString.substring(0, aLength);
		}
		return aString;
	}

    /**
     * This method calls {@link #cutString(String, int, int, CharSizeMap)} with the
     * ProportionalCharSizeMap.
     */
    public static String cutString(String string, int rows, int columns) {
        return cutString(string, rows, columns, ProportionalCharSizeMap.INSTANCE);
    }


    /**
	 * Cuts the given string and adds line breaks between words so that the given string will fit in
	 * an area which contains the given number of rows and columns. Because not each character has
	 * the same size (depending on the used font), a {@link CharSizeMap} must be used to define the
	 * character size of each character.
	 * 
	 * <p>
	 * {@link #TAB_CHAR Tabs} are replaced by {@link #BLANK_CHAR blanks}, because the size of a tab
	 * can not be determined.
	 * </p>
	 * 
	 * @param string
	 *        the string to cut; may be <code>null</code> or empty
	 * @param rows
	 *        the maximum number of rows
	 * @param columns
	 *        the maximum number of characters in a row (respecting the CharSizeMap)
	 * @param charSizeMap
	 *        {@link CharSizeMap} to determine the size of each character
	 * @return the cut string which will fit in an area which contains the given number of rows and
	 *         columns. In the resulting string spaces may be replaced by line breaks.
	 * 
	 * @see FontCharSizeMap
	 */
    public static String cutString(String string, int rows, int columns, CharSizeMap charSizeMap) {
        if (isEmpty(string)) return string;
        if (rows == 0 || columns == 0) return EMPTY_STRING;
        double rowSize = 0;
        double wordSize = 0;
        int rowCount = 1;
        StringBuilder buffer = new StringBuilder(string.length());
        StringBuilder wordBuffer = new StringBuilder(32);
        
        boolean firstWord = true; boolean autoBreak = false;
        
        for (int i = 0, length = string.length(); i < length; i++) {
            char character = string.charAt(i);
            if (character == TAB_CHAR) character = BLANK_CHAR;
            double charaterSize = charSizeMap.getSize(character);
            
            if (character == LINE_BREAK_CHAR) {
                if (firstWord) firstWord = false;
                else buffer.append(BLANK_CHAR);
                buffer.append(wordBuffer.toString());
                wordBuffer = new StringBuilder(32);
                wordSize = 0;
                rowSize = 0;
                rowCount++;
                if (rowCount > rows) break;
                buffer.append(LINE_BREAK_CHAR);
                firstWord = true;
                autoBreak = false;
            }
            else if (character == BLANK_CHAR) {
                if (rowSize == 0 && autoBreak) continue; // suppress multiple spaces on line break
                rowSize += charaterSize;
                if (rowSize > columns) {
                    if (firstWord) firstWord = false;
                    else buffer.append(BLANK_CHAR);
                    buffer.append(wordBuffer.toString());
                    wordBuffer = new StringBuilder(32);
                    wordSize = 0;
                    rowCount++;
                    rowSize = 0;
                    if (rowCount > rows) break;
                    buffer.append(LINE_BREAK_CHAR);
                    firstWord = true;
                    autoBreak = true;
                }
                else {
                    if (firstWord) firstWord = false;
                    else buffer.append(BLANK_CHAR);
                    buffer.append(wordBuffer.toString());
                    wordBuffer = new StringBuilder(32);
                    wordSize = 0;
                }
            }
            else {
                wordSize += charaterSize;
                rowSize += charaterSize;
                if (rowSize > columns) {
					if (wordSize >= rowSize) {
                        buffer.append(wordBuffer.toString());
                        wordBuffer = new StringBuilder(32);
                        wordSize = charaterSize;
                    }
                    rowCount++;
                    rowSize = wordSize;
                    if (rowCount > rows) break;
                    buffer.append(LINE_BREAK_CHAR);
                    firstWord = true;
                    autoBreak = true;
                }
                wordBuffer.append(character);
            }
        }
        if (wordBuffer.length() > 0) {
            if (!firstWord) buffer.append(BLANK_CHAR);
            buffer.append(wordBuffer);
        }
        return buffer.toString();
    }



    /**
     * This method is like {@link #cutString(String, int, int, CharSizeMap)} with the
     * difference, that line breaks will not replace spaces between words but be inserted additional.
     */
    public static String cutStringKeepSpaces(String string, int rows, int columns, CharSizeMap charSizeMap) {
        if (isEmpty(string)) return string;
        if (rows == 0 || columns == 0) return EMPTY_STRING;
        double rowSize = 0;
        double wordSize = 0;
        int rowCount = 1;
        StringBuilder buffer = new StringBuilder(string.length());
        StringBuilder wordBuffer = new StringBuilder(32);
        boolean stopped = false;
        
        for (int i = 0, length = string.length(); i < length; i++) {
            char character = string.charAt(i);
            if (character == LINE_BREAK_CHAR) {
                rowCount++;
                rowSize = 0;
                if (rowCount > rows) {
                    break;
                }
            }
            else {
                double charaterSize = charSizeMap.getSize(character);
                wordSize += charaterSize;
                rowSize += charaterSize;
                if (rowSize > columns) {
                    rowCount++;
                    boolean doContinue = false;
                    if (wordSize > columns) {
                        buffer.append(wordBuffer.toString());
                        // Reset word 
                        wordBuffer = new StringBuilder(32);
                        if (character == BLANK_CHAR) {
                            buffer.append(character);
                            wordSize = 0;
                            doContinue = true;
                        }
                        else {
                            wordSize = charaterSize;
                        }
                    }
                    else if (character == BLANK_CHAR) {
                        wordBuffer.append(character);
                        buffer.append(wordBuffer.toString());
                        // Reset word 
                        wordBuffer = new StringBuilder(32);
                        wordSize = 0;
                        doContinue = true;
                    }
                    rowSize = wordSize;
                    if (rowCount > rows) {
                        stopped = true;
                        break;
                    }
                    buffer.append(LINE_BREAK_CHAR);
                    if (i + 1 < length && string.charAt(i + 1) == LINE_BREAK_CHAR) {
                        i++; // ignore next line break
                    }
                    if (doContinue) continue;
                }
            }
            
            wordBuffer.append(character);
            if (character == BLANK_CHAR || character == '-' || character == LINE_BREAK_CHAR) {
                buffer.append(wordBuffer.toString());
                // Reset word 
                wordBuffer = new StringBuilder(32);
                wordSize = 0;
            }
        }
        
        if (!stopped) {
            buffer.append(wordBuffer.toString());
        }
        return buffer.toString();
    }


    /**
     * Reduce the given string, so that it has as maximum the given length.
     *
     * Depending on the length of the given string this method returns the
     * given string of cut a part from the middle of the string and replace
     * it with "..." until the requested length has been reached.
     *
     * @param    aString       The string to be minimized.
     * @param    aMaxLength    The maximum length of the returned string,
     *                         this value has to be larger than 3, otherwise
     *                         the original string will be returned.
     * @return   The string, which has maximum the given length.
     */
    public static String minimizeString(String aString, int aMaxLength) {
        return (minimizeString(aString, aMaxLength, aMaxLength >> 2));
    }

    /**
     * Reduce the given string, so that it has as maximum the given length.
     *
     * Depending on the length of the given string this method returns the
     * given string of cut a part from the middle of the string and replace
     * it with "..." until the requested length has been reached.
     *
     * @param    aString       The string to be minimized.
     * @param    aMaxLength    The maximum length of the returned string,
     *                         this value has to be larger than 3, otherwise
     *                         the original string will be returned.
     * @param    aStartPos     The position to start with cutting off
     *                         characters from the string.
     * @return   The string, which has maximum the given length.
     */
    public static String minimizeString(String aString,
                                        int aMaxLength,
                                        int aStartPos) {
        if (aString == null) {
            return aString; // GIGO (Garbage In -> Garbage Out)
        }


        int len = aString.length();
        if ((len > aMaxLength) && (aMaxLength > 3)
         && (aStartPos >= 0)   && (aStartPos <= (aMaxLength - 3))) {
            int theMax = len - (aMaxLength - (aStartPos + 3));

            aString = aString.substring(0, aStartPos) + "..." +
                      aString.substring(theMax);
        }

        return (aString);
    }

	/**
	 * Tests whether the given {@link String} (is non-empty and) starts with the
	 * given character.
	 * 
	 * @param s
	 *        The string to check.
	 * @param ch
	 *        The potential start character.
	 * @return Whether the given string starts with the given character.
	 */
	public static boolean startsWithChar(CharSequence s, char ch) {
		int length = s.length();
		return length > 0 && s.charAt(0) == ch;
	}

	/**
	 * Tests whether the given {@link String} (is non-empty and) ends with the
	 * given character.
	 * 
	 * @param s
	 *        The string to check.
	 * @param ch
	 *        The potential last character.
	 * @return Whether the given string ends with the given character.
	 */
	public static boolean endsWithChar(CharSequence s, char ch) {
		int length = s.length();
		return length > 0 && s.charAt(length - 1) == ch;
	}

	/**
	 * Converts the given string into a non-empty string or <code>null</code>.
	 * 
	 * @param input
	 *        The source string.
	 * @return Either the given string, or <code>null</code>, if the given string
	 *         {@link #isEmpty(CharSequence)}.
	 */
	public static String nonEmpty(String input) {
		return StringServices.isEmpty(input) ? null : input;
	}

	/**
	 * Checks that the specified {@link CharSequence} reference is neither {@code null} nor empty.
	 * This method is designed primarily for doing parameter validation in methods and constructors,
	 * as demonstrated below: <blockquote>
	 * 
	 * <pre>
	 * public Foo(String bar) {
	 * 	_bar = StringServices.requireNonEmpty(bar);
	 * }
	 * </pre>
	 * 
	 * </blockquote>
	 *
	 * @param seq
	 *        The {@link CharSequence} reference to check for emptiness.
	 * @param <T>
	 *        the type of the reference.
	 * @return {@code seq} if neither {@code null} nor empty.
	 * @throws IllegalArgumentException
	 *         if {@code seq} is {@code null} or empty.
	 */
	public static <T extends CharSequence> T requireNotEmpty(T seq) {
		if (StringServices.isEmpty(seq)) {
			throw new IllegalArgumentException("Value must neither be null or empty.");
		}
		return seq;
	}

	/**
	 * Checks that the specified {@link CharSequence} reference is neither {@code null} nor empty
	 * and throws a customized {@link IllegalArgumentException} if it is. This method is designed
	 * primarily for doing parameter validation in methods and constructors with multiple
	 * parameters, as demonstrated below: <blockquote>
	 * 
	 * <pre>
	 * public Foo(String bar, String baz) {
	 * 	_bar = StringServices.requireNonEmpty(bar, "bar must not be null or empty");
	 * 	_baz = StringServices.requireNonEmpty(baz, "baz must not be null or empty");
	 * }
	 * </pre>
	 * 
	 * </blockquote>
	 *
	 * @param seq
	 *        the object reference to check for nullity The {@link CharSequence} reference to check
	 *        for emptiness.
	 * @param message
	 *        detail message to be used in the event that a {@code
	 *                IllegalArgumentException} is thrown
	 * @param <T>
	 *        the type of the reference
	 * @return {@code seq} if neither {@code null} nor empty.
	 * @throws IllegalArgumentException
	 *         if {@code seq} is {@code null} or empty.
	 */
	public static <T extends CharSequence> T requireNotEmpty(T seq, String message) {
		if (StringServices.isEmpty(seq)) {
			throw new IllegalArgumentException(message);
		}
		return seq;
	}

	/**
	 * @deprecated Better directly check for <code>null</code>.
	 */
	@Deprecated
	public static final boolean isEmpty(ResKey key) {
		return key == null;
	}

    /**
     * Checks if an object is <code>null</code> or an empty string.
     *
     * @param aObject
     *            the object to check
     * @return <code>true</code>, if the object is <code>null</code> or an empty string
     */
    public static final boolean isEmpty(Object aObject) {
        return (aObject == null || (aObject instanceof CharSequence && ((CharSequence)aObject).length() == 0));
    }

    /**
     * Checks, whether two strings are both <code>null</code> or one equal to the other.
     * The parameter are objects so that this method can be used for other objects, too and
     * to avoid annoying casting before calling this method.
     *
     * @return <code>true</code>, if both strings are <code>null</code>, or one is
     *         equal to the other.
     */
    public static boolean equals(Object s1, Object s2) {
        return ((s1 == null) && (s2 == null)) || ((s1 != null) && (s1.equals(s2)));
    }

	/**
	 * Compare two {@link CharSequence}s for equal contents.
	 * 
	 * <p>
	 * The interface {@link CharSequence} does not require to override {@link Object#equals(Object)}
	 * to compare for content equality.
	 * </p>
	 * 
	 * @param s1
	 *        The first sequence to compare.
	 * @param s2
	 *        The second sequence to compare.
	 * @return Whether both {@link CharSequence}s have equal contents.
	 */
	public static boolean equalsCharSequence(CharSequence s1, CharSequence s2) {
		if (s1 == s2) {
			return true;
		}

		if (s1 == null || s2 == null) {
			return false;
		}

		if (s1.length() != s2.length()) {
			return false;
		}

		for (int n = 0, cnt = s1.length(); n < cnt; n++) {
			if (s1.charAt(n) != s2.charAt(n)) {
				return false;
			}
		}

		return true;
	}

    /**
     * Checks, whether two strings are both empty or one equal to
     * the other.
     *
     * @return <code>true</code>, if both strings are empty,
     *         or one is equal to the other.
     */
    public static boolean equalsEmpty(Object o1, Object o2) {
        boolean e1 = isEmpty(o1);
        return e1 && isEmpty(o2) || (!e1 && o1.equals(o2));
    }

	/**
	 * Return empty String for null String.
	 * 
	 * @param aString
	 *        a String to be checked
	 * @return EMPTY_STRING when aString was null, aString otherwise
	 * 
	 * @deprecated Use {@link #nonNull(String)}.
	 */
    @Deprecated
	public static String getEmptyString (String aString) {
		return nonNull(aString);
    }

	/**
	 * Return null String for empty String.
	 * 
	 * @param aString
	 *        a String to be checked
	 * @return null when aString was empty, aString otherwise
	 * 
	 * @deprecated Use {@link #nonEmpty(String)}.
	 */
    @Deprecated
	public static String getNullString(String aString) {
		return nonEmpty(aString);
    }

    /**
     * Check if a String array is empty, i.e. null or no elements.
     *
     * @param   anArray    The array to be checked
     * @return  true, if the array is empty, i.e. null or no elements.
     */
    public static boolean isEmpty (String [] anArray) {
        return ((anArray == null) || (anArray.length == 0));
    }

    /**
     * Check if a String array is empty, i.e. null or no elements.
     *
     * @param   anArray The array to be checked.
     * @param   part    a String that may be found in the Array.    
     * @return  true when at least one element of anArray equals part.
     */
    public static boolean contains (String [] anArray, String part) {
        if (anArray != null) {
            int len =  anArray.length;
            for (int i = 0; i < len; i++) {
                if (anArray[i].equals(part)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * This method converts the given string into a valid filename. 
     * If a non-valid character is found in the string, it will be 
     * replaced by the given replacement character.
     * 
     * @param aString
     *        A string. Must not be <code>null</code>.
     * @param aReplaceChar
     *        A replacement chart. Must not be <code>null</code>.
     */
    public static String toFileName(String aString, char aReplaceChar) {
        char[] notAllowedChars = new char[] {'<', '>', '?', '"', ':', '|', '\\', '/', '*'};
        
        StringBuilder buffer = new StringBuilder(aString.length());
        for (int i = 0; i < aString.length(); i++) {
            char    theChar   = aString.charAt(i);
            boolean isAllowed = true;
            for (int j = 0; j < notAllowedChars.length; j++) {
                if (theChar == notAllowedChars[j]) {
                    isAllowed = false;
                    break;
                }
            }
            
            buffer.append(isAllowed ? theChar : aReplaceChar);
        }

        return buffer.toString();
    }
    
    /**
     * Convert a String to a valid file name.
     *
     * Only letters, digits, '_' and '.' are allowed,
     * all other chars are replaced by their hex representation.
     *
     * @param aString   the string to be converted (must not be <code>null</code>)
     * @param len       aString.length()
     * @param startAt   Characters upt to this index do not need conversion.
     *
     * @return  a String containing only chars valid in file names.
     */
    protected static String cleanName(String aString, int len, int startAt) {

        // in average this schould suffice
        StringBuilder theResult = new StringBuilder(len);
        // copy clean chars
        theResult.append(aString.substring(0, startAt));
        // handle rest
        for (int i=startAt; i < len; i++) {
            char theChar = aString.charAt(i);
            if (Character.isUnicodeIdentifierPart(theChar)) {
                theResult.append(theChar);
            } else {
                theResult.append('_');
            }
        }
        return theResult.toString();
    }

    /**
     * Cleanup name by replacing anything that is not Letter or digit by '_'.
     *
     * @param aString the string to be converted (must not be <code>null</code>)
     *
     * @return  a String containing only Letters,Digits or '_'
     */
    public static String cleanName(String aString) {
        final int len = aString.length();

        for (int i=0 ; i < len; i++) {
            char theChar = aString.charAt(i);
            if (!Character.isUnicodeIdentifierPart(theChar)) {
                return cleanName(aString, len, i);
            }
        }
        // no "illegal" characters, fine
        return aString;
    }

    /**
     *  Cleanup name so that it will not harm inside HTML.
     *
     * @param aString   the string to be converted (must not be <code>null</code>)
     * @param len       aString.length()
     * @param startAt   Characters upt to this index do not need conversion.
     *
     * @return  a String containing no '&lt;' or '&gt;'
     */
    protected static String cleanHTMLName(String aString, int len, int startAt) {

        // in average this schould suffice
        StringBuilder theResult = new StringBuilder(len + 10);
        // copy clean chars
        theResult.append(aString.substring(0, startAt));
        // handle rest
        for (int i=startAt; i < len; i++) {
            char theChar = aString.charAt(i);
            if (theChar == '<') {
                continue;   // just drop it
            } if (theChar == '>') {
                continue;
            } // else {
            theResult.append(theChar);
        }
        return theResult.toString();
    }

    /**
     * Cleanup name so that it will not harm inside HTML.
     *
     * @param aString the string to be converted, null will result in EMPTY_STRING
     *
     * @return  a String containing no '&lt;' or '&gt;'
     */
    public static String cleanHTMLName(String aString) {
    	if(isEmpty(aString)){
    		return EMPTY_STRING;
    	}
        final int len = aString.length();

        for (int i=0 ; i < len; i++) {
            char theChar = aString.charAt(i);
            if (theChar == '<' || theChar == '>') {
                return cleanHTMLName(aString, len, i);
            }
        }
        // no "illegal" characters, fine
        return aString;
    }

    /** Create any Object that has a CTor with String.
     * 
     * @param value value of the object as String
     * @param cName classanme of desired Object
     * @return an object of the desired class by using a default CTor.
     * 
     * @throws Exception all sorts of evil things that can happen with 
     *          reflection.
     */ 
    public static Object getValueWithClass(String value, String cName) 
        throws Exception
    {
        Class<?> cl = Class.forName(cName);
        if (cl == String.class) // shortcut Strings
            return value;
        Constructor<?> ctor = cl.getConstructor(STRING_PARAM);
        
        return ctor.newInstance(new Object[] { value });
    }

    /**
     * Return an empty String in case the given one is null
     *
     * @deprecated duplicated method: use {@link #getEmptyString(String)} instead.
     */
    @Deprecated
	public static final String checkOnNull(String aString) {
         return getEmptyString(aString);
    }

    /** Trim the given String , when null return an empty String 
     */
    public static final String checkOnNullAndTrim(String aString) {
         if (aString == null)
             return EMPTY_STRING;

         return aString.trim();
    }

    /**
     * <code>null</code> safe version of {@link String#trim()}.
     */
    public static final String trim(String aString) {
        return aString == null ? null : aString.trim();
    }

    /**
     * <code>null</code> safe version of {@link Object#toString()}.
     */
    public static final String toString(Object aObject) {
        return aObject == null ? EMPTY_STRING : aObject.toString();
    }

    /**
     * Converts a collection of objects into a string.
     * The toString method of the objects in the collection 
     * is used to get a representation. The Collection 
     * is allowed to contain <code>null</code>.
     * 
     * @param aCollection a collection of objects 
     * @return a string containing all objects in the collection
     */
    public static String toString(Collection<?> aCollection) {
        return toString(aCollection, ", ");
    }

    /**
     * Converts a collection of objects into a string.
     * The toString method of the objects in the collection 
     * is used to get a representation. The Collection 
     * is allowed to contain <code>null</code>.
     *
     * @param aCollection  a collection of objects 
     * @param aSeparator   a string to separation the string representations 
     * @return a string containing all objects in the collection
     */
    public static String toString(Collection<?> aCollection, String aSeparator) {
        if (aCollection instanceof RandomAccess) {
            return toString((List<?>) aCollection, aSeparator);
        }
        if (aCollection == null || aCollection.isEmpty()) {
    		return EMPTY_STRING;
    	}
        return toString(aCollection.iterator(), aSeparator);
    }

    /**
     * Converts a List of objects into a string.
     * The toString method of the objects in the collection 
     * is used to get a representation. The List
     * is allowed to contain <code>null</code>.
     * 
     * @param aList        an arbitrary list.
     * @param aSeparator   a string to separate the string representations 
     * 
     * @return a string containing all objects in the collection as Strings.
     */
    public static String toString(List<?> aList, String aSeparator) {
        int size = aList.size();
        if (size == 0) {
            return EMPTY_STRING;
        }
        StringBuilder theResult = new StringBuilder(size * 10);
        theResult.append(aList.get(0));
        for (int i=1; i < size; i++) {
            theResult.append(aSeparator).append(aList.get(i));
        }
        return theResult.toString();
    }

    /**
	 * Converts a sequence of objects into a string.
	 * 
     * The toString method of the objects in the collection 
     * is used to get a representation. The Collection 
     * is allowed to contain <code>null</code>.
     * 
     * @param theIt        an arbitrary Iterator.
     * @param aSeparator   a string to separate the string representations 
     * 
     * @return a string containing all objects in the sequence as Strings.
	 */
	public static String toString(Iterator<?> theIt, String aSeparator) {
	    StringBuilder theResult = new StringBuilder();

		if (theIt.hasNext()) {
            theResult.append(theIt.next());
	        while (theIt.hasNext()) {
        		theResult.append(aSeparator).append(theIt.next());
	        }
		}
		
        return theResult.toString();
	}

    /**
     * Converts a collection of objects into a string.
     * The given formatter is used to get a representation of the objects within 
     * the collection. The Collection <em>must not</em> contain <code>null</code>.
     * 
     * @param    aCollection  A collection of objects, must not be <code>null</code>.
     * @param    aSeparator   A string to separation the string representations. 
     * @param    aFormatter   A formatter to format the objects from the collection, must not be <code>null</code>.
     * @return   A string containing all objects in the collection.
     */
    public static String toString(Collection<?> aCollection, String aSeparator, Format aFormatter) {

        if (!aCollection.isEmpty()) {
            StringBuilder theResult = new StringBuilder(aCollection.size() << 5);

            Iterator<?> theIt = aCollection.iterator();
            theResult.append(aFormatter.format(theIt.next()));

            while (theIt.hasNext()) {
                theResult.append(aSeparator)    
                         .append(aFormatter.format(theIt.next()));
            }
            return theResult.toString();
        }

        return EMPTY_STRING;
    }
    
    /**
     * Convert a Map to a String like "key:value;anotherKey:anotherValue";
     * The Map is allowed to contain <code>null</code> as key or value.
     */
    public static String toString(Map<?,?> aMap) {
        return toString(aMap, ',', ':');
    }

    /**
     * Convert a Map to a String like "key:value;anotherKey:anotherValue";
     * The Map is allowed to contain <code>null</code> as key or value.
     */
    public static <KEY,VAL> String toString(Map<KEY,VAL> aMap, char aSeparator, char aKeyValueSepartor) {
        if (!aMap.isEmpty()) {
            StringBuilder theResult = new StringBuilder(aMap.size() << 5); // * 32
            Iterator<Entry<KEY,VAL>> theIt = aMap.entrySet().iterator();
            Entry<KEY,VAL> theEntry = theIt.next();
            theResult.append(theEntry.getKey())
                     .append(aKeyValueSepartor)
                     .append(theEntry.getValue());
            while (theIt.hasNext()) {
                theResult.append(aSeparator);
                theEntry = theIt.next();
                theResult.append(theEntry.getKey())
                         .append(aKeyValueSepartor)
                         .append(theEntry.getValue());
            }
            return theResult.toString();
        } else {
        	return EMPTY_STRING;
        }
    }
    
    /**
	 * Replace all occurrences of the given string to search with the given
	 * replacement string.
	 * 
	 * @param target
	 *     The string in which the replacement should take place.
	 * @param stringToSearch
	 *     The String that is searched in the source string.
	 * @param replacementString
	 *     The string that is used as replacement for the searched
	 *     character.
	 * @return A string with all occurrences of the given character replaced
	 *     with the replacement string.
	 */
	public static String replace(String target, String stringToSearch, String replacementString) {
		int index = target.indexOf(stringToSearch);
		if (index < 0) return target;
		
		if(!stringToSearch.equals("")) {
			StringBuilder result = new StringBuilder(target.length());
			int lastIndex = 0;
			while (index >= 0) {
				result.append(target.substring(lastIndex, index));
				result.append(replacementString);
				lastIndex = index + stringToSearch.length();
				
				index = target.indexOf(stringToSearch, lastIndex);
			}
			result.append(target.substring(lastIndex));
			return result.toString();
		}
		else if(!replacementString.equals("")) {
			StringBuilder result = new StringBuilder(Math.max(1, target.length()));
			
			if(!target.equals("")) {
				result.append(target.substring(0, 1));
				for(int i = 1, length = target.length(); i < length; i++) {
					result.append(replacementString);
					result.append(target.substring(i, i + 1));
				}
			}
			else {
				result.append(replacementString);
			}
			return result.toString();
		}
		
		return target;
	}
	
    /**
	 * Replace all occurences of the given character to search with the given
	 * replacement string.
	 * 
	 * @param target
	 *     The string in which the replacement should take place.
	 * @param charToSearch
	 *     The character that is searched in the source string.
	 * @param replacementString
	 *     The string that is used as replacement for the searched
	 *     character.
	 * @return A string with all occurrences of the given character replaced
	 *     with the replacement string.
	 */
	public static String replace(String target, char charToSearch, String replacementString) {
		String theSearchString = String.valueOf(charToSearch);
		return replace(target, theSearchString, replacementString);
	}

    /**
     * Cuts leading chars from the given string.
     *
     * @param aChar
     *        the leading chars to cut
     * @param aString
     *        the string to check
     * @return the given string without leading given chars
     */
    public static String cutLeading(char aChar, String aString) {
        if (aString == null) return aString;
        int index = 0, length = aString.length();
        for (;index < length; index++) {
            if (aString.charAt(index) != aChar) break;
        }
        return index < length ? aString.substring(index) : EMPTY_STRING;
    }

    /**
     * Cuts trailing chars from the given string.
     *
     * @param aChar
     *        the trailing chars to cut
     * @param aString
     *        the string to check
     * @return the given string without trailing given chars
     */
    public static String cutTrailing(char aChar, String aString) {
        if (aString == null) return aString;
        int length = aString.length();
        int index = length - 1;
        for (;index >= 0; index--) {
            if (aString.charAt(index) != aChar) break;
        }
        return index >=0 ? aString.substring(0, index + 1) : EMPTY_STRING;
    }

    /**
     * Cuts leading and trailing chars from the given string.
     *
     * @param aChar
     *        the leading and trailing chars to cut
     * @param aString
     *        the string to check
     * @return the given string without leading and trailing given chars
     */
    public static String trim(char aChar, String aString) {
        return cutTrailing(aChar, cutLeading(aChar, aString));
    }

    /**
	 * Removes leading and tailing white space and replaces successive white space (' ', '\t', '\f',
	 * '\n', and '\r') with a single space character.
	 * <p>
	 * See {@link RegExpUtil#normalizeWhitespace(String)} for a non-trimming variant that
	 * additionally handles unicode characters.
	 * </p>
	 * 
	 * @param s
	 *        the string to normalize
	 * @return the given string with normalized white space.
	 */
    public static String normalizeWhiteSpace(String s) {
        if (s == null) {
            return null;
        }
        
        // Whether white space should be skipped. 
        boolean skip = true;
        
        int inputLength = s.length();

        // Check, whether string is already in normalized form.
        for (int readPos = 0; readPos < inputLength; readPos++) {
            char currentChar = s.charAt(readPos);
            
			switch (currentChar) {
            case BLANK_CHAR: {
            	if (skip) {
            		// Skip additional white space.
            		return internalNormalize(s, readPos, skip);
            	} else {
            		// First white space is kept.
            		skip = true;
            	}
            	break;
            }
            
            case '\t':
            case '\f':
            case LINE_BREAK_CHAR:
            case '\r': {
            	return internalNormalize(s, readPos, skip);
            }
            
            default: {
            	skip = false;
            	break;
            }
            }
        }
        
		if ((inputLength > 0) && (s.charAt(inputLength - 1) == BLANK_CHAR)) {
        	return s.substring(0, inputLength - 1);
        }
		
		return s;
    }

    /**
	 * Helper for {@link #normalizeWhiteSpace(String)} that performs the actual
	 * normalization, if the input string is not already in normalized form.
	 * 
	 * @param s
	 *        the string to normalize
	 * @param readPos
	 *        the index in the input string that has to be checked first. All
	 *        characters before this position are already in normalized from.
	 * @param skip
	 *        whether some white space character was seen before the given
	 *        initial position.
	 *        
	 * @return the normalized version of the given input string.
	 */
    private static String internalNormalize(String s, int readPos, boolean skip) {
        int inputLength = s.length();
        StringBuilder result = new StringBuilder(inputLength);
		
		result.append(s.substring(0, readPos));
		
        for (; readPos < inputLength; readPos++) {
            char currentChar = s.charAt(readPos);
			switch (currentChar) {
            case BLANK_CHAR:
            case '\t':
            case '\f':
            case LINE_BREAK_CHAR:
            case '\r': {
            	if (skip) {
            		// Skip additional white space.
            	} else {
            		// First white space seen, replace with space.
            		result.append(BLANK_CHAR);
            		skip = true;
            	}
            	break;
            }
            default: {
            	result.append(currentChar);
            	skip = false;
            	break;
            }
            }
        }
        
        int resultLength = result.length();
		if ((resultLength > 0) && (result.charAt(resultLength - 1) == BLANK_CHAR)) {
        	result.setLength(resultLength - 1);
        }
        
        return result.toString();
    }
	
	/**
	 * Fills the string up to a given length using a character.
	 * 
	 * @param s the input string that is filled up
	 * @param length the length that the string should be filled up to, if smaller or equal to the input string 
	 * then the input string is returned
	 * @param fillChar used to fill up the string 
	 * @param startPosition either {@link #START_POSITION_HEAD} or {@link #START_POSITION_TAIL}
	 * @return the filled up string
	 */
	public static String fillString(String s, int length, char fillChar, int startPosition){
		if (length <= s.length()){
			return s;
		}
		char ch[] = new char[length - s.length()];
		Arrays.fill(ch,fillChar);
		
		if (startPosition == START_POSITION_HEAD){
			return new String(ch) + s;
		}else{ // START_POSITION_TAIL 
			return s + new String(ch);
		}
	}

	/** 
     * Creates a string which is the amount-fold concatenation of the given string.
     * Example: repeadString("a", 3) returns "aaa".
     *
     * @param s
     *        the string to repead
     * @param amount
     *        the amount of concatenations
     * @return a string which is the amount-fold concatenation of the given string
     */
	public static String repeadString(String s, int amount) {
	    if (s == null) return null;
	    StringBuilder sb = new StringBuilder(s.length() * amount);
	    for (int i = 0; i < amount; i++)
	        sb.append(s);
	    return sb.toString();
	}

	/**
	 * Strip fill chars off a string.
	 * If the string is <code>null</code> or empty or
	 * consists only of fill chars
	 * the given anEmptyString will be returned
	 * 
	 * @param aString			the String
	 * @param fillChar	the fill char
	 * @param startPosition either {@link #START_POSITION_HEAD} or {@link #START_POSITION_TAIL}
	 * @param anEmptyString the string to return if there are only fill chars
	 * @return the string as defined above
	 */
	public static String stripString(String aString, char fillChar, int startPosition, String anEmptyString){
		int theLen = (aString != null) ? aString.length() : 0;
		if (theLen < 1){
			return anEmptyString;
		}

		boolean foundOtherChar = false;
		int i   = (startPosition == START_POSITION_HEAD) ? 0 : theLen-1;
		int inc = (startPosition == START_POSITION_HEAD) ? 1 : -1; 
		while (i<=theLen-1 && i>= 0 && !foundOtherChar) {
			foundOtherChar = aString.charAt(i) != fillChar;
			if (!foundOtherChar) {
				i += inc;
			}
		}
		
		String theRes = (startPosition == START_POSITION_HEAD) ? aString.substring(i) : aString.substring(0, i+1);
		if (theRes.length() == 0) {
			return anEmptyString;
		}
		
		return theRes;
	}

	/** Null safe {@link String#strip()}. */
	public static String stripNullsafe(String nullable) {
		if (nullable == null) {
			return null;
		}
		return nullable.strip();
	}

    /**
     * Return a String with given numSpaces number of spaces.
     * 
     * @param numSpaces there is no limit check here, take care ...
     */
    public static String getSpaces(int numSpaces){
        synchronized (spaces) {
            while (spaces.length() < numSpaces) {
                spaces.append(spaces);
            }
            return spaces.substring(0, numSpaces);
        }
    }

    /**
     * This method returns a random string with given the length. 
     * 
     * The string will contain only the following characters:
     *
     * a..Z, A..Z and 0..9.
     *
     * @param aLength
     *        A length (0..n).
     * @return Returns a random string.
     */
    public static String getRandomString(int aLength) {
        return getRandomString(new Random(), aLength);
    }
    
    /**
     * This method returns a random string with given the length. 
     * 
     * The string will contain only the following characters:
     *
     * a..Z, A..Z and 0..9.
     *
     * @param aLength
     *        A length (0..n).
     * @return Returns a random string.
     */
    public static String getRandomString(Random aRand, int aLength) {
        StringBuilder buffer  = new StringBuilder(aLength);
        int          counter = 0;
        while (counter < aLength) {
            char value = (char)('0' + aRand.nextInt(75));
            
            if (!Character.isLetter(value) && !Character.isDigit(value)) { continue; }
            
            buffer.append(value);
            counter++;
        }
        return buffer.toString();
    }

    /**
     *  This method returns a random string with pseudo Words including special characters.
     */
    public static String getRandomWords(int aLength) {
        return getRandomWords(new Random(), aLength);
    }
    
    /**
     * This method returns a random string with pseudo Words including special characters.
     */
    public static String getRandomWords(Random aRand, int aLength) {
        StringBuilder buffer  = new StringBuilder(aLength);
        int          counter = 0;
        while (counter < aLength) {
            
            int nextWord = counter + aRand.nextInt(2 + aRand.nextInt(10));
            buffer.append((char)('A' + aRand.nextInt(26)));  
            counter++;
            while (counter < nextWord && counter < aLength) {
                char value = (char)('a' + aRand.nextInt(30));  //  1 + '~' - 'a'; 
                if (!Character.isLetter(value) && !Character.isDigit(value)) { continue; }
                buffer.append(value);
                counter++;
            }
            if (counter < aLength && aRand.nextInt(100) < 20) {
                buffer.append((char)(BLANK_CHAR + aRand.nextInt(95)));  // 1 + '~' - ' ';
                counter++;
            }
            if (counter < aLength) {
                buffer.append(BLANK_CHAR);
                counter++;
            }
        }
        return buffer.toString();
    }

    /** 
     * Convert a time definition to milliseconds.
     * 
     * This time definition has the following format: [0..9]*[y | M | d | h | m | s ]
     * IF a diffrent parameter is given, {@link IllegalArgumentException}
     * will be thrown.
     * 
     * @param    aString The string to be converted to a long. If null number 
     *                   zero is returned. Must consist of only one Number and
     *                   only one character. E.g. 1y
     * @return   The value in milliseconds.
     * 
     * @deprecated This method was used for time calculation like "add three month to my date" 
     *             but what it really did was "add 1000L * 60 * 60 * 24 * 30 milliseconds to my date".
     *             The calculation did not differ between different length of month or leap years with the 
     *             result of unexpected dates.
     *             Better use {@link DateUtil#add(java.util.Date, String)} for such calculations.
     */
    @Deprecated
    public static long durationStringToLong(String aString) {
        if (isEmpty(aString)) {
            return 0;
        }
        long theFactor;
        int  len1     = aString.length() - 1;
        char lastChar = aString.charAt(len1);
        switch (lastChar) {
            case 'y':
                theFactor = 1000L * 60 * 60 * 24 * 365;
                break;
            case 'M':
                theFactor = 1000L * 60 * 60 * 24 * 30;
                break;
            case 'd':
                theFactor = 1000L * 60 * 60 * 24;
                break;
            case 'h':
                theFactor = 1000L * 60 * 60;
                break;
            case 'm':
                theFactor = 1000L * 60;
                break;
            case 's':
                theFactor = 1000L;
                break;
            default:
                throw new IllegalArgumentException("Parameter was: " + 
                        aString + ", but should be like [0..9][y | M | d | h | m | s ]");
        }
        long theValue = theFactor *
            Long.parseLong(aString.substring(0, len1));
        return theValue;
    }

    /**
     * This method sets the first character of a string to uppercase. If the string is empty
     * or the first character can not be set to uppercase, the same string will return.
     *
     * @param aString
     *        to capitalize the first character.
     * @return aString with the first character as uppercase
     */
    public static String capitalizeString(String aString) {
        if (isEmpty(aString)) return aString;
        char c = aString.charAt(0);
        return Character.isUpperCase(c) ? aString : Character.toUpperCase(c) + aString.substring(1);
    }

	/**
	 * This method sets the first character of a string to lower case. If the string is empty or the
	 * first character can not be set to lower case, the same string will return.
	 *
	 * @param string
	 *        to lower case the first character.
	 * @return string with the first character as lower case.
	 */
	public static String lowerizeString(String string) {
		if (isEmpty(string)) return string;
		char firstChar = string.charAt(0);
		return Character.isLowerCase(firstChar) ? string : Character.toLowerCase(firstChar) + string.substring(1);
	}

    /**
     * This method gets the value to a specified key, if key and value 
     * is separated by a colon (:) and each pair is separated by a semicolon. 
     * This should look like: key:value;key:value...
     * An empty string as key is allowed. If no key is found, a
     * NULL will be returned.
     * 
     * Use this with metaattributes for example.
     * 
     * @param aString where keys and values are listed as explained above 
     * @param aKey to get the corresponding value for
     * @return a property with the given key and the corresponding value. <code>null</code> 
     * if the key is not in the list.
     * 
     * 
     */
    public static String getSemicolonSeparatedValue(String aString, String aKey) {
        if(aString == null || aKey == null) {
            return null;
        }
        int startPos = aString.indexOf(aKey+':');
        if(startPos<0) {
            return null;
        }
        int endPos = aString.indexOf(';', startPos+aKey.length()+1);
        if(endPos<0) {
            return aString.substring(startPos+aKey.length()+1);
        }else {
            return aString.substring(startPos+aKey.length()+1, endPos);
        }
    }
    
    /**
     * This method extracts all key/values pairs from a string and returns
     * a property, containing all the key-value pairs. 
     * Each pair must be separated by a semicolon and key/value must be separated by a colon (:).
     * This should look like: key:value;key:value...
     * 
     * Use this with metaattributes for example.
     * 
     * @param aString the string with separated keys/values like: key:value;key:value...
     * @return a property with all key value pairs from the string or null if the string was null. 
     */
    public static Properties getAllSemicolonSeparatedValues(String aString) {
        Properties theProperty = new Properties();
        if(aString == null) {
            return theProperty;
        }
        
        int beginIndex = 0;
        int colonPos = aString.indexOf(':');
        int semicolonPos = aString.indexOf(';');
        
        String theKey;
        String theValue;
        
        while(colonPos>=0) {
            
            if (semicolonPos>=0) {
                theKey   = aString.substring(beginIndex, colonPos);
                theValue = aString.substring(colonPos + 1, semicolonPos);
                
                beginIndex = semicolonPos +1; 
                
                theProperty.setProperty(theKey, theValue);
                colonPos     = aString.indexOf(':', beginIndex);
                semicolonPos = aString.indexOf(';', beginIndex);
            } else {
                theKey   = aString.substring(beginIndex, colonPos);
                theValue = aString.substring(colonPos + 1, aString.length());
                
                theProperty.setProperty(theKey, theValue);
                break;
            }
        }
        
        return theProperty;
    }
    
    /**
     * This method returns the given String or if its empty it returns the given default.
     */
    public static String getStringOrDefault(String aString, String aDefault) {
        if (isEmpty(aString)) {
            return aDefault;
        }
        return aString;
    }

	/**
	 * Creates a debug representation of the object.
	 * <p>
	 * Use this method for writing objects to log messages.
	 * </p>
	 * 
	 * @param value
	 *        Is allowed to be null.
	 * @return Is never null.
	 */
	public static String debug(Object value) {
		try {
			return getObjectDescription(value);
		} catch (RuntimeException ex) {
			String message = "toString() failed: " + ex.getMessage();
			Logger.error(message, ex, StringServices.class);
			return message;
		}
	}

    /**
     * Returns the quoted {@link Object#toString()} representation of the given value, along with its class name.
     * 
     * @param value is allowed to be <code>null</code>.
     */
	public static String getObjectDescription(Object value) {
		return "'" + debugValue(value) + "' (Class: " + getClassNameNullsafe(value) + ")";
	}

	/**
	 * Returns the {@link Class} name of the given {@link Object}, unless it is a {@link Proxy}, for
	 * example a {@link ConfigurationItem}. In that case the class name is useless
	 * ("com.sun.proxy.$Proxy406") and the implemented interfaces of the {@link Proxy} are included.
	 */
	public static String getClassNameNullsafe(Object object) {
		if (object == null) {
			return "null";
		}
		return getNameNullsafe(object.getClass());
	}

	/**
	 * Returns the {@link Class#getName() name} of the class, unless it is a {@link Proxy}, for
	 * example a {@link ConfigurationItem}. In that case the class name is useless
	 * ("com.sun.proxy.$Proxy406") and the implemented interfaces of the {@link Proxy} are included.
	 */
	public static String getNameNullsafe(Class<?> classObject) {
		if (classObject == null) {
			return "null";
		}
		try {
			if (Proxy.isProxyClass(classObject)) {
				String joinedInterfaces = join(asList(classObject.getInterfaces()), ", ");
				return "Proxy(" + joinedInterfaces + ")";
			}
		} catch (RuntimeException ex) {
			return classObject.getName() + "(An error occurred: >>>" + ExceptionUtil.getFullMessage(ex) + "<<<)";
		}
		return classObject.getName();
	}



    // some useful static mappings

    /**
     * Map strings to lower case.
     */
    public static final Mapping LOWER_CASE_MAPPING = new Mapping() {
        @Override
		public Object map(Object input) {
            return input == null ? null : ((String)input).toLowerCase();
        }
    };

    /**
     * Map strings to lower case.
     */
    public static final Mapping UPPER_CASE_MAPPING = new Mapping() {
        @Override
		public Object map(Object input) {
            return input == null ? null : ((String)input).toUpperCase();
        }
    };

	/**
	 * Null-safe concatenation of two strings.
	 * 
	 * @param a
	 *        The first string to concatenate, may be <code>null</code>, which
	 *        means to ignore the string in the concatenation.
	 * @param b
	 *        The second string to concatenate, may be <code>null</code>, which
	 *        means to ignore the string in the concatenation.
	 * @return The concatenation of the non-<code>null</code> parameter strings,
	 *         never <code>null</code>.
	 */
	public static final String concatenate(String a, String b) {
		if (a == null) {
			return nonNull(b);
		}
		
		if (b == null) {
			return a;
		}
		
		if (a.length() == 0) {
			return b;
		} else if (b.length() == 0) {
			return a;
		} else {
			return a + b;
		}
	}
	
    /**
	 * Null-safe concatenation of strings.
	 * 
	 * @param subStrings - the strings to concatenate, ignoring <code>null</code> values.
	 * @return The concatenation of the non-<code>null</code> parameter strings.
	 */
	public static final String concatenate(String... subStrings) {
		int length = subStrings.length;
		if (length == 0) {
			return StringServices.EMPTY_STRING;
		}
		StringBuilder result = new StringBuilder(100);
		for(int i = 0; i < length; i++) {
			result.append(nonNull(subStrings[i]));
		}
		return result.toString();
	}

	/**
	 * Creates an int array out of a string, which contains separated parsable int's.
	 * 
	 * @param rawString
	 *        - the string, which contains the seperated parsable int's
	 * @param separator
	 * 		  - the separator between the parsable int's, must not be empty
	 * 
	 * @return array of parsed int's, may be <code>null</code>.
	 * @exception  NumberFormatException  if the string does not contain parsable integers.
	 */
	public static final int[] parseToIntArray(String rawString, char separator)
						      throws NumberFormatException {
		assert !isEmpty(separator) : "Separator must not be empty.";
		
		if (!isEmpty(rawString)) {
			String[] splittedRawString = split(rawString, separator);
			int[] result = new int[splittedRawString.length];
			for (int i = 0, size = splittedRawString.length; i < size; i++) {
				result[i] = Integer.parseInt(splittedRawString[i]);
			}
			return result;
		}
		return null;
	}

	/**
	 * Inserts the given string before the given index.
	 * 
	 * @param text
	 *        The text in which something should be inserted into.
	 * @param toInsert
	 *        The {@link CharSequence} to insert into the text.
	 * @throws IndexOutOfBoundsException
	 *         If the index is negative or larger than <code>text.length()</code>.
	 */
	public static String insert(String text, CharSequence toInsert, int index) {
		return text.substring(0, index) + toInsert.toString() + text.substring(index);
	}

	/**
	 * Formatter according to ISO 8601 see <a href="http://www.w3.org/TR/NOTE-datetime">W3C
	 * Date/Time formats</a>.
	 * 
	 * This is only one of the possible Formats, though. It is used for the dublin core attributes,
	 * but might be used in HTML-Headers, too
	 */
	public static DateFormat getIso8601Format() {
		return CalendarUtil.newSimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	}

	/** Normal format for date display. */
	public static DateFormat getDatetimeFormat() {
		return CalendarUtil.newSimpleDateFormat("dd.MM.yyyy HH:mm:ss");
	}

	/**
	 * Checks whether the first {@link CharSequence} starts with the second one.
	 * 
	 * @param composite
	 *        The sequence to check.
	 * @param part
	 *        A potential initial segment of <code>composite</code>
	 * @return <code>true</code> iff <code>part</code> is an initial segment of
	 *         <code>composite</code>.
	 */
	public static boolean startsWith(CharSequence composite, CharSequence part) {
		return startsWith(composite, part, 0);
	}

	/**
	 * Checks whether <code>composite</code> at given index starts with the <code>part</code>, i.e.
	 * 
	 * <p>
	 * it is checked whether for the <code>composite.charAt(index + i) = part.charAt(i)</code> for
	 * all <code>i &lt; part.length().</code>
	 * </p>
	 * 
	 * @param composite
	 *        The sequence to check.
	 * @param part
	 *        A potential initial segment of <code>composite</code>
	 * @param index
	 *        the first index in <code>composite</code> to check.
	 * 
	 * @throws IndexOutOfBoundsException
	 *         iff <code>index</code> is negative.
	 */
	public static boolean startsWith(CharSequence composite, CharSequence part, int index) {
		return internalStartWith(composite, part, index, false);
	}

	/**
	 * Checks whether the first {@link CharSequence} starts with the second one, ignoring case
	 * considerations.
	 * 
	 * @param composite
	 *        The sequence to check.
	 * @param part
	 *        A potential initial segment of <code>composite</code>
	 * @return <code>true</code> iff <code>part</code> is an initial segment of
	 *         <code>composite</code>.
	 */
	public static boolean startsWithIgnoreCase(CharSequence composite, CharSequence part) {
		return startsWithIgnoreCase(composite, part, 0);
	}

	/**
	 * Checks whether <code>composite</code> at given index starts with the <code>part</code>,
	 * ignoring case considerations.
	 * 
	 * @param composite
	 *        The sequence to check.
	 * @param part
	 *        A potential initial segment of <code>composite</code>
	 * @param index
	 *        the first index in <code>composite</code> to check.
	 * 
	 * @throws IndexOutOfBoundsException
	 *         iff <code>index</code> is negative.
	 */
	public static boolean startsWithIgnoreCase(CharSequence composite, CharSequence part, int index) {
		return internalStartWith(composite, part, index, true);

	}

	private static boolean internalStartWith(CharSequence composite, CharSequence part, int index, boolean ignoreCase) {
		if (index < 0) {
			throw new IndexOutOfBoundsException("Negative index: " + index);
		}
		int end = part.length() + index;
		if (composite.length() < end) {
			return false;
		}
		for (int i = index, j = 0; i < end; i++, j++) {
			char cChar = composite.charAt(i);
			char pChar = part.charAt(j);
			if (ignoreCase) {
				if (!equalsIgnoreCase(cChar, pChar)) {
					return false;
				}
			} else {
				if (cChar != pChar) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Taken from {@link String#regionMatches(boolean, int, String, int, int)} in Java 8.
	 */
	private static boolean equalsIgnoreCase(char c1, char c2) {
		if (c1 == c2) {
			return true;
		}
		// If characters don't match but case may be ignored,
		// try converting both characters to uppercase.
		// If the results match, then the comparison scan should
		// continue.
		char u1 = Character.toUpperCase(c1);
		char u2 = Character.toUpperCase(c2);
		if (u1 == u2) {
			return true;
		}
		// Unfortunately, conversion to uppercase does not work properly
		// for the Georgian alphabet, which has strange rules about case
		// conversion. So we need to make one last check before
		// exiting.
		if (Character.toLowerCase(u1) == Character.toLowerCase(u2)) {
			return true;
		}
		return false;
	}

	/**
	 * Converts a text separated by the given split regex to camel case with a beginning lower cased
	 * character.
	 */
	public static String toCamelCase(String text, String splitRegex) {
		String[] parts = text.split(splitRegex);

		StringBuffer result = new StringBuffer();

		for (int n = 0; n < parts.length; n++) {
			String part = parts[n];
			if (n > 0) {
				result.append(capitalizeString(part));
			} else {
				result.append(lowerizeString(part));
			}
		}

		return result.toString();
	}

	/**
	 * Changes the suffix for the given {@link String}.
	 */
	public static String changeSuffix(String string, String fromSuffix, String toSuffix) {
		return string.substring(0, string.length() - fromSuffix.length()) + toSuffix;
	}

	/**
	 * Creates a new random UUID.
	 * 
	 * @see UUID#randomUUID()
	 */
	public static String randomUUID() {
		return UUID.randomUUID().toString();
	}

}