/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.config.diff;

import java.util.zip.ZipEntry;

import com.top_logic.config.diff.zip.ZipReader.EntryResult;

/**
 * {@link CompareStatistics} for the root of a comparison.
 *
 * <p>
 * When all changes of the comparison have been seen, the root informs its
 * {@link CompareExecutedListener} (once).
 * </p>
 *
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

	/**
	 * Sets the listener that is informed when all changes of the comparison have been seen.
	 */
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

	/**
	 * Callback informed when all changes of a comparison have been seen by the user.
	 */
	public static interface CompareExecutedListener {
		/**
		 * Called when all changes between the two compared entries have been seen.
		 *
		 * @param aZE1
		 *         The source entry of the comparison.
		 * @param aZE2
		 *         The destination entry of the comparison.
		 */
		void compareExecuted(ZipEntry aZE1, ZipEntry aZE2);
	}
	
}
