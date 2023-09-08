/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.data.retrieval;

import com.top_logic.reporting.data.base.description.Description;

/**
 * The DataHandler is basically some kind of wrapper around an object.
 * 
 * It provides standarized access to the objects attributes and data.
 * This is done by providing a Description for each datatype to be accessed
 * by this interface. The Description provides metainformation about the data
 * (which can be used in e.g.GUIs.)
 * The actual values are provided as ValueHolder for the given Description.
 * 
 * @author    <a href="mailto:tri@top-logic.com">tri</a>
 */
public interface DataHandler {

	/**
     * Return all descriptions for this instance.
     * 
     * A description defines a possible set of values provided by the
     * handler. That definitions describe different operations on the
     * data so the user is able to select the operation he wants to
     * execute. If there are no descriptions for this handler, this
     * method must return an empty array (<code>null</code> is not
     * allowed).
     * 
	 * @return    All the datadescriptions of this handler (never 
     *            <code>null</code>, see above).
	 */
	public Description[] getDescriptions();
	
	/**
     * Returns the instance holding the real values for the requested
     * kind of information.
     * 
     * The given descriptor defines the values, the user wants to get 
     * from this data model. If there are no values, this instance has
     * to return <code>null</code>. If there is a returning value, that
     * value must provide data (the {@link ValueHolder} is not allowed
     * to be empty).
     * 
	 * @param    aDescription    The Description for which the values 
     *                           should be given, must not be <code>null</code>.
	 * @return   The ValueHolder for the given Description or 
     *           <code>null</code>, if there is no ValueHolder for 
     *           the description.
	 */
	public ValueHolder getValueHolder(Description aDescription);

	/**
     * Return the maximum depth this instance supports for evaluating values.
     * 
     * Depending on the returned depth, the system can aggregate values from
     * the children, if the user is willing to do this. If this method returns
     * a value larger than zero, the {@link #getChildren()} method has to
     * return at least one object, if the value is zero, there are no children!
     * 
	 * @return    The maximum evaluation depth.
     * @see       #getChildren()
	 */
	public int getMaximumDepth();
	
	/**
     * Return the direct children of this handler, if there are some.
     * 
     * This method is been used to represent hierachical data structures.
     * If this method returns at least one object, the maximum depth 
     * returned has to be larger than zero, if it returns <code>null</code>,
     * the maximum depth must be zero!
     * 
	 * @return    The child handlers of this DataHandler, if this 
     *            handler has no children, it returns <code>null</code>.
     * @see       #getMaximumDepth()
	 */
	public DataHandler[] getChildren();

	/**
     * Return the display name of this handler.
     * 
     * The user interface needs information about the handler for
     * displaying it. This method return the name for the user interface.
     * This includes, that the returned value should be different for
     * different instances of this class, otherwise the objects are not
     * unique for the user.
     * 
	 * @return    The display name of this datahandler, typically this 
     *            is the name of the wrapped object, must not be 
     *            <code>null</code>.
	 */	
	public String getDisplayName();
}
