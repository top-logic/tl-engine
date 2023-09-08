/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.event.convert;

import com.top_logic.basic.col.Filter;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.EventWriter;

/**
 * The class {@link FilteredEventRewriter} is a wrapper for some {@link EventWriter} which applies
 * the rewriter in case a filter matches the event. In other case the event is directly written to
 * the {@link EventWriter}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class FilteredEventRewriter implements EventRewriter {

	private final Filter<? super ChangeSet> _filter;

	private final EventRewriter _rewriter;

	/**
	 * Creates a new {@link FilteredEventRewriter}.
	 * 
	 * @param filter
	 *        the filter to decide whether the {@link ChangeSet} must be rewritten.
	 * @param rewriter
	 *        the rewriter to apply
	 */
	public FilteredEventRewriter(Filter<? super ChangeSet> filter, EventRewriter rewriter) {
		_filter = filter;
		_rewriter = rewriter;
	}

	@Override
	public void rewrite(ChangeSet cs, EventWriter out) {
		if (_filter.accept(cs)) {
			_rewriter.rewrite(cs, out);
		} else {
			out.write(cs);
		}
	}

}

