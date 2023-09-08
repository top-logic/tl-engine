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
 * Abstract class to describe {@link Line}s as long as there are still {@link LinePart}s are
 * gathered. Important is the abstract <tt>accepts()</tt> method, with which those TempLines know
 * which LineParts they accept and append, and which let go by.<br/>
 * <b>This class contains two static classes</b> for {@link TempSourceLine}s and
 * {@link TempDestLine}s which implementing the <tt>accepts()</tt> method.
 * 
 * @author <a href="mailto:aru@top-logic.com">aru</a>
 */
public abstract class TempLine extends AbstractLine {
	
	private boolean crSeen;
	
	private List<Line> producedLines = new ArrayList<>();

	protected int lineIndex = 1;
	
	public void offerLinePart(LinePart part) {
		if (accepts(part.getOperation())){
			if (part.isNL()) {
				boolean isCr = part.getText().equals("\r");
				if (isCr) {
					if (crSeen) {
						flushLine();
						addLinePart(part);
						// crSeen stays as it is.
					} else {
						addLinePart(part);
						crSeen = true;
					}
				} else {
					// line break, but not \r
					
					if (crSeen) {
						boolean isLf = part.getText().equals("\n");
						if (isLf) {
							// Combine \r\n.
							addLinePart(part);
							flushLine();
						} else {
							flushLine();
							
							// Produce empty line.
							addLinePart(part);
							flushLine();
						}
						
						crSeen = false;
					} else {
						// Terminate line.
						addLinePart(part);
						flushLine();
					}
				}
			} else {
				if (crSeen) {
					flushLine();
					crSeen = false;
				}
				addLinePart(part);
			}
		} else {
			annotateLine(part);
		}
	}
	
	public void flush() {
		if (!getLineParts().isEmpty()) {
			flushLine();
		}
	}
	
	private void flushLine() {
		Line producedLine = new Line(this);
		producedLines.add(producedLine);
		
		lineIndex++;
		
		internalReset();
	}
	
	public List<Line> getProducedLines() {
		return producedLines;
	}

	protected abstract boolean accepts(Operation operation);
	
	protected abstract void annotateLine(LinePart linePart);

	public static class TempSourceLine extends TempLine {

		@Override
		protected boolean accepts(Operation operation) {
			return Operation.EQUAL==operation || Operation.DELETE==operation;
		}
		
		@Override
		public void addLinePart(LinePart linePart) {
			super.addLinePart(linePart);
			
			annotateLine(linePart);
		}

		@Override
		protected void annotateLine(LinePart linePart) {
			linePart.setSrcLine(lineIndex);
		}

	}

	
	
	public static class TempDestLine extends TempLine {

		@Override
		protected boolean accepts(Operation operation) {
			return Operation.EQUAL==operation || Operation.INSERT==operation;
		}
		
		@Override
		public void addLinePart(LinePart linePart) {
			super.addLinePart(linePart);

			annotateLine(linePart);
		}

		@Override
		protected void annotateLine(LinePart linePart) {
			linePart.setDestLine(lineIndex);
		}
	}

	
	
	public boolean isEmpty() {
		return getLineParts().isEmpty();
	}

}
