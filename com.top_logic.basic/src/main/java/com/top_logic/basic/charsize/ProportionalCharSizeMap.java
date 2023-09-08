/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.charsize;

/**
 * The ProportionalCharSizeMap is a size map for proportional font types where all chars
 * have the same size.
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public class ProportionalCharSizeMap implements CharSizeMap {

    /** The single instance of this class. */
    public static final ProportionalCharSizeMap INSTANCE = new ProportionalCharSizeMap();

    @Override
	public double getSize(char character) {
        return 1.0;
    }

    @Override
	public double getSize(String string) {
        return string == null ? 0 : string.length();
    }

}
