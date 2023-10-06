/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.event.layout;

import java.util.Collections;
import java.util.List;

import com.top_logic.event.logEntry.LogEntry;
import com.top_logic.event.logEntry.LogEntryFilter;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.mig.html.ModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.util.TLContext;

/**
 * Create the List of LogEntries based on a LogEntryFilter.
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class LogEntryBuilder implements ModelBuilder {

	/**
	 * Singleton {@link LogEntryBuilder} instance.
	 */
	public static final LogEntryBuilder INSTANCE = new LogEntryBuilder();

	private LogEntryBuilder() {
		// Singleton constructor.
	}

    /** 
     * If aComponent is a LogEntryFilterComponent, we can calculate the entries from there.
     * 
     * @see com.top_logic.mig.html.ModelBuilder#getModel(Object, com.top_logic.mig.html.layout.LayoutComponent)
     */
    @Override
	public Object getModel(Object businessModel, LayoutComponent aComponent) {
		if (businessModel instanceof LogEntryFilter) {
			return this.createList((LogEntryFilter) businessModel);
        }
		return Collections.emptyList();
    }

    /** 
     * We do support a LogEntryFilter as model.
     *       
     * @see com.top_logic.mig.html.ModelBuilder#supportsModel(java.lang.Object, LayoutComponent)
     */
    @Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
        return aModel instanceof LogEntryFilter;
    }

    /**
     * Create the List of logEntries by resolving the LogEntryFilter.
     */
    protected List<LogEntry> createList(LogEntryFilter aFilter) {
		{
			PersonManager r = PersonManager.getManager();
			Person thePerson = TLContext.currentUser();
            List<LogEntry> theResult = aFilter.getLogEntries(thePerson);

            theResult = this.filterBySelection(theResult, aFilter.getSelection());

            return theResult;
        }
    }

    /**
	 * Please override to care for aSelection.
	 * 
	 * @param aResult
	 *        The result to shrink.
	 * @param aSelection
	 *        The {@link LogEntryFilter#getSelection() selection} of the used {@link LogEntryFilter}
	 * 
	 */
	protected List<LogEntry> filterBySelection(List<LogEntry> aResult, Object aSelection) {
        return aResult;
    }

}
