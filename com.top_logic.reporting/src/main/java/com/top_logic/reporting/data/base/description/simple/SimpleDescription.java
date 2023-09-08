/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.data.base.description.simple;

import com.top_logic.basic.StringServices;
import com.top_logic.reporting.data.base.description.Description;
import com.top_logic.reporting.data.processing.operator.Operator;
import com.top_logic.reporting.data.processing.operator.OperatorFactory;
import com.top_logic.reporting.data.processing.transformator.TableTransformator;

/** 
 * This class is not worth documenting.
 *
 * @author    <a href="mailto:mga@top-logic.com">mga</a>
 */
public class SimpleDescription implements Description {

    /** What is this ? */
    private String name;

    /** What is this ? */
    private Class type;
    
    /** What is this ? */
    private Operator[] operators;

    /** What is this ? */
    private String[] entries;

    /**
     * Constructor for SimpleDescription.
     * 
     * @param    aName      The name of the description.
     * @param    aClass     The type of the values defined by this descriptor.
     */
    public SimpleDescription(String aName, Class aClass) {
        this(aName, aClass, null, null);
    }

    /**
     * Constructor for SimpleDescription.
     * 
     * @param    aName      The name of the description.
     * @param    aClass     The type of the values defined by this descriptor.
     * @param    someOps    The kind of operators for this descriptor. If this
     *                      value is <code>null</code>, the method will ask
     *                      the {@link com.top_logic.reporting.data.processing.operator.OperatorFactory}
     *                      for support.
     * @param    someDesc   The descriptions for the different values of the
     *                      entries.
     */
    public SimpleDescription(String aName, Class aClass, 
                             Operator[] someOps, String[] someDesc) {
        super();

        if (StringServices.isEmpty(aName)) {
            throw new IllegalArgumentException("Parameter name is invalid " +
                                               "(was: " + aName + ')');
        }

        if (aClass == null) {
            throw new IllegalArgumentException("Parameter class is null");
        }

        if (someOps == null) {
            someOps = OperatorFactory.getInstance().getOperators(aClass);
        }

        if ((someDesc == null) || (someDesc.length == 0)) {
            someDesc = new String[] {aName};
        }

        this.name      = aName;
        this.type      = aClass;
        this.operators = someOps;
        this.entries   = someDesc;
    }

    /**
     * @see com.top_logic.reporting.data.base.description.Description#getType()
     */
    @Override
	public Class getType() {
        return (this.type);
    }

    /**
     * What does this function do ?
     */
    public Operator[] getOperators() {
        return (this.operators);
    }

    /**
     * What does this function do ?
     */
    public Operator getOperator(String aName) {
        // String   theKey;
        int      thePos    = 0;
        Operator theResult = null;

        if (!StringServices.isEmpty(aName)) {
            while ((theResult == null) && (thePos < this.operators.length)) {
                if (aName.equals(this.operators[thePos].getName())) {
                    theResult = this.operators[thePos];
                }
                else {
                    thePos++;
                }
            }
        }

        return (theResult);
    }

    /**
     * What is this function needed for ?
     */
	public TableTransformator[] getTransformators(){
		return null;
	}

    /**
     * What is this function needed for ?
     */
	public TableTransformator getTransformator(String aName){
		return null;
	}


    /**
     * @see com.top_logic.reporting.data.base.description.Description#getName()
     */
    @Override
	public String getName() {
        return (this.name);
    }

    /**
     * @see com.top_logic.reporting.data.base.description.Description#getDisplayName()
     */
    @Override
	public String getDisplayName() {
        return (this.name);
    }

    @Override
	public String toString() {
        return (this.getClass().getName() + " [" +
                    "name: " + this.name +
                    ']');
    }
	/**
	 *@see com.top_logic.reporting.data.base.description.Description#getNumberOfValueEntries()
	 */
	@Override
	public int getNumberOfValueEntries() {
		return 1;
	}

    /**
     * Returns the entries.
     */
    @Override
	public String[] getEntries() {
        return (this.entries);
    }
}
