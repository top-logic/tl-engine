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
 * This class gives the possibility to store and retrive LogEntries for Persons.
 * 
 * This class is a complex value holder along the "hasLogEntry" KA from
 * Person to LogEntry.
 * 
 * This is done by creating a knowledge association between the person and the LogEntry.
 * 
 *  The idea is, that for a period of two days the connection between Person and LogEntry is stored in 
 *  this way. After that, the connection is moved into an ArchiveUserDayEntry.
 *  This is done by the {@link DayEntryArchiverTask}.
 *  
 * 
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public class ActualUserDayEntry implements UserDayEntry {

    private Person person;

    private Date   date;

    /** List of {@link LogEntry LogEntries} matching person and date. */
    private List<LogEntry>   entries;

    /**
	 * Creates a {@link ActualUserDayEntry}.
	 */
    protected ActualUserDayEntry(Person aPerson, Date aDate, List<LogEntry> aList) {
        this.person  = aPerson;
        this.date    = LogEntry.alignToMiddleOfDay(aDate);
        this.entries = aList;
    }

    @Override
	public Person getPerson() {
        return this.person;
    }

    @Override
	public Date getDate() {
        return this.date;
    }
    
    @Override
	public List<LogEntry> getLogEntries() {
        return this.entries;
    }

    @Override
	public String toString() {
        return "ActualUserDayEntry for '" + person.getName() 
               + "' at " + date + " #" + entries.size();
    }
}
