/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import com.top_logic.basic.DateUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.model.TLObject;

/**
 * Specifies that an attribute is valid for some time only.
 * 
 * <p>
 * The informations is purely descriptive no actions are enforced. Whenever
 * {@link Wrapper#setValue(String, Object)} for the Attribute is called the validity will be
 * prolonged. One may call {@link TLObject#tTouch()} which will extend the validity without changing
 * the Attribute. In wired cases you may call {@link TLObject#tTouch()} which will set the base-date
 * for the validity to the given date.
 * </p>
 * 
 * <p>
 * The information of this object is persisted via
 * {MetaElementHolderWrapperProxy#ATTRIBUTE_SUFFIX_LAST_CHANGED}
 * </p>
 * 
 * <p>
 * Main entry point for reading information is {@link #getNextTimeout(Date)}.
 * </p>
 * 
 * refactored validity check
 *
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class ValidityCheck {

	private static final ValidityCheck NO_PATTERN_INSTANCE = new ValidityCheck();

    /** Constant for duration parameter. */
    private static final String PARAM_DURATION = "duration:";

    /** Constant for read only parameter. */
    private static final String PARAM_READ_ONLY = "readOnly:";

    /** Constant for reference date parameter. */
    private static final String PARAM_REFERENCE_DATE = "referenceDate:";

    /** Constant for type parameter. */
    private static final String PARAM_TYPE = "type:";

    /** Constant for validity duration parameter. */
    private static final String PARAM_VALIDITY = "validity:";

    /** Constant for fixed type. */
    public static final String TYPE_FIXED = "fixed";

    /** Constant for default type. */
    public static final String TYPE_NONE = "none";


	/**
	 * Special date (will be used for de-activating validity checks of an instance of an attribute).
	 */
    public static final Date INVALID_DATE = new Date(-1);

	/**
	 * Special flag (will be used for de-activating validity checks of an instance of an attribute).
	 */
    public static final int INVALID_DATE_FIELD = -1;

	/**
	 * Special flag (will be used for de-activating validity checks of an instance of an attribute).
	 */
    public static final long CHECK_DURATION_NOT_SET = -1;

    /** 
     * One of the Constants found in {@link Calendar} 
     * 
     * Via configuration only Calendar.MONTH, YEAR or DAY_OF_YEAR can be set
     * 
     * @see #getCalendarField(char)
     */
    private int     validityField;

    /** Amount the Date is valid specified in the unit defined by {@link #validityField}  */
    private int     validityAmount;
    
    /**
     * Duration in milliseconds when Attributes should be brought to the users attention.
     * 
     * E.G. some traffic-ligth will switch to yellow indicating that the Attribute should be checked.
     */
    private long    checkDuration;
    
    /**
     * Specifies how the next validity date will be calculated.
     * 
     * One of the TYPE_ constants defined in this class.
     * @see #TYPE_FIXED
     * @see #TYPE_NONE
     */
    private String  type;
    
    /** Base Date for calculations type is TYPE_FIXED */
    private Date    referenceDate;
    
    /** Read-only (as seen from GUI) , some kind of background-process may touch it, though */
    private boolean readOnly;

    /** The original specification for this Object */
    private String  validityCheck;

    /**
     * Create a new instance of this class.
     */
    private ValidityCheck() {
        this.checkDuration  = ValidityCheck.CHECK_DURATION_NOT_SET;
        this.validityAmount = -1;
        this.validityField  = Calendar.DAY_OF_YEAR;
        this.type           = TYPE_NONE;
        this.referenceDate  = null;
    }

    @Override
	public String toString() {
        return (this.getClass().getName() + " [" + this.toStringValues() + ']');
    }

    /**
     * An additional time frame used to indicate that the validity will expire soon. Within
     * the given duration before the expiration of an attribute the attribute will be
     * flagged "pending".
     *
     * @return the length of the time frame (in milliseconds) that the attribute is to be
     *         flagged pending; a value less or equal to zero indicates no such time frame;
     *         -1 if validity check is disabled
     */
    public long getCheckDuration() {
        return (this.checkDuration);
    }

    /**
     * Get the next date when the attribute expires.
     *
     * @param lastTouch
     *        the starting point for calculation, usually last changed time of an attribute
     *        or current date (needed for calculation), may be <code>null</code>
     * @return The next timeout date; may be <code>null</code>, if the validity doesn't
     *         expire (lastTouch is INVALID_DATE) / isn't active
     */
    public Date getNextTimeout(Date lastTouch) {
        if (!this.isActive() || ValidityCheck.INVALID_DATE.equals(lastTouch)) {
            return null;
        }
        if (lastTouch == null) {
            if (referenceDate != null) {
                lastTouch = getTouchTimeFor(referenceDate);
            }
            else {
                // Last touch and reference date is null. Don't know what to do.
                // Assume it is expired since end of last year.
                lastTouch = getTouchTimeFor(DateUtil.adjustDateToYearEnd(DateUtil.addYears(new Date(), -1)));
            }
        }
        lastTouch = DateUtil.adjustToDayEnd(lastTouch);
        if (TYPE_NONE.equals(this.type)) {
            Date nextTimeout = this.calcuateNewTimeoutDate(lastTouch);
            if (this.referenceDate == null) {
                return nextTimeout;
            }
            else {
                // What does a reference date mean without type = fixed??
                return nextTimeout.before(referenceDate) ? this.referenceDate : nextTimeout;
            }
        }
        else if (TYPE_FIXED.equals(this.type)) {
            if (this.referenceDate == null) {
                // Type fixed without a reference date makes no sense
                return this.calcuateNewTimeoutDate(lastTouch);
            }
            else {
                Date theRef = this.referenceDate;
                while (theRef.before(lastTouch)) {
					Calendar theCal = CalendarUtil.createCalendar(theRef);
                    theCal.add(this.validityField, this.validityAmount);
                    theRef = theCal.getTime();
                }

                Date theFirst = new Date(theRef.getTime() - this.checkDuration);
                if (theFirst.before(lastTouch)) {
                    return this.calcuateNewTimeoutDate(theRef);
                }
                else {
                    return theRef;
                }
            }
        }
        return null;
    }

    /**
     * Calculates the date on which an attribute must be touched so that
     * generate(resultDate) will return the given date.
     *
     * @param aDate
     *        the desired timeout date; <code>null</code> will return <code>null</code>
     * @return the date to touch an attribute, so that generate(resultDate) will return the
     *         given date; may be <code>null</code>
     */
    public Date getTouchTimeFor(Date aDate) {
        if (!this.isActive() || ValidityCheck.INVALID_DATE.equals(aDate)) {
            return null;
        }
        if (aDate == null) {
            return ValidityCheck.INVALID_DATE;
        }
        Date theDate = DateUtil.adjustToDayEnd(aDate);

        if (TYPE_FIXED.equals(this.type) && referenceDate != null) {
            Date theRef = this.referenceDate;
			Calendar theCal = CalendarUtil.createCalendar(theRef);
            Date theLast = theRef;
            while (theRef.before(theDate)) {
                theLast = theRef;
                theCal.add(this.validityField, this.validityAmount);
                theRef = theCal.getTime();
            }
            theDate = (DateUtil.distance(theDate, theLast) < DateUtil.distance(theDate, theRef)) ? theLast : theRef;
        }
        return calcuateNewTouchDate(theDate);
    }

    /**
     * Check, if the validity check is enabled for this meta attribute.
     *
     * @return <code>true</code> if validity check is active.
     */
    public boolean isActive() {
        return (this.validityAmount >= 0 && validityField >= 0);
    }

    /**
     * Check whether the attribute's validity is readOnly and may not be set manually by an
     * administrator.
     *
     * @return <code>true</code>, if the attribute's next timeout is readOnly and may not
     *         be set manually by an administrator, <code>false</code> otherwise.
     */
    public boolean isReadOnly() {
        return this.readOnly;
    }

    /**
     * Calculate a new timeout date out of the given one.
     *
     * @param aDate
     *        The date to be used as starting point, must not be <code>null</code>.
     * @return The new generated timeout date, never <code>null</code>.
     */
    private Date calcuateNewTimeoutDate(Date aDate) {
		Calendar theCal = CalendarUtil.createCalendar(aDate);
        theCal.add(this.validityField, this.validityAmount);
        return DateUtil.adjustToDayEnd(theCal);
    }

    /**
     * Calculate a new touch date out of the given one.
     *
     * @param aDate
     *        The date to be used as end point, must not be <code>null</code>.
     * @return The new generated touch date, never <code>null</code>.
     */
    private Date calcuateNewTouchDate(Date aDate) {
		Calendar theCal = CalendarUtil.createCalendar(aDate);
        theCal.add(this.validityField, -this.validityAmount);
        return DateUtil.adjustToDayEnd(theCal);
    }

    /**
     * Initialize this instance with the given pattern.
     *
     * The method will parse the given pattern and adjust the different instance variables
     * to the values in the pattern.
     *
     * @param    aPattern    The configuration to be used, must not be <code>null</code>.
     */
    private void init(String aPattern) {
        // TODO Use StringServices.getAllSemicolonSeparatedValues();
        
        String[] theValues = StringServices.toArray(aPattern, ";");

        for (int thePos = 0; thePos < theValues.length; thePos++) {
            String theToken = theValues[thePos].trim();

            if (theToken.startsWith(PARAM_VALIDITY)) {
                String theString = theToken.substring(PARAM_VALIDITY.length());
                if (StringServices.isEmpty(theString)) {
                    Logger.error("Missing '" + PARAM_VALIDITY + "' information.", this);
                }
                else {
                    String theAmount = theString.substring(0, theString.length() - 1);
                    char theDateFieldChar = theString.charAt(theString.length() - 1);

                    this.validityCheck  = theString;
                    this.validityAmount = Integer.parseInt(theAmount);
                    this.validityField  = getCalendarField(theDateFieldChar);
                    if (this.validityField == INVALID_DATE_FIELD) {
                        Logger.error("Invalid date field modifier: '" + theDateFieldChar + "'. Must be on of [y | M | d ].", this);
                    }
                }
            }
            else if (theToken.startsWith(PARAM_DURATION)) {
                String theString = theToken.substring(PARAM_DURATION.length());
                // this assumes always: 1 month = 30 days!
                try {
                    this.checkDuration = StringServices.durationStringToLong(theString);
                }
                catch (RuntimeException ex) {
                    Logger.error("Invalid duration field modifier: " + ex.getMessage(), ex, this);
                }
            }
            else if (theToken.startsWith(PARAM_TYPE)) {
                this.type = theToken.substring(PARAM_TYPE.length());
            }
            else if (theToken.startsWith(PARAM_REFERENCE_DATE)) {
                String theString = theToken.substring(PARAM_REFERENCE_DATE.length());
                try {
                    this.referenceDate = DateUtil.adjustToDayEnd(getDateFormat().parse(theString));
                }
                catch (ParseException ex) {
                    Logger.error("Unable to parse date " + theString, ex, this);
                }
            }
            else if (theToken.startsWith(PARAM_READ_ONLY)) {
                this.readOnly = Boolean.valueOf(theToken.substring(PARAM_READ_ONLY.length())).booleanValue();
            }
            else if (!StringServices.isEmpty(theToken)) {
                Logger.error("Unknown parameter in validityCheck pattern: " + theToken, this);
            }
        }
    }

    /**
     * Return the calendar field for the given character.
     *
     * @param aChar
     *        The character to be converted
     * @return The calendar representation of the given char or {@link #INVALID_DATE_FIELD}
     *         if the char is invalid
     */
    private int getCalendarField(char aChar) {
        switch (aChar) {
            case 'M':   return (Calendar.MONTH);
            case 'y':   return (Calendar.YEAR);
            case 'd':   return (Calendar.DAY_OF_YEAR);
            default :   return INVALID_DATE_FIELD;
        }
    }

    /**
     * Values to be displayed in {@link #toString()}.
     *
     * @return    The debugging values as string.
     */
    private String toStringValues() {
        return "validityCheck: '" + this.validityCheck +
               "', checkDuraction: " + this.checkDuration +
               ", type: " + this.type +
               ", referenceDate: " + this.referenceDate;
    }

	/** Date formatter for parsing the configured date. */
	private static DateFormat getDateFormat() {
		return CalendarUtil.newSimpleDateFormat("yyyyMMdd");
	}

	/**
	 * Returns a {@link ValidityCheck} based on the given pattern.
	 * 
	 * @param pattern
	 *        May be <code>null</code> or empty. The method will parse the given pattern and adjust
	 *        the different instance variables to the values in the pattern.
	 */
	public static ValidityCheck getValidityCheck(String pattern) {
		if (StringServices.isEmpty(pattern)) {
			return ValidityCheck.NO_PATTERN_INSTANCE;
		}
		ValidityCheck validityCheck = new ValidityCheck();
		validityCheck.init(pattern);
		return validityCheck;
	}
}
