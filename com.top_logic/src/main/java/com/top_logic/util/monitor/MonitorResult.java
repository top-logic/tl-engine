/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.monitor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.top_logic.util.monitor.MonitorMessage.Status;

/**
 * The result of an application wide check of the components.
 * 
 * <p>
 * The result can have three levels:
 * </p>
 * 
 * <ol>
 * <li>(isOK() &amp;&amp; isAlive()) == true: Everything OK, system is running normal.</li>
 * <li>(!isOK() &amp;&amp; isAlive()) == true: System is running but some component may fail. The
 * main components are runnning in a normal state (e.g. knowledge base).</li>
 * <li>(!isOK() &amp;&amp; !isAlive()) == true: System is not running. At least one of the main
 * components (e.g. knowledge base) is not up and running.</li>
 * </ol>
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class MonitorResult implements Serializable {

    /** The internal flag, if everything is OK. */
    private boolean okFlag;

    /** The internal flag, if application is running. */
    private boolean aliveFlag;

    /** The list of collected {@link MonitorMessage messages}. */
	private List<MonitorMessage> messages;

    /**
     * Create the result of an application test.
     */
    public MonitorResult() {
        super();

        this.okFlag    = true;
        this.aliveFlag = true;
		this.messages = new ArrayList<>();
    }

    /**
     * Return a string repesentation of this instance.
     * 
     * @return    The string representation for debugging.
     */
    @Override
	public String toString() {
        return (this.getClass().getName() + " ["
                + "ok: " + this.okFlag
                + ", alive: " + this.aliveFlag 
                + ", messages: " + this.messages.size() 
                + "]");
    }

    /**
	 * Return, if the monitored application is OK.
	 * 
	 * @return <code>true</code>, if all checked components return a {@link MonitorMessage} with
	 *         level {@link MonitorMessage.Status#INFO}.
	 */
    public boolean isOK() {
        return (this.okFlag);
    }

    /**
     * Return, if no component within the application has a fatal error.
     * 
     * @return    <code>true</code>, if no component reports a fatal error.
     */
    public boolean isAlive() {
        return (this.aliveFlag);
    }

    /**
     * Return the detailed {@link MonitorMessage monitor messages}.
     * 
     * @return    A list of the messages collected (will not be <code>null</code>).
     */
	public List<MonitorMessage> getMessages() {
        return (this.messages);
    }

    /**
     * Append the given message to the internal list.
     * 
     * @param    aMessage    The message to be appended.
     */
    public void addMessage(MonitorMessage aMessage) {
		Status theType = aMessage.getType();

        this.messages.add(aMessage);

        this.okFlag    = this.okFlag && MonitorMessage.Status.INFO.equals(theType);
        this.aliveFlag = this.aliveFlag && !MonitorMessage.Status.FATAL.equals(theType);
    }
}
