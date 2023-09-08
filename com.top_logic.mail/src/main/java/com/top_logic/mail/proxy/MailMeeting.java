/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mail.proxy;

import java.util.Date;
import java.util.Properties;

/**
 * Simple representation of a meeting as located on a mail server.
 * 
 * This interface should allow an easy access to meeting information as they are stored
 * on the server.
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public interface MailMeeting extends MailServerMessage {

    /** Invitation state of meeting (returned by {@link #getStatus()}. */
    public static final String INVITATION = "CONFIRMED";

    /** 
     * Return the start date of the meeting within the mail server.
     * 
     * @return    The requested start date, never <code>null</code>.
     */
    public Date getStartDate();

    /** 
     * Return the end date of the meeting within the mail server.
     * 
     * @return    The requested end date, never <code>null</code>.
     */
    public Date getEndDate();

    /** 
     * Return the location the meeting takes place within the mail server.
     * 
     * @return    The requested location, may be <code>null</code>.
     */
    public String getLocation();

    /** 
     * Return the state of the meeting.
     * 
     * State can be one of the following:
     * <ul>
     *   <li>CONFIRMED: Invitation to a meeting.</li>
     *   <li>TENTATIVE: Answer to a meeting.</li>
     *   <li>CANCELLED: Meeting has been canceled.</li>
     * </ul>
     * 
     * @return The requested status as string.
     */
    public String getStatus();

    /** 
     * Return the mail addresses of the participants of the meeting.
     * 
     * This will be all receivers of the mail and the sender himself.
     * 
     * @return    The requested participants, never <code>null</code>.
     */
    public String[] getParticipant();

    /** 
     * Return the description text of the meeting.
     * 
     * @return    The requested description, must not be <code>null</code>, may be empty.
     */
    public String getDescription();

    /** 
     * Common interface for accessing the properties send within the "text/calendar" entry.
     * 
     * @return    The requested properties, never <code>null</code>.
     */
    public Properties getProperties();

    /** 
     * Debugging representation of this instance.
     * 
     * @return    Debugging string.
     */
    @Override
	public String toString();
}
