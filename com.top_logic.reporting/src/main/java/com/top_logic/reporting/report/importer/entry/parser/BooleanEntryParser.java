/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.importer.entry.parser;

/**
 * The BooleanEntryParser parses tags with the tag name 'boolean'.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
@Deprecated
public class BooleanEntryParser extends AbstractEntryParser {

    /** The single instance of this class. */
    public static final BooleanEntryParser INSTANCE = new BooleanEntryParser();

    /**
     * Creates a {@link BooleanEntryParser}. 
     * Please, use the single instance of this class ({@link #INSTANCE}).
     */
    private BooleanEntryParser() {
        // Do nothing.
    }
    
    @Override
	public String toString() {
        return getClass() + "[tag name=boolean]";
    }

    @Override
	protected Object getValue(String aValueAsString) {
        return Boolean.valueOf(aValueAsString.trim());
    }

}

