/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.config.diff;

import java.util.zip.ZipEntry;

import com.top_logic.config.diff.zip.ZipReader.EntryResult;

/**
 * @author     <a href="mailto:aru@top-logic.com">aru</a>
 */
public class RootCompareStatistic extends CompareStatistics {

	boolean notlogged = true;
	private CompareExecutedListener listener = null;

	public RootCompareStatistic(EntryResult entryResult) {
		super(entryResult);
	}


	public RootCompareStatistic() {
		super();
	}

	public void setCompareExecutedListener(CompareExecutedListener aCEL) {
		listener = aCEL;
	}

	@Override
	protected void validateAllChangesSeen() {
		if (getEntryResult() != null) {
			if (getNumOfChildren() == getSeenChildren().size()) {
				setAllChildrenSeen(true);
				setAllChangesSeen(true);
			}
		} 
		else if (getNumOfChangedRegions() == getSeenChangedRegions().size()) {
			setAllChildrenSeen(true);
			setAllChangesSeen(true);
		}
		if (areAllChangesSeen() && notlogged) {
			if (listener != null) {
				listener.compareExecuted(getEntryResult().getSourceEntry(), getEntryResult().getDestEntry());
			}
			notlogged = false;
		}
	}

	public static interface CompareExecutedListener {
		void compareExecuted(ZipEntry aZE1, ZipEntry aZE2);
	}
	
}
