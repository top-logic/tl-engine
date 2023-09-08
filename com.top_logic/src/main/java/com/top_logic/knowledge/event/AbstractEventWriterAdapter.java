/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.event;

/**
 * {@link AbstractEventWriterAdapter} is an adapter for some {@link EventWriter}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractEventWriterAdapter implements EventWriter {

	@Override
	public void write(ChangeSet cs) {
		getImpl().write(cs);
	}

	@Override
	public void flush() {
		getImpl().flush();
	}

	@Override
	public void close() {
		getImpl().close();
	}

	/**
	 * Returns the implementation to delegate to.
	 */
	protected abstract EventWriter getImpl();

}

