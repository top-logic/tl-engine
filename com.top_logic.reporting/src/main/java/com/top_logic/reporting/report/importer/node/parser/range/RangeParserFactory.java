/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.importer.node.parser.range;

import java.util.HashMap;

import com.top_logic.reporting.report.importer.node.NodeParser;
import com.top_logic.reporting.report.importer.node.parser.category.ClassificationFunctionParser;
import com.top_logic.reporting.report.importer.node.parser.category.DateFunctionParser;
import com.top_logic.reporting.report.importer.node.parser.category.NumberFunctionParser;
import com.top_logic.reporting.report.importer.node.parser.category.StringFunctionParser;

/**
 * This factory creates {@link NodeParser}s.
 * 
 * @author <a href="mailto:fsc@top-logic.com">Friedemann Schneider</a>
 */
@Deprecated
public class RangeParserFactory {

    
    private static RangeParserFactory instance;
    
    private HashMap parsers;
    
    private RangeParserFactory() {
        this.parsers = new HashMap(3);
        this.parsers.put(StringFunctionParser.TYPE, StringRangeParser.getInstance());
        this.parsers.put(DateFunctionParser.TYPE,   DefaultRangeParser.getInstance());
        this.parsers.put(NumberFunctionParser.TYPE, DefaultRangeParser.getInstance());
        this.parsers.put(ClassificationFunctionParser.TYPE, DefaultRangeParser.getInstance());
    }
    
    public static synchronized RangeParserFactory getInstance() {
        if (instance == null) {
            instance = new RangeParserFactory();
        }
        return instance;
    }
    
    public NodeParser getParser(String aType) {
        return (NodeParser) this.parsers.get(aType);
    }
}
