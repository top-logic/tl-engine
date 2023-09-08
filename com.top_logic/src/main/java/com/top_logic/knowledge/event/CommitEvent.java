/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.event;

import com.top_logic.basic.config.XmlDateTimeFormat;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.util.Utils;


/**
 * {@link KnowledgeEvent} that represents a commit.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CommitEvent extends KnowledgeEvent {

	private String author;
	private String logMessage;
	private long commitTimeMillis;

	/**
	 * Creates a new {@link CommitEvent}
	 * 
	 * @param revision
	 *        See {@link #getRevision()}
	 * @param author
	 *        See {@link #getAuthor()}
	 * @param commitTimeMillis
	 *        See {@link #getDate()}
	 * @param logMessage
	 *        See {@link #getLog()}
	 */
	public CommitEvent(long revision, String author, long commitTimeMillis, String logMessage) {
		super(revision);
		
		this.author = author;
		this.commitTimeMillis = commitTimeMillis;
		this.logMessage = logMessage;
	}

	@Override
	public EventKind getKind() {
		return EventKind.commit;
	}

	/**
	 * The committer of this revision.
	 */
	public String getAuthor() {
		return this.author;
	}
	
	/**
	 * @see #getAuthor()
	 */
	public void setAuthor(String author) {
		this.author = author;
	}

	/**
	 * A log message that was created for the commit of this revision. 
	 */
	public String getLog() {
		return this.logMessage;
	}
	
	/**
	 * @see #getLog()
	 */
	public void setLog(String logMessage) {
		this.logMessage = logMessage;
	}

	/**
	 * The date and time when this revision was created.
	 */
	public long getDate() {
		return this.commitTimeMillis;
	}
	
	/**
	 * @see #getDate()
	 */
	public void setDate(long commitTimeMillis) {
		this.commitTimeMillis = commitTimeMillis;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		
		if (! (other instanceof CommitEvent)) {
			return false;
		}
		
		CommitEvent otherEvent = ((CommitEvent) other);
		return equalsCommitEvent(otherEvent);
	}

	protected final boolean equalsCommitEvent(CommitEvent otherEvent) {
		return super.equalsKnowledgeEvent(otherEvent) && 
			this.commitTimeMillis == otherEvent.commitTimeMillis &&
			Utils.equals(this.author, otherEvent.author) &&
			Utils.equals(this.logMessage, otherEvent.logMessage);
	}
	
	@Override
	public int hashCode() {
		// No further information required for hashing.
		return super.hashCode();
	}
	
	@Override
	protected void appendProperties(StringBuilder result) {
		super.appendProperties(result);
		result.append(", author: ");
		result.append(author);
		result.append(", date: ");
		result.append(XmlDateTimeFormat.formatTimeStamp(this.commitTimeMillis));
		result.append(", log: '");
		result.append(logMessage);
		result.append('\'');
	}

	@Override
	public <R,A> R visit(KnowledgeEventVisitor<R,A> v, A arg) {
		return v.visitCommit(this, arg);
	}

	/**
	 * Creates a {@link CommitEvent} for the given {@link Revision}.
	 * 
	 * @param revision
	 *        The revision to create a {@link CommitEvent} for.
	 * @return A {@link CommitEvent} that represents the given {@link Revision}.
	 */
	public static CommitEvent newCommitEvent(Revision revision) {
		return new CommitEvent(revision.getCommitNumber(), revision.getAuthor(), revision.getDate(), revision.getLog());
	}

}
