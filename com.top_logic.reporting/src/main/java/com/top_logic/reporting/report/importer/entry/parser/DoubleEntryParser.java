/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.importer.entry.parser;

/**
 * The DoubleEntryParser parses tags with the tag name 'double'.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
@Deprecated
public class DoubleEntryParser extends AbstractEntryParser {

    /** The single instance of this class. */
    public static final DoubleEntryParser INSTANCE = new DoubleEntryParser();

    /**
     * Creates a {@link DoubleEntryParser}. 
     * Please, use the single instance of this class ({@link #INSTANCE}).
     */
    private DoubleEntryParser() {
        // Do nothing.
    }
    
    @Override
	public String toString() {
        return getClass() + "[tag name=double]";
    }

    @Override
	protected Object getValue(String aValueAsString) {
		return Double.valueOf(aValueAsString.trim());
    }

}

