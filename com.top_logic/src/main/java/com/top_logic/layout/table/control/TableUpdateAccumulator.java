/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.control;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.top_logic.basic.col.LazyListModifyable;
import com.top_logic.layout.basic.AbstractAttachable;
import com.top_logic.layout.table.TableModel;

/**
 * Computes a set of non-overlapping updates from a number of {@link TableModel} changes.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class TableUpdateAccumulator extends AbstractAttachable {

	private List<UpdateRequest> updateRequestQueue;
	private List<UpdateRequest> normalizedUpdateQueue;

	/**
	 * Create a new TableUpdateAccumulator
	 */
	public TableUpdateAccumulator() {
		updateRequestQueue = new LazyListModifyable<>() {

			@Override
			protected List<UpdateRequest> initInstance() {
				return new ArrayList<>();
			}
		};

		normalizedUpdateQueue = new LazyListModifyable<>() {

			@Override
			protected List<UpdateRequest> initInstance() {
				return new ArrayList<>();
			}
		};
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void internalAttach() {
		// Nothing to do here
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void internalDetach() {
		clearUpdateQueue();
	}

	/**
	 * Adds an update request to the update queue.
	 * 
	 * @param firstRow
	 *        - the first row of the table range to update
	 * @param lastRow
	 *        - the last row of the table range to update
	 */
	public void addUpdate(int firstRow, int lastRow) {
		assert firstRow <= lastRow : "First row must be smaller as or equal to last row!";
		
		if (!isAttached()) {
			// Introduced with #5740. Revert to the lower, when the TableViewModel gets its own validation phase.
			// throw new IllegalStateException("Updates must not be added, if the accumulator is not attached!");
			return;
		}

		// If this method is called after getUpdates(), but in the same update cycle
		if (!normalizedUpdateQueue.isEmpty()) {
			updateRequestQueue.addAll(normalizedUpdateQueue);
			normalizedUpdateQueue.clear();
		}

		updateRequestQueue.add(new UpdateRequest(firstRow, lastRow));
	}

	/**
	 * Clears the update queue
	 */
	public void clearUpdateQueue() {
		normalizedUpdateQueue.clear();
		updateRequestQueue.clear();
	}

	/**
	 * Returns true, if there are any table updates, false otherwise.
	 */
	public boolean hasUpdates() {
		return !updateRequestQueue.isEmpty() || !normalizedUpdateQueue.isEmpty();
	}

	/**
	 * Returns the list of accumulated and normalized (combined, overlap-free) updates
	 */
	public List<UpdateRequest> getUpdates() {

		// Check if normalized update queue exists
		if (!normalizedUpdateQueue.isEmpty()) {
			return normalizedUpdateQueue;
		}
		
		/*
		 * Determine normalized (combined, overlap-free) update ranges
		 */

		// Sort update requests by their first row
		Collections.sort(updateRequestQueue, UpdateRequestComparator.INSTANCE);
		
		if (!updateRequestQueue.isEmpty()) {
			int newFirstRow = updateRequestQueue.get(0).getFirstAffectedRow();
			int newLastRow = updateRequestQueue.get(0).getLastAffectedRow();
			for (int i = 1, size = updateRequestQueue.size(); i < size; i++) {
				UpdateRequest updateRequest = updateRequestQueue.get(i);

				// If current update request begins at least at the very next row of the already
				// known last row (means, the current update request fits at least with a seam
				// to former update requests or is partially or fully covered by them), determine
				// maximum of last rows.
				if (updateRequest.getFirstAffectedRow() < (newLastRow + 2)) {
					newLastRow = Math.max(newLastRow, updateRequest.getLastAffectedRow());
				}

				// If a new update range begins, add previous update range to the normalized
				// update queue and set up new range parameters
				else {
					normalizedUpdateQueue.add(new UpdateRequest(newFirstRow, newLastRow));
					newFirstRow = updateRequest.getFirstAffectedRow();
					newLastRow = updateRequest.getLastAffectedRow();
				}

			}
			// Add last normalized update request
			normalizedUpdateQueue.add(new UpdateRequest(newFirstRow, newLastRow));
		}
		
		updateRequestQueue.clear();

		return normalizedUpdateQueue;
	}

	/**
	 * Holds the row range for an single update request.
	 */
	public class UpdateRequest {

		private final int firstRow;
		private final int lastRow;

		UpdateRequest(int firstRow, int lastRow) {
			this.firstRow = firstRow;
			this.lastRow = lastRow;
		}

		/**
		 * Returns the firstRow.
		 */
		public int getFirstAffectedRow() {
			return firstRow;
		}

		/**
		 * Returns the lastRow.
		 */
		public int getLastAffectedRow() {
			return lastRow;
		}
	}

	/**
	 * {@link Comparator} for {@link UpdateRequest}. Therefore two update entries will be treated as
	 * equal, if {@link UpdateRequest#getFirstAffectedRow()} returns the same value.
	 */
	private static class UpdateRequestComparator implements Comparator<UpdateRequest> {

		private static final UpdateRequestComparator INSTANCE = new UpdateRequestComparator();

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int compare(UpdateRequest o1, UpdateRequest o2) {
			return o1.getFirstAffectedRow() - o2.getFirstAffectedRow();
		}
	}
}
