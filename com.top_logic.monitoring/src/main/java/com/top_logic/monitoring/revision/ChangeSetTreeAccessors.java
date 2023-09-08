/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.monitoring.revision;

import java.util.Date;

import com.top_logic.basic.TLID;
import com.top_logic.basic.util.ResKey;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.util.MetaObjectUtils;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.CommitEvent;
import com.top_logic.knowledge.event.ItemDeletion;
import com.top_logic.knowledge.event.ItemEvent;
import com.top_logic.knowledge.event.ItemEventVisitor;
import com.top_logic.knowledge.event.ItemUpdate;
import com.top_logic.knowledge.event.ObjectCreation;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;
import com.top_logic.knowledge.service.Branch;
import com.top_logic.knowledge.service.HistoryManager;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseRuntimeException;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.wrap.WrapperAccessor;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.ReadOnlyAccessor;
import com.top_logic.layout.accessors.TypeSafeAccessor;
import com.top_logic.layout.compare.AbstractAuthorAccessor;
import com.top_logic.monitoring.revision.ChangeSetTreeBuilder.ChangeEntry;

/**
 * Static class providing utility classes and methods for the change set tree.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ChangeSetTreeAccessors {

	static KnowledgeBase kb() {
		return PersistencyLayer.getKnowledgeBase();
	}

	/**
	 * Resolves the object changed in the given event in the event revision.
	 */
	public static Object resolveChangedObject(HistoryManager hm, ItemEvent event) {
		ObjectBranchId id = event.getObjectId();
		Revision revision;
		MetaObject type = id.getObjectType();
		if (MetaObjectUtils.isVersioned(type)) {
			long eventRevision = event.getRevision();
			if (event instanceof ItemDeletion) {
				// Object was deleted in event revision.
				eventRevision--;
			}
			revision = hm.getRevision(eventRevision);
		} else {
			revision = Revision.CURRENT;
		}
		Branch branch = hm.getBranch(id.getBranchId());
		TLID objectName = id.getObjectName();
		KnowledgeItem item;
		try {
			item = hm.getKnowledgeItem(branch, revision, type, objectName);
		} catch (DataObjectException ex) {
			throw new KnowledgeBaseRuntimeException(ex);
		}
		if (item == null) {
			return id;
		} else {
			return item.getWrapper();
		}
	}

	/**
	 * {@link TypeSafeAccessor} for {@link ItemEvent}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static abstract class ItemEventAccessor extends TypeSafeAccessor<ItemEvent> {

		@Override
		protected Class<? extends ItemEvent> getType() {
			return ItemEvent.class;
		}

	}

	/**
	 * Accesses the historic version of the touched object in the accessed {@link ItemEvent}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class ObjectAccessor extends ItemEventAccessor {

		@Override
		public Object getValueTyped(ItemEvent object, String property) {
			HistoryManager hm = kb().getHistoryManager();
			return resolveChangedObject(hm, object);
		}
	}

	/**
	 * Accesses the {@link MetaObject} of the changed item.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class ObjectTypeAccessor extends ItemEventAccessor {

		@Override
		public Object getValueTyped(ItemEvent object, String property) {
			return object.getObjectType();
		}
	}

	/**
	 * Whether the {@link MetaObject} of the changed object of the accessed {@link ItemEvent} is
	 * versioned.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class SupportsHistoryAccessor extends ItemEventAccessor {

		@Override
		public Object getValueTyped(ItemEvent object, String property) {
			return MetaObjectUtils.isVersioned(object.getObjectType());
		}
	}

	/**
	 * {@link Accessor} for the {@link ResKey} describing whether the accessed {@link ItemEvent} is
	 * a creation, deletion, or update event.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class ChangeTypeAccessor extends ItemEventAccessor
			implements ItemEventVisitor<Object, Void> {

		@Override
		public Object getValueTyped(ItemEvent object, String property) {
			return object.visitItemEvent(this, null);
		}

		@Override
		public Object visitCreateObject(ObjectCreation event, Void arg) {
			return I18NConstants.CHANGE_TYPE_CREATION;
		}

		@Override
		public Object visitDelete(ItemDeletion event, Void arg) {
			return I18NConstants.CHANGE_TYPE_DELETION;
		}

		@Override
		public Object visitUpdate(ItemUpdate event, Void arg) {
			return I18NConstants.CHANGE_TYPE_UPDATE;
		}
	}

	/**
	 * {@link TypeSafeAccessor} for {@link ChangeSet}
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static abstract class ChangeSetAccessor extends TypeSafeAccessor<ChangeSet> {

		@Override
		protected Class<? extends ChangeSet> getType() {
			return ChangeSet.class;
		}

	}

	/**
	 * Access to the number of touched objects in the accessed {@link ChangeSet}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class ObjectCountAccessor extends ChangeSetAccessor {

		@Override
		public Object getValueTyped(ChangeSet object, String property) {
			return object.getCreations().size() + object.getUpdates().size() + object.getDeletions().size();
		}
	}

	/**
	 * Access to {@link WrapperAccessor#SELF self} reference in the {@link ChangeSet} tree.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class SelfAccessor extends ReadOnlyAccessor<Object> {

		@Override
		public Object getValue(Object object, String property) {
			HistoryManager hm = kb().getHistoryManager();
			if (object instanceof ChangeSet) {
				return hm.getRevision(((ChangeSet) object).getRevision());
			}
			if (object instanceof ItemEvent) {
				return resolveChangedObject(hm, (ItemEvent) object);
			}
			if (object instanceof ChangeEntry) {
				ChangeEntry changeEntry = (ChangeEntry) object;
				if (changeEntry.getAttributeI18N() != null) {
					return changeEntry.getAttributeI18N();
				}
				return changeEntry.getAttribute();
			}
			return null;
		}
	}

	/**
	 * Access to the author of the accessed {@link ChangeSet}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class AuthorAccessor extends AbstractAuthorAccessor<Object> {

		@Override
		public Object getValue(Object object, String property) {
			if (!(object instanceof ChangeSet)) {
				return null;
			}
			CommitEvent commit = ((ChangeSet) object).getCommit();
			if (commit == null) {
				return null;
			}
			return resolveAuthor(commit.getAuthor());
		}
	}

	/**
	 * Access to the date of the accessed {@link ChangeSet}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class DateAccessor extends ChangeSetAccessor {

		@Override
		public Object getValueTyped(ChangeSet object, String property) {
			CommitEvent commit = object.getCommit();
			if (commit == null) {
				return null;
			}
			return new Date(commit.getDate());
		}
	}

	/**
	 * Access to the log message of the accessed {@link ChangeSet}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class LogAccessor extends ChangeSetAccessor {

		@Override
		public Object getValueTyped(ChangeSet object, String property) {
			CommitEvent commit = object.getCommit();
			if (commit == null) {
				return null;
			}
			return commit.getLog();
		}
	}

	/**
	 * {@link TypeSafeAccessor} for {@link ChangeEntry}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static abstract class ChangeEntryAccessor extends TypeSafeAccessor<ChangeEntry> {

		@Override
		protected Class<? extends ChangeEntry> getType() {
			return ChangeEntry.class;
		}

	}

	/**
	 * Access to {@link ChangeEntry#getNewValue() new value} of the accessed {@link ChangeEntry}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class NewAttributeValueAccessor extends ChangeEntryAccessor {

		@Override
		public Object getValueTyped(ChangeEntry object, String property) {
			return object.getNewValue();
		}
	}

	/**
	 * Access to {@link ChangeEntry#getNewValue() new value} of the accessed {@link ChangeEntry}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class OldAttributeValueAccessor extends ChangeEntryAccessor {

		@Override
		public Object getValueTyped(ChangeEntry object, String property) {
			return object.getOldValue();
		}
	}

}


