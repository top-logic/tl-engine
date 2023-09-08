/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.event.convert;

import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.EventWriter;

/**
 * The class {@link AbstractMappingRewriter} rewrites an event by writing the mapping of it event.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractMappingRewriter implements EventRewriter {

	/**
	 * Writes the given {@link ChangeSet} by {@link #mapChangeSet(ChangeSet)}
	 * 
	 * @see com.top_logic.knowledge.event.convert.EventRewriter#rewrite(com.top_logic.knowledge.event.ChangeSet,
	 *      com.top_logic.knowledge.event.EventWriter)
	 */
	@Override
	public void rewrite(ChangeSet cs, EventWriter out) {
		ChangeSet changedCS = mapChangeSet(cs);
		if (changedCS != null) {
			out.write(changedCS);
		}
	}

	/**
	 * Changes the given {@link ChangeSet}.
	 * 
	 * @param input
	 *        The changeSet to adapt.
	 * 
	 * @return <code>null</code> to delete {@link ChangeSet}.
	 */
	protected abstract ChangeSet mapChangeSet(ChangeSet input);

}

