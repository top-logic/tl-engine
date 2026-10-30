/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.boundsec.manager.explain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.TLID;
import com.top_logic.basic.col.CloseableIterator;
import com.top_logic.element.boundsec.manager.ElementAccessManager;
import com.top_logic.element.boundsec.manager.StorageAccessManager;
import com.top_logic.element.boundsec.manager.rule.RoleProvider;
import com.top_logic.element.boundsec.manager.rule.RoleRule;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.security.SecurityStorage;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.StorageException;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.security.ModelAccessRights;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundHelper;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.BoundRole;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.tool.boundsec.wrap.Group;
import com.top_logic.util.TLContextManager;

/**
 * Analysis of the effective access rights of an account: which operations it may perform on an
 * object, through which roles, and where these roles come from.
 *
 * <p>
 * The analysis computes no decision and no role itself. The decisions are made by the access check
 * of the {@link ModelAccessRights} for the account, the roles the account holds are reported by the
 * {@link AccessManager}. The sources of the roles (direct role assignment, role rule, role parent,
 * and the group they reach the account through) are the entries of the {@link SecurityStorage} of a
 * {@link StorageAccessManager}. Without such a storage, the decisions and the held roles are
 * reported without sources.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class EffectiveAccess {

	private final ModelAccessRights _rights;

	private final AccessManager _accessManager;

	/**
	 * Creates an {@link EffectiveAccess} analysis.
	 *
	 * @param rights
	 *        The access check deciding the operations.
	 * @param accessManager
	 *        The access manager reporting the roles.
	 */
	public EffectiveAccess(ModelAccessRights rights, AccessManager accessManager) {
		_rights = rights;
		_accessManager = accessManager;
	}

	/**
	 * The analysis of the running access services.
	 */
	public static EffectiveAccess getInstance() {
		return new EffectiveAccess(ModelAccessRights.getInstance(), AccessManager.getInstance());
	}

	/**
	 * Whether the sources of the roles can be determined, i.e. the access manager keeps a
	 * {@link SecurityStorage} that is not being rebuilt.
	 */
	public boolean isSourcesAvailable() {
		return storage() != null;
	}

	/**
	 * Explains the access the given account has to the given object.
	 *
	 * @param person
	 *        The account to explain the access of.
	 * @param object
	 *        The object to explain the access to.
	 * @return The decision for every operation that applies to the object, and the roles the
	 *         account holds on the object deciding with their sources.
	 */
	public AccessExplanation explain(Person person, TLObject object) {
		TLClass type = object.tType() instanceof TLClass clazz ? clazz : null;
		boolean withoutSecurity = type != null && _rights.isWithoutSecurity(type);
		boolean internal = type != null && _rights.isInternal(type);

		TLObject decidingObject = _rights.getRoleHolder(object);
		List<BoundObject> roleParents = new ArrayList<>();
		Set<BoundedRole> held = new HashSet<>();
		if (decidingObject instanceof BoundObject holder) {
			BoundHelper.collectAllSecurityParents(holder, roleParents::add);
			for (BoundRole role : _accessManager.getRoles(person, holder)) {
				if (role instanceof BoundedRole bounded) {
					held.add(bounded);
				}
			}
		}

		List<OperationDecision> operations = decide(person, object, held);

		SecurityStorage storage = storage();
		List<HeldRole> heldRoles;
		if (storage != null && decidingObject instanceof BoundObject holder) {
			heldRoles = heldRoles(held, sources(storage, person, holder, roleParents));
		} else {
			heldRoles = heldRoles(held, Collections.emptyMap());
		}

		boolean restrictedUser = person != null && person.isRestrictedUser().booleanValue();
		return new AccessExplanation(person, object, Person.isAdmin(person), restrictedUser, withoutSecurity, internal,
			decidingObject, Collections.unmodifiableList(roleParents), operations, heldRoles, storage != null);
	}

	/**
	 * All roles the given account holds on any object, with their sources.
	 *
	 * <p>
	 * A role inherited from a role parent is reported once, on the role parent: the entries are
	 * those of the {@link SecurityStorage}, which keeps an inherited role on the object it is
	 * assigned on.
	 * </p>
	 *
	 * @param person
	 *        The account to report the roles of.
	 * @param roles
	 *        The roles to report, <code>null</code> or empty for all roles.
	 * @return The entries of the account. Empty when the {@link #isSourcesAvailable() sources are
	 *         not available}.
	 */
	public List<RoleAssignment> assignments(Person person, Collection<BoundedRole> roles) {
		SecurityStorage storage = storage();
		if (storage == null) {
			return Collections.emptyList();
		}
		Map<TLID, Group> groups = groups(person);
		Collection<TLID> roleIDs = roles == null || roles.isEmpty() ? null : ids(roles);
		List<SecurityStorage.StorageEntry> entries = entries(storage, groups.keySet(), null, roleIDs);

		ObjectResolver resolver = new ObjectResolver(groups);
		List<RoleAssignment> result = new ArrayList<>(entries.size());
		for (SecurityStorage.StorageEntry entry : entries) {
			RoleProvider rule = rule(entry);
			result.add(assignment(entry, resolver.resolve(entry, rule), groups, rule));
		}
		result.sort(ASSIGNMENT_ORDER);
		return result;
	}

	private List<OperationDecision> decide(Person person, TLObject object, Set<BoundedRole> held) {
		Collection<BoundCommandGroup> applicable = _rights.getObjectOperations(object).getOperations();

		// The decision is made in the context of the account, so that the bypasses of the access
		// check (administrator, restricted account) apply to the account, not to the one asking.
		Map<BoundCommandGroup, Boolean> allowed = new HashMap<>();
		Set<BoundCommandGroup> restricted = new HashSet<>();
		TLContextManager.<Void, RuntimeException, RuntimeException> inPersonContext(person, () -> {
			for (BoundCommandGroup operation : applicable) {
				allowed.put(operation, Boolean.valueOf(_rights.isAllowed(person, object, operation)));
				if (person != null && !SimpleBoundCommandGroup.isAllowedCommandGroup(person, operation)) {
					restricted.add(operation);
				}
			}
			return null;
		});

		List<OperationDecision> result = new ArrayList<>(applicable.size());
		for (BoundCommandGroup operation : applicable) {
			Set<BoundedRole> admitting = _rights.getAdmittingRoles(object, operation);
			Set<BoundedRole> granting = new HashSet<>(admitting);
			granting.retainAll(held);
			result.add(new OperationDecision(operation, _rights.getOperationOnRoleHolder(object, operation),
				allowed.get(operation).booleanValue(),
				Collections.unmodifiableSet(admitting), Collections.unmodifiableSet(granting),
				restricted.contains(operation)));
		}
		return Collections.unmodifiableList(result);
	}

	private static List<HeldRole> heldRoles(Set<BoundedRole> held, Map<BoundedRole, List<RoleAssignment>> sources) {
		List<HeldRole> result = new ArrayList<>(held.size());
		for (BoundedRole role : held) {
			List<RoleAssignment> roleSources = sources.getOrDefault(role, Collections.emptyList());
			roleSources.sort(ASSIGNMENT_ORDER);
			result.add(new HeldRole(role, Collections.unmodifiableList(roleSources)));
		}
		result.sort(Comparator.comparing(heldRole -> heldRole.role().getName()));
		return Collections.unmodifiableList(result);
	}

	private Map<BoundedRole, List<RoleAssignment>> sources(SecurityStorage storage, Person person,
			BoundObject holder, List<BoundObject> roleParents) {
		Map<TLID, TLObject> objects = new HashMap<>();
		objects.put(holder.getID(), holder);
		for (BoundObject parent : roleParents) {
			objects.put(parent.getID(), parent);
		}
		Map<TLID, Group> groups = groups(person);

		Map<BoundedRole, List<RoleAssignment>> result = new HashMap<>();
		for (SecurityStorage.StorageEntry entry : entries(storage, groups.keySet(), objects.keySet(), null)) {
			RoleAssignment assignment = assignment(entry, objects.get(entry.businessObject()), groups, rule(entry));
			if (assignment.role() != null) {
				result.computeIfAbsent(assignment.role(), x -> new ArrayList<>()).add(assignment);
			}
		}
		return result;
	}

	private static RoleAssignment assignment(SecurityStorage.StorageEntry entry, TLObject object, Map<TLID, Group> groups,
			RoleProvider rule) {
		RoleSource source;
		if (entry.isDirectAssignment()) {
			source = RoleSource.DIRECT;
		} else if (rule != null) {
			source = RoleSource.RULE;
		} else {
			source = RoleSource.UNKNOWN;
		}
		return new RoleAssignment(BoundedRole.getInstance(entry.role()), object, groups.get(entry.group()), source,
			rule);
	}

	private RoleProvider rule(SecurityStorage.StorageEntry entry) {
		if (entry.isDirectAssignment()) {
			return null;
		}
		return elementAccessManager().getRulefromPersitancyId(Integer.valueOf(entry.reason()));
	}

	private static List<SecurityStorage.StorageEntry> entries(SecurityStorage storage, Collection<TLID> groupIDs,
			Collection<TLID> objectIDs, Collection<TLID> roleIDs) {
		try {
			return storage.getEntries(groupIDs, objectIDs, roleIDs);
		} catch (StorageException ex) {
			throw new RuntimeException("Unable to read the security storage.", ex);
		}
	}

	/**
	 * The groups the roles of the given account are looked up for, by their identifiers.
	 */
	private Map<TLID, Group> groups(Person person) {
		Map<TLID, Group> result = new LinkedHashMap<>();
		for (Group group : elementAccessManager().getGroups(person)) {
			result.put(KBUtils.getWrappedObjectName(group), group);
		}
		return result;
	}

	private static Collection<TLID> ids(Collection<BoundedRole> roles) {
		List<TLID> result = new ArrayList<>(roles.size());
		for (BoundedRole role : roles) {
			result.add(role.getID());
		}
		return result;
	}

	/**
	 * The storage of the access manager, <code>null</code> when it has none or the storage is being
	 * rebuilt.
	 */
	private SecurityStorage storage() {
		if (!(_accessManager instanceof StorageAccessManager storageManager)) {
			return null;
		}
		SecurityStorage storage = storageManager.getSecurityStorage();
		if (storage == null || storage.isRebuilding()) {
			return null;
		}
		return storage;
	}

	private ElementAccessManager elementAccessManager() {
		return (ElementAccessManager) _accessManager;
	}

	/**
	 * Order of the entries: by role, then by group.
	 */
	private static final Comparator<RoleAssignment> ASSIGNMENT_ORDER = Comparator
		.comparing((RoleAssignment assignment) -> name(assignment.role()))
		.thenComparing(assignment -> assignment.group() == null ? "" : assignment.group().getName());

	private static String name(BoundedRole role) {
		return role == null ? "" : role.getName();
	}

	/**
	 * Resolves the objects of the entries of the {@link SecurityStorage}, which knows them by their
	 * identifiers only.
	 *
	 * <p>
	 * The object of a rule's entry is of the type the rule applies to. The object of a direct role
	 * assignment is found through the role assignments of the group.
	 * </p>
	 */
	private static final class ObjectResolver {

		private final Map<TLID, Group> _groups;

		private final Map<TLID, Map<TLID, TLObject>> _assignedByGroup = new HashMap<>();

		private final Map<RoleProvider, String[]> _tablesByRule = new HashMap<>();

		private final Map<TLID, TLObject> _resolved = new HashMap<>();

		private final KnowledgeBase _kb = PersistencyLayer.getKnowledgeBase();

		ObjectResolver(Map<TLID, Group> groups) {
			_groups = groups;
		}

		TLObject resolve(SecurityStorage.StorageEntry entry, RoleProvider rule) {
			TLID id = entry.businessObject();
			TLObject known = _resolved.get(id);
			if (known != null) {
				return known;
			}
			TLObject result;
			if (entry.isDirectAssignment()) {
				result = assignedObjects(entry.group()).get(id);
			} else if (rule instanceof RoleRule roleRule) {
				result = WrapperFactory.getWrapper(id, tables(roleRule), _kb);
			} else {
				result = null;
			}
			if (result != null) {
				_resolved.put(id, result);
			}
			return result;
		}

		private String[] tables(RoleRule rule) {
			return _tablesByRule.computeIfAbsent(rule,
				x -> TLModelUtil.potentialTables(rule.getMetaElement(), false).keySet().toArray(new String[0]));
		}

		private Map<TLID, TLObject> assignedObjects(TLID groupID) {
			return _assignedByGroup.computeIfAbsent(groupID, x -> {
				Map<TLID, TLObject> result = new HashMap<>();
				Group group = _groups.get(groupID);
				if (group == null) {
					return result;
				}
				try (CloseableIterator<KnowledgeObject> it = BoundedRole.roleAssignmentsForGroup(group.tHandle())) {
					while (it.hasNext()) {
						Object context = it.next().getAttributeValue(BoundedRole.ATTRIBUTE_OBJECT);
						if (context instanceof KnowledgeItem item) {
							TLObject object = item.getWrapper();
							result.put(KBUtils.getWrappedObjectName(object), object);
						}
					}
				}
				return result;
			});
		}
	}

}
