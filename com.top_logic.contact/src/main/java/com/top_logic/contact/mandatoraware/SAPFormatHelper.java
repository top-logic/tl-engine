/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.mandatoraware;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.util.TLContext;

/**
 * @author    <a href=mailto:fsc@top-logic.com>Mandelfried Müller</a>
 */
public class SAPFormatHelper {

	private static final String[]  ZERO_FILL         = new String[]{"", "0", "00", "000", "0000", "00000", "000000", "0000000", "00000000", "000000000", "0000000000"};

	private static final String DATE_10_FORMAT_PATTERN = "yyyy-MM-dd";

	private static final String DATE_8_FORMAT_PATTERN = "yyyyMMdd";

	/** 
	 * Static stuff only 
	 */
	private SAPFormatHelper() {
		super();
	}

	/** 
	 * Fill a SAP no with leading '0's so that it has 10 digits
	 * 
	 * @param aSAPN	the SAP no (possibly lacking leading 0s)
	 * @return the 10-digit SAP no or <code>null</code> if aSAPN is
	 * <code>null</code> or if it contains chars other than digits.
	 */
	public static String fillSAPNo(String aSAPN) {
		if (aSAPN == null) {
			return null;
		}

		int theLen = aSAPN.length();
		
		if (theLen > 10) {
			return null;
		}
		
		if (theLen > 0 && !Character.isDigit(aSAPN.charAt(0))) {
		    return aSAPN;
		}
		
		return ZERO_FILL[10-theLen] + aSAPN;
	}


	/** 
	 * Cut off the leading 0s of a SAP no
	 * 
	 * @param aSAPNo	the SAP no
	 * @return the SAP no without leading 0s, <code>null</code> if
	 * the SAP no was <code>null</code>.
	 */
	public static String stripSAPNo(String aSAPNo) {
		if (StringServices.isEmpty(aSAPNo)) {
			return aSAPNo;
		}
		int len = aSAPNo.length();
		int pos = 0;
		while (pos < len && aSAPNo.charAt(pos) == '0') {
		    pos++;
		}
		if (pos > 0) {
			aSAPNo = aSAPNo.substring(pos);
		}
		
		return aSAPNo;
	}
	
	/** 
	 * Get the Date from the String given by SAP
	 * 
	 * @param aSAPDate the date string from SAP
	 * @return the date or <code>null</code> if parsing fails.
	 */
	public static Date getDate(String aSAPDate) {
		try {
			if(StringServices.isEmpty(aSAPDate)){
				return null;
			}
			switch (aSAPDate.length()) {
				case 8:
					return parse(DATE_8_FORMAT_PATTERN, aSAPDate);
				case 10:	
					return parse(DATE_10_FORMAT_PATTERN, aSAPDate);
				default:
					return null;
			}
		} catch (ParseException e) {
			return null;
		}
	}

	/**
	 * Passes the date using the given pattern.
	 * 
	 * @param sapDate
	 *        the date to parse
	 * @param pattern
	 *        the pattern to use to parse the date
	 * 
	 * @return the {@link Date} represented by the given date String
	 * 
	 * @throws RuntimeException
	 *         if parsing fails.
	 */
	public static Date getDate(String sapDate, String pattern) {
		try {
			return parse(pattern, sapDate);
		} catch (ParseException e) {
			throw new RuntimeException("'" + sapDate + "' can not be parsed using pattern '" + pattern + "'", e);
		}
	}

	private static Date parse(String pattern, String aSAPDate) throws ParseException {
		// can not use static instance variables as DateFormat is not thread safe.
		return CalendarUtil.newSimpleDateFormat(pattern).parse(aSAPDate);
	}
	
	/** 
	 * Get the Double from the String given by SAP
	 * 
	 * @param aSAPDouble the date string from SAP
	 * @return the double or <code>null</code> if parsing fails.
	 */
	public static Double getDouble(String aSAPDouble) {
		try {
			// can not use static instance variables as DecimalFormat is not thread safe.
			DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ENGLISH);
			DecimalFormat numberFormat = new DecimalFormat("#,##0.0000", symbols);
			return Double.valueOf(numberFormat.parse(aSAPDouble).doubleValue());
		} catch (ParseException e) {
			return null;
		}
	}
	
	public static String formatPrice(Double aPrize) {
		NumberFormat thePriceFormat = new DecimalFormat("#,##0.00", new DecimalFormatSymbols(TLContext.getLocale()));
		return thePriceFormat.format(aPrize);
	}
	
	public static String formatExchRate(Double aRate) {
		NumberFormat theExchRateFormat = new DecimalFormat("#,##0.0000", new DecimalFormatSymbols(TLContext.getLocale()));
		return theExchRateFormat.format(aRate);
	}
	
	public static String formatQuantity(Double aQuantity) {
		NumberFormat theQuantityFormat = new DecimalFormat("#,##0.##", new DecimalFormatSymbols(TLContext.getLocale()));
		return theQuantityFormat.format(aQuantity);
	}
}
