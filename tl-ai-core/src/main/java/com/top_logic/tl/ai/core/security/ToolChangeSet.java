/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.top_logic.basic.Logger;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.element.meta.AssociationStorageDescriptor;
import com.top_logic.element.meta.SeparateTableStorage;
import com.top_logic.element.model.cache.ElementModelCacheService;
import com.top_logic.element.model.cache.ModelTables;
import com.top_logic.knowledge.event.ItemChange;
import com.top_logic.knowledge.event.ItemUpdate;
import com.top_logic.knowledge.objects.ChangeInspectable;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;
import com.top_logic.knowledge.service.db2.DBKnowledgeItem;
import com.top_logic.knowledge.service.db2.DefaultDBContext;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;

/**
 * The model changes a turn accumulated in its transaction, as seen by the {@link PreCommitGuard}.
 *
 * <p>
 * Read from the engine's own transaction bookkeeping immediately before the per-turn commit rather
 * than reported by the tools - see {@link #pending(KnowledgeBase)}. That is the whole point of the
 * layer: it exists for tools that performed no check of their own, and a tool that forgot to check
 * would equally forget to report.
 * </p>
 *
 * <p>
 * The guard needs the touched attributes per object, not only the objects, because model security is
 * attribute-granular: a user may be permitted to write an object yet not one specific attribute of
 * it.
 * </p>
 *
 * @param created
 *        The objects created during the turn, each with the type and the composition it was created
 *        into.
 * @param updated
 *        Objects modified during the turn, mapped to the attributes that were touched - an attribute
 *        whose values the engine keeps outside the object's own row included, on the object holding
 *        it. An object <em>created</em> in the same transaction never appears here - the engine books
 *        its attribute writes as part of the creation - which is the concrete form the
 *        {@link PreCommitGuard known limit} takes.
 * @param deleted
 *        Objects deleted during the turn. A row that only stores a value of another object - the link
 *        of a reference, the translation of an internationalized attribute - is no object of its own
 *        and is never listed here.
 *
 * @see PreCommitGuard#check(com.top_logic.tl.ai.core.agent.SecurityContext, ToolChangeSet)
 */
public record ToolChangeSet(
		List<Creation> created,
		Map<TLObject, Set<TLStructuredTypePart>> updated,
		List<TLObject> deleted) {

	/**
	 * One object created during the turn.
	 *
	 * <p>
	 * Carries the type and the container rather than only the new object, because those are the two
	 * things the guard is able to check: the new object itself does not exist in the committed state,
	 * where the checks are evaluated. See the {@link PreCommitGuard known limit}.
	 * </p>
	 *
	 * @param object
	 *        The created object. Kept for the audit record; not itself checkable.
	 * @param type
	 *        Its type, or <code>null</code> if the object has none (not an application object).
	 * @param container
	 *        The object it was created in, or <code>null</code> for a top-level creation.
	 * @param containerReference
	 *        The composition attribute of {@code container} it was created into, or
	 *        <code>null</code> if it was not created into a composition.
	 */
	public record Creation(TLObject object, TLClass type, TLObject container,
			TLStructuredTypePart containerReference) {

		/** */
		public Creation {
			if (object == null) {
				throw new IllegalArgumentException("Created object must not be null.");
			}
		}
	}

	/** An empty change set - the state of every turn in which no write tool ran. */
	public static final ToolChangeSet EMPTY = new ToolChangeSet(List.of(), Map.of(), List.of());

	/** */
	public ToolChangeSet {
		created = created == null ? List.of() : List.copyOf(created);
		// Insertion order preserved rather than Map.copyOf: the guard walks this map to build the
		// audit records, and an audit log whose line order varies between two identical turns is
		// harder to read than it needs to be.
		updated = updated == null ? Map.of()
			: Collections.unmodifiableMap(new LinkedHashMap<>(updated));
		deleted = deleted == null ? List.of() : List.copyOf(deleted);
	}

	/**
	 * Whether the turn changed nothing, in which case the guard has nothing to check.
	 */
	public boolean isEmpty() {
		return created.isEmpty() && updated.isEmpty() && deleted.isEmpty();
	}

	/**
	 * The changes the current transaction of the given {@link KnowledgeBase} has accumulated so far.
	 *
	 * <p>
	 * Reads the engine's transaction bookkeeping ({@link DefaultDBContext#getNewObjects()},
	 * {@link DefaultDBContext#getChangedObjects()}, {@link DefaultDBContext#getDroppedObjects()}) and
	 * the per-item {@link ChangeInspectable#getChange() change record} the engine also uses to build
	 * its commit event. Nothing here trusts a tool to declare what it did.
	 * </p>
	 *
	 * <p>
	 * <b>Only called for a run that enforces model access.</b> A run that enforces nothing never asks
	 * for the change set, so it pays none of this - see
	 * {@link PreCommitGuard#forRun()}.
	 * </p>
	 *
	 * <p>
	 * <b>An attribute whose values the engine keeps outside the row of the object holding it</b>
	 * books no change on that object. The engine creates, changes and deletes the rows that store the
	 * values instead: the links of a reference (every reference a model does not configure into a
	 * column, an enumeration value among them), the translations of an internationalized attribute,
	 * and the rows of the parts of a composition that keeps the container in the part's own row, as
	 * tl.doc keeps the children of a page. Each such row is read as what it is, a write of the
	 * attribute whose value it stores, and booked in {@link #updated()} on the object holding that
	 * attribute (Gitea #631, #638). Which object and which attribute a row stores a value of is the
	 * engine's own description of its storages ({@link ModelTables#getDescriptorsForTable}), the one
	 * its change log reads for the same question, so every {@link SeparateTableStorage} is covered,
	 * an application's own included.
	 * </p>
	 *
	 * <p>
	 * Left out are the rows that go with the creation or the deletion of an object, which the guard
	 * checks as that: the rows of an object created or deleted in the transaction, and a row dropped
	 * together with an object it refers to or with the part it is - else deleting an object would ask
	 * for the right to write every object that referred to it.
	 * </p>
	 *
	 * @param kb
	 *        The knowledge base whose current transaction to read. May be <code>null</code>.
	 * @return The accumulated changes, or {@link #EMPTY} if there is no transaction, or if the
	 *         knowledge base is not one whose bookkeeping can be read.
	 */
	public static ToolChangeSet pending(KnowledgeBase kb) {
		if (!(kb instanceof DBKnowledgeBase db)) {
			// Not the engine's own implementation: nothing to read, and guessing would be worse than
			// saying so.
			Logger.warn("Cannot read the pending changes of knowledge base '" + kb
				+ "': the pre-commit guard checks nothing for this turn.", ToolChangeSet.class);
			return EMPTY;
		}
		if (!(db.getCurrentDBContext() instanceof DefaultDBContext context)) {
			// No modifications were made in this interaction, or they are hidden (the immutable
			// context installed by KnowledgeBase.withoutModifications).
			return EMPTY;
		}

		Reader reader = new Reader(kb, context);
		reader.read();
		Map<TLObject, Set<TLStructuredTypePart>> updated = reader._updated;
		for (ValueWrite write : reader._writes) {
			if (write.consequence()) {
				continue;
			}
			Set<TLStructuredTypePart> attributes =
				updated.computeIfAbsent(write.holder(), key -> new LinkedHashSet<>());
			if (write.attribute() != null) {
				attributes.add(write.attribute());
			}
		}
		List<TLObject> deleted = deletions(context.getDroppedObjects(), reader._valueRows);
		if (reader._created.isEmpty() && updated.isEmpty() && deleted.isEmpty()) {
			return EMPTY;
		}
		return new ToolChangeSet(reader._created, updated, deleted);
	}

	/**
	 * The attributes kept outside the row of the object holding them that the current transaction
	 * changed, by that object - those that went with the creation or the deletion of an object
	 * included.
	 *
	 * <p>
	 * For a report of what a tool did. {@link #pending(KnowledgeBase)} leaves a row that went with a
	 * creation or a deletion to the guard's check of that, but a report shows its effect: deleting an
	 * object clears every reference to it, and that is a change of each object that referred to it.
	 * </p>
	 *
	 * @param kb
	 *        The knowledge base whose current transaction to read.
	 * @return The changed attributes by the object they belong to; empty without a transaction.
	 */
	public static Map<TLObject, Set<TLStructuredTypePart>> pendingSeparateTableChanges(KnowledgeBase kb) {
		if (!(kb instanceof DBKnowledgeBase db) || !(db.getCurrentDBContext() instanceof DefaultDBContext context)) {
			return Map.of();
		}
		Reader reader = new Reader(kb, context);
		reader.read();
		Map<TLObject, Set<TLStructuredTypePart>> result = new LinkedHashMap<>();
		for (ValueWrite write : reader._writes) {
			if (write.attribute() != null) {
				result.computeIfAbsent(write.holder(), key -> new LinkedHashSet<>()).add(write.attribute());
			}
		}
		return result;
	}

	/**
	 * A row the transaction created, changed or dropped that stores a value of {@link #attribute()}
	 * of {@link #holder()}: a write of that attribute.
	 *
	 * @param holder
	 *        The object holding the attribute.
	 * @param attribute
	 *        The attribute as the holder's type has it, or <code>null</code> if the row names one that
	 *        does not resolve; then only the right to write the holder can be asked.
	 * @param consequence
	 *        Whether the row went with the deletion of an object, which the guard checks as that: the
	 *        row was dropped together with an object it refers to, with the attribute it stores (a
	 *        model change), or with the part it is.
	 */
	private record ValueWrite(TLObject holder, TLStructuredTypePart attribute, boolean consequence) {
		// Intentionally empty.
	}

	/**
	 * How a row is read.
	 */
	private enum Version {

		/** A created or changed row, as it is now. */
		AFTER,

		/** A changed row, as it was before the transaction. */
		BEFORE,

		/** A dropped row that stored a value of another object, as it was before the transaction. */
		DROPPED,

		/**
		 * The row of an object deleted in the transaction that also stored a value of another object -
		 * a part kept in its own row - as it was before.
		 */
		DELETED;
	}

	/**
	 * One pass over the bookkeeping of a transaction.
	 */
	private static final class Reader {

		private final KnowledgeBase _kb;

		private final DefaultDBContext _context;

		/** The engine's description of its storages; built on first use. */
		private ModelTables _tables;

		private final Map<MetaObject, List<AssociationStorageDescriptor>> _descriptors = new HashMap<>();

		/** The identifiers of the items the transaction created. */
		private final Set<ObjectKey> _createdKeys = new HashSet<>();

		/** The changed rows to read as they were, too. */
		private final List<DBKnowledgeItem> _changed = new ArrayList<>();

		/** The dropped rows that store a value of an object, to read as they were. */
		private final List<DBKnowledgeItem> _dropped = new ArrayList<>();

		final List<Creation> _created = new ArrayList<>();

		/**
		 * The objects whose own rows changed, with the attributes stored there - a mutable copy, which
		 * takes the attributes stored elsewhere as well.
		 */
		final Map<TLObject, Set<TLStructuredTypePart>> _updated = new LinkedHashMap<>();

		final List<ValueWrite> _writes = new ArrayList<>();

		/** The dropped rows that only stored a value of another object: no objects of their own. */
		final Set<KnowledgeItem> _valueRows = new HashSet<>();

		Reader(KnowledgeBase kb, DefaultDBContext context) {
			_kb = kb;
			_context = context;
		}

		void read() {
			for (DBKnowledgeItem item : _context.getNewObjects()) {
				_createdKeys.add(item.tId());
			}
			for (DBKnowledgeItem item : _context.getNewObjects()) {
				TLObject object = applicationObject(item);
				if (object != null) {
					TLObject container = object.tContainer();
					_created.add(new Creation(object, asClass(object.tType()), container,
						container == null ? null : object.tContainerReference()));
				}
				addWrites(item, Version.AFTER);
			}
			for (DBKnowledgeItem item : _context.getChangedObjects()) {
				readChange(item);
			}
			for (DBKnowledgeItem item : _context.getDroppedObjects()) {
				if (!descriptors(item).isEmpty()) {
					_dropped.add(item);
				}
			}
			if (_changed.isEmpty() && _dropped.isEmpty()) {
				return;
			}
			// Read where the rows are as they were: a row deleted in this transaction still exists
			// there.
			_kb.withoutModifications(() -> {
				for (DBKnowledgeItem item : _changed) {
					addWrites(item, Version.BEFORE);
				}
				for (DBKnowledgeItem item : _dropped) {
					if (applicationObject(item) == null) {
						_valueRows.add(item);
						addWrites(item, Version.DROPPED);
					} else {
						addWrites(item, Version.DELETED);
					}
				}
				return null;
			});
		}

		/**
		 * Reads a changed item: an object whose own row changed, a row that stores a value of an
		 * object, or both - a part whose row also holds its container.
		 *
		 * <p>
		 * A column of an object's row that names no model attribute (an internal column, or the
		 * position of a part kept in its own row) yields no attribute. The object still appears in
		 * {@link ToolChangeSet#updated()} with whatever attributes did resolve, and the guard always
		 * checks the object-level right as well, so such a write is never unchecked - only checked
		 * less specifically.
		 * </p>
		 */
		private void readChange(DBKnowledgeItem item) {
			TLObject object = applicationObject(item);
			if (object != null) {
				TLStructuredType type = object.tType();
				Map<String, TLStructuredTypePart> own = tables().lookupColumnBinding(type);
				Set<String> columns = changedColumns(object, item);
				Set<TLStructuredTypePart> attributes = new LinkedHashSet<>();
				boolean storesForOthers = columns == null;
				if (columns != null) {
					for (String column : columns) {
						TLStructuredTypePart attribute = own.get(column);
						if (attribute == null) {
							// A column the engine describes as storing none of the object's own
							// attributes: it may hold the container of a part kept in its own row, or its
							// position there. Asked about by name all the same, as any column is.
							storesForOthers = true;
							attribute = type.getPart(column);
						}
						if (attribute != null) {
							attributes.add(attribute);
						}
					}
				}
				_updated.put(object, attributes);
				if (!storesForOthers) {
					// Only the object's own attributes changed: a part that was renamed did not move.
					return;
				}
			}
			if (descriptors(item).isEmpty()) {
				return;
			}
			addWrites(item, Version.AFTER);
			_changed.add(item);
		}

		/**
		 * Adds the writes the given row stands for, read as the given version, if it stores a value of
		 * an object.
		 */
		private void addWrites(DBKnowledgeItem item, Version version) {
			List<AssociationStorageDescriptor> descriptors = descriptors(item);
			if (descriptors.isEmpty()) {
				return;
			}
			try {
				Map<String, Object> row = referencedKeys(item);
				for (AssociationStorageDescriptor descriptor : descriptors) {
					addWrite(item, descriptor, row, version);
				}
			} catch (RuntimeException ex) {
				// By its identifier: the engine's own text of a row reads its values, which fails for
				// a deleted one.
				Logger.warn("Cannot resolve the value row '" + item.tId()
					+ "' stores: the pre-commit guard does not check this change.", ex, ToolChangeSet.class);
			}
		}

		private void addWrite(DBKnowledgeItem item, AssociationStorageDescriptor descriptor, Map<String, Object> row,
				Version version) {
			ObjectKey holderKey = current(descriptor.getBaseObjectId(row));
			if (holderKey == null) {
				// The row stores no value with this storage: a part that is in no container.
				return;
			}
			if (_createdKeys.contains(holderKey) || _context.isRemovedKey(holderKey)) {
				// Goes with the creation or the deletion of the object holding the attribute.
				return;
			}
			KnowledgeItem holderItem = _kb.resolveObjectKey(holderKey);
			TLObject holder = holderItem == null ? null : holderItem.getWrapper();
			if (holder == null || !(holder.tType() instanceof TLClass type)) {
				return;
			}
			TLStructuredTypePart attribute = attribute(type, descriptor.getPartId(row));
			if (attribute == null) {
				Logger.warn("Cannot resolve the attribute row '" + item.tId() + "' stores for '" + holder.tId()
					+ "': the pre-commit guard checks the object only.", ToolChangeSet.class);
			}
			boolean consequence = switch (version) {
				case AFTER -> false;
				case BEFORE -> isRemoved(attribute);
				case DROPPED -> isRemoved(attribute) || refersToRemoved(row);
				case DELETED -> true;
			};
			_writes.add(new ValueWrite(holder, attribute, consequence));
		}

		/**
		 * The engine's descriptions of the storages that keep values in the table of the given item,
		 * or in a table it derives from: a part kept in its own row may be of a type whose table
		 * derives from the one its container's storage names.
		 */
		private List<AssociationStorageDescriptor> descriptors(KnowledgeItem item) {
			return _descriptors.computeIfAbsent(item.tTable(), table -> {
				List<AssociationStorageDescriptor> result = new ArrayList<>();
				for (MetaObject current = table; current != null;
						current = current instanceof MOClass type ? type.getSuperclass() : null) {
					result.addAll(tables().getDescriptorsForTable(current).values());
				}
				return result;
			});
		}

		private ModelTables tables() {
			if (_tables == null) {
				_tables = ElementModelCacheService.getApplicationModelTables();
			}
			return _tables;
		}

		/**
		 * The attribute of the given type the given identifier names, or <code>null</code> if it
		 * names none.
		 */
		private TLStructuredTypePart attribute(TLClass type, ObjectKey id) {
			if (id == null) {
				return null;
			}
			KnowledgeItem item = _kb.resolveObjectKey(id);
			if (item == null || !(item.getWrapper() instanceof TLStructuredTypePart definition)) {
				return null;
			}
			// A row names the attribute where it was first defined, while the holder's type may
			// override it.
			TLStructuredTypePart concrete = type.getPart(definition.getName());
			return concrete != null && definition.equals(concrete.getDefinition()) ? concrete : definition;
		}

		private boolean isRemoved(TLStructuredTypePart attribute) {
			return attribute != null
				&& (_context.isRemovedKey(attribute.tId()) || _context.isRemovedKey(attribute.getDefinition().tId()));
		}

		/** Whether the given row refers to an object deleted in the transaction. */
		private boolean refersToRemoved(Map<String, Object> row) {
			for (Object key : row.values()) {
				if (key instanceof ObjectKey id && _context.isRemovedKey(current(id))) {
					return true;
				}
			}
			return false;
		}
	}

	/**
	 * The given identifier in the current revision, in which the transaction books its objects; a
	 * reference may name a historic one.
	 */
	private static ObjectKey current(ObjectKey key) {
		return key == null ? null : KBUtils.ensureHistoryContext(key, Revision.CURRENT_REV);
	}

	/**
	 * The identifiers of the objects the given row refers to, by the name of the reference: what an
	 * {@link AssociationStorageDescriptor} reads the object and the attribute from.
	 */
	private static Map<String, Object> referencedKeys(KnowledgeItem row) {
		Map<String, Object> result = new HashMap<>();
		for (MOAttribute column : row.tTable().getAttributes()) {
			if (column instanceof MOReference reference) {
				result.put(reference.getName(), row.getReferencedKey(reference));
			}
		}
		return result;
	}

	private static List<TLObject> deletions(Collection<DBKnowledgeItem> items, Set<KnowledgeItem> valueRows) {
		List<TLObject> result = new ArrayList<>(items.size());
		for (DBKnowledgeItem item : items) {
			if (valueRows.contains(item)) {
				// A write of the object holding the attribute, or the consequence of a deletion that
				// is listed in its own right.
				continue;
			}
			// Deliberately no tType() here: the item is deleted in this context, so reading its
			// values would fail. The guard resolves the type in the committed view.
			TLObject object;
			try {
				object = item.getWrapper();
			} catch (RuntimeException ex) {
				// An item whose application object cannot be resolved any more cannot be checked
				// either. Loud rather than silent: a skipped check is exactly what this layer exists
				// to prevent, so it must be visible to an operator.
				Logger.warn("Cannot resolve the deleted object of item '" + item
					+ "': the pre-commit guard does not check this deletion.", ex, ToolChangeSet.class);
				continue;
			}
			if (object != null) {
				result.add(object);
			}
		}
		return result;
	}

	/**
	 * The columns of the given object's own row whose values the transaction changed, or
	 * <code>null</code> if they cannot be read.
	 *
	 * <p>
	 * From the engine's change record, which also names the key columns of a table whose values did
	 * not change: those are no write. The values are compared and never kept - neither the audit log
	 * nor the message the model is given may quote what was written.
	 * </p>
	 */
	private static Set<String> changedColumns(TLObject object, KnowledgeItem item) {
		if (!(item instanceof ChangeInspectable inspectable)) {
			return Set.of();
		}
		ItemChange change;
		try {
			change = inspectable.getChange();
		} catch (RuntimeException ex) {
			// The change record is the engine's own commit-event machinery. Losing it costs
			// attribute granularity, not the check itself, so the turn continues.
			Logger.warn("Cannot read the pending attribute changes of '" + object
				+ "': the pre-commit guard checks this object at object level only.", ex,
				ToolChangeSet.class);
			return null;
		}
		if (change == null) {
			return Set.of();
		}
		Map<String, Object> before = change instanceof ItemUpdate update ? update.getOldValues() : null;
		Set<String> result = new LinkedHashSet<>();
		for (Map.Entry<String, Object> value : change.getValues().entrySet()) {
			if (before == null || !Objects.equals(value.getValue(), before.get(value.getKey()))) {
				result.add(value.getKey());
			}
		}
		return result;
	}

	/**
	 * The application object of the given item, or <code>null</code> if it is not one.
	 */
	private static TLObject applicationObject(KnowledgeItem item) {
		TLObject wrapper = item.getWrapper();
		if (wrapper == null) {
			return null;
		}
		return asClass(wrapper.tType()) == null ? null : wrapper;
	}

	private static TLClass asClass(TLStructuredType type) {
		return type instanceof TLClass result ? result : null;
	}
}
