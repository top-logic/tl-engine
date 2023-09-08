/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.charsize;

import java.util.Map;

/**
 * Generic CharSizeMap for fixed (manual) size declaration.
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public class MappingCharSizeMap implements CharSizeMap {

    /** The map containing the sizes of each character. */
    private final Map<Character, Double> charSizes;

    /** The default character size for characters not found in the map. */
    private final double defaultCharSize;


    /**
     * Creates a new instance of this class for the given font.
     */
    public MappingCharSizeMap(Map<Character, Double> charSizes) {
        this(charSizes, 1.0);
    }

    /**
     * Creates a new instance of this class for the given font.
     */
    public MappingCharSizeMap(Map<Character, Double> charSizes, double defaultCharSize) {
        this.charSizes = charSizes;
        this.defaultCharSize = defaultCharSize;
    }



    @Override
	public double getSize(char character) {
        Double size = charSizes.get(Character.valueOf(character));
        return size == null ? defaultCharSize : size.doubleValue();
    }

    @Override
	public double getSize(String string) {
        double result = 0.0;
        if (string != null)
        for (int i = 0, length = string.length(); i < length; i++) {
            result += getSize(string.charAt(i));
        }
        return result;
    }

}
