/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.data.processing;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.reporting.data.base.table.ReportTable;
import com.top_logic.reporting.data.base.table.TableDescriptor;
import com.top_logic.reporting.data.base.table.common.DefaultTableDescriptor;
import com.top_logic.reporting.data.base.value.Value;
import com.top_logic.reporting.data.processing.operator.Operator;
import com.top_logic.reporting.data.processing.transformator.TableTransformator;

/**
 * Once you know which data (by description) of which data handler you want 
 * to process with which operator, you can use this class to do this.
 * 
 * It automatically performs all neccessary actions and provides you with 
 * the result table of the operation. The use of an operator is optional.
 * This class makes retrieval and handling of value holders transparent.
 * 
 * @author    <a href="mailto:tri@top-logic.com">Thomas Richter</a>
 */
public class DataProcessor {

	private static DataProcessor singleton;

	public static final int ROWS		= 0;
	public static final int COLUMNS	= 1;

    /** private for singleton pattern */
	private DataProcessor() { /* private for singleton pattern */ }
	
	public synchronized static DataProcessor getInstance(){
		if(singleton == null){
			singleton = new DataProcessor();
		}
		return singleton;
	}

	public ReportTable applyTransformator(ReportTable aTable, TableTransformator aTransformator){
		return aTransformator.transform(aTable);	 
	}

	public ReportTable applyOperator(ReportTable aTable, Operator anOperator, int processingRange){
		List theValueTable = aTable.getTableData();
		List theResultData = new ArrayList();
		List theHeaderRow		= aTable.getTableDescriptor().getHeaderRow();
		List theHeaderColumn    = aTable.getTableDescriptor().getHeaderColumn();

		String operatorDisplayName = anOperator.getDisplayName()+" "+aTable.getTableDescriptor().getDisplayName();		

		if(processingRange == ROWS){
			theHeaderRow = new ArrayList();
			theHeaderRow.add(operatorDisplayName);
			for (int rowidx=0;rowidx < theValueTable.size();rowidx++){
				List aRow 		= (List)theValueTable.get(rowidx);
				Value[] valueArray = new Value[aRow.size()];
				aRow.toArray(valueArray);
				Value aValue 	= anOperator.process(valueArray);
				List aResultRow = new ArrayList();
				aResultRow.add(aValue);
				theResultData.add(aResultRow);
			}
		}else
			if (processingRange == COLUMNS){
			theHeaderColumn		= new ArrayList();
			theHeaderColumn.add(operatorDisplayName);
			List aRow 			= (List)theValueTable.get(0);
			int numbercolumns	= aRow.size();
			List theResultRow	= new ArrayList();
			for (int colidx =0; colidx < numbercolumns; colidx++){
				List aColumn 	= new ArrayList();
				for (int rowidx =0; rowidx < theValueTable.size();rowidx++){
					aRow 			= (List) theValueTable.get(rowidx);
					aColumn.add(aRow.get(colidx));
				}				
				Value[] valueArray = new Value[aColumn.size()];	
				aColumn.toArray(valueArray);			
				Value aValue = anOperator.process(valueArray);
				theResultRow.add(aValue);
			}
			theResultData.add(theResultRow);
		}

	String newDisplayName = aTable.getTableDescriptor().getDisplayName()+"("+anOperator.getDisplayName()+")";
	TableDescriptor newDescriptor = new DefaultTableDescriptor(theHeaderRow,theHeaderColumn,
															   aTable.getTableDescriptor().getType(),
															   aTable.getTableDescriptor().getNumberOfValueEntries(),
															   newDisplayName);

	ReportTable resultTable = new ReportTable(newDescriptor, theResultData);
	return resultTable;	
	}



}
