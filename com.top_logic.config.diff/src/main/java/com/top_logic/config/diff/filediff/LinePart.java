/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.config.diff.filediff;

import java.util.LinkedList;
import java.util.List;

import com.top_logic.config.diff.google.diff_match_patch.Diff;
import com.top_logic.config.diff.google.diff_match_patch.Operation;


/**
 * This class represents a part of lines text. It contains information about line endings
 * (<tt>hasEndOfLine</tt>), the kind of {@link Operation} used on this textpart and 
 * the part of the text itself. <br/>
 * A LinePart is part of the resulting {@link List} of the normalized result computed by Googles 
 * diff_match_patch library, a {@link LinkedList} of {@link Diff}s.
 * @author     <a href="mailto:aru@top-logic.com">aru</a>
 */
public abstract class LinePart {

	private final String text;
	private final Operation operation;
	private final int diffIndex;
	private int srcLine;
	private int dstLine;
	
	public LinePart(String text, Operation operation, int diffIndex) {
		this.text = text;
		this.operation = operation;
		this.diffIndex = diffIndex;
	}

	public String getText() {
		return (text);
	}

	public Operation getOperation() {
		return (operation);
	}
	
	public int getDiffIndex() {
		return diffIndex;
	}
	
	public int getSrcLine() {
		return srcLine;
	}
	
	public void setSrcLine(int lineIndex) {
		this.srcLine = lineIndex;
	}

	public int getDstLine() {
		return dstLine;
	}

	public void setDestLine(int lineIndex) {
		this.dstLine = lineIndex;
	}
	
	public abstract boolean isNL();
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof LinePart)) {
			return false;
		}
		
		return equalsLinePart((LinePart) obj);
	}

	private boolean equalsLinePart(LinePart other) {
		return this.operation == other.operation && this.text.equals(other.text);
	}

	@Override
	public String toString() {
		return text + " " + operation;
	}

}
