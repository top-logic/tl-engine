/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap.currency;

import java.util.Date;

import com.top_logic.knowledge.wrap.unit.Unit;
import com.top_logic.util.Utils;

/**
 * Class represents a value in a specific unit.
 * 
 * This can be used for money or weight where values have to be
 * comparable depending on their conversion factor.
 * 
 * When using the {@link #add(Amount)} and {@link #sub(Amount)} methods
 * the returned amount will be in the {@link #getSystemUnit() base unit}. 
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class Amount implements Comparable {

    /** The value stored based on given unit. */
    private final double value;

    /** The unit as base for the stored value. */
    private final Unit unit;

    /** Value for debugging reasons. */
    private final String debugname;

    /** Date on which this amount is valid (needed for exchange rates in currencies). */ 
    private final Date date;

    /**
	 * Creates a {@link Amount}.
	 * 
	 * @param aValue
	 *        The amount to be represented.
	 * @param aUnit
	 *        The unit, must not be <code>null</code>.
	 */
    public Amount(double aValue, Unit aUnit) {
        this(aValue, aUnit, null);
    }
    
    /**
	 * Creates a {@link Amount} on a specified date.
	 * 
	 * If the given date is not null and the given unit is a {@link Currency}, the conversion factor
	 * will take respect of this fact (represents the exchange ratio on that day).
	 * 
	 * @param aValue
	 *        The amount to be represented.
	 * @param aUnit
	 *        The unit, must not be <code>null</code>.
	 * @param aDate
	 *        The date this amount is valid, may be <code>null</code>.
	 */
    public Amount(double aValue, Unit aUnit, Date aDate) {
        if (aUnit == null) {
            throw new IllegalArgumentException("Given unit is null");
        }

        this.value     = aValue;
        this.date      = aDate;
        this.unit      = aUnit;
        this.debugname = (aValue + " " + aUnit.getName()) + ((this.date != null) ? " (" + this.date + ')' : "");
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
	public int compareTo(Object anObject) {
        double theValue = ((Amount) anObject).getValue(this.unit);
        double theOwn   = this.getValue(this.unit, this.date);

        return Double.compare(theOwn, theValue);
    }

    @Override
    public boolean equals(Object anObject) {
		if (anObject == this) {
			return true;
		}
    	if (anObject instanceof Amount) {
    		Amount other = (Amount) anObject;
    		boolean result = value == other.value && unit.equals(other.unit);
    		if (!result) {
    			return false;
    		}
    		return Utils.equals(date, other.date);
    	} else {
    		return false;
    	}
    }

    @Override
    public int hashCode() {
    	int hash = 17 + 13 * (int) value;
    	hash = 31 * hash + unit.hashCode();
    	hash = 37 * hash + ((date == null) ? 0 : date.hashCode());
        return hash;
    }

    @Override
	public String toString() {
        return this.debugname;
    }

    /**
     * This method returns the value.
     * 
     * @return    Returns the value.
     */
    public double getValue() {
        return (this.value);
    }

    /**
     * Return the value converted into the given unit.
     * 
     * @param     anotherUnit    The unit to convert the value to, must not be <code>null</code>.
     * @return    Returns the requested value.
     */
    public double getValue(Unit anotherUnit) {
        return (this.getValue(anotherUnit, this.date));
    }

    /**
     * This method returns the unit.
     * 
     * @return    Returns the unit.
     */
    public Unit getUnit() {
        return (this.unit);
    }

    /** 
     * Add the given amount to this one.
     * 
     * @param    anAmount    The amount to be added, must not be <code>null</code>.
     * @return   The new, resulting amount in system currency.
     */
    public Amount add(Amount anAmount) {
        Unit   theSystem = this.getSystemUnit();
        double theValue  = anAmount.getValue(theSystem);
        double theOwn    = this.getValue(theSystem);

        return (new Amount(theOwn + theValue, theSystem, this.date));
    }

    /** 
     * Subtract the given amount from this one.
     * 
     * @param    anAmount    The amount to be subtracted, must not be <code>null</code>.
     * @return   The new, resulting amount in system currency.
     */
    public Amount sub(Amount anAmount) {
        Unit   theSystem = this.getSystemUnit();
        double theValue  = anAmount.getValue(theSystem);
        double theOwn    = this.getValue(theSystem);

        return (new Amount(theOwn - theValue, theSystem, this.date));
    }

    /** 
     * Convert this value into another currency.
     * 
     * When converting, the new amount will have the given date for later conversion.
     * 
     * @param    aUnit    The currency to convert the value to, must not be <code>null</code>.
     * @param    aDate    The date for taking the exchange ration, may be <code>null</code>.
     * @return   The new created amount, never <code>nulll</code>.
     */
    public Amount convert(Unit aUnit, Date aDate) {
        double theValue = this.getValue(aUnit, aDate);

        return (new Amount(theValue, aUnit, aDate));
    }

    /** 
     * Return the value converted into the given unit on the given date.
     * 
     * @param     anotherUnit    The unit to convert the value to, must not be <code>null</code>.
     * @param     aDate          The date the conversion factor refers to, may be <code>null</code>.
     * @return    Returns the requested value.
     */
    protected double getValue(Unit anotherUnit, Date aDate) {
        double theOwnFactor = this.getConversionFactor(this.unit, aDate);
        double theExtFactor = this.getConversionFactor(anotherUnit, aDate);

        return ((theOwnFactor / theExtFactor) * this.value);
    }

    /** 
     * Return the conversion factor for the given unit on the date of this amount.
     * 
     * If the given unit is a {@link Currency} and the date is set in this instance,
     * the conversion factor on that date will be returned.
     * 
     * @param    aUnit    The unit to get the conversion factor for, must not be <code>null</code>.
     * @param    aDate    The date the conversion factor has to be valid for, may be <code>null</code>. 
     * @return   The conversion factor.
     */
    protected double getConversionFactor(Unit aUnit, Date aDate) {
        if ((aDate != null) && (aUnit instanceof Currency)) {
            return ((Currency) aUnit).getConversionFactor(aDate);
        }
        else {
            return aUnit.getConversionFactor();
        }
    }

    /** 
     * Return the system currency which is always the currency returned when
     * {@link #add(Amount)} and {@link #sub(Amount)} has been called.
     * 
     * @return    The requested system currency, never <code>null</code>.
     */
    protected Unit getSystemUnit() {
        return this.unit.isBaseUnit() ? this.unit : this.unit.getBaseUnit();
    }
}
