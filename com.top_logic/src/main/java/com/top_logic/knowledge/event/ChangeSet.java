/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.top_logic.knowledge.objects.identifier.ObjectBranchId;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.util.Utils;

/**
 * A {@link ChangeSet} represent a commit in the {@link KnowledgeBase}.
 * 
 * <p>
 * A {@link ChangeSet} is fully modifiable.
 * </p>
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ChangeSet {

	/**
	 * Visitor that adds the visited {@link KnowledgeEvent} to the corresponding list in the
	 * argument {@link ChangeSet}.
	 */
	private static final KnowledgeEventVisitor<ChangeSet, ChangeSet> CHANGESET_ENHANCER =
		new KnowledgeEventVisitor<>() {

			@Override
			public ChangeSet visitUpdate(ItemUpdate event, ChangeSet arg) {
				return arg.addUpdate(event);
			}

			@Override
			public ChangeSet visitDelete(ItemDeletion event, ChangeSet arg) {
				return arg.addDeletion(event);
			}

			@Override
			public ChangeSet visitCreateObject(ObjectCreation event, ChangeSet arg) {
				return arg.addCreation(event);
			}

			@Override
			public ChangeSet visitCommit(CommitEvent event, ChangeSet arg) {
				if (arg.getCommit() != null) {
					StringBuilder error = new StringBuilder();
					error.append("There is already a commit '");
					error.append(arg.getCommit());
					error.append("' for the given changeset '");
					error.append(arg);
					error.append("'");
					throw new IllegalArgumentException(error.toString());
				}
				return arg.setCommit(event);
			}

			@Override
			public ChangeSet visitBranch(BranchEvent event, ChangeSet arg) {
				return arg.addBranchEvent(event);
			}
		};

	/**
	 * {@link ItemEventVisitor} that merges the given {@link ItemUpdate} into the visited
	 * {@link ItemEvent}.
	 */
	public static final ItemEventVisitor<Void, ItemUpdate> MERGE_UPDATE =
		new ItemEventVisitor<>() {

			@Override
			public Void visitCreateObject(ObjectCreation event, ItemUpdate update) {
				event.getValues().putAll(update.getValues());
				return null;
			}

			@Override
			public Void visitUpdate(ItemUpdate event, ItemUpdate update) {
				event.getValues().putAll(update.getValues());
				Map<String, Object> oldValues = event.getOldValues();
				if (oldValues != null) {
					Map<String, Object> updatedOldValues = update.getOldValues();
					if (updatedOldValues != null) {
						oldValues.putAll(updatedOldValues);
					}
				}
				return null;
			}

			@Override
			public Void visitDelete(ItemDeletion event, ItemUpdate update) {
				Map<String, Object> updatedOldValues = update.getOldValues();
				if (updatedOldValues != null) {
					event.getValues().putAll(updatedOldValues);
				}
				return null;
			}

		};

	private static final KnowledgeEventVisitor<Void, ChangeSet> MERGE_INTO =
		new AbstractKnowledgeEventVisitor<>() {

			@Override
			public Void visitUpdate(ItemUpdate update, ChangeSet cs) {
				for (ItemChange target : cs.getCreations()) {
					if (mergeUpdateInto(target, update)) {
						return null;
					}
				}
				for (ItemChange target : cs.getUpdates()) {
					if (mergeUpdateInto(target, update)) {
						return null;
					}
				}
				for (ItemChange target : cs.getDeletions()) {
					if (mergeUpdateInto(target, update)) {
						return null;
					}
				}
				return super.visitUpdate(update, cs);
			}

			private boolean mergeUpdateInto(ItemChange baseEvent, ItemUpdate update) {
				if (baseEvent.getObjectId().equals(update.getObjectId())) {
					baseEvent.visitItemEvent(MERGE_UPDATE, update);
					return true;
				}
				return false;
			}

			@Override
			public Void visitDelete(ItemDeletion delete, ChangeSet cs) {
				for (Iterator<ObjectCreation> it = cs.getCreations().iterator(); it.hasNext();) {
					ObjectCreation target = it.next();
					if (target.getObjectId().equals(delete.getObjectId())) {
						// Drop creation.
						it.remove();

						// Ignore deletion. Create + delete in one change set are a no-op.
						return null;
					}
				}
				for (Iterator<ItemUpdate> it = cs.getUpdates().iterator(); it.hasNext();) {
					ItemUpdate target = it.next();
					if (target.getObjectId().equals(delete.getObjectId())) {
						// Drop update.
						it.remove();

						Map<String, Object> oldValues = target.getOldValues();
						if (oldValues != null) {
							// Old values of the delete are the values before the update, since the
							// update was dropped.
							delete.getValues().putAll(oldValues);
						}

						// Insert deletion.
						return super.visitDelete(delete, cs);
					}
				}
				return super.visitDelete(delete, cs);
			}

			@Override
			protected Void visitKnowledgeEvent(KnowledgeEvent event, ChangeSet cs) {
				cs.add(event);
				return null;
			}

		};

	private static class MergeIntoLargeChangeSet
			extends AbstractKnowledgeEventVisitor<Void, Map<ObjectBranchId, ItemEvent>> {

		private ChangeSet _cs;

		public MergeIntoLargeChangeSet(ChangeSet cs) {
			_cs = cs;
		}

		private ChangeSet cs() {
			return _cs;
		}

		public void merge(Iterable<? extends KnowledgeEvent> additionalEvents) {
			Map<ObjectBranchId, ItemEvent> index = createIndex();
			if (index == null) {
				// Clash in Ids. E.g. creation and update for same object.
				for (KnowledgeEvent additionalEvent : additionalEvents) {
					cs().merge(additionalEvent);
				}
			} else {
				for (KnowledgeEvent additionalEvent : additionalEvents) {
					additionalEvent.visit(this, index);
				}
			}

		}

		private Map<ObjectBranchId, ItemEvent> createIndex() {
			HashMap<ObjectBranchId, ItemEvent> result = new HashMap<>();
			for (ItemEvent event : cs().getCreations()) {
				ItemEvent clash = result.put(event.getObjectId(), event);
				if (clash != null) {
					return null;
				}
			}
			for (ItemEvent event : cs().getUpdates()) {
				ItemEvent clash = result.put(event.getObjectId(), event);
				if (clash != null) {
					return null;
				}
			}
			for (ItemEvent event : cs().getDeletions()) {
				ItemEvent clash = result.put(event.getObjectId(), event);
				if (clash != null) {
					return null;
				}
			}
			return result;
		}

		@Override
		public Void visitUpdate(ItemUpdate event, Map<ObjectBranchId, ItemEvent> arg) {
			ItemEvent mergeTarget = arg.get(event.getObjectId());
			if (mergeTarget != null) {
				mergeTarget.visitItemEvent(MERGE_UPDATE, event);
				return null;
			}
			return super.visitUpdate(event, arg);
		}

		@Override
		public Void visitDelete(ItemDeletion delete, Map<ObjectBranchId, ItemEvent> arg) {
			ItemEvent mergeTarget = arg.remove(delete.getObjectId());
			if (mergeTarget instanceof ObjectCreation) {
				// Drop creation.
				cs().getCreations().remove(mergeTarget);

				// Ignore deletion. Create + delete in one change set are a no-op.
				return null;
			} else if (mergeTarget instanceof ItemUpdate) {
				// Drop update.
				cs().getUpdates().remove(mergeTarget);

				Map<String, Object> oldValues = ((ItemUpdate) mergeTarget).getOldValues();
				if (oldValues != null) {
					// Old values of the delete are the values before the update, since the
					// update was dropped.
					delete.getValues().putAll(oldValues);
				}

				// Insert deletion.
				return super.visitDelete(delete, arg);
			}
			return super.visitDelete(delete, arg);
		}

		@Override
		protected Void visitItemEvent(ItemEvent event, Map<ObjectBranchId, ItemEvent> arg) {
			arg.put(event.getObjectId(), event);
			cs().add(event);
			return null;
		}

		@Override
		protected Void visitKnowledgeEvent(KnowledgeEvent event, Map<ObjectBranchId, ItemEvent> index) {
			cs().add(event);
			return null;
		}

	}

	/** @see #getCommit() */
	private CommitEvent _commit;

	/** @see #getCreations() */
	private final List<ObjectCreation> _creations = new ArrayList<>();

	/** @see #getUpdates() */
	private final List<ItemUpdate> _updates = new ArrayList<>();

	/** @see #getDeletions() */
	private final List<ItemDeletion> _deletions = new ArrayList<>();

	/** @see #getBranchEvents() */
	private final List<BranchEvent> _branchEvents = new ArrayList<>();

	/** @see #getRevision() */
	private long _revision;

	/**
	 * Creates a {@link ChangeSet}.
	 * 
	 * @param revision
	 *        see {@link #getRevision()}
	 */
	public ChangeSet(long revision) {
		_revision = revision;
	}

	/**
	 * The revision which is represented by this {@link ChangeSet}. All events within this
	 *         {@link ChangeSet} hav the same revision.
	 */
	public long getRevision() {
		return _revision;
	}

	/**
	 * Sets the value of {@link #getRevision()}.
	 * 
	 * <p>
	 * It must be ensured that each event has the same revision.
	 * </p>
	 * 
	 * @param revision
	 *        The new value of {@link #getRevision()}
	 */
	public void setRevision(long revision) {
		_revision = revision;
	}

	/**
	 * Returns the {@link CommitEvent} done to commit this {@link ChangeSet}.
	 * 
	 * <p>
	 * May be <code>null</code> to indicate that the {@link ChangeSet} is part of another
	 * {@link ChangeSet}. If the {@link #getCommit() commit} is <code>null</code>, and the
	 * {@link ChangeSet} is written to the {@link KnowledgeBase} then no commit is done.
	 * </p>
	 */
	public CommitEvent getCommit() {
		return _commit;
	}

	/**
	 * Sets the new value of {@link #getCommit()}.
	 * 
	 * @param commit
	 *        The new value of {@link #getCommit()}.
	 * 
	 * @return A reference to this {@link ChangeSet}
	 */
	public ChangeSet setCommit(CommitEvent commit) {
		_commit = commit;
		return this;
	}

	/**
	 * Returns the {@link ObjectCreation} done within this {@link ChangeSet}.
	 * 
	 * <p>
	 * The order of the {@link ObjectCreation} does <b>not</b> represent the order in which the
	 * object creation has been done within the commit.
	 * </p>
	 * 
	 * @return Never <code>null</code>. Result is empty if no object was created within this
	 *         {@link ChangeSet}.
	 */
	public List<ObjectCreation> getCreations() {
		return _creations;
	}

	/**
	 * Adds the given {@link ObjectCreation} to the list of {@link #getCreations() creations}.
	 * 
	 * @param evt
	 *        The {@link ObjectCreation} to add.
	 * 
	 * @return A reference to this {@link ChangeSet}.
	 */
	public ChangeSet addCreation(ObjectCreation evt) {
		getCreations().add(evt);
		return this;
	}

	/**
	 * Returns the {@link ItemUpdate} done within this {@link ChangeSet}.
	 * 
	 * <p>
	 * The order of the {@link ItemUpdate} does <b>not</b> represent the order in which the objects
	 * has been updated within the commit.
	 * </p>
	 * 
	 * @return Never <code>null</code>. Result is empty if no object was changed within this
	 *         {@link ChangeSet}.
	 */
	public List<ItemUpdate> getUpdates() {
		return _updates;
	}

	/**
	 * Adds the given {@link ItemUpdate} to the list of {@link #getUpdates() updates}.
	 * 
	 * @param evt
	 *        The {@link ItemUpdate} to add.
	 * 
	 * @return A reference to this {@link ChangeSet}.
	 */
	public ChangeSet addUpdate(ItemUpdate evt) {
		getUpdates().add(evt);
		return this;
	}

	/**
	 * Returns the {@link ItemDeletion} done within this {@link ChangeSet}.
	 * 
	 * <p>
	 * The order of the {@link ItemDeletion} does <b>not</b> represent the order in which the
	 * objects has been deleted within the commit.
	 * </p>
	 * 
	 * @return Never <code>null</code>. Result is empty if no object was deleted within this
	 *         {@link ChangeSet}.
	 */
	public List<ItemDeletion> getDeletions() {
		return _deletions;
	}

	/**
	 * Adds the given {@link ItemDeletion} to the list of {@link #getDeletions() deletions}.
	 * 
	 * @param evt
	 *        The {@link ItemDeletion} to add.
	 * 
	 * @return A reference to this {@link ChangeSet}.
	 */
	public ChangeSet addDeletion(ItemDeletion evt) {
		getDeletions().add(evt);
		return this;
	}

	/**
	 * Returns the {@link BranchEvent} done within this {@link ChangeSet}.
	 * 
	 * <p>
	 * The order of the {@link BranchEvent} does <b>not</b> represent the order in which the
	 * branches has been created within the commit.
	 * </p>
	 * 
	 * @return Never <code>null</code>. Result is empty if no branch was created within this
	 *         {@link ChangeSet}.
	 */
	public List<BranchEvent> getBranchEvents() {
		return _branchEvents;
	}

	/**
	 * Adds the given {@link BranchEvent} to the list of {@link #getBranchEvents() branch creations}
	 * .
	 * 
	 * @param evt
	 *        The {@link BranchEvent} to add.
	 * 
	 * @return A reference to this {@link ChangeSet}.
	 */
	public ChangeSet addBranchEvent(BranchEvent evt) {
		getBranchEvents().add(evt);
		return this;
	}

	/**
	 * Adds the given {@link KnowledgeEvent} to the corresponding list of {@link KnowledgeEvent}.
	 * 
	 * @param evt
	 *        The {@link KnowledgeEvent} to add.
	 * 
	 * @return A reference to this {@link ChangeSet}.
	 * 
	 * @see #merge(KnowledgeEvent)
	 */
	public ChangeSet add(KnowledgeEvent evt) {
		return evt.visit(CHANGESET_ENHANCER, this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (_revision ^ (_revision >>> 32));
		result = prime * result + ((_commit == null) ? 0 : _commit.hashCode());
		result = prime * result + _creations.hashCode();
		result = prime * result + _deletions.hashCode();
		result = prime * result + _branchEvents.hashCode();
		result = prime * result + _updates.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ChangeSet other = (ChangeSet) obj;
		if (_revision != other._revision)
			return false;
		if (_commit == null) {
			if (other._commit != null)
				return false;
		} else if (!_commit.equals(other._commit))
			return false;
		if (!equalsIgnoreOrder(_creations, other._creations))
			return false;
		if (!equalsIgnoreOrder(_deletions, other._deletions))
			return false;
		if (!equalsIgnoreOrder(_updates, other._updates))
			return false;
		if (!equalsIgnoreOrder(_branchEvents, other._branchEvents))
			return false;
		return true;
	}

	boolean equalsIgnoreOrder(List<?> l1, List<?> l2) {
		switch (l1.size()) {
			case 0:
				return l2.isEmpty();
			case 1:
				if (l2.size() != 1) {
					return false;
				}
				return Utils.equals(l1.get(0), l2.get(0));
			default:
				return new HashSet<Object>(l1).equals(new HashSet<>(l2));
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ChangeSet [rev=");
		builder.append(_revision);
		if (!_branchEvents.isEmpty()) {
			builder.append(", branchEvents=");
			builder.append(_branchEvents);
		}
		if (_commit != null) {
			builder.append(", commit=");
			builder.append(_commit);
		}
		if (!_creations.isEmpty()) {
			builder.append(", _creations=");
			builder.append(_creations);
		}
		if (!_updates.isEmpty()) {
			builder.append(", _updates=");
			builder.append(_updates);
		}
		if (!_deletions.isEmpty()) {
			builder.append(", _deletions=");
			builder.append(_deletions);
		}
		builder.append("]");
		return builder.toString();
	}

	/**
	 * Merges the given event into this {@link ChangeSet} by potentially modifying existing events.
	 * 
	 * <p>
	 * This method simulates a change to an open transaction (represented by this {@link ChangeSet}
	 * ). If the object touched by the given {@link KnowledgeEvent} touches an object already
	 * touched in this {@link ChangeSet} by another event, those two events are merged to a single
	 * event.
	 * </p>
	 * 
	 * @param additionalEvent
	 *        The event to add to this {@link ChangeSet}. In contrast to
	 *        {@link #add(KnowledgeEvent)}, existing events in this {@link ChangeSet} are
	 *        potentially modified, instead of simply adding the event.
	 * 
	 * @see #add(KnowledgeEvent)
	 * @see #mergeAll(Iterable)
	 */
	public void merge(KnowledgeEvent additionalEvent) {
		additionalEvent.visit(MERGE_INTO, this);
	}
	
	/**
	 * Merges all given events.
	 * 
	 * @param additionalEvents
	 *        The events to {@link #merge(KnowledgeEvent)} into this {@link ChangeSet}.
	 * 
	 * @see #merge(KnowledgeEvent)
	 */
	public void mergeAll(Iterable<? extends KnowledgeEvent> additionalEvents) {
		int largeCSThreshold = 1000;
		if (numberEvents() > largeCSThreshold || (additionalEvents instanceof Collection<?>
			&& ((Collection<?>) additionalEvents).size() > largeCSThreshold)) {
			new MergeIntoLargeChangeSet(this).merge(additionalEvents);
		} else {
			for (KnowledgeEvent additionalEvent : additionalEvents) {
				merge(additionalEvent);
			}
		}
	}

	private int numberEvents() {
		int result = 0;
		if (getCommit() != null) {
			result++;
		}
		result += getBranchEvents().size();
		result += getCreations().size();
		result += getUpdates().size();
		result += getDeletions().size();
		return result;
	}

	/**
	 * Creates a copy of this {@link ChangeSet}.
	 */
	public ChangeSet copy() {
		ChangeSet copy = new ChangeSet(getRevision());
		CopyKnowledgeEventVisitor copyVisitor = CopyKnowledgeEventVisitor.INSTANCE;
		for (BranchEvent evt : getBranchEvents()) {
			copy.addBranchEvent(copyVisitor.copy(evt));
		}
		for (ObjectCreation evt : getCreations()) {
			copy.addCreation(copyVisitor.copy(evt));
		}
		for (ItemDeletion evt : getDeletions()) {
			copy.addDeletion(copyVisitor.copy(evt));
		}
		for (ItemUpdate evt : getUpdates()) {
			copy.addUpdate(copyVisitor.copy(evt));
		}
		copy.setCommit(copyVisitor.copy(getCommit()));
		return copy;
	}
}
