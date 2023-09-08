/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.business;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.Logger;
import com.top_logic.contact.business.CSVLineTokenizer.InvalidLineException;


/**
 * Parses a complete CVS-File and keeps int in memory.
 * 
 * Allows a kind of queries agains parsed data.
 * 
 * @author <a href=mailto:tri@top-logic.com>tri</a>
 */
public class CSVModell {

	/**
	 * The Modell of Datasets, List of ( Maps of Lists (???) indexed by ???) 
	 */
	List	theMaps; // TODO TRI does dont conform to BOS-style

    /** TODO TRI Map of (List of invalid lines) indexed by filenames (not used) */
	Map		filesWithInValidCSVLines;

	/**
	 * Create a new CSVModell ...
	 */
	public CSVModell(
            File csvFile,
            String aTokenStartQuote, 
            String aTokenEndQuote, 
            String aSeparator,
            String aNewLine,
            boolean aRequiresQuotes,
            boolean anAllowMultiline) throws IOException {
		this(csvFile, "UTF-8", aTokenStartQuote, aTokenEndQuote, aSeparator, aNewLine, aRequiresQuotes, anAllowMultiline, false);
	}

	   public CSVModell(
	            File csvFile,
	            String encoding,
	            String aTokenStartQuote, 
	            String aTokenEndQuote, 
	            String aSeparator,
	            String aNewLine,
	            boolean aRequiresQuotes,
	            boolean anAllowMultiline,
	            boolean ignoreMissingCells
	            ) throws IOException {
	        super();
	        theMaps = new ArrayList();
	        filesWithInValidCSVLines = new HashMap();
	        init(csvFile, encoding, aTokenStartQuote, aTokenEndQuote, aSeparator, aNewLine, aRequiresQuotes, anAllowMultiline, ignoreMissingCells);
	    }
	
	private CSVModell(List aList) {
		super();
		theMaps = new ArrayList();
		init(aList);
	}

    /**
     * TODO TRI init ...
     * 
     * @param dataSets List of Strings (???)
     */
	public void init(List dataSets) {
		Iterator lines = dataSets.iterator();
		while (lines.hasNext()) {
			List aDataSet = (List) lines.next();
			Iterator theValues = aDataSet.iterator();
			int tokenIdx = 0;
			while (theValues.hasNext()) {
				String aValue = (String) theValues.next();
				if (theMaps.size() <= tokenIdx) {
					theMaps.add(new HashMap());
				}
				Map theMapForValue = (Map) theMaps.get(tokenIdx++);
				List theList = (List) theMapForValue.get(aValue);
				if (theList == null) {
					theList = new ArrayList();
					theMapForValue.put(aValue, theList);
				}
				theList.add(aDataSet);
				theMapForValue.put(aValue, theList);
			}
		}
	}
//
//	/**
//	 * Adds a CSV LINE to the list of invalid lines for the given filename  
//	 * @param fileName
//	 * @param aLine
//	 */
//	private void addInValidLine(String fileName, String aLine) {
//		List aList = (List) filesWithInValidCSVLines.get(fileName);
//		if (aList == null) {
//			aList = new ArrayList();
//			filesWithInValidCSVLines.put(fileName, aList);
//		}
//		aList.add(aLine);
//	}

	/** 
	 * Build up a CSV modell consisting of a List of Maps (one Map for each
     * column), where the map stores all datasets (lines) for each value of that
     * column (as List of Datasets) where each Dataset is a list of values
     * (String).
	 */
	private void init(
            File file,
            String encoding,
            String aTokenStartQuote, 
            String aTokenEndQuote, 
            String aSeparator,
            String aNewLine,
            boolean aRequiresQuotes,
            boolean anAllowMultiline,
            boolean ignoreMissingCells        
	) throws IOException {
        BufferedReader theReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding));
        CSVLineTokenizer theTokenizer = new CSVLineTokenizer(theReader, aTokenStartQuote, aTokenEndQuote, aSeparator, aNewLine, aRequiresQuotes, anAllowMultiline, ignoreMissingCells);
        List theDataSet = null;
        boolean finished = false;
		while (!finished) {
			try {
                theDataSet = theTokenizer.getNextEntry();
                if (theDataSet != null) {
                	Iterator theValues = theDataSet.iterator();
                	int tokenIdx = 0;
                	while (theValues.hasNext()) {
                		String aValue = (String) theValues.next();
                		if (theMaps.size() <= tokenIdx) {
                			theMaps.add(new HashMap());
                		}
                		Map theMapForValue = (Map) theMaps.get(tokenIdx++);
                		List theList = (List) theMapForValue.get(aValue);
                		if (theList == null) {
                			theList = new ArrayList();
                			theMapForValue.put(aValue, theList);
                		}
                		theList.add(theDataSet);
                		theMapForValue.put(aValue, theList);
                	}
                } else {
                    finished = true;
                }
            } catch (InvalidLineException e) {
                Logger.error("File: "+file.getName()+": "+e.toString(), this);
            }
		}
	}
//
//	/**
//	 * Utility method to convert a csv string into a list of values
//	 * 
//	 * @param aLine
//	 * @return aList of values
//	 */
//	private List getLineAsValueList(String aLine, String sep) {
//		List theResult = new ArrayList();
//		StringTokenizer st = new StringTokenizer(aLine, sep, true);
//		String aValue = "";
//		boolean wasSep = false;
//		while (st.hasMoreTokens()) {
//			aValue = st.nextToken();
//			if (aValue.equals(sep)) {
//				if (wasSep) {
//					theResult.add("");
//				}
//				wasSep = true;
//			} else {
//				wasSep = false;
//				aValue = aValue.replaceAll("\"", "");
//				theResult.add(aValue);
//			}
//		}
//		return theResult;
//	}

	public CSVModell getPreparedCSVModell(String[] aKeySet) {
		List aList = getDataSets(aKeySet);
		return new CSVModell(aList);
	}

	/**
	 * Finds all datasets matching to the given keyset. Empty keys are ignored,
	 * given keys are all matched (AND) to the datasets
	 * 
	 * @return a List of DataSets where each DataSet is a List of (String)
	 *         values
	 */
	public List getDataSets(String[] aKeySet) {
		List theResult = null;
		int startIdx = 0;

		for (int idx = startIdx; idx < aKeySet.length; idx++) {
			String theKey = aKeySet[idx];
			if (theKey != null) {
				Map theMapForKey = (Map) theMaps.get(idx);
				List theDataSets = (List) theMapForKey.get(theKey);
				if (theDataSets == null) {
					theDataSets = new ArrayList();
				}
				if (theResult == null) {
					theResult = new ArrayList(theDataSets);
				} else {
					theResult.retainAll(theDataSets);
				}
			}
		}
		if (theResult == null) {
			return Collections.EMPTY_LIST;
		} else {
			return theResult;
		}
	}

	/**
	 * @param columnIdx -
	 *            the idx of the column to be returned
	 * @return One column of the DataSets matching the given keyset
	 */
	public Collection getDataSets(String[] aKeySet, int columnIdx, boolean distinct) {
		Collection theResult;
		if (distinct) {
			theResult = new HashSet();
		} else {
			theResult = new ArrayList();
		}
		Iterator dataSets = getDataSets(aKeySet).iterator();
		while (dataSets.hasNext()) {
			List aDataSet = (List) dataSets.next();
			theResult.add(aDataSet.get(columnIdx));
		}
		return theResult;
	}

	public Set getDataSetValues(int colIdx) {
		return ((Map) theMaps.get(colIdx)).keySet();
	}
	/**
	 * If a csv file contains a line with a token number that does
	 * not match the first line, this file is added to this map.
	 * Key are the filenames, value is a list of Strings which represent the lines as read from the csv file
	 * @return Returns the filesWithInValidCSVLines.
	 */
	public Map getFilesWithInValidCSVLines() {
		return filesWithInValidCSVLines;
	}
    
    
}