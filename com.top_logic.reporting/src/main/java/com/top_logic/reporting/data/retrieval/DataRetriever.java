/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.data.retrieval;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.reporting.data.base.description.Description;
import com.top_logic.reporting.data.base.table.ReportTable;
import com.top_logic.reporting.data.base.table.TableDescriptor;
import com.top_logic.reporting.data.base.table.common.DefaultTableDescriptor;
import com.top_logic.reporting.data.base.value.Value;
import com.top_logic.reporting.data.base.value.common.NullValue;

/**
 * @author    <a href="mailto:tri@top-logic.com">tri</a>
 */
public class DataRetriever {

	private static DataRetriever singleton;

    /** private for singleton pattern */
	private DataRetriever(){ /* private for singleton pattern */}

	
	public synchronized static DataRetriever getInstance(){
		if(singleton == null){
			singleton = new DataRetriever();
		}
		return singleton;
	}

	public ReportTable getValueTable(DataHandler aDataHandler, Description aDescription, int anEvalDepth){
	
		DataHandler[] handlers = getDataHandlersForDepth(aDataHandler, anEvalDepth);

		List aTable = new ArrayList();
		List theHeaderRow = new ArrayList();
		List theHeaderColumn = new ArrayList();
		
		for (int i = 0; i < handlers.length; i++) {
			List aRow = new ArrayList();
			DataHandler theCurrent = handlers[i];
			ValueHolder theValueHolder = theCurrent.getValueHolder(aDescription);
			if(theValueHolder!= null){
				Range[] theRange = this.getRangesForDescription(aDataHandler, aDescription, anEvalDepth);
				Arrays.sort (theRange);
				for (int columns=0; columns < theRange.length;columns++){
					Value aValue = theValueHolder.getValue(theRange[columns]);
					if (aValue!=null){
						aRow.add(aValue);
					}else{
						aRow.add(NullValue.INSTANCE);
					}
					if(columns >= theHeaderRow.size())
						theHeaderRow.add(theRange[columns].getDisplayName());
				}
				theHeaderColumn.add(theCurrent.getDisplayName());
				aTable.add(aRow);
			}
		}

		TableDescriptor theDescriptor = new DefaultTableDescriptor(theHeaderRow, theHeaderColumn, aDescription);
		return new ReportTable(theDescriptor,aTable);
	}


	private DataHandler[] getDataHandlersForDepth(DataHandler theRootHandler, int aDepth){
		List theHandlers = new ArrayList();
		theHandlers.add(theRootHandler);
		
		
		DataHandler[] anArray = new DataHandler[1];
		anArray[0] = theRootHandler;
		
		for (int i=1;i<=aDepth;i++){
			anArray = getChildsFromHandlers(anArray);
			theHandlers.addAll(Arrays.asList(anArray));
			
							
		}		
		
		DataHandler[] theResult = new DataHandler[theHandlers.size()];
		theHandlers.toArray(theResult);
		return theResult;
	}


	private DataHandler[] getChildsFromHandlers(DataHandler[] theHandlers){
		List theResultList = new ArrayList();
		
		for (int i=0;i<theHandlers.length;i++){
			DataHandler[] currentChilds = theHandlers[i].getChildren();
			if(currentChilds!=null){
				theResultList.addAll(Arrays.asList(currentChilds));
			}
		}
		
		DataHandler[] theResult = new DataHandler[theResultList.size()];
		theResultList.toArray(theResult);
		return theResult;
	}


	private Range[] getRangesForDescription(DataHandler rootHandler, Description aDesc, int evalDepth){
		Set theRanges = new HashSet();
		DataHandler[] theHandlers = getDataHandlersForDepth(rootHandler,evalDepth);
		for (int i=0;i<theHandlers.length;i++){
			ValueHolder aHolder = theHandlers[i].getValueHolder(aDesc);
			if (aHolder!=null){
				Range[] tmp = aHolder.getRange();
				if(tmp!=null){
					theRanges.addAll(Arrays.asList(tmp));
				}
			}
		}
		Range[] theResult = new Range[theRanges.size()];
		theRanges.toArray(theResult);
		return theResult;
	}


}
