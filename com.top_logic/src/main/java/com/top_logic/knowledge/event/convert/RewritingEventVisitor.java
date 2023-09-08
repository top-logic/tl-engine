/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.event.convert;

import java.util.Iterator;

import com.top_logic.knowledge.event.AbstractKnowledgeEventVisitor;
import com.top_logic.knowledge.event.BranchEvent;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.CommitEvent;
import com.top_logic.knowledge.event.EventWriter;
import com.top_logic.knowledge.event.ItemDeletion;
import com.top_logic.knowledge.event.ItemUpdate;
import com.top_logic.knowledge.event.KnowledgeEvent;
import com.top_logic.knowledge.event.ObjectCreation;

/**
 * {@link EventRewriter} which simply writes the event to the given {@link EventWriter}. Before the
 * {@link ChangeSet} is written, all contained events are visited.
 * 
 * @see SimpleEventRewriter
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class RewritingEventVisitor extends AbstractKnowledgeEventVisitor<Object, Void> implements
		EventRewriter {

	/** Value to return during visit of event to indicate that the event must be skipped. */
	protected static final Object SKIP_EVENT = Boolean.TRUE;

	/** Value to return during visit of event to indicate that the event must be used. */
	protected static final Object APPLY_EVENT = Boolean.FALSE;

	/** Visit argument. */
	protected static Void none = null;

	@Override
	public void rewrite(ChangeSet cs, EventWriter out) {
		processEvents(cs);
		out.write(cs);
	}

	/**
	 * Processes the events in the given {@link ChangeSet}.
	 * 
	 * @param cs
	 *        The {@link ChangeSet} to rewrite.
	 */
	protected void processEvents(ChangeSet cs) {
		processBranches(cs);

		// Note: The order is important: Objects must first be deleted, before new object with the
		// same key values can be re-created. Updating an object is only possible, if it is first
		// created. The migration may produce multiple changes for the same object in in one
		// revision!
		processDeletions(cs);
		processCreations(cs);
		processUpdates(cs);

		processCommit(cs);
	}

	/**
	 * Processes the {@link ChangeSet#getBranchEvents() branch events} of the rewritten
	 * {@link ChangeSet}.
	 */
	protected void processBranches(ChangeSet cs) {
		Iterator<BranchEvent> branches = cs.getBranchEvents().iterator();
		while (branches.hasNext()) {
			BranchEvent event = branches.next();
			Object applyEvent = event.visit(this, none);
			if (applyEvent != APPLY_EVENT) {
				branches.remove();
			}
		}
	}

	/**
	 * Processes the {@link ChangeSet#getCommit() commit} of the rewritten {@link ChangeSet}.
	 */
	protected void processCommit(ChangeSet cs) {
		CommitEvent commit = cs.getCommit();
		if (commit != null) {
			Object applyEvent = commit.visit(this, none);
			if (applyEvent != APPLY_EVENT) {
				cs.setCommit(null);
			}
		}
	}

	/**
	 * Processes the {@link ChangeSet#getUpdates() updates} of the rewritten {@link ChangeSet}.
	 */
	protected void processUpdates(ChangeSet cs) {
		Iterator<ItemUpdate> updates = cs.getUpdates().iterator();
		while (updates.hasNext()) {
			ItemUpdate update = updates.next();
			Object applyEvent = update.visit(this, none);
			if (applyEvent != APPLY_EVENT || isEmpty(update)) {
				updates.remove();
			}
		}
	}

	private boolean isEmpty(ItemUpdate update) {
		return update.getValues().isEmpty();
	}

	/**
	 * Processes the {@link ChangeSet#getDeletions() deletions} of the rewritten {@link ChangeSet}.
	 */
	protected void processDeletions(ChangeSet cs) {
		Iterator<ItemDeletion> deletions = cs.getDeletions().iterator();
		while (deletions.hasNext()) {
			Object applyEvent = deletions.next().visit(this, none);
			if (applyEvent != APPLY_EVENT) {
				deletions.remove();
			}
		}
	}

	/**
	 * Processes the {@link ChangeSet#getCreations() creations} of the rewritten {@link ChangeSet}.
	 */
	protected void processCreations(ChangeSet cs) {
		Iterator<ObjectCreation> creations = cs.getCreations().iterator();
		while (creations.hasNext()) {
			Object applyEvent = creations.next().visit(this, none);
			if (applyEvent != APPLY_EVENT) {
				creations.remove();
			}
		}
	}

	@Override
	protected Object visitKnowledgeEvent(KnowledgeEvent event, Void arg) {
		return APPLY_EVENT;
	}

}

