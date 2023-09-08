/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.list.model;

import java.util.ArrayList;
import java.util.Collections;

import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.filter.FilterFactory;

/**
 * An {@link AbstractListModelView list model view} that only shows a subset of
 * the items from its base model.
 * 
 * <p>
 * If an item from the base model is shown, is determined by a {@link Filter}
 * instance.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class FilteredListModel extends AbstractListModelView {

	private Filter filter;
	private ArrayList cache;
	
	public FilteredListModel(ListModel base) {
		super(base);
		this.filter = FilterFactory.trueFilter();
	}

	public boolean hasMatch(Filter testedFilter) {
		checkAttachException();
		for (int cnt = base.getSize(), baseIndex = 0; baseIndex < cnt; baseIndex++) {
			Object item = base.getElementAt(baseIndex);
			if (testedFilter.accept(item)) {
				return true;
			}
		}
		return false;
	}

	public void setFilter(Filter newFilter) {
		this.filter = newFilter;
		
		if (isAttached()) {
			int baseSize = base.getSize();
			int cacheIndex = 0;
			int cacheIndexElement = cacheIndex < cache.size() ? ((Integer) cache.get(cacheIndex)).intValue() : -1;
			for (int baseIndex = 0; baseIndex < baseSize; baseIndex++) {
				Object item = base.getElementAt(baseIndex);
				
				boolean itemDidMatch = cacheIndexElement == baseIndex;
				if (itemDidMatch) {
					cacheIndex++;
					cacheIndexElement = cacheIndex < cache.size() ? ((Integer) cache.get(cacheIndex)).intValue() : -1;
				}
				
				boolean itemDoesMatch = filter.accept(item);
				
				if (itemDoesMatch && (! itemDidMatch)) {
					// The item must be added to this filtered list.
					this.cache.add(cacheIndex, Integer.valueOf(baseIndex));
					fireIntervalAdded(this, cacheIndex, cacheIndex);
					cacheIndex++;
				}
				else if (itemDidMatch && (! itemDoesMatch)) {
					cacheIndex--;
					this.cache.remove(cacheIndex);
					fireIntervalRemoved(this, cacheIndex, cacheIndex);
				}
				if (!isAttached()) return;
			}
		}
	}

	/**
	 * @see com.top_logic.layout.list.model.AbstractListModelView#internalAttach()
	 */
	@Override
	protected void internalAttach() {
		super.internalAttach();
		initCache();
	}

	/**
	 * @see com.top_logic.layout.list.model.AbstractListModelView#internalDetach()
	 */
	@Override
	protected void internalDetach() {
		super.internalDetach();
		cache = null;
	}

	private void initCache() {
		this.cache = new ArrayList();
		for (int cnt = base.getSize(), baseIndex = 0; baseIndex < cnt; baseIndex++) {
			Object item = base.getElementAt(baseIndex);
			if (filter.accept(item)) {
				this.cache.add(Integer.valueOf(baseIndex));
			}
		}
	}

	@Override
	public int getSize() {
		checkAttachException();
		return cache.size();
	}

	@Override
	public Object getElementAt(int index) {
		checkAttachException();
		return base.getElementAt(((Integer) cache.get(index)).intValue());
	}

	@Override
	public void contentsChanged(ListDataEvent event) {
		int firstIndex = event.getIndex0();
		int lastIndex = event.getIndex1();
		
		int internalStartIndex = searchInternalStartIndex(firstIndex);
		int internalStopIndex = searchInternalStopIndex(lastIndex);

		ArrayList updatedCache = 
			new ArrayList(internalStartIndex + (cache.size() - internalStopIndex));
		
		updatedCache.addAll(0, cache.subList(0, internalStartIndex));
		
		for (int baseIndex = firstIndex; baseIndex <= lastIndex; baseIndex++) {
			Object item = base.getElementAt(baseIndex);
			if (filter.accept(item)) {
				updatedCache.add(Integer.valueOf(baseIndex));
			}
		}
		
		int internalUpdatedStopIndex = updatedCache.size();
		
		updatedCache.addAll(cache.subList(internalStopIndex, cache.size()));
		
		this.cache = updatedCache;
		
		int internalChangedStopIndex = Math.min(internalUpdatedStopIndex, internalStopIndex);
		
		if (internalChangedStopIndex > internalStartIndex) {
			fireContentsChanged(this, internalStartIndex, internalChangedStopIndex - 1);
			if (!isAttached()) return;
		}
		
		if (internalUpdatedStopIndex < internalStopIndex) {
			// Contents was removed at the end of the updated interval.
			fireIntervalRemoved(this, internalUpdatedStopIndex, internalStopIndex - 1);
		}
		else if (internalUpdatedStopIndex > internalStopIndex) {
			// Contents was added at the end of the updated interval.
			fireIntervalAdded(this, internalStopIndex, internalUpdatedStopIndex - 1);
		}
	}

	@Override
	public void intervalAdded(ListDataEvent event) {
		int firstIndex = event.getIndex0();
		int lastIndex = event.getIndex1();
		int insertionCount = lastIndex - firstIndex + 1;
			
		ArrayList cacheInsertion = new ArrayList();
		for (int index = firstIndex; index <= lastIndex; index++) {
			if (filter.accept(base.getElementAt(index))) {
				cacheInsertion.add(Integer.valueOf(index));
			}
		}
		
		if (cacheInsertion.size() == 0) {
			// Filtered model did not change.
			return;
		}

		int internalStartIndex = searchInternalStartIndex(firstIndex);
		
		// Update the cache elements that point to base elemens, whose indexes
		// have changed (increased by the number of inserted elements into the
		// base model).
		updateCachedIndexes(internalStartIndex, insertionCount);
			
		// Insert the new indexes into the cache.
		cache.addAll(internalStartIndex, cacheInsertion);
		
		fireIntervalAdded(this, internalStartIndex, internalStartIndex + cacheInsertion.size() - 1);
	}

	@Override
	public void intervalRemoved(ListDataEvent event) {
		int firstIndex = event.getIndex0();
		int lastIndex = event.getIndex1();
		
		int removalCount = lastIndex - firstIndex + 1;
		
		int internalStartIndex = searchInternalStartIndex(firstIndex);
		int internalStopIndex = searchInternalStopIndex(lastIndex);
		
		int internalRemovalCount = internalStopIndex - internalStartIndex;
		
		// Update the cached indexes of elements, whose indexes have changed
		// (decreased by the number of items deleted from the base model).
		//
		// Note: Update internal indexes before potentially exiting (because no
		// observer-visible change happende to this model).
		updateCachedIndexes(internalStartIndex, -removalCount);
		
		if (internalRemovalCount == 0) {
			// The filtered model did not change.
			return;
		}
		
		// Remove the corresponding elements from the filtered model.
		cache.subList(internalStartIndex, internalStopIndex).clear();
		
		fireIntervalRemoved(this, internalStartIndex, internalStopIndex - 1);
	}

	private int searchInternalStartIndex(int firstIndex) {
		int internalStartIndex;
		{
			int searchResult = 
				Collections.binarySearch(cache, Integer.valueOf(firstIndex));
		
			if (searchResult >= 0) {
				internalStartIndex = searchResult;
			} else {
				// searchResult == -(insertion point) - 1
				internalStartIndex = -searchResult - 1;
			}
		}
		return internalStartIndex;
	}

	private int searchInternalStopIndex(int lastIndex) {
		int internalStopIndex;
		{
			int searchResult = 
				Collections.binarySearch(cache, Integer.valueOf(lastIndex));
			
			if (searchResult >= 0) {
				internalStopIndex = searchResult + 1;
			} else {
				// searchResult == -(insertion point) - 1
				internalStopIndex = -searchResult - 1;
			}
		}
		return internalStopIndex;
	}
	
	private void updateCachedIndexes(int internalStartIndex, int delta) {
		for (int internalSize = cache.size(), n = internalStartIndex; n < internalSize; n++) {
			cache.set(n, Integer.valueOf(((Integer) cache.get(n)).intValue() + delta));
		}
	}

}
