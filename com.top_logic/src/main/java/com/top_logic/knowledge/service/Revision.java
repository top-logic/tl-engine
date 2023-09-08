/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service;

import java.util.Date;
import java.util.Set;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.compare.AbstractAuthorAccessor;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.core.Author;
import com.top_logic.model.core.TlCoreFactory;
import com.top_logic.model.core.generated.RevisionBase;
import com.top_logic.util.TLContext;


/**
 * Representation of a revision in a versioned {@link KnowledgeBase}.
 * 
 * <p>
 * A revision (except {@link #CURRENT}) is an immutable consistent state of the
 * knowledge base. Such revisions represent the state of the knowledge base
 * directly after a commit operation. The state represented by a
 * {@link Revision} can be made mutable by
 * {@link HistoryManager#createBranch(Branch, Revision, Set) creating} a
 * {@link Branch} based on that revision.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface Revision extends RevisionBase, Comparable<Revision> {

	/**
	 * The first commit number that is created during setup.
	 */
	public static long FIRST_REV = 1;
	
	/**
	 * Internal representative for the {@link #CURRENT} {@link Revision} as
	 * commit number, where a <code>long</code> argument is expected instead of
	 * a {@link Long}.
	 */
	public static long CURRENT_REV = Long.MAX_VALUE;
	
	/**
	 * The pseudo commit number that stores the current state of all objects.
	 */
	public static final Long CURRENT_REV_WRAPPER = Long.valueOf(Revision.CURRENT_REV);

	/**
	 * Pseudo {@link Revision} that represents the state of the system before
	 * setup.
	 */
	Revision INITIAL = new TransientRevision() {

		/**
		 * Pseudo commit number that represents the state of the system before
		 * setup.
		 */
		private long INITIAL_REV = 0;

		@Override
		public String getAuthor() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Author resolveAuthor() {
			return null;
		}

		@Override
		public long getDate() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Date resolveDate() {
			return null;
		}

		@Override
		public String getLog() {
			return StringServices.EMPTY_STRING;
		}

		@Override
		public long getCommitNumber() {
			return INITIAL_REV;
		}
		
		@Override
		public boolean isCurrent() {
			return false;
		}
		
        /** "INITIAL"  */
        @Override
        public String toString() {
            return "INITIAL";
        }
		
	}; 
	
	/**
	 * Placeholder for the current not yet committed revision.
	 */
	@Label("The current revision")
	Revision CURRENT = new TransientRevision() {

		@Override
		public String getAuthor() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Author resolveAuthor() {
			return null;
		}

		@Override
		public long getDate() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Date resolveDate() {
			return null;
		}

		@Override
		public String getLog() {
			return StringServices.EMPTY_STRING;
		}

		@Override
		public long getCommitNumber() {
			return CURRENT_REV;
		}
		
		@Override
		public boolean isCurrent() {
			return true;
		}
		
	    /** "CURRENT"  */
        @Override
        public String toString() {
            return "CURRENT";
        }

	}; 

	/**
	 * Whether this is a placeholder for the {@link #CURRENT} not yet created
	 * revision.
	 * 
	 * <p>
	 * The current revision does not support the {@link #getAuthor()},
	 * {@link #getLog()}, and {@link #getDate()} methods.
	 * </p>
	 */
	public boolean isCurrent();
	
	/**
	 * The commit number that identifies this revision.
	 * 
	 * @return The commit number of this revision or {@link Revision#CURRENT_REV} for the
	 *         {@link #CURRENT} revision.
	 */
	public long getCommitNumber();

	/**
	 * The {@link TLContext#getContextId()} of the committer of this revision.
	 * 
	 * <p>
	 * It is illegal to call this method for the {@link #CURRENT} or {@link #INITIAL} revision.
	 * </p>
	 */
	public String getAuthor();

	/**
	 * Resolves the {@link Person}, that committed this {@link Revision}. If the {@link Revision}
	 * was not created by a {@link Person} (e.g. a change by the system), then <code>null</code> is
	 * returned.
	 * 
	 * @return The {@link Person} that has committed this {@link Revision} or <code>null</code>.
	 */
	default Author resolveAuthor() {
		return AbstractAuthorAccessor.resolvePerson(getAuthor());
	}

	/**
	 * A log message that was created for the commit of this revision. 
	 */
	public String getLog();

	/**
	 * The date and time when this revision was created.
	 */
	public long getDate();
	
	/**
	 * Resolves {@link #getDate()} to a {@link Date} object.
	 */
	default Date resolveDate() {
		return new Date(getDate());
	}

	@Override
	default Object tValue(TLStructuredTypePart part) {
		switch (part.getName()) {
			case REVISION_ATTR:
				return getCommitNumber();
			case DATE_ATTR:
				return resolveDate();
			case AUTHOR_ATTR:
				return resolveAuthor();
			case LOG_ATTR:
				return getLog();
			case T_TYPE_ATTR:
				return tType();
			default:
				throw new IllegalArgumentException("Unknown part: " + part);
		}
	}

	@Override
	default TLStructuredType tType() {
		return TlCoreFactory.getRevisionType();
	}

	/**
	 * Compares this {@link Revision} to the other by comparing {@link #getCommitNumber()}.
	 * 
	 * <p>
	 * Note: this class has a natural ordering that is inconsistent with equals.
	 * </p>
	 * 
	 * @return
	 *         <ul>
	 *         <li><code>-1</code> when commit number of this revision is less than the revision of
	 *         the other revision.</li>
	 *         <li><code>0</code> when commit number of this revision is equal to the revision of
	 *         the other revision.</li>
	 *         <li><code>1</code> when commit number of this revision is larger than the revision of
	 *         the other revision.</li>
	 *         </ul>
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	default int compareTo(Revision other) {
		long commitNo = this.getCommitNumber();
		long otherCommitNo = other.getCommitNumber();
		if (commitNo < otherCommitNo) {
			return -1;
		}
		if (commitNo == otherCommitNo) {
			return 0;
		}
		return 1;
	}

	/**
	 * Base class for transient revisions.
	 * 
	 * @see Revision#INITIAL
	 * @see Revision#CURRENT
	 */
	abstract class TransientRevision implements Revision {
		@Override
		public KnowledgeItem tHandle() {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean tValid() {
			return true;
		}

		@Override
		public Object tSetData(String property, Object value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public ObjectKey tId() {
			return null;
		}
	}

}