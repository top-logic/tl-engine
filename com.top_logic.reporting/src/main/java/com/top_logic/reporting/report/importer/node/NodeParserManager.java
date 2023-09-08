/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.importer.node;

import java.lang.reflect.Constructor;
import java.util.HashMap;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.reporting.report.exception.NotAllowedException;
import com.top_logic.reporting.report.exception.ReportingException;
import com.top_logic.reporting.report.exception.UnsupportedException;
import com.top_logic.reporting.report.importer.node.parser.I18NParser;
import com.top_logic.reporting.report.importer.node.parser.category.ClassificationFunctionParser;
import com.top_logic.reporting.report.importer.node.parser.category.DateFunctionParser;
import com.top_logic.reporting.report.importer.node.parser.category.NumberFunctionParser;
import com.top_logic.reporting.report.importer.node.parser.category.SameFunctionParser;
import com.top_logic.reporting.report.importer.node.parser.category.StringFunctionParser;
import com.top_logic.reporting.report.model.ReportConfiguration;
import com.top_logic.reporting.report.xmlutilities.ReportReader;

/**
 * The NodeParserManager manages the {@link NodeParser}s. 
 * Parser can be (un-)registered.
 * The manager is implemented with the singleton pattern.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 * @deprecated use {@link ReportReader} and {@link ReportConfiguration}
 */
@Deprecated
public class NodeParserManager {

    /** See {@link #getInstance()}. */
    private static NodeParserManager instance;
    
    /** 
     * This map contains the registered {@link NodeParser}s.
     * Keys:   tag names{@link String}.
     * Values: {@link NodeParser}s.  
     */
    private HashMap constructors;
    
    private Object[] defaultParameters = new Object[] {};

    /** 
     * Creates a {@link NodeParserManager}.
     */
    private NodeParserManager() {
        this.constructors = new HashMap();
        
        /** Category function parsers */
        registerParser(ClassificationFunctionParser.TYPE, ClassificationFunctionParser.class);
        registerParser(StringFunctionParser.TYPE, StringFunctionParser.class);
        registerParser(NumberFunctionParser.TYPE, NumberFunctionParser.class);
        registerParser(DateFunctionParser.TYPE, DateFunctionParser.class);
        registerParser(SameFunctionParser.TYPE, SameFunctionParser.class);

        /** Misc parser */
        registerParser(I18NParser.TYPE, I18NParser.class);
    }

    /** 
     * Returns the single instance of this class never <code>null</code>.
     */
    public static synchronized NodeParserManager getInstance() {
        if (instance == null) {
            instance = new NodeParserManager();
        }
        
        return instance;
    }
    
    /**
     * Registers the given parser for the tag name.
     * 
     * @param aTagName
     *        See {@link #getParser(String)}.
     * @param aParser
     *        The {@link NodeParser} must not be <code>null</code>.
     */
    public void registerParser(String aTagName, Class aParser) {
        if (StringServices.isEmpty(aTagName)) {
            throw new NotAllowedException(this.getClass(), "It is not allowed that the tag name is null or empty.");
        }
        if (aParser == null) {
            throw new NotAllowedException(this.getClass(), "It is not allowed that the NodeParser is null.");
        }
        try {
            this.constructors.put(aTagName, aParser.getConstructor(new Class[] {}));
        } catch (NoSuchMethodException nex) {
            Logger.warn ("Unable to register parser: "+aParser, NodeParserManager.class); 
            throw new ReportingException(this.getClass(), "Unable to register parser: "+aParser, nex);
        }
    }
    
    /**
     * Unregisters the {@link NodeParser} for the given tag name. This method
     * returns <code>true</code> if the parser could be removed, 
     * <code>false</code> otherwise.
     * 
     * @param aTagName
     *        See {@link #getParser(String)}.
     */
    public boolean unregisterParser(String aTagName) {
        return this.constructors.remove(aTagName) != null ? true : false;
    }
    
    /**
     * Returns the {@link NodeParser} for the given node name or throws a
     * {@link UnsupportedException}.
     * 
     * @param aNodeName
     *        A correct tag name. Must not be <code>null</code> or empty.
     */
    public NodeParser getParser(String aNodeName) {
        
        Constructor theConst = (Constructor) this.constructors.get(aNodeName); 
        try {
            NodeParser theParser = (NodeParser) theConst.newInstance(this.defaultParameters);
            return theParser;
        } catch (Exception ex) {
            throw new ReportingException(this.getClass(), "Unable to instanciate parser "+aNodeName, ex);
        }
    }
    
}

