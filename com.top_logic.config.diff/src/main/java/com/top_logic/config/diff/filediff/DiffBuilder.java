/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.config.diff.filediff;

import java.util.ArrayList;
import java.util.List;

/**
 * Algorithm grouping {@link Line}s into {@link Region}s of changes.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class DiffBuilder {
	private List<Region> regions;
	
	private Region current;

	public FileDiff createDiff(final List<Line> sourceLines, List<Line> destLines) {
		regions = new ArrayList<>();
		current = null;
		
		InputQueue<Line> srcQueue = new ListQueue<>(sourceLines);
		InputQueue<Line> dstQueue = new ListQueue<>(destLines);
		
		if(!(srcQueue.isEmpty() && dstQueue.isEmpty())) {
			while(true) {
				AbstractLine srcLine = srcQueue.peekOrNull();
				AbstractLine dstLine = dstQueue.peekOrNull();
				
				if (srcLine == null && dstLine == null) {
					break;
				}
				
				int comparison = compare(srcLine, dstLine);
				if (comparison == 0) {
					produceUnchangedLine(srcLine);
					
					srcQueue.pop();
					dstQueue.pop();
				} else {
					if (comparison < 0) {
						produceChangedRegion().addSourceLine(srcLine);
						srcQueue.pop();
					} else {
						produceChangedRegion().addDestLine(dstLine); 
						dstQueue.pop();
					}
				}
			}
		}

		return new FileDiff(regions);
	}

	private int compare(AbstractLine srcLine, AbstractLine dstLine) {
		if (srcLine == null) {
			// Consume dstLine.
			return 1;
		}
		if (dstLine == null) {
			// Consume srcLine.
			return -1;
		}
		
		int srcIndex = srcLine.getLineParts().get(0).getDiffIndex();
		int dstIndex = dstLine.getLineParts().get(0).getDiffIndex();
		
		if (srcIndex < dstIndex) {
			return -1;
		}
		else if (srcIndex > dstIndex) {
			return 1;
		}
		else {
			if (srcLine.equals(dstLine)) {
				return 0;
			} else {
				return -1;
			}
		}
	}
	
	private void produceUnchangedLine(AbstractLine srcLine) {
		if (isFirst() || current.isChanged()) {
			addRegion(new Region(false));
		}
		
		current.addSourceLine(srcLine);
		current.addDestLine(srcLine);
	}

	private Region produceChangedRegion() {
		if (isFirst() || !current.isChanged()) {
			addRegion(new Region(true));
		}
		return current;
	}

	protected boolean isFirst() {
		return current == null;
	}

	private void addRegion(Region region) {
		regions.add(region);
		current = region;
	}

}