/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.data.processing.operator;

import com.top_logic.reporting.data.base.value.Value;

/**
 * An Operator provides mechanism to process data.
 * 
 * @author    <a href="mailto:tri@top-logic.com">tri</a>
 */
public interface Operator {

	/**
	 * Executes this operation on a set of given values.
     * 
     * The given data must not be <code>null</code>, nevertheless
     * some values in the array can be NullValues, which
     * results in a call for the {@link #getNeutralValue()}
     * for this operation. A neutral value can differ from operation
     * to operation (e.g. Neutral for SUM and DIFF is '0', whereas neutral for DIV
     * and MULTIPLY is '1').The values themself can also contain <code>null</code>, 
     * if they represent a set of data. But this should be handled in the
     * operating methods of the Values themself
     * 
     * The returning value must not be <code>null</code>, because
     * the reporting engine has to visualizise the result in some 
     * (generic) way
     * So if processing is not possible and it becomes neccessary that this
     * method returns null for whatever reason, return a new NullValue instead.
     * Such a value is threatened as null with the exception that it can be
     * displayed in the proper way
     * Also this method has to take care of the entrynames of the result
     * It can retrieve the names of arguments and then is responsible to set the entry
     * names of the result Value. However, those names are just used for display, 
     * so setting them is not mandatory.
     * 
	 * @param    anValueArray    The values which should be processed.
	 * @return   The result value. or a NullValue if processing was not possible
     * @see       #getNeutralValue()
     * @see 	  com.top_logic.reporting.data.base.value.common.NullValue
	 */
	public Value process(Value[] anValueArray);
	
	/**
     * The name returned by this method identifies the description internally.
     * 
     * Because this value will be used for identifying the object, this
     * method must not return <code>null</code>.
     * 
     * @return    The Name (ID) of this description (not <code>null</code>).
	 */	
	public String getName();


	/**
     * The neutral value of this Operator.
     * 
     * This is a value which does not affect the caluclation in any way. 
	 * This is the value which is involved in caluclations instead 
     * of undefined value.
     *
     * 
     * @return    The value of this Operator to be used when there is a
     *            <code>null</code> in the given values, must not be
     *            <code>null</code>.
	 */
	public Value getNeutralValue();
	
    /**
     * To be used to show this operator in GUIs.
     * 
     * @return    The display name of this operator, must not be
     *            <code>null</code>.
     */
	public String getDisplayName();

    /**
     * Return the different types, this operator can process.
     * 
     * The returned value must not be <code>null</code>, otherwise an
     * operator doesn't make sense.
     * 
     * @return    The array of supported types (classes), must not be
     *            <code>null</code>.
     */
    public Class[] getSupportedTypes();
}
