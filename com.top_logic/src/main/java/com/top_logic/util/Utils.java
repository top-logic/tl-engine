/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util;

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import com.top_logic.basic.StringServices;
import com.top_logic.mig.html.HTMLFormatter;
import com.top_logic.model.TLObject;

/**
 * Collection of some general utility methods.
 * String related utility methods  are located in
 * {@link com.top_logic.basic.StringServices}
 *
 * @author    <a href="mailto:tsa@top-logic.com">Theo Sattler</a>
 *
 */
public class Utils extends com.top_logic.basic.util.Utils {
	
    /**
     * This method provides attribute value access via a path expression.
     * The elements of the path a separated by a dot ('.'), every element
     * names an attribute of a wrapper. All but the last values must be
     * of type wrapper themselves in order to allow descending along the path.
     *
     * @param aPath   a path denoting successive attribute access
     * @param aBase   the wrapper to start with
     * @return  the value of the last attribute
     */
	public static Object getValueByPath(String aPath, TLObject aBase) {
		int theIndex = aPath.indexOf('.');
		if (theIndex > 0) {
			String prefix = aPath.substring(0, theIndex);
			String suffix = aPath.substring(theIndex + 1);
			Object theNewBase = aBase.tValueByName(prefix);
			if (!(theNewBase instanceof TLObject)) {
				return null;
            }
			return getValueByPath(suffix, (TLObject) theNewBase);
		} else {
			return aBase.tValueByName(aPath);
        }
    }

    /**
     * This method provides attribute value access via a path expression.
     * The elements of the path a separated by a dot ('.'), every element
     * names an attribute of a wrapper. All but the last values must be
     * of type wrapper themselves in order to allow descending along the path.
     *
     * @param aPath   a path denoting successive attribute access
     * @param aBase   the wrapper to start with
     * @param aValue  the value to be set to the referenced attribute
     */
	public static void setValueByPath(String aPath, TLObject aBase, Object aValue) {
        int theIndex = aPath.lastIndexOf('.');
        if (theIndex < 0) {
			aBase.tUpdateByName(aPath, aValue);
        } else {
            String theBasePath = aPath.substring(0, theIndex);
            String theAttName  = aPath.substring(theIndex + 1);
			TLObject theLeafWrapper = (TLObject) getValueByPath(theBasePath, aBase);
			theLeafWrapper.tUpdateByName(theAttName, aValue);
        }
    }

    /**
     * Converts the given object into a Date.
     *
     * @param aObject
     *            the object to convert to Date
     * @return the given object as a Data
     */
    public static Date getDateValue(Object aObject) {
        Object theObject = resolveCollection(aObject);
        if (theObject == null) return null;
        if (theObject instanceof Date) return (Date)theObject;
        if (theObject instanceof Calendar) return ((Calendar)theObject).getTime();
        if (theObject instanceof Number) return new Date(((Number)aObject).longValue());
        try {
            return HTMLFormatter.getInstance().getDateFormat().parse(theObject.toString());
        }
        catch (Exception e) {
            // The objects seems to be anything but a Date
            return null;
        }
    }

	/**
	 * Creates a {@link MessageFormat} for the given pattern and the locale of the current user and
	 * formats the given arguments.
	 * 
	 * <p>
	 * In contrast to {@link MessageFormat#format(String, Object...)} this method respects the
	 * locale of the current user.
	 * </p>
	 * 
	 * @param pattern
	 *        The pattern to apply.
	 * @param arguments
	 *        The arguments used in pattern.
	 * 
	 * @return The formatted string.
	 * 
	 * @see MessageFormat#format(String, Object...)
	 * @see #newMessageFormat(String)
	 */
	public static String format(String pattern, Object... arguments) {
		return newMessageFormat(pattern).format(arguments);
	}

	/**
	 * Creates a {@link MessageFormat} for the given pattern and the locale of the current user.
	 * 
	 * <p>
	 * In contrast to {@link MessageFormat#format(String, Object...)} this method respects the
	 * locale of the current user.
	 * </p>
	 * 
	 * @param pattern
	 *        The pattern to apply.
	 * 
	 * @see #format(String, Object...)
	 */
	public static MessageFormat newMessageFormat(String pattern) {
		Locale currentLocale = TLContext.getLocale();
		return new MessageFormat(pattern, currentLocale);
	}

	/**
	 * Creates a random {@link UUID} replacing for safety dashes with underscores and adding an id
	 * prefix.
	 */
	public static String getRandomID() {
		return "ID_" + StringServices.randomUUID().replace('-', '_');
	}

}
