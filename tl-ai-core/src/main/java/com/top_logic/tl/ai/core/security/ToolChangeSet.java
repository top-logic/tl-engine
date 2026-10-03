/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.Logger;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.element.meta.AssociationStorage;
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
 *        Objects modified during the turn, mapped to the attributes that were touched - a reference
 *        whose values are stored as links included, on the object holding it. An object
 *        <em>created</em> in the same transaction never appears here - the engine books its
 *        attribute writes as part of the creation - which is the concrete form the
 *        {@link PreCommitGuard known limit} takes.
 * @param deleted
 *        Objects deleted during the turn. The link of a reference is no object of its own and is
 *        never listed here.
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
	 * <b>A reference whose values are stored as links</b> - in a table of their own rather than in
	 * the row of the object holding the reference, which is where the engine stores every reference
	 * a model does not configure into a column, an enumeration value among them - books no change on
	 * the object holding it: the engine creates, reorders and drops link items instead, which carry
	 * no application type. Each of them is read here as what it is, a write of the reference it
	 * stores, and booked in {@link #updated()} on the object holding it (Gitea #631). Left out are
	 * the links that go with the creation or the deletion of an object, which the guard checks as
	 * that: the links of an object created or deleted in the transaction, and a link dropped together
	 * with the object it pointed to - else deleting an object would ask for the right to write every
	 * object that referred to it.
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

		List<LinkWrite> links = linkWrites(kb, context);
		List<Creation> created = creations(context.getNewObjects());
		Map<TLObject, Set<TLStructuredTypePart>> updated = updates(context.getChangedObjects());
		for (LinkWrite link : links) {
			if (link.consequence()) {
				continue;
			}
			Set<TLStructuredTypePart> attributes =
				updated.computeIfAbsent(link.holder(), key -> new LinkedHashSet<>());
			if (link.attribute() != null) {
				attributes.add(link.attribute());
			}
		}
		List<TLObject> deleted = deletions(context.getDroppedObjects(), links);
		if (created.isEmpty() && updated.isEmpty() && deleted.isEmpty()) {
			return EMPTY;
		}
		return new ToolChangeSet(created, updated, deleted);
	}

	/**
	 * The references whose values are stored as links that the current transaction changed, by the
	 * object holding them - those that went with the creation or the deletion of an object included.
	 *
	 * <p>
	 * For a report of what a tool did. {@link #pending(KnowledgeBase)} leaves a link that went with a
	 * creation or a deletion to the guard's check of that, but a report shows its effect: deleting an
	 * object clears every reference to it, and that is a change of each object that referred to it.
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
		for (LinkWrite link : linkWrites(kb, context)) {
			if (link.attribute() != null) {
				result.computeIfAbsent(link.holder(), key -> new LinkedHashSet<>()).add(link.attribute());
			}
		}
		return result;
	}

	/**
	 * A link of a reference stored as links that the transaction created, changed or dropped: a write
	 * of {@link #attribute()} of {@link #holder()}.
	 *
	 * @param link
	 *        The link item.
	 * @param holder
	 *        The object holding the reference, at the source end of the link.
	 * @param attribute
	 *        The reference as the holder's type has it, or <code>null</code> if the link names one
	 *        that does not resolve; then only the right to write the holder can be asked.
	 * @param consequence
	 *        Whether the link went with the creation or the deletion of an object, which the guard
	 *        checks as that: the holder was created or deleted in the transaction, or the link was
	 *        dropped together with the object it pointed to or the reference it stored.
	 */
	private record LinkWrite(KnowledgeItem link, TLObject holder, TLStructuredTypePart attribute,
			boolean consequence) {
		// Intentionally empty.
	}

	/**
	 * The links of a reference stored as links that the transaction created, changed or dropped.
	 */
	private static List<LinkWrite> linkWrites(KnowledgeBase kb, DefaultDBContext context) {
		List<LinkWrite> result = new ArrayList<>();
		for (DBKnowledgeItem item : context.getNewObjects()) {
			addLinkWrite(result, context, item, false);
		}
		List<DBKnowledgeItem> before = new ArrayList<>();
		for (DBKnowledgeItem item : context.getChangedObjects()) {
			if (item instanceof KnowledgeAssociation) {
				// Reordering a list changes its links in place. Read after the change and, as dropped,
				// before it, in case a link was moved from one object to another.
				addLinkWrite(result, context, item, false);
				before.add(item);
			}
		}
		for (DBKnowledgeItem item : context.getDroppedObjects()) {
			if (item instanceof KnowledgeAssociation) {
				before.add(item);
			}
		}
		if (!before.isEmpty()) {
			// Read where the links are as they were: a link deleted in this transaction still exists
			// there.
			kb.withoutModifications(() -> {
				for (DBKnowledgeItem item : before) {
					addLinkWrite(result, context, item, true);
				}
				return null;
			});
		}
		return result;
	}

	/**
	 * Adds the write the given item stands for, if it is the link of a reference stored as links.
	 *
	 * @param dropped
	 *        Whether the item is read as it was before the transaction, because it is dropped or
	 *        changed in it.
	 */
	private static void addLinkWrite(List<LinkWrite> result, DefaultDBContext context, DBKnowledgeItem item,
			boolean dropped) {
		if (!(item instanceof KnowledgeAssociation link) || applicationObject(item) != null) {
			// Not a link, or one the application models as an object of its own (a role assignment),
			// which is checked as that object.
			return;
		}
		TLObject holder;
		TLStructuredTypePart attribute;
		try {
			KnowledgeItem source = link.getSourceObject();
			holder = source == null ? null : source.getWrapper();
			if (holder == null || !(holder.tType() instanceof TLClass type)) {
				return;
			}
			attribute = storedAttribute(type, item);
		} catch (RuntimeException ex) {
			// By its identifier: the engine's own text of a link reads its ends, which fails for a
			// deleted one.
			Logger.warn("Cannot resolve the reference link '" + item.tId()
				+ "' stores: the pre-commit guard does not check this change.", ex, ToolChangeSet.class);
			return;
		}
		if (attribute == null) {
			if (item.tTable().getAttributeOrNull(WrapperMetaAttributeUtil.META_ATTRIBUTE_ATTR) == null) {
				// A link of some other kind: it stores no attribute value.
				return;
			}
			Logger.warn("Cannot resolve the attribute link '" + item.tId() + "' stores for '" + holder.tId()
				+ "': the pre-commit guard checks the object only.", ToolChangeSet.class);
		}
		boolean consequence = holder.tHandle().getState() == KnowledgeItem.State.NEW
			|| context.isRemovedKey(holder.tId())
			|| (dropped && (context.isRemovedKey(link.getDestinationIdentity()) || isRemoved(context, attribute)));
		result.add(new LinkWrite(item, holder, attribute, consequence));
	}

	/**
	 * The attribute of the given type whose value the given link stores, or <code>null</code> if it
	 * stores none.
	 */
	private static TLStructuredTypePart storedAttribute(TLClass type, KnowledgeItem link) {
		MOStructure table = link.tTable();
		if (table.getAttributeOrNull(WrapperMetaAttributeUtil.META_ATTRIBUTE_ATTR) != null) {
			// A table holding the links of many references names the one per link - where it was
			// first defined, while the holder's type may override it.
			TLStructuredTypePart definition = WrapperMetaAttributeUtil.getMetaAttribute(link);
			if (definition == null) {
				return null;
			}
			TLStructuredTypePart concrete = type.getPart(definition.getName());
			return concrete != null && definition.equals(concrete.getDefinition()) ? concrete : definition;
		}
		// A table of one reference's own, which is how the engine stores the members of a group.
		for (TLStructuredTypePart part : type.getAllParts()) {
			if (part.getStorageImplementation() instanceof AssociationStorage storage && storage.monomophicTable()
				&& table.isSubtypeOf(storage.getTable())) {
				return part;
			}
		}
		return null;
	}

	private static boolean isRemoved(DefaultDBContext context, TLStructuredTypePart attribute) {
		return attribute != null
			&& (context.isRemovedKey(attribute.tId()) || context.isRemovedKey(attribute.getDefinition().tId()));
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
			// A copy that can take the references stored as links as well.
			result.put(object, new LinkedHashSet<>(touchedAttributes(object, item)));
		}
		return result;
	}

	private static List<TLObject> deletions(Collection<DBKnowledgeItem> items, List<LinkWrite> links) {
		Set<KnowledgeItem> linkItems = new HashSet<>();
		for (LinkWrite link : links) {
			linkItems.add(link.link());
		}
		List<TLObject> result = new ArrayList<>(items.size());
		for (DBKnowledgeItem item : items) {
			if (linkItems.contains(item)) {
				// A write of the object holding the reference, or the consequence of a deletion that
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
