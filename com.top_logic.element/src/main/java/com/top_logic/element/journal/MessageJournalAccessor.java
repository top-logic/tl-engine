/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.journal;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.Logger;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.knowledge.journal.JournalManager;
import com.top_logic.knowledge.journal.JournalResult;
import com.top_logic.knowledge.journal.JournalResultEntry;
import com.top_logic.knowledge.journal.MessageJournalResultAttributeEntry;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.DispatchingAccessor;
import com.top_logic.layout.ReadOnlyPropertyAccessor;

/**
 * Accessor for {@link JournalResult}s. 
 * 
 * @author <a href="mailto:tbe@top-logic.com">Till Benz</a>
 */
public final class MessageJournalAccessor extends DispatchingAccessor {

	public static final Accessor INSTANCE = new MessageJournalAccessor();
	
	public static final String COMMIT_ID_COLUMN = "commit-id";
	public static final String DATE_COLUMN = "date";
	public static final String USER_COLUMN = "user";
	public static final String MESSAGE_COLUMN = "message";
	
	private MessageJournalAccessor() {
		super(constructAccessors());
	}

	private static Map constructAccessors() {
		HashMap accessors = new HashMap();
		accessors.put(COMMIT_ID_COLUMN, new ReadOnlyPropertyAccessor() {
			@Override
			public Object getValue(Object target) {
				JournalResult jr = (JournalResult) target;
				return Long.valueOf(jr.commitId);
			}
		});
		accessors.put(DATE_COLUMN, new ReadOnlyPropertyAccessor() {
			@Override
			public Object getValue(Object target) {
				JournalResult jr = (JournalResult) target;
				return new Date(jr.timeMillis);
			}
		});
		accessors.put(USER_COLUMN, new ReadOnlyPropertyAccessor() {
			@Override
			public Object getValue(Object target) {
				JournalResult jr = (JournalResult) target;
				return Person.byName(jr.userID);
			}
		});
		accessors.put(MESSAGE_COLUMN, new ReadOnlyPropertyAccessor() {
			@Override
			public Object getValue(Object target) {
				JournalResult jr = (JournalResult) target;
				List resultEntries = (List) jr.entries;
				List attributeEntries = (List) ((JournalResultEntry) resultEntries.get(0)).getAttributEntries();
				MessageJournalResultAttributeEntry entry = (MessageJournalResultAttributeEntry) attributeEntries.get(0);
				return entry.getMessage();
			}
		});
		return accessors;
	}
	
	/** 
	 * Return the latest comment on a given supplier for the given meta attribute.
	 * 
	 * @param    aSupplier    The wrapper to get the comment for, must nor be <code>null</code>.
	 * @param    aMA          The meta attribute to get the comment for, must not be <code>null</code>.
	 * @return   The requested comment, never null but may be empty.
	 */
	public static String getLastComment(Wrapper aSupplier, TLStructuredTypePart aMA) {
	    try {
	        List theJournal = JournalManager.getInstance().getMessageJournal(KBUtils.getWrappedObjectName(aSupplier), aMA.getName(), null, null);

            if (!theJournal.isEmpty()) {
                Object theMessage = theJournal.get(theJournal.size() - 1);
    
                return (String) MessageJournalAccessor.INSTANCE.getValue(theMessage, MessageJournalAccessor.MESSAGE_COLUMN);
            }
	    }
	    catch (Exception ex) {
	        Logger.error("Unable to get latest comment for meta attribute " + aMA + " in " + aSupplier, ex, MessageJournalAccessor.class);
	    }

        return ("");
	}
}
