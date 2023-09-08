/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.event.logEntry;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.top_logic.base.bus.MonitorEvent;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.event.ModelTrackingService;
import com.top_logic.event.bus.AbstractReceiver;
import com.top_logic.event.bus.BusEvent;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.tool.boundsec.BoundChecker;
import com.top_logic.tool.boundsec.BoundHelper;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.util.TLContext;

/**
 * This class is responsible for receiving events and persisting
 * them as {@link LogEntry}s if necessary.
 * 
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
@ServiceDependencies({LogEntryConfiguration.Module.class, ModelTrackingService.Module.class})
public class LogEntryReceiver extends AbstractReceiver {

    private long  numberOfEventsProcessed=0;
    
//    private LogEntryManager logManager  = LogEntryManager.getManager();

	/**
	 * Configuration for {@link LogEntryReceiver}.
	 * 
	 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
	 */
	public interface Config extends AbstractReceiver.Config {
		String MODEL = "model";

		String TRACK = "track";

		/**
		 * Namespace for the service.
		 */
		@Override
		@StringDefault(TRACK)
		String getServiceNamespace();

		/**
		 * Name of the service.
		 */
		@Override
		@StringDefault(MODEL)
		String getServiceName();
	}
	/**
	 * @param context
	 *        {@link InstantiationContext} context to instantiate sub configurations.
	 * @param config
	 *        Configuration for {@link LogEntryReceiver}.
	 */
	public LogEntryReceiver(InstantiationContext context, Config config) {
		super(context, config);
	}

    /**
     * @see com.top_logic.event.bus.IReceiver#receive(com.top_logic.event.bus.BusEvent)
     */
    @Override
	public void receive(BusEvent aBusEvent) throws RemoteException {
        if (aBusEvent instanceof MonitorEvent) {
            processEvent((MonitorEvent) aBusEvent);
        }
    }

    /**
     * Processes the given CoreEvent
     * @param anEvent the CoreEvent to process
     */
    private void processEvent(MonitorEvent anEvent) {
        //      getConsoleInformation(anEvent);
        if (check(anEvent)) {
            makePersistent(anEvent);
            numberOfEventsProcessed++;
        }
    }

    /**
     * Makes the given CoreEvent persistent
     * 
     * @param anEvent the event to become persistent
     */
    protected void makePersistent(MonitorEvent anEvent) {
        try{
            TLContext.pushSuperUser(); // No need to check KA Security in this case
            LogEntry theEntry = LogEntry.createLogEntry(anEvent);
            writeUserLists(anEvent, theEntry);
        }
        catch(Exception e){
            Logger.warn("Problem creating a log entry for event "+anEvent+".",e,this);
        } finally {
            TLContext.popSuperUser();
        }
    }



    /**
     * Updates the list of LogEntries for all users which are allowed to see anEntry.
     * 
     * If in the list of a user already is an entry which is duplicate to anEntry, 
     * the existing entry is replaced by anEntry
     */
    private void writeUserLists(MonitorEvent anEvent, LogEntry anEntry) {
        List                persons = getPersonsForEntry(anEvent, anEntry);
        Iterator            iter    = persons.iterator();
        UserDayEntryManager udm     = UserDayEntryManager.getInstance();
        while(iter.hasNext()){
            Person thePerson = (Person)iter.next();
            udm.addLogEntry(thePerson, anEntry);
        }
    }
    
    private boolean check(MonitorEvent anEvent) {
        Object theMessage = anEvent.getMessage();
        if (theMessage instanceof Wrapper) {
            return LogEntryConfiguration.getInstance().check((Wrapper) theMessage, anEvent.getType());
        }
        return false;
    }
    
    /**
     * Check for AllAlivePersons agains anEvent.getSecurityChecker().
     * 
     * @return a list with the persons which are allowed to see the given LogEntry
     */
    protected List getPersonsForEntry(MonitorEvent anEvent, LogEntry anEntry) {        
        PersonManager personManager = PersonManager.getManager();
        BoundHelper   boundHelper   = BoundHelper.getInstance();
        try{
          
            Object      theSource = anEvent.getMessage();
            List        res       = personManager.getAllAlivePersons();
            
            if (res.isEmpty()) {
                Logger.warn("No alive Persons, check PersonManager",this);
                return res;
            }
            if (!(theSource instanceof BoundObject)) {
                Logger.warn("Source is no BoundObject, allowing allPersons",this);
                return res;
            } 
            
            BoundObject theModel = (BoundObject) theSource;
			Collection<BoundChecker> securityCheckers = boundHelper.getDefaultCheckers(theModel, null);
			if (securityCheckers.isEmpty()) {
                Logger.warn("No bound checker found for "+theModel+", allowing root access only.",this);
                return Collections.singletonList(personManager.getRoot()); 
            }

            for (int i = 0; i < res.size(); i++) {
                Person thePers = (Person) res.get(i);
				boolean anyCheckerAllows = false;
				for (BoundChecker checker : securityCheckers) {
					if (checker.allow(thePers, theModel)) {
						anyCheckerAllows = true;
						break;
					}
				}
				if (!anyCheckerAllows) {
                    res.remove(i--);
                }
            }
            if (res.isEmpty()) {
                Logger.warn("Nobody is allowed to see '" + theModel 
                        + "'. Allowing root access only." ,this);
                res.add(personManager.getRoot());
            }
            return res;
        }
        catch(Exception e){
            Logger.error("Failed to getPersonsForEntry()",e,this);
        }
        return Collections.EMPTY_LIST;
    }

    /**
     * the number of events processed by this receiver
     */
    public long getNumbersOfEventsProcessed(){
        return numberOfEventsProcessed;
    }
    
	@Override
	protected void startUp() {
		super.startUp();
		subscribe();
	}

	@Override
	protected void shutDown() {
		unsubscribe();
		super.shutDown();
	}

	/**
	 * {@link BasicRuntimeModule} for {@link LogEntryReceiver}.
	 * 
	 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
	 */
    public static final class Module extends AbstractReceiverModule<LogEntryReceiver> {

		/**
		 * Singleton {@link Module} instance.
		 */
		public static final Module INSTANCE = new Module();

		@Override
		public Class<LogEntryReceiver> getImplementation() {
			return LogEntryReceiver.class;
		}

	}
    
}
