/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.attr.MOAttributeImpl;
import com.top_logic.dob.attr.storage.ImmutableLongStorage;
import com.top_logic.dob.attr.storage.ImmutableObjectStorage;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.knowledge.service.BasicTypes;
import com.top_logic.knowledge.service.Revision;

/**
 * Utilities for type {@link BasicTypes#REVISION_TYPE_NAME}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public final class RevisionType {

	/**
	 * Storage to access resp. store {@link Revision#getLog()}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	@FrameworkInternal
	public static class LogStorage extends ImmutableObjectStorage {

		/** Singleton {@link LogStorage} instance. */
		public static final LogStorage INSTANCE = new LogStorage();

		/**
		 * Creates a new {@link LogStorage}.
		 */
		protected LogStorage() {
			// singleton instance
		}

		@Override
		public void initCacheValue(MOAttribute attribute, DataObject item, Object[] storage, Object cacheValue) {
			((RevisionImpl) item).initLog((String) cacheValue);
		}

		@Override
		public Object getCacheValue(MOAttribute attribute, DataObject item, Object[] storage) {
			return ((Revision) item).getLog();
		}
	}

	/**
	 * Storage to access resp. store {@link Revision#getAuthor()}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	@FrameworkInternal
	public static class AuthorStorage extends ImmutableObjectStorage {

		/** Singleton {@link AuthorStorage} instance. */
		public static final AuthorStorage INSTANCE = new AuthorStorage();

		/**
		 * Creates a new {@link AuthorStorage}.
		 */
		protected AuthorStorage() {
			// singleton instance
		}

		@Override
		public void initCacheValue(MOAttribute attribute, DataObject item, Object[] storage, Object cacheValue) {
			((RevisionImpl) item).initAuthor((String) cacheValue);
		}

		@Override
		public Object getCacheValue(MOAttribute attribute, DataObject item, Object[] storage) {
			return ((Revision) item).getAuthor();
		}
	}

	/**
	 * Storage to access resp. store {@link Revision#getCommitNumber()}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	@FrameworkInternal
	public static final class RevisionStorage extends ImmutableLongStorage {

		/** Singleton {@link RevisionStorage} instance. */
		public static final RevisionStorage INSTANCE = new RevisionStorage();

		/**
		 * Creates a new {@link RevisionStorage}.
		 */
		protected RevisionStorage() {
			// singleton instance
		}

		@Override
		public void initLongValue(MOAttribute attribute, DataObject item, Object[] storage, long cacheValue) {
			((RevisionImpl) item).initCommitNumber(cacheValue);
		}

		@Override
		public long getLongValue(MOAttribute attribute, DataObject item, Object[] storage) {
			return ((Revision) item).getCommitNumber();
		}
	}

	/**
	 * Storage to access resp. store {@link Revision#getDate()}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	@FrameworkInternal
	public static class DateStorage extends ImmutableLongStorage {

		/** Singleton {@link DateStorage} instance. */
		public static final DateStorage INSTANCE = new DateStorage();

		/**
		 * Creates a new {@link DateStorage}.
		 */
		protected DateStorage() {
			// singleton instance
		}

		@Override
		public void initLongValue(MOAttribute attribute, DataObject item, Object[] storage, long cacheValue) {
			((RevisionImpl) item).initDate(cacheValue);
		}

		@Override
		public long getLongValue(MOAttribute attribute, DataObject item, Object[] storage) {
			return ((Revision) item).getDate();
		}
	}

	/**
	 * Name of the identifier attribute of a {@link Revision} object.
	 */
	public static final String REVISION_ATTRIBUTE_NAME = BasicTypes.REVISION_REV_ATTRIBUTE;

	/**
	 * Name of the log message attribute of a {@link Revision} object.
	 */
	public static final String LOG_ATTRIBUTE_NAME = "log";

	/**
	 * Name of the commit time-stamp attribute of a {@link Revision} object.
	 */
	public static final String DATE_ATTRIBUTE_NAME = BasicTypes.REVISION_DATE_ATTRIBUTE;

	/**
	 * Name of the author attribute of a {@link Revision} object.
	 */
	public static final String AUTHOR_ATTRIBUTE_NAME = "author";

	/**
	 * Returns the {@link DBAttribute} containing the revision.
	 * 
	 * @param revisionType
	 *        The type for {@link Revision} objects.
	 */
	public static final DBAttribute getRevisionAttribute(MOClass revisionType) {
		return (MOAttributeImpl) revisionType.getAttributeOrNull(RevisionType.REVISION_ATTRIBUTE_NAME);
	}

	/**
	 * Returns the {@link DBAttribute} containing the commit message of the revision.
	 * 
	 * @param revisionType
	 *        The type for {@link Revision} objects.
	 */
	public static final DBAttribute getLogAttribute(MOClass revisionType) {
		return (MOAttributeImpl) revisionType.getAttributeOrNull(RevisionType.LOG_ATTRIBUTE_NAME);
	}

	/**
	 * Returns the {@link DBAttribute} containing the date of the revision.
	 * 
	 * @param revisionType
	 *        The type for {@link Revision} objects.
	 */
	public static final DBAttribute getDateAttribute(MOClass revisionType) {
		return (MOAttributeImpl) revisionType.getAttributeOrNull(RevisionType.DATE_ATTRIBUTE_NAME);
	}

	/**
	 * Returns the {@link DBAttribute} containing the author of the revision, i.e. the committer.
	 * 
	 * @param revisionType
	 *        The type for {@link Revision} objects.
	 */
	public static final DBAttribute getAuthorAttribute(MOClass revisionType) {
		return (MOAttributeImpl) revisionType.getAttributeOrNull(RevisionType.AUTHOR_ATTRIBUTE_NAME);
	}

}
