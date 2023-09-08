/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.list.model;

import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;

/**
 * A {@link AbstractListModelView view of} a range of items in a base
 * {@link ListModel}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class RangeListModel extends AbstractListModelView {

	private int rangeStart;
	private int rangeStop;

	public RangeListModel(ListModel base) {
		super(base);
	}
	
	public int getRangeStartIndex() {
		return rangeStart;
	}
	
	public int getRangeStopIndex() {
		return rangeStop;
	}
	
	public int getRangeSize() {
		return rangeStop - rangeStart;
	}

	public void setRange(int newRangeStart, int newRangeSize) {
		checkAttachException();
		int newRangeStop = newRangeStart + newRangeSize;
		
		checkRange(newRangeStart, newRangeStop);
		
		// Save the state before the update.
		int rangeStartBefore = this.rangeStart;
		int rangeStopBefore = this.rangeStop;
		int rangeSizeBefore = rangeStopBefore - rangeStartBefore;

		if (newRangeStart == rangeStartBefore && newRangeStop == rangeStopBefore) {
			// No change.
			return;
		}
				
		// Event handling.
		if (newRangeStart >= rangeStopBefore || newRangeStop <= rangeStartBefore) {
			// There is no overlap. Assume a complete content change.
			
			// Shrink interval to zero elements.
			this.rangeStart = newRangeStart;
			this.rangeStop = newRangeStart;
			if (rangeSizeBefore > 0) {
				fireIntervalRemoved(this, 0, rangeSizeBefore - 1);
				if(!isAttached()) return;
			}
			
			// Add new elements.
			this.rangeStop = newRangeStop;
			fireIntervalAdded(this, 0, newRangeSize - 1);
		} else {
			this.rangeStart = newRangeStart;
			int leftInsertionCount = rangeStartBefore - newRangeStart;
			if (leftInsertionCount > 0) {
				// Content was inserted to the left.
				fireIntervalAdded(this, 0, leftInsertionCount - 1);
			}
			else if (leftInsertionCount < 0) {
				// Content was removed from the left.
				int leftDeletionCount = -leftInsertionCount;
				fireIntervalRemoved(this, 0, leftDeletionCount - 1);
			}
			if(!isAttached()) return;
			
			int prelimaryRangeSize = rangeSizeBefore + leftInsertionCount;

			this.rangeStop = newRangeStop;
			int rightInsertionCount = newRangeStop - rangeStopBefore;
			if (rightInsertionCount > 0) {
				// Content was inserted to the right.
				fireIntervalAdded(this, prelimaryRangeSize, prelimaryRangeSize + rightInsertionCount - 1);
			}
			else if (rightInsertionCount < 0) {
				// Content was removed from the right.
				int rightDeletionCount = -rightInsertionCount;
				fireIntervalRemoved(this, prelimaryRangeSize - rightDeletionCount, prelimaryRangeSize - 1);
			}
		}
	}

	private void checkRange(int start, int stop) {
		if ((start < 0) || (stop < 0)) {
			throw new IndexOutOfBoundsException(
				"Negative range index: (" + start + ", " + stop + ")");
		}
		if (start > stop) {
			throw new IndexOutOfBoundsException(
				"Negative range size: (" + start + ", " + stop + ")");
		}
		if (stop > base.getSize()) {
			throw new IndexOutOfBoundsException(
				"Range does not match base model size: base.size=" + base.getSize() + ", range=(" + start + ", " + stop + ")");
		}
	}

	@Override
	public int getSize() {
		return this.rangeStop - this.rangeStart;
	}

	@Override
	public Object getElementAt(int index) {
		checkAttachException();
		return base.getElementAt(rangeStart + index);
	}

	
	// ListDataListener implementation.
	
	@Override
	public void contentsChanged(ListDataEvent event) {
		int startIndex = event.getIndex0();
		int stopIndex = event.getIndex1() + 1;

		checkRange(startIndex, stopIndex);

		if (startIndex >= rangeStop) {
			// View model does not change.
			return;
		}
		
		if (stopIndex <= rangeStart) {
			// View model does not change.
			return;
		}
		
		int internalStartIndex = Math.max(rangeStart, startIndex) - rangeStart;
		int internalStopIndex = Math.min(rangeStop, stopIndex) - rangeStart;
		
		int internalChangedCount = internalStopIndex - internalStartIndex;
		assert internalChangedCount > 0 : 
			"The base model was changed at some visible index.";
		
		fireContentsChanged(this, internalStartIndex, internalStopIndex - 1);
	}

	@Override
	public void intervalAdded(ListDataEvent event) {
		int startIndex = event.getIndex0();
		int stopIndex = event.getIndex1() + 1;

		int insertionCount = stopIndex - startIndex;
		
		if (startIndex >= rangeStop) {
			// The insertion happened right of the visible range, the view model
			// does not change.
			return;
		}

		if (startIndex <= rangeStart) {
			// The insertion happened right of the visible range, the internal
			// range must be adjusted, the model contents does not change.
			this.rangeStart += insertionCount;
			this.rangeStop += insertionCount;
			return;
		}

		// The insertion happened within the range of this model. The range size
		// does increase.
		int internalInsertStartIndex = startIndex - rangeStart;
		int internalInsertStopIndex = stopIndex - rangeStart;
		this.rangeStop += insertionCount;
		fireIntervalAdded(this, internalInsertStartIndex, internalInsertStopIndex - 1);
	}

	@Override
	public void intervalRemoved(ListDataEvent event) {
		int startIndex = event.getIndex0();
		int stopIndex = event.getIndex1() + 1;

		int deletionCount = stopIndex - startIndex;
		
		if (startIndex >= rangeStop) {
			// The deletion happened completely right of the visible range. The
			// view model does not change.
			return;
		}
		
		if (stopIndex <= rangeStart) {
			// The deletion happened right of the visible range, the internal
			// range must be adjusted, the model contents does not change.
			this.rangeStart -= deletionCount;
			this.rangeStop -= deletionCount;
			return;
		}

		int shiftCount = rangeStart - startIndex;
		if (shiftCount > 0) {
			// The deletion happened partially left of the visible range. Adjust
			// the range accordingly.
			this.rangeStart -= shiftCount;
			this.rangeStop -= shiftCount;
			
			// Adjust the event range.
			startIndex = rangeStart;
		}

		// The deletion happened entirely within the range of this model. The
		// range size does decrease.
		int internalStartIndex = startIndex - rangeStart;
		int internalStopIndex = stopIndex - rangeStart;
		this.rangeStop -= deletionCount;
		fireIntervalRemoved(this, internalStartIndex, internalStopIndex - 1);
	}
	
}
