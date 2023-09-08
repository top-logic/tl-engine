/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.event.logEntry;

import static com.top_logic.knowledge.search.ExpressionFactory.*;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.top_logic.base.locking.Lock;
import com.top_logic.base.locking.LockService;
import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.TLID;
import com.top_logic.basic.col.MutableInteger;
import com.top_logic.basic.col.TupleFactory.Pair;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.util.Computation;
import com.top_logic.basic.util.ResKey;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.ex.IncompatibleTypeException;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MOReference.ReferencePart;
import com.top_logic.knowledge.objects.InvalidLinkException;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.search.CompiledQuery;
import com.top_logic.knowledge.search.Expression;
import com.top_logic.knowledge.search.Order;
import com.top_logic.knowledge.search.RevisionQuery;
import com.top_logic.knowledge.search.SetExpression;
import com.top_logic.knowledge.service.BasicTypes;
import com.top_logic.knowledge.service.BulkIdLoad;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.db2.DBKnowledgeAssociation;
import com.top_logic.knowledge.service.db2.DBKnowledgeItem;
import com.top_logic.util.Resources;
import com.top_logic.util.monitor.ApplicationMonitor;
import com.top_logic.util.monitor.MonitorComponent;
import com.top_logic.util.monitor.MonitorMessage;
import com.top_logic.util.monitor.MonitorMessage.Status;
import com.top_logic.util.monitor.MonitorResult;
import com.top_logic.util.sched.CleanUpTask;
import com.top_logic.util.sched.task.impl.TaskImpl;
import com.top_logic.util.sched.task.result.TaskResult;
import com.top_logic.util.sched.task.result.TaskResult.ResultType;
import com.top_logic.util.sched.task.schedule.SchedulingAlgorithm;

/**
 * A Task that condenses{@link UserDayEntry UserDayEntries} to {@link ArchiveUserDayEntry ArchiveUserDayEntries}.
 * 
 * UserDayEntry are based on (a potentially large number)
 * of KAs which are replaced by the ArchiveUserDayEntry.
 * 
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public class DayEntryArchiverTask<C extends DayEntryArchiverTask.Config<?>> extends TaskImpl<C>
		implements MonitorComponent {

	/**
	 * Typed configuration interface of {@link DayEntryArchiverTask}.
	 * 
	 * @since 5.7.3
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config<I extends DayEntryArchiverTask<?>> extends TaskImpl.Config<I> {

		/**
		 * Number of {@link UserDayEntry} to process in each commit.
		 */
		@IntDefault(75)
		int getCommitInterval();
	}
 
	private static final Comparator<KnowledgeItem> OBJECT_NAME_COMPARATOR = new Comparator<>() {

		@Override
		public int compare(KnowledgeItem o1, KnowledgeItem o2) {
			ObjectKey o1Key = DBKnowledgeItem.KEY_MAPPING.map(o1);
			ObjectKey o2Key = DBKnowledgeItem.KEY_MAPPING.map(o2);
			return o1Key.getObjectName().compareTo(o2Key.getObjectName());
		}
	};

    private final int commitInterval;

	private final KnowledgeBase _kb = PersistencyLayer.getKnowledgeBase();

	/**
	 * Query searching all {@link LogEntry#ASS_TYPE} associations with destination type
	 * {@link LogEntry#CLASS_NAME}, ordered by id of {@link LogEntry}.
	 */
	private final CompiledQuery<KnowledgeAssociation> _logEntryAssociationQuery;
	{
		SetExpression allAssociations = allOf(LogEntry.ASS_TYPE);
		Expression destTypeIsLogEntry =
			eqBinary(
				reference(BasicTypes.ASSOCIATION_TYPE_NAME, DBKnowledgeAssociation.REFERENCE_DEST_NAME,
					ReferencePart.type),
			literal(LogEntry.CLASS_NAME));
		SetExpression filter = filter(allAssociations, destTypeIsLogEntry);
		Order order =
			order(reference(BasicTypes.ASSOCIATION_TYPE_NAME, DBKnowledgeAssociation.REFERENCE_DEST_NAME,
				ReferencePart.name));
		RevisionQuery<KnowledgeAssociation> query = queryUnresolved(filter, order, KnowledgeAssociation.class);
		_logEntryAssociationQuery = _kb.compileQuery(query);
	}
    
    /**
     * Constructor for the DayEntryArchiverTask which sets the properties.
     * 
     * @throws Exception when setup fails (Inherited form FileManager)
     */
	@Deprecated
    public DayEntryArchiverTask(Properties prop) throws Exception {
        super(prop);               
        String theCount = prop.getProperty("commitInterval", "75");
        this.commitInterval = Integer.valueOf(theCount).intValue();
    }
    
	/**
	 * Creates a {@link DayEntryArchiverTask} from the given configuration.
	 */
	public DayEntryArchiverTask(InstantiationContext context, C config) {
		super(context, config);
		this.commitInterval = config.getCommitInterval();
	}

    /**
     * Constructor for testing only.
     */
	public DayEntryArchiverTask(String name) {
		super(name);
        this.commitInterval = 75;
    }
    
	@Override
	protected void onAttachToScheduler() {
		super.onAttachToScheduler();

		ApplicationMonitor.getInstance().registerMonitor(getName(), this);
	}

	@Override
	protected void onDetachFromScheduler() {
		ApplicationMonitor.getInstance().unregisterMonitor(getName());

		super.onDetachFromScheduler();
	}

	/**
	 * This archives the DayUserEntries.
	 */
    @Override
	public void run () { 
    	super.run();
		getLog().taskStarted();
        try {
            int    theCount = this.performArchive();

			ResKey encodedMessage;
            if (theCount > 0) {
				encodedMessage = I18NConstants.ARCHIVED_SUCCESSFULLY__COUNT.fill(theCount);

				Logger.info(Resources.getSystemInstance()
					.decodeMessageFromKeyWithEncodedArguments(encodedMessage), this);
			} else {
				encodedMessage = I18NConstants.NOTHING_TO_ARCHIVE;
            }

			getLog().taskEnded(ResultType.SUCCESS, encodedMessage);
        } 
        catch (Exception ex) {
			getLog().taskEnded(ResultType.ERROR, ResultType.ERROR.getMessageI18N(), ex);
            Logger.error ("Error in archiving DayUserEntries",ex, DayEntryArchiverTask.class);                
        }           
    }
    
    protected int performArchive() {
        // try archiving for the last three days
        // so we should have no problems if one night no archiving is performed
             
        return ThreadContext.inSystemContext(DayEntryArchiverTask.class, new Computation<Integer>() {
			@Override
			public Integer run() {
				int res = 0;
				Lock tokenContext = null;
		        try{
		            // get token, we could be in a cluster           
		            // Get new context
					tokenContext =
						Lock.createLock(LockService.getInstance().getLockTimeout(), getTokenContextName());
					if (tokenContext.tryLock()) {
		            // we get the token context, so we can execute the archive task
		                
		                res = archive();
		                tokenContext.unlock();
		            }
		            else{
		                Logger.info("Token context could not be locked for archiving LogEntries.",this);
		            }
		            tokenContext = null;
		        }
		        finally{
		            // release token
		            if(tokenContext!=null){
		                try{
		                    tokenContext.unlock();
		                }
		                catch(Exception e){
		                    // ignore
		                }
		            }
		        }
		        return Integer.valueOf(res);
			}
        }).intValue();
    }
    
    /**
     * Caller is responsible to check needed token contexts and for creation
     * and cleanup of TLContext
     * 
     * This method must be used to call this task embedded in another thread.
     * when using run, this must be done in an own thread
     *
     */
    public void runTaskExternally(){
    	archive();
    }

    /** 
     * Perform the arhiving for all persons and all dates.
     * 
     * @return number or Events archived
     */
    protected int archive() {
		MutableInteger counter = new MutableInteger();
        try {
			Map<Pair<KnowledgeObject, java.sql.Date>, StringBuilder> personDateIdList =
				getPersonDateIdList(counter);
            
            if (! personDateIdList.isEmpty()) {
				this.commit(_kb, "Failed to commit deleted '" + LogEntry.ASS_TYPE + "' associations. Deleted "
					+ counter.intValue()
					+ " associations before.");
				this.writeBackArchiveUserDayEntries(_kb, personDateIdList);
				this.commit(_kb, "Finally failed to store ArchiveUserDayEntries");
            }

        }
        catch (Exception ex) {
            Logger.error("Failed to archive log entries", ex, DayEntryArchiverTask.class);
        }
		return counter.intValue();
    }

	private Map<Pair<KnowledgeObject, java.sql.Date>, StringBuilder> getPersonDateIdList(MutableInteger counter)
			throws NoSuchAttributeException, InvalidLinkException, DataObjectException {
		Map<Pair<KnowledgeObject, java.sql.Date>, StringBuilder> personDateIdList =
			new HashMap<>();
		List<KnowledgeAssociation> logEntryAssociations = _logEntryAssociationQuery.search();
		if (logEntryAssociations.isEmpty()) {
			return personDateIdList;
		}

		// Load LogEntry's
		Set<ObjectKey> destinationIDs = new HashSet<>();
		for (KnowledgeAssociation association : logEntryAssociations) {
			destinationIDs.add(association.getDestinationIdentity());
		}
		// Sort LogEntry's to have same order as associations
		KnowledgeItem[] entries = BulkIdLoad.load(_kb, destinationIDs).toArray(new KnowledgeItem[0]);
		Arrays.sort(entries, OBJECT_NAME_COMPARATOR);

		int index = 0;
		for (KnowledgeItem log : entries) {
			java.sql.Date theDate = LogEntry.alignToMiddleOfDay((Date) log.getAttributeValue(LogEntry.DATE));

			while (index < logEntryAssociations.size()) {
				KnowledgeAssociation association = logEntryAssociations.get(index);
				if (!association.getDestinationIdentity().equals(log.tId())) {
					// Reached next association.
					break;
				}
				index++;
				aggregateLogEntry(personDateIdList, log.getObjectName(), theDate, association);

				counter.inc();
				if ((counter.intValue() % commitInterval) == 0) {
					StringBuilder msg = new StringBuilder();
					msg.append("Failed to commit deleted '");
					msg.append(LogEntry.ASS_TYPE);
					msg.append("' associations. Deleted ");
					msg.append(counter.intValue());
					msg.append(" associations before.");
					this.commit(_kb, msg.toString());
				}
			}
		}
		assert index == logEntryAssociations.size() : "Search results and logentries have different order.";
		return personDateIdList;
	}

    /**
     * Commit the {@link KnowledgeBase} and log anError if commit failed for some reason.
     */
    private boolean commit(KnowledgeBase aKB, String anError) {
        if (! aKB.commit()) {
			getLog().taskEnded(
				ResultType.ERROR, ResultType.ERROR.getMessageI18N(), new RuntimeException("Commit failed!"));
            Logger.error(anError, DayEntryArchiverTask.class);
            return false;
        }
        return true;
    }

    /** 
     * Aggregate the hasLogEntry KA into the personDateIdList.
     */
	protected void aggregateLogEntry(Map<Pair<KnowledgeObject, java.sql.Date>, StringBuilder> personDateIdList,
            TLID logEntryid, java.sql.Date theDate, KnowledgeAssociation hasLE)
                throws InvalidLinkException, DataObjectException 
    {
		KnowledgeObject personItem = hasLE.getSourceObject();

		Pair<KnowledgeObject, java.sql.Date> key = new Pair<>(personItem, theDate);
        
        StringBuilder buf = personDateIdList.get(key);
        if (buf == null) {
			personDateIdList.put(key, new StringBuilder(256).append(IdentifierUtil.toExternalForm(logEntryid)));
        } else {
			buf.append(ArchiveUserDayEntry.SEPARATOR).append(IdentifierUtil.toExternalForm(logEntryid));
        }
        hasLE.delete();
    }

    /** 
     * Feed content of personDateIdList back to ArchiveUserDayEntries.
     * 
     * @param personDateIdList  Map Person,Date to List of IDs (StringBuilder)
     */
	private void writeBackArchiveUserDayEntries(KnowledgeBase base,
			Map<Pair<KnowledgeObject, java.sql.Date>, StringBuilder> personDateIdList)
            throws DataObjectException, IncompatibleTypeException, NoSuchAttributeException {
        int count = 0;
		for (Map.Entry<Pair<KnowledgeObject, java.sql.Date>, StringBuilder> entry : personDateIdList.entrySet()) {
			Pair<KnowledgeObject, java.sql.Date> key = entry.getKey();
            StringBuilder buf =  entry.getValue();
            
			KnowledgeObject person = key.getFirst();
			java.sql.Date theDate = key.getSecond();
            
			KnowledgeObject aude = ArchiveUserDayEntry.getExistingEntryKO(base, person, theDate);
            if (aude == null) {
				aude = ArchiveUserDayEntry.createEntryKO(base, person, theDate);
                aude.setAttributeValue(ArchiveUserDayEntry.ENTRY_IDS, buf.toString());
            }
            else {
                String idList = (String) aude
                        .getAttributeValue(ArchiveUserDayEntry.ENTRY_IDS)
                        + ArchiveUserDayEntry.SEPARATOR
                        + buf.toString();
               aude.setAttributeValue(ArchiveUserDayEntry.ENTRY_IDS, idList);
            }
            if ((++count % commitInterval) == 0) {
				this.commit(base, "Failed to store ArchiveUserDayEntry " + aude);
            }
        }
    }

    String getTokenContextName() {
        return "DayEntryArchiverTask";
    }

	@Override
	public void checkState(MonitorResult result) {
		result.addMessage(newMonitorMessage());
	}

	private MonitorMessage newMonitorMessage() {
		Status type = this.getShouldStop() ? MonitorMessage.Status.ERROR : MonitorMessage.Status.INFO;
        StringBuilder message = new StringBuilder(256);  
        
        message.append(getName());
		if (lastSched != SchedulingAlgorithm.NO_SCHEDULE) {
            message.append(" last: ");
            message.append(CleanUpTask.getLogFormat().format(new Date(lastSched)));
        }
		if (nextShed != SchedulingAlgorithm.NO_SCHEDULE) {
            message.append(" next: ");
            message.append(CleanUpTask.getLogFormat().format(new Date(nextShed)));
        }
		TaskResult result = getLog().getCurrentResult();
		if (result != null) {
			Resources resources = Resources.getInstance();
			message.append(' ');
			ResKey resultTypeI18N = result.getResultType().getMessageI18N();
			message.append(resources.getString(resultTypeI18N));
			message.append(": ");
			message.append(resources.getString(result.getMessage()));
        }
        
        return new MonitorMessage(type , message.toString(), this);
	}
    
    @Override
	public String getDescription() {
        return "Archives LogEntries, ActualUserDayEntries are changed to ArchiveDayUserEntries.";
    }
    
}
