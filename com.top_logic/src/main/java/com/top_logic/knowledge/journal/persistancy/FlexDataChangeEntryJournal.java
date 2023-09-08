/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.journal.persistancy;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringID;
import com.top_logic.basic.TLID;
import com.top_logic.basic.sql.CommitContext;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.attr.NextCommitNumberFuture;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.identifier.DefaultObjectKey;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MOClassImpl;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.dob.simple.GenericDataObject;
import com.top_logic.knowledge.journal.ChangeJournalAttributeEntry;
import com.top_logic.knowledge.journal.ChangeJournalResultAttributeEntry;
import com.top_logic.knowledge.journal.JournalAttributeEntry;
import com.top_logic.knowledge.journal.JournalResultAttributeEntry;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.FlexDataManager;
import com.top_logic.knowledge.service.FlexDataManagerFactory;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.db2.FlexData;
import com.top_logic.knowledge.service.db2.MOKnowledgeItemImpl;
import com.top_logic.knowledge.service.db2.NoFlexData;
import com.top_logic.util.TLContext;

/**
 * A {@link JournalPersistancyHandler} that stores the journal into two separate
 * {@link FlexDataManager}s.
 * 
 * @author <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class FlexDataChangeEntryJournal implements JournalPersistancyHandler {

	private static final MetaObject CHANGE_TYPE = new MOClassImpl("Entry");

	public static final String JOURNAL_PRE_VALUE_TYPE_NAME = "JournalPreValue";

	public static final String JOURNAL_POST_VALUE_TYPE_NAME = "JournalPostValue";

	/** Data table of values after a change */
	private final FlexDataManager postValueData;

	/** Data table of values before a change */
	private final FlexDataManager preValueData;

	public FlexDataChangeEntryJournal(KnowledgeBase kb) {
		ConnectionPool pool = ConnectionPoolRegistry.getDefaultConnectionPool();
		MORepository moRepository = kb.getMORepository();
		FlexDataManagerFactory factory = FlexDataManagerFactory.getInstance();

		try {
			MOKnowledgeItemImpl preValueType =
				(MOKnowledgeItemImpl) moRepository.getMetaObject(JOURNAL_PRE_VALUE_TYPE_NAME);
			preValueData = factory.newFlexDataManager(pool, preValueType);

			MOKnowledgeItemImpl postValueType =
				(MOKnowledgeItemImpl) moRepository.getMetaObject(JOURNAL_POST_VALUE_TYPE_NAME);
			postValueData = factory.newFlexDataManager(pool, postValueType);
		} catch (UnknownTypeException ex) {
			throw (AssertionError) new AssertionError(
				"Journal manager types not installed in the persistency layer.").initCause(ex);
		}
	}

	@Override
	public Collection<JournalResultAttributeEntry>/* <ChangeJournalResultAttributeEntry> */getJournal(long aCommitId, TLID anIdentifier,
			PooledConnection connection, DBHelper sqlDialect)
			throws SQLException {

		ObjectKey key = createKey(aCommitId, anIdentifier);
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		FlexData preValues = preValueData.load(kb, key, false);
		FlexData postValues = postValueData.load(kb, key, false);

		if (preValues == NoFlexData.INSTANCE && postValues == NoFlexData.INSTANCE) {
			return Collections.emptyList();
		}
		
		Set<String> allAttributes = new HashSet<>();
		allAttributes.addAll(preValues.getAttributes());
		allAttributes.addAll(postValues.getAttributes());
		
		List<JournalResultAttributeEntry> result = new ArrayList<>(allAttributes.size());
		for (String attribute : allAttributes) {
			result.add(new ChangeJournalResultAttributeImpl(attribute, preValues, postValues));
		}
		return result;
	}

	@Override
	public void journal(final long aCommitId, TLID anIdentifier, Collection<? extends JournalAttributeEntry> entries,
			final PooledConnection connection)
			throws SQLException {

		ObjectKey key = createKey(aCommitId, anIdentifier);
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		CommitContext context = new CommitContext() {

			@Override
			public boolean transactionStarted() {
				return true;
			}

			@Override
			public PooledConnection getConnection() {
				return connection;
			}

			@Override
			public long getCommitNumber() {
				return aCommitId;
			}

			@Override
			public TLID createID() {
				throw new UnsupportedOperationException();
			}
		};

		FlexData preValues = preValueData.load(kb, key, true);
		FlexData postValues = postValueData.load(kb, key, true);

		try {
			for (JournalAttributeEntry entry : entries) {
				ChangeJournalAttributeEntry changeEntry = (ChangeJournalAttributeEntry) entry;
				preValues.setAttributeValue(entry.getName(), wrapObject(changeEntry.getPreValue()));
				Object postValue = changeEntry.getPostValue();
				if (postValue == NextCommitNumberFuture.INSTANCE) {
					// Note: This happens if document changes are written to the journal.
					postValue = aCommitId;
				}
				postValues.setAttributeValue(entry.getName(), wrapObject(postValue));
			}

			preValueData.store(key, preValues, context);
			postValueData.store(key, postValues, context);
		} catch (DataObjectException e) {
			Logger.error("Failed to store journal entries", e, FlexDataChangeEntryJournal.class);
		}
	}

	/**
	 * Create an empty {@link KnowledgeObject} that holds the id, represented by commitId and id of
	 * the original changed object
	 */
	private ObjectKey createKey(long commitId, TLID id) {
		TLID valueOf = StringID.valueOf(commitId + "-" + id);
		return new DefaultObjectKey(TLContext.TRUNK_ID, Revision.CURRENT.getCommitNumber(), CHANGE_TYPE, valueOf);
	}

	/**
	 * Mask reference values
	 */
	private Object wrapObject(Object value) {
		if (value instanceof DataObject) {
			// It's copied from implementation of ChangeEntryJournalPersistancyHandler#store
			String strangeValue;
			if (value instanceof GenericDataObject) {
				GenericDataObject data = (GenericDataObject) value;
				strangeValue = data.getMetaObjectName() + ' ' + data.getIdentifier();

			} else {
				DataObject data = (DataObject) value;
				strangeValue = data.tTable().getName() + ' ' + data.getIdentifier();
			}
			return strangeValue;
		}
		return value;
	}

	private static class ChangeJournalResultAttributeImpl implements ChangeJournalResultAttributeEntry {

		private final String name;

		private final FlexData preValues;

		private final FlexData postValues;

		public ChangeJournalResultAttributeImpl(String name, FlexData preValues, FlexData postValues) {
			this.name = name;
			this.preValues = preValues;
			this.postValues = postValues;
		}

		@Override
		public String getName() {
			return this.name;
		}

		@Override
		public Object getPost() {
			return this.postValues.getAttributeValue(this.name);
		}

		@Override
		public Object getPre() {
			return this.preValues.getAttributeValue(this.name);
		}

		@Override
		public String getType() {
			return ChangeJournalResultAttributeImpl.class.toString();
		}
	}
}
