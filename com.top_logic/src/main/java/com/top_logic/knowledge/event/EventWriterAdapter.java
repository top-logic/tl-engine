/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.event;

/**
 * The class {@link EventWriterAdapter} is an {@link AbstractEventWriterAdapter} based on some fixed
 * given {@link EventWriter}
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class EventWriterAdapter extends AbstractEventWriterAdapter {

	/**
	 * Returned by {@link #getImpl()}
	 */
	private final EventWriter _impl;

	/**
	 * Creates a new {@link EventWriterAdapter}.
	 * 
	 * @param impl
	 *        the implementation to delegate to
	 */
	public EventWriterAdapter(EventWriter impl) {
		_impl = impl;
	}

	@Override
	protected final EventWriter getImpl() {
		return _impl;
	}

}

