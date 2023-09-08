/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.config.diff;

import com.top_logic.config.diff.filediff.FileDiff;
import com.top_logic.config.diff.filediff.FileDiffGenerator;

/**
 * This class is a simplification of the original config.diff that compares entire Zip Archives 
 * with all the File in them. 
 * Here only two texts are compared.
 * 
 * @author     <a href="mailto:aru@top-logic.com">aru</a>
 */
public class SimpleTextDiff {

	RootCompareStatistic statistic = null;		// some information about the differences
	String text1 = null;						// the source text to compare with
	String text2 = null;						// the destination text to compare with the source text
	String tableHeadText1 = null;				// text to display in the HTML tables head above the source texts column
	String tableHeadText2 = null;				// text to display in the HTML tables head above the destination texts column
	
	
	
	/**
	 * Creates a new SimpleTextDiff ...
	 * 
	 * @param text1 - the source text to compare with
	 * @param text2 - the destination text to compare with the source text
	 * @param tableHeadText1 - text to display in the HTML tables head above the source texts column
	 * @param tableHeadText2 - text to display in the HTML tables head above the destination texts column
	 */
	public SimpleTextDiff(String text1, String text2, String tableHeadText1, String tableHeadText2) {
		setText1(text1);
		setText2(text2);
		setTableHeadText1(tableHeadText1);
		setTableHeadText2(tableHeadText2);
		statistic = new RootCompareStatistic();
		initializeStatistic();
	}
	
	
	
	/**
	 * This Method computes a HTML representation of the changes between the two texts
	 * that where compared.
	 * 
	 * @param markViewedChange - true, if the currently viewed change shall be highlighted, false otherwise
	 * 
	 * @return a HTML representation of the changes between the two texts
	 */
	public String getHTML(boolean markViewedChange) {
		return new FileDiffGenerator(markViewedChange).generateHtmlToString(
				getFileDiff(), 
				getTableHeadText1(), 
				getTableHeadText2(), 
				getStatistic());
	}
	

	
	/**
	 * this method returns and computes the {@link FileDiff} with the found differences
	 * between the two texts.
	 * 
	 * @return the {@link FileDiff}
	 */
	public FileDiff getFileDiff() {
		return new FileDiffGenerator().generate(getText1(), getText2());

	}

	
	
	/**
	 * this method initializes the {@link CompareStatistics} with statistics
	 * about the differences found between the two texts.
	 *
	 */
	private void initializeStatistic() {
		RootCompareStatistic theStatistic = getStatistic();
		theStatistic.findChanges(getFileDiff());
		if (theStatistic.getNumOfChangedRegions() > 0) {
			theStatistic.setCurrentChangedRegion(1);
			theStatistic.getSeenChangedRegions().add(Integer.valueOf(1));
		}
	}

	
	
	public RootCompareStatistic getStatistic() {
		return (statistic);
	}

	public String getText1() {
		return (text1);
	}

	public void setText1(String text1) {
		this.text1 = text1;
	}

	public String getText2() {
		return (text2);
	}

	public void setText2(String text2) {
		this.text2 = text2;
	}

	public String getTableHeadText1() {
		return (tableHeadText1);
	}

	public void setTableHeadText1(String tableHeadText1) {
		this.tableHeadText1 = tableHeadText1;
	}

	public String getTableHeadText2() {
		return (tableHeadText2);
	}

	public void setTableHeadText2(String tableHeadText2) {
		this.tableHeadText2 = tableHeadText2;
	}
}