/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.constraints;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.charsize.CharSizeMap;
import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.I18NConstants;
import com.top_logic.util.Resources;

/**
 * A StringLengthConstraint with respects line breaks, font, font size, ...
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public class ExtendedStringLengthConstraint extends AbstractStringConstraint {

    /** The amount of rows / lines to allow. */
    public final int rows;

    /** The amount of columns to allow. */
    public final int columns;

    /** Additional check for maximum length of input. May be -1 to skip this check. */
    public final int maxLength;

    /** The font size map for fonts with different char sizes. */
    public final CharSizeMap sizeMap;



    /**
     * Creates a new instance of this class.
     */
    public ExtendedStringLengthConstraint(int rows, int columns, CharSizeMap sizeMap) {
        this(rows, columns, sizeMap, -1);
    }

    /**
     * Creates a new instance of this class.
     */
    public ExtendedStringLengthConstraint(int rows, int columns, CharSizeMap sizeMap, int maxLength) {
        this.rows = rows;
        this.columns = columns;
        this.sizeMap = sizeMap;
        this.maxLength = maxLength;
    }



    @Override
    protected boolean checkString(String value) throws CheckException {
        if (StringServices.isEmpty(value)) return true;

		String cutted = StringServices.cutString(value, Integer.MAX_VALUE, columns, sizeMap);
		int actualLineBreaks = StringServices.count(cutted, "\n");
		if (actualLineBreaks >= rows) {
			throw new CheckException(getCheckExceptionMessage(actualLineBreaks + 1));
        }

		int length = value.length();
        if ((maxLength >= 0) && (length > maxLength)) {
			throw new CheckException(Resources.getInstance().getString(
				I18NConstants.STRING_TO_LONG__MAXIMUM_LENGTH_OVERFLOW.fill(Integer.valueOf(maxLength),
					Integer.valueOf(length), Integer.valueOf(length - maxLength))));
        }

        return true;
    }

	/**
	 * Gets the I18Ned CheckException message.
	 * 
	 * @param actualRows
	 *        The number of rows entered.
	 */
	protected String getCheckExceptionMessage(int actualRows) {
		return Resources.getInstance().getString(I18NConstants.EXTENDED_STRING_TOO_LONG__ROWS_COLUMNS_ACTUAL.fill(
			Integer.valueOf(rows), Integer.valueOf(columns), Integer.valueOf(actualRows)));
	}

}
