/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.util;

import java.util.Date;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.TLID;
import com.top_logic.basic.xml.TagUtil;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.data.Link;

/** 
 * Converter for values (including DataObjects} to Strings.
 * 
 * It is as of now (2005-09-13) not Currently used, KHA.
 *
 * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class DOAttributeConverter {

    /** Strings that need no special encoding when written as XML. */
    protected static final String DONT_ENCODE = "()+-*/., =_";

    private static final char TEXT_PRE = 't';

    private static final char STRING_PRE = 's';

    private static final char INTEGER_PRE = 'i';

    private static final char FLOAT_PRE = 'f';

    private static final char LONG_PRE = 'l';

    private static final char DOUBLE_PRE = 'd';

    private static final char BOOLEAN_PRE = 'b';

    private static final char DATE_PRE = 'D';

    private static final char LINK_PRE = 'L';

    private static final char UNKNOWN_PRE = 'U';

    /** The only instance of this class. */
    private static DOAttributeConverter singleton;

    /**
     * Constructor for DOAttributeConverter.
     */
    protected DOAttributeConverter() {
        super();
    }

    protected String _toStringUnknown(Object anObject) {
        return (UNKNOWN_PRE + anObject.toString());
    }

    protected String _toObjectUnknown(String aString) {
        return (null);
    }

    /**
     * Convert the given object to a matching string representation.
     *
     * @param    anObject    The object to be converted.
     * @return   The string representation of the given object.
     */
    protected String _toString(Object anObject) {
        String theValue = null;

        if (anObject instanceof String) {
            if (((String) anObject).length() > 253) {
                theValue = Character.toString(TEXT_PRE);
            }
            else {
                theValue = STRING_PRE + (String) anObject;
//                theValue = STRING_PRE + getEncoded((String) anObject);
            }
        }
        else if (anObject instanceof Date) {
            theValue = DATE_PRE + Long.toString(((Date) anObject).getTime());
        }
        else if (anObject instanceof DataObject) {
            DataObject theObject = (DataObject) anObject;
			TLID theID = theObject.getIdentifier();
            String     theType   = theObject.tTable().getName();

            theValue = LINK_PRE + new Link(theType, theID).getDisplay();
        }
        else {
            char thePre;

            if (anObject instanceof Integer) {
                thePre = INTEGER_PRE;
            }
            else if (anObject instanceof Double) {
                thePre = DOUBLE_PRE;
            }
            else if (anObject instanceof Float) {
                thePre = FLOAT_PRE;
            }
            else if (anObject instanceof Long) {
                thePre = LONG_PRE;
            }
            else if (anObject instanceof Boolean) {
                thePre = BOOLEAN_PRE;
            }
            else {
                thePre   = UNKNOWN_PRE;
                theValue = this._toStringUnknown(anObject);
            }

            if (thePre != UNKNOWN_PRE) {
                theValue = thePre + anObject.toString();
            }
        }

        return (theValue);
    }

    /**
     * Convert the given string representation to a matching object.
     *
     * @param    aString    The string to be converted.
     * @return   The object for the given string representation.
     */
    protected Object _toObject(String aString) {
        Object theObject = null;

        if (!StringServices.isEmpty(aString)) {
            char   theType  = aString.charAt(0);
            String theValue = aString.substring(1);

            switch (theType) {
                case STRING_PRE  : theObject = theValue;
                                   break;
                case TEXT_PRE    : theObject = null;
                                   break;
                case INTEGER_PRE : theObject = Integer.valueOf(theValue);
                                   break;
                case LONG_PRE    : theObject = Long.valueOf(theValue);
                                   break;
                case DOUBLE_PRE  : theObject = Double.valueOf(theValue);
                                   break;
                case FLOAT_PRE   : theObject = Float.valueOf(theValue);
                                   break;
                case BOOLEAN_PRE : theObject = Boolean.valueOf(theValue);
                                   break;
                case DATE_PRE    : theObject = new Date(Long.parseLong(theValue));
                                   break;
                case LINK_PRE    : theObject = new Link(theValue);
                                   break;
                default          : theObject = this._toObjectUnknown(aString);
                                   break;
            }
        }

        return (theObject);
    }

    /**
     * Convert the given object to a matching string representation.
     *
     * @param    anObject    The object to be converted.
     * @return   The string representation of the given object.
     */
    public static String toString(Object anObject) {
        return (getInstance()._toString(anObject));
    }

    /**
     * Convert the given string representation to a matching object.
     *
     * @param    aString    The string to be converted.
     * @return   The object for the given string representation.
     */
    public static Object toObject(String aString) {
        return (getInstance()._toObject(aString));
    }

    /**
     * Check, if the given object is a string and the length is longer
     * than 253 characters.
     * 
     * @param    aValue    The object to be inspected.
     * @return   <code>true</code>, if it represents a text (as defined above).
     */
    public static boolean isText(Object aValue) {
        if (aValue instanceof String) {
            return (((String) aValue).length() > 253);
        }
        return (false);
    }

    public static boolean isTextValue(String aString) {
        return (!StringServices.isEmpty(aString) 
                && (aString.charAt(0) == TEXT_PRE));
    }

    public static DOAttributeConverter getInstance() {
        if (singleton == null) {
            singleton = new DOAttributeConverter();
        }

        return (singleton);
    }

    /**
     * Replace all non-valid characters by unicodes.
     *
     * @param    aString    The string to be encoded.
     */
    protected static String getEncoded (String aString) {
        char         theChar;
        int          theLength = aString.length ();
        StringBuffer theBuffer = new StringBuffer (theLength);

        for (int thePos = 0; thePos < theLength; thePos++) {
            theChar = aString.charAt (thePos);

            if (Character.isLetterOrDigit (theChar) 
                || (DONT_ENCODE.indexOf (theChar) > -1)) {
                theBuffer.append (theChar);
            }
            else {
                theBuffer.append (getUnicodeStringFor (theChar));
            }
        }

        return (theBuffer.toString ());
    }

    /**
     * Returns a unicode string for the given character.
     *
     * Because the toString() method of Integer is slow, we encode some known
     * values with constants.
     *
     * @param    aChar    The character to be encoded.
     * @return   The unicode for the character (HTML conform).
     */
    protected static String getUnicodeStringFor (char aChar) {
        String theEncoding;

        switch (aChar) {
            case '&'  : theEncoding = TagUtil.AMP_CHAR;
                        break;
            case '\"' : theEncoding = TagUtil.QUOT_CHAR;
                        break;
            case '\'' : theEncoding = TagUtil.APOS_CHAR;
                        break;
            case '<'  : theEncoding = TagUtil.LT_CHAR;
                        break;
            case '>'  : theEncoding = TagUtil.GT_CHAR;
                        break;
            default   : theEncoding = "&#" + Integer.toString (aChar) + ';';
                        break;
        }

        return (theEncoding);
    }
}
