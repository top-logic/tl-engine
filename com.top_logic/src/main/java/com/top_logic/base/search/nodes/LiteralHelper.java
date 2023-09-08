/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.search.nodes;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.top_logic.base.search.QueryException;
import com.top_logic.base.search.parser.QueryPrefixConstants;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.time.CalendarUtil;

/**
 * Helper class with several methods to parse and format Literal.
 *
 * @author    <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class LiteralHelper implements QueryPrefixConstants   {

    /** Usefull constant for a Integer 0 expression */
	protected static final Integer INTEGER0 = Integer.valueOf(0);

    /** Usefull constant for a Long 0 expression */
	protected static final Long LONG0 = Long.valueOf(0);

    /** This class contains static methods only */
    private LiteralHelper()  {
    }

    /** 
     * Create a number from a Decimal/Hex/Octal String 
     */
    public static Number parseInteger(String anInt) {
        int  len   = anInt.length();
        int  start = 0;     // start of actual Number
        if (len < 1)
            throw new NumberFormatException("Unexpected empty Integer expression");
        char last  = anInt.charAt(len-1);
        int  radix = 10;    // base for converion (Octal, Decimal, hex)
        boolean isLong = last == 'l' || last == 'L';
        if (anInt.charAt(0) == '0')  { // 0 or 0x/0X {
            if (len < 2)                // Simply "0"
                return INTEGER0;
            if (len < 3 && isLong)      // Simply "0L"
                return LONG0;
            char second = anInt.charAt(1);
            if (second == 'x'|| second == 'X')  { // 0x / 0X
                start = 2;
                radix = 16;
            }
            else  {
                start = 1; // "0..."
                radix = 8;
            }
        }
        if (start != 0 || isLong) {
            if (isLong)
                return Long.valueOf   (anInt.substring(start,len -1), radix);
            else
                return Integer.valueOf(anInt.substring(start)       , radix);
        } 
        // else simply Decimal
        return Integer.valueOf(anInt);
    }

    /** 
     * Create a Number from a Double/Float String 
     */
    public static Number parseFloat(String aFloat) {

        int  len   = aFloat.length();
        if (len < 1)
            throw new NumberFormatException("Unexpected empty FloatingPoint expression");
        char last  = aFloat.charAt(len-1);
        if (last == 'd' || last == 'D')
            return Double.valueOf (aFloat.substring(0,len -1));
        if (last == 'f' || last == 'F') {
            return Float.valueOf  (aFloat.substring(0,len -1));
        }
        return Double.valueOf(aFloat);
    }

    /** 
     * Parse a Literal of a given kind to an object.
     *
     * @param kind one of the Tokens from 
     *          {@link com.top_logic.base.search.parser.QueryPrefixConstants}
     *
     * @throws QueryException in case something fails
     */
    public static Object objectForKind(int kind, String aValue) 
        throws QueryException {
        Object result = null;
        try  {
          switch (kind) {
            case FLOATING_POINT_LITERAL:
                result = parseFloat(aValue);
                break;
            case INTEGER_LITERAL:
                result = parseInteger(aValue);
                break;
            case STRING_LITERAL:
                result = StringServices.unQuoteAndEscape(aValue);
                break;
            case IDENTIFIER:    // No need to care for escapes here
                result = aValue;
                break;
            case DATE_LITERAL:
                result = getIsoDate().parse(aValue);
                break;
            case TIME_LITERAL:
                result = getIsoTimeFormat().parse(aValue);
                break;
            case DATE_TIME_LITERAL:
					result = getIsoDateTimeFormat().parse(aValue);
                break;
            case BOOLEAN_LITERAL:
                throw new QueryException("Use LITERAL_TRUE/FALSE to create a Boolean literal.");
            default:    
                throw new QueryException("Unexpected kind : " + kind);
        }}
        catch (ParseException pe)  {
            throw new QueryException(pe);
        }
        return result;
    }

	/** Date formatter to parse/create ISO dates. */
	protected static SimpleDateFormat getIsoDate() {
		return CalendarUtil.newSimpleDateFormat("yyyy-MM-dd");
	}

	/** Date formatter to parse/create ISO times. */
	protected static SimpleDateFormat getIsoTimeFormat() {
		return CalendarUtil.newSimpleDateFormat("HH:mm:ss");
	}

	/** Date formatter to parse/create ISO date and time. */
	protected static SimpleDateFormat getIsoDateTimeFormat() {
		return CalendarUtil.newSimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	}

    
}
