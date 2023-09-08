/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.migrate.tl.util;

import com.top_logic.basic.Protocol;
import com.top_logic.knowledge.event.AbstractKnowledgeEventVisitor;
import com.top_logic.knowledge.event.BranchEvent;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.CommitEvent;
import com.top_logic.knowledge.event.EventWriter;
import com.top_logic.knowledge.event.ItemDeletion;
import com.top_logic.knowledge.event.ItemUpdate;
import com.top_logic.knowledge.event.ObjectCreation;
import com.top_logic.knowledge.event.convert.EventRewriter;

/**
 * The class {@link AbstractMigrationRewriter} is an {@link EventRewriter} which rewrites the event
 * by visiting it. By default it writes the event to the given writer.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractMigrationRewriter extends AbstractKnowledgeEventVisitor<Void, Void> implements
		EventRewriter {

	/**
	 * Return value in visit case
	 */
	protected static final Void none = null;

	/**
	 * {@link Protocol} to write messages to
	 */
	protected final Protocol _protocol;

	/**
	 * Creates a {@link AbstractMigrationRewriter}.
	 * 
	 * @param protocol
	 *        see {@link AbstractMigrationRewriter#_protocol}
	 */
	public AbstractMigrationRewriter(Protocol protocol) {
		_protocol = protocol;
	}

	@Override
	public void rewrite(ChangeSet cs, EventWriter out) {
		try {
			for (BranchEvent branchEvent : cs.getBranchEvents()) {
				branchEvent.visit(this, none);
			}
			for (ObjectCreation creation : cs.getCreations()) {
				creation.visit(this, none);
			}
			for (ItemDeletion deletion : cs.getDeletions()) {
				deletion.visit(this, none);
			}
			for (ItemUpdate update : cs.getUpdates()) {
				update.visit(this, none);
			}
			CommitEvent commit = cs.getCommit();
			if (commit != null) {
				commit.visit(this, none);
			}
		} catch (RuntimeException ex) {
			throw _protocol.fatal("Unable to process change set " + cs, ex);
		}
	}

}

