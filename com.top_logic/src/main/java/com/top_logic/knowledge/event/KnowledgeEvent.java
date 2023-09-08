/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.event;

import java.util.Comparator;

/**
 * Base class for a hierarchy of events that can represent the complete history
 * of an application.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class KnowledgeEvent {

	private long revision;

	/**
	 * Creates a {@link KnowledgeEvent}.
	 * 
	 * @param revision
	 *        See {@link #getRevision()}
	 */
	public KnowledgeEvent(long revision) {
		this.revision = revision;
	}
	
	/**
	 * The revision that describes when this event happened.
	 */
	public final long getRevision() {
		return revision;
	}
	
	/**
	 * @see #getRevision()
	 */
	public void setRevision(long revision) {
		this.revision = revision;
	}

	/**
	 * The {@link EventKind} of this event. 
	 */
	public abstract EventKind getKind();

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		
		if (! (other instanceof KnowledgeEvent)) {
			return false;
		}
		
		KnowledgeEvent otherEvent = ((KnowledgeEvent) other);
		return equalsKnowledgeEvent(otherEvent);
	}

	protected final boolean equalsKnowledgeEvent(KnowledgeEvent otherEvent) {
		return this.revision == otherEvent.revision;
	}
	
	@Override
	public int hashCode() {
		return (int) this.revision;
	}

	@Override
	public final String toString() {
		StringBuilder result = new StringBuilder();
		result.append(getClass().getSimpleName());
		result.append('(');
		appendProperties(result);
		result.append(')');
		return result.toString();
	}

	/**
	 * Appends all properties of this object to the given {@link StringBuilder}
	 * for {@link #toString()}.
	 */
	protected void appendProperties(StringBuilder result) {
		result.append("rev: ");
		result.append(this.revision);
	}

	/**
	 * Visit interface for the {@link KnowledgeEvent} hierarchy.
	 * 
	 * @param v
	 *        The concrete visitor.
	 * @param arg
	 *        The argument passed to the visit.
	 * @return The value computed by the given visitor.
	 */
	public abstract <R,A> R visit(KnowledgeEventVisitor<R,A> v, A arg);

	/**
	 * {@link Comparator} that orders {@link KnowledgeEvent} by their
	 * {@link KnowledgeEvent#getRevision()} and {@link KnowledgeEvent#getKind()}.
	 * 
	 * <p>
	 * Events of the same revision are ordered by the index order of
	 * {@link EventKind}.
	 * </p>
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public final static class RevisionOrder implements Comparator<KnowledgeEvent> {

		/**
		 * Singleton {@link RevisionOrder} instance.
		 */
		public static final RevisionOrder ASCENDING_INSTANCE = new RevisionOrder(true);
		public static final RevisionOrder DESCENDING_INSTANCE = new RevisionOrder(false);
		
		/** whether lower revision shall be smaller than higher revisions */
		private final boolean ascending;

		private RevisionOrder(boolean ascending) {
			this.ascending = ascending;
			// Singleton constructor.
		}
		
		@Override
		public int compare(KnowledgeEvent e1, KnowledgeEvent e2) {
			long e1Rev = e1.getRevision();
			long e2Rev = e2.getRevision();
			if (e1Rev > e2Rev) {
				return ascending ? 1 : -1;
			}
			else if (e1Rev < e2Rev) {
				return ascending ? -1 : 1;
			} else {
				return e1.getKind().ordinal() - e2.getKind().ordinal();
			}
		}
		
	}
	
}
