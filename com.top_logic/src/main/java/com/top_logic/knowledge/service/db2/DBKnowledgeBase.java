/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import static com.top_logic.knowledge.search.ExpressionFactory.*;
import static java.lang.Math.min;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.RandomAccess;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpSessionBindingEvent;
import jakarta.servlet.http.HttpSessionBindingListener;

import com.top_logic.base.context.TLInteractionContext;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.DebugHelper;
import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.InteractionContext;
import com.top_logic.basic.Log;
import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.Logger;
import com.top_logic.basic.LongID;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.StringID;
import com.top_logic.basic.SubSessionContext;
import com.top_logic.basic.TLID;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.col.CloseableIterator;
import com.top_logic.basic.col.CloseableIteratorAdapter;
import com.top_logic.basic.col.ComparatorChain;
import com.top_logic.basic.col.InlineList;
import com.top_logic.basic.col.LongRange;
import com.top_logic.basic.col.LongRangeSet;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.NameValueBuffer;
import com.top_logic.basic.col.TupleFactory.Pair;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.db.schema.setup.SchemaSetup;
import com.top_logic.basic.db.schema.setup.config.KeyAttributes;
import com.top_logic.basic.db.schema.setup.config.SchemaConfiguration;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.db.sql.SQLExpression;
import com.top_logic.basic.db.sql.SQLFactory;
import com.top_logic.basic.db.sql.SQLOrder;
import com.top_logic.basic.db.sql.SQLPart;
import com.top_logic.basic.db.sql.SQLQuery;
import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.basic.message.Message;
import com.top_logic.basic.sched.SchedulerServiceHandle;
import com.top_logic.basic.sql.CommitContext;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.sql.ResultSetReader;
import com.top_logic.basic.thread.UnboundListener;
import com.top_logic.basic.util.ComputationEx2;
import com.top_logic.basic.util.StopWatch;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.MetaObject.Kind;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.AbstractMetaObject;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MOReference.DeletionPolicy;
import com.top_logic.dob.meta.MOReference.HistoryType;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.dob.util.MetaObjectUtils;
import com.top_logic.knowledge.event.BranchEvent;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.ChangeSetReader;
import com.top_logic.knowledge.event.CommitEvent;
import com.top_logic.knowledge.event.CumulatedEventReader;
import com.top_logic.knowledge.event.DefaultChangeSetReader;
import com.top_logic.knowledge.event.DefaultEventWriter;
import com.top_logic.knowledge.event.EventReader;
import com.top_logic.knowledge.event.EventWriter;
import com.top_logic.knowledge.event.ItemChange;
import com.top_logic.knowledge.event.ItemDeletion;
import com.top_logic.knowledge.event.ItemEvent;
import com.top_logic.knowledge.event.ItemUpdate;
import com.top_logic.knowledge.event.KnowledgeBaseProtocol;
import com.top_logic.knowledge.event.KnowledgeEvent;
import com.top_logic.knowledge.event.KnowledgeEventMixer;
import com.top_logic.knowledge.event.ObjectCreation;
import com.top_logic.knowledge.objects.InvalidLinkException;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;
import com.top_logic.knowledge.objects.identifier.ObjectReference;
import com.top_logic.knowledge.search.AbstractQuery;
import com.top_logic.knowledge.search.Attribute;
import com.top_logic.knowledge.search.BranchParam;
import com.top_logic.knowledge.search.BufferedCompiledQuery;
import com.top_logic.knowledge.search.CompiledQuery;
import com.top_logic.knowledge.search.ConcatenatedCompiledQuery;
import com.top_logic.knowledge.search.EmptyCompiledQuery;
import com.top_logic.knowledge.search.Expression;
import com.top_logic.knowledge.search.ExpressionFactory;
import com.top_logic.knowledge.search.HistoryQuery;
import com.top_logic.knowledge.search.HistoryQueryArguments;
import com.top_logic.knowledge.search.InternalExpressionFactory;
import com.top_logic.knowledge.search.Order;
import com.top_logic.knowledge.search.QueryArguments;
import com.top_logic.knowledge.search.RangeParam;
import com.top_logic.knowledge.search.RevisionQuery;
import com.top_logic.knowledge.search.RevisionQuery.LoadStrategy;
import com.top_logic.knowledge.search.RevisionQueryArguments;
import com.top_logic.knowledge.search.SetExpression;
import com.top_logic.knowledge.service.AbstractKnowledgeBase;
import com.top_logic.knowledge.service.BasicTypes;
import com.top_logic.knowledge.service.Branch;
import com.top_logic.knowledge.service.BulkIdLoad;
import com.top_logic.knowledge.service.ChangeEventBuilder;
import com.top_logic.knowledge.service.CommitHandler;
import com.top_logic.knowledge.service.Committable;
import com.top_logic.knowledge.service.CompiledQueryCache;
import com.top_logic.knowledge.service.FlexDataManager;
import com.top_logic.knowledge.service.FlexDataManagerFactory;
import com.top_logic.knowledge.service.HistoryManager;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseConfiguration;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.KnowledgeBaseFactory;
import com.top_logic.knowledge.service.KnowledgeBaseRefetch;
import com.top_logic.knowledge.service.KnowledgeBaseRuntimeException;
import com.top_logic.knowledge.service.Messages;
import com.top_logic.knowledge.service.ReaderConfig;
import com.top_logic.knowledge.service.RefetchTimeout;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.UpdateChain;
import com.top_logic.knowledge.service.UpdateEvent;
import com.top_logic.knowledge.service.UpdateListener;
import com.top_logic.knowledge.service.db2.diff.DiffEventReader;
import com.top_logic.knowledge.service.db2.expr.exec.HistorySearch;
import com.top_logic.knowledge.service.db2.expr.transform.AnyExpansion;
import com.top_logic.knowledge.service.db2.expr.transform.BranchExpressionEnhancement;
import com.top_logic.knowledge.service.db2.expr.transform.ConstantFolding;
import com.top_logic.knowledge.service.db2.expr.transform.ExpressionCompileProtocol;
import com.top_logic.knowledge.service.db2.expr.transform.ExpressionNormalization;
import com.top_logic.knowledge.service.db2.expr.transform.ExpressionSplitter;
import com.top_logic.knowledge.service.db2.expr.transform.NullReferenceSimplifier;
import com.top_logic.knowledge.service.db2.expr.transform.SetExpressionNormalization;
import com.top_logic.knowledge.service.db2.expr.transform.SetSimplification;
import com.top_logic.knowledge.service.db2.expr.transform.UnionExtraction;
import com.top_logic.knowledge.service.db2.expr.transform.UnionSplitter;
import com.top_logic.knowledge.service.db2.expr.transform.sql.SQLBuilder;
import com.top_logic.knowledge.service.db2.expr.visit.ConcreteTypeComputation;
import com.top_logic.knowledge.service.db2.expr.visit.ExpressionPrinter;
import com.top_logic.knowledge.service.db2.expr.visit.PolymorphicTypeComputation;
import com.top_logic.knowledge.service.db2.expr.visit.SimpleExpressionEvaluator;
import com.top_logic.knowledge.service.db2.expr.visit.SymbolCreator;
import com.top_logic.knowledge.service.db2.expr.visit.TypeBinding;
import com.top_logic.knowledge.service.event.CommitChecker;
import com.top_logic.knowledge.service.event.CommitVetoException;
import com.top_logic.knowledge.service.event.Modification;
import com.top_logic.knowledge.service.event.ModificationListener;
import com.top_logic.knowledge.service.merge.MergeConflictException;
import com.top_logic.knowledge.wrap.ImplementationFactory;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.model.TLObject;
import com.top_logic.util.TLContext;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.message.MessageStoreFormat;


/**
 * A KnowledgeBase with direct Database access.
 * 
 * The cache is used as main semaphore, too. So think about 
 * proper synchronization before changing anything.
 * 
 * Locking is not totally fool-proof due to design limitations: e.g.
 * <ul>
 *      <li>Queries may return stale, cached objects </li>
 * </ul>
 * 
 * TODO BHU reimplement checkMetaData, we will need it !
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 * @author    <a href="mailto:klaus.halfmann@top-logic.com">Klaus Halfmann</a>
 */
@FrameworkInternal
public class DBKnowledgeBase extends AbstractKnowledgeBase
		implements HistoryManager, CommitHandler, HttpSessionBindingListener, UnboundListener {

	/** Special revision placeholder to access data in the session revision. */
	public static final long IN_SESSION_REVISION = -1;

	/**
	 * {@link DBContext} per thread.
	 * 
	 * <p>
	 * Note: This variable must <b>not</b> be static, as otherwise access to different
	 * {@link DBKnowledgeBase}s within the same thread would share same {@link DBContext}.
	 * </p>
	 */
	private final Property<DBContext> _localDBContext = TypedAnnotatable.property(DBContext.class, "localDBContext");

	/**
	 * {@link Branch} by {@link Thread} used by method without explicit branch attribute.
	 * 
	 * <p>
	 * Note: This variable must <b>not</b> be static, as otherwise access to different
	 * {@link DBKnowledgeBase}s within the same thread would share same {@link Branch}.
	 * </p>
	 */
	private final Property<Branch> _currentBranch = TypedAnnotatable.property(Branch.class, "currentBranch");

    /**
     * @see #getName()
     */
    protected String name;
    
    /** This one cares about DB-Specific Mappings. */
    protected DBHelper dbHelper;

	/** Our MetaObjects are found here */
    protected DBTypeRepository moRepository;

	private final CompiledQueryCache _queryCache = new ConcurrentCompiledQueryCache();

    /** Map of all Object indexed by indentifier (may be a HashMapWeak) */
	final HashMap<DBObjectKey, Object> cache = new HashMap<>();
    
	private final HashMap<Long, BranchReference> branchById = new HashMap<>();

	private final ReferenceQueue<KnowledgeItemInternal> danglingReferences =
		new ReferenceQueue<>();

	private final ReferenceQueue<Branch> branchGarbage = new ReferenceQueue<>();
    
	private List<UpdateListener> updateListeners = new CopyOnWriteArrayList<>();

	private List<UpdateListener> _updateListenersHighPrio = new CopyOnWriteArrayList<>();

	/**
	 * Holder of {@link ModificationListener} added by
	 * {@link #addModificationListener(ModificationListener)}.
	 */
	private final ArrayList<ModificationListener> modificationListeners = new ArrayList<>();

	/**
	 * Holder of {@link CommitChecker} added by {@link #addCommitChecker(CommitChecker)}.
	 */
	private final ArrayList<CommitChecker> vetoListeners = new ArrayList<>();

	Expressions _expressions;

	/**
	 * @see KnowledgeBaseConfiguration#getRefetchTimeout()
	 */
	private long refetchTimeout = 2 * 60 * 1000;

	/**
	 * @see KnowledgeBaseConfiguration#getRefetchLogTime()
	 */
	private long refetchLogTime;

	/**
	 * @see KnowledgeBaseConfiguration#getCommitWarnTime()
	 */
	private long commitWarnTime;

	private final SequenceManager sequenceManager = new RowLevelLockingSequenceManager();

	/**
	 * Commit number of the {@link UpdateEvent} that was sent as last.
	 * 
	 * <p>
	 * Note: Must be accessed from within a context synchronized at {@link #refetchLock}.
	 * </p>
	 * 
	 * @see #fireEvent(UpdateEvent)
	 */
	private long _lastSentEvent = 0;

	/**
	 * The contents of the {@link #cache} contains contents up to this revision number.
	 * 
	 * <p>
	 * Note: Must be accessed from within a context synchronized at {@link #refetchLock}.
	 * </p>
	 * 
	 * @see #refetchLock
	 */
	private long lastLocalRevision = 0;
	
	/** Entry in {@link SequenceManager} holding the last commit number. */
	public static final String REVISION_SEQUENCE = "rev";
	
	/** Entry in {@link SequenceManager} holding the last branch id. */
	public static final String BRANCH_SEQUENCE = "branch";

	/**
	 * Entry in {@link SequenceManager} defining the slot used to create {@link TLID} by this
	 * {@link KnowledgeBase}.
	 */
	public static final String ID_SEQ = "id";

	private static final Comparator<AbstractAssociationCache<?, ?>> OBJECT_NAME_ORDER =
		new Comparator<>() {

		@Override
			public int compare(AbstractAssociationCache<?, ?> o1, AbstractAssociationCache<?, ?> o2) {
			return (o1.getBaseItem().getObjectName()).compareTo(o2.getBaseItem().getObjectName());
		}
	};

	private static final class BranchHistoryNameOrder implements Comparator<AbstractAssociationCache<?, ?>> {
		/**
		 * Singleton {@link DBKnowledgeBase.BranchHistoryNameOrder} instance.
		 */
		public static final BranchHistoryNameOrder INSTANCE = new DBKnowledgeBase.BranchHistoryNameOrder();

		private BranchHistoryNameOrder() {
			// Singleton constructor.
		}

		@Override
		public int compare(AbstractAssociationCache<?, ?> c1, AbstractAssociationCache<?, ?> c2) {
			KnowledgeItem o1 = c1.getBaseItem();
			KnowledgeItem o2 = c2.getBaseItem();

			int branchCompare = CollectionUtil.compareLong(o1.getBranchContext(), o2.getBranchContext());
			if (branchCompare != 0) {
				return branchCompare;
			}
			int historyCompare = CollectionUtil.compareLong(o1.getHistoryContext(), o2.getHistoryContext());
			if (historyCompare != 0) {
				return historyCompare;
			}
			int nameCompare = o1.getObjectName().compareTo(o2.getObjectName());
			return nameCompare;
		}
	}

	private class RemoveDeletedObjectsCache implements CleanupOldValues {

		private final Iterable<? extends DBKnowledgeItem> _deletedObjects;

		public RemoveDeletedObjectsCache(Iterable<? extends DBKnowledgeItem> deletedObjects) {
			_deletedObjects = deletedObjects;
		}

		@Override
		public void cleanup() {
			synchronized (cache) {
				syncCacheCleanupReferences();
				for (DBKnowledgeItem deletedObject : _deletedObjects) {
					DBObjectKey key = deletedObject.tId();
					IDReference reference = key.getReference();
					syncCacheCleanupReference(reference);
				}
			}
		}

	}

	private FlexDataManager versionedDataManager;

	private ImplementationFactory implementationFactory = new ImplementationFactory();

	/**
	 * <p>
	 * Note: Must be accessed from within a context synchronized at {@link #refetchLock}.
	 * </p>
	 * 
	 * @see #refetchLock
	 */
	private UpdateChainLink updateChainTail = new UpdateChainLink(lastLocalRevision);

	/**
	 * <p>
	 * Note: Must be accessed from within a context synchronized at {@link #refetchLock}.
	 * </p>
	 * 
	 * @see #refetchLock
	 */
	private boolean _refetchLocked;

	/**
	 * Object to synchronize access to variables that deal with the last local revision of this
	 * {@link DBKnowledgeBase}, its update, and corresponding {@link UpdateEvent}s.
	 * 
	 * @see #lastLocalRevision
	 * @see #_refetchLocked
	 * @see #updateChainTail
	 */
	private final Object refetchLock = new Object();

	/**
	 * @see KnowledgeBaseConfiguration#isSingleNodeOptimization()
	 */
	private boolean singleNodeOptimization;

	private SchedulerServiceHandle _periodicRefetch;

	private SchedulerServiceHandle _historyCleanup;

	/**
	 * @see #getConnectionPool()
	 */
	private ConnectionPool connectionPool;

	private String dbUrl;

	/**
	 * @see KnowledgeBaseConfiguration#getDisableVersioning()
	 */
	private boolean disableVersioning;

	/**
	 * @see #getChunkSize()
	 */
	private int chunkSize = 500;

	/**
	 * TRUNK of this {@link KnowledgeBase}. It is cached to avoid multiple lookup because most
	 * requests occur in trunk.
	 */
	private Branch _trunk;

	/**
	 * Optimized SQL statement for fetching the commit number that was created just before a given
	 * date.
	 * 
	 * @see #createRevisionAtDateSQL()
	 */
	private String _revisionAtDateSql;

	private PersistentIdFactory _idFactory;

	private Map<MetaObject, Set<String>> _keyAttributes;

	private KnowledgeBaseConfiguration _configuration;

	private SchemaSetup _schemaSetup;

    /**
     * Empty constructor for {@link KnowledgeBaseFactory}.
     */
    public DBKnowledgeBase() { 
		this(null);
    }

	/**
	 * Empty constructor for {@link KnowledgeBaseFactory}.
	 */
	protected DBKnowledgeBase(SchemaSetup schemaSetup) {
		_schemaSetup = schemaSetup;
	}

	@Override
	public KnowledgeBaseConfiguration getConfiguration() {
		if (_configuration == null) {
			throw new IllegalStateException("KnowledgeBase not yet initialized.");
		}
		return _configuration;
	}

	@Override
	public SchemaSetup getSchemaSetup() {
		if (_schemaSetup == null) {
			throw new IllegalStateException("KnowledgeBase not yet initialized.");
		}
		return _schemaSetup;
	}

	private SchemaSetup loadSchemaSetup(InstantiationContext context, KnowledgeBaseConfiguration configuration,
			ConnectionPool pool) {
		String kbName = configuration.getName();
		SchemaSetup schemaSetup;
		if (configuration.isOverridePersistentTypeDefinitions()) {
			context.info(
				"Update persistent type definitions of KB '" + kbName + "' with definition from configuration files, "
					+ "because this KnowledgBase is configured to ignore persistent type definitions.");
			schemaSetup = KBUtils.getSchemaConfigResolved(configuration);
		} else {
			context.info("Read type definitions of KB '" + kbName + "' from persistency.");
			schemaSetup = readPersistentSchemaSetup(context, pool, kbName);
			if (schemaSetup == null) {
				context
					.info("No type definition for KB '" + kbName
						+ "' read from persistency, ceate type definitions from configuration files.");
				schemaSetup = KBUtils.getSchemaConfigResolved(configuration);
			}
		}
		return schemaSetup;
	}

	private SchemaSetup readPersistentSchemaSetup(InstantiationContext context, ConnectionPool pool,
			String kbName) {
		if (pool.isDryRun()) {
			context.info("No persistent schema setup, because system is in \"dry run\" mode.");
			return null;
		}
		PooledConnection readConnection = pool.borrowReadConnection();
		try {
			SchemaConfiguration schema = KBSchemaUtil.loadSchema(readConnection, kbName);
			return context.getInstance(schema);
		} catch (KnowledgeBaseException ex) {
			context.error("Loading persistent schema configuration failed.", ex);
			return null;
		} finally {
			pool.releaseReadConnection(readConnection);
		}
	}

	/**
	 * Stores a resolved version of the {@link SchemaSetup} in the database to read it on next boot.
	 * 
	 * @param log
	 *        {@link Log} to log problems to.
	 */
	public void storeSchemaSetup(Log log) {
		String kbName = getName();
		/* Resolve the MetaObject configuration to ensure that the persistent schema configuration
		 * does not depend on the files containing the configurations. */
		SchemaConfiguration config = getSchemaSetup().getConfig();
		PooledConnection connection = getConnectionPool().borrowWriteConnection();
		try {
			KBSchemaUtil.storeSchema(connection, kbName, config);

			connection.commit();
		} catch (SQLException ex) {
			log.error("Unable to store knowledge base configuration: " + config, ex);
		} finally {
			getConnectionPool().releaseWriteConnection(connection);
		}
	}

	/**
	 * Setup from configuration.
	 */
    @Override
	public void initialize(Protocol protocol, KnowledgeBaseConfiguration configuration) {
		try {
    		if (name != null) { // already initialized ...
    			return;
    		}
			_configuration = configuration;
    		this.name = configuration.getName();
    		this.singleNodeOptimization = configuration.isSingleNodeOptimization();
    		
    		this.refetchTimeout = configuration.getRefetchTimeout();
			this.refetchLogTime = configuration.getRefetchLogTime();
			this.commitWarnTime = configuration.getCommitWarnTime();
    		this.disableVersioning = configuration.getDisableVersioning();
    		this.chunkSize = configuration.getReaderChunkSize();
			this.connectionPool = ConnectionPoolRegistry.getConnectionPool(configuration.getConnectionPool());
			InstantiationContext context = InstantiationContext.toContext(protocol);
			if (_schemaSetup == null) {
				_schemaSetup = loadSchemaSetup(context, _configuration, connectionPool);
			}
			this.dbHelper = this.connectionPool.getSQLDialect();
			this.moRepository =
				DBTypeRepository.newRepository(context, this.dbHelper, _schemaSetup, !disableVersioning);
    		
			initKeyAttributes();
			initIdFactory();
			initDataManagers();
			initImplementationFactory();

			_revisionAtDateSql = createRevisionAtDateSQL();
			this._expressions = new Expressions(this);
    	} catch (SQLException ex) {
    		protocol.fatal("Access to database failed", ex);
		} catch (DataObjectException ex) {
			protocol.fatal("TypeSystem misconfigured", ex);
		} catch (ConfigurationException ex) {
			protocol.fatal("Unable to instantiate TypeSystem.", ex);
		}
	}

	private void initKeyAttributes() {
		Map<MetaObject, Set<String>> keyAttributes = moRepository.getMetaObjects()
			.stream()
			.filter(MOClass.class::isInstance)
			.filter(mo -> ((MOClass) mo).getAnnotation(KeyAttributes.class) != null)
			.collect(Collectors.toMap(Function.identity(),
				mo -> ((MOClass) mo).getAnnotation(KeyAttributes.class).getAttributes()));

		/* Sort configured types such that the super classes occur after subclasses. If concrete
		 * subtypes are added in such order (ignoring already added types), configurations for more
		 * concrete subclasses win against superclasses. */
		List<MetaObject> sortedTypes = CollectionUtil.topsort(new Mapping<MetaObject, List<MetaObject>>() {

			@Override
			public List<MetaObject> map(MetaObject input) {
				if (input instanceof MOClass) {
					return Collections.<MetaObject> singletonList(((MOClass) input).getSuperclass());
				}
				return Collections.emptyList();
			}
		}, keyAttributes.keySet(), false);
		Collections.reverse(sortedTypes);

		for (int i = 0; i < sortedTypes.size(); i++) {
			MetaObject type = sortedTypes.get(i);
			Set<String> attributesForType = keyAttributes.get(type);
			for (MetaObject t : moRepository.getConcreteSubtypes(type)) {
				if (keyAttributes.containsKey(t)) {
					// a more special type already added key attributes for this concrete subtype
					continue;
				}
				keyAttributes.put(t, attributesForType);
			}
		}
		_keyAttributes = keyAttributes;
	}

	/**
	 * Determines the key attribute names for the the given {@link MetaObject}.
	 * 
	 * @param type
	 *        The type to get key attributes for.
	 */
	public Set<String> getKeyAttributes(MetaObject type) {
		Set<String> keyAttributes = _keyAttributes.get(type);
		if (keyAttributes == null) {
			keyAttributes = Collections.emptySet();
		}
		return keyAttributes;
	}

	private void initIdFactory() {
		assert connectionPool != null;
		_idFactory = new PersistentIdFactory(connectionPool, ID_SEQ);
	}

	/**
	 * Initialize {@link #dbHelper} and {@link #dbUrl} by accessing the given
	 * database using a primitive form of retry logic upon failure.
	 * 
	 * @param currentConnectionPool
	 *        the database to initialize from
	 * @throws SQLException
	 *         If database access finally fails.
	 */
	private void initDbProperties(ConnectionPool currentConnectionPool) throws SQLException {
		this.dbUrl = getSchemaUrl(currentConnectionPool);
	}

	private String getSchemaUrl(ConnectionPool currentConnectionPool) throws SQLException {
		String url;
		PooledConnection connection = currentConnectionPool.borrowReadConnection();
		try {
			// No retry, as connection must be available at this time
			url = connection.getMetaData().getURL();
		} finally {
			currentConnectionPool.releaseReadConnection(connection);
		}
		return url;
	}

	/**
	 * Chunk size for reading history events.
	 * 
	 * <p>
	 * During reading the history, the given number of revisions are cached in memory before they
	 * are returned by the readers.
	 * </p>
	 * 
	 * @see KnowledgeBaseConfiguration#getReaderChunkSize()
	 */
    public final int getChunkSize() {
		return chunkSize;
	}
    
    /**
     * Sets the {@link #getChunkSize()} property.
     */
    public final void setChunkSize(int chunkSize) {
		this.chunkSize = chunkSize;
	}
    
	private void initDataManagers() {
		FlexDataManagerFactory factory = FlexDataManagerFactory.getInstance();
		
		MOKnowledgeItemImpl dataType = lookupType(AbstractFlexDataManager.FLEX_DATA);
		this.versionedDataManager = factory.newFlexDataManager(connectionPool, dataType);
	}

	private void initImplementationFactory() {
		implementationFactory.init(moRepository);
	}

	/**
	 * Initialises the local variables of this {@link KnowledgeBase} depending on the database
	 * tables.
	 */
	@FrameworkInternal
	public void initLocalVariablesFromDatabase(Protocol protocol) {
		try {
			initDbProperties(this.connectionPool);
		} catch (SQLException ex) {
			protocol.fatal("Access to database failed", ex);
		}
		installTrunk();

		long lastGlobalRevision = getLastRevisionId();
		synchronized (refetchLock) {
			this.lastLocalRevision = lastGlobalRevision;
			syncRefetchUpdateRevisionAndPublishUpdate(new UpdateChainLink(lastLocalRevision));
			_lastSentEvent = lastGlobalRevision;
		}
	}

	/**
	 * Unconditionally sets {@link #getLastLocalRevision()} to the given revision.
	 */
	void resetLastRevision(long revision) {
		synchronized (refetchLock) {
			this.lastLocalRevision = revision;
			_lastSentEvent = revision;
		}
	}

    @Override
	public void startup(Protocol protocol) {
		initLocalVariablesFromDatabase(protocol);

		long refetchInterval = _configuration.getRefetchInterval();
		if (refetchInterval > 0 && !singleNodeOptimization) {
			_periodicRefetch = KnowledgeBaseRefetch.createPeriodic(this);
			_periodicRefetch.start(refetchInterval, TimeUnit.MILLISECONDS);
		}
		long cleanupInterval = _configuration.getCleanupUnversionedTypesInterval();
		if (cleanupInterval > 0) {
			List<? extends MOKnowledgeItem> unversionedConcreteTypes =
				moRepository.getUnversionedItemApplicationTypes().stream()
					.filter(type -> !type.isAbstract())
					.collect(Collectors.toList());
			if (!unversionedConcreteTypes.isEmpty()) {
				_historyCleanup = HistoryCleanup.newCleanupHandle(protocol, this, unversionedConcreteTypes);
				_historyCleanup.start(cleanupInterval, TimeUnit.SECONDS);
			}
		}
	}

	@Override
	public void shutDown() {
		if (_periodicRefetch != null) {
			_periodicRefetch.stop(_configuration.getRefetchWaitOnStopTimeout(), TimeUnit.MILLISECONDS);
		}
		if (_historyCleanup != null) {
			_historyCleanup.stop(10L, TimeUnit.SECONDS);
		}
	}

	void installTrunk() {
		this._trunk = getBranch(TLContext.TRUNK_ID);
	}

	boolean isItemType(MetaObject table) {
		return table.isSubtypeOf(getItemType());
	}
 
	@Override
	public String toString() {
		return name + " [types: " + getObjectTypes().size()
					+ ((cache != null) ? (", cache size: " + cache.size()) : "")
                    + ", DB: " + dbUrl
                    + "] ";
    }

    @Override
	public String getName() {
        return name;
    }

    /**
     * @see KnowledgeBaseConfiguration#getCommitWarnTime()
     */
    long getCommitWarnTime() {
    	return commitWarnTime;
    }
    
	@Override
	public TLID createID() {
		if (IdentifierUtil.SHORT_IDS) {
			return LongID.valueOf(_idFactory.createId());
		} else {
			return StringID.createRandomID();
		}
	}

	@Override
	public KnowledgeItem createKnowledgeItem(Branch branch, TLID objectName, String typeName,
			Iterable<Entry<String, Object>> initialValues) throws DataObjectException {
		assert objectName == null || isInternalId(objectName) : "Invalid identifier: " + objectName;
		MetaObject mo;
		try {
			mo = moRepository.getMetaObject(typeName);
		} catch (UnknownTypeException ex) {
			throw new KnowledgeBaseRuntimeException("Unknown type '" + typeName + "'.", ex);
		}

		if (MetaObjectUtils.isAbstract(mo)) {
			throw new DataObjectException("Can not create item for abstract type '" + mo + "'.");
		}

		if (!isItemType(mo)) {
			throw new DataObjectException("Objects must have item type: " + mo);
		}

		MOKnowledgeItemImpl type = (MOKnowledgeItemImpl) mo;
		long dataBranch = branch.getBaseBranchId(type);
        
		DBContext context = createDBContext();

        // Note: The check whether the given type is abstract is done during
		// creating. Therefore, try to allocate object before trying to create
		// an identifier for the new object.
		DBKnowledgeObjectBase result = (DBKnowledgeObjectBase) newKnowledgeItem(type);
		initNew(type, result, dataBranch, objectName, getSessionRevision());

		context.putNewObject(result);
		try {
			/* set initial values after the identifier was initialised, because reference attribute
			 * may need identifier to adopt value to context. */
			result.init(context, initialValues);
			result.initWrapper();
		} catch (DataObjectException ex) {
			// Remove former added item to have clean DBContext.
			context.dropNewItem(result);
			throw ex;
		}

		context.finishPutNewObject(result);

		return result;
	}
	
	private boolean isInternalId(TLID objectName) {
		return IdentifierUtil.SHORT_IDS ? objectName instanceof LongID : objectName instanceof StringID;
	}

	@Override
	public <T extends KnowledgeItem> List<T> createObjects(Iterable<ObjectCreation> creations, Class<T> expectedType)
			throws DataObjectException {
		DBContext context = createDBContext();
		List<DBKnowledgeObjectBase> createdObjects = new ArrayList<>();
		long sessionRevision = getSessionRevision();
		for (ObjectCreation creation : creations) {
			ObjectBranchId objectId = creation.getObjectId();
			MetaObject createType = objectId.getObjectType();
			MOKnowledgeItemImpl type = lookupType(createType.getName());
			if (createType != type) {
				throw errorEventFromForeignKnowledgBase(creation);
			}

			// Note: The check whether the given type is abstract is done during
			// creating. Therefore, try to allocate object before trying to create
			// an identifier for the new object.
			DBKnowledgeObjectBase newObject = (DBKnowledgeObjectBase) newKnowledgeItem(type);
			// Check that result has correct type for unchecked cast of result list
			CollectionUtil.dynamicCast(expectedType, newObject);

			long dataBranch = getBranch(objectId.getBranchId()).getBaseBranchId(type);
			initNew(type, newObject, dataBranch, objectId.getObjectName(), sessionRevision);
			createdObjects.add(newObject);
		}
		context.putNewObjects(createdObjects);
		int i = 0;
		for (ObjectCreation creation : creations) {
			DBKnowledgeObjectBase newObject = createdObjects.get(i++);
			try {
				newObject.init(context, creation.applicationValues(newObject));
			} catch (DataObjectException ex) {
				// Remove former added items to have clean DBContext.
				for (DBKnowledgeItem item : createdObjects) {
					context.dropNewItem(item);
				}
				throw new DataObjectException("Error initialising object for event " + creation, ex);
			}
		}
		assert i == createdObjects.size() : "Number of created objects match number of ObjectCreation's.";

		/* Initialize Wrapper's in separate loop, because creating wrapper may depend on other
		 * wrapper (e.g. on type which may be created in same CS). Therefore all values must be set
		 * before the first wrapper can be initialized. */
		for (DBKnowledgeObjectBase newObject : createdObjects) {
			newObject.initWrapper();
		}

		context.finishPutNewObjects(createdObjects);

		// each element is of expected type as check occurs after creation.
		@SuppressWarnings("unchecked")
		List<T> typeSafeCreations = (List<T>) createdObjects;
		return typeSafeCreations;
	}

	private DataObjectException errorEventFromForeignKnowledgBase(ObjectCreation creation) {
		StringBuilder errorMessage = new StringBuilder();
		errorMessage.append("Type of object to create ('");
		errorMessage.append(creation.getObjectType());
		errorMessage.append("') is of foreign KnowledgeBase. Creation: ");
		errorMessage.append(creation);
		DataObjectException dataObjectException = new DataObjectException(errorMessage.toString());
		return dataObjectException;
	}

	private void initNew(MOKnowledgeItemImpl type, DBKnowledgeObjectBase result, long dataBranch,
			TLID customObjectName, long sessionRevision) {
		TLID objectName = newItemIdentifier(type, dataBranch, customObjectName, sessionRevision);
		result.initNew(dataBranch, objectName);
	}

	/**
	 * Creates an identifier for a newly created knowledge item.
	 * 
	 * @param type
	 *        The type of the new item.
	 * @param branchContext
	 *        The branch context in which the item is created.
	 * @param customObjectName
	 *        The (optional) custom name of the new item.
	 * @param sessionRevision
	 *        The revision of the creating session.
	 * @return The identifier of the new item.
	 */
	private TLID newItemIdentifier(MOKnowledgeItemImpl type, long branchContext, TLID customObjectName,
			long sessionRevision) {
		TLID objectName;
		if (customObjectName == null) {
			objectName = createID();
		} else {
			objectName = customObjectName;
		}
		
        if (customObjectName != null) {
			DBObjectKey newIdentifier = new DBObjectKey(branchContext, Revision.CURRENT_REV, type, objectName);
        	// Check for name clashes of custom identifier. Check finds only
			// clashes with objects currently in cache. Clashes with objects
			// currently not cached are detected at commit time.
			KnowledgeItemInternal currentObject = cleanupAndLookupCache(newIdentifier, sessionRevision);
			if (currentObject != null) {
    			throw new IllegalArgumentException("Object with given name already exists: " + newIdentifier);
    		}
        }
		return objectName;
	}

	/**
	 * Creates a new {@link KnowledgeItem} instance with given type.
	 * 
	 * @param type
	 *        The type of item to create.
	 * @return The new item.
	 */
	/* package protected */final AbstractDBKnowledgeItem newKnowledgeItem(MOKnowledgeItem type) {
		return type.getImplementationFactory().newKnowledgeItem(this, type);
	}

	/* package protected */final AbstractDBKnowledgeItem newImmutableItem(MOKnowledgeItem type) {
		return type.getImplementationFactory().newImmutableItem(this, type);
	}

    @Override
	public KnowledgeObject getKnowledgeObject(String aTypeName, TLID objectName) {
		MOKnowledgeItem type = lookupType(aTypeName);
		if (type.isAbstract()) {
			throw new IllegalArgumentException("Abstract type '" + aTypeName + "' does not have instances. ID: "
				+ objectName);
		}
		long branchContext = this.getContextBranch().getBranchId();
		DBContext context = getCurrentDBContext();
		return (KnowledgeObject) itemById(context,
			toSearchIdentity(branchContext, Revision.CURRENT_REV, type, objectName));
    }

    /** 
     * Workaround for declared exception in {@link MORepository#getMetaObject(String)}. 
     */
	public MOKnowledgeItemImpl lookupType(String aTypeName) {
		MOKnowledgeItemImpl type;
		try {
			type = (MOKnowledgeItemImpl) moRepository.getMetaObject(aTypeName);
		} catch (UnknownTypeException ex) {
			throw new KnowledgeBaseRuntimeException("Unknown type '" + aTypeName + "'.", ex);
		}
		return type;
	}
    
	@Override
	public boolean hasHistory() {
		return ! disableVersioning;
	}
	
    @Override
	public Revision getRevision(long commitNumber) {
		if (commitNumber == Revision.CURRENT_REV) {
    		return Revision.CURRENT;
    	}
		if (commitNumber == 0) {
			return Revision.INITIAL;
		}
    	// Fetch the revision from the database. Note: this method is called
    	// during proxy resolution in the revision proxy object (and
    	// therefore must not return another proxy)!
		DBContext context = getCurrentDBContext();
		DBObjectKey requestedIdentity =
			new DBObjectKey(TLContext.TRUNK_ID, Revision.CURRENT_REV, getRevisionType(), LongID.valueOf(commitNumber));
		return (Revision) itemById(context, requestedIdentity);
    }
    
    @Override
	public Revision getRevisionAt(long time) {
		PooledConnection readConnection = connectionPool.borrowReadConnection();
		try {
			try (PreparedStatement statement = readConnection.prepareStatement(_revisionAtDateSql)) {
				statement.setLong(1, time);

				try (ResultSet resultSet = statement.executeQuery()) {
					if (resultSet.next()) {
						long commitNumber = resultSet.getLong(1);
						return getRevision(commitNumber);
					} else {
						return Revision.INITIAL;
					}
				}
			}
		} catch (SQLException ex) {
			throw new KnowledgeBaseRuntimeException(ex);
		} finally {
			connectionPool.releaseReadConnection(readConnection);
		}
    }

	/**
	 * @see #_revisionAtDateSql
	 */
	private String createRevisionAtDateSQL() throws SQLException {
		DBHelper sqlDialect = connectionPool.getSQLDialect();
		MOKnowledgeItemImpl revisionType = getRevisionType();
		String revColumn = sqlDialect.columnRef(RevisionType.getRevisionAttribute(revisionType).getDBName());
		String dateColumn = sqlDialect.columnRef(RevisionType.getDateAttribute(revisionType).getDBName());
		String revisionTable = sqlDialect.tableRef(revisionType.getDBName());

		StringBuilder buffer = new StringBuilder();
		buffer.append("SELECT ");
		buffer.append("max(");
		buffer.append(revColumn);
		buffer.append(")");
		buffer.append(" FROM ");
		buffer.append(revisionTable);
		buffer.append(" WHERE ");
		buffer.append(dateColumn);
		buffer.append(" = ");
		buffer.append("(");
		buffer.append("SELECT ");
		buffer.append("max(");
		buffer.append(dateColumn);
		buffer.append(")");
		buffer.append(" FROM ");
		buffer.append(revisionTable);
		buffer.append(" WHERE ");
		buffer.append(dateColumn);
		buffer.append(" <= ?");
		buffer.append(")");
		String sql = buffer.toString();
		return sql;
	}

	@Override
	public long getLastRevision() {
		return getLastLocalRevision();
    }

	private long getLastRevisionId() {
		MOKnowledgeItemImpl revisionType = getRevisionType();
		String getMaxRevStatement = 
			"SELECT max(" + dbHelper.columnRef(RevisionType.getRevisionAttribute(revisionType).getDBName()) + ") "
			+ "FROM " + dbHelper.tableRef(revisionType.getDBName());
		
    	PooledConnection readConnection = connectionPool.borrowReadConnection();
    	try {
    		return fetchLongValue(readConnection, getMaxRevStatement);
		} finally {
			connectionPool.releaseReadConnection(readConnection);
		}
	}
    
    @Override
	public long getLastUpdate(KnowledgeItem item) {
		return ((KnowledgeItemInternal) item).getLastUpdate();
    }
    
    @Override
	public long getCreateRevision(KnowledgeItem item) {
		return item.getCreateCommitNumber();
    }
//    
//	private RevisionProxy newRevisionProxy(Long commitNumber) {
//		return new RevisionProxy(this, commitNumber);
//	}
    
    @Override
	public final KnowledgeItem getKnowledgeItem(Revision revision, KnowledgeItem anObject) {
    	KnowledgeItemInternal item = (KnowledgeItemInternal) anObject;
		long branchContext = item.getBranchContext();
		long historyContext = revision.getCommitNumber();
		
		if (historyContext == item.getHistoryContext()) {
    		return item;
    	}
    	
		MOKnowledgeItem itemType = (MOKnowledgeItem) item.tTable();
		ObjectKey requestedIdentity = toSearchIdentity(branchContext, historyContext, itemType, item.getObjectName());
		DBContext context = getCurrentDBContext();
    	
		return itemById(context, requestedIdentity);
    }

    @Override
	public final KnowledgeItem getKnowledgeItem(Branch branch, Revision revision, MetaObject type, TLID objectName) {
    	DBContext context = getCurrentDBContext();
    	return itemById(context, toSearchIdentity(branch.getBranchId(), revision.getCommitNumber(), (MOKnowledgeItem) type, objectName));
    }
    
    @Override
	public KnowledgeAssociation getKnowledgeAssociation(String aTypeName, TLID objectName) {
		MOKnowledgeItem type = lookupType(aTypeName);
		long branchContext = this.getContextBranch().getBranchId();
		DBContext context = getCurrentDBContext();
		return (KnowledgeAssociation) itemById(context,
			toSearchIdentity(branchContext, Revision.CURRENT_REV, type, objectName));
    }
    
	@Override
	public ObjectReference getItemIdentity(final KnowledgeItem item, Object original) {
		return new ObjectReference(original, TLContext.TRUNK_ID, Revision.CURRENT_REV, item.tTable(),
			((KnowledgeItemInternal) item).tId().getObjectName());
    }

	/**
	 * Calls {@link #itemById(DBContext, ObjectKey, long, boolean)} with implicit data revision
	 * given by {@link #getDataRevision(long)}.
	 * 
	 * @see #itemById(DBContext, ObjectKey, long, boolean)
	 */
	KnowledgeItemInternal itemById(DBContext context, ObjectKey requestedIdentity) {
		return itemById(context, requestedIdentity, false);
	}

	private KnowledgeItemInternal itemById(DBContext context, ObjectKey requestedIdentity, boolean cacheOnly) {
		return itemById(context, requestedIdentity, IN_SESSION_REVISION, cacheOnly);
	}

	/**
	 * Fetch the {@link KnowledgeItem} with the given identifier.
	 * 
	 * <p>
	 * If the revision of the given identifier is not {@link Revision#CURRENT_REV}, an immutable
	 * historic object is returned that represents the state of the requested object at the point in
	 * time denoted by the revision number. If the identifier revision is
	 * {@link Revision#CURRENT_REV} the current mutable object is returned.
	 * </p>
	 * 
	 * If the object is already loaded that instance is returned.
	 * 
	 * @param requestedIdentity
	 *        The {@link KnowledgeItemInternal#tId() identity} of the requested object.
	 * @param dataRevision
	 *        The history context to load the data from, or {@link #IN_SESSION_REVISION}, see
	 *        {@link DBAccess#fetch(DBKnowledgeBase, PooledConnection, long, TLID, long, long)}
	 * @return The current or historic version of the object.
	 * 
	 * @see ObjectKey#getHistoryContext() for the requested revision.
	 */
	private KnowledgeItemInternal itemById(DBContext context, ObjectKey requestedIdentity, long dataRevision,
			boolean cacheOnly) {
		if (dataRevision == IN_SESSION_REVISION) {
			MOKnowledgeItem objectType = (MOKnowledgeItem) requestedIdentity.getObjectType();
			long historyContext = requestedIdentity.getHistoryContext();
			dataRevision = getDataRevision(historyContext);
		}
		boolean includeLocalChanges = context != null && HistoryUtils.isCurrent(requestedIdentity);
		KnowledgeItemInternal result = cleanupAndLookupCache(requestedIdentity, dataRevision);
        if (result != null) {
			if (includeLocalChanges && isLocallyRemoved(context, requestedIdentity)) {
				return null;
			}
        	return result;
		}

		if (includeLocalChanges) {
			KnowledgeItemInternal newlyCreatedObject = getLocallyCreated(context, requestedIdentity);
			if (newlyCreatedObject != null) {
				return newlyCreatedObject;
			}
		}

		if (cacheOnly) {
			return null;
		}

		return fetchItemById(context, requestedIdentity, dataRevision);
	}

	private KnowledgeItemInternal fetchItemById(DBContext context, ObjectKey requestedIdentity, long dataRevision) {
        // Not in cache, search for data item.
		long requestedHistoryContext = requestedIdentity.getHistoryContext();
		preventFutureHistoryCtx(requestedHistoryContext, requestedIdentity);
		long requestedBranchContext = requestedIdentity.getBranchContext();
		
		MOKnowledgeItem type = (MOKnowledgeItem) requestedIdentity.getObjectType();

		Branch requestedBranch = getBranch(requestedBranchContext);
		long dataBranchContext = requestedBranch.getBaseBranchId(type);
        
        MOKnowledgeItem table = type;
        PooledConnection readConnection = connectionPool.borrowReadConnection();
        try {
    		Expression search = createSearchExpression(table, requestedIdentity);
			KnowledgeItemInternal fetchedItem =
				fetchItem(readConnection, table, dataBranchContext, requestedIdentity.getObjectName(),
					requestedHistoryContext, dataRevision);
    		return toSearchResult(context, dataBranchContext, requestedHistoryContext, search, fetchedItem);
		} catch (SQLException e) {
			throw new KnowledgeBaseRuntimeException("Lookup failed", e);
		} finally {
			connectionPool.releaseReadConnection(readConnection);
		}
	}

	private Expression createSearchExpression(MOClass table, ObjectKey requestedIdentity) {
		MOAttribute idAttr = table.getAttributeOrNull(BasicTypes.IDENTIFIER_ATTRIBUTE_NAME);
		Attribute idExpr = InternalExpressionFactory.attributeTyped(idAttr);
		Expression nameExpr = ExpressionFactory.literal(requestedIdentity.getObjectName());
		return ExpressionFactory.eqBinary(idExpr, nameExpr);
	}

	private void preventFutureHistoryCtx(long historyContext, ObjectKey requestedIdentity)
			throws IllegalArgumentException {
		if (historyContext == Revision.CURRENT_REV) {
			// current object
			return;
		}
		final long lastRev = getSessionRevision();
		if (historyContext > lastRev) {
			// future object
			throw new IllegalArgumentException("Unable to resolve future object: " + requestedIdentity
				+ ". Last revision: " + lastRev + ".");
		}
	}

	/**
	 * Looks up the object with the given identity from the cache.
	 * 
	 * <p>
	 * Note: This method must be called form a context synchronized at the {@link #cache} map.
	 * </p>
	 * 
	 * @param identity
	 *        The identity of the object to be returned.
	 * @param lookupRevision
	 *        The revision in which the lookup occurs.
	 * @param getDeleted
	 *        Whether objects that are deleted in the given revision must be reported.
	 * @return The object found in the cache or <code>null</code>, if the object is not cached.
	 */
	private KnowledgeItemInternal syncCacheLookupCache(ObjectKey identity, long lookupRevision, boolean getDeleted) {
		Object cacheEntry = this.cache.get(identity);
		if (cacheEntry instanceof IDReference) {
			IDReference reference = (IDReference) cacheEntry;
			KnowledgeItemInternal result = reference.get();
			if (result == null) {
				// Drop garbage collected reference.
				cache.remove(identity);
				
				// Mark reference as already processed. If it is found later in
				// its reference queue, it is silently dropped.
				reference.destroy();
			}
			
			if (!getDeleted) {
				if (result instanceof DBKnowledgeItem) {
					// only current objects can be deleted.
					if (!((DBKnowledgeItem) result).valuesAlive(lookupRevision).isAlive()) {
						// object was deleted in given revision.
						return null;
					}
				}
			}
			
			return result;
		} else if (cacheEntry instanceof CacheValidity) {
			CacheValidity validity = (CacheValidity) cacheEntry;
			if (lookupRevision > validity.maxValidity()) {
				// session to new
				return null;
			}
			while (true) {
				CacheValidity formerValidity = validity.formerValidity();
				if (formerValidity == null) {
					// No former validity with valid data for session.
					break;
				}
				if (lookupRevision > formerValidity.maxValidity()) {
					// session to new for former validity.
					break;
				}
				validity = formerValidity;
			}
			KnowledgeItemInternal cachedObject = validity.getReference().get();
			if (!getDeleted) {
				if (cachedObject instanceof DBKnowledgeItem) {
					// only current objects can be deleted.
					if (!((DBKnowledgeItem) cachedObject).valuesAlive(lookupRevision).isAlive()) {
						// object was deleted in given revision.
						return null;
					}
				}
			}
			return cachedObject;
		} else {
			return null;
		}
	}

	/**
	 * Inserts the given item into the {@link #cache} map.
	 * 
	 * <p>
	 * Note: Must be called from a context synchronized on the {@link #cache} map.
	 * </p>
	 * 
	 * @param item
	 *        The object to insert into the cache.
	 * @param revision
	 *        The revision from which on the given item is valid.
	 */
	final void syncCacheInsertCache(KnowledgeItemInternal item, long revision) {
		// Create soft reference for building the GC-limited cache.
		DBObjectKey identity = item.tId();
		
		IDReference reference = new IDReference(item, this.danglingReferences, identity);
		identity.updateReference(reference);
		
		Object clash = cache.put(identity, reference);
		if (clash != null) {
			syncCacheHandleInsertClash(identity, reference, clash, revision);
		}
	}

	private void syncCacheHandleInsertClash(DBObjectKey identity, IDReference reference, Object clash, long revision) {
		CacheValidity formerCacheValidity;
		if (clash instanceof IDReference) {
			KnowledgeItemInternal cachedItem = ((IDReference) clash).get();
			if (cachedItem == null) {
				/* old reference was cleared in the meanwhile. Inserted reference is the only valid
				 * reference now. */
				/* Actually this must not occur: As long as the update event for the delete session
				 * exists, the reference can not be cleared. During finalization of the update event
				 * the cache entry is actively removed. Therefore there is a hard reference (in the
				 * update event) until the cache entry is removed. */
				return;
			}
			formerCacheValidity = new CacheValidity((IDReference) clash, Revision.FIRST_REV, revision - 1);
		} else {
			formerCacheValidity = (CacheValidity) clash;
			formerCacheValidity.updateMaxValidity(revision - 1);
		}
		CacheValidity newCacheValidity = new CacheValidity(reference, revision, Revision.CURRENT_REV);
		newCacheValidity.setFormerValidity(formerCacheValidity);
		// Remove entry as otherwise the old key is stored in cache.
		cache.remove(identity);
		cache.put(identity, newCacheValidity);
	}

	/**
	 * Drop all those identities from cache, whose objects have been garbage
	 * collected.
	 * 
	 * <p>
	 * Note: Must be called from a context synchronized on the {@link #cache}
	 * map.
	 * </p>
	 */
	void syncCacheCleanupReferences() {
		while (true) {
			IDReference reference = (IDReference) this.danglingReferences.poll();
			if (reference == null) {
				break;
			}
			
			syncCacheCleanupReference(reference);
		}
	}

	/**
	 * Removes the given reference from cache entries.
	 * 
	 * <p>
	 * Note: Must be called from a context synchronized on the {@link #cache} map.
	 * </p>
	 * 
	 * @param reference
	 *        The reference pointing to the removed object.
	 */
	void syncCacheCleanupReference(IDReference reference) {
		ObjectKey identity = reference.getIdentity();
		
		if (identity == null) {
			/* reference was removed before, e.g. by general cache cleanup or by removing it
			 * explicit in cleanup action. */
			return;
		}
		syncCacheRemoveCacheEntry(identity, reference);

		/* Mark reference as removed. */
		reference.destroy();
	}

	/**
	 * Removes the cache entry for the given key.
	 * 
	 * <p>
	 * Note: Must be called from a context synchronized on the {@link #cache} map.
	 * </p>
	 * 
	 * @param referenceToRemove
	 *        The corresponding reference pointing to the removed object.
	 */
	private void syncCacheRemoveCacheEntry(ObjectKey identity, IDReference referenceToRemove) {
		Object removed = cache.remove(identity);
		if (removed == null) {
			/* Already removed. */
			return;
		}
		if (removed == referenceToRemove) {
			/* There is only one entry, which is the given one. */
			return;
		}
		if (removed instanceof IDReference) {
			/* This may happen in following situation: Object A is deleted (rev1), recreated, and
			 * again deleted (rev2). GC first cleans rev2 which completely clears the cache for A.
			 * Before rev1 can be cleaned by GC, A is recreated. In this case the cache entry is the
			 * reference to the new A. */
			return;
		}

		CacheValidity mostRecentValidity = ((CacheValidity) removed).removeEntryFor(referenceToRemove);
		if (mostRecentValidity == null) {
			/* No valid item found at all. */
			return;
		}

		CacheValidity formerValidity = mostRecentValidity.formerValidity();
		if (formerValidity == null) {
			/* Only one cache entry. Cache reference directly */
			IDReference newReference = mostRecentValidity.getReference();
			cache.put(newReference.getIdentity(), newReference);
		} else {
			cache.put(mostRecentValidity.getReference().getIdentity(), mostRecentValidity);
		}
	}
	
	class IDReference extends SoftReference<KnowledgeItemInternal> {

		/**
		 * @see #getIdentity()
		 */
		private DBObjectKey id;

		public IDReference(KnowledgeItemInternal referent, ReferenceQueue<KnowledgeItemInternal> q, DBObjectKey id) {
			super(referent, q);
			this.id = id;
		}

		/**
		 * The {@link ObjectKey} that uses this reference to point to its cached object.
		 * 
		 * @return The owning {@link ObjectKey}, or <code>null</code>, if this reference was dropped
		 *         early from cache.
		 * 
		 * @see DBKnowledgeBase#syncCacheLookupCache(ObjectKey, long, boolean)
		 */
		public final DBObjectKey getIdentity() {
			return id;
		}

		/**
		 * Marks this {@link IDReference} as dropped from cache before its
		 * reference queue has been processed.
		 */
		public final void destroy() {
			this.id = null;
		}

		@Override
		public String toString() {
			return "IDReference [id=" + id + "]";
		}

	}
	
    /**
	 * Check, whether the object identified by the given identifier is locally removed in the given
	 * context.
	 * 
	 * @param context
	 *        The context to check for.
	 * @param identity
	 *        The object identifier to check.
	 * 
	 * @return Whether the object is locally removed in the given context.
	 */
	private boolean isLocallyRemoved(DBContext context, ObjectKey identity) {
		return context.isRemovedKey(identity);
	}

	/**
	 * Returns the the object identified by the given identifier which is locally created in the
	 * given context.
	 * 
	 * @param context
	 *        The context to check for.
	 * @param identity
	 *        The object identifier to return object for.
	 * 
	 * @return The object which is locally created in the given context, or <code>null</code> if no
	 *         was created.
	 */
	private KnowledgeItemInternal getLocallyCreated(DBContext context, ObjectKey identity) {
		return context.getNew(identity);
	}

    /*package protected*/ final DBAccess getDBAccess(MOKnowledgeItem type) {
		return ((MOKnowledgeItemImpl) type).getDBAccess();
	}

    /**
     * Creates an {@link ObjectKey} that is only valid for {@link #cache} lookup.
     */
    private ObjectKey toSearchIdentity(long requestedBranchContext, long requestedHistoryContext, MOKnowledgeItem requestedType, TLID requestedName) {
		return new DBObjectKey(requestedBranchContext, requestedHistoryContext, requestedType, requestedName);
	}

	@Override
	public Collection<KnowledgeObject> getAllKnowledgeObjects(String type) {
		return allItemsOfType(type, getObjectType());
    }

    @Override
	public Collection<KnowledgeAssociation> getAllKnowledgeAssociations(String type) {
		return allItemsOfType(type, getAssociationType());
    }
    
	private Collection allItemsOfType(String type, MOClass superType) {
		if (type == null) {
			throw new NullPointerException("'type' must not be 'null'.");
		}

		MOKnowledgeItem table = lookupType(type);

		if (!table.isSubtypeOf(superType)) {
			throw new IllegalArgumentException("'" + type + "' must be a subtype of '" + superType.getName() + "'");
		}
		Branch contextBranch = this.getContextBranch();
		
		CompiledQuery<?> compiledQuery = _expressions.allOfTypeQuery(table);
		RevisionQueryArguments queryArguments = _expressions.allOfTypeArguments(contextBranch);
		
		return compiledQuery.search(queryArguments);
	}

	@Override
	public Collection<KnowledgeObject> getAllKnowledgeObjects() {
		Branch contextBranch = getContextBranch();
		RevisionQueryArguments queryArguments = Expressions.createArgumentWithBranch(contextBranch);
		return _expressions.anyObjects.search(queryArguments);
	}

	@Override
	public Collection<KnowledgeAssociation> getAllKnowledgeAssociations() {
		Branch contextBranch = getContextBranch();
		RevisionQueryArguments queryArguments = Expressions.createArgumentWithBranch(contextBranch);
		return _expressions.anyAssociations.search(queryArguments);
    }

	@Override
	public DataObject getObjectByAttribute(String typeName, String attrName, Object value) {
		MOKnowledgeItem table = lookupType(typeName);
		
		SimpleQuery<KnowledgeItem> query = SimpleQuery.queryUnresolved(KnowledgeItem.class, table,
			getValueMatch(table, attrName, value), RangeParam.first);
		CompiledQuery<KnowledgeItem> compiledQuery = compileSimpleQuery(query);
		try (CloseableIterator<KnowledgeItem> results = compiledQuery.searchStream()) {
			if (results.hasNext()) {
				KnowledgeItem result = results.next();
				if (results.hasNext()) {
					throw new IllegalStateException(
						"There is more than one object in table '" + typeName + "' with value '" + value
							+ "' in attribute '" + attrName + "', only one such object is expected.");
				}
				return result;
			} else {
				return null;
			}
		}
	}

	private Expression getValueMatch(MOKnowledgeItem table, String attrName, Object value) {
		MOAttribute attr = table.getAttributeOrNull(attrName);
		if (attr != null) {
			return DBKnowledgeBase.createValueMatch(attr, value);
		} else {
			return DBKnowledgeBase.createFlexValueMatch(attrName, value);
		}

	}

	private <T> List<T> executeSimpleQuery(SimpleQuery<T> query, RevisionQueryArguments arguments) {
		CompiledQuery<T> compiledQuery = compileSimpleQuery(query);
		return compiledQuery.search(arguments);
	}

	/**
	 * Find an already cached version for the given result fetched from the DB.
	 * 
	 * <p>
	 * An already cached copy of the given item may be updated in the current context and either no
	 * longer match the given search criterion, or now match the search criterion, while no result
	 * was found in the DB. In any case, the most current version that matches the given search
	 * criterion is returned.
	 * </p>
	 * 
	 * <p>
	 * If the requested history context is not {@link Revision#CURRENT}, the given result from the
	 * DB is always returned unmodified since there cannot be a more current version in the current
	 * context.
	 * </p>
	 * 
	 * @param dataBranchContext
	 *        The branch from which the data was fetched.
	 * @param requestedHistoryContext
	 *        The {@link Revision#getCommitNumber()} of the revision that was searched.
	 * @param search
	 *        The criterion that the result must match. If a more current version is found in the
	 *        current context, it is checked, that it still matches the search criterion.
	 * @param fetchedItem
	 *        The item as returned from the {@link DBAccess} layer or the cached version of it.
	 * 
	 * @return The given item if it still matches the search expression, or some other item in the
	 *         context matching the search expression otherwise.
	 */
	private KnowledgeItemInternal toSearchResult(DBContext context, long dataBranchContext,
			long requestedHistoryContext, final Expression search, KnowledgeItemInternal fetchedItem) {
		// Process search result.
		if (requestedHistoryContext == Revision.CURRENT_REV) {
			if (context == null) {
				return fetchedItem;
			}
			
			if (fetchedItem != null) {
				if (!context.isPersistentItemModified(fetchedItem)) {
					// no local changes which may violate the search expression.
					return fetchedItem;
				}
				
				// Note: An object only has an identity after it is
				// inserted into the cache.
				DBObjectKey foundIdentity = fetchedItem.tId();
				if (isLocallyRemoved(context, foundIdentity)) {
					// Proceed as if nothing was found in the storage. Search in
					// context, whether another (new or modified) item now
					// matches the search criteria.
				} else {
					// Check, whether the current version still matches the
					// search criterion.
					if (SimpleExpressionEvaluator.matches(search, fetchedItem)) {
						return fetchedItem;
					} else {
						// Search another item in context.
					}
				}
			} else {
				// Search in context.
			}

			// No result was found in the database. Search within the cached
			// objects in the local context.
			DBKnowledgeItem matchingNewObject = findFirst(dataBranchContext, search, context.getNewObjects());
			if (matchingNewObject != null) {
				return matchingNewObject;
			}

			DBKnowledgeItem matchingUpdatedObject = findFirst(dataBranchContext, search, context.getChangedObjects());
			if (matchingUpdatedObject != null) {
				return matchingUpdatedObject;
			}

			// Noting found.
			return null;
		} else {
			// No post-processing required for historic immutable objects.
			return fetchedItem;
		}
	}

	private static DBKnowledgeItem findFirst(long branchContext, Expression search,
			Collection<? extends DBKnowledgeItem> source) {
		for (DBKnowledgeItem item : source) {
			if (branchContext != item.getBranchContext()) {
				continue;
			}
			if (SimpleExpressionEvaluator.matches(search, item)) {
				return item;
			}
		}
		return null;
	}

	@Override
	public final List getObjectsByAttribute(Branch branch, Revision revision, String type, String attrName, Object value) {
		long historyContext = revision.getCommitNumber();
		return internalGetObjectsByAttribute(branch, historyContext, KnowledgeItem.class, type, attrName, value);
	}

	private <E> List<E> internalGetObjectsByAttribute(Branch contextBranch, long historyContext, Class<E> expectedType,
			String type,
			String attrName, Object value) {
		MOKnowledgeItem table = lookupType(type);
		if (table.isAbstract()) {
			// No such type or abstract
			return Collections.emptyList();
		}
		
		final Expression eq = getValueMatch(table, attrName, value);
		SimpleQuery<E> simpleQuery = SimpleQuery.queryUnresolved(expectedType, table, eq);
		RevisionQueryArguments revisionArgs = revisionArgs();
		revisionArgs.setRequestedBranch(contextBranch);
		revisionArgs.setRequestedRevision(historyContext);
		return executeSimpleQuery(simpleQuery, revisionArgs);
	}

	@Override
	public Map<?, List<LongRange>> search(HistoryQuery query) {
		return search(query, ExpressionFactory.historyArgs());
	}
	
	@Override
	public Map<?, List<LongRange>> search(HistoryQuery query, HistoryQueryArguments queryArguments) {
		Object[] arguments = queryArguments.getArguments();
		BranchParam branchParam = query.getBranchParam();
		Branch requestedBranch = QueryArguments.resolveRequestedBranch(queryArguments, branchParam, this);
		int stopRow = queryArguments.getStopRow();
		
		checkQuery(query, arguments);
		
		Map<MetaObject, HistoryQuery> monomorphicSearches = preprocessSearch(query);
		
		if (monomorphicSearches.isEmpty()) {
			return Collections.emptyMap();
		}
		
		Map<ObjectBranchId, List<LongRange>> result = null;
		
		/* search in database occurs in database current, i.e. it may contain results that must not
		 * be visible to this KnowledgeBase. Therefore the result must be adapted: 1. If some object
		 * matches at the last KnowledgeBase revision, it matches from that time forever (it seems
		 * for this KnowledgeBase). 2. If some object does not match at this time, it does also not
		 * match at any later time. */
		// fetch here to be stable for complete search
		long currentRevision = getSessionRevision();

		PooledConnection readConnection = connectionPool.borrowReadConnection();
		try {
			for (Iterator<Entry<MetaObject, HistoryQuery>> it = monomorphicSearches.entrySet().iterator(); it.hasNext(); ) {
				Entry<MetaObject, HistoryQuery> entry = it.next();
				HistoryQuery monomorphicQuery = entry.getValue();
				Object[] enhancedArguments =
					addInternalArguments(monomorphicQuery, requestedBranch, Revision.CURRENT_REV, Revision.CURRENT_REV,
						0, -1, arguments);

				// TODO: Transform order.
				List<SQLOrder> orderBy = new ArrayList<>();
				List<HistorySearch> searches = SQLBuilder.createHistorySearches(this.moRepository, monomorphicQuery);
				for (HistorySearch search : searches) {
					try {
						Map<ObjectBranchId, List<LongRange>> partialResult = 
							fetchLifePeriods(readConnection, search, stopRow,
								monomorphicQuery.getArgumentIndexByName(), enhancedArguments);

						if (result == null) {
							result = partialResult;
						} else {
							for (Entry<ObjectBranchId, List<LongRange>> entry2 : partialResult.entrySet()) {
								List<LongRange> clash = result.put(entry2.getKey(), entry2.getValue());
								if (clash != null) {
									result.put(entry2.getKey(), LongRangeSet.union(clash, entry2.getValue()));
								}
							}
						}
					} catch (SQLException ex) {
						throw new KnowledgeBaseRuntimeException(ex);
					}
				}
			}
		} finally {
			connectionPool.releaseReadConnection(readConnection);
		}
		
		assert result != null : "At least one search was executed.";
		adaptToCurrentRevision(result, currentRevision);
		return result;
	}

	/**
	 * Adapts the result based on database current to a result according to the current revision of
	 * this {@link KnowledgeBase}.
	 */
	private void adaptToCurrentRevision(Map<ObjectBranchId, List<LongRange>> result, long currentRevision) {
		Iterator<Entry<ObjectBranchId, List<LongRange>>> resultIt = result.entrySet().iterator();
		while (resultIt.hasNext()) {
			Entry<ObjectBranchId, List<LongRange>> resultEntry = resultIt.next();
			List<LongRange> lifePeriods = resultEntry.getValue();
			if (lifePeriods.isEmpty()) {
				assert false : "Found Object that never matches the query.";
				continue;
			}
			
			if (lifePeriods.get(0).getStartValue() > currentRevision) {
				// Object was created or matches query first time in a later revision. This result
				// can not be visible by this KB.
				resultIt.remove();
				continue;
			}

			// Find latest LongRange which is visible by this KnowledgeBase, i.e. the latest
			// LongRange which has a start <= the current revision.
			int index = lifePeriods.size() - 1;
			LongRange lifePeriod;
			do {
				lifePeriod = lifePeriods.get(index);
				if (lifePeriod.getStartValue() <= currentRevision) {
					// lifePeriods.get(0).getStartValue() <= currentRevision so break occurs
					break;
				} else {
					index--;
				}
			} while (index >= 0);
			long endValue = lifePeriod.getEndValue();
			if (endValue == Long.MAX_VALUE) {
				assert index == lifePeriods.size() -1;
				// no changes in database after KnowledgeBase current:
				// [current, Long.Max] \subset lifePeriod
				continue;
			}

			if (endValue + 1 > currentRevision) {
				// Object matched at revision time
				resultEntry.setValue(LongRangeSet.union(lifePeriods, LongRangeSet.endSection(currentRevision)));
			} else {
				if (index == lifePeriods.size() - 1) {
					// last period ends before KnowledgeBase current, i.e. life period is correct.
					// lifePeriod < [current, Long.Max]
					continue;
				} else {
					// Object has not matched at revision time
					resultEntry.setValue(LongRangeSet.intersect(lifePeriods, LongRangeSet.startSection(endValue)));
				}
			}

		}
	}

	@Override
	public <E> List<E> search(RevisionQuery<E> query) {
		return search(query, ExpressionFactory.revisionArgs());
	}
	
	@Override
	public <E> List<E> search(RevisionQuery<E> query, RevisionQueryArguments queryArguments) {
		try (CloseableIterator<E> stream = searchStream(query, queryArguments)) {
			return toList(stream);
		}
	}

	/**
	 * Adds all elements of the iterator to a list and closes it.
	 * <p>
	 * Should be called directly after creating stream
	 * </p>
	 */
	private static <E> List<E> toList(CloseableIterator<E> stream) {
		ArrayList<E> result = new ArrayList<>();
		while (stream.hasNext()) {
			result.add(stream.next());
		}
		return result;
	}

	@Override
	public <E> CloseableIterator<E> searchStream(RevisionQuery<E> query) {
		return searchStream(query, ExpressionFactory.revisionArgs());
	}

	@Override
	public <E> CloseableIterator<E> searchStream(final RevisionQuery<E> query,
			RevisionQueryArguments queryArguments) {
		checkQuery(query, queryArguments.getArguments());
		
		CompiledQuery<E> compiledQuery = compileQuery(query);
		final ConnectionPool pool = getConnectionPool();
		boolean statementReturned = false;
		final PooledConnection readConnection = pool.borrowReadConnection();
		try {
			final CloseableIterator<E> searchResult = compiledQuery.searchStream(readConnection, queryArguments);
			CloseableIteratorAdapter<E> adaptedResult = new CloseableIteratorAdapter<>(searchResult) {

				@Override
				protected void internalClose() {
					try {
						searchResult.close();
					} finally {
						pool.releaseReadConnection(readConnection);
					}
				}
			};
			statementReturned = true;
			return adaptedResult;
		} finally {
			if (!statementReturned) {
				pool.releaseReadConnection(readConnection);
			}
		}
	}

	@Override
	public <E> CompiledQuery<E> compileQuery(RevisionQuery<E> query) {
		Map<MetaObject, RevisionQuery<E>> monomorphicSearches = preprocessSearch(query);
		
		CompiledQuery<E> result;
		switch (monomorphicSearches.size()) {
			case 0: {
				result = EmptyCompiledQuery.getInstance();
				break;
			}
			case 1: {
				Entry<MetaObject, RevisionQuery<E>> entry = monomorphicSearches.entrySet().iterator().next();
				result = compileMonomorphicQuery((MOKnowledgeItemImpl) entry.getKey(), entry.getValue());
				break;
			}
			default: {
				List<CompiledQuery<E>> monomorphicQueries = new ArrayList<>();
				for (Entry<MetaObject, RevisionQuery<E>> entry : monomorphicSearches.entrySet()) {
					MOKnowledgeItemImpl resultType = (MOKnowledgeItemImpl) entry.getKey();
					CompiledQuery<E> compiledQuery = compileMonomorphicQuery(resultType, entry.getValue());
					monomorphicQueries.add(compiledQuery);
				}
				result = new ConcatenatedCompiledQuery<>(getConnectionPool(), monomorphicQueries);
				break;
			}
		}
		return result;

	}

	private <E> CompiledQuery<E> compileMonomorphicQuery(MOKnowledgeItemImpl resultType,
			RevisionQuery<E> monomorphicQuery) {

		boolean fullLoad = fullLoad(resultType, monomorphicQuery);
		SQLPart select = SQLBuilder.createRevisionSearchSQL(dbHelper, moRepository, monomorphicQuery, fullLoad);

		Map<String, Integer> argumentIndexByName = monomorphicQuery.getArgumentIndexByName();
		final CompiledStatement sql = SQLQuery.toSql(dbHelper, select, argumentIndexByName);

		return new MonomorphicSearch<>(this, resultType, monomorphicQuery, sql, fullLoad);
	}

	private boolean fullLoad(MOKnowledgeItemImpl resultType, RevisionQuery<?> monomorphicQuery) {
		LoadStrategy loadStrategy = monomorphicQuery.getLoadStrategy();
		if (loadStrategy == LoadStrategy.DEFAULT) {
			if (monomorphicQuery.getRangeParam() == RangeParam.first) {
				return true;
			} else {
				return resultType.getFullLoad();
			}
		} else {
			return loadStrategy.getFullLoad();
		}
	}

	private void checkQuery(AbstractQuery<?> query, Object[] arguments) {
		if (query.getArgumentIndexByName().size() != arguments.length) {
			throw new IllegalArgumentException("Argument count mismatch, expected '" + query.getArgumentIndexByName().size() + "', got '" + arguments.length + "'.");
		}
	}

	private <Q extends AbstractQuery<?>> Map<MetaObject, Q> preprocessSearch(Q search) {
		int verbosity = Logger.isDebugEnabled(DBKnowledgeBase.class) ? Protocol.DEBUG : Protocol.INFO;
		BufferingProtocol inner = new BufferingProtocol();
		inner.setVerbosity(verbosity);
		ExpressionCompileProtocol protocol = new ExpressionCompileProtocol(inner);

		Map<MetaObject, Q> monomorphicSearches;
		synchronized (search) {
			/* Processing query modifies it internally. If the query is cached static, it must be
			 * ensured that no concurrent modification occurs. */
			processSearchPhase0(search, protocol);

			processSearchPhase1(search, protocol);
			processSearchPhase2(search, protocol);

			search.setSearch(ExpressionSplitter.makeTableAccessUnique(search.getSearch()));

			processSearchPhase1(search, protocol);
			processSearchPhase2(search, protocol);
			
			monomorphicSearches = UnionSplitter.splitUnions(protocol, search);
		}
		protocol.checkErrors();

		if (monomorphicSearches.isEmpty()) {
			return Collections.emptyMap();
		}
		
		for (Entry<MetaObject, Q> entry : monomorphicSearches.entrySet()) {
			Q monomorphicSearch = entry.getValue();
			
			processSearchPhase2(monomorphicSearch, protocol);

			switch (monomorphicSearch.getBranchParam()) {
				case single:
					// need hint for javac compiler
					monomorphicSearch = BranchExpressionEnhancement.<Q> addBranchExpressions(monomorphicSearch);
					protocol.checkErrors();
					entry.setValue(monomorphicSearch);
					typeAnalysis(monomorphicSearch, protocol);
					break;
				default:
					break;
			}
			
			SymbolCreator.computeSymbols(this.moRepository, protocol, monomorphicSearch);
			protocol.checkErrors();
			
			entry.setValue(monomorphicSearch);
		}
		return monomorphicSearches;
	}

	@Override
	public <E> CompiledQuery<E> compileSimpleQuery(final SimpleQuery<E> simpleQuery) {
		final RevisionQuery<E> query = simpleQuery.toRevisionQuery(this);

		final CompiledQuery<E> searchQuery = compileQuery(query);

		CompiledQuery<E> result = new BufferedCompiledQuery<>(getConnectionPool()) {

			@Override
			public List<E> search(PooledConnection connection, RevisionQueryArguments arguments) {
				List<E> dbResult = searchQuery.search(connection, arguments);
				DBKnowledgeBase.this.adaptToTransaction(simpleQuery, query, arguments, dbResult, true);
				return dbResult;
			}
		};
		
		return result;
	}

	private Map<ObjectBranchId, List<LongRange>> fetchLifePeriods(PooledConnection readConnection, HistorySearch search, int stopRow, Map<String, Integer> argumentIndexByName, Object[] arguments) throws SQLException {
		CompiledStatement sql = SQLQuery.toSql(this.dbHelper, search.getSelect(), argumentIndexByName);
		
		ResultSetReader<ObjectBranchId> objectIdReader = search.getObjectIdReader();
		ResultSetReader<List<LongRange>> lifePeriodReader = search.getLifePeriodReader();
		
		DBHelper sqlDialect = dbHelper;
		int retry = sqlDialect.retryCount();
		while (true) {
			try {
				try (ResultSet resultSet = sql.executeQuery(readConnection, arguments)) {
					// Install context to readers.
					objectIdReader.setResultSet(resultSet);
					lifePeriodReader.setResultSet(resultSet);
					
					int cnt = (stopRow >= 0) ? stopRow : Integer.MAX_VALUE;
					HashMap<ObjectBranchId, List<LongRange>> partialResult =
						new HashMap<>();
					while (resultSet.next() && (cnt > 0)) {
						ObjectBranchId objectId = objectIdReader.read();
						List<LongRange> lifePeriod = lifePeriodReader.read();
						
						// Fetch the object.
						List<LongRange> clash = partialResult.put(objectId, lifePeriod);
						if (clash != null) {
							partialResult.put(objectId, LongRangeSet.union(clash, lifePeriod));
						}
						cnt--;
					}
					return partialResult;
				}
			} catch (SQLException ex) {
				readConnection.closeConnection(ex);
				if ((retry <= 0) || (! sqlDialect.canRetry(ex))) {
					throw ex;
				}
				retry--;
			}
		}
	}

	private void processSearchPhase0(AbstractQuery<?> query, ExpressionCompileProtocol protocol) {
		boolean debugEnabled = Logger.isDebugEnabled(DBKnowledgeBase.class);
		if (debugEnabled) {
			Logger.debug("Query input: " + ExpressionPrinter.toString(query), DBKnowledgeBase.class);
		}
		query.setSearch(SetSimplification.simplifySets(query.getSearch()));

		TypeBinding.bindTypes(this.moRepository, protocol, query);
		protocol.checkErrors();
		
		query.setSearch(AnyExpansion.expandAny(this.moRepository, query.getSearch()));
		if (debugEnabled) {
			Logger.debug("Query any: " + ExpressionPrinter.toString(query), DBKnowledgeBase.class);
		}
	}

	private void processSearchPhase1(AbstractQuery<?> query, ExpressionCompileProtocol protocol) {
		query.setSearch(UnionExtraction.extractUnions(query.getSearch()));
		if (Logger.isDebugEnabled(DBKnowledgeBase.class)) {
			Logger.debug("Query union: " + ExpressionPrinter.toString(query.getSearch()), DBKnowledgeBase.class);
		}
		protocol.checkErrors();
	}

	private void processSearchPhase2(AbstractQuery<?> query, ExpressionCompileProtocol protocol) {
		query.setSearch(SetExpressionNormalization.normalizeSets(query.getSearch()));
		boolean debugEnabled = Logger.isDebugEnabled(DBKnowledgeBase.class);
		if (debugEnabled) {
			Logger.debug("Query normalized sets: " + ExpressionPrinter.toString(query.getSearch()),
				DBKnowledgeBase.class);
		}
		
		ExpressionNormalization.normalizeExpressions(query);
		if (debugEnabled) {
			Logger.debug("Query normalized expressions: " + ExpressionPrinter.toString(query.getSearch()),
				DBKnowledgeBase.class);
		}

		typeAnalysis(query, protocol);

		query.setSearch(ConstantFolding.foldConstants(query.getSearch()));
		if (debugEnabled) {
			Logger.debug("Query simplified: " + ExpressionPrinter.toString(query.getSearch()), DBKnowledgeBase.class);
		}

		query.setSearch(NullReferenceSimplifier.simplifyNullReferences(query.getSearch()));
		if (debugEnabled) {
			Logger.debug("Simplified null: " + ExpressionPrinter.toString(query.getSearch()), DBKnowledgeBase.class);
		}
	}

	void typeAnalysis(AbstractQuery<?> query, ExpressionCompileProtocol protocol) {
		TypeBinding.bindTypes(this.moRepository, protocol, query);
		protocol.checkErrors();
		PolymorphicTypeComputation.computeTypes(this.moRepository, protocol, query);
		protocol.checkErrors();
		ConcreteTypeComputation.computeTypes(this.moRepository, protocol, query);
		protocol.checkErrors();
	}

	@Override
	public CompiledQueryCache getQueryCache() {
		return _queryCache;
	}

	@Override
	public Iterator<KnowledgeItem> getObjectsByAttribute(String type, String attrName, Object value) {
		Branch contextBranch = this.getContextBranch();
		return internalGetObjectsByAttribute(contextBranch, Revision.CURRENT_REV, KnowledgeItem.class, type, attrName,
			value).iterator();
    }
    
	@Override
	public Iterator<KnowledgeItem> getObjectsByAttribute(String type, String[] attributeNames, Object[] values) {
		Branch contextBranch = this.getContextBranch();
		
		return getObjectsByAttributes(contextBranch, Revision.CURRENT, type, attributeNames, values).iterator();
	}

	@Override
	public List getObjectsByAttributes(Branch contextBranch, Revision revision, String type, String[] attributeNames,
			Object[] values) {
		MOKnowledgeItem table = lookupType(type);
		
		// Build search expression.
		Expression search = InternalExpressionFactory.hasTypeTyped(table);
		if (attributeNames != null) {
			for (int n = 0, cnt = values.length; n < cnt; n++) {
				Expression match = getValueMatch(table, attributeNames[n], values[n]);
				search = ExpressionFactory.and(search, match);
			}
		}
		SimpleQuery<KnowledgeItem> simpleQuery = SimpleQuery.queryUnresolved(KnowledgeItem.class, table, search);
		return executeSimpleQuery(simpleQuery, revisionArgs());
	}

    /**
	 * Remove the given item with the next {@link #commit()}.
	 */
    @Override
	public void delete(KnowledgeItem item) throws DataObjectException {
    	item.delete();
    }

	@Override
	public void deleteAll(Collection<? extends KnowledgeItem> items) throws DataObjectException {
		KnowledgeObjectImpl.deleteAll(this, items);
	}

	@Override
	public Branch createBranch(Branch baseBranch, Revision baseRevision, Set<? extends MetaObject> branchedTypes) {
    	if (baseBranch == null) {
    		throw new IllegalArgumentException("Base branch must not be null.");
    	}
    	return createBranch((BranchImpl) baseBranch, baseRevision.getCommitNumber(), branchedTypes);
    }
    
	/* package protected */ Branch createBranch(BranchImpl baseBranch, long baseRevision,
			Set<? extends MetaObject> branchedObjectTypes) {
    	// Direct commit independent of current context. 
    	PooledConnection commitConnection = connectionPool.borrowWriteConnection();
    	try {
    		// Reserve identifiers.
			long newBranchId =
				sequenceManager.nextSequenceNumber(dbHelper, commitConnection, dbHelper.retryCount(), BRANCH_SEQUENCE);
			RevisionImpl createRevision =
				createRevision(commitConnection, Messages.BRANCH_CREATED__ID.fill(Long.valueOf(newBranchId)));
			long createRev = createRevision.getCommitNumber();
        	
			UpdateChainLink link = null;
			beginCommit(createRev);
    		try {
            	BranchImpl newBranch = doCreateBranch(commitConnection,
						baseBranch, baseRevision, createRev,
					newBranchId, branchedObjectTypes);

            	commitConnection.commit();
            	
        		synchronized (cache) {
					syncCacheCleanupReferences();

					syncCacheInsertCache(newBranch, createRev);
        		}
            	
				link = new UpdateChainLink(newBranchEvent(baseBranch, newBranch, createRevision));
    			
    			return newBranch;
    		} finally {
				endCommit(link);
    		}
		} catch (SQLException e) {
			throw new KnowledgeBaseRuntimeException(e);
		} catch (RefetchTimeout ex) {
			String message = "Commit aborted in response to long running concurrent refetch.";
			throw new KnowledgeBaseRuntimeException(message, ex);
		} catch (MergeConflictException ex) {
			String message = "Commit aborted, because local changes come in conflict with former changes.";
			throw new KnowledgeBaseRuntimeException(message, ex);
		} finally {
			connectionPool.releaseWriteConnection(commitConnection);
		}
    }

	private UpdateEvent newBranchEvent(BranchImpl baseBranch, BranchImpl newBranch, RevisionImpl createRevision) {

		// TODO: Since branch objects are no regular object, the newly created
		// branch is not explicitly announced. This decision should be
		// reconsidered.
		long createRev = createRevision.getCommitNumber();
		Map<ObjectKey, KnowledgeItem> noItems = Collections.<ObjectKey, KnowledgeItem> emptyMap();

		ChangeSet changeSet = new ChangeSet(createRev);

		long baseBranchId;
		if (baseBranch != null) {
			baseBranchId = baseBranch.getBranchId();
		} else {
			// Trunk has no base branch.
			baseBranchId = 0;
		}
		BranchEvent branchEvent =
			new BranchEvent(createRev, newBranch.getBranchId(), baseBranchId, newBranch.baseRevision());
		branchEvent.setBranchedTypeNames(newBranch.getBranchedTypes());
		changeSet.addBranchEvent(branchEvent);

		changeSet.setCommit(CommitEvent.newCommitEvent(createRevision));
		return new UpdateEvent(this, false, createRev, noItems, noItems, noItems, changeSet);
	}

	BranchImpl doCreateBranch(PooledConnection commitConnection,
			BranchImpl baseBranch, long baseRevision,
			long createRev, long newBranchId, Set<? extends MetaObject> branchedObjectTypes)
			throws SQLException {
		
		long baseBranchId = baseBranch == null ? BranchImpl.NO_BASE_BRANCH : baseBranch.getBranchId();
		
		Set<String> branchedObjectTypeNames = new HashSet<>();
		Set<MetaObject> unbranchedObjectTypes;
		// Null is short-hand for "all types".
		if (branchedObjectTypes == null) {
			List<? extends MetaObject> objectTypes = getObjectTypes();
			HashSet<MetaObject> branchableTypes = CollectionUtil.newSet(objectTypes.size());
			for (MetaObject type : objectTypes) {
				if ((((MOStructure) type).getDBMapping()).multipleBranches()) {
					branchableTypes.add(type);
					branchedObjectTypeNames.add(type.getName());
				}
			}
			branchedObjectTypes = branchableTypes;
			unbranchedObjectTypes = Collections.emptySet();
		} else {
			for (Iterator<? extends MetaObject> it = branchedObjectTypes.iterator(); it.hasNext();) {
				MetaObject type = it.next();
				if (type.getKind() != Kind.item) {
					throw new IllegalArgumentException("Not an item type: " + type.getName());
				}
				if (!(((MOStructure) type).getDBMapping()).multipleBranches()) {
					throw new IllegalArgumentException("Type " + type.getName() + " does not support branching.");
				}
				branchedObjectTypeNames.add(type.getName());
			}
			unbranchedObjectTypes = new HashSet<>(getObjectTypes());
			unbranchedObjectTypes.removeAll(branchedObjectTypes);
		}
		
		if (baseBranchId == BranchImpl.NO_BASE_BRANCH && (!branchedObjectTypes.isEmpty())) {
			throw new IllegalArgumentException("Cannot branch types without base branch.");
		}
		
		// Prepare initial switch base map.
		Map<MetaObject, Long> baseBranchIdByType;
		if (baseBranch == null) {
			// This is the creation of TRUNK during table setup. Mark
			// all types to have data in TRUNK.
			baseBranchIdByType = new HashMap<>();
			for (Iterator<? extends MetaObject> it = getObjectTypes().iterator(); it.hasNext();) {
				MetaObject type = it.next();
				baseBranchIdByType.put(type, Long.valueOf(newBranchId));
			}
		} else {
			boolean associationCheckRequired = branchedObjectTypes.size() > 0 && unbranchedObjectTypes.size() > 0;

			if (associationCheckRequired) {
				// check that no associations exists which would lead to branch crossing references
				checkBranchCrossingReferences(commitConnection, baseBranch, baseRevision, branchedObjectTypes,
					branchedObjectTypeNames);
				checkBranchCrossingValueTypes(commitConnection, baseBranch, baseRevision, getValueTypes(),
					branchedObjectTypeNames);
			}

			baseBranchIdByType = baseBranch.getBaseBranchIdByNonSystemType();

			// Branch all requested types.
			branchTypes(branchedObjectTypes, baseRevision, commitConnection, newBranchId, createRev, baseBranchIdByType);
			// Branch all value types.
			branchValueTypes(getValueTypes(), baseRevision, commitConnection, baseBranchId, newBranchId, createRev,
				branchedObjectTypeNames, associationCheckRequired);
			versionedDataManager.branch(commitConnection, newBranchId, createRev, baseBranchId, baseRevision,
				branchedObjectTypeNames);
		}
		
		// Store the base branch for type information.
		for (Iterator<Entry<MetaObject, Long>> it = baseBranchIdByType.entrySet().iterator(); it.hasNext();) {
			Entry<MetaObject, Long> entry = it.next();
			
			MetaObject type = entry.getKey();
			Long baseBranchForTypeId = entry.getValue();
			
			// Branches are creates without using the current transaction.
			DBContext context = null;
			AbstractDBKnowledgeItem switchLink =
				BranchSupport.newBranchSwitchLink(this, context, newBranchId, type, baseBranchForTypeId);
			MOKnowledgeItemImpl linkType = (MOKnowledgeItemImpl) switchLink.tTable();
			linkType.getDBAccess().insert(commitConnection, createRev, switchLink);
		}

		// Create branch object instance.
		MOKnowledgeItemImpl branchType = getBranchType();
		AbstractDBKnowledgeItem branchItem = newImmutableItem(branchType);
		BranchImpl newBranch = (BranchImpl) branchItem;
		newBranch.initNew(newBranchId, createRev, baseBranchId, baseRevision, baseBranchIdByType);
		
		branchType.getDBAccess().insert(commitConnection, createRev, branchItem);

		return newBranch;
	}

	private void checkBranchCrossingValueTypes(PooledConnection connection, BranchImpl baseBranch,
			long baseRevision, List<? extends MetaObject> valueTypes, Collection<String> branchedObjectTypeNames) {
		for (MetaObject branchedObjectType : valueTypes) {
			CompiledQuery<?> compiledQuery = _expressions.branchCrossingValuesQuery(branchedObjectType);
			RevisionQueryArguments arguments =
				_expressions.branchCrossingValuesArguments(baseBranch, baseRevision, branchedObjectTypeNames);

			try (CloseableIterator<?> branchCrossingReferences = compiledQuery.searchStream(connection, arguments)) {
				if (branchCrossingReferences.hasNext()) {
					StringBuilder error = new StringBuilder();
					error.append("There are association links between branched and unbranched objects: ");
					error.append(branchCrossingReferences.next());
					throw new IllegalArgumentException(error.toString());
				}
			}
		}

	}

	/**
	 * Check, whether all association links are either both branched, or both unbranched.
	 * 
	 * @throws IllegalArgumentException
	 *         If there is an association link whose one end is branched, while the other end is not
	 *         branched.
	 */
	private void checkBranchCrossingReferences(PooledConnection connection, Branch baseBranch, long baseRevision,
			Collection<? extends MetaObject> branchedObjectTypes, Collection<String> branchedObjectTypeNames)
			throws IllegalArgumentException {

		for (MetaObject branchedObjectType : branchedObjectTypes) {
			List<? extends MOReference> references = MetaObjectUtils.getReferences(branchedObjectType);
			for (MOReference reference : references) {
				if (reference.getHistoryType() != HistoryType.CURRENT) {
					continue;
				}
				if (!reference.isBranchGlobal() && reference.isMonomorphic()) {
					if (!branchedObjectTypeNames.contains(reference.getMetaObject().getName())) {
						StringBuilder error = new StringBuilder();
						error.append("The type to branch ");
						error.append(branchedObjectType);
						error.append(" has a branch local, current, monomorphic reference of unbranched type ");
						error.append(reference.getMetaObject());
						throw new IllegalArgumentException(error.toString());

					}
				}
			}
		}

		for (MetaObject branchedObjectType : branchedObjectTypes) {
			CompiledQuery<?> compiledQuery = _expressions.branchCrossingReferenceQuery(branchedObjectType);
			RevisionQueryArguments arguments =
				_expressions.branchCrossingRefererenceArguments(baseBranch, baseRevision, branchedObjectTypeNames);

			try (CloseableIterator<?> branchCrossingReferences = compiledQuery.searchStream(connection, arguments)) {
				if (branchCrossingReferences.hasNext()) {
					StringBuilder error = new StringBuilder();
					error.append("There are references from a branched to an unbranched object: ");
					error.append(branchCrossingReferences.next());
					throw new IllegalArgumentException(error.toString());
				}
			}
		}

	}

	private void branchValueTypes(Collection<? extends MetaObject> valueTypes, long baseRevision,
			PooledConnection context, long baseBranchId, long newBranchId, long createRev,
			Collection<String> branchedObjectTypeNames, boolean associationCheck) throws SQLException {
		
		for (Iterator<? extends MetaObject> it = valueTypes.iterator(); it.hasNext();) {
			MOKnowledgeItemImpl type = (MOKnowledgeItemImpl) it.next();
			
			if (! moRepository.containsMetaObject(type)) {
				throw new IllegalArgumentException("Unknown type '" + type.getName() + "'.");
			}
			
			if (type.isSystem() || type.isAbstract()) {
				// Happens, if the inverse set "all types but some types" is built.
				continue;
			}
			
			SQLExpression filterExpr;
			if (associationCheck) {
				filterExpr = _expressions.createBranchFilter(type, branchedObjectTypeNames);
			} else {
				filterExpr = SQLFactory.literalTrueLogical();
			}
			branchType(context, newBranchId, createRev, baseBranchId, baseRevision, type, filterExpr);
		}
	}

	private void branchTypes(Collection<? extends MetaObject> branchedTypes, long baseRevision,
			PooledConnection context, long newBranchId, long createRev, Map<MetaObject, Long> baseBranchIdByType)
			throws SQLException {

		for (Iterator<? extends MetaObject> it = branchedTypes.iterator(); it.hasNext();) {
			MOKnowledgeItemImpl type = (MOKnowledgeItemImpl) it.next();

			if (!moRepository.containsMetaObject(type)) {
				throw new IllegalArgumentException("Unknown type '" + type.getName() + "'.");
			}

			if (type.isSystem() || type.isAbstract()) {
				// Happens, if the inverse set "all types but some types" is built.
				continue;
			}

			Long baseBranchIdForType = baseBranchIdByType.put(type, Long.valueOf(newBranchId));

			branchType(context, newBranchId, createRev, baseBranchIdForType.longValue(), baseRevision, type,
				SQLFactory.literalTrueLogical());
		}
	}

	private void branchType(PooledConnection context, long branchId,
			long createRev, long baseBranchId, long baseRevision,
			MOKnowledgeItemImpl type, SQLExpression filterExpr) throws SQLException {
		DBAccess access = type.getDBAccess();
		
		access.branch(context, branchId, createRev, baseBranchId, baseRevision, filterExpr);
	}
	
	@Override
	public Branch getTrunk() {
		assert _trunk != null : "No Trunk available, KnowledgeBase was not started.";
		return _trunk;
	}
	
	@Override
	public Branch getContextBranch() {
		return findCurrentBranch(true);
	}

	/**
	 * Searches for the current branch and (if requested) installs it via
	 * {@link #setCurrentBranch(Branch)}
	 */
	private Branch findCurrentBranch(boolean install) {
		Branch current = currentInteraction().get(_currentBranch);
		if (current != null) {
			return current;
		}

		TLContext theContext = TLContext.getContext();
		if (theContext == null) {
			return getTrunk();
		}

		Branch newCurrentBranch;
		// Install the thread-local context branch from the session context.
		Branch sessionBranch = theContext.getSessionBranch(this);
		if (sessionBranch != null) {
			newCurrentBranch = sessionBranch;
		} else {
			// no session branch was set
			newCurrentBranch = getTrunk();
		}
		if (install) {
			/* Initialize thread local context branch to guarantee that all following calls to
			 * getContextBranch() return the same branch. The context branch must be stable to
			 * shield internal APIs from concurrently switching the context branch. */
			setCurrentBranch(newCurrentBranch);
		}

		return newCurrentBranch;
	}

	private void setCurrentBranch(Branch result) {
		assert TLContext.getContext() != null : "No context setup. If no context is given no one informs the KB to remove its value";
		currentInteraction().set(_currentBranch, result);
	}
	
	@Override
	public Branch setContextBranch(Branch branch) {
		TLContext theContext = TLContext.getContext();
		if (theContext == null) {
			// Must not set the thread local context branch, because this would
			// not be dropped when the thread leaves its current scope.
			
			// Must not create a context, because this context would not have a
			// context ID. In such context, DB access fails.
			throw new IllegalStateException("No context set up.");
		}
		if (branch != null) {
			if (branch.getHistoryManager() != this) {
				throw new IllegalArgumentException("Branch '" + this + "' was not created by '" + this + "'");
			}
		}
		Branch oldBranch = findCurrentBranch(false);
		setCurrentBranch(branch);
		return oldBranch;
	}

	private static class BranchReference extends SoftReference<Branch> {

		private Long id;

		public BranchReference(Branch referent, ReferenceQueue<Branch> q) {
			super(referent, q);
			
			this.id = Long.valueOf(referent.getBranchId());
		}
		
		public Long getId() {
			return id;
		}
	}
	
	@Override
	public Branch getBranch(long branchId) {
		synchronized (branchById) {
			BranchReference result = branchById.get(Long.valueOf(branchId));
			if (result != null) {
				Branch cachedBranch = result.get();
				if (cachedBranch != null) {
					return cachedBranch;
				}
			}
			
			// Clean up garbage.
			BranchReference collectedReference;
			while ((collectedReference = (BranchReference) branchGarbage.poll()) != null) {
				branchById.remove(collectedReference.getId());
			}
		}
		
		BranchImpl newBranch;
		PooledConnection readConnection = connectionPool.borrowReadConnection();
		try {
			newBranch =
				(BranchImpl) fetchItem(readConnection, getBranchType(), TLContext.TRUNK_ID, LongID.valueOf(branchId),
					Revision.CURRENT_REV);
			if (newBranch == null) {
				throw new IllegalArgumentException("Branch with ID '" + branchId + "' does not exist.");
			}
			newBranch.onLoad(readConnection);
		} catch (SQLException ex) {
			throw new KnowledgeBaseRuntimeException(ex);
		} finally {
			connectionPool.releaseReadConnection(readConnection);
		}

		return enterBranch(newBranch);
	}

	Branch enterBranch(BranchImpl newBranch) {
		synchronized (branchById) {
			Long branchId = Long.valueOf(newBranch.getBranchId());
			
			BranchReference concurrentlyLoadedBranchRef = branchById.get(branchId);
			if (concurrentlyLoadedBranchRef != null) {
				Branch concurrentlyLoadedBranch = concurrentlyLoadedBranchRef.get();
				if (concurrentlyLoadedBranch != null) {
					return concurrentlyLoadedBranch;
				}
			}
			
			branchById.put(branchId, new BranchReference(newBranch, branchGarbage));
			return newBranch;
		}
	}

	/**
	 * Fetches an item with the data revision computed by
	 * {@link #getDataRevision(long)}.
	 * 
	 * @see #fetchItem(PooledConnection, MOKnowledgeItem, long, TLID, long, long)
	 * @see #getDataRevision(long)
	 */
	private KnowledgeItemInternal fetchItem(PooledConnection connection, MOKnowledgeItem type, long branchID,
			TLID objectID, long itemRevision) throws SQLException {
		long dataRevision = getDataRevision(itemRevision);
		return fetchItem(connection, type, branchID, objectID, itemRevision, dataRevision);
	}

	/**
	 * Fetches an item from the database and updates the item cache with that item.
	 * 
	 * @param connection
	 *        Database connection.
	 * @param type
	 *        Type of the item to fetch.
	 * @param branchID
	 *        Id of the branch of the desired item.
	 * @param objectID
	 *        Id that describes the item uniquely.
	 * @param itemRevision
	 *        History context to assign to the fetched item, see
	 *        {@link DBAccess#fetch(DBKnowledgeBase, PooledConnection, long, TLID, long, long)}
	 * @param dataRevision
	 *        Revision of the data to fetch, see
	 *        {@link DBAccess#fetch(DBKnowledgeBase, PooledConnection, long, TLID, long, long)}
	 * @return The desired item (or the cached version if already exists) or <code>null</code>, if
	 *         no such item exists.
	 */
	private KnowledgeItemInternal fetchItem(PooledConnection connection, MOKnowledgeItem type, long branchID,
			TLID objectID, long itemRevision, long dataRevision) throws SQLException {
		DBAccess dbAccess = getDBAccess(type);
		return dbAccess.fetch(this, connection, branchID, objectID, itemRevision, dataRevision);
	}

	/**
	 * Determines the data revision (the revision in which the data must be used) for an object with
	 * the given item revision.
	 * 
	 * @param itemRevision
	 *        The revision (history context) of an {@link KnowledgeItem}.
	 * @return The revision which can be used to load data for the item in the given revision.
	 */
	long getDataRevision(long itemRevision) {
		long dataRevision;
		if (itemRevision == Revision.CURRENT_REV) {
			// fetching in current is incorrect, because it would cause items whose data have
			// history context larger than the revision of the knowledge base.
			dataRevision = getSessionRevision();
		} else {
			dataRevision = itemRevision;
		}
		return dataRevision;
	}
    
    @Override
	public MORepository getMORepository() {
        return moRepository;
    }

	@Override
	public final Transaction beginTransaction() {
		return beginTransaction(null);
	}

    @Override
	public Transaction beginTransaction(Message commitMessage) {
    	return internalCreateDBContext().begin(false, commitMessage);
    }
    
    /**
     * TODO #2829: Delete TL 6 deprecation 
     * @deprecated Use {@link #beginTransaction(Message)}.
     */
    @Override
	@Deprecated
    public boolean begin() {
    	internalCreateDBContext().begin(true, null);
    	return true;
    }

    /**
     * TODO #2829: Delete TL 6 deprecation 
     * @deprecated Use {@link #beginTransaction(Message)}.
     */
    @Override
	@Deprecated
    public boolean commit() {
        DBContext dbContext = getCurrentDBContext();
        
        if (dbContext == null) {
        	return true;
        }

        boolean success = false;
        try {
            dbContext.commitAnonymous();
            success = true;
        }
        catch (CommitVetoException e) {
        	Logger.info("Commit veto", e, this);
        } catch (KnowledgeBaseException ex) {
        	Logger.error("Commit failed", ex, this);
		}
        return success;
    }

	/*package protected*/ final void dropDBContext() {
		currentInteraction().set(_localDBContext, null);
	}

	private DBContext lookupDBContext() {
		return currentInteraction().get(_localDBContext);
	}

	/**
     * TODO #2829: Delete TL 6 deprecation 
     * @deprecated Use {@link #beginTransaction(Message)}.
     */
    @Override
	@Deprecated
	public boolean rollback() {
        DBContext dbContext = getCurrentDBContext();
        if (dbContext == null) {
        	// Context not yet allocated. There are no local modifications.
			// Roll-back is a no-operation.
        	return true;
        }
        
        // Context roll-back uninstalls the context.
        dbContext.rollback();
        
        return true;
    }

    @Override
	public void addModificationListener(ModificationListener listener) {
		if (!modificationListeners.contains(listener)) {
			modificationListeners.add(listener);
		}
	}

	@Override
	public void removeModificationListener(ModificationListener listener) {
		modificationListeners.remove(listener);
	}

	@Override
	public void addCommitChecker(CommitChecker checker) {
		if (!vetoListeners.contains(checker)) {
			vetoListeners.add(checker);
		}
	}

	@Override
	public void removeCommitChecker(CommitChecker checker) {
		vetoListeners.remove(checker);
	}

	@Override
	public synchronized boolean addUpdateListener(UpdateListener listener) {
    	if (! updateListeners.contains(listener)) {
    		updateListeners.add(listener);
    		return true;
    	} else {
    		return false;
    	}
    }
    
	synchronized boolean addUpdateListenerHighPrio(UpdateListener listener) {
		if (!_updateListenersHighPrio.contains(listener)) {
			_updateListenersHighPrio.add(listener);
			return true;
		} else {
			return false;
		}
	}

    @Override
	public synchronized boolean removeUpdateListener(UpdateListener listener) {
    	return updateListeners.remove(listener);
    }
    
	synchronized boolean removeUpdateListenerHighPrio(UpdateListener listener) {
		return _updateListenersHighPrio.remove(listener);
	}

    @Override
	public UpdateChain getUpdateChain() {
    	synchronized (refetchLock) {
    		return new UpdateChainView(this.updateChainTail);
		}
    }
    
	@Override
	public UpdateChain getSessionUpdateChain() {
		return new UpdateChainView(sessionUpdateLink());
	}

	/**
	 * This method is called outside the refetch lock. Therefore it is possible, the events are not
	 * send in correct order. The method waits until ththe event, if it is the "next" or caches the
	 * event until the all previous events were sent.
	 */
	private void fireEvent(UpdateEvent event) {
		synchronized (refetchLock) {
			boolean wasInterrupted = false;
			while (true) {
				long commitNumber = event.getCommitNumber();
				assert commitNumber > _lastSentEvent;
				if (commitNumber == _lastSentEvent + 1) {
					// Unlock before sending event!
					_lastSentEvent = commitNumber;
					refetchLock.notifyAll();

					try {
						fireUpdateHighPrio(event);
						// Notify synchronous listeners.
						fireUpdate(event);
					} catch (Throwable ex) {
						Logger.error("Unable to process event with number '" + commitNumber, ex);
						if (wasInterrupted) {
							Thread.currentThread().interrupt();
						}
						throw ex;
					}
					break;
				} else {
					try {
						refetchLock.wait();
					} catch (InterruptedException ex) {
						Logger.warn("Thread waiting for sending event with number '" + commitNumber
								+ "' interrupted. Last sent event: " + +_lastSentEvent,
							ex);
						wasInterrupted = true;
					}
				}
			}
			if (wasInterrupted) {
				Thread.currentThread().interrupt();
			}
		}
	}

	private void fireUpdateHighPrio(UpdateEvent event) {
		for (UpdateListener updateListener : _updateListenersHighPrio) {
			updateListener.notifyUpdate(this, event);
		}
	}

	private void fireUpdate(UpdateEvent event) {
		updateWrappers(event);
		for (UpdateListener updateListener : updateListeners) {
			updateListener.notifyUpdate(this, event);
    	}
    }

	List<Modification> fireUpcomingDeletionEvent(DBContext modifiedContext, KnowledgeItem item) {
		modifiedContext.lock();
		try {
			List<Modification> modifications = Collections.emptyList();
			for (int i = 0, size = modificationListeners.size(); i < size; i++) {
				ModificationListener listener = modificationListeners.get(i);
				Modification modification = listener.notifyUpcomingDeletion(this, item);
				if (modification != Modification.NONE) {
					if (modifications.isEmpty()) {
						modifications = new ArrayList<>(size - i);
					}
					modifications.add(modification);
				}
			}
			return modifications;
		} finally {
			modifiedContext.unlock();
		}
	}

	List<Modification> fireKBChangeEvent(DBContext modifiedContext,
			Map<ObjectKey, ? extends KnowledgeItem> createdObjects,
			Map<ObjectKey, ? extends KnowledgeItem> updatedObjects,
			Map<ObjectKey, ? extends KnowledgeItem> removedObjects) {
		modifiedContext.lock();
		try {
			List<Modification> modifications = Collections.emptyList();
			for (int i = 0, size = modificationListeners.size(); i < size; i++) {
				ModificationListener listener = modificationListeners.get(i);
				Modification modification = listener.createModification(this, createdObjects, updatedObjects, removedObjects);
				if (modification != Modification.NONE) {
					if (modifications.isEmpty()) {
						modifications = new ArrayList<>(size - i);
					}
					modifications.add(modification);
				}
			}
			return modifications;
		} finally {
			modifiedContext.unlock();
		}
	}

	protected void fireEventVeto(DBContext modifiedContext, UpdateEvent event) throws CommitVetoException {
		modifiedContext.lock();
		try {
			for (int n = 0, cnt = vetoListeners.size(); n < cnt; n++) {
				CommitChecker listener = vetoListeners.get(n);
				try {
					listener.checkCommit(event);
				} catch (I18NRuntimeException | CommitVetoException ex) {
					// Stop processing, stop change.
					throw ex;
				} catch (Exception ex) {
					// Exception must not be propagated, to isolate the knowledge
					// base from errors in registered listeners.
					Logger.error("Non-veto exception in a knowledge base veto event listener.", ex, this);
					CommitVetoException commitVetoException =
						new CommitVetoException("Non-veto exception in a knowledge base veto event listener.", this);
					commitVetoException.initCause(ex);
					throw commitVetoException;
				}
			}
		} finally {
			modifiedContext.unlock();
		}
	}

	@Override
	public KnowledgeAssociation createAssociation(Branch branch, TLID objectName, KnowledgeItem src,
			KnowledgeItem dest, String typeName) throws DataObjectException {
		NameValueBuffer initialValues = new NameValueBuffer(2);
		initialValues.put(DBKnowledgeAssociation.REFERENCE_SOURCE_NAME, src);
		initialValues.put(DBKnowledgeAssociation.REFERENCE_DEST_NAME, dest);
		return createKnowledgeItem(branch, objectName, typeName, initialValues, KnowledgeAssociation.class);
	}

    // Special for DBKnowledgeBase

	/**
	 * Get or create a {@link DBContext} for the current thread.
	 * 
	 * <p>
	 * If the current thread has not yet a {@link DBContext} a new
	 * {@link DBContext} is created.
	 * <p>
	 */
    private DBContext internalCreateDBContext() {
    	DBContext existingDbContext = lookupDBContext();
    	if (existingDbContext != null) {
    		return existingDbContext;
    	}
    	
		TLSubSessionContext subsession = TLContextManager.getSubSession();
		if (subsession == null) {
			throw new IllegalStateException("Knowledge base operations require a valid session context.");
    	}
    	
		String contextId = subsession.getContextId();
		
		if (contextId != null)  {
			DBContext newDbContext = new DefaultDBContext(this, contextId);
			installContext(newDbContext);
		    
			subsession.getSessionContext().addHttpSessionBindingListener(this);
		    
		    return newDbContext;
		} else {
			// Here, staticContext is illegal, because changes would be
			// stored in staticContext, but the commit would silently
			// ignore the changes.
		    throw new KnowledgeBaseRuntimeException("No contextId in TLContext, cannot create context.");
		}
    }

    @Override
	public final CommitContext getCurrentContext() {
    	return getCurrentDBContext();
    }
    
    @Override
	public final CommitContext createCommitContext() {
    	return createDBContext();
    }
    
    /**
     * Workaround for inability of covariant return type overwriting. 
     * 
     * @see #getCurrentContext()
     */
    public final DBContext getCurrentDBContext() {
    	return lookupDBContext();
    }
    
    /**
     * Workaround for inability of covariant return type overwriting. 
     * 
     * @see #createCommitContext()
     */
    public final DBContext createDBContext() {
    	return internalCreateDBContext();
    }

    /**
     * Relay the Commitable interface to the current Context.
     * 
     * @see com.top_logic.knowledge.service.CommitHandler#addCommittable(com.top_logic.knowledge.service.Committable)
     */
    @Override
	public boolean addCommittable(Committable aCommitable) {
        return this.createDBContext().addCommittable(aCommitable);
    }

    /**
     * Relay the Commitable interface to the current Context.
     * 
     * @see com.top_logic.knowledge.service.CommitHandler#addCommittable(com.top_logic.knowledge.service.Committable)
     */
    @Override
	public boolean addCommittableDelete(Committable aCommitable) {
        return this.createDBContext().addCommittableDelete(aCommitable);
    }

    /**
     * Relay the Commitable interface to the current Context.
     * 
     * @see com.top_logic.knowledge.service.CommitHandler#removeCommittable(com.top_logic.knowledge.service.Committable)
     */
    @Override
	public boolean removeCommittable(Committable aCommittable) {
        DBContext context = getCurrentDBContext();
        return context != null
            && context.removeCommittable(aCommittable);
    }

    /**
      * Is ignored here (and should not be called anyway).
      */
    @Override
	public void valueBound(HttpSessionBindingEvent anEvent) {
        // ignored
    }

    /** 
     * Rollback and cleanup transient Objects on Logout...
     * 
     * @param anEvent may be null, so use with care.
     */
    @Override
	public void valueUnbound(HttpSessionBindingEvent anEvent) {
        TLContext tlContext = TLContext.getContext();
        if (tlContext != null) {
            DBContext dbContext = lookupDBContext();
            if (dbContext != null) {
            	autoRollback(dbContext);
            }
        }
    }

	private void autoRollback(DBContext dbContext) {
		Logger.warn("Dropped dangling DBContext: " + dbContext, new Exception("RemovalStackTrace").initCause(dbContext.getAllocationStackTrace()), DBKnowledgeBase.class);
		dbContext.rollbackComplete(true);
		
		onAutoRollback();
	}

	/**
	 * Hook for subclasses to perform actions on auto-rollback events.
	 */
    protected void onAutoRollback() {
		// Hook method.
	}

	/**
     * Cleanup the DBContext at end of Thread.
     */
    @Override
	public void threadUnbound(InteractionContext context) {
        DBContext lostDbContext = lookupDBContext();
        if (lostDbContext != null) {
            autoRollback(lostDbContext);
        }
		dropDBContext();
    }

	List<KnowledgeAssociation> getOutgoingAssociations(KnowledgeObjectInternal knowledgeObject,
			String associationTypeName) {
		CompiledQuery<KnowledgeAssociation> query = _expressions.outgoingRefereeQuery(associationTypeName);
		RevisionQueryArguments queryArguments = _expressions.refereeQueryArguments(knowledgeObject);
		return query.search(queryArguments);
	}
	
	List<KnowledgeAssociation> getAssociations(KnowledgeObjectInternal source, String anAssociationType,
			KnowledgeObjectInternal target) {
		assert source.getHistoryContext() == target.getHistoryContext() :
			"Associations between objects of different age cannot be computed.";

		CompiledQuery<KnowledgeAssociation> query = _expressions.associationsBetweenQuery(anAssociationType);
		RevisionQueryArguments queryArguments = _expressions.associationsBetweenArguments(source, target);
		return query.search(queryArguments);
	}

	List<KnowledgeAssociation> getIncomingAssociations(KnowledgeObjectInternal knowledgeObject,
			String associationTypeName) {
		CompiledQuery<KnowledgeAssociation> query = _expressions.incomingRefereeQuery(associationTypeName);
		RevisionQueryArguments queryArguments = _expressions.refereeQueryArguments(knowledgeObject);
		return query.search(queryArguments);
	}

	/**
	 * Returns a collection of all current objects which have a {@link MOReference} pointing to the
	 * given object.
	 */
	public List<KnowledgeItem> getAnyReferer(KnowledgeItem any) {
		return getAnyReferer(any, null, KnowledgeItem.class);
	}

	/**
	 * Returns a collection of all current objects which have a {@link MOReference} of the given
	 * {@link MOReference#getDeletionPolicy() deletion policy} pointing to the given object.
	 * 
	 * @param policy
	 *        the policy which a {@link MOReference} must have such that the corresponding referee
	 *        can be found. <code>null</code> means each policy is allowed.
	 */
	public <T extends KnowledgeItem> List<T> getAnyReferer(KnowledgeItem any, DeletionPolicy policy,
			Class<T> expectedType) {
		return getAnyReferer(Revision.CURRENT, any, policy, expectedType);
	}

	/**
	 * Returns a collection of all objects in the given revision which have a {@link MOReference}
	 * pointing to the given object.
	 */
	public List<KnowledgeItem> getAnyReferer(Revision requestedRevision, KnowledgeItem any) {
		return getAnyReferer(requestedRevision, any, null, KnowledgeItem.class);
	}

	/**
	 * Returns a collection of all objects in the current revision which have a {@link MOReference}
	 * of the given {@link MOReference#getDeletionPolicy() deletion policy} pointing to the given
	 * object.
	 * 
	 * @param policy
	 *        the policy which a {@link MOReference} must have such that the corresponding referee
	 *        can be found. <code>null</code> means each policy is allowed.
	 */
	public <T extends KnowledgeItem> List<T> getAnyReferer(Revision requestedRevision, KnowledgeItem any,
			DeletionPolicy policy, Class<T> expectedType) {
		Map<MetaObject, CompiledQuery<T>> completeQuery =
			anyRefereesQuery(any.tTable(), policy, Boolean.FALSE, expectedType);
		if (completeQuery.isEmpty()) {
			return Collections.emptyList();
		}
		RevisionQueryArguments revisionArgs = anyRefereesArguments(requestedRevision, any);
		ArrayList<T> result = new ArrayList<>();
		for (CompiledQuery<T> query : completeQuery.values()) {
			result.addAll(query.search(revisionArgs));
		}
		return result;
	}

	/**
	 * Returns a collection of all objects in the current revision which have a {@link MOReference}
	 * of the given {@link MOReference#getDeletionPolicy() deletion policy} pointing to one element
	 * in given item set.
	 * 
	 * @param policy
	 *        the policy which a {@link MOReference} must have such that the corresponding referee
	 *        can be found. <code>null</code> means each policy is allowed.
	 */
	public <T extends KnowledgeItem> List<T> getAnyReferer(Revision requestedRevision, MetaObject targetType,
			Collection<? extends KnowledgeItem> items, DeletionPolicy policy, Class<T> expectedType) {
		int size = items.size();
		switch (size) {
			case 0:
				return Collections.emptyList();
			case 1:
				KnowledgeItem item;
				if (items instanceof RandomAccess) {
					item = ((List<? extends KnowledgeItem>) items).get(0);
				} else {
					item = items.iterator().next();
				}
				return getAnyReferer(requestedRevision, item, policy, expectedType);
			default:
				Map<MetaObject, CompiledQuery<T>> completeQuery =
					anyRefereesQuery(targetType, policy, Boolean.TRUE, expectedType);
				if (completeQuery.isEmpty()) {
					return Collections.emptyList();
				}
				List<T> result = new ArrayList<>();
				int maxSetSize = dbHelper.getMaxSetSize();
				if (size > maxSetSize) {
					Iterator<List<KnowledgeItem>> it = CollectionUtil.chunk(maxSetSize, items.iterator());
					while (it.hasNext()) {
						RevisionQueryArguments revisionArgs = anyRefereesArguments(requestedRevision, it.next());
						for (CompiledQuery<T> query : completeQuery.values()) {
							result.addAll(query.search(revisionArgs));
						}
					}
				} else {
					RevisionQueryArguments revisionArgs = anyRefereesArguments(requestedRevision, items);
					for (CompiledQuery<T> query : completeQuery.values()) {
						result.addAll(query.search(revisionArgs));
					}
				}
				return result;
		}
	}

	/**
	 * Returns queries that search for an item that references a given item via a
	 * {@link MOReference} with a given {@link DeletionPolicy}.
	 * 
	 * <p>
	 * The result is a mapping from the type of a potential referee to the {@link CompiledQuery}
	 * searching for them.
	 * </p>
	 * 
	 * @param targetType
	 *        The type of the target, or the type of each target object if multipleTargets.
	 * @param policy
	 *        The {@link DeletionPolicy} a reference must have to be included in the search query.
	 *        If <code>null</code> then each reference is used.
	 * @param multipleTargets
	 *        Whether the target is a single object or a sequence of objects.
	 * @param expectedType
	 *        The implementation class of the searched items.
	 * 
	 * @see #anyRefereesArguments(Revision, KnowledgeItem)
	 * @see #anyRefereesArguments(Revision, Iterable)
	 */
	public <T extends KnowledgeItem> Map<MetaObject, CompiledQuery<T>> anyRefereesQuery(MetaObject targetType,
			DeletionPolicy policy, Boolean multipleTargets, Class<T> expectedType) {
		return _expressions.anyRefereeQuery(targetType, policy, multipleTargets, expectedType);
	}

	/**
	 * Creates a {@link RevisionQueryArguments} for the queries contained in the result of
	 * {@link #anyRefereesQuery(MetaObject, DeletionPolicy, Boolean, Class)} where multipleTargets
	 * is <code>true</code>.
	 */
	public RevisionQueryArguments anyRefereesArguments(Revision requestedRevision,
			Iterable<? extends KnowledgeItem> items) {
		return _expressions.anyRefereeArguments(requestedRevision, items);
	}

	/**
	 * Creates a {@link RevisionQueryArguments} for the queries contained in the result of
	 * {@link #anyRefereesQuery(MetaObject, DeletionPolicy, Boolean, Class)} where multipleTargets
	 * is <code>false</code>.
	 */
	public RevisionQueryArguments anyRefereesArguments(Revision requestedRevision, KnowledgeItem item) {
		return _expressions.anyRefereeArguments(requestedRevision, item);
	}

	List<KnowledgeAssociation> getAnyOutgoingAssociations(KnowledgeObjectInternal target) {
		RevisionQueryArguments queryArguments = _expressions.refereeQueryArguments(target);
		return _expressions.sourceRefereeQuery().search(queryArguments);
	}

	List<KnowledgeAssociation> getAnyIncomingAssociations(KnowledgeObjectInternal target) {
		RevisionQueryArguments queryArguments = _expressions.refereeQueryArguments(target);
		return _expressions.destRefereeQuery().search(queryArguments);
	}

	List<? extends MetaObject> getObjectTypes() {
		return moRepository.getConcreteSubtypes(getObjectType());
	}

	List<? extends MetaObject> getValueTypes() {
		return moRepository.getConcreteSubtypes(getAssociationType());
	}

	/**
	 * @deprecated testing only
	 */
	public Object switchThreadContext(Object o1) {
		return currentInteraction().set(_localDBContext, (DBContext) o1);
	}

	/* package protected */RevisionImpl createRevision(PooledConnection commitConnection, Message logMessage)
			throws SQLException, MergeConflictException, RefetchTimeout {
		
		SubSessionContext subsession = currentInteraction().getSubSessionContext();
		if (subsession == null) {
			throw new IllegalStateException("No session available.");
		}
		String contextId = subsession.getContextId();
		if (contextId == null) {
			throw new IllegalStateException("No context ID in current context.");
		}
		
		long waitForNumberTime = System.currentTimeMillis();
		long commitNumber =
			sequenceManager.nextSequenceNumber(dbHelper, commitConnection, dbHelper.retryCount(), REVISION_SEQUENCE);
		long commitStartTime = System.currentTimeMillis();
		if ((commitStartTime - waitForNumberTime) > getCommitWarnTime()) {
			Logger.info(
				"Waited for commit number " + commitNumber + " for "
						+ StopWatch.toStringMillis((commitStartTime - waitForNumberTime), TimeUnit.SECONDS) + ".",
				DBKnowledgeBase.class);
		}
		
		long lastCommittedRevision = commitNumber - 1;
		refetch(commitConnection, lastCommittedRevision);
		RevisionImpl newRevision =
			internalCreateRevision(commitNumber, contextId, commitStartTime, logMessage);
		
		// Insert revision.
		getDBAccess(newRevision.tTable()).insert(commitConnection, commitNumber, newRevision);
		
		return newRevision;
	}

	private TLInteractionContext currentInteraction() {
		TLInteractionContext interaction = TLContextManager.getInteraction();
		if (interaction == null) {
			throw new IllegalStateException("KnowledgeBase operations require a valid InteractionContext.");
		}
		return interaction;
	}

	/* package protected */ RevisionImpl internalCreateRevision(long commitNumber, String author, long date,
			Message logMessage) {
		// Create commit message
		RevisionImpl newRevision = (RevisionImpl) newImmutableItem(getRevisionType());
        String serializedMessage = MessageStoreFormat.toString(logMessage);
        newRevision.initNew(commitNumber, author, date, serializedMessage);

		return newRevision;
	}

	@Override
	public int refetch() throws RefetchTimeout {
		long latestRevision = getLastRevisionId();
		
		PooledConnection readConnection = this.connectionPool.borrowReadConnection();
		try {
			return refetch(readConnection, latestRevision);
		} catch (SQLException e) {
			throw new KnowledgeBaseRuntimeException(e);
		} catch (MergeConflictException e) {
			throw new KnowledgeBaseRuntimeException(e);
		} finally {
			connectionPool.releaseReadConnection(readConnection);
		}
	}
	
	private int refetch(PooledConnection context, long lastRemoteRevision) throws SQLException, RefetchTimeout,
			MergeConflictException {
		/* Must not skip following code, because waiting for currentRefetchRevision ==
		 * lastLocalRevision is also necessary in single node case. This is e.g. necessary in case
		 * of concurrent commit on the same node: The database lock is released *before* the updates
		 * are published. The second commit has to wait until the first commit is completely
		 * finished. This is signalized by setting localRevision to currentRefetchRevision. So if
		 * this code is not executed the second commit does not wait. */
//		if (singleNodeOptimization) {
//			updateLocalRevision(lastRemoteRevision);
//			return 0;
//		}
		
		// TODO: Review synchronization.
		UpdateChainLink noRefetch;
		long firstRemoteRevision;
		synchronized (refetchLock) {
			// The following assertion is also wrong: After fetching the last
			// committed revision from the database by a refetch thread and
			// entering the refetch lock above, there might be an interleaving
			// local commit, which increments the local cache revision.
			// 
			// assert lastLocalRevision <= lastRemoteRevision : "Future revision in cache.";
			
			if (_refetchLocked) {
				// Concurrent refetch is running, await completion.
				long waitTime = refetchTimeout;
				while (true) {
					long waited = -System.currentTimeMillis();
					try {
						refetchLock.wait(waitTime);
					} catch (InterruptedException e) {
						throw new KnowledgeBaseRuntimeException("Refetch interrupted.", e);
					}

					waited += System.currentTimeMillis();
					waitTime -= waited;
					
					if (_refetchLocked) {
						// The wait condition is still true.
						if (waitTime <= 0) {
							// The max wait time has been elapsed.
							throw new RefetchTimeout(
									"Could not update data cache, refetch did not complete in time: " + 
									"lastLocalRevision=" + lastLocalRevision + ", " + 
									"lastRemoteRevision=" + lastRemoteRevision);
						}
					} else {
						break;
					}
				}
			}

			boolean refetchRequired = lastRemoteRevision > lastLocalRevision;
			firstRemoteRevision = lastLocalRevision + 1;
			
			if (refetchRequired) {
				// Block concurrent refetches.
				_refetchLocked = true;
				noRefetch = null;
			} else {
				noRefetch = updateChainTail;
			}
		}
		
		if (noRefetch != null) {
			updateSessionRevision(noRefetch, true);
			return 0;
		}
		int objCnt;
		try {
			objCnt = refetch(context, firstRemoteRevision, lastRemoteRevision);
		} finally {
			// release refetch lock
			synchronized (refetchLock) {
				unlockRefetch();
			}
		}
		DBContext currentContext = lookupDBContext();
		if (currentContext != null) {
			currentContext.checkMergeConflict();
		}
		return objCnt;
	}

	private int refetch(PooledConnection context, long firstRemoteRevision, long lastRemoteRevision) throws SQLException {
		long nanoTime = -System.nanoTime();
		int objCnt = internalRefetch(context, firstRemoteRevision, lastRemoteRevision);
		nanoTime += System.nanoTime();
		long milliTime = nanoTime / 1000000L;
		if (milliTime >= refetchLogTime) {
			Logger.info("Refetched " + objCnt + " objects in " + DebugHelper.toDuration(milliTime)
				+ " (revision"
				+ (firstRemoteRevision != lastRemoteRevision ?
					"s" + " from '" + firstRemoteRevision + "' to '" + lastRemoteRevision + "'" :
					" " + firstRemoteRevision) + ").",
				DBKnowledgeBase.class);
		}
		return objCnt;
	}

	private int internalRefetch(PooledConnection context, long firstRemoteRevision, long lastRemoteRevision)
			throws SQLException {
		int objCnt = 0;
		
		Map<MetaObject, Object> updatedObjects = new HashMap<>();
		Map<ObjectKey, ItemChange> changeById = new HashMap<>();
		DBKey touchedObjectKey = new DBKey();
		ChangeEventBuilder eventBuilder = new ChangeEventBuilder(this);
		BulkIdLoad loader = new BulkIdLoad(this);
		boolean eventCreated = false;
		long processedRevision = -1;
		try (ChangeSetReader changes = getChangeSetReader(firstRemoteRevision, lastRemoteRevision)) {
			while (true) {
				ChangeSet cs = changes.read();
				if (cs == null) {
					break;
				}
				long revision = cs.getRevision();
				eventBuilder.setCommitNumber(revision);

				objCnt += cs.getCreations().size();
				for (ObjectCreation creation : cs.getCreations()) {
					touchedObjectKey.update(Revision.CURRENT_REV, creation.getObjectId());
					DBObjectKey createdObjectId = touchedObjectKey.toStableKey();
					eventBuilder.addCreatedObjectKey(createdObjectId, null);
					changeById.put(createdObjectId, creation);
				}

				List<ItemUpdate> updates = cs.getUpdates();
				long branchContext = -1;
				DBObjectKey updatedObjectId;
				for (ItemUpdate update : updates) {
					touchedObjectKey.update(Revision.CURRENT_REV, update.getObjectId());
					DBKnowledgeItem cachedItem =
						(DBKnowledgeItem) cleanupAndLookupCache(touchedObjectKey, revision - 1);
					if (cachedItem != null) {
						long itemBranch = cachedItem.getBranchContext();
						if (itemBranch != branchContext) {
							if (updatedObjects.size() > 0) {
								refetchUpdates(context, branchContext, revision, updatedObjects);
								updatedObjects.clear();
							}
							branchContext = itemBranch;
						}
						addCachedItem(updatedObjects, cachedItem);
						updatedObjectId = cachedItem.tId();
					} else {
						updatedObjectId = touchedObjectKey.toStableKey();
					}
					eventBuilder.addUpdatedObjectKey(updatedObjectId, cachedItem);
					changeById.put(updatedObjectId, update);
					objCnt++;
				}
				if (updatedObjects.size() > 0) {
					refetchUpdates(context, branchContext, revision, updatedObjects);
					updatedObjects.clear();
				}

				Object cachedDeletedItems = InlineList.newInlineList();
				List<ItemDeletion> deletions = cs.getDeletions();
				for (ItemDeletion deletion : deletions) {
					touchedObjectKey.update(Revision.CURRENT_REV, deletion.getObjectId());
					DBObjectKey deletedObjectKey;
					DBKnowledgeItem cachedItem;
					synchronized (cache) {
						syncCacheCleanupReferences();
						cachedItem = (DBKnowledgeItem) syncCacheLookupCache(touchedObjectKey, revision - 1, true);
					}
					if (cachedItem != null) {
						cachedDeletedItems = InlineList.add(DBKnowledgeItem.class, cachedDeletedItems, cachedItem);
						deletedObjectKey = cachedItem.tId();
					} else {
						deletedObjectKey = touchedObjectKey.toStableKey();
					}
					eventBuilder.addDeletedObjectKey(deletedObjectKey, cachedItem);
					changeById.put(deletedObjectKey, deletion);
					objCnt++;
				}

				UpdateChainLink updateLink = new UpdateChainLink(eventBuilder.createEvent(true, cs));
				dropDeleted(updateLink, InlineList.toList(DBKnowledgeItem.class, cachedDeletedItems), revision);
				announceUpdate(updateLink, changeById, loader);
				processedRevision = revision;
				eventCreated = true;
			}
		} finally {
			{
				synchronized (refetchLock) {
					if (eventCreated) {
						// Insert a new tail element to the update queue that ensures that
						// all memory can be released after all asynchronous update
						// listeners have processed all updates.
						syncRefetchUpdateRevisionAndPublishUpdate(new UpdateChainLink(processedRevision));
					} else {
						/* Merge conflicts are checked at the end of the refetch. */
						updateSessionRevision(updateChainTail, false);
					}

					if (this.lastLocalRevision < processedRevision) {
						// The last revision was an empty revision with no entries
						// in the xref table. Make sure to nevertheless release the
						// refetch lock.
						//
						// Note: Check before set is required, because if the last local
						// revision had been advanced to the current refetch revision,
						// the refetch lock would have been freed and a concurrent
						// refetch could already have started.
						this.lastLocalRevision = processedRevision;
						refetchLock.notifyAll();
					}
				}
			}
		}
		
		return objCnt;
	}

	private void addCachedItem(Map<MetaObject, Object> updatedObjects, DBKnowledgeItem cachedItem) {
		MetaObject type = cachedItem.tTable();
		Object inlineListNames = updatedObjects.get(type);
		if (inlineListNames == null) {
			inlineListNames = InlineList.newInlineList();
		}
		updatedObjects.put(type,
			InlineList.add(TLID.class, inlineListNames, cachedItem.getObjectName()));
	}

	private void refetchUpdates(PooledConnection context, long branchContext, long updateRevision,
			Map<MetaObject, Object> updatedObjects) {
		for (Entry<MetaObject, Object> inlineNamesByType : updatedObjects.entrySet()) {
			MOKnowledgeItemImpl type = (MOKnowledgeItemImpl) inlineNamesByType.getKey();
			List<TLID> updatedObjectNames = InlineList.toList(TLID.class, inlineNamesByType.getValue());
			refetchUpdates(context, branchContext, updateRevision, type, updatedObjectNames);
		}
	}

	private void refetchUpdates(PooledConnection context, long branchContext, long updateRevision,
			MOKnowledgeItemImpl type, List<TLID> updatedObjectNames) {
		int maxSetSize = dbHelper.getMaxSetSize();
		int cnt = updatedObjectNames.size();
		if (cnt > maxSetSize) {
			int chunks = (cnt + maxSetSize - 1) / maxSetSize;
			for (int n = 0; n < chunks; n++) {
				int offset = n * maxSetSize;
				List<TLID> chunkIds = updatedObjectNames.subList(offset, Math.min(offset + maxSetSize, cnt));
				refetchChunk(context, branchContext, updateRevision, type, chunkIds);
			}
		} else {
			refetchChunk(context, branchContext, updateRevision, type, updatedObjectNames);
		}
	}

	private void refetchChunk(PooledConnection context, long branchContext, long updateRevision, MOKnowledgeItemImpl type, List<TLID> chunkIds) {
		CompiledQuery<KnowledgeItem> compiledQuery = _expressions.refetchChunkQuery(type);
		RevisionQueryArguments queryArguments = _expressions.refetchChunkArguments(getBranch(branchContext), updateRevision, chunkIds);

		try (CloseableIterator<KnowledgeItem> stream = compiledQuery.searchStream(context, queryArguments)) {
			while (stream.hasNext()) {
				// Data cache is automatically updated with new object versions.
				stream.next();
			}
		}
	}

	/**
	 * Synchronized version of {@link #syncRefetchPublishUpdate(UpdateChainLink)}.
	 */
	private void announceUpdate(UpdateChainLink remoteRevision, Map<ObjectKey, ItemChange> changesById,
			BulkIdLoad loader) {
		UpdateEvent event = remoteRevision.getUpdateEvent();
		/* Update to new revision to ensure that fetching objects occur in the correct revision,
		 * e.g. to determine wrapper class, access to attribute values is needed which is only in
		 * the refetch revision possible. */
		/* Do not merge the remote event, because its construction is not finished: The created
		 * items and updated items are not loaded, yet. */
		updateSessionRevision(remoteRevision, false, false);
		/* Fetch items to avoid fetching items one by one. */
		event.fetch(loader);

		/* Merge new remote revision explicit as this was not made during update of session
		 * revision. */
		DBContext currentContext = lookupDBContext();
		if (currentContext != null) {
			currentContext.merge(event);
		}

		// Bring caches in sync.
		updateCaches(event, changesById);

		// Notify asynchronous listeners.
		synchronized (refetchLock) {
			syncRefetchPublishUpdate(remoteRevision);
		}
		
		fireEvent(event);
	}

	void updateCaches(UpdateEvent event) {
		ChangeSet changes = event.getChanges();
		Map<ObjectKey, ItemChange> updateById = new HashMap<>();
		for (ItemChange creation : changes.getCreations()) {
			updateById.put(creation.getObjectId().toCurrentObjectKey(), creation);
		}
		for (ItemChange update : changes.getUpdates()) {
			updateById.put(update.getObjectId().toCurrentObjectKey(), update);
		}
		for (ItemChange deletion : changes.getDeletions()) {
			updateById.put(deletion.getObjectId().toCurrentObjectKey(), deletion);
		}
		updateCaches(event, updateById);
	}

	void updateCaches(UpdateEvent event, Map<ObjectKey, ItemChange> changes) {
		long revision = event.getCommitNumber();
		notifyAssociationChanges(revision, event.getCreatedObjectKeys(), changes, KnowledgeObjectInternal.TYPE_CREATION);
		notifyAssociationChanges(revision, event.getUpdatedObjectKeys(), changes,
			KnowledgeObjectInternal.TYPE_MODIFICATION);
		notifyAssociationChanges(revision, event.getDeletedObjectKeys(), changes, KnowledgeObjectInternal.TYPE_DELETION);
	}

	private void updateWrappers(UpdateEvent event) {
		if (event.isRemote()) {
			for (KnowledgeItem updatedItem : event.getUpdatedObjects().values()) {
				TLObject object = updatedItem.getWrapper();
				if (object == null) {
					continue;
				}

				if (object instanceof Wrapper) {
					((Wrapper) object).refetchWrapper();
				}
			}
		}
	}

	/**
	 * @param revision
	 *        Currently re-fetched revision.
	 * @param changedKeys
	 *        keys of object that has been changed.
	 * @param changes
	 *        {@link ItemChange} by {@link ObjectKey}.
	 * @param changeType
	 *        Kind of change to process: {@link KnowledgeObjectInternal#TYPE_CREATION} for new
	 *        objects, {@link KnowledgeObjectInternal#TYPE_MODIFICATION} for updated object,
	 *        {@link KnowledgeObjectInternal#TYPE_DELETION} for deleted objects
	 */
	private void notifyAssociationChanges(long revision, Set<ObjectKey> changedKeys,
			Map<ObjectKey, ItemChange> changes, int changeType) {
		Map<DBObjectKey, Set<Pair<KnowledgeObjectInternal, MOReference>>> baseObjectsByLinkKey =
			new HashMap<>();
		for (Iterator<ObjectKey> it = changedKeys.iterator(); it.hasNext();) {
			DBObjectKey key = (DBObjectKey) it.next();
			MetaObject type = key.getObjectType();
			
			// Prevent resolving an object that has no references at all.
			List<? extends MOReference> references = MetaObjectUtils.getReferences(type);
			if (references.isEmpty()) {
				continue;
			}
			
			switch (changeType) {
				case KnowledgeObjectInternal.TYPE_CREATION:
					break;
				case KnowledgeObjectInternal.TYPE_MODIFICATION:
					if (!changes.containsKey(key)) {
						// There is actually no change: changes were reverted within transaction.
						continue;
					}
					break;
				case KnowledgeObjectInternal.TYPE_DELETION:
					KnowledgeItemInternal link = resolveIdentifierFromGlobalCache(key, revision);
					// Deletion is only relevant, if link is cached; if link is not cached, it is
					// not contained in any cache as cache contains hard references.
					if (link == null) {
						continue;
					}
					break;
				default:
					throw new IllegalArgumentException("Illegal change type: " + changeType);
			}
			ItemChange changeEvent = changes.get(key);
			collectReferenceChangesToBaseObjects(revision, references, key, changeEvent, baseObjectsByLinkKey,
				changeType);
		}

		notifyReferenceChanges(revision, baseObjectsByLinkKey);
	}

	private void notifyReferenceChanges(long revision,
			Map<DBObjectKey, Set<Pair<KnowledgeObjectInternal, MOReference>>> baseObjectsByLinkKey) {
		if (baseObjectsByLinkKey.isEmpty()) {
			return;
		}
		Set<DBObjectKey> linkKeys = baseObjectsByLinkKey.keySet();
		List<KnowledgeItem> links;
		if (baseObjectsByLinkKey.size() == 1) {
			KnowledgeItem foundItem = resolveIdentifier(linkKeys.iterator().next(), revision);
			assert foundItem != null : "The access occurs in the given revision. As the item was touched in that revision it must be found.";
			links = Collections.singletonList(foundItem);
		} else {
			BulkIdLoad loader = new BulkIdLoad(this);
			loader.addAll(linkKeys);
			// load links in re-fetched revision. Just using load would load the objects in current
			// KnowledgeBase Revision which is less than the re-fetch revision.
			links = loader.loadUncachedInRevision(revision);
		}
		
		for (KnowledgeItem link : links) {
			ObjectKey objectKey = link.tId();
			Set<Pair<KnowledgeObjectInternal, MOReference>> baseObjects = baseObjectsByLinkKey.get(objectKey);
			for (Pair<KnowledgeObjectInternal, MOReference> xxx : baseObjects) {
				KnowledgeObjectInternal baseObject = xxx.getFirst();
				MOReference changedReferenceAttribute = xxx.getSecond();
				baseObject.notifyAssociationChange(revision, changedReferenceAttribute, (KnowledgeItemInternal) link);
			}
		}
	}

	/**
	 * @param revision
	 *        Currently refetched revision
	 * @param references
	 *        references to check.
	 * @param linkKey
	 *        changed link
	 * @param baseObjectsByLinkKey
	 *        Mapping of the link key to the set of base objects whose cache must be updated, due to
	 *        the link .
	 * @param changeType
	 *        Kind of change to process: {@link KnowledgeObjectInternal#TYPE_CREATION} for new
	 *        objects, {@link KnowledgeObjectInternal#TYPE_MODIFICATION} for updated object,
	 *        {@link KnowledgeObjectInternal#TYPE_DELETION} for deleted objects
	 */
	private void collectReferenceChangesToBaseObjects(long revision, List<? extends MOReference> references,
			DBObjectKey linkKey, ItemChange update,
			Map<DBObjectKey, Set<Pair<KnowledgeObjectInternal, MOReference>>> baseObjectsByLinkKey, int changeType) {
		switch (changeType) {
			case KnowledgeObjectInternal.TYPE_CREATION: {
				Map<String, Object> newValues = update.getValues();
				for (MOReference reference : references) {
					ObjectKey globalReferencedKey = (ObjectKey) newValues.get(reference.getName());
					if (globalReferencedKey != null) {
						// reference is new, so it must be updated in each case
						collectReferenceChangesToBaseObject(revision, linkKey, globalReferencedKey,
							reference, baseObjectsByLinkKey);
					}
				}
				break;
			}
			case KnowledgeObjectInternal.TYPE_DELETION: {
				Map<String, Object> oldValues = update.getValues();
				for (MOReference reference : references) {
					ObjectKey oldReferencedKey = (ObjectKey) oldValues.get(reference.getName());
					if (oldReferencedKey != null) {
						// reference is deleted, so it must be updated in each case
						collectReferenceChangesToBaseObject(revision, linkKey, oldReferencedKey, reference,
							baseObjectsByLinkKey);
					}
				}
				break;
			}
			case KnowledgeObjectInternal.TYPE_MODIFICATION: {
				Map<String, Object> newValues = update.getValues();
				Map<String, Object> oldValues = ((ItemUpdate)update).getOldValues();
				for (MOReference reference : references) {
					ObjectKey oldReferencedKey = (ObjectKey) oldValues.get(reference.getName());
					ObjectKey globalReferencedKey = (ObjectKey) newValues.get(reference.getName());

					if (globalReferencedKey != null) {
						collectReferenceChangesToBaseObject(revision, linkKey, globalReferencedKey,
							reference, baseObjectsByLinkKey);
						if (oldReferencedKey != null) {
							collectReferenceChangesToBaseObject(revision, linkKey, oldReferencedKey,
								reference, baseObjectsByLinkKey);
						}
					} else {
						if (oldReferencedKey != null) {
							collectReferenceChangesToBaseObject(revision, linkKey, oldReferencedKey,
								reference, baseObjectsByLinkKey);
						} else {
							// a different attribute had changed. Must inform referenced object
							// because a filtered association query may base on the changed
							// attribute.
							KnowledgeItemInternal link = resolveIdentifier(linkKey, revision,false);
							ObjectKey referencedKey = link.getReferencedKey(reference);
							if (referencedKey != null) {
								collectReferenceChangesToBaseObject(revision, linkKey, referencedKey,
									reference, baseObjectsByLinkKey);
							}
						}
					}
				}
				break;
			}
			default:
				throw new IllegalArgumentException("Illegal change type: " + changeType);
		}
	}

	private void collectReferenceChangesToBaseObject(long changeRevision, DBObjectKey linkKey, ObjectKey baseKey,
			MOReference reference,
			Map<DBObjectKey, Set<Pair<KnowledgeObjectInternal, MOReference>>> baseObjectsByLinkKey) {
		KnowledgeObjectInternal baseObject =
			(KnowledgeObjectInternal) resolveIdentifierFromGlobalCache(baseKey, changeRevision);
		if (baseObject == null) {
			return;
		}
		Set<Pair<KnowledgeObjectInternal, MOReference>> baseObjects = baseObjectsByLinkKey.get(linkKey);
		if (baseObjects == null) {
			baseObjects = new HashSet<>();
			baseObjectsByLinkKey.put(linkKey, baseObjects);
		}
		baseObjects.add(new Pair<>(baseObject, reference));
	}

	/**
	 * Creates publishes the given {@link UpdateChainLink} to the {@link #getUpdateChain() update
	 * chain}.
	 * 
	 * <p>
	 * Note: Must be called from within a context synchronized at {@link #refetchLock}.
	 * </p>
	 * 
	 * @param link
	 *        The update chain link to publish.
	 * 
	 * @see #announceUpdate(UpdateChainLink, Map, BulkIdLoad) The synchronized version.
	 */
	private void syncRefetchPublishUpdate(UpdateChainLink link) {
		this.updateChainTail.setNextUpdate(link);
		this.updateChainTail = link;
		
		if (link.hasEvent()) {
			// Advance the re-fetch start pointer. Even if the refetch terminates
			// abnormally after fetching some revisions, duplicate re-fetch must be
			// prevented under all circumstances. Therefore, the cache
			// revision must be adjusted to the last fetched revision.
			this.lastLocalRevision = link.getRevision();
			
			refetchLock.notifyAll();
		}
	}
	
	/* package protected */void dropDeleted(UpdateChainLink link, Collection<? extends DBKnowledgeItem> deletedObjects,
			long deleteRevision) {
		if (deletedObjects.isEmpty()) {
			return;
		}
		for (DBKnowledgeItem deletedObject : deletedObjects) {
			deletedObject.invalidate(deleteRevision);
		}
		registerForCleanup(link, new RemoveDeletedObjectsCache(deletedObjects));

	}

	/* package protected */void insertNew(DBContext createContext, AbstractDBKnowledgeItem newObject) {
		newObject.localCommit(createContext);
        
        synchronized (cache) {
			syncCacheCleanupReferences();
        	
			syncCacheInsertCache(newObject, createContext.getCommitNumber());
        }
        
	}

	/*package protected*/ long fetchLongValue(PooledConnection connection, String fetchSource) {
		int retry = dbHelper.retryCount();
		while (true) {
			try {
				long maxRevision;
				// Lookup the maximum commit number from the revision table.
				try (PreparedStatement getStmt = connection.prepareStatement(fetchSource);
						ResultSet result = getStmt.executeQuery()) {
					if (result.next()) {
						maxRevision = result.getLong(1);
					} else {
						maxRevision = 0;
					}
				}
				return maxRevision;
			} catch (SQLException ex) {
				if (dbHelper.canRetry(ex)) {
					connection.closeConnection(ex);
					if (retry-- > 0) {
						continue;
					}
				}
				
				throw (KnowledgeBaseRuntimeException) 
					new KnowledgeBaseRuntimeException("Could not determine the commit number maximum.").initCause(ex);
			}
		}
	}

	/**
	 * Resolves the item for the given {@link DBObjectKey}. A potential lookup in the database
	 * occurs in the implicit data revision.
	 * 
	 * @see #resolveIdentifier(DBObjectKey,long)
	 */
	private KnowledgeItemInternal resolveIdentifier(DBObjectKey identity, boolean cacheOnly) {
		return resolveIdentifier(identity, IN_SESSION_REVISION, cacheOnly);
	}

	/**
	 * Looks up the {@link KnowledgeItem} with the given key.
	 * 
	 * @param identity
	 *        The key of the searched {@link KnowledgeItem}.
	 * @param dataRevision
	 *        The revision to load data from (if a database lookup is necessary).
	 * 
	 * @return The item with the given key, or <code>null</code>, if none does exist.
	 */
	KnowledgeItemInternal resolveIdentifier(DBObjectKey identity, long dataRevision) {
		return resolveIdentifier(identity, dataRevision, false);
	}

	private KnowledgeItemInternal resolveIdentifier(DBObjectKey identity, long dataRevision, boolean cacheOnly) {
		KnowledgeItemInternal cached = identity.getCached();
		if (cached != null) {
			return cached;
		}
		
		DBContext context = getCurrentDBContext();
		KnowledgeItemInternal item = itemById(context, identity, dataRevision, cacheOnly);
		if (item == null) {
			return null;
		}
		
		identity.updateReference(item.tId().getReference());
		return item;
	}

	/* package protected */KnowledgeItemInternal resolveIdentifierFromGlobalCache(ObjectKey identity,
			long lookupRevision) {
		if (identity instanceof DBObjectKey) {
			DBObjectKey internalKey = (DBObjectKey) identity;
			KnowledgeItemInternal cached = internalKey.getCached();
			if (cached != null) {
				return cached;
			}

			synchronized (cache) {
				syncCacheCleanupReferences();

				cached = syncCacheLookupCache(identity, lookupRevision, false);
				if (cached != null) {
					// Fix outdated identity.
					internalKey.updateReference(cached.tId().getReference());
				}
				return cached;
			}
		} else {
			return cleanupAndLookupCache(identity, lookupRevision);
		}
	}

	FlexDataManager getFlexDatamanager(MOClass type) {
		return versionedDataManager;
	}

	/**
	 * @deprecated TODO Ticket #8924: For internal use only. Will be removed after wrapper creation
	 *             and knowledge base has been joined.
	 */
	@Deprecated
	public ImplementationFactory implementationFactory() {
		return getImplementationFactory();
	}

	ImplementationFactory getImplementationFactory() {
		return implementationFactory;
	}

	/**
	 * Locks the refetch lock for a commit.
	 * 
	 * <p>
	 * After the commit, {@link #endCommit(UpdateChainLink)} must be called to unlock the refetch.
	 * </p>
	 * 
	 * @param newRevision
	 *        The revision that is created within the starting commit.
	 */
	/*package protected*/ void beginCommit(long newRevision) {
		synchronized (refetchLock) {
			assert newRevision == 1 || this.lastLocalRevision == newRevision - 1 :
				"Cache is not up to date, lastLocalRevision=" + this.lastLocalRevision + ", newRevision=" + newRevision;
			
			_refetchLocked = true;
		}
	}
	
	/*package protected*/ long getLastLocalRevision() {
		synchronized (refetchLock) {
			return lastLocalRevision;
		}
	}

	/**
	 * Unlocks the refetch lock after a commit and announces the changes if the commit succeeded.
	 * 
	 * @param commitLink
	 *        The update link describing the revision. <code>null</code>, if the commit failed.
	 */
	/* package protected */void endCommit(UpdateChainLink commitLink) {
		boolean success = commitLink != null;
		
		synchronized (refetchLock) {
			if (success) {
				updateSessionRevision(commitLink, false);

				// Insert a new tail to the update chain. This ensures that all
				// event memory can be released after all asynchronous update
				// listeners have processed all outstanding updates.
				UpdateChainLink dummyLink = new UpdateChainLink(commitLink.getRevision());
				updateSessionRevision(dummyLink, false);

				syncRefetchPublishUpdate(commitLink);
				syncRefetchPublishUpdate(dummyLink);
			}
			unlockRefetch();
		}
		
		if (success) {
			fireEvent(commitLink.getUpdateEvent());
		}
	}

	/**
	 * <p>
	 * Note: Must be accessed from within a context synchronized at {@link #refetchLock}.
	 * </p>
	 */
	private void unlockRefetch() {
		_refetchLocked = false;
		refetchLock.notifyAll();
	}

	/**
	 * Updates the local revision and publishes the {@link UpdateChainLink}.
	 * 
	 * <p>
	 * Note: Must be called from within a context synchronized at {@link #refetchLock}.
	 * </p>
	 * 
	 * @param link
	 *        The update chain link to publish.
	 */
	private void syncRefetchUpdateRevisionAndPublishUpdate(UpdateChainLink link) {
		updateSessionRevision(link, false);
		syncRefetchPublishUpdate(link);
	}

	@Override
	public <T extends TLObject, C> C resolveLinks(KnowledgeObject ako, AbstractAssociationQuery<T, C> query) {
		return cacheFor(ako, query).getLinksDirect();
	}
	

	@Override
	public <T extends TLObject> void fillCaches(Iterable<KnowledgeObject> items, AbstractAssociationQuery<T, ?> query)
			throws InvalidLinkException {
		Iterator<KnowledgeObject> it = items.iterator();
		if (!it.hasNext()) {
			return;
		}

		KnowledgeItem exampleItem = it.next();
		long branchContext = exampleItem.getBranchContext();
		long historyContext = exampleItem.getHistoryContext();

		boolean uniform = true;
		List<AbstractAssociationCache<T, ?>> caches = new ArrayList<>();
		for (KnowledgeObject item : items) {
			AbstractAssociationCache<T, ?> cache = cacheFor(item, query);
			if (cache.isFilled()) {
				continue;
			}

			if (branchContext != item.getBranchContext() || historyContext != item.getHistoryContext()) {
				uniform = false;
			}

			caches.add(cache);
		}
		
		if (caches.isEmpty()) {
			return;
		}
		
		if (uniform) {
			fillCachesUniform(branchContext, historyContext, query, caches);
		} else {
			fillCachesHeterogenious(query, caches);
		}
	}

	private <T extends TLObject> void fillCachesUniform(long branchContext, long historyContext,
			AbstractAssociationQuery<T, ?> query, List<AbstractAssociationCache<T, ?>> caches) {
		Collections.sort(caches, OBJECT_NAME_ORDER);

		fillCachesUniform(branchContext, historyContext, query, caches, 0, caches.size());
	}

	private <T extends TLObject> void fillCachesHeterogenious(AbstractAssociationQuery<T, ?> query,
			List<AbstractAssociationCache<T, ?>> caches) {

		assert caches.size() > 0 : "Only allocated, if at least one element was added.";

		Collections.sort(caches, BranchHistoryNameOrder.INSTANCE);

		int start = 0;
		KnowledgeItem exampleItem = caches.get(start).getBaseItem();
		long branchContext = exampleItem.getBranchContext();
		long historyContext = exampleItem.getHistoryContext();
		for (int n = 1, cnt = caches.size(); n < cnt; n++) {
			KnowledgeItem item = caches.get(n).getBaseItem();

			if (branchContext != item.getBranchContext() || historyContext != item.getHistoryContext()) {
				fillCachesUniform(branchContext, historyContext, query, caches, start, n);

				start = n;
				exampleItem = item;
				branchContext = exampleItem.getBranchContext();
				historyContext = exampleItem.getHistoryContext();
			}
		}
		fillCachesUniform(branchContext, historyContext, query, caches, start, caches.size());
	}

	private <T extends TLObject> void fillCachesUniform(long branchContext, long historyContext,
			AbstractAssociationQuery<T, ?> query, List<AbstractAssociationCache<T, ?>> caches, int cachesStart,
			int cachesStop) {

		Branch requestedBranch = getBranch(branchContext);
		int cachesCount = cachesStop - cachesStart;
		if (cachesCount < 2) {
			// Inefficient for a single object.
			return;
		}
		int chunkSize = Math.min(dbHelper.getMaxSetSize(), cachesCount);
		
		ArrayList<Object> namesChunk = new ArrayList<>(chunkSize);
		for (int chunkStart = cachesStart; chunkStart < cachesStop; chunkStart += chunkSize) {
			List<AbstractAssociationCache<T, ?>> cachesChunk =
				caches.subList(chunkStart, Math.min(cachesStop, chunkStart + chunkSize));

			namesChunk.clear();
			for (AbstractAssociationCache<T, ?> item : cachesChunk) {
				namesChunk.add(item.getBaseItem().getObjectName());
			}

			RevisionQuery<T> revisionQuery = getChunkQuery(query, namesChunk);
			RevisionQueryArguments queryArguments =
				revisionArgs().setRequestedBranch(requestedBranch).setRequestedRevision(historyContext);

			long revisionBeforeSearch = getSessionRevision();
			List<T> links = search(revisionQuery, queryArguments);
			int linkCount = links.size();

			int start = 0;
			for (AbstractAssociationCache<T, ?> cache : cachesChunk) {
				KnowledgeItem cacheBase = cache.getBaseItem();

				int stop = start;
				while (stop < linkCount) {
					KnowledgeItem link = links.get(stop).tHandle();
					Object referencedObject;
					try {
						referencedObject = link.getAttributeValue(query.getReferenceAttribute());
					} catch (NoSuchAttributeException ex) {
						throw new KnowledgeBaseRuntimeException(ex);
					}
					if (referencedObject != cacheBase) {
						break;
					}

					stop++;
				}

				cache.update(revisionBeforeSearch, links.subList(start, stop));

				start = stop;
			}
		}
	}

	@Override
	public long getSessionRevision() {
		TLInteractionContext interactionContext = TLContextManager.getInteraction();
		if (interactionContext == null) {
			synchronized (refetchLock) {
				return updateChainTail.getRevision();
			}
		}
		long interactionRevision = interactionContext.getInteractionRevision(getHistoryManager());
		if (interactionRevision > 0L) {
			return interactionRevision;
		}
		UpdateChainLink sessionRevision = sessionUpdateLink();
		interactionContext.updateInteractionRevision(getHistoryManager(), sessionRevision.getRevision());
		return sessionRevision.getRevision();
	}

	private UpdateChainLink sessionUpdateLink() {
		TLSubSessionContext subSession = TLContextManager.getSubSession();
		if (subSession == null) {
			synchronized (refetchLock) {
				return updateChainTail;
			}
		}
		UpdateChainLink currentSessionRevision = getSessionRevision(subSession);
		if (currentSessionRevision != null) {
			return currentSessionRevision;
		}
		/* Subsession revision installed lazy when accessing the session revision. This may happen
		 * concurrently by read threads. Ensure that only one thread sets the session revision. */
		synchronized (subSession) {
			UpdateChainLink concurrentlyInsertedRevision = getSessionRevision(subSession);
			if (concurrentlyInsertedRevision != null) {
				return concurrentlyInsertedRevision;
			}
			// session revision not yet requested
			UpdateChainLink updateLink;
			synchronized (refetchLock) {
				updateLink = updateChainTail;
			}
			subSession.updateSessionRevision(getHistoryManager(), updateLink);
			return updateLink;
		}
	}

	private UpdateChainLink getSessionRevision(TLSubSessionContext subSession) {
		return subSession.getSessionRevision(getHistoryManager());
	}

	@Override
	public long updateSessionRevision() throws MergeConflictException {
		UpdateChainLink updateLink;
		synchronized (refetchLock) {
			updateLink = updateChainTail;
		}
		updateSessionRevision(updateLink, true);
		return updateLink.getRevision();
	}

	/**
	 * It is expected that this method is always called when the application is single threaded,
	 * e.g. during a command execution, a commit, or a refetch. Therefore updating the session
	 * revision can not be concurrent to the lazy fetch of the session revision.
	 * 
	 * @see #sessionUpdateLink()
	 */
	private void updateSessionRevision(UpdateChainLink revision, boolean checkMergeConflict) {
		updateSessionRevision(revision, checkMergeConflict, true);
	}

	private void updateSessionRevision(UpdateChainLink revision, boolean checkMergeConflict, boolean mergeRevision) {
		DBContext currentChanges = lookupDBContext();
		TLSubSessionContext subSession = TLContextManager.getSubSession();
		if (currentChanges != null) {
			UpdateChainLink currentSessionRevision = getSessionRevision(subSession);
			if (currentSessionRevision != null) {
				mergeChanges(currentChanges, currentSessionRevision, revision, mergeRevision);
			} else {
				// May be null if not requested before.
			}

			/* Update session and interaction before a potential merge conflict breaks the control
			 * flow. */
			HistoryUtils.updateSessionAndInteractionRevision(getHistoryManager(), subSession, revision);

			if (checkMergeConflict) {
				currentChanges.checkMergeConflict();
			}
		} else {
			HistoryUtils.updateSessionAndInteractionRevision(getHistoryManager(), subSession, revision);
		}

	}

	private void mergeChanges(DBContext currentChanges, UpdateChainLink currentRevision,
			UpdateChainLink newRevision, boolean mergeNewRevision) {
		long currentRevisionNumber = currentRevision.getRevision();
		if (newRevision.getRevision() < currentRevisionNumber) {
			throw new IllegalArgumentException("Update of local revision to past: Current revision: "
				+ currentRevisionNumber + ", New revision: " + newRevision.getRevision());
		}
		if (currentRevision == newRevision) {
			/* Update to same session revision. A potential UpdateEvent was processed before. */
			return;
		}
		while (true) {
			currentRevision = currentRevision.getNextUpdate();
			if (currentRevision == null) {
				/* Reached end of chain. This occurs because a refetched revision is installed into
				 * chain <b>after</b> session revision was updated. */
				if (mergeNewRevision) {
					merge(currentChanges, newRevision);
				}
				break;
			}
			if (currentRevision == newRevision) {
				// new revision reached
				if (mergeNewRevision) {
					merge(currentChanges, newRevision);
				}
				break;
			}
			merge(currentChanges, currentRevision);
		}
	}

	private void merge(DBContext currentChanges, UpdateChainLink mergeRevision) {
		if (mergeRevision.hasEvent()) {
			UpdateEvent updateEvent = mergeRevision.getUpdateEvent();
			currentChanges.merge(updateEvent);
		}
	}

	private <T extends TLObject> RevisionQuery<T> getChunkQuery(AbstractAssociationQuery<T, ?> query,
			List<Object> namesChunk) {
		MORepository scope = getMORepository();
		AssociationQueryImpl<?, ?> queryImpl = (AssociationQueryImpl<?, ?>) query;
		SetExpression search = queryImpl.createSearch(scope, namesChunk);
		Order order = order(queryImpl.createReferenceExpr(scope));
		return queryResolved(search, order, query.getExpectedType());
	}

	private static <T extends TLObject, C, A extends Associations<T, C>> AbstractAssociationCache<T, C> cacheFor(
			KnowledgeObject ako, AbstractAssociationQuery<T, C> query) {
		return ((KnowledgeObjectInternal) ako).getAssociationCache(query);
	}

	@Override
	public HistoryManager getHistoryManager() {
		return this;
	}

	@Override
	public KnowledgeItem resolveObjectKey(ObjectKey key) {
		return resolveObjectKey(key, false);
	}

	@Override
	public KnowledgeItem resolveCachedObjectKey(ObjectKey objectKey) {
		return resolveObjectKey(objectKey, true);
	}

	private KnowledgeItem resolveObjectKey(ObjectKey key, boolean cacheOnly) {
		if (key instanceof DBObjectKey) {
			return resolveIdentifier((DBObjectKey) key, cacheOnly);
		} else {
			return itemById(getCurrentDBContext(), key, cacheOnly);
		}
	}

	/**
	 * The {@link ConnectionPool} of this {@link KnowledgeBase}.
	 */
	public ConnectionPool getConnectionPool() {
		return connectionPool;
	}
	
	@Override
	public ChangeSetReader getChangeSetReader(ReaderConfig readerConfig) {
		Revision startRev = readerConfig.getStartRev();
		Revision stopRev = readerConfig.getStopRev();
		if (stopRev.compareTo(startRev) < 0) {
			throw new IllegalArgumentException("Stop revision '" + stopRev + "' must not be less than start revision '"
				+ startRev + "'");
		}

		long startRevision = startRev.getCommitNumber();
		long stopRevision = stopRev.equals(Revision.CURRENT) ? getLastRevision() : stopRev.getCommitNumber();
		boolean needBranchEvents = readerConfig.needBranchEvents();
		boolean needCommitEvents = readerConfig.needCommitEvents();
		Set<String> typeFilter = readerConfig.getTypeNames();
		Set<Long> branchFilter = readerConfig.getBranches();
		Comparator<? super ItemEvent> order = readerConfig.getOrder();

		try {
			return getChangeSetReader(startRevision, stopRevision, needBranchEvents, needCommitEvents, typeFilter,
				branchFilter, order);
		} catch (SQLException ex) {
			throw new KnowledgeBaseRuntimeException(ex);
		}

	}

	private ChangeSetReader getChangeSetReader(long startRevision, long stopRevisionIncl) throws SQLException {
		Set<String> allTypes = null;
		Set<Long> allBranches = null;
		Comparator<? super ItemEvent> noParticularOrder = null;
		return getChangeSetReader(startRevision, stopRevisionIncl, true, true, allTypes, allBranches, noParticularOrder);
	}

	private ChangeSetReader getChangeSetReader(long startRevision, long stopRevisionIncl, boolean withBranchEvents,
			boolean withCommitEvents, Set<String> typeFilter, Set<Long> branchFilter,
			Comparator<? super ItemEvent> order) throws SQLException {

		// increase the stop revision since all reader work with stop revision exclusive
		long stopRevisionExcl = stopRevisionIncl + 1;

		final BranchEventReader branchReader =
			withBranchEvents ? new BranchEventReader(this, startRevision, stopRevisionExcl) : null;
		final CommitEventReader commitReader =
			withCommitEvents ? new CommitEventReader(this, startRevision, stopRevisionExcl) : null;

		EventReader<? extends KnowledgeEvent> reader =
			internalGetReader(startRevision, stopRevisionExcl, typeFilter, branchFilter, branchReader, commitReader,
				order);
		return new DefaultChangeSetReader(reader);
	}

	/**
	 * the smaller one of startRev and stopRev is inclusive, the larger one is
	 * exclusive.
	 */
	private EventReader<? extends KnowledgeEvent> internalGetReader(long startRev, long stopRev, Set<String> typeNames,
			Set<Long> branches, final BranchEventReader branchReader,
			final CommitEventReader commitReader, Comparator<? super ItemEvent> order) throws SQLException {
		
		boolean keepOldValues = true;

		final Comparator<KnowledgeEvent> revisionOrder;
		if (startRev <= stopRev) {
			revisionOrder = KnowledgeEvent.RevisionOrder.ASCENDING_INSTANCE;
		} else {
			revisionOrder = KnowledgeEvent.RevisionOrder.DESCENDING_INSTANCE;
		}

		final Comparator<? super ItemEvent> itemOrder;
		if (order == null) {
			itemOrder = revisionOrder;
		} else {
			itemOrder = ComparatorChain.<ItemEvent>newComparatorChain(revisionOrder, order);
		}
		
		final OrderedItemEventReader itemReader =
			new OrderedItemEventReader(this, keepOldValues, startRev, stopRev, this.chunkSize, typeNames, branches,
				itemOrder);
		return new KnowledgeEventMixer(revisionOrder, branchReader, itemReader, commitReader);
	}
	
	@Override
	public EventWriter getReplayWriter() {
		DBContext currentContext = lookupDBContext();
		if (currentContext != null) {
			throw new IllegalStateException("Cannot start event replay with open transaction.");
		}

    	TLContext tlContext = TLContext.getContext();
    	if (tlContext == null) {
    		throw new IllegalStateException("Knowledge base operations require a valid thread context.");
    	}
		
    	final ReplayContext replayContext = new ReplayContext(this, tlContext.getContextId(), getLastRevision() + 1);
		installContext(replayContext);
		
		final Protocol protocol = _configuration.isStopReplayOnError() ? new KnowledgeBaseProtocol()
			: new LogProtocol(DefaultEventWriter.class);
		return new ReplayWriter(this, protocol, replayContext);
	}

	/**
	 * {@link EventWriter} which is given if someone calls {@link KnowledgeBase#getReplayWriter()}
	 * 
	 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
	 */
	private final class ReplayWriter extends DefaultEventWriter {
		
			private ReplayContext replayContext;

			public ReplayWriter(DBKnowledgeBase dbKnowledgeBase, Protocol protocol, ReplayContext replayContext) {
				super(dbKnowledgeBase, protocol);
				this.replayContext = replayContext;
			}

			/**
			 * The last revision number assigned in a commit event.
			 */
			private long lastRevision;

		@Override
		public void write(ChangeSet cs) {
			checkCorrectRevision(cs.getRevision());
			super.write(cs);
		}

		private void checkCorrectRevision(final long eventRev) {
			final CommitContext currentKBContext = ((DBKnowledgeBase) _kb).getCurrentContext();
			if (currentKBContext != replayContext) {
				throw new IllegalStateException("CommitContext in KnowledgeBase '" + currentKBContext
					+ "' differs from Replay-CommitContext '" + replayContext + "'");
			}
			if (eventRev != Revision.FIRST_REV && eventRev != replayContext.getNextCommitNumber()) {
				/* First revision must be pass as in doCommit the revision of this KnowledgeBase is
				 * adopted to the first revision of the source KB. Moreover the initial branch
				 * creation (TRUNK) is reported (and ignored by #visitBranch()) */
				throw new IllegalArgumentException("Try to write event for revision " + eventRev
					+ " out of sequence. Expected event for revision " + replayContext.getNextCommitNumber());
			}
		}

		@Override
		protected State doCommit(CommitEvent event, State currentState) {
			long nextRevision = event.getRevision();
				
			if (nextRevision == Revision.FIRST_REV) {
				// The first revision is created during system setup. During
				// replay, only revision attributes can be modified.
				replayContext.setUpdateCommitNumber(nextRevision);
			}

			// Make sure that the revision is created, even if no
			// modifications were done.
			replayContext.beginReplay();

			replayContext.setAuthor(event.getAuthor());
			replayContext.setDate(event.getDate());
			replayContext.setLogMessage(event.getLog());
			replayContext.setCommitNumber(nextRevision);

			this.lastRevision = nextRevision;

			// Commit in any case, since branch replay is not automatically
			// committed during replay.
			try {
				internalCommit(false);
			} catch (KnowledgeBaseException ex) {
				throw _protocol.fatal(enhanceErrorMessage(event, "Failed to commit replayed events."), ex);
			}

			return State.clean;
		}
			
		@Override
		protected void beginCommit() {
			replayContext.beginReplay();
		}

		@Override
		protected void internalCommit(boolean finalized) throws KnowledgeBaseException {
			final Transaction tx = replayContext.resetReplayTransaction();
			if (!finalized) {
				if (tx != null) {
					// Commit in any case, since branch replay is not automatically
					// committed during replay.
					tx.commit();
				} else {
					_protocol.info("Commit without modification");
				}
			} else {
				if (tx != null) {
					tx.rollback();
					_protocol
						.error("Unable to close event writer normally: There are still changes but no final commit.");
				}
			}
		}

			@Override
			protected void doCreateBranch(BranchEvent event) {
				BranchImpl baseBranch = (BranchImpl) _kbHistory.getBranch(event.getBaseBranchId());
				Revision baseRevision = _kbHistory.getRevision(event.getBaseRevisionNumber());
				
			beginCommit();

				Set<MetaObject> branchedTypes = resolveBranchedTypes(event.getBranchedTypeNames());
				long replayedBranchId = event.getBranchId();
					
				try {
		    		Connection commitConnection = replayContext.getConnection();
		    		
					// Ensure that the sequence manager keeps in sync with
					// created branches during migration.
		    		while (true) {
					long nextBranch =
						sequenceManager.nextSequenceNumber(dbHelper, commitConnection, dbHelper.retryCount(),
							BRANCH_SEQUENCE);
		    			
		    			if (nextBranch == replayedBranchId) {
		    				break;
		    			} else if (nextBranch > replayedBranchId) {
							throw _protocol.fatal(
								"Branch replay uses already existing branch ID '" + replayedBranchId + "', expected '" + nextBranch + "'.");
		    			}
		    		}
					
					BranchImpl newBranch = DBKnowledgeBase.this.doCreateBranch(
					replayContext.getConnection(),
						baseBranch, 
						baseRevision.getCommitNumber(), 
						replayContext.getCommitNumber(), 
						replayedBranchId, 
						branchedTypes);
					
					DBKnowledgeBase.this.enterBranch(newBranch);
				} catch (SQLException ex) {
					throw _protocol.fatal("Branch replay failed.", ex);
				}
			}
			
		@Override
		public void flush() {
			if (replayContext.isClosed()) {
				/* Do not close twice when something went wrong earlier to prevent hiding the real
				 * problem. */
				return;
			}
			if (replayContext.hasCommitConnection()) {
				/* Do not initialise connection when actually no change occurred. */
				try {
					replayContext.getConnection().commit();
				} catch (SQLException ex) {
					throw new KnowledgeBaseRuntimeException("Replay failed.", ex);
				}
			}
		}

		@Override
		public void close() {
			if (replayContext.isClosed()) {
				/* Do not close twice when something went wrong earlier to prevent hiding the real
				 * problem. */
				return;
			}
			if (replayContext.hasCommitConnection()) {
				/* Do not initialise connection when actually no change occurred. */
				try {
					RowLevelLockingSequenceManager.resetSequence(replayContext.getConnection(), REVISION_SEQUENCE,
						lastRevision);
				} catch (SQLException ex) {
					throw new KnowledgeBaseRuntimeException("Replay failed.", ex);
				}

			}
			super.close();

			dropDBContext();
			replayContext.close(true);
		}

		@Override
		public void rollbackCurrentChanges() {
			final Transaction tx = replayContext.resetReplayTransaction();
			if (tx != null) {
				tx.rollback(null);
			}
		}

	}

	// Deprecated API.
	
    /**
	 * @deprecated Use either {@link #getCurrentContext()} or {@link #createCommitContext()}.
	 */
	@Override
	@SuppressWarnings("deprecation")
    public CommitContext getCommitContext(boolean create) {
    	if (create) {
    		return internalCreateDBContext();
    	} else {
    		return lookupDBContext();
    	}
    }
    
	@Override
	public ChangeSetReader getDiffReader(Revision startRev, Branch startBranch, Revision stopRev, Branch stopBranch, boolean detailed) {

		long startRevision = startRev.equals(Revision.CURRENT) ? getLastRevision() + 1 : startRev.getCommitNumber();
		long stopRevision = stopRev.equals(Revision.CURRENT) ? getLastRevision() + 1 : stopRev.getCommitNumber();

		if (!detailed) {
			try {
				DiffEventReader diffReader = new DiffEventReader(this, startRevision, startBranch.getBranchId(), stopRevision, stopBranch.getBranchId());
				return new DefaultChangeSetReader(diffReader);
			} catch (SQLException ex) {
				throw new KnowledgeBaseRuntimeException(ex);
			}
		}
		
		if (startBranch.equals(stopBranch)) {
			/*
			 * It is essentially an undo from revision start to revision end
			 * (inclusive) (if startRev > stopRev) or a redo from revision start
			 * to revision end (if startRev < stopRev)
			 */
			try {
				if (startRevision <= stopRevision) {
					/*
					 * Don't report the start revision since these events must
					 * not be done again
					 */
					startRevision += 1;
					// This is done since the larger revision is exclusive
					stopRevision += 1;
				} else {
					// This is done since the larger revision is exclusive
					startRevision += 1;
				}
				EventReader<? extends KnowledgeEvent> reader = internalGetReader(startRevision, stopRevision, null,
					Collections.singleton(startBranch.getBranchId()), null, null, /*order*/ null);
				return new DefaultChangeSetReader(reader);
			} catch (SQLException ex) {
				throw new KnowledgeBaseRuntimeException(ex);
			}
		}
		
		final long commonParentRevision = getCommonParentRevision(startRevision, startBranch, stopRevision, stopBranch);
		/*
		 * since all event happen during that revision events must not be
		 * reported. It suffices to consider with rev +1
		 */
		final long relevantParentRevision = commonParentRevision + 1;

		final List<EventReader<? extends KnowledgeEvent>> readers = new ArrayList<>();
		try {
			if (startRevision > commonParentRevision) {
				fillReaders(startRevision, relevantParentRevision, startBranch, readers);
			} else {
				/*
				 * startBranch is a parent branch of stop branch and stop branch
				 * was branched later than start revision.
				 */
			}
			if (commonParentRevision < stopRevision) {
				fillReaders(relevantParentRevision, stopRevision + 1, stopBranch, readers);
			} else {
				/*
				 * stop branch is a parent branch of start branch and start
				 * branch was branched later than stop revision.
				 */
			}
		} catch (SQLException ex) {
			throw new KnowledgeBaseRuntimeException(ex);
		}

		return new DefaultChangeSetReader(new CumulatedEventReader<>(readers));
	}

	/**
	 * Adds to the given {@link EventReader} list {@link EventReader} to get the diff events to come
	 * from the given start revision to the end revision.
	 * 
	 * <ul>
	 * <li>If start revision equals end revision, nothing is done.</li>
	 * <li>If start revision is larger than end revision, then the given branch determines the
	 * branch to start from.</li>
	 * <li>If start revision is less than end revision, then the given branch determines the branch
	 * to end.</li>
	 * </ul>
	 * 
	 * @see #internalGetReader(long, long, Set, Set, BranchEventReader, CommitEventReader,
	 *      Comparator)
	 */
	private void fillReaders(long startRevision, long endRevision, Branch branch, List<? super EventReader<? extends KnowledgeEvent>> readers) throws SQLException {
		if (startRevision == endRevision) {
			return;
		}
		
		final long baseRevision = branch.getBaseRevision().getCommitNumber();
		final Set<Long> requestedBranches = Collections.singleton(Long.valueOf(branch.getBranchId()));
		
		if (startRevision > endRevision) {
			final long endRev = branch.getCreateRevision().getCommitNumber();
			final long actualEndRevision = Math.max(endRev, endRevision);
			readers.add(internalGetReader(startRevision, actualEndRevision, null, requestedBranches, null, null, /*order*/ null));
			if (baseRevision > endRevision) {
				fillReaders(baseRevision, endRevision, branch.getBaseBranch(), readers);
			}
		} else {
			if (baseRevision > startRevision) {
				fillReaders(startRevision, baseRevision + 1, branch.getBaseBranch(), readers);
			}
			final long startRev = branch.getCreateRevision().getCommitNumber() + 1;
			final long actualStartRevision = Math.max(startRev, startRevision);
			readers.add(internalGetReader(actualStartRevision, endRevision, null, requestedBranches, null, null, /*order*/ null));
		}
	}

	private long getCommonParentRevision(long revision1, Branch branch1, long revision2, Branch branch2) {
		
		// contains all branches on the way to come from branch1 to trunk
		final List<Branch> pathToTrunk = new ArrayList<>();
		pathToTrunk.add(branch1);
		Branch baseBranch1 = branch1;
		while ((baseBranch1 = baseBranch1.getBaseBranch()) != null) {
			pathToTrunk.add(baseBranch1);
		}

		// check whether branch2 is a parent branch of branch1
		final int index = pathToTrunk.indexOf(branch2);
		if (index != -1) {
			// branch2 is a parent branch of branch1
			if (index == 0) {
				// branch1 and branch2 are the same
				return min(revision1, revision2);
			} else {
				return min(pathToTrunk.get(index - 1).getBaseRevision().getCommitNumber(), revision2);
			}
		}

		Branch baseBranch2 = branch2;
		do {
			final int baseBranchIndex = pathToTrunk.indexOf(baseBranch2.getBaseBranch());
			if (baseBranchIndex != -1) {
				// the base branch of baseBranch2 is a parent of branch1
				final long baseRevision = baseBranch2.getBaseRevision().getCommitNumber();
				if (baseBranchIndex == 0) {
					// branch1 is the base branch of baseBranch2
					return min(revision1, baseRevision);
				} else {
					return min(pathToTrunk.get(baseBranchIndex - 1).getBaseRevision().getCommitNumber(), baseRevision);
				}
			}
		}
		while ((baseBranch2 = baseBranch2.getBaseBranch()) != null);

		throw new UnreachableAssertion("Trunk should be a common parent branch");
	}

	private static Expression createFlexValueMatch(String attributeName, Object value) {
		if (value == null) {
			// Type for null. Choose random primitive type, as flex attributes does not have a
			// declared type.
			MOPrimitive nullType = MOPrimitive.STRING;
			return isNull(InternalExpressionFactory.flexTyped(nullType, attributeName));
		}
		MetaObject primitiveType = AbstractMetaObject.typeOfValue(value);
		Expression flex = InternalExpressionFactory.flexTyped(primitiveType, attributeName);
		return ExpressionFactory.eq(true, flex, ExpressionFactory.literal(value));
	}

	/**
	 * Creates an {@link Expression} which expresses the equality of the data in the given column
	 * and the given value.
	 */
	private static Expression createValueMatch(MOAttribute atribute, Object value) {
		Expression columnValue;
		if (atribute instanceof MOReference) {
			columnValue = InternalExpressionFactory.referenceTyped((MOReference) atribute);
		} else {
			columnValue = InternalExpressionFactory.attributeTyped(atribute);
		}
		if (value == null) {
			return isNull(columnValue);
		} else {
			MetaObject valueType = atribute.getMetaObject();
			if (atribute instanceof MOReference) {
				return ExpressionFactory.eq(true, columnValue, ExpressionFactory.literal(value));
			} else {
				// case-insensitive search for an attribute which is not a string is nonsense
				boolean binary = !MOPrimitive.STRING.equals(valueType) || ((DBAttribute) atribute).isBinary();
				return ExpressionFactory.eq(binary, columnValue, ExpressionFactory.literal(value));
			}
		}
	}

	MOKnowledgeItemImpl getRevisionType() {
		return lookupType(BasicTypes.REVISION_TYPE_NAME);
	}

	MOKnowledgeItem getItemType() {
		return lookupType(BasicTypes.ITEM_TYPE_NAME);
	}

	MOKnowledgeItem getObjectType() {
		return lookupType(BasicTypes.OBJECT_TYPE_NAME);
	}

	MOKnowledgeItem getAssociationType() {
		return lookupType(BasicTypes.ASSOCIATION_TYPE_NAME);
	}

	MOKnowledgeItem getBranchSwitchType() {
		return lookupType(BranchSupport.BRANCH_SWITCH_TYPE_NAME);
	}

	MOKnowledgeItemImpl getBranchType() {
		return lookupType(BasicTypes.BRANCH_TYPE_NAME);
	}

	/**
	 * Returns the object key which is known by this {@link KnowledgeBase} and represents the same
	 * object as the given key, or the given key if no key is known which represents the same
	 * object.
	 * 
	 * @param key
	 *        must not be <code>null</code>
	 */
	public ObjectKey getCachedKey(ObjectKey key) {
		KnowledgeItemInternal cachedObject = cleanupAndLookupCache(key, getSessionRevision());
		if (cachedObject != null) {
			return cachedObject.tId();
		} else {
			return key;
		}
	}

	final KnowledgeItemInternal cleanupAndLookupCache(ObjectKey key, long lookupRevision) {
		KnowledgeItemInternal result;
		synchronized (cache) {
			syncCacheCleanupReferences();
			result = syncCacheLookupCache(key, lookupRevision, false);
		}
		return result;
	}

	/**
	 * @see DBTypeRepository#getSystemTypes()
	 */
	Collection<? extends MOKnowledgeItem> getSystemTypes() {
		return moRepository.getSystemTypes();
	}

	/**
	 * Adds internal arguments that can later be accessed by all queries.
	 * 
	 * @param historyContext
	 *        Value that can be accessed later when using parameter
	 *        {@link SQLBuilder#HISTORY_CONTEXT_PARAM}.
	 * @param dataRevision
	 *        Value that can be accessed later when using parameter
	 *        {@link SQLBuilder#REQUESTED_REVISION_PARAM}.
	 */
	Object[] addInternalArguments(AbstractQuery<?> monomorphicQuery, final Branch requestedBranch,
			Long historyContext, Long dataRevision, Integer startRow, Integer stopRow, Object[] oldArgs) {
		int newArraySize = oldArgs.length;
		BranchParam branchParam = monomorphicQuery.getBranchParam();
		List<MetaObject> allOfTypes = monomorphicQuery.getAllOfTypes();
		if (branchParam == BranchParam.single) {
			newArraySize += allOfTypes.size();
		}
		// Data revision
		newArraySize++;
		// History context
		newArraySize++;
		RangeParam rangeParam = monomorphicQuery.getRangeParam();
		switch (rangeParam) {
			case range:
				newArraySize += 2;
				break;
			case head:
				newArraySize += 1;
				break;
			default:
				break;
		}

		Object[] arguments = new Object[newArraySize];
		System.arraycopy(oldArgs, 0, arguments, 0, oldArgs.length);
		int nextIndex = oldArgs.length;
		if (branchParam == BranchParam.single) {
			for (int i = 0; i < allOfTypes.size(); i++) {
				arguments[nextIndex++] =
					Long.valueOf(requestedBranch.getBaseBranchId(allOfTypes.get(i)));
			}
		}
		arguments[nextIndex++] = dataRevision;
		arguments[nextIndex++] = historyContext;
		switch (rangeParam) {
			case range:
				arguments[nextIndex++] = startRow;
				arguments[nextIndex++] = stopRow;
				break;
			case head:
				arguments[nextIndex++] = stopRow;
				break;
			default:
				break;
		}
		return arguments;
	}

	/**
	 * Creates a {@link KnowledgeItemInternal} and initializes it with data from the given
	 * {@link ResultSet}.
	 * 
	 * @param connection
	 *        The connection used to get result set.
	 * @param resultSet
	 *        the result set to fetch data for the result item from.
	 * @param dbOffset
	 *        First index of the type in the result set.
	 * @param type
	 *        the expected type of the result {@link KnowledgeItem}
	 * @param historyContext
	 *        history context of the desired object
	 * @param dataRevision
	 *        the data revision of the requested items.
	 */
	KnowledgeItemInternal findOrCreateItem(PooledConnection connection, ResultSet resultSet, int dbOffset,
			MOKnowledgeItem type, long historyContext, long dataRevision) throws SQLException {

		// Create identifier
		KnowledgeItemFactory factory = type.getImplementationFactory();
		DBObjectKey identifier =
			factory.createIdentifier(dbHelper, resultSet, DBAttribute.DEFAULT_DB_OFFSET, historyContext, type);
	
		return findOrCreateItem(connection, resultSet, dbOffset, identifier, dataRevision);
	}

	/**
	 * Creates a {@link KnowledgeItemInternal} with the given identifier and initializes its values
	 * from the given {@link ResultSet}.
	 * 
	 * <p>
	 * <b>Note:</b> It is expected, but not checked, that the data belong to the object identified
	 * by the given key.
	 * </p>
	 * 
	 * @param connection
	 *        The connection used to get result set.
	 * @param resultSet
	 *        the result set to fetch data for the result item from.
	 * @param dbOffset
	 *        First index of the type of the new item in the result set.
	 * @param identifier
	 *        The identifier for the new item.
	 * @param dataRevision
	 *        The revision of the data of the result set.
	 */
	KnowledgeItemInternal findOrCreateItem(PooledConnection connection, ResultSet resultSet, int dbOffset,
			DBObjectKey identifier, long dataRevision) throws SQLException {

		KnowledgeItemInternal cachedCopy;
		synchronized (cache) {
			syncCacheCleanupReferences();
			
			cachedCopy = syncCacheLookupCache(identifier, dataRevision, false);
			if (cachedCopy == null) {
				KnowledgeItemInternal newItem = createItem(connection, resultSet, dbOffset, identifier);
				syncCacheInsertCache(newItem, dataRevision);
				
				return newItem;
			}
		}
		if (!HistoryUtils.isCurrent(cachedCopy)) {
			// no refetch necessary because historic object never change.
			return cachedCopy;
		}
		if (cachedCopy instanceof DBKnowledgeItem) {
			// Update values.
			((DBKnowledgeItem) cachedCopy).refetch(resultSet, dbOffset);
		}
		
		// Check that cached copy is up to date.
		return cachedCopy;
	}

	private KnowledgeItemInternal createItem(PooledConnection connection, ResultSet resultSet, int dbOffset,
			DBObjectKey identifier) throws SQLException {
		MOKnowledgeItem resultType = identifier.getObjectType();
		AbstractDBKnowledgeItem result;
		if (identifier.getHistoryContext() == Revision.CURRENT_REV) {
			result = newKnowledgeItem(resultType);
		} else {
			result = newImmutableItem(resultType);
		}

		// initialise identifier
		result.initIdentifier(identifier);

		// Load attribute values
		result.loadAttributeValues(resultSet, dbOffset);

		// Notify item loaded from database
		result.onLoad(connection);
		return result;
	}

	/**
	 * Method called by reflection from tests to reset cache.
	 * 
	 * @see "test.com.top_logic.KBTestUtils#clearCache(KnowledgeBase)"
	 */
	@CalledByReflection
	private void clearCacheForTests() {
		for (DBObjectKey key : cache.keySet()) {
			java.lang.ref.Reference<KnowledgeItemInternal> reference = key.getReference();
			if (reference instanceof IDReference) {
				((IDReference) reference).destroy();
			}
			key.updateReference(null);
		}
		cache.clear();
	}

	/**
	 * Registers the old values for cleanup if they are not longer used.
	 * 
	 * @param oldValues
	 *        The values to remove when not longer needed.
	 * @param pointerToOldValues
	 *        The Pointer now having the given old values as {@link Values#formerValidity()}
	 */
	<T extends ValidityChain<T>> void registerForCleanup(T oldValues, T pointerToOldValues) {
		/* Values older than the that value objects are not longer needed if the min validity is
		 * reached. Therefore The can be removed when the event with that revision is removed. */
		UpdateChainLink sessionRevision = getSessionRevision(TLContextManager.getSubSession());
		/* can not use maximum validity of old values as cleanup revision, because there might be a
		 * gap, between the old value and the pointer to them. */
		long cleanupRevision = pointerToOldValues.minValidity() - 1;

		if (sessionRevision.getRevision() > cleanupRevision) {
			registerForCleanup(sessionRevision, pointerToOldValues);
			return;
		}
		while (sessionRevision.getRevision() < cleanupRevision) {
			sessionRevision = sessionRevision.getNextUpdate();
		}
		registerForCleanup(sessionRevision, pointerToOldValues);
	}

	void registerForCleanup(UpdateChainLink sessionRevision, CleanupOldValues cleanupAction) {
		sessionRevision.registerForCleanup(cleanupAction);
	}

	@Override
	public <T, E1 extends Throwable, E2 extends Throwable> T withoutModifications(ComputationEx2<T, E1, E2> job)
			throws E1, E2 {
		DBContext before = installContext(ImmutableDBContext.INSTANCE);
		try {
			return job.run();
		} finally {
			installContext(before);
		}
	}

	final DBContext installContext(DBContext dbContext) {
		TLInteractionContext currentInteraction = currentInteraction();
		DBContext before = currentInteraction.set(_localDBContext, dbContext);
		currentInteraction.addUnboundListener(this);
		return before;
	}

	@Override
	public <T, E1 extends Throwable, E2 extends Throwable> T inRevision(long rev, ComputationEx2<T, E1, E2> job)
			throws E1, E2 {
		long sessionRevision = getSessionRevision();
		DBContext before = installContext(rev, ImmutableDBContext.INSTANCE);
		try {
			return job.run();
		} finally {
			installContext(sessionRevision, before);
		}
	}

	private DBContext installContext(long rev, DBContext dbContext) {
		TLInteractionContext currentInteraction = currentInteraction();
		DBContext before = currentInteraction.set(_localDBContext, dbContext);
		currentInteraction.addUnboundListener(this);

		currentInteraction.updateInteractionRevision(this, rev);
		return before;
	}

	/**
	 * <code>null</code> when no changes occurred, or the modified result.
	 */
	<E> List<E> adaptToTransaction(SimpleQuery<E> simpleQuery, RevisionQuery<E> query, RevisionQueryArguments arguments,
			List<E> dbResult, boolean adaptInline) {
		DBContext currentDBContext = getCurrentDBContext();
		if (currentDBContext == null) {
			/* No local changes. */
			return dbResult;
		}
		long requestedHistoryContext = arguments.getRequestedRevision();
		if (requestedHistoryContext != Revision.CURRENT_REV) {
			/* There are no changes on historic results. */
			return dbResult;
		}

		AdaptToContext<E> adaption = new AdaptToContext<>(this, currentDBContext, simpleQuery, query, arguments);
		return adaption.adaptResult(dbResult, adaptInline);
	}

	/**
	 * <code>null</code> when no changes occurred, or the modified result.
	 */
	<E> List<E> adaptToUpdate(final UpdateEvent update, final SimpleQuery<E> simpleQuery, final RevisionQuery<E> query,
			final RevisionQueryArguments arguments, final List<E> dbResult, final boolean adaptInline) {
		assert arguments.getRequestedRevision() == Revision.CURRENT_REV : "Can only adapt result for current query. Search revision: "
			+ arguments.getRequestedRevision();

		AdaptToUpdate<E> adaption = new AdaptToUpdate<>(update, simpleQuery, query, arguments);
		return adaption.adaptResult(dbResult, adaptInline);
	}

}