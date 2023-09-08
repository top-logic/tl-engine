/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.event;

import java.util.HashMap;
import java.util.Map;

import com.top_logic.basic.col.MapUtil;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.ItemEvent;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;

/**
 * The class {@link TestChangeSet} tests {@link ChangeSet}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestChangeSet {

	/**
	 * Indexes the given events by their {@link ObjectBranchId}.
	 */
	public static <E extends ItemEvent> Map<ObjectBranchId, E> index(Iterable<E> events) {
		HashMap<ObjectBranchId, E> result = MapUtil.newMap(16);
		for (E event : events) {
			E formerEvent = result.put(event.getObjectId(), event);
			if (formerEvent != null) {
				throw new IllegalArgumentException("More than one event for same object: " + formerEvent + ", " + event);
			}
		}
		return result;
	}

}
