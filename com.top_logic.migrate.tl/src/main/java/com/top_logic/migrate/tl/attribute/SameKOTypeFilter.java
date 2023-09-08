/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.migrate.tl.attribute;

import com.top_logic.basic.col.Filter;
import com.top_logic.knowledge.event.ItemEvent;

/**
 * Filter matching all events having the given KO type.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SameKOTypeFilter implements Filter<ItemEvent> {

	private final String _koType;

	/**
	 * Creates a new {@link SameKOTypeFilter}.
	 * 
	 * @param type
	 *        The knowledge object type to match.
	 */
	public SameKOTypeFilter(String type) {
		_koType = type;
	}

	@Override
	public boolean accept(ItemEvent event) {
		return event.getObjectType().getName().equals(_koType);
	}

}

