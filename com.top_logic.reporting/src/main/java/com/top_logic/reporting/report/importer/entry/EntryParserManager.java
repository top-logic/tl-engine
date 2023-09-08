/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.importer.entry;

import java.util.HashMap;

import com.top_logic.basic.StringServices;
import com.top_logic.reporting.report.exception.NotAllowedException;
import com.top_logic.reporting.report.exception.UnsupportedException;
import com.top_logic.reporting.report.importer.entry.parser.BooleanEntryParser;
import com.top_logic.reporting.report.importer.entry.parser.DateEntryParser;
import com.top_logic.reporting.report.importer.entry.parser.DoubleEntryParser;
import com.top_logic.reporting.report.importer.entry.parser.MetaElementEntryParser;
import com.top_logic.reporting.report.importer.entry.parser.StringEntryParser;

/**
 * The EntryParserManager manages the {@link EntryParser}s. 
 * Parser can be (un-)registered.
 * The manager is implemented with the singleton pattern.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
@Deprecated
public class EntryParserManager {

    /** See {@link #getInstance()}. */
    private static EntryParserManager INSTANCE;
    
    /** 
     * This map contains the registered {@link EntryParser}s.
     * Keys:   tag names{@link String}.
     * Values: {@link EntryParser}s.  
     */
    private HashMap parsers;

    /** 
     * Creates a {@link EntryParserManager}.
     */
    private EntryParserManager() {
        this.parsers = new HashMap();
        
        registerParser("string",       StringEntryParser.INSTANCE);
        registerParser("date",         DateEntryParser.INSTANCE);
        registerParser("boolean",      BooleanEntryParser.INSTANCE);
        registerParser("double",       DoubleEntryParser.INSTANCE);
        registerParser("meta-element", MetaElementEntryParser.INSTANCE);
    }

    /** 
     * Returns the single instance of this class never <code>null</code>.
     */
    public static synchronized EntryParserManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new EntryParserManager();
        }
        
        return INSTANCE;
    }
    
    /**
     * Registers the given parser for the tag name.
     * 
     * @param aTagName
     *        See {@link #getEntryParser(String)}.
     * @param aParser
     *        The {@link EntryParser} must not be <code>null</code>.
     */
    public void registerParser(String aTagName, EntryParser aParser) {
        if (StringServices.isEmpty(aTagName)) {
            throw new NotAllowedException(this.getClass(), "It is not allowed that the tag name is null or empty.");
        }
        if (aParser == null) {
            throw new NotAllowedException(this.getClass(), "It is not allowed that the EntryParser is null.");
        }
        
        this.parsers.put(aTagName, aParser);
    }
    
    /**
     * Unregisters the {@link EntryParser} for the given tag name. This method
     * returns <code>true</code> if the parser could be removed, 
     * <code>false</code> otherwise.
     * 
     * @param aTagName
     *        See {@link #getEntryParser(String)}.
     */
    public boolean unregisterParser(String aTagName) {
        return this.parsers.remove(aTagName) != null ? true : false;
    }
    
    /**
     * Returns the {@link EntryParser} for the given tag name or throws a
     * {@link UnsupportedException}.
     * 
     * @param aTagName
     *        A correct tag name. Must not be <code>null</code> or empty.
     */
    public EntryParser getEntryParser(String aTagName) {
        EntryParser theParser = (EntryParser) this.parsers.get(aTagName);
        
        if (theParser == null) {
            throw new UnsupportedException(this.getClass(), "For the tag name: '" + aTagName + "' is no EntryParser available. Only this parsers are registered: " + StringServices.toString(this.parsers.values(), ",") + '.');
        }
        
        return theParser;
    }
    
}

