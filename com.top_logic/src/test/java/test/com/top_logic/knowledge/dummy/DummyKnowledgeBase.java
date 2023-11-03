/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.dummy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import com.top_logic.basic.Protocol;
import com.top_logic.basic.StringID;
import com.top_logic.basic.TLID;
import com.top_logic.basic.col.CloseableIterator;
import com.top_logic.basic.col.EmptyClosableIterator;
import com.top_logic.basic.col.LongRange;
import com.top_logic.basic.db.schema.setup.SchemaSetup;
import com.top_logic.basic.message.Message;
import com.top_logic.basic.util.ComputationEx2;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.knowledge.event.ChangeSetReader;
import com.top_logic.knowledge.event.EmptyChangeSetReader;
import com.top_logic.knowledge.event.EventWriter;
import com.top_logic.knowledge.event.ObjectCreation;
import com.top_logic.knowledge.objects.InvalidLinkException;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.search.CompiledQuery;
import com.top_logic.knowledge.search.EmptyCompiledQuery;
import com.top_logic.knowledge.search.HistoryQuery;
import com.top_logic.knowledge.search.HistoryQueryArguments;
import com.top_logic.knowledge.search.RevisionQuery;
import com.top_logic.knowledge.search.RevisionQueryArguments;
import com.top_logic.knowledge.service.AbstractKnowledgeBase;
import com.top_logic.knowledge.service.Branch;
import com.top_logic.knowledge.service.CompiledQueryCache;
import com.top_logic.knowledge.service.HistoryManager;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseConfiguration;
import com.top_logic.knowledge.service.NoHistoryManager;
import com.top_logic.knowledge.service.NoTransaction;
import com.top_logic.knowledge.service.ReaderConfig;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.UpdateChain;
import com.top_logic.knowledge.service.UpdateEvent;
import com.top_logic.knowledge.service.UpdateListener;
import com.top_logic.knowledge.service.db2.AbstractAssociationQuery;
import com.top_logic.knowledge.service.db2.ConcurrentCompiledQueryCache;
import com.top_logic.knowledge.service.db2.SimpleQuery;
import com.top_logic.knowledge.service.event.CommitChecker;
import com.top_logic.knowledge.service.event.ModificationListener;
import com.top_logic.model.TLObject;

/**
 * The DummyKnowledgeBase a dummy knowledge base for testing.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class DummyKnowledgeBase extends AbstractKnowledgeBase {

	/**
	 * Singleton instance of {@link DummyKnowledgeBase}.
	 */
    public static final KnowledgeBase INSTANCE = new DummyKnowledgeBase();
    
	private static final UpdateChain NO_UPDATES = new UpdateChain() {
		@Override
		public boolean next() {
			return false;
		}

		@Override
		public long getRevision() {
			throw new NoSuchElementException();
		}

		@Override
		public UpdateEvent getUpdateEvent() {
			throw new NoSuchElementException();
		}
	};

	private HistoryManager historyManager = new NoHistoryManager(this);

	private final CompiledQueryCache _queryCache = new ConcurrentCompiledQueryCache();

	@Override
	public void addModificationListener(ModificationListener listener) {
		// Ignore
	}

	@Override
	public void removeModificationListener(ModificationListener listener) {
		// Ignore
	}

	@Override
	public boolean addUpdateListener(UpdateListener listener) {
		return true;
	}
	
	@Override
	public boolean removeUpdateListener(UpdateListener listener) {
		return true;
	}
	
	@Override
	public UpdateChain getUpdateChain() {
		return NO_UPDATES;
	}

	@Override
	public UpdateChain getSessionUpdateChain() {
		return getUpdateChain();
	}

	@Override
	public final Transaction beginTransaction() {
		return beginTransaction(null);
	}
	
	@Override
	public Transaction beginTransaction(Message commitMessage) {
		return new NoTransaction(this);
	}
	
	/**
     * TODO #2829: Delete TL 6 deprecation 
     * @deprecated Use {@link #beginTransaction(Message)}.
     */
    @Override
	@Deprecated
    public boolean begin() {
        return false;
    }

    /**
     * TODO #2829: Delete TL 6 deprecation 
     * @deprecated Use {@link #beginTransaction(Message)}.
     */
    @Override
	@Deprecated
    public boolean commit() {
        return true;
    }

    @Override
	public KnowledgeAssociation createAssociation(Branch branch, TLID aId, KnowledgeItem aSrcKO,
			KnowledgeItem aDestKO, String aTypeName) throws DataObjectException {
        return null;
    }

	@Override
	public KnowledgeItem createKnowledgeItem(Branch branch, TLID objectName, String typeName,
			Iterable<Entry<String, Object>> initialValues) throws DataObjectException {
		return null;
	}
	
	@Override
	public <T extends KnowledgeItem> List<T> createObjects(Iterable<ObjectCreation> creations, Class<T> expectedType)
			throws DataObjectException {
		return new ArrayList<>();
	}

    @Override
	public void delete(KnowledgeItem item) throws DataObjectException {
    	// Ignore.
    }

	@Override
	public void deleteAll(Collection<? extends KnowledgeItem> items) throws DataObjectException {
		// Ignore.
	}

    @Override
	public Collection getAllKnowledgeAssociations() {
        return null;
    }

    @Override
	public Collection getAllKnowledgeObjects() {
        return null;
    }

    @Override
	public Collection getAllKnowledgeObjects(String aType) {
        return null;
    }

    @Override
	public Collection<KnowledgeAssociation> getAllKnowledgeAssociations(String aType) {
    	return null;
    }
    
    @Override
	public KnowledgeAssociation getKnowledgeAssociation(String aType, TLID objectName) {
    	return null;
    }
    
    @Override
	public KnowledgeObject getKnowledgeObject(String aType, TLID objectName) {
    	return null;
    }
    
    @Override
	public MORepository getMORepository() {
        return null;
    }

    @Override
	public String getName() {
        return null;
    }

    @Override
	public DataObject getObjectByAttribute(String aType, String attrName, Object aValue) {
        return null;
    }

    @Override
	public Iterator<KnowledgeItem> getObjectsByAttribute(String aType, String attrName, Object aValue) throws UnknownTypeException {
        return null;
    }

    @Override
	public Iterator<KnowledgeItem> getObjectsByAttribute(String type, String[] aAttrNames,
            Object[] aValues) throws UnknownTypeException,
            NoSuchAttributeException {
        return null;
    }

    @Override
	public void initialize(Protocol protocol, KnowledgeBaseConfiguration config) {
    	// Ignore.
    }
    
    @Override
	public void startup(Protocol protocol) {
    	//  Ignored.
    }

	@Override
	public void shutDown() {
		// Ignored.
	}

    /**
     * TODO #2829: Delete TL 6 deprecation 
     * @deprecated Use {@link #beginTransaction(Message)}.
     */
    @Override
	@Deprecated
    public boolean rollback() {
        return false;
    }

	@Override
	public HistoryManager getHistoryManager() {
		return historyManager;
	}

	@Override
	public int refetch() {
		return 0;
	}
    
	@Override
	public KnowledgeItem resolveObjectKey(ObjectKey key) {
		return getKnowledgeObject(key.getObjectType().getName(), key.getObjectName());
	}

	@Override
	public KnowledgeItem resolveCachedObjectKey(ObjectKey key) {
		return getKnowledgeObject(key.getObjectType().getName(), key.getObjectName());
	}

	@Override
	public ChangeSetReader getChangeSetReader(ReaderConfig readerConfig) {
		return EmptyChangeSetReader.INSTANCE;
	}
	
	@Override
	public EventWriter getReplayWriter() {
		throw new UnsupportedOperationException();
	}

	@Override
	public ChangeSetReader getDiffReader(Revision startRev, Branch startBranch, Revision stopRev, Branch stopBranch, boolean detailed) {
		return EmptyChangeSetReader.INSTANCE;
	}

	@Override
	public Map<?, List<LongRange>> search(HistoryQuery query) {
		return new HashMap<>();
	}

	@Override
	public Map<?, List<LongRange>> search(HistoryQuery query, HistoryQueryArguments queryArguments) {
		return new HashMap<>();
	}

	@Override
	public <E> List<E> search(RevisionQuery<E> query) {
		return new ArrayList<>();
	}

	@Override
	public <E> List<E> search(RevisionQuery<E> query, RevisionQueryArguments queryArguments) {
		return new ArrayList<>();
	}

	@Override
	public <E> CloseableIterator<E> searchStream(RevisionQuery<E> query) {
		return EmptyClosableIterator.getInstance();
	}

	@Override
	public <E> CloseableIterator<E> searchStream(RevisionQuery<E> query, RevisionQueryArguments queryArguments) {
		return EmptyClosableIterator.getInstance();
	}

	@Override
	public <E> CompiledQuery<E> compileQuery(RevisionQuery<E> query) {
		return EmptyCompiledQuery.getInstance();
	}

	@Override
	public <E> CompiledQuery<E> compileSimpleQuery(SimpleQuery<E> simpleQuery) {
		return EmptyCompiledQuery.getInstance();
	}

	@Override
	public CompiledQueryCache getQueryCache() {
		return _queryCache;
	}

	@Override
	public <T extends TLObject, C> C resolveLinks(KnowledgeObject aKO, AbstractAssociationQuery<T, C> query) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T extends TLObject> void fillCaches(Iterable<KnowledgeObject> items, AbstractAssociationQuery<T, ?> query)
			throws InvalidLinkException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addCommitChecker(CommitChecker checker) {
		// Ignore
	}

	@Override
	public void removeCommitChecker(CommitChecker checker) {
		// Ignore
	}
	
	@Override
	public TLID createID() {
		return StringID.createRandomID();
	}

	@Override
	public <T, E1 extends Throwable, E2 extends Throwable> T withoutModifications(ComputationEx2<T, E1, E2> job)
			throws E1, E2 {
		// No local modifications
		return job.run();
	}

	@Override
	public <T, E1 extends Throwable, E2 extends Throwable> T inRevision(long rev, ComputationEx2<T, E1, E2> job)
			throws E1, E2 {
		return job.run();
	}

	@Override
	public KnowledgeBaseConfiguration getConfiguration() {
		return null;
	}

	@Override
	public SchemaSetup getSchemaSetup() {
		return null;
	}
}

