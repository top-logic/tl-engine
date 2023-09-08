/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office.excel;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.top_logic.base.office.POIUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.constraints.AbstractStringConstraint;
import com.top_logic.util.Resources;

/**
 * Constraint that checks if a given String describes an area or a cell within an
 * excel sheet. A cell consists of one or two letters (A - IV) and a number (1 -
 * 65536).
 * 
 * A valid area is defined via a start cell and an end cell. The start cell must
 * be "smaller" than the end cell, i.e. the letter part and the number part must
 * be less or equal.
 * 
 * @author <a href="mailto:tbe@top-logic.com">tbe</a>
 */
public class ExcelCellConstraint extends AbstractStringConstraint {
	
	private static final Pattern CELL_PATTERN = Pattern.compile("[a-zA-Z]{1,2}\\d{1,5}");

	private boolean isArea;

	public ExcelCellConstraint() {
		this.isArea = false;
    }
	
	/**
	 * C'tor
	 * 
	 * @param isArea
	 *            if <code>true</code> this constraint checks for a table area,
	 *            otherwise it checks single cells.
	 */
	public ExcelCellConstraint(boolean isArea) {
		this.isArea = isArea;
	}

	@Override
	protected boolean checkString(String value) throws CheckException {
		if (StringServices.isEmpty(value)) {
			return true;
		}

		if(isArea) {
			return checkAreaValidity(value);
		}
		else {
			return checkCellValidity(value);
		}
	}
	
	/**
	 * Checks whether a given string is consistent with the cell pattern
	 * {@link #CELL_PATTERN} and the the defined rows and columns are within the
	 * Excel specifications.
	 * 
	 * @return <code>true</code> if the given string matches the pattern and has
	 *         valid rows/columns
	 * @throws CheckException
	 *             if either the string does not match the pattern or the
	 *             columns/rows are outside the specifications
	 */
	public static boolean checkCellValidity(String aCell) throws CheckException {
		if (!isValidCell(aCell)) {
			return false;
		}
		int theCol = POIUtil.getColumn(aCell);
		int theRow = POIUtil.getRow(aCell);

		if ((theCol < 0) || (theCol > 255) || (theRow < 0) || (theRow > 65535)) {
			throw new CheckException(Resources.getInstance().getMessage(I18NConstants.ERROR_POSITION_WRONG, aCell));
		}
		return true;
	}

	/**
	 * Checks whether the given string consists of two cell descriptions
	 * separated by a ':'.
	 * 
	 * @param aCellArea
	 *            the area description
	 * @return <code>true</code> if two valid cell descriptions are found.
	 * @throws CheckException
	 *             if the given string does not contain two valid cell
	 *             descriptions separated by a ':'.
	 */
	public static boolean checkAreaValidity(String aCellArea) throws CheckException {
		int dColIndx = aCellArea.indexOf(':');

		if (dColIndx < 0) {
			throw new CheckException(Resources.getInstance().getMessage(I18NConstants.ERROR_CELL_AREA_WRONG, aCellArea));
		}

		String theStart = aCellArea.substring(0, dColIndx);
		String theEnd   = aCellArea.substring(dColIndx + 1);

		if (!checkCellValidity(theStart) || !checkCellValidity(theEnd)) {
			return false;
		}

		int theStartCol = POIUtil.getColumn(theStart);
		int theEndCol   = POIUtil.getColumn(theEnd);
		int theStartRow = POIUtil.getRow(theStart);
		int theEndRow   = POIUtil.getRow(theEnd);

		if ((theStartCol < 0) || (theStartRow > 65535)) {
			throw new CheckException(Resources.getInstance().getMessage(I18NConstants.ERROR_POSITION_START_WRONG__AREA,
				aCellArea));
		}
		else if ((theEndCol < 0) || (theEndRow > 65535)) {
			throw new CheckException(Resources.getInstance().getMessage(I18NConstants.ERROR_POSITION_END_WRONG__AREA,
				aCellArea));
		}
		else if ((theEndCol < theStartCol) || (theEndRow < theStartRow)) {
			throw new CheckException(Resources.getInstance().getMessage(I18NConstants.ERROR_COORDS_WRONG__AREA,
				aCellArea));
		}
		return true;
	}

	/**
	 * Checks whether a given string is consistent with the cell pattern
	 * {@link #CELL_PATTERN}
	 * 
	 * @param aString
	 *            a String to check
	 * @return <code>true</code> if the given string matches the pattern
	 * @throws CheckException
	 *             if the string does not match the pattern.
	 */
	private static boolean isValidCell(String aString) throws CheckException {
		Matcher theMatcher = CELL_PATTERN.matcher(aString);

		if (!theMatcher.matches()) {
			throw new CheckException(Resources.getInstance().getMessage(I18NConstants.ERROR_INVALID_INPUT, aString));
		}

		return true;
	}
}
