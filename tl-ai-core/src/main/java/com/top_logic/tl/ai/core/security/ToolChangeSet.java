/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.Logger;
import com.top_logic.element.meta.kbbased.WrapperMetaAttributeUtil;
import com.top_logic.knowledge.event.ItemChange;
import com.top_logic.knowledge.objects.ChangeInspectable;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.KnowledgeBase;
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
 *        Objects modified during the turn, mapped to the attributes that were touched. An object
 *        <em>created</em> in the same transaction never appears here - the engine books its
 *        attribute writes as part of the creation - which is the concrete form the
 *        {@link PreCommitGuard known limit} takes.
 * @param deleted
 *        Objects deleted during the turn.
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
	 * A created item that carries no application type - the link of a reference kept in a link
	 * table, or one of the engine's own bookkeeping items - is not returned: it has no
	 * {@link TLClass} to check a right against. A dropped link is returned among the deletions, where
	 * the engine grants the DELETE the guard asks, because it checks no rights on an item that is no
	 * bound object. Neither is booked as a change of the object that holds the reference, so a change
	 * of such a reference - an enumeration value, for one - is not checked by the guard at all
	 * (Gitea #631). {@link #pendingLinks(KnowledgeBase)} reads those changes for a report.
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

		List<Creation> created = creations(context.getNewObjects());
		Map<TLObject, Set<TLStructuredTypePart>> updated = updates(context.getChangedObjects());
		List<TLObject> deleted = deletions(context.getDroppedObjects());
		if (created.isEmpty() && updated.isEmpty() && deleted.isEmpty()) {
			return EMPTY;
		}
		return new ToolChangeSet(created, updated, deleted);
	}

	/**
	 * The attributes the current transaction changed whose values are stored as links - a reference
	 * kept in a link table rather than in the object's own row - by the object that holds them.
	 *
	 * <p>
	 * Such a change is invisible to {@link #pending(KnowledgeBase)}: the link item has no application
	 * type, and the object holding the reference is not booked as changed when only its link is. A
	 * report of what a tool did needs them - setting an enumeration value is such a change - so they
	 * are read here, separately. {@link #pending(KnowledgeBase)} and with it the
	 * {@link PreCommitGuard} stay as they are: counting the holder of every removed link as changed
	 * would ask for a WRITE right on every object that merely referred to a deleted one.
	 * </p>
	 *
	 * @param kb
	 *        The knowledge base whose current transaction to read.
	 * @return The changed attributes by the object they belong to; empty without a transaction.
	 */
	public static Map<TLObject, Set<TLStructuredTypePart>> pendingLinks(KnowledgeBase kb) {
		if (!(kb instanceof DBKnowledgeBase db) || !(db.getCurrentDBContext() instanceof DefaultDBContext context)) {
			return Map.of();
		}
		Map<TLObject, Set<TLStructuredTypePart>> result = new LinkedHashMap<>();
		for (DBKnowledgeItem item : context.getNewObjects()) {
			addLink(result, item);
		}
		for (DBKnowledgeItem item : context.getDroppedObjects()) {
			// A link deleted in this transaction is read where it still exists.
			kb.withoutModifications(() -> {
				addLink(result, item);
				return null;
			});
		}
		return result;
	}

	private static void addLink(Map<TLObject, Set<TLStructuredTypePart>> result, DBKnowledgeItem item) {
		if (!(item instanceof KnowledgeAssociation link)
			|| item.tTable().getAttributeOrNull(WrapperMetaAttributeUtil.META_ATTRIBUTE_ATTR) == null) {
			return;
		}
		TLStructuredTypePart attribute;
		TLObject holder;
		try {
			attribute = WrapperMetaAttributeUtil.getMetaAttribute(item);
			holder = link.getSourceObject().getWrapper();
		} catch (RuntimeException ex) {
			Logger.warn("Cannot resolve the attribute of link '" + item + "'.", ex, ToolChangeSet.class);
			return;
		}
		if (attribute == null || holder == null || holder.tType() == null) {
			return;
		}
		// The link names the attribute where it was first defined; the holder's type may override it.
		TLStructuredTypePart concrete = holder.tType().getPart(attribute.getName());
		result.computeIfAbsent(holder, key -> new LinkedHashSet<>()).add(concrete != null ? concrete : attribute);
	}

	private static List<Creation> creations(Collection<DBKnowledgeItem> items) {
		List<Creation> result = new ArrayList<>(items.size());
		for (DBKnowledgeItem item : items) {
			TLObject object = applicationObject(item);
			if (object == null) {
				continue;
			}
			TLObject container = object.tContainer();
			result.add(new Creation(object, asClass(object.tType()), container,
				container == null ? null : object.tContainerReference()));
		}
		return result;
	}

	private static Map<TLObject, Set<TLStructuredTypePart>> updates(Collection<DBKnowledgeItem> items) {
		Map<TLObject, Set<TLStructuredTypePart>> result = new LinkedHashMap<>();
		for (DBKnowledgeItem item : items) {
			TLObject object = applicationObject(item);
			if (object == null) {
				continue;
			}
			result.put(object, touchedAttributes(object, item));
		}
		return result;
	}

	private static List<TLObject> deletions(Collection<DBKnowledgeItem> items) {
		List<TLObject> result = new ArrayList<>(items.size());
		for (DBKnowledgeItem item : items) {
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
	 * The model attributes the given item's pending change touched.
	 *
	 * <p>
	 * The names come from the engine's change record; only the names are read, never the values -
	 * neither the audit log nor the message the model is given may quote what was written.
	 * </p>
	 *
	 * <p>
	 * A storage-level attribute that maps to no model attribute (an internal column, or a reference
	 * whose storage spreads over several columns) yields no entry. The object still appears in
	 * {@link #updated()} with whatever attributes did resolve, and the guard always checks the
	 * object-level right as well, so such a write is never unchecked - only checked less
	 * specifically.
	 * </p>
	 */
	private static Set<TLStructuredTypePart> touchedAttributes(TLObject object, KnowledgeItem item) {
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
			return Set.of();
		}
		if (change == null) {
			return Set.of();
		}
		TLStructuredType type = object.tType();
		if (type == null) {
			return Set.of();
		}
		Set<TLStructuredTypePart> result = new LinkedHashSet<>();
		for (String attribute : change.getValues().keySet()) {
			TLStructuredTypePart part = type.getPart(attribute);
			if (part != null) {
				result.add(part);
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
