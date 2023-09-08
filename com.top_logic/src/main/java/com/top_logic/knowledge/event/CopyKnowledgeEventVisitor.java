/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.event;

import java.util.Map;

/**
 * {@link KnowledgeEventVisitor} which returns a copy of the visited {@link KnowledgeEvent}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CopyKnowledgeEventVisitor implements KnowledgeEventVisitor<KnowledgeEvent, Void> {

	/** Singleton {@link CopyKnowledgeEventVisitor}. */
	public static CopyKnowledgeEventVisitor INSTANCE = new CopyKnowledgeEventVisitor();

	/**
	 * Copies the given {@link KnowledgeEvent}.
	 */
	@SuppressWarnings("unchecked")
	public <T extends KnowledgeEvent> T copy(T event) {
		if (event == null) {
			return null;
		}
		return (T) event.visit(this, null);
	}

	@Override
	public KnowledgeEvent visitCreateObject(ObjectCreation event, Void arg) {
		ObjectCreation result = new ObjectCreation(event.getRevision(), event.getObjectId());
		copyItemChange(event, result);
		return result;
	}

	private void copyItemChange(ItemChange source, ItemChange target) {
		target.getValues().putAll(source.getValues());
	}

	@Override
	public KnowledgeEvent visitDelete(ItemDeletion event, Void arg) {
		ItemDeletion result = new ItemDeletion(event.getRevision(), event.getObjectId());
		copyItemChange(event, result);
		return result;
	}

	@Override
	public KnowledgeEvent visitUpdate(ItemUpdate event, Void arg) {
		Map<String, Object> oldValues = event.getOldValues();
		ItemUpdate result = new ItemUpdate(event.getRevision(), event.getObjectId(), oldValues != null);
		copyItemChange(event, result);
		if (oldValues != null) {
			result.getOldValues().putAll(oldValues);
		}
		return result;
	}

	@Override
	public KnowledgeEvent visitBranch(BranchEvent event, Void arg) {
		BranchEvent result = new BranchEvent(event.getRevision(), event.getBranchId(), event.getBaseBranchId(),
			event.getBaseRevisionNumber());
		result.setBranchedTypeNames(event.getBranchedTypeNames());
		return result;
	}

	@Override
	public KnowledgeEvent visitCommit(CommitEvent event, Void arg) {
		return new CommitEvent(event.getRevision(), event.getAuthor(), event.getDate(), event.getLog());
	}

}

