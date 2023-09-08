/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.reporting.office;

import java.util.Properties;

import com.top_logic.reporting.office.AbstractDelegationSymbolResolver;
import com.top_logic.reporting.office.ExpansionContext;


/**
 * A WRONG implementation of a Symbol Resolver to test the more exotic exeptions
 * in the AbstractDelegationSymbolResolver.
 * Do NOT use this as a template for writing your own Resolver!
 * 
 * @author    <a href=mailto:jco@top-logic.com>jco</a>
 */
public class FaultyDelegationSymbolResolver extends
        AbstractDelegationSymbolResolver {

    public static final int NO_SYMBOLS = 1;
    public static final int WRONG_METHOD_NAME = 2;
    public static final int INVOCATION_TARGET = 3;
    public static final int ILLEGAL_ARGUMENT = 4;
    
    private int mode;
    public FaultyDelegationSymbolResolver(int faultyMode) {
        super();
        mode = faultyMode;
        initDelegationMap();
    }

    /** TODO jco Overriden to/because
     * 
     * 
     * @see com.top_logic.reporting.office.AbstractDelegationSymbolResolver#initDelegationMap()
     */
    @Override
	protected void initDelegationMap() {
        switch (mode) {
            case NO_SYMBOLS:
                delegationMap = null;
                break;

            case WRONG_METHOD_NAME:
                delegationMap = new Properties();
                delegationMap.setProperty("EXAMPLE_KEY","noSuchMethod");                
                break;

            case INVOCATION_TARGET:
                delegationMap = new Properties();
                delegationMap.setProperty("EXAMPLE_KEY","invocationException");                
                break;

            case ILLEGAL_ARGUMENT:
                delegationMap = new Properties();
                delegationMap.setProperty("EXAMPLE_KEY","illegalArgument");                
                break;

            default:
                break;
        }
    }
    
    public Object invocationException (ExpansionContext aContext) {
        throw new NullPointerException ("we throw an exception so se what happend here");
    }

    public Object illegalArgument (String aParam) {
        return "Must not process " + aParam;
    }
}
