/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.event;


/**
 * {@link EventReader} that reports no events at all.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class EmptyEventReader<E> extends AbstractEventReader<E> {
	
	private static final EmptyEventReader INSTANCE = new EmptyEventReader();

	private EmptyEventReader() {
		// Singleton constructor.
	}
	
	/**
	 * Type save access to the singleton instance of this class.
	 */
	public static <E> EventReader<E> getInstance() {
		return INSTANCE;
	}
	
	@Override
	public E readEvent() {
		// No events.
		return null;
	}

	@Override
	public void close() {
		// No resources.
	}
}