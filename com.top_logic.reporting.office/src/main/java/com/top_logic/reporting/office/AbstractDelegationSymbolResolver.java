/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.office;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;

import com.top_logic.basic.Logger;


/**
 * This is an abstract class to delegate the resolving to certain methods.
 * The concrete methods are implemented in the specific subclasses.
 * The resolving of the static symbol is done by interpreting a Properties of the 
 * class.
 * 
 * @author    <a href=mailto:jco@top-logic.com>jco</a>
 */
public abstract class AbstractDelegationSymbolResolver implements StaticSymbolResolver {

    protected Properties delegationMap;
    protected static Class[] METHOD_PARAM = {ExpansionContext.class};
 
    public AbstractDelegationSymbolResolver() {
        initDelegationMap();
    }
    
    /**
     * fill the delegation map of the resolver with key | value pairs, where the
     * key is a symbol name and the value is a method name.
     */
    protected abstract void initDelegationMap ();
    
    /**
     * here we delegate to a certain method in the same class
     * as defined in the delegationMap.
     * The method must habe the signatur methodName(ExpansionContext):Object
     *  
     * @see com.top_logic.reporting.office.StaticSymbolResolver#resolveSymbol(com.top_logic.reporting.office.ExpansionContext, com.top_logic.reporting.office.ExpansionObject)
     */
    @Override
	public Object resolveSymbol(ExpansionContext aContext, ExpansionObject aSymbol) {
        
        if (aSymbol != null) {
            String symbolContent = aSymbol.getSymbolContent();
            String methodName = getMethodNameForSymbol(symbolContent);
           
            if (methodName != null) {

                try {
                    Method theMethod = this.getClass().getMethod(methodName,
                                                                 METHOD_PARAM);
                    
                    return theMethod.invoke(this, new Object[] { aContext });
                }
                catch (NoSuchMethodException exp) {
                    Logger.error("call of unknown method: " + methodName, exp,
                                 this);
                }
                catch (InvocationTargetException exp) {
                    Logger.error("Something in the invoked method: "
                                 + methodName + " went wrong", exp, this);
                }
                catch (Exception exp) {
                    //IllegalArgumentException can not occur here!
                    Logger.error("something went wrong", exp, this);
                }
            }          
        }
        return null;
    }
    
    /**
     * @param aStaticSymbol the given symbol name
     * @return a method name as configured in the delegationMap or <code>null</code> if the symbol does not exist.
     */
    protected String getMethodNameForSymbol (String aStaticSymbol) {
        if (delegationMap != null) {
            return delegationMap.getProperty(aStaticSymbol);
        } else {
            return null;
        }
    }

}
