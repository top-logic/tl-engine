
/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.monitor.bus;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.top_logic.base.bus.MonitorEvent;
import com.top_logic.base.bus.UserEvent;
import com.top_logic.base.user.UserInterface;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.util.Utils;
import com.top_logic.event.bus.Bus;
import com.top_logic.event.bus.BusEvent;
import com.top_logic.event.bus.IReceiver;
import com.top_logic.event.bus.Sender;

/**
 * Listener of {@link com.top_logic.base.bus.UserEvent}s.
 * Supported are backup on file with buffering for fast access.
 * 
 * This class is not in use anymore since it writes to a file.
 * Its the Sucessuor the {@link com.top_logic.knowledge.monitor.UserMonitor}
 * uses the Knowledgebase as storage. The class is left here as 
 * an exmaple how things could be done, too.
 * 
 *                                 and implements IReceiver
 *                                 and to use EventBuffer, EventWriter, EventReader
 */
public class UserMonitor extends DefaultMonitor implements IReceiver {
    
    /**
     * default number of events to be stored/cached by this buffer.
     */
    private static final int DEFAULT_EVENT_CACHE_SIZE  = 10;      

    /** The list of login infos. */
    private List loginInfo;

    private int eventBufferSize;   
        
    /** The event writer to write out the events. */
    private EventWriter eventWriter;       
    
    /** The event buffer to store the events. */
    private EventBuffer eventBuffer;     

	/**
	 * Configuration for {@link UserMonitor}.
	 * 
	 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
	 */
    public interface Config extends DefaultMonitor.Config {
		/**
		 * Namespace for the service.
		 */
		@Override
		@StringDefault(Bus.CHANGES)
		String getServiceNamespace();

		/**
		 * Name of the service.
		 */
		@Override
		@StringDefault(Bus.USER)
		String getServiceName();
    }
	/**
	 * @param context
	 *        {@link InstantiationContext} context to instantiate sub configurations.
	 * @param config
	 *        Configuration for {@link UserMonitor}.
	 */
	public UserMonitor(InstantiationContext context, Config config) {
		super(context, config);

		// initialize
		this.init();

		// reader
		List theEvents = null;
		try {
			theEvents = new EventReader(super.createReader()).readAllEvents(this);
		} catch (Exception e) {
			Logger.error("Unable to read UserEvents", e, this);
		}
		for (int i = 0; (theEvents != null) && (i < theEvents.size()); i++) {
			this.getBuffer().addEvent((BusEvent) theEvents.get(i));
		}

		// login info
		this.loginInfo = new ArrayList(this.getBuffer().getSize());

		// Set logout on all users that where still logged in
		// when the machine was shut down last time
		Iterator theLoginInfos = this.getLoginInfoList().iterator();
		Date theCurrentDate = new Date();
		while (theLoginInfos.hasNext()) {
			LoginInfo theInfo = (LoginInfo) theLoginInfos.next();
			if (theInfo.getLogout() == null) {
				theInfo.setLogout(theCurrentDate);
			}
		}
	}
    
    /**
     * Initialize buffer, writer and reader.
     */
    private void init () {
        //buffer size              
        try {
			this.eventBufferSize = getConfig().getNumberEventsToStore();
        }
        catch(NumberFormatException e) {
            //leave default                
            Logger.debug ("Unable set new size of event buffer, reason is: ", e, this);
        }

        if (this.eventBufferSize == 0) {     
            this.eventBufferSize = DEFAULT_EVENT_CACHE_SIZE;   
        }                  
    } 
    
    private EventBuffer getBuffer () {
        if(this.eventBuffer == null) {
            this.eventBuffer = new EventBuffer(this.eventBufferSize);            
        }
        return this.eventBuffer;
    }    
    
    private EventWriter getWriter () {
        if(this.eventWriter == null) {
            try {
                this.eventWriter = new EventWriter(this.createWriter());      
            } catch (IOException e) {
                Logger.error ("Unable to initialize Writer of UserEvents, reason is: ", e, this);            
            }             
        }
        return this.eventWriter;
    }
    

//BUFFER    

    /**
     * Returns buffer size.
     */ 
    public int getMaxBufSize () {
        return this.getBuffer().getSize();
    }

    /**
     * Returns all cached user events.
     * 
     * TODO KHA should not be used in UserMonitor Testcases
     * @see com.top_logic.base.monitor.bus.EventBuffer#getAllEvents()
     */ 
    public List getAllUserEvents () {
        return this.getBuffer().getAllEvents ();
    }   
    
//RECEIVER      

    /**
     * Is invoked when a new message has been published on the bus. 
     *
     * Because this instance only stores UserEvents, all other events
     * will be ignored.
     * 
     * @param    aBusEvent   The event object providing information 
     *                       about the event and its source.
     */
    @Override
    protected void process(MonitorEvent aBusEvent) {
        if (aBusEvent instanceof UserEvent) {
            final UserEvent theUserEvent = (UserEvent) aBusEvent;
            //add event to cache
            this.getBuffer().addEvent(theUserEvent);    
    
            //do action on receive
			if (Utils.equals(theUserEvent.getType(), UserEvent.LOGGED_IN)) {
                this.login (theUserEvent);
            }
			else if (Utils.equals(theUserEvent.getType(), UserEvent.LOGGED_OUT)) {
                this.logout (theUserEvent);
            }            

            //backup all currently cached events           
            try {
                 this.getWriter().writeAllEvents (this.getBuffer().getAllEvents());
            } catch (IOException e) {
              Logger.error ("Unable to write event buffer, reason is: ", e, this);                           
            }         
        }
    }
        
    /**
     * Returns the last login time of a user.
     * @see com.top_logic.base.monitor.bus.UserMonitor#getLastLogin(UserInterface, int)
     */    
    public UserEvent getLastLogin (UserInterface aUser) {
        return this.getLastLogin (aUser, 0);
    }    

    /**
     * Returns the last login time of a user.
     *
     * This method can be used to find out, when a user has login to the
     * system. The second parameter is used to say, which last login you
     * mean. Often it's not useful to find the last login but the login 
     * before. If you want to find the changes since the last time the
     * user worked with the system.
     *
     * @param    aUser       The user to be checked.
     * @param    anAmount    The login times to be ignored (0 for last one,
     *                       1 for the one before, etc).
     * @return   The matching event or null, if none found.
     */
    public UserEvent getLastLogin (UserInterface aUser, int anAmount) {
        return this.getLastLogin (aUser.getUserName (), anAmount);
    }
    
    /**
     * Returns the last login time of a user.
     * @see com.top_logic.base.monitor.bus.UserMonitor#getLastLogin(String, int)
     */       
    public UserEvent getLastLogin (String aName) {
        return this.getLastLogin (aName, 0);
    }    

    /**
     * Returns the last login time of a user.
     *
     * This method can be used to find out, when a user has login to the
     * system. The second parameter is used to say, which last login you
     * mean. Often it's not useful to find the last login but the login 
     * before. If you want to find the changes since the last time the
     * user worked with the system.
     *
     * @param    aName       The name of the user to be checked.
     * @param    anAmount    The login times to be ignored (0 for last one,
     *                       1 for the one before, etc).
     * @return   The matching event or null, if none found.
     */
    public UserEvent getLastLogin (String aName, int anAmount) {
        return (this.getEvent (aName, anAmount, UserEvent.LOGGED_IN));
    }
    
//LOGOUT    
    
    /**
     * Returns the last login time of a user.
     * @see com.top_logic.base.monitor.bus.UserMonitor#getLastLogout(UserInterface, int)
     */     
    public UserEvent getLastLogout (UserInterface aUser) {
        return this.getLastLogout (aUser, 0);
    }    

    /**
     * Returns the last logout time of a user.
     *
     * This method can be used to find out, when a user has logged out 
     * from the system. The second parameter is used to say, which last 
     * logout you mean. Often it's not useful to find the last logout 
     * but the logout before. If you want to find the changes since the 
     * last time the user finished his worke with the system.
     *
     * @param    aUser       The user to be checked.
     * @param    anAmount    The logout times to be ignored (0 for last one,
     *                       1 for the one before, etc).
     * @return   The matching event or null, if none found.
     */
    public UserEvent getLastLogout (UserInterface aUser, int anAmount) {
        return this.getLastLogout (aUser.getUserName (), anAmount);
    }
    
    /**
     * Returns the last login time of a user.
     * @see com.top_logic.base.monitor.bus.UserMonitor#getLastLogout(String, int)
     */       
    public UserEvent getLastLogout (String aName) {
        return this.getLastLogout (aName, 0);
    }    

    /**
     * Returns the last logout time of a user.
     *
     * This method can be used to find out, when a user has logged out 
     * from the system. The second parameter is used to say, which last 
     * logout you mean. Often it's not useful to find the last logout 
     * but the logout before. If you want to find the changes since the 
     * last time the user finished his worke with the system.
     *
     * @param    aName       The name of the user to be checked.
     * @param    anAmount    The logout times to be ignored (0 for last one,
     *                       1 for the one before, etc).
     * @return   The matching event or null, if none found.
     */
    public UserEvent getLastLogout (String aName, int anAmount) {
        return (this.getEvent (aName, anAmount, UserEvent.LOGGED_OUT));
    }
    
//LOGININFO    

    /**
     * Returns the list of LoginInfo stored within this instance.
     *
     * This list contains the information about login and logout times
     * of the last users using the system.
     *
     * @return    The requested list (of type LoginInfo).
     * @see       com.top_logic.base.monitor.bus.LoginInfo
     */
    public List getLoginInfoList () {
        return (this.loginInfo);
    }

    /**
     * Processes the login of a user.
     *
     * This method will notify the login of a user. Therefore a new 
     * logging instance will be created, which stores the login and
     * logout time of the user.
     *
     * @param    anEvent    The event describing the login event.
     */
    protected void login (UserEvent anEvent) {
        this.addLoginInfo (anEvent.getPassiveUser (), anEvent.getDate ());
    }

    /**
     * Processes the logout of a user.
     *
     * This method will notify the logout of a user. Therefore the
     * last (open) login of the given user will be searched and the
     * logout time will be stored for this user. If there is no login
     * event of the user, a new one will be created.
     *
     * @param    anEvent    The event describing the logout event.
     */
    protected void logout (UserEvent anEvent) {
        UserInterface theUser = anEvent.getPassiveUser ();
        LoginInfo     theInfo = this.getLatestLoginInfo (theUser, null);

        if (theInfo != null) {
            theInfo.setLogout (anEvent.getDate ());
        }
    }

    /**
     * Look up the latest LoginInfo for the given user.
     *
     * If there is no such info object in the list, a new one 
     * will be created using the given date as login time.
     *
     * @param    aUser    The affected user.
     * @param    aDate    The login date of the user (can be null).
     * @return   The searched last login info or null.
     */
    protected LoginInfo getLatestLoginInfo (UserInterface aUser, Date aDate) {
        LoginInfo theCurr;
        LoginInfo theInfo = null;
        Iterator  theIt   = this.loginInfo.iterator ();

        while (theInfo == null && theIt.hasNext ()) {
            theCurr = (LoginInfo) theIt.next ();

            if (aUser.equals (theCurr.getUser ()) && theCurr.isOpen ()) {
                theInfo = theCurr;
            }
        }

        if (theInfo == null) {
            try {
                theInfo = this.addLoginInfo (aUser, aDate);
            }
            catch (IllegalArgumentException ex) {
                Logger.warn ("Unable to create LoginInfo, reason is: ", ex,
                             this);
            }
        }

        return (theInfo);
    }

    /**
     * Create a new login info for the given parameters.
     *
     * Additionaly this new instance will be added to the list of login
     * infos.
     *
     * @param    aUser    The user (not null).
     * @param    aDate    The login time (can be null).
     * @return   The new instance of LoginInfo.
     * @throws   IllegalArgumentException    If the given user is null.
     */
    protected LoginInfo addLoginInfo (UserInterface aUser, Date aDate) 
                                            throws IllegalArgumentException {
        int       thePos  = this.loginInfo.size ();
        LoginInfo theInfo = new LoginInfo (aUser, aDate);

        if (thePos >= this.getBuffer().getSize()) {
            this.loginInfo.remove (thePos - 1);
        }

        this.loginInfo.add (0, theInfo);

        return (theInfo);
    }
    
//USEREVENT     

    /**
     * Return the user event specified by the given parameters.
     *
     * This method tries to find the stored UserEvent specified by
     * the name of the user, the type of the event (e.g. LOGGED_IN or
     * LOGGED_OUT) and the number of the event. It can happen, that
     * there are different events which match to aName and aType, so 
     * you can define the number of the requested event.
     *
     * @param    aName       The username of the passive user in the event.
     * @param    anAmount    The number of the event.
     * @param    aType       The kind of event (e.g. LOGGED_IN).
     * @return   The requested event or null.
     */
    protected UserEvent getEvent (String aName, int anAmount, String aType) {
        UserEvent currEvent;
        String    theName;
        Iterator  theIt    = this.getAllUserEvents().iterator ();
        UserEvent theEvent = null;

        while ((theEvent == null) && theIt.hasNext ()) {
            currEvent = (UserEvent) theIt.next ();

            if (aType.equals( currEvent.getType ())) {
                theName = currEvent.getPassiveUser ().getUserName ();

                if (aName.equals (theName)) {
                    if (anAmount == 0) {
                        theEvent = currEvent;
                    }
                    else {
                        anAmount--;
                    }
                }
            }
        }

        return (theEvent);
    }
    
    /**
     * Method to read MonitorEvent from BufferedReader.
     * 
     * @param aReader reader to read from
     * @return event UserEvent
     */   
    @Override
	public MonitorEvent readEvent(BufferedReader aReader) throws IOException {     
        // Not used in this case ...
        /* String theSourceID    = */ aReader.readLine ();
        /* String theSourceType  = */ aReader.readLine ();
        String theMessageUser   = aReader.readLine ();        
        long   theDate        = 0;        
        try {
            theDate        = Long.parseLong(aReader.readLine ());
        }
        catch (NumberFormatException ex) { /* ignored */ }
        String    theType     = aReader.readLine ();
        //user
        String theUser        = aReader.readLine ();
        if (theUser != null) {        
            UserInterface messageUser = MonitorEvent.getUser (theMessageUser);
            UserInterface theUserI;
            if (theMessageUser.equals(theUser))
                theUserI = messageUser;
            else
                theUserI = MonitorEvent.getUser (theUser);
            if (messageUser == null || theUserI    == null) {
                Logger.warn("Unable to recreate correct Event for Users" + theMessageUser + ' ' + theUser, this);
                return null;
            }
               
            return new UserEvent (new Sender(), messageUser, theUserI, new Date (theDate),theType); 
        }    
        return null;    
    }      
}
