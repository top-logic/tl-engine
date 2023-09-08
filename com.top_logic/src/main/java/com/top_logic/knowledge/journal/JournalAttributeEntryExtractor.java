/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.journal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.top_logic.knowledge.event.ItemDeletion;
import com.top_logic.knowledge.event.ItemEvent;
import com.top_logic.knowledge.event.ItemEventVisitor;
import com.top_logic.knowledge.event.ItemUpdate;
import com.top_logic.knowledge.event.ObjectCreation;
import com.top_logic.util.Utils;

/**
 * Creates a list of {@link JournalAttributeEntry} for a given event.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class JournalAttributeEntryExtractor implements ItemEventVisitor<List<ChangeJournalAttributeEntry>, Void> {

	/** An instance of {@link JournalAttributeEntryExtractor}. */
	public static final JournalAttributeEntryExtractor INSTANCE = new JournalAttributeEntryExtractor();

	/**
	 * Creates a list of {@link JournalAttributeEntry} for the given event.
	 */
	public List<ChangeJournalAttributeEntry> createEntries(ItemEvent event) {
		return event.visitItemEvent(this, null);
	}

	@Override
	public List<ChangeJournalAttributeEntry> visitCreateObject(ObjectCreation event, Void arg) {
		Map<String, Object> oldValues = Collections.emptyMap();
		Map<String, Object> newValues = event.getValues();
		Collection<String> attributes = newValues.keySet();
		return createJournalEntries(attributes, oldValues, newValues);
	}

	@Override
	public List<ChangeJournalAttributeEntry> visitDelete(ItemDeletion event, Void arg) {
		Map<String, Object> oldValues = event.getValues();
		Map<String, Object> newValues = Collections.emptyMap();
		Collection<String> attributes = newValues.keySet();
		return createJournalEntries(attributes, oldValues, newValues);
	}

	@Override
	public List<ChangeJournalAttributeEntry> visitUpdate(ItemUpdate event, Void arg) {
		Map<String, Object> oldValues = event.getOldValues();
		Map<String, Object> newValues = event.getValues();
		Collection<String> attributes = newValues.keySet();
		return createJournalEntries(attributes, oldValues, newValues);
	}

	/**
	 * Creates a List of {@link ChangeJournalAttributeEntry} from the given values.
	 * 
	 * @param attributes
	 *        The set of attributes to create entries for.
	 * @param oldValues
	 *        Holder of the old values
	 * @param newValues
	 *        Holder of the new values.
	 */
	protected List<ChangeJournalAttributeEntry> createJournalEntries(Collection<String> attributes,
			Map<String, Object> oldValues, Map<String, Object> newValues) {
		List<ChangeJournalAttributeEntry> result = new ArrayList<>(attributes.size());
		for (String attribute : attributes) {
			Object oldValue = oldValues.get(attribute);
			Object newValue = newValues.get(attribute);
			assert !Utils.equals(oldValue, newValue);
			addEntry(result, attribute, oldValue, newValue);
		}
		return result;
	}

	/**
	 * Adds an {@link JournalAttributeEntry} for the given attribute and the given old and new
	 * values.
	 */
	protected void addEntry(List<ChangeJournalAttributeEntry> result, String attribute, Object oldValue, Object newValue) {
		JournalAttributeEntryImpl journalEntry = new JournalAttributeEntryImpl(attribute, oldValue, newValue);
		result.add(journalEntry);
	}
}
