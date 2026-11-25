/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.boundsec.manager;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.element.boundsec.manager.rule.GroupWithName;
import com.top_logic.element.boundsec.manager.rule.NavigationRule;
import com.top_logic.element.boundsec.manager.rule.PathElement;
import com.top_logic.element.boundsec.manager.rule.PathNavigation;
import com.top_logic.element.boundsec.manager.rule.RoleProvider;
import com.top_logic.element.boundsec.manager.rule.RoleProvider.Type;
import com.top_logic.element.boundsec.manager.rule.RoleRule;
import com.top_logic.element.boundsec.manager.rule.SingletonPathElement;
import com.top_logic.element.boundsec.manager.rule.config.HistoryMode;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLType;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.BoundRole;
import com.top_logic.tool.boundsec.wrap.Group;

/**
 * Properties of the role rules and role parent rules that are derived from the configuration once
 * the rules are loaded.
 *
 * <p>
 * The analysis determines the rules whose results are not kept in the security storage, and the
 * roles a person may hold on a historic version of an object without holding them on a current
 * object.
 * </p>
 */
public final class RuleAnalysis {

	/** {@link RuleAnalysis} of a configuration without rules. */
	public static final RuleAnalysis EMPTY =
		new RuleAnalysis(Collections.emptyMap(), Collections.emptyMap());

	private final Map<TLClass, Collection<RoleProvider>> _rules;

	private final Set<RoleProvider> _notStored = new HashSet<>();

	/**
	 * For a rule that is not stored, because it inherits from a rule not stored, that other rule.
	 */
	private final Map<RoleProvider, RoleProvider> _notStoredCause = new HashMap<>();

	private final Map<TLClass, Set<BoundRole>> _notStoredRoles = new HashMap<>();

	private final Set<TLClass> _typesWithAnchoredParents = new HashSet<>();

	private final Set<BoundRole> _rolesBeyondStorage = new HashSet<>();

	private boolean _historicRoleParents;

	/**
	 * Creates a {@link RuleAnalysis}.
	 *
	 * @param rules
	 *        The role rules that apply to the objects of each concrete type.
	 * @param roleParents
	 *        The role parent rules that apply to the objects of each concrete type.
	 */
	public RuleAnalysis(Map<TLClass, Collection<RoleProvider>> rules,
			Map<TLClass, Collection<NavigationRule>> roleParents) {
		_rules = rules;
		computeNotStored();
		computeRolesBeyondStorage();
		for (Map.Entry<TLClass, Collection<NavigationRule>> entry : roleParents.entrySet()) {
			for (NavigationRule rule : entry.getValue()) {
				if (rule.getHistory().hasHistoricForwardStep()) {
					_historicRoleParents = true;
				}
				if (rule.getHistory().getMode() == HistoryMode.ANCHORED) {
					_typesWithAnchoredParents.add(entry.getKey());
				}
			}
		}
	}

	private void computeNotStored() {
		for (Collection<RoleProvider> rules : _rules.values()) {
			for (RoleProvider rule : rules) {
				if (rule instanceof RoleRule roleRule && roleRule.getHistory().hasHistoricForwardStep()) {
					_notStored.add(rule);
				}
			}
		}
		boolean changed = true;
		while (changed) {
			changed = false;
			for (Collection<RoleProvider> rules : _rules.values()) {
				for (RoleProvider rule : rules) {
					if (_notStored.contains(rule) || rule.getType() != Type.inheritance) {
						continue;
					}
					RoleProvider cause = notStoredSource((RoleRule) rule);
					if (cause != null) {
						_notStored.add(rule);
						_notStoredCause.put(rule, cause);
						changed = true;
					}
				}
			}
		}
		for (Map.Entry<TLClass, Collection<RoleProvider>> entry : _rules.entrySet()) {
			for (RoleProvider rule : entry.getValue()) {
				if (_notStored.contains(rule)) {
					_notStoredRoles.computeIfAbsent(entry.getKey(), type -> new HashSet<>()).add(rule.getRole());
				}
			}
		}
	}

	/**
	 * A rule not stored that grants the source role of the given inheritance rule on an object the
	 * path of the given rule may end at.
	 */
	private RoleProvider notStoredSource(RoleRule rule) {
		BoundRole sourceRole = rule.getSourceRole();
		for (TLClass endType : endTypes(rule.getMetaElement(), rule.isInherit(), rule.getPath(),
			rule.getSourceMetaElement())) {
			for (RoleProvider source : _rules.getOrDefault(endType, Collections.emptyList())) {
				if (source != rule && _notStored.contains(source) && source.getRole().equals(sourceRole)) {
					return source;
				}
			}
		}
		return null;
	}

	private void computeRolesBeyondStorage() {
		for (Collection<RoleProvider> rules : _rules.values()) {
			for (RoleProvider rule : rules) {
				if (_notStored.contains(rule)
					|| (rule instanceof RoleRule roleRule && roleRule.getHistory().isVersionDependent(true))) {
					_rolesBeyondStorage.add(rule.getRole());
				}
			}
		}
		boolean changed = true;
		while (changed) {
			changed = false;
			for (Collection<RoleProvider> rules : _rules.values()) {
				for (RoleProvider rule : rules) {
					if (rule.getType() == Type.inheritance && _rolesBeyondStorage.contains(rule.getSourceRole())) {
						changed |= _rolesBeyondStorage.add(rule.getRole());
					}
				}
			}
		}
	}

	/**
	 * Whether the groups the given rule grants its role to on current objects are kept in the
	 * security storage.
	 *
	 * <p>
	 * A rule is not stored, if its path navigates a historic reference in forward direction, or if
	 * it inherits a role that a rule not stored grants on the object at the end of its path. Such a
	 * rule is evaluated whenever access is checked.
	 * </p>
	 */
	public boolean isStored(RoleProvider rule) {
		return !_notStored.contains(rule);
	}

	/**
	 * All rules that are not {@link #isStored(RoleProvider) stored}.
	 */
	public Set<RoleProvider> getNotStoredRules() {
		return Collections.unmodifiableSet(_notStored);
	}

	/**
	 * The rule not stored, from which the given rule inherits and which is the reason why the given
	 * rule is not stored either.
	 *
	 * @return <code>null</code>, if the given rule is stored or not stored for its own path.
	 */
	public RoleProvider getNotStoredCause(RoleProvider rule) {
		return _notStoredCause.get(rule);
	}

	/**
	 * Whether a rule not stored grants one of the given roles on objects of the given type.
	 */
	public boolean hasNotStoredRule(TLClass type, Collection<? extends BoundRole> roles) {
		Set<BoundRole> notStored = _notStoredRoles.get(type);
		if (notStored == null) {
			return false;
		}
		for (BoundRole role : roles) {
			if (notStored.contains(role)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Whether a rule not stored grants a role on objects of the given type.
	 */
	public boolean hasNotStoredRule(TLClass type) {
		return _notStoredRoles.containsKey(type);
	}

	/**
	 * Whether the roles a person holds on a current object can always be looked up in the security
	 * storage by the identities of the object and its role parents.
	 *
	 * <p>
	 * This is not the case, if some rule is not stored, or if a role parent of a current object may
	 * be a historic version.
	 * </p>
	 */
	public boolean isStorageSufficientForCurrentObjects() {
		return _notStored.isEmpty() && !_historicRoleParents;
	}

	/**
	 * Whether a role parent rule with mode {@link HistoryMode#ANCHORED} applies to the given type.
	 */
	public boolean hasAnchoredRoleParents(TLClass type) {
		return _typesWithAnchoredParents.contains(type);
	}

	/**
	 * Whether a person may hold the given role on some object without an entry in the security
	 * storage: on a historic version of an object, or through a rule that is not stored.
	 */
	public boolean isBeyondStorage(BoundRole role) {
		return _rolesBeyondStorage.contains(role);
	}

	/**
	 * The concrete types of the objects the given path may end at.
	 *
	 * @param startType
	 *        The type the rule is declared for.
	 * @param inherit
	 *        Whether the rule applies to the subtypes of the start type.
	 * @param path
	 *        The path of the rule.
	 * @param restriction
	 *        The only type accepted at the end of the path, <code>null</code> for no restriction.
	 */
	public static Set<TLClass> endTypes(TLClass startType, boolean inherit, List<PathElement> path,
			TLClass restriction) {
		if (restriction != null) {
			return Collections.singleton(restriction);
		}
		TLType end = null;
		for (PathElement step : path) {
			if (step instanceof PathNavigation navigation) {
				end = navigation.isInverse() ? navigation.getReference().getOwner()
					: navigation.getReference().getType();
			} else if (step instanceof SingletonPathElement singletonStep) {
				TLObject singleton = singletonStep.getSingleton();
				end = singleton == null ? null : singleton.tType();
				if (end == null) {
					return Collections.emptySet();
				}
			} else if (step instanceof GroupWithName) {
				end = Group.getGroupType();
			}
		}
		if (end == null) {
			// The path stays at the object the rule is evaluated for.
			if (!inherit) {
				return Collections.singleton(startType);
			}
			end = startType;
		}
		if (end instanceof TLClass endClass) {
			return TLModelUtil.getConcreteReflexiveTransitiveSpecializations(endClass);
		}
		return Collections.emptySet();
	}

}
