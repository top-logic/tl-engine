/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.config.diff.filediff;

import java.util.ArrayList;
import java.util.List;


/**
 * This class organizes the {@link Line}s of the compared texts into regions; Those with changes,
 * and those without.
 * <p>
 * <b>sourceLines</b> - Lines from the original text. <br/>
 * <b>destLines</b> - Lines from the text with the changes; the one to compare with.<br/>
 * <b>isChanged</b> - Flag, set if the region contains lines with changes.
 * </p>
 * 
 * @author <a href="mailto:aru@top-logic.com">aru</a>
 */
public class Region {
	
	private List<AbstractLine> sourceLines;
	private List<AbstractLine> destLines;

	private final boolean  isChanged;
	
	public Region(boolean changed) {
		sourceLines = new ArrayList<>();
		destLines = new ArrayList<>();
		isChanged = changed;
	}
	
	/**
	 * Whether this region contains changed lines.
	 */
	public boolean isChanged(){
		return isChanged;
	}

	/**
	 * Appends a line of the source text to this region.
	 */
	public void addSourceLine(AbstractLine line) {
		sourceLines.add(line);
	}
	
	/**
	 * Appends a line of the destination text to this region.
	 */
	public void addDestLine(AbstractLine line) {
		destLines.add(line);
	}
		
	/**
	 * A copy of the source text lines of this region.
	 */
	public List<AbstractLine> getSourceLines() {
		return new ArrayList<>(sourceLines);
	}
	/**
	 * A copy of the destination text lines of this region.
	 */
	public List<AbstractLine> getDestLines() {
		return new ArrayList<>(destLines);
	}
	
	/*
	 * The following method is for an easier debugging, if necessary
	 */
	@Override
	public String toString() {
		StringBuffer res = new StringBuffer();
		res.append("SOURCE: ");
		for(AbstractLine line : getSourceLines()){
			res.append(line.getText() + "\n");
		}
		res.append("\nDEST: ");
		for(AbstractLine line : getDestLines()){
			res.append(line.getText()+ "\n");
		}
		return res.toString();
	}
}
