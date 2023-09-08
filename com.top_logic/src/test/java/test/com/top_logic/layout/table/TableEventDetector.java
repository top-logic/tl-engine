/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.table;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.top_logic.layout.table.TableModel;
import com.top_logic.layout.table.model.TableModelEvent;
import com.top_logic.layout.table.model.TableModelListener;

/**
 * {@link TableModelListener} to detect type and amount of {@link TableModelEvent}s, which will be
 * fired during modifying operations of a {@link TableModel}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class TableEventDetector implements TableModelListener {

	private Map<Integer, Integer> detectedEvents;

	public TableEventDetector(TableModel modelToObserve) {
		detectedEvents = new HashMap<>();

		modelToObserve.addTableModelListener(this);
	}

	public int getSortEventCount() {
		return getEventCount(TableModelEvent.COLUMN_SORT_UPDATE);
	}

	public int getInvalidateEventCount() {
		return getEventCount(TableModelEvent.INVALIDATE);
	}

	public int getFilterEventCount() {
		return getEventCount(TableModelEvent.COLUMN_FILTER_UPDATE);
	}

	public int getOverallEventCount() {
		int overallEventCount = 0;
		Collection<Integer> eventCounts = detectedEvents.values();
		for (Integer eventTypeCount : eventCounts) {
			overallEventCount += eventTypeCount;
		}

		return overallEventCount;
	}

	public void resetEventCounters() {
		detectedEvents.clear();
	}

	@Override
	public void handleTableModelEvent(TableModelEvent event) {

		if (wasEventTypeAlreadyDetected(event)) {
			increaseEventTypeOccurenceCount(event);
		} else {
			registerFirstEventTypeOccurence(event);
		}
	}

	private int getEventCount(int eventType) {
		if (detectedEvents.containsKey(eventType)) {
			return detectedEvents.get(eventType);
		}

		return 0;
	}

	private boolean wasEventTypeAlreadyDetected(TableModelEvent event) {
		return detectedEvents.containsKey(event.getType());
	}

	private void increaseEventTypeOccurenceCount(TableModelEvent event) {
		detectedEvents.put(event.getType(), detectedEvents.get(event.getType()) + 1);
	}

	private Integer registerFirstEventTypeOccurence(TableModelEvent event) {
		return detectedEvents.put(event.getType(), 1);
	}
}
