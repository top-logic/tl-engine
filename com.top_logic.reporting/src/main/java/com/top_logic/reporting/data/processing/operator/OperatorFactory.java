/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.data.processing.operator;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.top_logic.basic.Configuration;
import com.top_logic.basic.Logger;
import com.top_logic.basic.Reloadable;


/** 
 * Generic manager for the supported operators in the system.
 * 
 * This implementation look up the known operators in the configuration
 * file. Every operator named there has to provide a public constructor
 * with one parameter of type "java.lang.String". All these instances will
 * be inspected for their supported operation types (the classes they can
 * handle). The user can get the list of supported operators for his data
 * by the method {@link #getOperators(java.lang.Class)}.
 * 
 * TODO This class is not thread safe.
 *
 * @author    <a href="mailto:mga@top-logic.com">mga</a>
 */
public class OperatorFactory implements Reloadable {

    /** Reference to the only instance of this class. */
    private static OperatorFactory singleton;

    /** Map value classes to theire Operators */
    private Map/*<Class<Value>, Operator[]>*/ opersPerClass;

    /** The list of all registered operators. */
    private Operator[] operators;

    /**
     * Protected constructor for singleton pattern in the OperatorFactory.
     */
    protected OperatorFactory() {
        super();

    }

    /**
     * Forces the implementing class to reload the configuration.
     *
     * @return    true, if reloading succeeds.
     */
    @Override
	public boolean reload () {
        this.opersPerClass       = null;
        this.operators = null;

        return (true);
    }

    /**
     * Returns a user understandable name of the implementing class.
     *
     * If the returned value is empty, the value will not be displayed in the
     * user interface.
     *
     * @return    The name of the class.
     */
    @Override
	public String getName () {
        return ("Reporting - Operators");
    }

    /**
     * Returns the description of the functionality of the implementing class.
     *
     * @return    The description of the function of this class.
     */
    @Override
	public String getDescription () {
        return ("The known operators for the reporting engine. These can " +
                "be used for performing operations on variuos data in the " +
                "system.");
    }

    /**
     * Returns true, if the instance uses the XMLproperties.
     *
     * This is important for the Reloadable function to clarify, if the 
     * XMLProperties have to be reloaded also.
     *
     * @return    true we use  XMLProperties.
     */
    @Override
	public boolean usesXMLProperties () {
        return (true);
    }

    /**
     * Return the operators known for the given type of object (the class).
     * 
     * @param    aClass    The class to get the operators for.
     * @return   The array of known operators, this cannot be <code>null</code>,
     *           if there is no operator, the returning value will be an
     *           empty array.
     */
    public Operator[] getOperators(Class aClass) {
        Map        theMap    = this.getOpersPerClass();
        Operator[] theResult = (Operator[]) theMap.get(aClass);
        
        if (theResult == null) {
            theResult = this.createOperators(aClass);
            theMap.put(aClass, theResult);
        }
        
        return theResult;
    }

    /**
     * Create the array of operators for aType matching {@link Operator#getSupportedTypes()}
     * 
     * @param    aType    The class to get the operators for.
     * @return   The array of known operators, this cannot be <code>null</code>,
     *           if there is no operator, the returning value will be an
     *           empty array.
     */
    protected Operator[] createOperators(Class aType) {

        Operator[] theOps    = this.getOperators();
        List       theList   = new ArrayList(theOps.length);

        for (int thePos = 0; thePos < theOps.length; thePos++) {
            Class[] supportedTypes = theOps[thePos].getSupportedTypes();

            for (int theIdx = 0; theIdx < supportedTypes.length; theIdx++) {
				if (supportedTypes[theIdx].isAssignableFrom(aType)) {
					theList.add(theOps[thePos]);
					break;
				}
            }
        }

        Operator[] theResult = new Operator[theList.size()];
        theResult = (Operator[]) theList.toArray(theResult);

        return (theResult);
    }

    /**
     * Retrieve all operators defined in the configuration.
     * 
     * This method loads all operators defined in the configuration.
     * Every class listed there has to provide a public constructor 
     * with "java.jang.String", otherwise the instanciation fails.
     * 
     * @return    The found and instantiated operators.
     */
    protected Operator[] getOperators() {
        if (this.operators == null) {
            Operator[] theResult = null;
    
            if (theResult == null) {
                String      theKey;
                String      theValue;
                Object[]    theParam;
                Class[]     strParam = new Class[] {java.lang.String.class};
                Properties  theProps = Configuration.getConfiguration(OperatorFactory.class)
                                                    .getProperties();
                Set         theSet   = theProps.keySet();
                // int      theSize  = theSet.size();
                int         thePos   = 0;
                Iterator    theIt    = theSet.iterator();
                List        theList  = new ArrayList(theProps.size());
    
                while (theIt.hasNext()) {
                    theKey   = (String) theIt.next();
                    theValue = theProps.getProperty(theKey);
                    theParam = new String[] {theKey};
    
                    try {
                        Constructor cTor        = Class.forName(theValue).getConstructor(strParam);
                        Operator   theOperator  = (Operator) cTor.newInstance(theParam);

                        theList.add(theOperator);
                        thePos++;
                    }
                    catch (Exception ex) {
                        Logger.warn("Unable to create operator " + theKey + 
                                    " (" + theValue + ")",ex, this);
                    }
                }

                theResult = new Operator[thePos];

                if (thePos > 0) {
                    theResult = (Operator[]) theList.toArray(theResult);
                }
            }

            this.operators = theResult;
        }

        return (this.operators);
    }

    /**
     * Return the map holding all known operators for the different types
     * of descriptions.
     * 
     * @return    The requested map.
     */
    protected Map/*<Class, Operator[]>*/ getOpersPerClass() {
        if (this.opersPerClass == null) {
            this.opersPerClass = new HashMap();
        }

        return (this.opersPerClass);
    }

    /**
     * Returns the only instance of this class.
     * 
     * @return    The only instance of this class.
     */    
    public static OperatorFactory getInstance() {
        if (singleton == null) {
            singleton = new OperatorFactory();
        }

        return (singleton);
    }
}
