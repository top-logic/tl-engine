/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.wrap;

import static com.top_logic.knowledge.search.ExpressionFactory.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.InteractionContext;
import com.top_logic.basic.col.CloseableIterator;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.util.MetaObjectUtils;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.search.RangeParam;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;
import com.top_logic.knowledge.service.db2.SimpleQuery;
import com.top_logic.model.TLObject;
import com.top_logic.tool.boundsec.BoundRole;

/**
 * Memo of the role assignments of groups, used during one interaction.
 * <p>
 * Without the memo, the roles of a person on an object are looked up with one query per group of
 * the person, for the object and again for each of its role parents. A check over many objects
 * therefore issues many queries, although the groups of a person typically hold few role
 * assignments, and role parents (e.g. a security root) are shared by many objects.
 * </p>
 * <p>
 * The memo loads all role assignments of a group with a single query, the first time the group is
 * asked for. Afterwards, the roles of the group on any object are answered from the memo. A group
 * holding more than {@link #MAX_LOADED_ASSIGNMENTS} assignments is not loaded completely: its
 * roles are looked up per object as before, and only the answers are stored, so that a role parent
 * is still asked only once.
 * </p>
 * <p>
 * The memo is discarded together with the interaction, and it is replaced as soon as the knowledge
 * base has a newer revision than the one it was created for. It is not used while the current
 * thread has uncommitted changes, since a role assignment or group membership changed in the
 * current transaction is not part of the committed state the memo is loaded from.
 * </p>
 *
 * @see BoundedRole#getLocalAndGlobalAndGroupRoles(TLObject, com.top_logic.knowledge.wrap.person.Person)
 */
final class RoleAssignmentMemo {

	/**
	 * The maximum number of role assignments of a group that are loaded at once.
	 */
	static final int MAX_LOADED_ASSIGNMENTS = 1000;

	private static final Property<RoleAssignmentMemo> PROPERTY =
		TypedAnnotatable.property(RoleAssignmentMemo.class, "roleAssignments");

	private final KnowledgeBase _kb;

	private final long _revision;

	private final Map<ObjectKey, GroupAssignments> _assignmentsByGroup = new HashMap<>();

	private RoleAssignmentMemo(KnowledgeBase kb, long revision) {
		_kb = kb;
		_revision = revision;
	}

	/**
	 * The memo of the current interaction for the current revision of the given knowledge base.
	 *
	 * @return <code>null</code> when there is no interaction, or when the current thread has
	 *         uncommitted changes in the given knowledge base. Role assignments must then be looked
	 *         up directly.
	 */
	static RoleAssignmentMemo current(KnowledgeBase kb) {
		InteractionContext interaction = ThreadContextManager.getInteraction();
		if (interaction == null) {
			return null;
		}
		if (!(kb instanceof DBKnowledgeBase dbKB) || dbKB.hasUncommittedChanges()) {
			return null;
		}
		long revision = kb.getHistoryManager().getLastRevision();
		RoleAssignmentMemo memo = interaction.get(PROPERTY);
		if (memo == null || memo._kb != kb || memo._revision != revision) {
			memo = new RoleAssignmentMemo(kb, revision);
			interaction.set(PROPERTY, memo);
		}
		return memo;
	}

	/**
	 * Whether the memo answers for the given object.
	 * <p>
	 * The memo holds the current role assignments of objects of its knowledge base. A historic
	 * object is not answered from it.
	 * </p>
	 */
	boolean covers(TLObject object) {
		KnowledgeItem handle = object.tHandle();
		return handle != null && handle.getKnowledgeBase() == _kb
			&& handle.getHistoryContext() == Revision.CURRENT_REV;
	}

	/**
	 * Adds the roles the given group holds on the given object to the given result.
	 *
	 * @param result
	 *        The roles found so far.
	 * @param context
	 *        The object to look up the roles on, must be {@link #covers(TLObject) covered}.
	 * @param group
	 *        The group holding the roles.
	 */
	void addRoles(Collection<BoundRole> result, TLObject context, Group group) {
		GroupAssignments assignments = _assignmentsByGroup.get(group.tId());
		if (assignments == null) {
			assignments = load(group);
			_assignmentsByGroup.put(group.tId(), assignments);
		}
		assignments.addRoles(result, context, group);
	}

	private GroupAssignments load(Group group) {
		MetaObject roleAssignmentType =
			_kb.getMORepository().getMetaObject(BoundedRole.ROLE_ASSIGNMENT_OBJECT_NAME);
		MOReference contextAttr = MetaObjectUtils.getReference(roleAssignmentType, BoundedRole.ATTRIBUTE_OBJECT);
		MOReference ownerAttr = MetaObjectUtils.getReference(roleAssignmentType, BoundedRole.ATTRIBUTE_OWNER);

		SimpleQuery<KnowledgeObject> query = SimpleQuery.queryUnresolved(KnowledgeObject.class, roleAssignmentType,
			eqBinary(reference(ownerAttr), literal(group.tHandle())), RangeParam.head);

		Map<ObjectKey, Set<BoundRole>> rolesByContext = new HashMap<>();
		int count = 0;
		try (CloseableIterator<KnowledgeObject> it =
			_kb.compileSimpleQuery(query).searchStream(revisionArgs().setStopRow(MAX_LOADED_ASSIGNMENTS + 1))) {
			while (it.hasNext()) {
				if (++count > MAX_LOADED_ASSIGNMENTS) {
					// Too many to keep: look up per object.
					return new GroupAssignments(new HashMap<>(), false);
				}
				KnowledgeObject assignment = it.next();
				ObjectKey context = assignment.getReferencedKey(contextAttr);
				BoundRole role =
					((KnowledgeItem) assignment.getAttributeValue(BoundedRole.ATTRIBUTE_ROLE)).getWrapper();
				rolesByContext.computeIfAbsent(context, key -> new HashSet<>()).add(role);
			}
		}
		return new GroupAssignments(rolesByContext, true);
	}

	/**
	 * The role assignments of a single group.
	 */
	private static final class GroupAssignments {

		private final Map<ObjectKey, Set<BoundRole>> _rolesByContext;

		private final boolean _complete;

		/**
		 * Creates a {@link GroupAssignments}.
		 *
		 * @param rolesByContext
		 *        The roles of the group by the object they are assigned on.
		 * @param complete
		 *        Whether the given roles are all roles of the group. Otherwise, the roles on an
		 *        object not contained are looked up and stored on demand.
		 */
		GroupAssignments(Map<ObjectKey, Set<BoundRole>> rolesByContext, boolean complete) {
			_rolesByContext = rolesByContext;
			_complete = complete;
		}

		void addRoles(Collection<BoundRole> result, TLObject context, Group group) {
			ObjectKey key = context.tId();
			Set<BoundRole> roles = _rolesByContext.get(key);
			if (roles == null) {
				if (_complete) {
					return;
				}
				roles = new HashSet<>();
				BoundedRole.addLocalRoles(roles, context, group);
				_rolesByContext.put(key, roles);
			}
			result.addAll(roles);
		}
	}

}
