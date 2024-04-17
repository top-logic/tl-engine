/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.util;

import java.awt.Dimension;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import com.top_logic.basic.ArrayUtil;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.DateUtil;
import com.top_logic.basic.ExceptionUtil;
import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.Settings;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.ComparableComparator;
import com.top_logic.basic.col.FilterUtil;
import com.top_logic.basic.col.MapUtil;
import com.top_logic.basic.col.factory.CollectionFactory;
import com.top_logic.basic.col.iterator.IteratorUtil;
import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.LocaleValueProvider;
import com.top_logic.basic.config.TimeZoneValueProvider;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.FileBasedBinaryContent;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.basic.time.TimeUtil;
import com.top_logic.basic.type.PrimitiveTypeUtil;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.basic.xml.TagUtil;
import com.top_logic.basic.xml.XMLStreamUtil;
import com.top_logic.basic.xml.XPathUtil;
import com.top_logic.basic.xml.XsltUtil;

/**
 * General useful utilities.
 * 
 * @see ArrayUtil
 * @see CollectionUtil
 * @see CollectionFactory
 * @see MapUtil
 * @see IteratorUtil
 * @see FilterUtil
 * 
 * @see StringServices
 * @see RegExpUtil
 * @see StreamUtilities
 * @see FileUtilities
 * 
 * @see DateUtil
 * @see CalendarUtil
 * @see TimeUtil
 * 
 * @see NumberUtil
 * @see ExceptionUtil
 * @see PrimitiveTypeUtil
 * 
 * @see TagUtil
 * @see DOMUtil
 * @see XMLStreamUtil
 * @see XPathUtil
 * @see XsltUtil
 * 
 * @see ConfigUtil
 * @see ModuleUtil
 * @see IdentifierUtil
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class Utils {

	/**
	 * Throws a {@link NullPointerException} if the object is null.
	 * 
	 * @param message
	 *        The error message if the object is null.
	 * @return The given object, for convenience.
	 */
	public static <T> T requireNonNull(T object, String message) {
		if (object == null) {
			throw new NullPointerException(message);
		}
		return object;
	}

	/**
	 * Throws a {@link NullPointerException} if the object is null.
	 * 
	 * @return The given object, for convenience.
	 */
	public static <T> T requireNonNull(T object) {
		if (object == null) {
			throw new NullPointerException();
		}
		return object;
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
		return StringServices.debug(value);
	}

	/**
	 * Null safe version of equals.
	 *
	 * @param a
	 *        the first parameter
	 * @param b
	 *        the second parameter
	 * @return <code>true</code> if both are identical (e.g. both are null), or both are equal.
	 */
	public static boolean equals(Object a, Object b) {
		if (a == b) {
			// objects identical or both null
			return true;
		}
		if (a != null) {
			// safely use equals on a
			return a.equals(b);
		}
		// a is null, but b is not null
		return false;
	}

	/**
	 * call toString on anObject if anObject is not null.
	 *
	 * @param anObject
	 *        to be toString()ed
	 * @param aNullString
	 *        if anObject is null
	 * @return anObject.toString() if not null, aNullString otherwise
	 */
	public static String objectToString(Object anObject, String aNullString) {
		return anObject == null ? aNullString : anObject.toString();
	}

	/**
	 * Deliver String representation of a Map. Useful for Debugging purposes.
	 *
	 * @param aMap
	 *        to be toString()ed
	 * @param aKeyValueSeparator
	 *        to separate keys and values
	 * @param anEntrySeparator
	 *        to separate entries
	 * @param withNewLine
	 *        true if each entry should get a newLine
	 * @return string representation of the Map
	 */
	public static String mapToString(Map aMap,
			String aKeyValueSeparator,
			String anEntrySeparator,
			boolean withNewLine) {

		aKeyValueSeparator = (aKeyValueSeparator == null) ? " -> " : aKeyValueSeparator;
		anEntrySeparator = (anEntrySeparator == null) ? " ; " : anEntrySeparator;

		StringWriter theResult = new StringWriter();
		PrintWriter out = new PrintWriter(theResult);

		Iterator theIt = aMap.entrySet().iterator();

		while (theIt.hasNext()) {
			Map.Entry theEntry = (Map.Entry) theIt.next();
			out.print(objectToString(theEntry.getKey(), "<null>"));
			out.print(aKeyValueSeparator);
			out.print(objectToString(theEntry.getValue(), "<null>"));
			if (theIt.hasNext()) {
				out.print(anEntrySeparator);
			}
			if (withNewLine) {
				out.println();
			}
		}

		return theResult.toString();
	}

	/**
	 * Folds the objects given via an iterator using the given folder function.
	 */
	public static Object fold(Object anInitialResult, Iterator someObjects, FolderFunction aFolderFunction) {
		if (someObjects == null || !someObjects.hasNext()) {
			return anInitialResult;
		}
		Object theResult = anInitialResult;
		while (someObjects.hasNext()) {
			theResult = aFolderFunction.fold(theResult, someObjects.next());
		}
		return theResult;
	}

	/**
	 * Helper interface for the {@link #fold(Object, Object)} method declaring a folder function.
	 */
	public static interface FolderFunction {
		public Object fold(Object currentResult, Object aNewObject);
	}

	/**
	 * Checks if the given Boolean is not null and true. This is used for easy handling of Booleans
	 * in if statements.
	 *
	 * @return true if aBoolean equal to Boolean.TRUE
	 */
	public static boolean isTrue(Boolean aBoolean) {
		if (aBoolean != null) {
			return aBoolean.booleanValue();
		} else {
			return false;
		}
	}

	/**
	 * Checks if the given Boolean is not null and false. This is used for easy handling of Booleans
	 * in if statements.
	 *
	 * @return true if aBoolean equal to Boolean.FALSE
	 */
	public static boolean isFalse(Boolean aBoolean) {
		if (aBoolean != null) {
			return !aBoolean.booleanValue();
		} else {
			return false;
		}
	}

	/**
	 * Checks whether a is less than b.
	 */
	public static boolean isLess(Comparable a, Comparable b) {
		return a.compareTo(b) < 0;
	}

	/**
	 * Checks whether a is less than or equal to b.
	 */
	public static boolean isLessOrEqual(Comparable a, Comparable b) {
		return a.compareTo(b) <= 0;
	}

	/**
	 * Checks whether a is greater than b.
	 */
	public static boolean isGreater(Comparable a, Comparable b) {
		return a.compareTo(b) > 0;
	}

	/**
	 * Checks whether a is greater than or equal to b.
	 */
	public static boolean isGreaterOrEqual(Comparable a, Comparable b) {
		return a.compareTo(b) >= 0;
	}

	/**
	 * Checks whether a is equal to b.
	 */
	public static boolean isEqual(Comparable a, Comparable b) {
		return a.compareTo(b) == 0;
	}

	/**
	 * Check, if the application is running on a windows machine.
	 * 
	 * @return <code>true</code> if running on windows.
	 */
	public static boolean isWindows() {
		return (System.getProperty("os.name").startsWith("Windows"));
	}

	/**
	 * Checks if a given Object is <code>null</code> or empty.
	 *
	 * Note: If the value is a {@link String} use {@link StringServices#isEmpty(CharSequence)}
	 * instead, if the value is a {@link Collection} use
	 * {@link CollectionUtil#isEmptyOrNull(Collection)} instead and if the value is an array use
	 * {@link ArrayUtil#isEmpty(Object[])} instead.
	 *
	 * @param value
	 *        The object to check.
	 * @return returns <code>true</code> if the value is <code>null</code> or empty,
	 *         <code>false</code> otherwise.
	 */
	public static boolean isEmpty(Object value) {
		if (value == null) {
			return true;
		} else if (value instanceof String) {
			return ((String) value).isEmpty();
		} else if (value instanceof Collection) {
			return ((Collection<?>) value).isEmpty();
		} else if (value instanceof Map) {
			return ((Map<?, ?>) value).isEmpty();
		} else if (value.getClass().isArray()) {
			return Array.getLength(value) == 0;
		} else {
			return false;
		}
	}

	/**
	 * This method returns an instance of the given class or <code>null</code> if an error occurred.
	 * All exceptions are catched only a logger message is set.
	 *
	 * @param aClassName
	 *        A class name. Must NOT be <code>null</code>
	 */
	public static Object getInstanceFor(String aClassName) {
		Object instance = null;
		try {
			instance = Class.forName(aClassName).newInstance();
		} catch (Exception e) {
			Logger.error(
				"Couldn't instantiate an instance of the class ('" + aClassName + "'). Returned ('null') instead.", e,
				Utils.class);
		}

		return instance;
	}

	/**
	 * Sort the second list according to the first one.
	 *
	 * This will check for objects contained in both lists and order them. New values in the second
	 * list will be appended as they are in the new created return value.
	 *
	 * @param anOld
	 *        The list containing the order to reconstruct, must not be <code>null</code>.
	 * @param aNew
	 *        The new list to be sorted like the old one, must not be <code>null</code>.
	 * @return A new list containing all elements from "aNew" ordered by the one from "anOld", never
	 *         <code>null</code>.
	 */
	public static List toOriginalOrder(List anOld, List aNew) {
		List theNew = new ArrayList(aNew);
		List theResult = new ArrayList(aNew.size());

		for (Iterator theIt = anOld.iterator(); theIt.hasNext();) {
			Object theEntry = theIt.next();

			if (theNew.contains(theEntry)) {
				theResult.add(theEntry);
				theNew.remove(theEntry);
			}
		}

		theResult.addAll(theNew);

		return theResult;
	}

	/**
	 * Converts the given object into a String.
	 *
	 * @param aObject
	 *        the object to convert to String
	 * @return the given object as a String; may be <code>null</code>
	 */
	public static String getStringValue(Object aObject) {
		Object theObject = resolveCollection(aObject);
		return theObject == null ? null : theObject.toString();
	}

	/**
	 * Converts the given object into a Double.
	 *
	 * @param aObject
	 *        the object to convert to Double
	 * @return the given object as a Double; may be <code>null</code>
	 */
	public static Float getFloatValue(Object aObject) {
		Object theObject = resolveCollection(aObject);
		if (theObject == null)
			return null;
		if (theObject instanceof Float)
			return (Float) theObject;
		if (theObject instanceof Number)
			return Float.valueOf(((Number) theObject).floatValue());
		try {
			return Float.valueOf(theObject.toString());
		} catch (Exception e) {
			// The objects seems to be anything but a Double
			return null;
		}
	}

	/**
	 * Converts the given object into a float.
	 * 
	 * @param aObject
	 *        the object to convert to float
	 * @return the given object as a float
	 */
	public static float getfloatValue(Object aObject) {
		Object theObject = resolveCollection(aObject);
		if (theObject == null)
			return 0.0f;
		if (theObject instanceof Number)
			return ((Number) theObject).floatValue();
		try {
			return Float.parseFloat(theObject.toString());
		} catch (Exception e) {
			// The objects seems to be anything but a double
			return 0.0f;
		}
	}

	/**
	 * Converts the given object into a Double.
	 * 
	 * @param aObject
	 *        the object to convert to Double
	 * @return the given object as a Double; may be <code>null</code>
	 */
	public static Double getDoubleValue(Object aObject) {
		Object theObject = resolveCollection(aObject);
		if (theObject == null)
			return null;
		if (theObject instanceof Double)
			return (Double) theObject;
		if (theObject instanceof Number)
			return Double.valueOf(((Number) theObject).doubleValue());
		try {
			return Double.valueOf(theObject.toString());
		} catch (Exception e) {
			// The objects seems to be anything but a Double
			return null;
		}
	}

	/**
	 * Converts the given object into a double.
	 *
	 * @param aObject
	 *        the object to convert to double
	 * @return the given object as a double
	 */
	public static double getdoubleValue(Object aObject) {
		Object theObject = resolveCollection(aObject);
		if (theObject == null)
			return 0.0;
		if (theObject instanceof Number)
			return ((Number) theObject).doubleValue();
		try {
			return Double.parseDouble(theObject.toString());
		} catch (Exception e) {
			// The objects seems to be anything but a double
			return 0.0;
		}
	}

	/**
	 * Converts the given object into a Integer.
	 *
	 * @param aObject
	 *        the object to convert to Integer
	 * @return the given object as a Integer; may be <code>null</code>
	 */
	public static Integer getIntegerValue(Object aObject) {
		Object theObject = resolveCollection(aObject);
		if (theObject == null)
			return null;
		if (theObject instanceof Integer)
			return (Integer) theObject;
		if (theObject instanceof Number)
			return Integer.valueOf(((Number) theObject).intValue());
		try {
			return Integer.valueOf(theObject.toString());
		} catch (Exception e) {
			// The objects seems to be anything but an Integer
			return null;
		}
	}

	/**
	 * Converts the given object into a int.
	 *
	 * @param aObject
	 *        the object to convert to int
	 * @return the given object as a int
	 */
	public static int getintValue(Object aObject) {
		Object theObject = resolveCollection(aObject);
		if (theObject == null)
			return 0;
		if (theObject instanceof Number)
			return ((Number) theObject).intValue();
		try {
			return Integer.parseInt(theObject.toString());
		} catch (Exception e) {
			// The objects seems to be anything but an int
			return 0;
		}
	}

	/**
	 * Converts the given object into a Long.
	 *
	 * @param aObject
	 *        the object to convert to Long
	 * @return the given object as a Long; may be <code>null</code>
	 */
	public static Long getLongValue(Object aObject) {
		Object theObject = resolveCollection(aObject);
		if (theObject == null)
			return null;
		if (theObject instanceof Long)
			return (Long) theObject;
		if (theObject instanceof Number)
			return Long.valueOf(((Number) theObject).longValue());
		try {
			return Long.valueOf(theObject.toString());
		} catch (Exception e) {
			// The objects seems to be anything but a Long
			return null;
		}
	}

	/**
	 * Converts the given object into a long.
	 *
	 * @param aObject
	 *        the object to convert to long
	 * @return the given object as a long
	 */
	public static long getlongValue(Object aObject) {
		Object theObject = resolveCollection(aObject);
		if (theObject == null)
			return 0;
		if (theObject instanceof Number)
			return ((Number) theObject).longValue();
		try {
			return Long.parseLong(theObject.toString());
		} catch (Exception e) {
			// The objects seems to be anything but a long
			return 0;
		}
	}

	/**
	 * Converts the given object into a Boolean.
	 * 
	 * @param aObject
	 *        the object to convert to Boolean
	 * @return the given object as a Boolean; may be <code>null</code>
	 * 
	 * @see Boolean#parseBoolean(String)
	 * @see Boolean#valueOf(String)
	 * @see StringServices#parseBoolean(String)
	 * @see Utils#getbooleanValue(Object)
	 */
	public static Boolean getBooleanValue(Object aObject) {
		Object theObject = resolveCollection(aObject);
		if (theObject instanceof Boolean)
			return (Boolean) theObject;
		String theString = theObject == null ? StringServices.EMPTY_STRING : theObject.toString().trim();
		return theString.equalsIgnoreCase("true") ? Boolean.TRUE
			: theString.equalsIgnoreCase("false") ? Boolean.FALSE : null;
	}

	/**
	 * Converts the given object into a boolean.
	 * 
	 * @param aObject
	 *        the object to convert to boolean
	 * @return the given object as a boolean
	 * 
	 * @see Boolean#parseBoolean(String)
	 * @see Boolean#valueOf(String)
	 * @see StringServices#parseBoolean(String)
	 * @see Utils#getBooleanValue(Object)
	 */
	public static boolean getbooleanValue(Object aObject) {
		Object theObject = resolveCollection(aObject);
		if (theObject instanceof Boolean)
			return ((Boolean) theObject).booleanValue();
		return ((theObject != null) && theObject.toString().trim().equalsIgnoreCase("true"));
	}

	/**
	 * Gets the first element of a collection, if the given object is a collection.
	 *
	 * @param aObject
	 *        the object to get the proper value from
	 * @return the value as a Object
	 */
	public static Object resolveCollection(Object aObject) {
		while (aObject instanceof Collection) {
			aObject = CollectionUtil.getFirst((Collection) aObject);
		}
		return aObject;
	}

	/**
	 * Compute the dimension to which an image with the given aspect ration (width / height) can be
	 * scaled so that it best fits the given available space without changing its aspect ratio.
	 * 
	 * @param availableSpace
	 *        The space available for rendering the image.
	 * @param imageAspectRatio
	 *        The aspect ration of the image (with / height).
	 * @return The dimension of the image.
	 */
	public static Dimension getImageDimension(Dimension availableSpace, float imageAspectRatio) {
		if (availableSpace.height <= 0 || availableSpace.width <= 0) {
			// There is no space at all.
			return new Dimension(0, 0);
		}

		float spaceAspectRatio = (float) availableSpace.width / availableSpace.height;

		if (imageAspectRatio > spaceAspectRatio) {
			// The width of the available space is used as base for scaling.
			int baseWidth = availableSpace.width;
			return new Dimension(baseWidth, (int) (baseWidth / imageAspectRatio));
		} else {
			int baseHeight = availableSpace.height;
			// The height of the available space is used as base for scaling.
			return new Dimension((int) (baseHeight * imageAspectRatio), baseHeight);
		}
	}

	/**
	 * Compute the dimension to which an image with the given aspect ration (width / height) can be
	 * scaled so that it best fits the given available space without changing its aspect ratio.
	 * 
	 * @param availableSpace
	 *        The space available for rendering the image.
	 * @param imageDimension
	 *        The dimension of the image.
	 * @param noEnlarge
	 *        Whether enlarging the image should be prevented to avoid reducing image sharpness.
	 * @return The dimension of the image.
	 */
	public static Dimension getImageDimension(Dimension availableSpace, Dimension imageDimension, boolean noEnlarge) {
		if (availableSpace.height <= 0 || availableSpace.width <= 0) {
			// There is no space at all.
			return new Dimension(0, 0);
		}

		float spaceAspectRatio = ((float) availableSpace.width) / availableSpace.height;

		if (imageDimension.height <= 0 || imageDimension.width <= 0) {
			// There is no image at all.
			if (noEnlarge) {
				return new Dimension(0, 0);
			} else {
				return new Dimension(availableSpace.width, availableSpace.height);
			}
		}

		float imageAspectRatio = ((float) imageDimension.width) / imageDimension.height;

		if (imageAspectRatio > spaceAspectRatio) {
			// The width of the available space or the image is used as base for scaling.
			int baseWidth = noEnlarge ? Math.min(availableSpace.width, imageDimension.width) : availableSpace.width;
			return new Dimension(baseWidth, (int) (baseWidth / imageAspectRatio));
		} else {
			// The height of the available space or the image is used as base for scaling.
			int baseHeight = noEnlarge ? Math.min(availableSpace.height, imageDimension.height) : availableSpace.height;
			return new Dimension((int) (baseHeight * imageAspectRatio), baseHeight);
		}
	}

	/**
	 * Convert a collection object that is not already in an order into a sorted list to provide a
	 * stable order used for display issues.
	 * <p>
	 * Other objects will be returned untouched.
	 * </p>
	 * 
	 * @param object
	 *        an object
	 * @return sorted list or object
	 */
	public static Object createSortedListForDisplay(Object object) {
		if (object instanceof Collection && (!(object instanceof List))) {
			ArrayList convertedResult = new ArrayList((Collection) object);
			Collections.sort(convertedResult, ComparableComparator.INSTANCE);
			return convertedResult;
		}
		return object;
	}

	/**
	 * Parses the given time zone {@link String}.
	 * 
	 * @param timeZone
	 *        A time zone {@link String} as returned by {@link #formatTimeZone(TimeZone)}.
	 * @return <code>null</code>, if time zone is <code>null</code> or empty.
	 * 
	 * @see #formatTimeZone(TimeZone)
	 * @see TimeZoneValueProvider
	 */
	public static TimeZone parseTimeZone(String timeZone) {
		try {
			// There is no real property, use name of class instead
			String propertyName = Utils.class.getName();
			return TimeZoneValueProvider.INSTANCE.getValue(propertyName, timeZone);
		} catch (ConfigurationException ex) {
			throw new IllegalArgumentException(ex);
		}
	}

	/**
	 * Formats the given {@link Locale}.
	 * 
	 * @param timeZone
	 *        A {@link Locale}.
	 * @return Empty string, if timeZone is <code>null</code>. Otherwise a string which can be used
	 *         to recreate {@link TimeZone} by {@link #parseTimeZone(String)}.
	 * 
	 * @see #parseTimeZone(String)
	 * @see TimeZoneValueProvider
	 */
	public static String formatTimeZone(TimeZone timeZone) {
		return TimeZoneValueProvider.INSTANCE.getSpecification(timeZone);
	}

	/**
	 * Parses the given locale {@link String}.
	 * 
	 * @param locale
	 *        A locale {@link String} as returned by {@link #formatLocale(Locale)}.
	 * @return <code>null</code>, if locale is <code>null</code> or empty.
	 * 
	 * @see #formatLocale(Locale)
	 * @see LocaleValueProvider
	 */
	public static Locale parseLocale(String locale) {
		try {
			// There is no real property, use name of class instead
			String propertyName = Utils.class.getName();
			return LocaleValueProvider.INSTANCE.getValue(propertyName, locale);
		} catch (ConfigurationException ex) {
			throw new IllegalArgumentException(ex);
		}
	}

	/**
	 * Formats the given {@link Locale}.
	 * 
	 * @param locale
	 *        A {@link Locale}.
	 * @return Empty string, if locale is <code>null</code>. Otherwise a string which can be used to
	 *         recreate locale by {@link #parseLocale(String)}.
	 * 
	 * @see #parseLocale(String)
	 * @see LocaleValueProvider
	 */
	public static String formatLocale(Locale locale) {
		return LocaleValueProvider.INSTANCE.getSpecification(locale);
	}

	/**
	 * creates a {@link BinaryContent} returns a clone of the given {@link InputStream} multiple
	 * times.
	 * <p>
	 * Requires the {@link Settings} service to be started.
	 * </p>
	 * 
	 * @param baseStream
	 *        stream on which the binary content should based.
	 */
	public static BinaryContent createBinaryContent(InputStream baseStream) throws IOException {
		File file = File.createTempFile("InputStreamClone", ".bin", Settings.getInstance().getTempDir());
		final FileOutputStream output = new FileOutputStream(file);
		try {
			StreamUtilities.copyStreamContents(baseStream, output);
		} finally {
			output.close();
		}
		return FileBasedBinaryContent.createBinaryContent(file);
	}

	/**
	 * Stores the big-endian bytes representation of the given long into the given byte array
	 * starting at the given position.
	 * 
	 * <p>
	 * The positions <code>offset</code> to <code>offset + 7</code> are accessed, i.e. the array
	 * needs at least size <code>offset + 7</code>.
	 * </p>
	 * 
	 * @param b
	 *        The byte array to store byte representation to.
	 * @param offset
	 *        The index to store the highest byte.
	 * @param l
	 *        The long to store.
	 * 
	 * @see #bytesToLong(byte[], int)
	 */
	public static void longToBytes(byte[] b, int offset, long l) {
		for (int i = offset + (Long.SIZE / Byte.SIZE) - 1; i >= offset; i--) {
			b[i] = (byte) (l & 0xFF);
			l >>= Byte.SIZE;
		}
	}

	/**
	 * Reads the big-endian bytes representation of a long from the given byte array starting at the
	 * given position.
	 * 
	 * <p>
	 * The positions <code>offset</code> to <code>offset + 7</code> are accessed, i.e. the array
	 * needs at least size <code>offset + 7</code>.
	 * </p>
	 * 
	 * @param b
	 *        The byte array holding byte representation of the long.
	 * @param offset
	 *        The index holding the highest byte.
	 * 
	 * @return The stored long.
	 * 
	 * @see #longToBytes(byte[], int, long)
	 */
	public static long bytesToLong(byte[] b, int offset) {
		long result = 0;
		for (int i = offset, end = offset + (Long.SIZE / Byte.SIZE) - 1; i <= end; i++) {
			result <<= Byte.SIZE;
			result |= (b[i] & 0xFF);
		}
		return result;
	}

}
