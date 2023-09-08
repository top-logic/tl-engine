/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.event.logEntry;

import java.util.Date;
import java.util.List;

import com.top_logic.knowledge.wrap.person.Person;

/**
 * Common interface for either the archived or "hot" LogEntries.
 * 
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public interface UserDayEntry  {

    /**
     * Returns the person to which this UserDayEntry belongs
     */
    public Person getPerson();

    /**
     * returns the date of this UserdayEntry, the date represents the DAY on which all 
     * LogEntries of the UserDayEntry were created
     * 
     * @return a dtae representing the day for all LogEntries
     */
    public Date getDate();

    /**
     * Returns a list with all LogEntries belonging to this UserDayEntry.
     * The list contains NO duplicates.
     * 
     * @return a list with all LogEntries without duplicates
     */
    public List<LogEntry> getLogEntries();
}
