/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.importer.entry.parser;

/**
 * The StringEntryParser parses tags with the tag name 'string'.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
@Deprecated
public class StringEntryParser extends AbstractEntryParser {

    /** The single instance of this class. */
    public static final StringEntryParser INSTANCE = new StringEntryParser();

    /**
     * Creates a {@link StringEntryParser}. 
     * Please, use the single instance of this class ({@link #INSTANCE}).
     */
    private StringEntryParser() {
        // Do nothing.
    }
    
    @Override
	public String toString() {
        return getClass() + "[tag name=string]";
    }

    @Override
	protected Object getValue(String aValueAsString) {
        return aValueAsString;
    }

}

