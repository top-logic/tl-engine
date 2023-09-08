/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.journal;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.TLID;
import com.top_logic.basic.col.ObjectFlag;
import com.top_logic.basic.col.TupleFactory;
import com.top_logic.basic.col.TupleFactory.Tuple;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.db.sql.Batch;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.db.sql.SQLDelete;
import com.top_logic.basic.db.sql.SQLInsert;
import com.top_logic.basic.db.sql.SQLQuery;
import com.top_logic.basic.db.sql.SQLQuery.Parameter;
import com.top_logic.basic.db.sql.SQLSelect;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.util.Computation;
import com.top_logic.knowledge.journal.persistancy.ChangeEntryJournalPersistancyHandler;
import com.top_logic.knowledge.journal.persistancy.JournalPersistancyHandler;
import com.top_logic.knowledge.journal.persistancy.MessageJournalPersistancyHandler;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.db2.RowLevelLockingSequenceManager;
import com.top_logic.util.TLContext;

/**
 * Handles journal entries for commitable objects.
 * 
 * The journal stores changes to commitable (and journallable) objects.
 * dkh: hmm ... it journals whatever is passed to it. DBContext checks for Journallables and 
 * asks if isToJournal, which is true if type is registered (somehow) in project's xml.
 * 
 * TODO TSA split into read (== default) and write cache.
 * TODO TSA explain how to register objects to be journalled
 * TODO TSA can this be a subclass of the NewDataManager ?
 * TODO TSA make terminate(or at least flush()) public and call at end of StartStopListener.
 * TODO TSA /KHA terminate does not doe its Job
 * 
 * The journal is organized in three layers. A <code>JournalLine</code>
 * holds a unique commit id and the id of the committing user. 
 * A set of <code>JournalEntries</code> is associated to each 
 * journal line.
 * Each journal entry consists of <code>JournalAttributeEntry</code>s.
 * Each journal attribute entry holds the information about a single attribut.
 * 
 * Depending on the type of attribute entry this information can consist of
 * pre and post value of an attribute, or some generated message, or ...
 * 
 * @author    <a href="mailto:tsa@top-logic.com">Theo Sattler</a>
 * 
 */
@ServiceDependencies({
	ApplicationConfig.Module.class,
	ConnectionPoolRegistry.Module.class
})
public class JournalManager extends ManagedClass {

	private static final String MESSAGE_COLUMN = "MESSAGE";

	private static final String CAUSE_COLUMN = "CAUSE";

	private static final String ATTR_TYPE_COLUMN = "ATTR_TYPE";

	private static final String ATTR_COLUMN = "ATTR";

	private static final String TYPE_COLUMN = "TYPE";

	private static final String IDENTIFIER_COLUMN = "IDENTIFIER";

	private static final String USER_ID_COLUMN = "USER_ID";

	private static final String TIME_COLUMN = "TIME";

	private static final String COMMIT_ID_COLUMN = "COMMIT_ID";

	private static final String JOURNAL_LINE_SEQUENCE = "journal";

    private static final String CONNECTION_POOL_PROPERTY = "connectionPool";

	/** The prefix used in the configuration to indentify ko type entries */
    protected static final String CONFIGURATION_TYPE_ENTRY_PREFIX = "use_";
    
    // TODO TSA Use IN (koid, koid ,,,) and ORDER BY to speed up retrieveal.
    
	// CHange all

	private final SQLQuery<SQLInsert> _createJounralLineIdSQLQuery;

	private final SQLQuery<SQLInsert> _createJounralEntrySQLQuery;

	private final SQLQuery<SQLSelect> _getCommitInfosForKOAndDateSQLQuery;

	private final SQLQuery<SQLSelect> _getCommitInfosForKOSQLQuery;

	private final SQLQuery<SQLDelete> _removeJournalEntriesForKOSQLQuery;

	private final SQLQuery<SQLDelete> _removeJournalAttributesPreForKOSQLQuery;

	private final SQLQuery<SQLDelete> _removeJournalAttributesPostForKOSQLQuery;

	private final SQLQuery<SQLDelete> _removeJournalAttributesMessageKOSQLQuery;

	private final SQLQuery<SQLSelect> _getUserAccessSQLQuery;

	private final SQLQuery<SQLSelect> _getUserAccessWithDateSQLQuery;

	/** SQLStatmenet to insert a new journal entry */
	private static final int NUMBER_PARAMETERS_CREATE_JOURNAL_ENTRY = 3;

    /** Used to synchronize accessing of the JournalManager */
	private final ObjectFlag<ThreadState> mutex = new ObjectFlag<>(ThreadState.DEAD);
    
    private static enum ThreadState {
    	
    	/**
    	 * The thread is alive and running
    	 */
    	ALIVE,
    	
		/**
		 * Someone has called {@link JournalManagerThread#terminate()}. That
		 * forces the thread to complete potentially added lines and then to
		 * terminate.
		 */
    	TERMINATED,
    	
		/**
		 * State set by the thread to indicate that it is dead
		 */
    	DEAD
    }
    

    /** the name of the auto executer thread */
    private static final String THREAD_NAME = "JournalManager";
    
    protected       DBHelper        dbHelper;
    
    /** the journaled ko types (a list of meta object names) */
    private Collection<String> types;
    
    /** indicates that there is anything to journal at all (types is not empty) */
	private final boolean isActiveFlag;
    
    /** the journaler thread */
	private JournalManagerThread thread;
    
    /** 
     * map of all registered persistancy handlers
     * key:   class of JournalAttributeEntry
     * value: instance of JournalEntryPersistancyHandler 
     */
    private Map<Class<?>, JournalPersistancyHandler> persistancyHandler;
    
    private List <JournalPersistancyHandler>     persistancyReaders;

	private ConnectionPool connectionPool;

	private final RowLevelLockingSequenceManager _sequences;

	/**
	 * Configuration for the data source of the {@link JournalManager}.
	 * 
	 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
	 */
	public interface Config extends ServiceConfiguration<JournalManager> {
		/**
		 * Entries to declare the journaled knowledge objects.
		 */
		@ListBinding()
		List<String> getJournaledKnowledgeObjects();

		/**
		 * Connection Pool.
		 */
		@StringDefault(ConnectionPoolRegistry.DEFAULT_POOL_NAME)
		String getConnectionPool();
	}

	/**
	 * @param context
	 *        {@link InstantiationContext} context to instantiate sub configurations.
	 * @param config
	 *        Configuration for {@link JournalManager}.
	 */
	public JournalManager(InstantiationContext context, Config config) throws SQLException {
		super(context, config);

		_sequences = new RowLevelLockingSequenceManager();

        // initialize persistency handler map
        // TODO TSA make this configurable
        this.persistancyHandler = new HashMap<>();
		this.persistancyHandler.put(ChangeJournalAttributeEntry.class, createAttributeHandler(config));
		this.persistancyHandler.put(MessageJournalAttributeEntry.class, new MessageJournalPersistancyHandler());
		this.persistancyReaders = new ArrayList<>(persistancyHandler.values());
		// read the types to consider
		this.types = new HashSet<>(config.getJournaledKnowledgeObjects());
		this.isActiveFlag = !this.types.isEmpty();

		if (this.isActiveFlag) {
			String poolName = config.getConnectionPool();
			this.connectionPool = ConnectionPoolRegistry.getConnectionPool(poolName);
			this.dbHelper = this.connectionPool.getSQLDialect();
		}

		_createJounralLineIdSQLQuery = createCreateJounralLineIdSQLQuery();
		_createJounralEntrySQLQuery = createCreateJounralEntrySQLQuery();
		_getCommitInfosForKOAndDateSQLQuery = createGetCommitInfosForKOAndDateSQLQuery();
		_getCommitInfosForKOSQLQuery = createGetCommitInfosForKOSQLQuery();
		_removeJournalEntriesForKOSQLQuery = createRemoveJournalEntriesForKOSQLQuery();
		_removeJournalAttributesPreForKOSQLQuery = createRemoveJournalAttributesPreForKOSQLQuery();
		_removeJournalAttributesPostForKOSQLQuery = createRemoveJournalAttributesPostForKOSQLQuery();
		_removeJournalAttributesMessageKOSQLQuery = createRemoveJournalAttributesMessageKOSQLQuery();
		_getUserAccessSQLQuery = createGetUserAccessSQLQuery();
		_getUserAccessWithDateSQLQuery = createGetUserAccessWithDateSQLQuery();
	}
	
	private SQLQuery<SQLInsert> createCreateJounralLineIdSQLQuery() {
		List<Parameter> parameters = parameters(parameterDef(DBType.LONG, "commitId"),
			parameterDef(DBType.DATETIME, "time"), parameterDef(DBType.STRING, "userId"));

		return
			query(parameters, insert(table("JOURNAL_LINE_STORAGE", NO_TABLE_ALIAS),
				Arrays.asList(COMMIT_ID_COLUMN, TIME_COLUMN, USER_ID_COLUMN),
				Arrays.asList(parameter(DBType.LONG, "commitId"), parameter(DBType.DATETIME, "time"),
					parameter(DBType.STRING, "userId"))));
	}

	private SQLQuery<SQLInsert> createCreateJounralEntrySQLQuery() {
		List<Parameter> parameters = parameters(parameterDef(DBType.LONG, "commitId"),
			parameterDef(DBType.ID, "identifier"), parameterDef(DBType.STRING, "type"));

		return
			query(parameters, insert(table("JOURNAL_ENTRY_STORAGE", NO_TABLE_ALIAS),
				Arrays.asList(COMMIT_ID_COLUMN, IDENTIFIER_COLUMN, TYPE_COLUMN),
				Arrays.asList(parameter(DBType.LONG, "commitId"), parameter(DBType.ID, "identifier"),
					parameter(DBType.STRING, "type"))));
	}

	private SQLQuery<SQLSelect> createGetCommitInfosForKOAndDateSQLQuery() {
		List<Parameter> parameters = parameters(parameterDef(DBType.ID, "idColumn"),
			parameterDef(DBType.DATETIME, "timeMin"), parameterDef(DBType.DATETIME, "timeMax"));

		return
			query(parameters,
				select(
					columns(columnDef(column("l", COMMIT_ID_COLUMN)), columnDef(column("l", TIME_COLUMN)),
						columnDef(column("l", USER_ID_COLUMN))),
					innerJoin(table("JOURNAL_LINE_STORAGE", "l"), table("JOURNAL_ENTRY_STORAGE", "e")),
					and(eq(column("l", COMMIT_ID_COLUMN), column("e", COMMIT_ID_COLUMN)),
						eq(column("e", IDENTIFIER_COLUMN), parameter(DBType.ID, "idColumn")),
						ge(column("l", TIME_COLUMN), parameter(DBType.DATETIME, "timeMin")),
						le(column("l", TIME_COLUMN), parameter(DBType.DATETIME, "timeMax")))));
	}

	private SQLQuery<SQLSelect> createGetCommitInfosForKOSQLQuery() {
		List<Parameter> parameters = parameters(parameterDef(DBType.ID, "idColumn"));

		return
			query(parameters,
				select(
					columns(columnDef(column("e", COMMIT_ID_COLUMN)), columnDef(column("l", TIME_COLUMN)),
						columnDef(column("l", USER_ID_COLUMN))),
					innerJoin(table("JOURNAL_ENTRY_STORAGE", "e"), table("JOURNAL_LINE_STORAGE", "l")),
					and(eq(column("e", IDENTIFIER_COLUMN), parameter(DBType.ID, "idColumn")),
						eq(column("e", COMMIT_ID_COLUMN), column("l", COMMIT_ID_COLUMN)))));
	}

	private SQLQuery<SQLDelete> createRemoveJournalEntriesForKOSQLQuery() {
		List<Parameter> parameters = parameters(parameterDef(DBType.ID, "id"));

		return
			query(parameters, delete(table("JOURNAL_ENTRY_STORAGE", NO_TABLE_ALIAS),
				eq(column(NO_TABLE_ALIAS, IDENTIFIER_COLUMN), parameter(DBType.ID, "id"))));
	}

	private SQLQuery<SQLDelete> createRemoveJournalAttributesPreForKOSQLQuery() {
		List<Parameter> parameters = parameters(parameterDef(DBType.ID, "id"));

		return
			query(parameters, delete(table("JOURNAL_ATTRIBUTE_PRE", NO_TABLE_ALIAS),
				eq(column(NO_TABLE_ALIAS, IDENTIFIER_COLUMN), parameter(DBType.ID, "id"))));
	}

	private SQLQuery<SQLDelete> createRemoveJournalAttributesPostForKOSQLQuery() {
		List<Parameter> parameters = parameters(parameterDef(DBType.ID, "id"));

		return
			query(parameters, delete(table("JOURNAL_ATTRIBUTE_POST", NO_TABLE_ALIAS),
				eq(column(NO_TABLE_ALIAS, IDENTIFIER_COLUMN), parameter(DBType.ID, "id"))));
	}

	private SQLQuery<SQLDelete> createRemoveJournalAttributesMessageKOSQLQuery() {
		List<Parameter> parameters = parameters(parameterDef(DBType.ID, "id"));

		return
			query(parameters, delete(table("JOURNAL_ATTRIBUTE_MESSAGE", NO_TABLE_ALIAS),
				eq(column(NO_TABLE_ALIAS, IDENTIFIER_COLUMN), parameter(DBType.ID, "id"))));
	}

	private SQLQuery<SQLSelect> createGetUserAccessSQLQuery() {
		List<Parameter> parameters = parameters(parameterDef(DBType.STRING, "type"));

		return
			query(parameters,
				select(
					columns(columnDef(column("l", TIME_COLUMN)), columnDef(column("l", USER_ID_COLUMN)),
						columnDef(column("e", "IDENTIFIER_COLUMN"))),
					innerJoin(table("JOURNAL_LINE_STORAGE", "l"), table("JOURNAL_ENTRY_STORAGE", "e")),
					and(eq(column("l", COMMIT_ID_COLUMN), column("e", COMMIT_ID_COLUMN)),
						eq(column("e", TYPE_COLUMN), parameter(DBType.STRING, "type")))));
	}

	private SQLQuery<SQLSelect> createGetUserAccessWithDateSQLQuery() {
		List<Parameter> parameters = parameters(parameterDef(DBType.STRING, "type"),
			parameterDef(DBType.DATETIME, "timeMin"), parameterDef(DBType.DATETIME, "timeMax"));

		return
			query(parameters,
				select(
					columns(columnDef(column("l", TIME_COLUMN)), columnDef(column("l", USER_ID_COLUMN)),
						columnDef(column("e", IDENTIFIER_COLUMN))),
					innerJoin(table("JOURNAL_LINE_STORAGE", "l"), table("JOURNAL_ENTRY_STORAGE", "e")),
					and(eq(column("l", COMMIT_ID_COLUMN), column("e", COMMIT_ID_COLUMN)),
						eq(column("e", TYPE_COLUMN), parameter(DBType.STRING, "type")),
						ge(column("l", TIME_COLUMN), parameter(DBType.DATETIME, "timeMin")),
						le(column("l", TIME_COLUMN), parameter(DBType.DATETIME, "timeMax")))));
	}

	@Override
	protected void startUp() {
		super.startUp();
		if (isActiveFlag && thread == null) {
			// start journal thread
			this.thread = new JournalManagerThread(THREAD_NAME);
			synchronized (mutex) {
				this.thread.start();
				mutex.set(ThreadState.ALIVE);
			}
		}
	}

	/**
	 * Creates the {@link JournalPersistancyHandler} for {@link ChangeJournalAttributeEntry}s.
	 * 
	 * @param config
	 *        The configuration of the {@link JournalManager} service.
	 */
	protected JournalPersistancyHandler createAttributeHandler(Config config) {
		return new ChangeEntryJournalPersistancyHandler();
	}

    /**
	 * Trigger the journaling of the journal line. The line is added to the
	 * lines to journal, and the journal thread is notified.
	 * 
	 * @param aJournalLine
	 *            the line to journal
	 */
    public void journal(JournalLine aJournalLine) {
        if (aJournalLine != null && ! aJournalLine.isEmpty()) {
       		this.thread.addLine(aJournalLine);
        } 
    }
    
    /**
     * Remove the complete journal for the given anIdentifier ?
     * 
     * TODO TSA add retry logic
     * 
     * @param anIdentifier the ko id for which the journal is removed
     */
	public synchronized void removeJournal(TLID anIdentifier) throws SQLException {
        
        PooledConnection connection = connectionPool.borrowWriteConnection();
        try {
			CompiledStatement theAttStatement =
				_removeJournalAttributesPreForKOSQLQuery.toSql(dbHelper);
			theAttStatement.executeUpdate(connection, anIdentifier);
			connection.commit();

			theAttStatement = _removeJournalAttributesPostForKOSQLQuery.toSql(dbHelper);
			theAttStatement.executeUpdate(connection, anIdentifier);
			connection.commit();

			theAttStatement = _removeJournalAttributesMessageKOSQLQuery.toSql(dbHelper);
			theAttStatement.executeUpdate(connection, anIdentifier);
			connection.commit();
            
			CompiledStatement theStatement =
				_removeJournalEntriesForKOSQLQuery.toSql(dbHelper);
			theStatement.executeUpdate(connection, anIdentifier);
			connection.commit();
        } catch (Exception e) {
            Logger.error("Could not remove journal for "+anIdentifier+".", e, this);
            connection.rollback();
        } finally {
        	connectionPool.releaseWriteConnection(connection);
        }
    }
    
    /**
	 * Get the complete journal for the given ko id and the given knowledge base
	 * 
	 * @param aKOId
	 *            the id of a knowledge object
	 * @param aKB
	 *            the name of the kb the ko resides in (not evaluated)
	 * 
	 * @return a List of journal results
	 */
	public List<JournalResult> getJournal(TLID aKOId, String aKB) {
        
        return this.getJournal(aKOId, aKB, -1, -1);
	}
    
    /**
     * Get the complete journal for the given ko id and the given knowledge base.
     * 
     * The from and to values are only used if from > 0 and to >= from. Otherwise,
     * the complete journal is returned.
     * 
     * @param aKOId
     *            the id of a knowledge object
     * @param aKB
     *            the name of the kb the ko resides in (not evaluated)
     * @param aFrom
     *            the start time of the requested journal
     * @param aTo
     *            the end time of the requested journal
     * 
     * @return a List of journal results for the given ko id committed between
     *         aFrom and aTo, empty List if no results found
     */
	public List<JournalResult> getJournal(TLID aKOId, String aKB, long aFrom, long aTo) {
		Collection<TLID> theCollection = Collections.singletonList(aKOId);
        return this.getJournal(theCollection, aKB, aFrom, aTo);
    }
    
    /**
     * Get the complete journal for the given ko id and the given knowledge base.
     * 
     * The from and to values are only used if from > 0 and to >= from. Otherwise,
     * the complete journal is returned.
     * 
     * @param someKOIds
     *            the ids of the knowledge objects
     * @param aKB
     *            the name of the kb the ko resides in (not evaluated)
     * @param aFrom
     *            the start time of the requested journal
     * @param aTo
     *            the end time of the requested journal
     *            
     * TODO TSA Use IN (koid,koid,...) to speed up query.
     * 
     * @return a List of journal results for the given ko id committed between
     *         aFrom and aTo, empty List if no results found
     */
	public List<JournalResult> getJournal(Collection<TLID> someKOIds, String aKB, long aFrom, long aTo) {
        boolean withDate;
        withDate = aFrom >= 0 && aTo >= aFrom;
        
        // the journal results requested
        List<JournalResult> theResult = new ArrayList<>(10);
        
		CompiledStatement theCommitInfoStatement = null;
        int               retry     = dbHelper.retryCount(); 
        
        List<JournalPersistancyHandler> theHandlers = this.getReadPersistancyHandlers();
        int  persistancyHandlerSize   = theHandlers.size();
        while (retry-- > 0) {
        	PooledConnection readConnection = connectionPool.borrowReadConnection();
        	try {
                if (withDate) {
					theCommitInfoStatement =
						_getCommitInfosForKOAndDateSQLQuery.toSql(dbHelper);
                } else {
					theCommitInfoStatement = _getCommitInfosForKOSQLQuery.toSql(dbHelper);
                }
                // this map contains the journal results indexed by the commit id
                // in order to allow you to find the respective journal result
                // when handling the data for a certain commit id
                Map<Long,JournalResult> theResults = new HashMap<>();
                // iterate over all participating kos
				Iterator<TLID> theKOIt = someKOIds.iterator();
                while (theKOIt.hasNext()) {
					TLID theKOId = theKOIt.next();
                    // this list contains only journal results relevant for the current ko.
                    // it is used later to get the entry date for these resultes
                    Collection<JournalResult> theJournalResults = new ArrayList<>();
                    
                    // get the journal results for the given ko
					List<Object> args = new LinkedList<>();
					args.add(theKOId);
					if (withDate) { // cache the Timestamps
						args.add(new Timestamp(aFrom));
						args.add(new Timestamp(aTo));
					}

					try (ResultSet theCommitInfos =
						theCommitInfoStatement.executeQuery(readConnection, args.toArray())) {
					while (theCommitInfos.next()) {
						long theCommitId = theCommitInfos.getLong(1);
						// the journal result may already have been created due to another ko
						JournalResult theJournalResult = theResults.get(theCommitId);
						if (theJournalResult == null) {
							long theTime = dbHelper.getTimestamp(theCommitInfos, 2).getTime();
							String theUserId = theCommitInfos.getString(3);
							theJournalResult =
								new JournalResult(theCommitId, theTime, theUserId, new ArrayList<>());
							// store the journal result in the entry map to allow reuse for other
							// kos
							theResults.put(theCommitId, theJournalResult);
							// store the journal result in the actual result array
							theResult.add(theJournalResult);
						}
						// store the journal result in the collection of results relevant for the
						// current ko
						theJournalResults.add(theJournalResult);
					}
					}
                    
					//iterate over the journal results relevant for the current ko
                    Iterator<JournalResult> theJRIt = theJournalResults.iterator();
					while (theJRIt.hasNext()) {
                        JournalResult theJR = theJRIt.next();
						long theCommitId = theJR.commitId;
                        
                        JournalResultEntryImpl theResultEntry = new JournalResultEntryImpl(theKOId, null);
                        
                        for (int i=0; i < persistancyHandlerSize; i++) {
                            JournalPersistancyHandler theHandler = theHandlers.get(i);
                            Collection<JournalResultAttributeEntry> theAttributes = theHandler.getJournal(theCommitId, theKOId, readConnection, dbHelper);
                            theResultEntry.add(theAttributes);
                        }
                        
                        JournalResult theJournalResult = theResults.get(theCommitId);
                        theJournalResult.entries.add(theResultEntry);
                        
					}
				}
				return theResult;
			} catch (SQLException sqx) {
			    // Better forget about the Connection
                readConnection.closeConnection(sqx);
                if (retry > 0 && dbHelper.canRetry(sqx)) {
                    Logger.info("Could not query for user retry " + retry, this);
                    continue;
                }
				throw new RuntimeException("Reading journal failed.", sqx);
			} finally {
			    connectionPool.releaseReadConnection(readConnection);
			}
		}
		return theResult;
    }
    
    /**
	 * Get the time, user ID and object ID of all accesses to objects of the given type in between
	 * the given time interval
	 * 
	 * @param aKOType
	 *        must not be <code>null</code>
	 * @param aFrom
	 *        may be <code>null</code>
	 * @param aTo
	 *        may be <code>null</code>
	 * @return a list of {@link Tuple}s containing access time ({@link Date}), user ID
	 *         ({@link String}) and object ID ({@link String}).
	 */
    public List<Tuple> getJournal(String aKOType, long aFrom, long aTo) {
        boolean withDate;
        withDate = aFrom >= 0 && aTo >= aFrom;

        // the journal results requested
        List<Tuple> theResult = new ArrayList<>();
        
		int retry = dbHelper.retryCount();
        
        while (retry-- > 0) {
            PooledConnection readConnection = connectionPool.borrowReadConnection();
            try {
				final CompiledStatement theCommitInfoStatement;
                if (withDate) {
					theCommitInfoStatement = _getUserAccessWithDateSQLQuery.toSql(dbHelper);
                } else {
					theCommitInfoStatement = _getUserAccessSQLQuery.toSql(dbHelper);
                }
                
				List<Object> args = new LinkedList<>();
				args.add(aKOType);
				if (withDate) {
					args.add(new Timestamp(aFrom));
					args.add(new Timestamp(aTo));
				}

				try (ResultSet theCommitInfos = theCommitInfoStatement.executeQuery(readConnection, args.toArray())) {
					while (theCommitInfos.next()) {
						long theTime = dbHelper.getTimestamp(theCommitInfos, 1).getTime();
						String theUserId = theCommitInfos.getString(2);
						String theKoId = theCommitInfos.getString(3);

						Tuple theKey = TupleFactory.newTuple(new Date(theTime), theUserId, theKoId);
						theResult.add(theKey);
					}
				}
               
               return theResult;
           } catch (SQLException sqx) {
               // Better forget about the Connection
               readConnection.closeConnection(sqx);
               if (retry > 0 && dbHelper.canRetry(sqx)) {
                   Logger.info("Query for journal failed due to db-connection reasons! Retry " + retry  + " of " + dbHelper.retryCount(), this);
                   continue;
               }
               Logger.error("Query for journal failed due to db-connection reasons!", sqx, this);
           } finally {
				connectionPool.releaseReadConnection(readConnection);
           }
       }
       return theResult;
    }
    
    /**
	 * Retrieves a journal of messages for attributes of an object.
	 * 
	 * @param requestedIdentifier
	 *        The object identifier of the object, for which the journal is
	 *        requested. May not be <code>null</code>.
	 * @param requestedAttr
	 *        The name of the attribute for which the journal is requested. If
	 *        the argument is <code>null</code>, a journal for all those
	 *        attributes of the requested object is retrieved that match the
	 *        other criteria.
	 * @param requestedAttrType
	 *        The type of attributes for which the journal is requested. If the
	 *        argument is <code>null</code>, a journal for all those
	 *        attributes of the requested object is retrieved that match the
	 *        other criteria.
	 * @param requestedCause
	 *        The cause the message for which the journal entries are requested.
	 *        If the argument is <code>null</code>, a journal for all
	 *        messages of all attributes of the requested object is retrieved
	 *        that match the other criteria.
	 * @return A list of {@link JournalResult} objects with
	 *         {@link JournalResultEntry}s containing
	 *         {@link MessageJournalResultAttributeEntry}s for the requested
	 *         messages.
	 */
	public List<JournalResult> getMessageJournal(TLID requestedIdentifier, String requestedAttr,
			String requestedAttrType, String requestedCause) throws SQLException {
    	assert requestedIdentifier != null;
    	
		String proc = createMessageJounralSQLQuery(requestedAttr, requestedAttrType, requestedCause);

    	PooledConnection readConnection = connectionPool.borrowReadConnection();
    	try {
			try (PreparedStatement statement = readConnection.prepareStatement(proc)) {
				// Fill statement parameters.
				{
					int index = 1;
					IdentifierUtil.setId(statement, index++, requestedIdentifier);
					if (requestedAttr != null)
						statement.setString(index++, requestedAttr);
					if (requestedAttrType != null)
						statement.setString(index++, requestedAttrType);
					if (requestedCause != null)
						statement.setString(index++, requestedCause);
				}

				try (ResultSet result = statement.executeQuery()) {

				ArrayList<JournalResult> journalResults = new ArrayList<>();
				JournalResult currentResult = null;
				JournalResultEntryImpl currentEntry = null;
				while (result.next()) {
					int index = 1;
					long commitId = result.getLong(index++);
					if ((currentResult == null) || (currentResult.commitId != commitId)) {
						long time = dbHelper.getTimestamp(result, index++).getTime();
						String userId = result.getString(index++);
						TLID identifier = IdentifierUtil.getId(result, index++);

						currentEntry = new JournalResultEntryImpl(identifier, new ArrayList<>());
						currentResult = new JournalResult(commitId, time, userId, Collections.singletonList(currentEntry));
						journalResults.add(currentResult);
					} else {
						index += 3;
					}

					String attr = result.getString(index++);
					String type = result.getString(index++);
					String cause = result.getString(index++);
					String message = result.getString(index++);

					currentEntry.add(new MessageJournalResultAttributeEntry(attr, type, cause, message));
				}
				return journalResults;
			}
			}
		} finally {
			connectionPool.releaseReadConnection(readConnection);
		}
    }

	private String createMessageJounralSQLQuery(String requestedAttr, String requestedAttrType, String requestedCause) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT ");
		sb.append("l." + dbHelper.columnRef(COMMIT_ID_COLUMN));
		sb.append(",");
		sb.append("l." + dbHelper.columnRef(TIME_COLUMN));
		sb.append(",");
		sb.append("l." + dbHelper.columnRef(USER_ID_COLUMN));
		sb.append(",");
		sb.append("a." + dbHelper.columnRef(IDENTIFIER_COLUMN));
		sb.append(",");
		sb.append("a." + dbHelper.columnRef(ATTR_COLUMN));
		sb.append(",");
		sb.append("a." + dbHelper.columnRef(ATTR_TYPE_COLUMN));
		sb.append(",");
		sb.append("a." + dbHelper.columnRef(CAUSE_COLUMN));
		sb.append(",");
		sb.append("a." + dbHelper.columnRef(MESSAGE_COLUMN));
		sb.append(" FROM ");
		sb.append(dbHelper.tableRef("JOURNAL_LINE_STORAGE") + " AS " + "l");
		sb.append(",");
		sb.append(dbHelper.tableRef("JOURNAL_ATTRIBUTE_MESSAGE") + " AS " + "a");
		sb.append(" WHERE ");
		sb.append("l." + dbHelper.columnRef(COMMIT_ID_COLUMN));
		sb.append(" = ");
		sb.append("a." + dbHelper.columnRef(COMMIT_ID_COLUMN));
		sb.append(" AND ");
		sb.append("a." + dbHelper.columnRef(IDENTIFIER_COLUMN));
		sb.append(" = ? ");
		if (requestedAttr != null) {
			sb.append(" AND ");
			sb.append("a." + dbHelper.columnRef(ATTR_COLUMN));
			sb.append(" = ? ");
		}
		if (requestedAttrType != null) {
			sb.append(" AND ");
			sb.append("a." + dbHelper.columnRef(ATTR_TYPE_COLUMN));
			sb.append(" = ? ");
		}
		if (requestedCause != null) {
			sb.append(" AND ");
			sb.append("a." + dbHelper.columnRef(CAUSE_COLUMN));
			sb.append(" = ? ");
		}
		sb.append(" ORDER BY ");
		sb.append("l." + dbHelper.columnRef(COMMIT_ID_COLUMN));
		String proc = sb.toString();
		return proc;
	}
    
	/**
	 * Ensure that all journal lines added so far are made persistent.
	 * 
	 * Used for testing and while shutting down.
	 */
	public void flush() {
		synchronized (mutex) {
			try {
				while (mutex.get() == ThreadState.ALIVE // ALIVE state implies there is a thread 
						&& thread.workToComplete()) {
					mutex.wait();
				}
			} catch (InterruptedException ex) {
				Logger.warn("Unexpect", ex, this);
			}
		}
	}
    
    /**
     * Check if kos of the given type are to be journalled.
     * 
     * @param aKoType   a knowledge object type
     * @return <code>true</code> if knowledge objects of the given type
     *     are to be journalled.
     */
    public boolean isToJournal(String aKoType) {
        return this.types.contains(aKoType);
    }
    
    /**
     * Add the given type name to the type to journall
     */
    public void addType(String aKOTypeName) {
        this.types.add(aKOTypeName);
    }
    
    /**
     * a List of the typenames (Strings) of those journalables for which
     * journaling is enabled at the moment
     */
    public List<String> getTypes(){
    	return new ArrayList<>(this.types);
    }

    /** Must run only once (via JournalManagerThread). */
    protected void journalLine(JournalLine aLine) throws SQLException {
        PooledConnection connection = connectionPool.borrowWriteConnection();
        try {
            /* boolean isConsistent = */ this.checkConsistency(aLine);

            // store  line info, with retry logic
            int  retry   = dbHelper.retryCount();
			long theCommitId = _sequences.nextSequenceNumber(dbHelper, connection, retry, JOURNAL_LINE_SEQUENCE);
            while (retry-- > 0) {
                try {
					CompiledStatement theStatement =
						_createJounralLineIdSQLQuery.toSql(dbHelper);
					// theStatement.clearParameters();
					theStatement.executeUpdate(connection, theCommitId, new Timestamp(aLine.getTime()),
						aLine.getUserId());
                } catch (SQLException sqlx) {
                    connection.closeConnection(sqlx);
                    if (retry > 0 && dbHelper.canRetry(sqlx)) {
                        Logger.info("journalLine retry " + retry, this);
                        continue;
                    }
                    throw sqlx;
                }
                break;
            }
            int size = aLine.size();
			int maxBatchSize = dbHelper.getMaxBatchSize(NUMBER_PARAMETERS_CREATE_JOURNAL_ENTRY);
			int numberBatches = 0;
			CompiledStatement createJournalStm = _createJounralEntrySQLQuery.toSql(dbHelper);

			try (Batch batch = createJournalStm.createBatch(connection)) {
				for (int i = 0; i < size; i++) {
					JournalEntry theEntry = aLine.get(i);

					batch.addBatch(theCommitId, theEntry.getIdentifier(), theEntry.getType());

					numberBatches++;
					if (numberBatches >= maxBatchSize) {
						batch.executeBatch();
						numberBatches = 0;
					}
				}
				if (numberBatches > 0) {
					/* int[] batchResult = */batch.executeBatch();
				}
			}
			for (int i = 0; i < size; i++) {
				JournalEntry theEntry = aLine.get(i);
				this.journalEntryAttributes(connection, theCommitId, theEntry);
			}
            
            connection.commit();
        } catch (SQLException sqx) {
            Logger.warn("Failed to journalLine, rolling back", sqx, this);
            connection.rollback();
        } finally {
        	connectionPool.releaseWriteConnection(connection);
        }
        
    }
    
    private boolean checkConsistency(JournalLine aLine) {
        if (aLine == null || aLine.isEmpty())   return true;
        boolean isConsistent = true;
		HashSet<TLID> theIdentifiers = new HashSet<>();
        for (Iterator<JournalEntry> theIt = aLine.iterator(); theIt.hasNext();) {
            JournalEntry theEntry      = theIt.next();
			TLID theIdentifier = theEntry.getIdentifier();
			// FIXME: The identifiers set is always empty!
            if (theIdentifiers.contains(theIdentifier)) {
                isConsistent = false;
                break;
            }
        }
        if (!isConsistent) {
            StringBuffer theContent = new StringBuffer();
            for (Iterator<JournalEntry> theIt = aLine.iterator(); theIt.hasNext();) {
                JournalEntry theEntry = theIt.next();
                theContent.append("identifier: ");
                theContent.append(theEntry.getIdentifier());
                theContent.append("attributes: ");
                for (Iterator theAttIt = theEntry.getAttributes().iterator(); theAttIt.hasNext();) {
                    JournalAttributeEntry theAttEntry = (JournalAttributeEntry) theAttIt.next();
                    theContent.append(theAttEntry.getName());
                    theContent.append(';');
                }
                theContent.append(' ');
            }
            Logger.error("Inconsistent journal entry. User: "+aLine.getUserId()+" Content: "+theContent, this);
        }
        return isConsistent;
    }

    protected final void journalEntry(long aCommitId, JournalEntry anEntry, PreparedStatement createJournalStm) throws SQLException {
        // store  line info
		TLID theIdentifier = anEntry.getIdentifier();
        createJournalStm.setLong  (1, aCommitId);
		IdentifierUtil.setId(createJournalStm, 2, theIdentifier);
        createJournalStm.setString(3, anEntry.getType());
        createJournalStm.addBatch();
    }

    protected void journalEntryAttributes(PooledConnection connection, long aCommitId, JournalEntry anEntry) throws SQLException {
        // store  line info
		TLID theIdentifier = anEntry.getIdentifier();
        List   theAttributes = anEntry.getAttributes();
        if (theAttributes != null) {
            journalAttributes(connection, aCommitId, theIdentifier, theAttributes);
        }
    }

    /** 
     * Journal a Collection of {@link JournalPersistancyHandler}s.
     */
    private void journalAttributes(PooledConnection connection,
			long aCommitId, TLID theIdentifier, List theAttributes) throws SQLException {

		Map<Class<? extends JournalAttributeEntry>, Collection<? extends JournalAttributeEntry>> blockByClass =
			new HashMap<>();
		for (Object object : theAttributes) {
			Class clazz = object.getClass();
			Collection entriesOfClass = blockByClass.get(clazz);
			if (entriesOfClass == null) {
				entriesOfClass = new HashSet();
				blockByClass.put(clazz, entriesOfClass);
			}
			entriesOfClass.add(object);
		}

		for (Entry<Class<? extends JournalAttributeEntry>, Collection<? extends JournalAttributeEntry>> entry : blockByClass
			.entrySet()) {
			Class<? extends JournalAttributeEntry> attributeType = entry.getKey();

			JournalPersistancyHandler theHandler = lookupPersistencyHandler(attributeType);
			if (theHandler == null) {
				Logger.warn("No PersistancyHandler for " + attributeType, this);
				continue;
			}

			Collection<? extends JournalAttributeEntry> attributes = entry.getValue();
			theHandler.journal(aCommitId, theIdentifier, attributes, connection);
		}

		// for (int i = 0, size = theAttributes.size(); i < size; i++) {
		// JournalAttributeEntry theAttribute = (JournalAttributeEntry) theAttributes.get(i);
		// JournalPersistancyHandler theHandler = lookupPersistencyHandler(theAttribute.getClass());
		//
		// theHandler.journal(aCommitId, theIdentifier, theAttribute, connection);
		// }
    }
    
	private JournalPersistancyHandler lookupPersistencyHandler(Class<? extends JournalAttributeEntry> entryClass) {
		JournalPersistancyHandler handler = this.persistancyHandler.get(entryClass);
		if (handler == null) {
			Iterator<Entry<Class<?>, JournalPersistancyHandler>> theHandlerTypes =
				this.persistancyHandler.entrySet().iterator();

			while (theHandlerTypes.hasNext()) {
				Entry<Class<?>, JournalPersistancyHandler> theEntry = theHandlerTypes.next();
				Class<?> theType = theEntry.getKey();
				if (theType.isAssignableFrom(entryClass)) {
					handler = theEntry.getValue();
					persistancyHandler.put(entryClass, handler);
					break;
				}
			}
		}
		return handler;
	};

    protected class JournalManagerThread extends Thread {

        /** a queue holding journallines to be stored */
        private LinkedList<JournalLine> lines = new LinkedList<>();
        
        private boolean inProgress;

        /**
         * Create a JournalManagerThread with given name.
         */
        public JournalManagerThread(String name) {
            super(name);
            this.setDaemon(true);
        }

		/**
		 * Pop entries from the work list and perform them.
		 */
		@Override
		public void run() {
			try {
				ThreadContext.inSystemContext(JournalManager.class, new Computation<Void>() {
					@Override
					public Void run() {
						// run until someone requests this thread to terminate
						while (true) {
							// journal all lines added to the manager
							while (true) {
								final JournalLine line;
								synchronized (mutex) {
									if (! hasJobs()) {
										// currently no lines to work
										break;
									}
									line = lines.removeFirst();
									inProgress = true;
								}
								try {
									journalLine(line);
								} catch (SQLException e) {
									Logger.error("Line not journaled.", e, JournalManagerThread.class);
								} finally {
									synchronized (mutex) {
										inProgress = false;
										mutex.notifyAll();
									}
								}
								Thread.yield(); // Be nice
							}

							// check whether this thread was requested to
							// terminate
							synchronized (mutex) {
								if (mutex.get() == ThreadState.TERMINATED) {
									break;
								}
							}

							// Wait until something to do
							synchronized (mutex) {
								try {
									while (!workToComplete() && mutex.get() != ThreadState.TERMINATED) {
										mutex.wait();
									}
								} catch (InterruptedException ex) {
									// well, seems there is something to do now
								}

							}
						}
						return null;
					}
				});
			} catch (Throwable ex) {
				Logger.error(this + " died unexpectedly ", ex, JournalManagerThread.class);
			} finally {
				synchronized (mutex) {
					mutex.set(ThreadState.DEAD);
					mutex.notifyAll();
				}
			}
		}
        
		void addLine(JournalLine journal) {
			assert journal != null : "Must not add null journals";
			synchronized (mutex) {
				if (mutex.get() != ThreadState.ALIVE) {
					throw new IllegalStateException("The thread processing the given line is marked as terminated or already dead");
				}
				lines.add(journal);
				mutex.notifyAll();
			}
		}

		/**
		 * Declares whether the thread has work to complete or not.
		 */
		boolean workToComplete() {
			return inProgress || hasJobs();
		}

		private boolean hasJobs() {
			return !lines.isEmpty();
		}
    }
    
	/**
	 * Cause the {@link JournalManagerThread} to terminate.
	 */
	public void terminate() {
		synchronized (mutex) {
			if (mutex.get() != ThreadState.ALIVE) {
				// terminate already done or requested
				return;
			}
			mutex.set(ThreadState.TERMINATED);
			mutex.notifyAll();
		}
	}
    
	/**
	 * Terminates the {@link JournalManagerThread thread} and causes the current
	 * thread to wait until the thread has terminated.
	 */
	public void waitUntilTerminated() {
		synchronized (mutex) {
			terminate();
			try {
				while (mutex.get() != ThreadState.DEAD) {
					mutex.wait();
				}
			} catch (InterruptedException ex) {
				Logger.warn("Unexpect", ex, this);
			}
		}
	}

//
//    public static JournalLine createJournalLine(long aCommitId, String aUserId, Collection someJournalables) {
//        JournalLine theLine = new JournalLine(aUserId, someJournalables.size());
//        Iterator theIt = someJournalables.iterator();
//        while (theIt.hasNext()) {
//            Journallable theJournallable = (Journallable) theIt.next();
//            theLine.add(theJournallable.getJournalEntry());
//        }
//        return theLine;
//    }
    
    public static JournalManager getInstance() {
    	return Module.INSTANCE.getImplementationInstance();
    }

    /** indicates that there is anything to journal at all (types is not empty) */
    public static boolean isActive() {
        return Module.INSTANCE.isActive() && getInstance().isActiveFlag;   
    }
    
    /**
     * Return a List of handlers that can read Collections of TODO TSA
     */
    private List<JournalPersistancyHandler> getReadPersistancyHandlers () {
        return this.persistancyReaders;
    }

    /** 
     * Allow access to the DBHelper for testing.
     */
    public DBHelper getDBHelper() {
        return dbHelper;
    }

	/**
	 * Create a {@link JournalLine} from within the commit context that is
	 * forwarded to {@link #journal(JournalLine)} after the commit succeeded.
	 * 
	 * @param allObjects
	 *        All committed objects (wrappers).
	 * @param modifiedObjectsByKey
	 *        All modified {@link KnowledgeItem}s indexed by
	 *        {@link KnowledgeItem#tId()}.
	 * @param newObjectsByKey
	 *        All newly allocated {@link KnowledgeItem}s indexed by
	 *        {@link KnowledgeItem#tId()}.
	 * @param removedObjectsByKey
	 *        All removed {@link KnowledgeItem}s indexed by
	 *        {@link KnowledgeItem#tId()}.
	 * @return The created {@link JournalLine}.
	 */
	public JournalLine createJournalLine(Set<Journallable> allObjects, Map modifiedObjectsByKey, Map newObjectsByKey, Map removedObjectsByKey) {
		try {
			String theUserId = null;
			if (allObjects == null || allObjects.isEmpty()) {
				return null;
			}
			TLContext theContext = TLContext.getContext();
			if (theContext != null) {
				theUserId = theContext.getCurrentUserName();
			}
			JournalLine theLine = new JournalLine(theUserId, allObjects.size());
			for (Journallable theJournallable : allObjects) {
				// check if the journalmanager is supposed to journal this object
				if (isToJournal(theJournallable.getJournalType())) {
					JournalEntry theEntry =
						theJournallable.getJournalEntry(modifiedObjectsByKey, newObjectsByKey, removedObjectsByKey);
					if (theEntry != null) {
						theLine.add(theEntry);
					}
				}
			}
			//TODO TSA what if there are no committables? journal empty line?
			return theLine;
		} catch (Exception ex) {
		    // commit must continue nontheless (?).
		    Logger.error("Unable to create journal line." ,ex, JournalManager.class);
		    return null;
		}
	}

	/**
	 * Module for {@link JournalManager}.
	 * 
	 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
	 */
	public static final class Module extends TypedRuntimeModule<JournalManager> {
		/**
		 * Module instance.
		 */
		public static final Module INSTANCE = new Module();

		@Override
		public Class<JournalManager> getImplementation() {
			return JournalManager.class;
		}
	}
	
	@Override
	protected void shutDown() {
		waitUntilTerminated();
		super.shutDown();
	}

}


