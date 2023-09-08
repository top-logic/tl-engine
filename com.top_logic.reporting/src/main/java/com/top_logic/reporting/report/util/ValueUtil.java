/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.util;

import java.awt.Paint;
import java.awt.Shape;
import java.text.DateFormat;
import java.util.Collection;

import org.jfree.chart.ui.RectangleEdge;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.util.NumberUtil;
import com.top_logic.knowledge.wrap.list.FastListElement;

/**
 * The ValueUtil contains useful static methods for values.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class ValueUtil {

    private ValueUtil() {
        // Use the static methods of this class.
    }

    /**
     * This method is a convenience method to extract a int value form the
     * given object. The given object must be an {@link Number}. If the given
     * number object is <code>null</code> the default int value is
     * returned.
     * 
     * @param aNumberObject
     *        A {@link Number} as {@link Object}. Maybe <code>null</code>.
     * @param aDefault
     *        A default value. This value is used if the object is
     *        <code>null</code>.
     */
    public static int getIntValue(Object aNumberObject, int aDefault) {
        if (aNumberObject == null)  return aDefault;

        return ((Number) aNumberObject).intValue();
    }
    
    /**
     * This method is a convenience method to extract a boolean value form the
     * given object. The given object must be an {@link Boolean}. If the given
     * boolean object is <code>null</code> the default boolean value is
     * returned.
     * 
     * @param aBooleanObject
     *        A {@link Boolean} as {@link Object}. Maybe <code>null</code>.
     * @param aDefault
     *        A default value. This value is used if the object is
     *        <code>null</code>.
     */
    public static boolean getBooleanValue(Object aBooleanObject, boolean aDefault) {
        if (aBooleanObject == null) return aDefault;
        
        return getBooleanValue((Boolean) aBooleanObject, aDefault);
    }

    /** See {@link #getBooleanValue(Object, boolean)}. */
    public static boolean getBooleanValue(Boolean aBoolean, boolean aDefault) {
        if (aBoolean == null) return aDefault;
        
        return aBoolean.booleanValue();
    }
    
    /**
     * This method returns the given string object as string. If the string
     * object is <code>null</code> the default string is returned.
     * 
     * @param aStringObject
     *        A {@link String} as {@link Object}. Maybe <code>null</code>.
     * @param aDefault
     *        A default value. This value is used if the object is
     *        <code>null</code>.
     */
    public static String getStringValue(Object aStringObject, String aDefault) {
        if (aStringObject == null) return aDefault;

        return getStringValue((String) aStringObject, aDefault);
    }
    
    /** See {@link #getStringValue(Object, String)}. */
    public static String getStringValue(String aString, String aDefault) {
        if (aString == null) return aDefault;
        
        return aString;
    }
    
    /**
     * This method returns the given shape object as shape. If the shape
     * object is <code>null</code> the default shape is returned.
     * 
     * @param aShape
     *        A {@link Shape} as {@link Object}. Maybe <code>null</code>.
     * @param aDefault
     *        A default value. This value is used if the object is
     *        <code>null</code>.
     */
    public static Shape getShapeValue(Object aShape, Shape aDefault) {
        return getShapeValue((Shape) aShape, aDefault);
    }
    
    /** See {@link #getShapeValue(Shape, Shape)} */
    public static Shape getShapeValue(Shape aShape, Shape aDefault) {
        if (aShape == null) return aDefault;
        
        return aShape;
    }
    
    /**
     * This method returns the given paint object as paint. If the paint
     * object is <code>null</code> the default paint is returned.
     * 
     * @param aPaintObject
     *        A {@link Paint} as {@link Object}. Maybe <code>null</code>.
     * @param aDefaultPaint
     *        A default color. This color is used if the object is
     *        <code>null</code>.
     */
    public static Paint getPaint(Object aPaintObject, Paint aDefaultPaint) {
        return getPaint((Paint) aPaintObject, aDefaultPaint);
    }
    
    /** See {@link #getPaint(Object, Paint)}. */
    public static Paint getPaint(Paint aPaint, Paint aDefaultPaint) {
        if (aPaint == null) return aDefaultPaint;
        
        return aPaint;
    }
    
    /**
     * This method returns the given legend position object as rectangle edge. If the 
     * object is <code>null</code> the default legend position is returned.
     * 
     * @param aLegendPositionObject
     *        A {@link RectangleEdge} as {@link Object}. Maybe <code>null</code>.
     * @param aDefaultLegendPosition
     *        A default {@link RectangleEdge}. This legend position is used if the object is
     *        <code>null</code>.
     */
    public static RectangleEdge getLegendPosition(Object aLegendPositionObject, RectangleEdge aDefaultLegendPosition) {
        return getLegendPosition((RectangleEdge) aLegendPositionObject, aDefaultLegendPosition);
    }
    
    /** See {@link #getLegendPosition(Object, RectangleEdge)}. */
    public static RectangleEdge getLegendPosition(RectangleEdge aLegendPosition, RectangleEdge aDefaultLegendPosition) {
        if (aLegendPosition == null) return aDefaultLegendPosition;
        
        return aLegendPosition;
    }
    
    /**
     * This method returns for single select fast list attributes the selected value. The
     * attribute value of a fast list attribute is a collection or null. This method is a
     * convenience method to get the selected fast list element.
     * 
     * @param aFastListAttributeValue
     *            A value from a fast list attribute.
     * @return Returns the selected {@link FastListElement} or <code>null</code> if no
     *         element is selected.
     */
    public static FastListElement getFastListSelection(Object aFastListAttributeValue) {
        if ( !(aFastListAttributeValue instanceof Collection) ) { return null; }
        
        Collection selection = (Collection) aFastListAttributeValue;
        
        return (FastListElement) CollectionUtil.getFirst(selection);
    }
    
    
    /**
     * See {@link #getFastListSelection(Object)}.
     * 
     * Returns the name of the selected fast list element or an empty string if the no
     * element selected.
     * 
     * @param aFastListAttributeValue
     *            A value from a fast list attribute.
     */
    public static String getFastListSelectionAsString(Object aFastListAttributeValue) {
        FastListElement selected = getFastListSelection(aFastListAttributeValue);
        
        return selected != null ? selected.getName() : "";
    }
    
    /**
	 * This method returns the given date format object as {@link DateFormat}.
	 * If the date format object is <code>null</code> the default is returned.
	 * 
	 * @param aDateFormat
	 *            A {@link DateFormat} as {@link Object}. Maybe
	 *            <code>null</code>.
	 * @param aDefault
	 *            A default. This default value is used if the object is
	 *            <code>null</code>.
	 */
    public static DateFormat getDateFormat(Object aDateFormat, DateFormat aDefault) {
    	if (aDateFormat == null) { return aDefault; }
    	
    	return (DateFormat) aDateFormat;
    }

    /**
	 * This method is a convenience method to extract a double value form the given object. The
	 * given object must be an {@link Number}. If the given number object is <code>null</code> the
	 * default value is returned.
	 * 
	 * @param aValue
	 *        A {@link Number} as {@link Object}. Maybe <code>null</code>.
	 * @param aDefault
	 *        A default value. This value is used if the object is <code>null</code>.
	 */
	public static double getDoubleValue(Object aValue, double aDefault) {
		if (aValue == null) { return aDefault; }
		
		return NumberUtil.getDoubleValue(aValue);
	}
    
}

