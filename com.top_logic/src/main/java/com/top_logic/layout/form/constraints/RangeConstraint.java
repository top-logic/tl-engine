/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.constraints;

import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.Mappings;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.I18NConstants;
import com.top_logic.util.Resources;

/**
 * A generic Range constraint based on {@link Comparable}s.
 * 
 * @author     <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class RangeConstraint extends AbstractConstraint {

	private Mapping<Object, Object> _conversion = Mappings.identity();
	
    /** Lower Range, may be null */
	private Comparable lower;
    
	private boolean _lowerInclusive = true;
	
    /** Upper Range, may be null */
	private Comparable upper;

	private boolean _upperInclusive = true;
	
	private ResKey2 _errorKeyToBig;

	private ResKey2 _errorKeyToSmall;

    /**
	 * Creates a new RangeConstraint without ranges. Use the method
	 * {@link #setLowerAndUpper(Comparable, Comparable)} to set the lower and
	 * upper bound.
	 */
    public RangeConstraint() {
    	this.lower = null;
    	this.upper = null;
    }
    
    /** 
     * Create a new RangeConstraint with lower an upper bounds.
     * 
     * @param aLower null indicates no lower limit
     * @param anUpper null indicates no anUpper limit
     * 
     * @throws IllegalArgumentException when both are null or aLower &gt; anUpper.
     */
    public RangeConstraint(Comparable aLower, Comparable anUpper) {
    	setLowerAndUpper(aLower, anUpper);
    }

	/**
	 * The I18N key that is used to report an overflow with arguments for value and upper bound.
	 */
	public final ResKey2 getErrorKeyToBig() {
		if (_errorKeyToBig == null) {
			if (isUpperInclusive()) {
				return I18NConstants.VALUE_TO_BIG__VALUE_MAXIMUM;
			} else {
				return I18NConstants.VALUE_TO_BIG__VALUE_BOUND;
			}
		}
		return _errorKeyToBig;
	}

	/**
	 * Sets the {@link #getErrorKeyToBig()} property.
	 * 
	 * @return This instance for call chaining.
	 */
	public RangeConstraint setErrorKeyToBig(ResKey2 errorKeyToBig) {
		_errorKeyToBig = errorKeyToBig;
		return this;
	}

	/**
	 * The I18N key that is used to report an underflow with arguments for value and lower bound.
	 */
	public final ResKey2 getErrorKeyToSmall() {
		if (_errorKeyToSmall == null) {
			if (isLowerInclusive()) {
				return I18NConstants.VALUE_TO_SMALL__VALUE_MINIMUM;
			} else {
				return I18NConstants.VALUE_TO_SMALL__VALUE_BOUND;
			}
		}
		return _errorKeyToSmall;
	}

	/**
	 * Sets the {@link #getErrorKeyToSmall()} property.
	 * 
	 * @return This instance for call chaining.
	 */
	public RangeConstraint setErrorKeyToSmall(ResKey2 errorKeyToSmall) {
		_errorKeyToSmall = errorKeyToSmall;
		return this;
	}

	/**
	 * Sets the {@link #getErrorKeyToSmall()} property.
	 * 
	 * @return This instance for call chaining.
	 */
	public RangeConstraint setErrorKeyToSmall(ResKey errorKeyToSmall) {
		return setErrorKeyToSmall(errorKeyToSmall.asResKey2());
	}

    /**
     * Check again lower an upper values.
     *  
     *  @return true as we have no dependencies we know of.
     */
    @Override
	public boolean check(Object aValue) throws CheckException {
        if (aValue == null) {
        	// Note: An specialized constraint must not imply a mandatory constraint.
            return (true);
        }
        
		Object testValue = convert(aValue);
		if (getLower() != null && lowerBoundMissmatch(testValue))
            throw new CheckException(getValueToSmallMessage(aValue));

		if (getUpper() != null && upperBoundMissmatch(testValue))
            throw new CheckException(getValueToBigMessage(aValue));
        
        return true;
    }

	private Object convert(Object aValue) {
		return _conversion.map(aValue);
	}

	private boolean lowerBoundMissmatch(Object testValue) {
		int comparison = getLower().compareTo(testValue);
		return isLowerInclusive() ? comparison > 0 : comparison >= 0;
	}

	private boolean upperBoundMissmatch(Object testValue) {
		int comparison = getUpper().compareTo(testValue);
		return isUpperInclusive() ? comparison < 0 : comparison <= 0;
	}

	/**
	 * This method returns the value to big message for the
	 * {@link CheckException} and NEVER <code>null</code>. This method is a
	 * hook for subclasses to return detailed message.
	 * 
	 * @param aValue
	 *            A value that is to big for the declared range.
	 * @return Returns the value to big message for the {@link CheckException}
	 */
	protected String getValueToBigMessage(Object aValue) {
		return Resources.getInstance().getString(getErrorKeyToBig().fill(aValue, getUpper()));
	}

	/**
	 * This method returns the value to small message for the
	 * {@link CheckException} and NEVER <code>null</code>. This method is a
	 * hook for subclasses to return detailed message.
	 * 
	 * @param aValue
	 *            A value that is to small for the declared range.
	 * @return Returns the value to small message for the {@link CheckException}
	 */
	protected String getValueToSmallMessage(Object aValue) {
		return Resources.getInstance().getString(getErrorKeyToSmall().fill(aValue, getLower()));
	}

	/**
	 * The lower bound for comparison.
	 * 
	 * @see #isLowerInclusive()
	 */
	public final Comparable getLower() {
		return this.lower;
	}

	/**
	 * The upper bound for comparison.
	 * 
	 * @see #isUpperInclusive()
	 */
	public final Comparable getUpper() {
		return this.upper;
	}

	/**
	 * This method sets the lower and upper bound.
	 * 
	 * @param aLower
	 *            A lower bound must NOT be <code>null</code> and before the
	 *            upper bound.
	 * @param anUpper
	 *            A upper bound must NOT be <code>null</code> and after the
	 *            upper bound.
	 */
	public void setLowerAndUpper(Comparable aLower, Comparable anUpper) {
		if (aLower == null && anUpper == null) {
            throw new IllegalArgumentException("Either upper or lower bound must be != null");
        }
        if (aLower != null && anUpper != null && aLower.compareTo(anUpper) > 0) {
            throw new IllegalArgumentException("Lower bound "              + aLower 
                                            + " greater than upper bound " + anUpper);
        }
        
        this.lower = aLower;
        this.upper = anUpper;
	}
	
	/**
	 * Whether the {@link #getLower()} bound is inclusive.
	 */
	public final boolean isLowerInclusive() {
		return _lowerInclusive;
	}

	/**
	 * @see #isLowerInclusive()
	 */
	public final void setLowerInclusive(boolean lowerInclusive) {
		_lowerInclusive = lowerInclusive;
	}

	/**
	 * Whether the {@link #getUpper()} bound is inclusive.
	 */
	public final boolean isUpperInclusive() {
		return _upperInclusive;
	}

	/**
	 * @see #isUpperInclusive()
	 */
	public final void setUpperInclusive(boolean upperInclusive) {
		_upperInclusive = upperInclusive;
	}

	/**
	 * The {@link Mapping} to apply to the current field value before comparision with
	 * {@link #getLower()} and {@link #getUpper()}.
	 */
	public final Mapping<?, ?> getConversion() {
		return _conversion;
	}

	/**
	 * @see #getConversion()
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public final void setConversion(Mapping conversion) {
		_conversion = conversion;
	}

}
