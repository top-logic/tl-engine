/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.data.base.table;

import java.util.List;

import com.top_logic.reporting.data.processing.operator.Operator;
import com.top_logic.reporting.data.processing.operator.OperatorFactory;
import com.top_logic.reporting.data.processing.transformator.TableTransformator;
import com.top_logic.reporting.data.processing.transformator.TransformatorFactory;

/**
 * The AbtractTableDescriptor extends the AbstractDescription.
 * KHA: This is plain wrong when I look at the code ?
 * This way the always everthesame stuff
 * if done by this abstract implementation, so the specific implementation does not need to care for it
 * and redundant code is prevented.
 *
 * @author    <a href="mailto:tri@top-logic.com">tri</a>
 * 
 */
public abstract class AbstractTableDescriptor implements TableDescriptor{

    /** What is this memeber used for ? */
	List theHeaderRow;

    /** What is this memeber used for ? */
	List theHeaderColumn;

    /** What is this memeber used for ? */
	Class theType;

    /** What is this memeber used for ? */
	int theValueSize;

    /** What is this memeber used for ? */
	private Operator[] theOperators;

    /** What is this memeber used for ? */
	private TableTransformator[] theTransformators;

	protected AbstractTableDescriptor(List aHeaderRow, List aHeaderColumn, Class aType, int valueSize){
		this.theHeaderColumn = aHeaderColumn;
		this.theHeaderRow = aHeaderRow;
		this.theType = aType;
		this.theValueSize = valueSize;
		theOperators = OperatorFactory.getInstance().getOperators(this.getType());
		theTransformators = TransformatorFactory.getInstance().getTransformatorsForTypeAndSize(this.getType(),this.getNumberOfValueEntries());

	}

	/**
	 * @see com.top_logic.reporting.data.base.table.TableDescriptor#getTransformators()
	 */
	@Override
	public TableTransformator[] getTransformators() {
		return this.theTransformators;
	}


	/**
	 * What does this function do ?
	 */
	@Override
	public Operator[] getOperators() {
		return this.theOperators;
	}


	/**
	 * Returns the aHeaderColumn.
	 * @return List
	 */
	@Override
	public List getHeaderColumn() {
		return theHeaderColumn;
	}

	/**
	 * Returns the aHeaderRow.
	 * @return List
	 */
	@Override
	public List getHeaderRow() {
		return theHeaderRow;
	}

	@Override
	public Class getType(){
		return theType;
	}

	@Override
	public int getNumberOfValueEntries(){
		return theValueSize;
	}
}
