/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.util.ResourceCleaner;
import com.top_logic.knowledge.service.UpdateEvent;


/**
 * A single link of a chain of {@link UpdateEvent}s in the {@link DBKnowledgeBase}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class UpdateChainLink {

	private List<CleanupOldValues> _cleanupActions;

	private final long revision;
	private final UpdateEvent changeEvent;
	
	private UpdateChainLink nextUpdate;

	public UpdateChainLink(long revision) {
		this(revision, null);
	}
	
	public UpdateChainLink(UpdateEvent changeEvent) {
		this(changeEvent.getCommitNumber(), changeEvent);
	}

	private UpdateChainLink(long revision, UpdateEvent changeEvent) {
		this.revision = revision;
		this.changeEvent = changeEvent;
	}
	
	public synchronized UpdateChainLink getNextUpdate() {
		return nextUpdate;
	}

	/** @see #getNextUpdate() */
	@FrameworkInternal
	public synchronized void setNextUpdate(UpdateChainLink update) {
		assert this.nextUpdate == null : "Chain may not be changed.";
		this.nextUpdate = update;
	}
	
	public UpdateEvent getUpdateEvent() {
		return changeEvent;
	}
	
	public long getRevision() {
		return revision;
	}

	public boolean hasEvent() {
		return changeEvent != null;
	}

	/**
	 * Registers the given cleanup action to clean up old data when this {@link UpdateChainLink} is
	 * not longer needed.
	 */
	void registerForCleanup(CleanupOldValues cleanupAction) {
		synchronized (this) {
			if (_cleanupActions == null) {
				List<CleanupOldValues> cleanupActions = new ArrayList<>();
				_cleanupActions = cleanupActions;
				ResourceCleaner.register(this, () -> cleanup(cleanupActions));
			}
			_cleanupActions.add(cleanupAction);
		}
		
	}

	private static void cleanup(List<CleanupOldValues> cleanupActions) {
		for (CleanupOldValues action : cleanupActions) {
			action.cleanup();
		}
	}

}
