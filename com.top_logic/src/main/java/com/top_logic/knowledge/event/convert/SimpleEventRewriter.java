/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.event.convert;

import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.EventWriter;

/**
 * The class {@link SimpleEventRewriter} is an {@link EventRewriter} which simply writes the given
 * event to the output.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public final class SimpleEventRewriter implements EventRewriter {

	/**
	 * The singleton instance of this class.
	 */
	public static final SimpleEventRewriter INSTANCE = new SimpleEventRewriter();

	private SimpleEventRewriter() {
		// singleton instance
	}

	@Override
	public void rewrite(ChangeSet cs, EventWriter out) {
		out.write(cs);
	}

	/**
	 * Returns the singleton instance of {@link SimpleEventRewriter}.
	 */
	public static EventRewriter getInstance() {
		return INSTANCE;
	}

}

