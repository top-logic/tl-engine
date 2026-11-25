/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.boundsec.manager.coverage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.dob.meta.MOReference.HistoryType;
import com.top_logic.element.boundsec.manager.ElementAccessManager;
import com.top_logic.element.boundsec.manager.RuleAnalysis;
import com.top_logic.element.boundsec.manager.rule.GroupWithName;
import com.top_logic.element.boundsec.manager.rule.NavigationRule;
import com.top_logic.element.boundsec.manager.rule.PathElement;
import com.top_logic.element.boundsec.manager.rule.PathNavigation;
import com.top_logic.element.boundsec.manager.rule.RoleProvider;
import com.top_logic.element.boundsec.manager.rule.RoleProvider.Type;
import com.top_logic.element.boundsec.manager.rule.RoleRule;
import com.top_logic.element.boundsec.manager.rule.RuleHistory;
import com.top_logic.element.boundsec.manager.rule.SingletonPathElement;
import com.top_logic.element.boundsec.manager.rule.config.HistoryMode;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.kbbased.storage.ForeignKeyStorage;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLType;
import com.top_logic.model.security.ModelAccessRights;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.BoundRole;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.boundsec.wrap.BoundedRole;

/**
 * Checks the role rules and role parent rules of a type that treat historic versions of objects.
 *
 * <p>
 * The findings are warnings about rules that never grant a role, that are expensive, or whose roles
 * depend on themselves.
 * </p>
 *
 * @see RuleHistory
 */
final class HistoryRuleCheck {

	private final ElementAccessManager _accessManager;

	private final ModelAccessRights _accessRights;

	private final Map<TLClass, Set<TLClass>> _containerTypes;

	private final Map<Node, List<Edge>> _edges = new HashMap<>();

	HistoryRuleCheck(ElementAccessManager accessManager, ModelAccessRights accessRights,
			Map<TLClass, Set<TLClass>> containerTypes) {
		_accessManager = accessManager;
		_accessRights = accessRights;
		_containerTypes = containerTypes;
	}

	/**
	 * Adds the findings for the given type.
	 */
	void addFindings(TLClass type, List<CoverageFinding> findings) {
		Collection<RoleProvider> roleRules = _accessManager.getRules(type);
		Collection<NavigationRule> parentRules = _accessManager.getRoleParentRules(type);
		if (roleRules.isEmpty() && parentRules.isEmpty()) {
			return;
		}
		String typeName = TLModelUtil.qualifiedName(type);
		List<RuleUse> rules = new ArrayList<>();
		for (RoleProvider rule : roleRules) {
			if (rule instanceof RoleRule roleRule) {
				rules.add(new RuleUse(rule.getConfigId(), roleRule.getHistory(), rule.getPath(), true));
			}
		}
		for (NavigationRule rule : parentRules) {
			rules.add(new RuleUse(rule.getId(), rule.getHistory(), rule.getPath(), false));
		}

		addNeverCovers(type, typeName, rules, findings);
		addAnchorStorage(type, typeName, rules, findings);
		addAnchorStepFromCurrent(type, typeName, rules, findings);
		addNoCurrentRoleSource(type, typeName, rules, findings);
		addNotStored(type, typeName, roleRules, findings);
		addMixedBackwardsStep(type, typeName, rules, findings);
		addCycles(type, typeName, findings);
	}

	private void addNeverCovers(TLClass type, String typeName, List<RuleUse> rules, List<CoverageFinding> findings) {
		Set<String> ruleIds = new LinkedHashSet<>();
		Set<String> references = new LinkedHashSet<>();
		for (RuleUse rule : rules) {
			if (rule.history().getMode() != HistoryMode.ANCHORED) {
				continue;
			}
			boolean covered = false;
			for (TLReference anchor : rule.history().getAnchors()) {
				if (canBeCovered(type, anchor.getType(), new HashSet<>())) {
					covered = true;
					break;
				}
			}
			if (!covered) {
				ruleIds.add(rule.id());
				for (TLReference anchor : rule.history().getAnchors()) {
					references.add(TLModelUtil.qualifiedName(anchor));
				}
			}
		}
		if (!ruleIds.isEmpty()) {
			findings.add(CoverageFinding.historyRules(FindingKind.ANCHOR_NEVER_COVERS, type,
				I18NConstants.ANCHOR_NEVER_COVERS__TYPE_RULES_REFERENCES.fill(typeName, join(ruleIds),
					join(references)),
				List.copyOf(ruleIds)));
		}
	}

	private boolean canBeCovered(TLClass type, TLType target, Set<TLClass> visited) {
		if (!visited.add(type)) {
			return false;
		}
		if (!(target instanceof TLClass targetClass)) {
			return true;
		}
		if (TLModelUtil.isGeneralization(targetClass, type) || TLModelUtil.isGeneralization(type, targetClass)) {
			return true;
		}
		for (TLClass container : _containerTypes.getOrDefault(type, Collections.emptySet())) {
			if (canBeCovered(container, target, visited)) {
				return true;
			}
		}
		return false;
	}

	private void addAnchorStorage(TLClass type, String typeName, List<RuleUse> rules,
			List<CoverageFinding> findings) {
		Map<TLReference, Set<String>> withoutIndex = new LinkedHashMap<>();
		for (RuleUse rule : rules) {
			for (TLReference anchor : anchorReferences(rule)) {
				if (AttributeOperations.getStorageImplementation(anchor) instanceof ForeignKeyStorage) {
					// No index is created for a historic reference stored in a column.
					withoutIndex.computeIfAbsent(anchor, x -> new LinkedHashSet<>()).add(rule.id());
				}
			}
		}
		for (Map.Entry<TLReference, Set<String>> entry : withoutIndex.entrySet()) {
			findings.add(CoverageFinding.historyRules(FindingKind.ANCHOR_WITHOUT_INDEX, type,
				I18NConstants.ANCHOR_WITHOUT_INDEX__TYPE_RULES_REFERENCE.fill(typeName, join(entry.getValue()),
					TLModelUtil.qualifiedName(entry.getKey())),
				List.copyOf(entry.getValue())));
		}
	}

	/**
	 * The historic references the given rule navigates backwards or names as anchors.
	 */
	private static Set<TLReference> anchorReferences(RuleUse rule) {
		Set<TLReference> result = new LinkedHashSet<>(rule.history().getAnchors());
		for (PathElement step : rule.path()) {
			if (step instanceof PathNavigation navigation && navigation.isAnchorStep()) {
				result.add(navigation.getReference());
			}
		}
		return result;
	}

	private void addAnchorStepFromCurrent(TLClass type, String typeName, List<RuleUse> rules,
			List<CoverageFinding> findings) {
		Set<String> ruleIds = new LinkedHashSet<>();
		for (RuleUse rule : rules) {
			// The object the rule is evaluated for may be a historic version.
			boolean mayBeHistoric = true;
			for (PathElement step : rule.path()) {
				if (step instanceof PathNavigation navigation) {
					if (navigation.isAnchorStep()) {
						if (!mayBeHistoric) {
							ruleIds.add(rule.id());
						}
						// An anchor is a current object.
						mayBeHistoric = false;
					} else if (!navigation.isInverse()
						&& navigation.getReference().getHistoryType() != HistoryType.CURRENT) {
						mayBeHistoric = true;
					}
				} else if (step instanceof SingletonPathElement || step instanceof GroupWithName) {
					mayBeHistoric = false;
				}
			}
		}
		if (!ruleIds.isEmpty()) {
			findings.add(CoverageFinding.historyRules(FindingKind.ANCHOR_STEP_FROM_CURRENT, type,
				I18NConstants.ANCHOR_STEP_FROM_CURRENT__TYPE_RULES.fill(typeName, join(ruleIds)),
				List.copyOf(ruleIds)));
		}
	}

	private void addNoCurrentRoleSource(TLClass type, String typeName, List<RuleUse> rules,
			List<CoverageFinding> findings) {
		for (RuleUse rule : rules) {
			if (rule.history().getMode() != HistoryMode.ANCHORED && !rule.history().hasAnchorStep()) {
				return;
			}
		}
		findings.add(CoverageFinding.historyRules(FindingKind.NO_CURRENT_ROLE_SOURCE, type,
			I18NConstants.NO_CURRENT_ROLE_SOURCE__TYPE.fill(typeName), Collections.emptyList()));
	}

	private void addNotStored(TLClass type, String typeName, Collection<RoleProvider> roleRules,
			List<CoverageFinding> findings) {
		RuleAnalysis analysis = _accessManager.getRuleAnalysis();
		Set<String> reported = new HashSet<>();
		for (RoleProvider rule : roleRules) {
			RoleProvider cause = analysis.getNotStoredCause(rule);
			if (cause != null && reported.add(rule.getConfigId())) {
				findings.add(CoverageFinding.historyRules(FindingKind.RULE_NOT_STORED, type,
					I18NConstants.RULE_NOT_STORED__TYPE_RULE_SOURCE.fill(typeName, rule.getConfigId(),
						cause.getConfigId()),
					Collections.singletonList(rule.getConfigId())));
			}
		}
	}

	private void addMixedBackwardsStep(TLClass type, String typeName, List<RuleUse> rules,
			List<CoverageFinding> findings) {
		Map<TLReference, Set<String>> mixed = new LinkedHashMap<>();
		for (RuleUse rule : rules) {
			if (!isEvaluatedOnVersions(rule)) {
				continue;
			}
			for (PathElement step : rule.path()) {
				if (step instanceof PathNavigation navigation && navigation.isInverse()
					&& navigation.getReference().getHistoryType() == HistoryType.MIXED) {
					mixed.computeIfAbsent(navigation.getReference(), x -> new LinkedHashSet<>()).add(rule.id());
				}
			}
		}
		for (Map.Entry<TLReference, Set<String>> entry : mixed.entrySet()) {
			findings.add(CoverageFinding.historyRules(FindingKind.MIXED_BACKWARDS_STEP, type,
				I18NConstants.MIXED_BACKWARDS_STEP__TYPE_RULES_REFERENCE.fill(typeName, join(entry.getValue()),
					TLModelUtil.qualifiedName(entry.getKey())),
				List.copyOf(entry.getValue())));
		}
	}

	private static boolean isEvaluatedOnVersions(RuleUse rule) {
		RuleHistory history = rule.history();
		if (rule.roleRule()) {
			return history.isVersionDependent(true);
		}
		return history.getMode() != HistoryMode.CURRENT;
	}

	private void addCycles(TLClass type, String typeName, List<CoverageFinding> findings) {
		Set<BoundRole> roles = new LinkedHashSet<>();
		for (RoleProvider rule : _accessManager.getRules(type)) {
			roles.add(rule.getRole());
		}
		boolean parentsOfVersions = false;
		for (NavigationRule rule : _accessManager.getRoleParentRules(type)) {
			parentsOfVersions |= rule.getHistory().isVersionDependent(false);
		}
		if (parentsOfVersions) {
			roles.addAll(BoundedRole.getAll());
		}
		for (BoundRole role : roles) {
			Node start = new Node(type, role);
			for (Edge edge : edges(start)) {
				if (!edge.versionDependent()) {
					continue;
				}
				if (!edge.gate() && edge.target().equals(start)) {
					// A role inherited from an object of the same type, as in a hierarchy whose
					// data is a tree.
					continue;
				}
				List<String> path = new ArrayList<>();
				path.add(edge.rule());
				if (findPath(edge.target(), start, new HashSet<>(), path)) {
					findings.add(CoverageFinding.historyRules(FindingKind.ROLE_CYCLE, type,
						I18NConstants.ROLE_CYCLE__TYPE_ROLE_RULES.fill(typeName, role.getName(),
							String.join(" -> ", path)),
						List.copyOf(new LinkedHashSet<>(path))));
					break;
				}
			}
		}
	}

	private boolean findPath(Node from, Node to, Set<Node> visited, List<String> path) {
		if (from.equals(to)) {
			return true;
		}
		if (!visited.add(from)) {
			return false;
		}
		for (Edge edge : edges(from)) {
			path.add(edge.rule());
			if (findPath(edge.target(), to, visited, path)) {
				return true;
			}
			path.remove(path.size() - 1);
		}
		return false;
	}

	/**
	 * The roles on types, on which the given role on the given type depends.
	 */
	private List<Edge> edges(Node node) {
		List<Edge> cached = _edges.get(node);
		if (cached != null) {
			return cached;
		}
		List<Edge> result = new ArrayList<>();
		TLClass type = node.type();
		BoundRole role = node.role();
		for (RoleProvider provider : _accessManager.getRules(type)) {
			if (!(provider instanceof RoleRule rule) || !rule.getRole().equals(role)) {
				continue;
			}
			RuleHistory history = rule.getHistory();
			if (rule.getType() == Type.inheritance) {
				for (TLClass end : RuleAnalysis.endTypes(rule.getMetaElement(), rule.isInherit(), rule.getPath(),
					rule.getSourceMetaElement())) {
					result.add(new Edge(new Node(end, rule.getSourceRole()), rule.getConfigId(),
						history.isVersionDependent(true), false));
				}
			}
			addGateEdges(result, rule.getConfigId(), history);
		}
		for (NavigationRule rule : _accessManager.getRoleParentRules(type)) {
			RuleHistory history = rule.getHistory();
			for (TLClass end : RuleAnalysis.endTypes(rule.getMetaElement(), rule.isInherit(), rule.getPath(), null)) {
				result.add(new Edge(new Node(end, role), rule.getId(), history.isVersionDependent(false),
					false));
			}
			addGateEdges(result, rule.getId(), history);
		}
		_edges.put(node, result);
		return result;
	}

	/**
	 * Adds the dependencies of a rule on the roles that may read its anchor references.
	 */
	private void addGateEdges(List<Edge> result, String ruleId, RuleHistory history) {
		if (history.getMode() != HistoryMode.ANCHORED) {
			return;
		}
		for (TLReference anchor : history.getAnchors()) {
			if (!(anchor.getOwner() instanceof TLClass owner)) {
				continue;
			}
			for (TLClass anchorType : TLModelUtil.getConcreteReflexiveTransitiveSpecializations(owner)) {
				if (_accessRights.hasGrant(anchorType, anchor, SimpleBoundCommandGroup.READ)) {
					Set<BoundedRole> readers =
						_accessRights.getAllowedRoles(anchorType, anchor, SimpleBoundCommandGroup.READ);
					for (TLClass holder : roleHolderTypes(anchorType, new HashSet<>())) {
						for (BoundedRole reader : readers) {
							result.add(new Edge(new Node(holder, reader), ruleId, true, true));
						}
					}
				} else {
					for (TLClass holder : roleHolderTypes(anchorType, new HashSet<>())) {
						for (BoundedRole reader : _accessRights.getAllowedRoles(holder, SimpleBoundCommandGroup.READ)) {
							result.add(new Edge(new Node(holder, reader), ruleId, true, true));
						}
					}
				}
			}
		}
	}

	/**
	 * The types at the end of the chain of access parents of the given type.
	 */
	private Set<TLClass> roleHolderTypes(TLClass type, Set<TLClass> visited) {
		if (!visited.add(type)) {
			return Collections.emptySet();
		}
		if (_accessRights.getAccessParent(type) == null) {
			return Collections.singleton(type);
		}
		Set<TLClass> result = new LinkedHashSet<>();
		for (TLClass parent : _accessRights.getAccessParentTypes(type)) {
			for (TLClass concrete : TLModelUtil.getConcreteReflexiveTransitiveSpecializations(parent)) {
				result.addAll(roleHolderTypes(concrete, visited));
			}
		}
		return result;
	}

	private static String join(Collection<String> values) {
		return String.join(", ", values);
	}

	/**
	 * A rule applying to the checked type.
	 */
	private record RuleUse(String id, RuleHistory history, List<PathElement> path, boolean roleRule) {
		// Pure value.
	}

	/**
	 * A role on a type.
	 */
	private record Node(TLClass type, BoundRole role) {
		// Pure value.
	}

	/**
	 * A dependency of a role on a type on another one.
	 *
	 * @param target
	 *        The role the dependency leads to.
	 * @param rule
	 *        The id of the rule creating the dependency.
	 * @param versionDependent
	 *        Whether the dependency only exists on historic versions of objects.
	 * @param gate
	 *        Whether the dependency is the one of a rule on the readers of its anchor references.
	 */
	private record Edge(Node target, String rule, boolean versionDependent, boolean gate) {
		// Pure value.
	}

}
