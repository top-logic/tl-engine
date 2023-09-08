/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.rewriters;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.knowledge.event.AbstractKnowledgeEventVisitor;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.CommitEvent;
import com.top_logic.knowledge.event.EventWriter;
import com.top_logic.knowledge.event.KnowledgeEvent;
import com.top_logic.knowledge.event.convert.EventRewriter;

/**
 * {@link EventRewriter} merging all revision between given revisions (including those revisions)
 * into one revision.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class RevisionMerge extends AbstractKnowledgeEventVisitor<Object, Void> implements
		EventRewriter {

	private static final String MIGRATION_AUTHOR = "Migration";

	/**
	 * Configuration of a {@link RevisionMerge}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends PolymorphicConfiguration<RevisionMerge> {

		/**
		 * The first revision to merge (inclusive).
		 */
		long getStartRevision();

		/**
		 * Sets {@link #getStartRevision()}.
		 */
		void setStartRevision(long value);

		/**
		 * The last revision to merge (inclusive). Must be <code>&gt;= startRevision</code>
		 */
		long getStopRevision();

		/**
		 * Sets {@link #getStopRevision()}.
		 */
		void setStopRevision(long value);

	}

	private final long _startRevision;

	private final long _stopRevision;

	private List<KnowledgeEvent> _events = null;

	private List<String> _comments = null;

	/**
	 * Creates a {@link RevisionMerge} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public RevisionMerge(InstantiationContext context, Config config) {
		_startRevision = config.getStartRevision();
		_stopRevision = config.getStopRevision();
		if (_startRevision > _stopRevision) {
			StringBuilder startGreaterStopError = new StringBuilder();
			startGreaterStopError.append("Start revision ");
			startGreaterStopError.append(_startRevision);
			startGreaterStopError.append(" must not be greater than stop revision ");
			startGreaterStopError.append(_stopRevision);
			context.error(startGreaterStopError.toString());
		}
	}

	@Override
	public void rewrite(ChangeSet cs, EventWriter out) {
		long revision = cs.getRevision();
		if (_startRevision == revision) {
			_events = new ArrayList<>();
			_comments = new ArrayList<>();
		}
		if (_events != null) {
			mergeAll(cs);
			if (_stopRevision == revision) {
				writeMergedCS(out, cs);
				_events = null;
				_comments = null;
			} else {
				writeEmptyChangeSet(out, cs);
			}
		} else {
			out.write(cs);
		}
	}

	private void writeMergedCS(EventWriter out, ChangeSet orig) {
		long revision = orig.getRevision();
		ChangeSet cs = new ChangeSet(revision);
		for (KnowledgeEvent additionalEvent : _events) {
			// ensure correct event revision.
			additionalEvent.setRevision(revision);
			cs.merge(additionalEvent);
		}
		StringBuilder logMessage = new StringBuilder();
		for (String msg : _comments) {
			if (logMessage.length() > 0) {
				logMessage.append(",");
			}
			logMessage.append(msg);
		}
		cs.setCommit(newCommitEvent(orig, logMessage.toString()));
		out.write(cs);
	}

	private void writeEmptyChangeSet(EventWriter out, ChangeSet orig) {
		ChangeSet cs = new ChangeSet(orig.getRevision());
		cs.setCommit(newCommitEvent(orig, "Produced to merge revisions " + _startRevision + " to " + _stopRevision));
		out.write(cs);
	}

	private CommitEvent newCommitEvent(ChangeSet orig, String logMessage) {
		return new CommitEvent(orig.getRevision(), MIGRATION_AUTHOR, orig.getCommit().getDate(),
			logMessage);
	}

	private void mergeAll(ChangeSet cs) {
		_events.addAll(cs.getUpdates());
		_events.addAll(cs.getBranchEvents());
		_events.addAll(cs.getCreations());
		_events.addAll(cs.getDeletions());
		_comments.add(cs.getCommit().getLog());
	}

}

