/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import com.top_logic.basic.StringServices;
import com.top_logic.mig.html.HTMLConstants;

/**
 * Class that provides {@link View} that writes a given number of {@link HTMLConstants#NBSP}.
 * 
 * @since 5.7.3
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class HTMLSpaces {

	private static View ONE_SPACE = createSpaceView(1);

	private static View TWO_SPACE = createSpaceView(2);

	private static View THREE_SPACE = createSpaceView(3);

	private static View FOUR_SPACE = createSpaceView(4);

	private static View FIVE_SPACE = createSpaceView(5);

	/**
	 * Creates a view that writes a given number of {@link HTMLConstants#NBSP}.
	 * 
	 * @param numberSpaces
	 *        Number (positive) of spaces to write.
	 */
	public static View createSpaces(int numberSpaces) {
		switch (numberSpaces) {
			case 1:
				return ONE_SPACE;
			case 2:
				return TWO_SPACE;
			case 3:
				return THREE_SPACE;
			case 4:
				return FOUR_SPACE;
			case 5:
				return FIVE_SPACE;
			default:
				if (numberSpaces < 0) {
					throw new IllegalArgumentException("Unable to write a negative number of spaces.");
				}
				return createSpaceView(numberSpaces);
		}
	}

	private static View createSpaceView(int numberSpaces) {
		return new TextView(StringServices.repeadString(HTMLConstants.NBSP, numberSpaces));
	}


}

