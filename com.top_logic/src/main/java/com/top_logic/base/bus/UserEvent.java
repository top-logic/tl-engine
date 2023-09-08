/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.bus;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import com.top_logic.base.user.UserInterface;
import com.top_logic.event.bus.Sender;

/**
 * The Event which should be used to make Changes of Users available
 * to the ApplicationBus
 * Changes (sp types of this event are) :
 * CREATED, MODIFIED, DELETED, LOGGED_IN, LOGGED_OUT
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class UserEvent extends MonitorEvent {

    /** Type login. */
    public static final String LOGGED_IN  = "loggedIn";

    /** Type logout. */
    public static final String LOGGED_OUT = "loggedOut";

    /** The ID of the session. */
    private String          id;

    /** The machine, the user is working on. */
    private String          machine;

    /**
     * Creates a new UserEvent where the active user and the passive user are
     * the same. (see description of second constructor)
     * 
     * @param aSender the Sender which sends this event
     * @param aUser theUser which as triggered the event this User is active and
     *            passive at the same time e.g. a user has logged out himself
     * @param aMode Type of the Event - what has actually happened
     */
    public UserEvent(Sender aSender, UserInterface aUser, String anID, String aMachine, String aMode) {
        this(aSender, aUser, aUser, anID, aMachine, aMode);
    }

    /**
     * Creates a new UserEvent
     * 
     * @param aSender the Sender which sends this event
     * @param thePassiveUser theUser which is subject of this event e.g. the
     *            User who has been Logged in / out, created or whatever
     * @param theActiveUser User who performed the actions mentioned above
     * @param aMode Type of the Event - what has actually happened
     */
    public UserEvent(Sender aSender, UserInterface thePassiveUser, UserInterface theActiveUser, String anID, String aMachine, String aMode) {
        super(aSender, thePassiveUser, "not available", theActiveUser, aMode);

        this.id      = anID;
        this.machine = aMachine;
    }

    /**
     * Creates a new UserEvent
     * 
     * @param aSender the Sender which sends this event
     * @param thePassiveUser theUser which is subject of this event e.g. the
     *            User who has been Logged in / out, created or whatever
     * @param theActiveUser User who performed the actions mentioned above
     * @param aMode Type of the Event - what has actually happened
     */
    public UserEvent(Sender aSender, UserInterface thePassiveUser, UserInterface theActiveUser, Date aDate,
            String aMode) {
        super(aSender, thePassiveUser, "not available", theActiveUser, aDate, aMode);
    }

    /**
     * Return a debugging string representation of this instance.
     * 
     * @return    The string representation of this instance.
     */
    @Override
	public String toString() {
        return (this.getClass().getName() + " [" +
                    "user: '" + this.getPassiveUser() +
                    "', type: " + this.getType() +
                    ", machine: '" + this.machine +
                    "']");
    }

    /**
     * the passive User, which is the subject of this event
     */
    public UserInterface getPassiveUser() {
        return (UserInterface) (this.getMessage());
    }

    /**
     * the active user which has performed the change of the passive
     *         user. The active user can be the same as the passive
     */
    public UserInterface getActiveUser() {
        return (this.getUser());
    }

    /**
     * Return the ID of the session.
     * 
     * @return    The ID of the session.
     */
    public String getSessionID() {
        return (this.id);
    }

    /**
     * Return the IP address of the machine the user is working from.
     * 
     * @return    The users machine.
     */
    public String getMachine() {
        return (this.machine);
    }

    /**
     * Method to write MonitorEvent to PrintWriter.
     * 
     * @param aWriter writer to write to
     */
    @Override
	public void writeEvent(PrintWriter aWriter) throws IOException {
        Object theSource  = getSourceObject();
        Object theMessage = getMessage();
        aWriter.println(getID(theSource));
        aWriter.println(getType(theSource));
        aWriter.println(getUser(theMessage));
        aWriter.println(getDate().getTime());
        aWriter.println(getType());
        aWriter.println(getUser(this.getUser()));
    }

}
