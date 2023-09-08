/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.event;

/**
 * {@link ChangeSetReader} without any result.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public final class EmptyChangeSetReader implements ChangeSetReader {

	/** Singleton instance of {@link EmptyChangeSetReader}. */
	public static final EmptyChangeSetReader INSTANCE = new EmptyChangeSetReader();

	private EmptyChangeSetReader() {
		// singleton instance
	}

	@Override
	public ChangeSet read() {
		// No ChangeSets here
		return null;
	}

	@Override
	public void close() {
		// nothing to release
	}

}

