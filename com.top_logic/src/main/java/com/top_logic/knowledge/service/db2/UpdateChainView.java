/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.util.NoSuchElementException;

import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.knowledge.service.UpdateChain;
import com.top_logic.knowledge.service.UpdateEvent;

/**
 * Pointer into an {@link UpdateChain}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class UpdateChainView implements UpdateChain {

	private UpdateChainLink current;
	
	public UpdateChainView(UpdateChainLink current) {
		this.current = current;
	}

	@Override
	public boolean next() {
		// Move the current pointer to the next update that has an event, if
		// there is any.
		while (true) {
			UpdateChainLink nextUpdate = current.getNextUpdate();
			if (nextUpdate == null) {
				return false;
			}
			
			current = nextUpdate;
			if (current.hasEvent()) {
				return true;
			}
		}
	}
	
	@Override
	public long getRevision() {
		checkState();
		return current.getRevision();
	}

	@Override
	public UpdateEvent getUpdateEvent() {
		checkState();
		return current.getUpdateEvent();
	}

	private void checkState() {
		if (! current.hasEvent()) {
			throw new NoSuchElementException();
		}
	}

	/**
	 * Determines the current {@link UpdateChainLink} holding {@link #getRevision()} and
	 * {@link #getUpdateEvent()}.
	 */
	@FrameworkInternal
	public UpdateChainLink current() {
		return current;
	}

}
