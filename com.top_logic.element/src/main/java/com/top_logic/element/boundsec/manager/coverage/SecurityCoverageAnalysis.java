/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.element.boundsec.manager.coverage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.top_logic.element.boundsec.ElementBoundHelper;
import com.top_logic.element.boundsec.manager.ElementAccessManager;
import com.top_logic.element.boundsec.manager.rule.IdentityPathElement;
import com.top_logic.element.boundsec.manager.rule.NavigationRule;
import com.top_logic.element.boundsec.manager.rule.PathElement;
import com.top_logic.element.boundsec.manager.rule.PathNavigation;
import com.top_logic.element.boundsec.manager.rule.RoleProvider;
import com.top_logic.element.boundsec.manager.rule.SingletonPathElement;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.security.AccessParent;
import com.top_logic.model.security.ModelAccessRights;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundHelper;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.tool.boundsec.simple.CommandGroupRegistry;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.util.model.ModelService;

/**
 * Checks the model-based access definition of an application for completeness.
 *
 * <p>
 * For every concrete global type the analysis answers the two questions that decide whether a user
 * can ever see an object of that type: does a rule deliver a role on it, and is a role granted the
 * read operation on it? Each gap is reported as a {@link CoverageFinding}. A type with an
 * {@link AccessParent access parent} delegates every access decision to another object and needs no
 * definition of its own; it is reported as delegated, with a finding only where a rule applying to
 * it is shadowed by the access parent.
 * </p>
 *
 * <p>
 * A role reaches an object either through a rule that computes it or through a role assignment that
 * names the object. The analysis reads the role assignments of the knowledge base once, so that a
 * grant to a role an application assigns by hand is not mistaken for a dead grant. A role source,
 * in contrast, must be a rule: an assignment on a single object says nothing about the objects
 * created next.
 * </p>
 *
 * <p>
 * The analysis is a pure computation over the definitions that are in effect: it neither changes
 * them nor writes anything to the log.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SecurityCoverageAnalysis {

	private final TLModel _model;

	private final ElementAccessManager _accessManager;

	private final ModelAccessRights _accessRights;

	private final Set<String> _excludedModules;

	private final boolean _rootFallbackActive;

	private final TLClass _securityRootType;

	private final List<BoundCommandGroup> _operations;

	private final List<TLReference> _compositeReferences;

	private final Map<TLClass, Set<BoundedRole>> _directlyAssignedRoles;

	/**
	 * Creates a {@link SecurityCoverageAnalysis}.
	 *
	 * @param model
	 *        The application model whose types are analyzed.
	 * @param accessManager
	 *        The access manager holding the role rules and the role parent rules.
	 * @param accessRights
	 *        The configured grants.
	 * @param excludedModules
	 *        Names of the modules whose types are not analyzed. Must not be <code>null</code>.
	 */
	public SecurityCoverageAnalysis(TLModel model, ElementAccessManager accessManager, ModelAccessRights accessRights,
			Set<String> excludedModules) {
		_model = Objects.requireNonNull(model);
		_accessManager = Objects.requireNonNull(accessManager);
		_accessRights = Objects.requireNonNull(accessRights);
		_excludedModules = Set.copyOf(excludedModules);

		BoundHelper boundHelper = BoundHelper.getInstance();
		_rootFallbackActive = boundHelper.useDefaultObject();
		// The security root is only relevant as the fallback role parent, so it is not
		// resolved in an application that does not use it.
		_securityRootType = _rootFallbackActive ? securityRootType(boundHelper) : null;

		_operations = new ArrayList<>(CommandGroupRegistry.getInstance().getAllCommandGroups());
		_operations.sort(Comparator.comparing(BoundCommandGroup::getID));

		_compositeReferences = compositeReferences(model);

		_directlyAssignedRoles = directlyAssignedRoles();
	}

	/**
	 * Creates a {@link SecurityCoverageAnalysis} for the running application.
	 *
	 * @param excludedModules
	 *        Names of the modules whose types are not analyzed.
	 * @return The analysis over the application model and the started security services.
	 */
	public static SecurityCoverageAnalysis newInstance(Set<String> excludedModules) {
		return new SecurityCoverageAnalysis(ModelService.getApplicationModel(),
			(ElementAccessManager) AccessManager.getInstance(), ModelAccessRights.getInstance(), excludedModules);
	}

	/**
	 * The coverage of every analyzed type, ordered by the qualified type name.
	 */
	public List<TypeCoverage> analyze() {
		List<TypeCoverage> result = new ArrayList<>();
		for (TLClass type : TLModelUtil.getAllGlobalClasses(_model)) {
			if (isAnalyzed(type)) {
				result.add(analyze(type));
			}
		}
		result.sort(Comparator.comparing(coverage -> TLModelUtil.qualifiedName(coverage.type())));
		return result;
	}

	/**
	 * Whether the given type is part of the {@link #analyze() analysis result}.
	 *
	 * <p>
	 * An abstract type is skipped, since no object has it. A type of an excluded module is skipped,
	 * since its access definition is not maintained by the analyzed application.
	 * </p>
	 *
	 * @param type
	 *        The type to check.
	 */
	public boolean isAnalyzed(TLClass type) {
		return !type.isAbstract() && !_excludedModules.contains(type.getModule().getName());
	}

	/**
	 * Analyzes the access definition of a single type.
	 *
	 * @param type
	 *        The type to analyze. Typically a type {@link #isAnalyzed(TLClass) selected} by the
	 *        analysis.
	 * @return The coverage of the given type.
	 */
	public TypeCoverage analyze(TLClass type) {
		List<RoleProvider> roleRules = List.copyOf(_accessManager.getRules(type));
		List<NavigationRule> parentRules = List.copyOf(_accessManager.getRoleParentRules(type));
		Set<BoundedRole> readRoles = Set.copyOf(_accessRights.getAllowedRoles(type, SimpleBoundCommandGroup.READ));
		boolean withoutSecurity = _accessRights.isWithoutSecurity(type);
		boolean internal = _accessRights.isInternal(type);
		AccessParent accessParent = _accessRights.getAccessParent(type);
		List<TLReference> containers = containerReferences(type);

		List<CoverageFinding> findings = new ArrayList<>();
		if (withoutSecurity || internal) {
			// An exempt type needs no definition.
		} else if (accessParent != null) {
			List<String> shadowed = shadowedRules(roleRules, parentRules);
			if (!shadowed.isEmpty()) {
				findings.add(CoverageFinding.shadowedRules(type, shadowed));
			}
		} else {
			if (roleRules.isEmpty() && parentRules.isEmpty()) {
				findings.add(CoverageFinding.noRoleSource(type, _rootFallbackActive));
			}
			if (readRoles.isEmpty()) {
				findings.add(CoverageFinding.noReadGrant(type));
			}
			addDeadGrantFindings(type, findings);
		}
		return new TypeCoverage(type, withoutSecurity, internal, accessParent, containers, readRoles, roleRules,
			parentRules, Collections.unmodifiableList(findings));
	}

	/**
	 * The ids of the given rules, which an access parent shadows: each role rule once, under the id
	 * of the configuration it was created from, followed by the role parent rules.
	 */
	private static List<String> shadowedRules(List<RoleProvider> roleRules, List<NavigationRule> parentRules) {
		List<String> result = new ArrayList<>();
		for (RoleProvider rule : roleRules) {
			if (!result.contains(rule.getConfigId())) {
				result.add(rule.getConfigId());
			}
		}
		for (NavigationRule rule : parentRules) {
			result.add(rule.getId());
		}
		return result;
	}

	/**
	 * The compositions an object of the given type can be contained in.
	 */
	private List<TLReference> containerReferences(TLClass type) {
		return _compositeReferences.stream()
			.filter(reference -> TLModelUtil.isCompatibleType(reference.getType(), type))
			.toList();
	}

	/**
	 * Reports every operation whose grant names a role that cannot be delivered on the given type.
	 */
	private void addDeadGrantFindings(TLClass type, List<CoverageFinding> findings) {
		for (BoundCommandGroup operation : _operations) {
			Set<BoundedRole> granted = _accessRights.getAllowedRoles(type, operation);
			if (granted.isEmpty()) {
				continue;
			}
			Set<BoundedRole> dead = new LinkedHashSet<>();
			for (BoundedRole role : granted) {
				if (!canDeliver(type, role, new HashSet<>())) {
					dead.add(role);
				}
			}
			if (!dead.isEmpty()) {
				findings.add(CoverageFinding.deadGrant(type, operation, dead));
			}
		}
	}

	/**
	 * Whether a user can hold the given role on an object of the given type, either on the object
	 * itself or on one of its role parents.
	 *
	 * <p>
	 * A role reaches an object through a rule that computes it, or through a role assignment that
	 * names the object explicitly. Both count, otherwise every role that an application assigns by
	 * hand would look undeliverable.
	 * </p>
	 *
	 * @param visited
	 *        The types already inspected along the role parent chain, guarding against cycles.
	 * @return Also <code>true</code> when the end of a role parent path cannot be determined
	 *         statically, so that an undecidable path never produces a finding.
	 */
	private boolean canDeliver(TLClass type, BoundedRole role, Set<TLClass> visited) {
		if (!visited.add(type)) {
			return false;
		}
		if (_accessManager.canHaveRole(type, role) || isAssignedDirectly(type, role)) {
			return true;
		}
		Collection<NavigationRule> parentRules = _accessManager.getRoleParentRules(type);
		if (parentRules.isEmpty()) {
			return _rootFallbackActive && _securityRootType != null
				&& (_accessManager.canHaveRole(_securityRootType, role)
					|| isAssignedDirectly(_securityRootType, role));
		}
		for (NavigationRule rule : parentRules) {
			TLType endType = endType(rule);
			if (!(endType instanceof TLClass endClass)) {
				// The role parents of the path cannot be determined statically.
				return true;
			}
			if (canDeliver(endClass, role, visited)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Whether the given role is assigned on an object of the given type without a rule computing
	 * it.
	 *
	 * @see #directlyAssignedRoles()
	 */
	private boolean isAssignedDirectly(TLClass type, BoundedRole role) {
		return _directlyAssignedRoles.getOrDefault(type, Collections.emptySet()).contains(role);
	}

	/**
	 * The roles held through a role assignment, indexed by every type an object carrying such an
	 * assignment has.
	 *
	 * <p>
	 * The assignments are read once, so that the analysis of a model with many types does not
	 * query the knowledge base per type. An assignment is indexed under the type of its object and
	 * under all generalizations of that type, because an object of a specialization is an object of
	 * its generalizations as well.
	 * </p>
	 */
	private static Map<TLClass, Set<BoundedRole>> directlyAssignedRoles() {
		Map<TLClass, Set<BoundedRole>> result = new HashMap<>();
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		for (KnowledgeObject assignment : kb.getAllKnowledgeObjects(BoundedRole.ROLE_ASSIGNMENT_OBJECT_NAME)) {
			TLObject object = reference(assignment, BoundedRole.ATTRIBUTE_OBJECT);
			TLObject role = reference(assignment, BoundedRole.ATTRIBUTE_ROLE);
			if (!(object != null && role instanceof BoundedRole assignedRole)) {
				continue;
			}
			if (!(object.tType() instanceof TLClass objectType)) {
				continue;
			}
			for (TLClass generalization : TLModelUtil.getReflexiveTransitiveGeneralizations(objectType)) {
				result.computeIfAbsent(generalization, ignored -> new HashSet<>()).add(assignedRole);
			}
		}
		return result;
	}

	/**
	 * The object a reference of a role assignment points to.
	 *
	 * @return <code>null</code> when the reference is not filled.
	 */
	private static TLObject reference(KnowledgeObject assignment, String attribute) {
		Object value = assignment.getAttributeValue(attribute);
		return value instanceof KnowledgeItem item ? item.getWrapper() : null;
	}

	/**
	 * The type of the objects a role parent rule navigates to.
	 *
	 * @return <code>null</code> when the path cannot be followed statically.
	 */
	private TLType endType(NavigationRule rule) {
		TLType current = rule.getMetaElement();
		for (PathElement step : rule.getPath()) {
			if (step instanceof PathNavigation navigation) {
				TLReference reference = navigation.getReference();
				current = navigation.isInverse() ? reference.getOwner() : reference.getType();
			} else if (step instanceof SingletonPathElement singleton) {
				TLObject value = singleton(singleton.getConfig());
				current = value == null ? null : value.tType();
			} else if (step instanceof IdentityPathElement) {
				// The identity step stays on the current object, so the type does not change.
			} else {
				return null;
			}
			if (current == null) {
				return null;
			}
		}
		return current;
	}

	/**
	 * Resolves the module singleton a {@link SingletonPathElement} navigates to.
	 */
	private TLObject singleton(SingletonPathElement.Config config) {
		TLModule module = _model.getModule(config.getModule());
		return module == null ? null : module.getSingleton(config.getSingletonName());
	}

	/**
	 * The type of the security root, whose roles every object inherits when the global default
	 * role parent is active.
	 *
	 * @return <code>null</code> when the application has no security root.
	 */
	private static TLClass securityRootType(BoundHelper boundHelper) {
		if (!(boundHelper instanceof ElementBoundHelper elementBoundHelper)) {
			return null;
		}
		BoundObject root = elementBoundHelper.securityRoot();
		if (root != null && root.tType() instanceof TLClass rootType) {
			return rootType;
		}
		return null;
	}

	/**
	 * All compositions of the model, indexed by the part they are defined by so that an override
	 * does not count as a second container.
	 */
	private static List<TLReference> compositeReferences(TLModel model) {
		Map<TLStructuredTypePart, TLReference> byDefinition = new LinkedHashMap<>();
		for (TLClass owner : TLModelUtil.getAllGlobalClasses(model)) {
			for (TLStructuredTypePart part : owner.getLocalParts()) {
				if (!(part instanceof TLReference reference) || !reference.isComposite()) {
					continue;
				}
				TLStructuredTypePart definition = reference.getDefinition();
				TLReference clash = byDefinition.get(definition);
				if (clash == null || (clash.isDerived() && !reference.isDerived())) {
					byDefinition.put(definition, reference);
				}
			}
		}
		List<TLReference> result = new ArrayList<>();
		for (TLReference reference : byDefinition.values()) {
			if (!reference.isDerived()) {
				// A derived reference cannot be navigated by a role parent rule, since it does
				// not fire the change notifications the rule invalidation relies on.
				result.add(reference);
			}
		}
		result.sort(Comparator.comparing(TLModelUtil::qualifiedName));
		return result;
	}

}
