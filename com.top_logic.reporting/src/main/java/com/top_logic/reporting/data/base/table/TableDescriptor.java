/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.data.base.table;

import java.util.List;

import com.top_logic.reporting.data.base.description.Description;
import com.top_logic.reporting.data.processing.operator.Operator;
import com.top_logic.reporting.data.processing.transformator.TableTransformator;

/**
 * The {@link TableDescriptor} describes - you guess it - a {@link ReportTable}.
 * Describing means the tables structure and the tables data are described.
 * We already have the Description for Describing data, so the {@link TableDescriptor} extends this
 * interface for describing {@link ReportTable}s
 * 
 * @author    <a href="mailto:tri@top-logic.com">tri</a>
 */
public interface TableDescriptor extends Description {

	/**
     * Return all operators, which can be used with this description.
     * 
     * The kind of operators depend on the type of the data as described
     * in {@link #getType()}.
     * 
	 * @return    All Operators which can be used on the described data.
	 * 			   or null if no Ops are available
	 */
	public Operator[] getOperators();
	

	/**
     * Return all {@link TableTransformator}s, which can be used with this description.
	 * @return    All {@link TableTransformator}s which can be used on the described data or 
	 * null if no Transformators are available
	 */
	public TableTransformator[] getTransformators();

	/**
	 * the header row of the Table (the Veryfirst row, usually containing Strings to act as
	 * columnlabels) Note: the header row needs to have the very same number of columns as the
	 * actual table data
	 */
	public List getHeaderRow();
	
	/**
	 * the header column of the Table (the Veryfirst row, usually containing Strings to act as
	 * rowlabels) Note: the header column needs to have the very same number of rows as the actual
	 * table data
	 */
	public List getHeaderColumn();
	
}
