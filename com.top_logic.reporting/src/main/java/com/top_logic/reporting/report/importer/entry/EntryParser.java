/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.importer.entry;

import org.w3c.dom.Node;

import com.top_logic.reporting.report.importer.entry.parser.AbstractEntryParser;

/**
 * The EntryParser parses only {@link Entry}s from {@link Node}s.
 * See {@link #parse(Node)}.
 * 
 * The EntryParser have to be registered to the {@link EntryParserManager}
 * for a tag name (e.g. 'string' or 'boolean').
 * 
 * See {@link AbstractEntryParser}.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
@Deprecated
public interface EntryParser {

    /** The constant for the attribute 'key' of an entry. */
    public static final String KEY_ATTR   = "key";
    /** The constant for the attribute 'value' of an entry. */
    public static final String VALUE_ATTR = "value";

    /**
     * Returns an {@link Entry} which is filled with the values from the entry
     * node. An entry node is a node which has only the two attributes: 'key'
     * and 'value'.
     * 
     * * E.g. shows an entry nodes.
     * ... 
     *    <tag-name  key="class" value="java.util.Date" />
     * ...
     */
    public Entry parse(Node aNode);
    
}

