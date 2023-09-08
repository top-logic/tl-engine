/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.data.processing.transformator;

import com.top_logic.reporting.data.base.table.ReportTable;

/**
 * @author    <a href="mailto:tri@top-logic.com">tri</a>
 */
public interface TableTransformator {

	/**
	 * performs the implemented action on the table
	 * Any of the tables cells might be modifed by a transformator.
	 * Even the number of rows and columns may change
	 * NOTE: this method has to take care of NullValues which may appear within the data
	 * 
	 * @param aTable the Table to be transformed. If null is given a NullPointerException is thrown
	 * @return the transformed Table
	 * @exception IllegalArgumentException if the given resulttable contains values of an unsupported type
	 * @exception NullPointerException if the given table was null
	 */
	public ReportTable transform(ReportTable aTable) throws IllegalArgumentException;

	/** a string representation of this transformator to be used in GUIs*/
	public String getDisplayName();

	/** a string which can be used to identify this type of transformator inernally*/
	public String getName();

	/** the Type of Values this operator can work with */
	public Class getType();
	
	/**
	 * the minimum Dimension (numer of entries) a Value has to have to be compatible with this Transformator
	 * note: in general a transformator can work with values of a greater dimension, so this is the minimum requiered size 
	 */
	public int getRequiredValueSize();
	
	/**
	 * the number of entries the values in the transformed table will have
 	 */
	public int getTransformedValueSize();

	/**@return
	 * an Array of TableTransformators which could be applied to this Transformators result
	 * according to their requiered Dimension and type
	 */
	public TableTransformator[] getCompatibleTransformators();
}
