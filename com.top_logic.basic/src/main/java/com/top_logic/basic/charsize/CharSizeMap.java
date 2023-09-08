/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.charsize;

/**
 * This mapping computes the size (width) of a character in relation to a reference character.
 * This can be used to compute the size of a string for fonts with different character sizes.
 *
 * E.g. reference character = 'a';
 *
 *      getSize('a') = 1.0
 *      getSize('i') = 0.44
 *      getSize('W') = 1.2
 *
 *      getSize('aaa') = 3.0
 *      getSize('iii') = 1.32
 *      getSize('WWW') = 3.6
 *
 *
 * @author <a href=mailto:tdi@top-logic.com>tdi</a>
 */
public interface CharSizeMap {

    /** The reference character, which will have always a size of 1.0. */
    public static final char REFERENCE_CHARACTER = 'a';

    /**
     * This method returns the character size in relation to the {@link #REFERENCE_CHARACTER}.
     *
     * E.g. getSize('i') = 0.44 * REFERENCE_CHARACTER; getSize('W') = 1.2 * REFERENCE_CHARACTER
     *
     * @param character
     *        the character to get the size for
     * @return the character size in relation to the reference character
     */
    public double getSize(char character);

    /**
     * This method returns the size of the string in relation to the {@link #REFERENCE_CHARACTER}.
     *
     * E.g. getSize('aaa') = 3.0 * REFERENCE_CHARACTER; getSize('iii') = 1.32 * REFERENCE_CHARACTER
     *
     * @param string
     *        the string to get the size for
     * @return the string size in relation to the reference character
     */
    public double getSize(String string);

}
