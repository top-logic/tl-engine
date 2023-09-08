/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.config.diff.filediff;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.config.diff.google.diff_match_patch.Operation;

/**
 * Abstract class representing Lines of Text, with or without changes. Those {@link Line}s containing
 * a {@link List} of {LinePart}s.
 * 
 * @author     <a href="mailto:aru@top-logic.com">aru</a>
 */
public abstract class AbstractLine {

	private final List<LinePart> lineParts;
	
	protected AbstractLine() {
		this(new ArrayList<>());
	}
	
	protected AbstractLine(List<LinePart> lineParts) {
		this.lineParts = lineParts;
	}

	public boolean isUnchanged() {
		boolean isEqual = true;
		for (LinePart linePart : getLineParts()) {
			if( ! Operation.EQUAL.equals(linePart.getOperation())) {
				isEqual = false;
				break;
			}
		}
		return isEqual;
	}
	
	public boolean isChanged() {
		return !isUnchanged();
	}
	
	protected void internalReset(){
		lineParts.clear();
	}
	
	public final List<LinePart> getLineParts() {
		return lineParts;
	}
	

	public void addLinePart(LinePart linePart) {
		lineParts.add(linePart);
	}

	public String getText() {
		StringBuilder result = new StringBuilder();
		for (LinePart linePart : getLineParts()) {
			result.append(linePart.getText());
		}
		return result.toString();
	}
	
	@Override
	public String toString() {
		return getText();
	}

	@Override
	public boolean equals(Object obj) {
		if (! (obj instanceof AbstractLine)) {
			return false;
		}
		
		return equalsLine((AbstractLine) obj);
	}

	private boolean equalsLine(AbstractLine other) {
		List<LinePart> myParts = this.getLineParts();
		List<LinePart> otherParts = other.getLineParts();
		
		return myParts.equals(otherParts);
	}
	

	@Override
	public int hashCode() {
		StringBuilder sb = new StringBuilder();
		sb.append(getText());
		return sb.toString().hashCode();
	}
	
	public boolean hasEndOfLine() {
		List<LinePart> parts = getLineParts();
		if(parts.isEmpty()){
			return false;
		}
		else{
			LinePart part = parts.get(parts.size()-1);
			return part.isNL();
		}
	}

}
