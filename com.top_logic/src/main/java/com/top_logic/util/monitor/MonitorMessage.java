/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.monitor;

import java.io.Serializable;

import com.top_logic.basic.StringServices;

/**
 * Message returned as a result of a test performed by a
 * {@link com.top_logic.util.monitor.MonitorComponent}.
 * 
 * @see MonitorComponent#checkState(MonitorResult)
 * @see MonitorResult#addMessage(MonitorMessage)
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class MonitorMessage implements Serializable {

	/**
	 * Status of a message, see {@link MonitorMessage#getType()}.
	 */
	public enum Status {

		/** State flag for an information message. */
		INFO,

		/** State flag for an error message. */
		ERROR,

		/** State flag for a fatal message. */
		FATAL,

		;
	}

	/** The sending component. */
    private transient Object component;

    /** The display message to be send. */
    private String message;

	/** The type of report. */
	private Status type;

    /**
	 * Create a new instance of a message.
	 * 
	 * @param aType
	 *        See {@link #getType()}.
	 * @param aMessage
	 *        The message, must not be <code>null</code>.
	 * @param aComp
	 *        The {@link MonitorComponent} sending the message, must not be <code>null</code>.
	 * @throws IllegalArgumentException
	 *         If one parameter is invalid according to the documentation above.
	 */
	public MonitorMessage(Status aType, String aMessage, Object aComp) throws IllegalArgumentException {
        if (!(Status.INFO.equals(aType) || Status.ERROR.equals(aType) || Status.FATAL.equals(aType))) {
            throw new IllegalArgumentException("Type of message is invalid (given value is '" +
                                               aType + "')!");
        }
        else if (aComp == null) {
            throw new IllegalArgumentException("No component defined (is null)!");
        }
        else if (StringServices.isEmpty(aMessage)) {
            throw new IllegalArgumentException("No message defined (is null or empty)!");
        }

        this.type      = aType;
        this.message   = aMessage;
        this.component = aComp;
    }

    /**
     * Return a string representation of this instance.
     * 
     * @return    The string representation of this instance.
     */
    @Override
	public String toString() {
        return (this.getClass().getName() + " [" +
                    "type: " + this.type +
                    ", message: '" + this.message +
                    "', component: " + this.component +
                    "]");
    }

    /**
     * Return the type / level of this message.
     */
	public Status getType() {
        return (this.type);
    }

    /**
     * The component sending this message.
     * 
     * @return    The component sending this message, may be <code>null</code>
     *            if component is not within this VM (is a transient attribute). 
     */
    public Object getComponent() {
        return (this.component);
    }

    /**
     * The explaining message of this instance.
     * 
     * @return    The message to be used for displaying this instance (not the
     *            debugging value but the one for the UI).
     */
    public String getMessage() {
        return (this.message);
    }
}
