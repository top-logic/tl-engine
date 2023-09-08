/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap.unit;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import com.top_logic.knowledge.wrap.Wrapper;

/**
 * Interface describing a Unit.
 * 
 * (Translations should can done using a prefix + getName(),
 *  but this is a business class and not a GUI class)
 * 
 * @author    <a href=mailto:kha@top-logic.com>Klaus Halfmann</a>
 */
public interface Unit extends Wrapper {

	/**
	 * I18N key of the display name attribute.
	 */
	public String DISPLAY_NAME_ATTRIBUTE_KEY = "tl.unit.name";
    
    /** 
     * Return true when this is not a derived unit.
     */
    public boolean isBaseUnit();

    /** Return a Unit this one is based on.
     * 
     * @return null for base units (isBaseUnit())
     */
    public Unit getBaseUnit();
    
    /** 
     * @param aUnit The new base unit.
     */
    public void setBaseUnit(Unit aUnit);

    /**
     * Return the conversion factor to the base unit.
     * 
     * @return 0.0 in case Unit cannot be converted via a simple Factor (Celsius/Farenheit)
     *         1.0 in case of a basUnit (getBaseUnit() == null)
     */
    public double getConversionFactor() throws IllegalStateException;
    
    /** 
     * @param aFactor The new conversion factor between this unit and its base unit.
     */
    public void setConversionFactor(Double aFactor);
   
    /**
     * Return the Format to display a Number as String.
     * 
     * @param aLocale defines the language to use.
     */ 
    public NumberFormat getFormat(Locale aLocale);

    /**
     * Return the NumberFormat using some default Locale.
     */ 
    public NumberFormat getFormat();

	/**
	 * The format specification compatible with {@link DecimalFormat#DecimalFormat(String)}.
	 */
	public String getFormatSpec();

    /**
	 * @see #getFormatSpec()
	 */
    public void setFormatSpec(String formatSpec);

    /** 
     * Returns the sort order of this unit in the list of all units
     * used to create some ordering for the GUI.
     */
    public Integer getSortOrder();
    
    /** 
     * @param aSortOrder The new sort order of this unit.
     */
    public void setSortOrder(Integer aSortOrder);
    
    /**
     * Divide by the conversion factor format the number.
     */
    public String format(Number aNumber);
    
    /**
     * parses the given String and converts and multiplies it with the conversion factor.
     * 
     * @throws ParseException based on the formatter used.
     */
    public Number parse(String aNumber) throws ParseException;

}
