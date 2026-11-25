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
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.security.AccessParentFunction;
import com.top_logic.model.security.ApplicableOperations;
import com.top_logic.model.security.ModelAccessRights;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.tool.boundsec.securityObjectProvider.SecurityRootObjectProvider;
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
 * read operation on it? Each gap is reported as a {@link CoverageFinding}. A type whose
 * {@link AccessParentFunction access parent} delegates every access decision to another object needs
 * no definition of its own; it is reported as delegated, with a finding only where a rule applying
 * to it is shadowed by the access parent. A type whose access parent is restricted to some
 * operations is checked like a type deciding for itself for the operations it does not delegate.
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
 * A grant of {@link SimpleBoundCommandGroup#CREATE} is checked where the engine checks it, see
 * {@link ModelAccessRights#isAllowedCreate(com.top_logic.knowledge.wrap.person.Person, TLClass, TLObject)}:
 * on the context the caller passes, or on the security root when it passes none. The context is not
 * known statically, so the role must be deliverable on one of the plausible contexts: the security
 * root, a type holding the created type in a composition, or a type the created type references,
 * see {@link #createContextTypes(TLClass)}. Every other operation is checked on the type itself and
 * its role parents.
 * </p>
 *
 * <p>
 * Beyond the read access, the analysis checks the operations: an operation declared for a type
 * that no role holds there, and an operation a place of the application - a view, a command, a
 * form, a privileged script, see {@link OperationUse} - checks on a type it does not apply to, or
 * that is no registered command group at all.
 * </p>
 *
 * <p>
 * The analysis is a pure computation over the definitions that are in effect: it neither changes
 * them nor writes anything to the log. Besides the findings it answers the questions an editor of
 * the grants of a type asks: which roles a user can hold on objects of the type
 * ({@link #getHoldableRoles(TLClass)}), and which places check an operation on them
 * ({@link #getUses(TLClass, BoundCommandGroup)}).
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SecurityCoverageAnalysis {

	private final TLModel _model;

	private final ElementAccessManager _accessManager;

	private final ModelAccessRights _accessRights;

	private final Set<String> _excludedModules;

	private final List<BoundCommandGroup> _operations;

	private final List<TLReference> _compositeReferences;

	/**
	 * The types an object of a type is created in, by the type, see {@link #containerTypes(TLModel)}.
	 */
	private final Map<TLClass, Set<TLClass>> _containerTypes;

	private final HistoryRuleCheck _historyRuleCheck;

	private final Map<TLClass, Set<BoundedRole>> _directlyAssignedRoles;

	private final List<OperationUse> _uses;

	/**
	 * Whether {@link #_uses} are all the places of the application checking an operation, so that
	 * a grant none of them checks can be reported.
	 */
	private final boolean _usesKnown;

	/**
	 * The findings about the {@link #_uses}, by the type they are reported for; the key
	 * <code>null</code> holds those of uses not declaring the type they check.
	 */
	private final Map<TLClass, List<CoverageFinding>> _useFindings;

	/**
	 * Creates a {@link SecurityCoverageAnalysis} that does not know the {@link OperationUse}s of the
	 * application.
	 *
	 * <p>
	 * Without knowing where operations are checked, the analysis reports no
	 * {@link FindingKind#UNCHECKED_GRANT}.
	 * </p>
	 *
	 * @see #SecurityCoverageAnalysis(TLModel, ElementAccessManager, ModelAccessRights, Set, List)
	 */
	public SecurityCoverageAnalysis(TLModel model, ElementAccessManager accessManager, ModelAccessRights accessRights,
			Set<String> excludedModules) {
		this(model, accessManager, accessRights, excludedModules, null);
	}

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
	 * @param uses
	 *        The places of the application checking an operation; <code>null</code> when they are
	 *        not known, so that a grant nothing checks is not reported.
	 */
	public SecurityCoverageAnalysis(TLModel model, ElementAccessManager accessManager, ModelAccessRights accessRights,
			Set<String> excludedModules, List<OperationUse> uses) {
		_model = Objects.requireNonNull(model);
		_accessManager = Objects.requireNonNull(accessManager);
		_accessRights = Objects.requireNonNull(accessRights);
		_excludedModules = Set.copyOf(excludedModules);
		_usesKnown = uses != null;
		_uses = uses == null ? Collections.emptyList() : List.copyOf(uses);

		_operations = new ArrayList<>(CommandGroupRegistry.getInstance().getAllCommandGroups());
		_operations.sort(Comparator.comparing(BoundCommandGroup::getID));

		_compositeReferences = compositeReferences(model);
		_containerTypes = containerTypes(model);
		_historyRuleCheck = new HistoryRuleCheck(accessManager, accessRights, _containerTypes);

		_directlyAssignedRoles = directlyAssignedRoles();

		_useFindings = useFindings();
	}

	/**
	 * Creates a {@link SecurityCoverageAnalysis} for the running application.
	 *
	 * @param excludedModules
	 *        Names of the modules whose types are not analyzed.
	 * @return The analysis over the application model and the started security services.
	 */
	public static SecurityCoverageAnalysis newInstance(Set<String> excludedModules) {
		return newInstance(excludedModules, null);
	}

	/**
	 * Creates a {@link SecurityCoverageAnalysis} for the running application.
	 *
	 * @param excludedModules
	 *        Names of the modules whose types are not analyzed.
	 * @param uses
	 *        The places of the application checking an operation, <code>null</code> when they are
	 *        not known.
	 * @return The analysis over the application model and the started security services.
	 */
	public static SecurityCoverageAnalysis newInstance(Set<String> excludedModules, List<OperationUse> uses) {
		return new SecurityCoverageAnalysis(ModelService.getApplicationModel(),
			(ElementAccessManager) AccessManager.getInstance(), ModelAccessRights.getInstance(), excludedModules,
			uses);
	}

	/**
	 * The roles a user can hold on an object of the given type.
	 *
	 * <p>
	 * A role is held on an object through a role rule computing it on the object or on one of its
	 * role parents, or through a role assignment naming such an object. A grant of an operation to
	 * any other role never takes effect on the type. A role parent path whose end cannot be
	 * followed statically counts as delivering every role.
	 * </p>
	 *
	 * <p>
	 * A type delegating to an {@link AccessParentFunction access parent} holds no roles of its own;
	 * the roles of the access parent decide.
	 * </p>
	 *
	 * @param type
	 *        The type of the objects.
	 * @return The roles, ordered by their names.
	 */
	public List<BoundedRole> getHoldableRoles(TLClass type) {
		List<BoundedRole> result = new ArrayList<>();
		for (BoundedRole role : BoundedRole.getAll()) {
			if (canHold(type, role)) {
				result.add(role);
			}
		}
		result.sort(Comparator.comparing(BoundedRole::getName));
		return result;
	}

	/**
	 * Whether a user can hold the given role on an object of the given type.
	 *
	 * @see #getHoldableRoles(TLClass)
	 */
	public boolean canHold(TLClass type, BoundedRole role) {
		return canDeliver(type, role, new HashSet<>());
	}

	/**
	 * Whether a grant of the given operation to the given role on the given type can take effect.
	 *
	 * <p>
	 * A grant of {@link SimpleBoundCommandGroup#CREATE} takes effect when a user can hold the role
	 * where the object may be created: on one of its {@link #createContextTypes(TLClass) context
	 * types}. A grant of any other operation takes effect when a user can
	 * {@link #canHold(TLClass, BoundedRole) hold} the role on an object of the type.
	 * </p>
	 *
	 * @param type
	 *        The type carrying the grant.
	 * @param operation
	 *        The granted operation.
	 * @param role
	 *        The role the operation is granted to.
	 * @return Whether the grant can take effect.
	 */
	public boolean isLiveGrant(TLClass type, BoundCommandGroup operation, BoundedRole role) {
		if (SimpleBoundCommandGroup.CREATE.equals(operation)) {
			return canDeliverCreate(type, role);
		}
		return canHold(type, role);
	}

	/**
	 * Whether a user can hold the given role where an object of the given type may be created.
	 *
	 * @return Also <code>true</code> when the application has no security root whose type is known,
	 *         so that an undecidable creation never produces a finding.
	 */
	private boolean canDeliverCreate(TLClass type, BoundedRole role) {
		if (securityRootType() == null) {
			return true;
		}
		for (TLClass context : createContextTypes(type)) {
			if (canDeliver(context, role, new HashSet<>())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * The types of the objects an object of the given type may be created in, ordered by their
	 * names.
	 *
	 * <p>
	 * The engine checks the creation on the context the caller passes, see
	 * {@link ModelAccessRights#isAllowedCreate(com.top_logic.knowledge.wrap.person.Person, TLClass, TLObject)}.
	 * Since the caller is not known statically, every plausible context counts:
	 * </p>
	 * <ul>
	 * <li>the security root, the context of a creation without context,</li>
	 * <li>the {@link #containerTypes(TLModel) containers}, the types holding the type in a
	 * composition,</li>
	 * <li>the types the type references, as the object it hangs on is a natural context: the
	 * targets of the references in effect on the type, inverse references included, and their
	 * specializations. Computed references and the type's own compositions are left out: nothing
	 * is created in the value of a computed reference, and the parts of an object are created after
	 * it.</li>
	 * </ul>
	 */
	private List<TLClass> createContextTypes(TLClass type) {
		Set<TLClass> result = new LinkedHashSet<>();
		TLClass rootType = securityRootType();
		if (rootType != null) {
			result.add(rootType);
		}
		result.addAll(_containerTypes.getOrDefault(type, Collections.emptySet()));
		for (TLStructuredTypePart part : type.getAllParts()) {
			if (!(part instanceof TLReference reference) || !(reference.getType() instanceof TLClass target)) {
				continue;
			}
			if (!reference.isBackwards() && (reference.isDerived() || reference.isComposite())) {
				continue;
			}
			result.addAll(TLModelUtil.getReflexiveTransitiveSpecializations(target));
		}
		List<TLClass> sorted = new ArrayList<>(result);
		sorted.sort(Comparator.comparing(TLModelUtil::qualifiedName));
		return sorted;
	}

	/**
	 * The type of the {@link #securityRoot() security root}, <code>null</code> when it is not
	 * known.
	 */
	private static TLClass securityRootType() {
		TLObject root = securityRoot();
		return root != null && root.tType() instanceof TLClass rootType ? rootType : null;
	}

	/**
	 * The places of the application checking an operation, as given to this analysis.
	 */
	public List<OperationUse> getOperationUses() {
		return _uses;
	}

	/**
	 * The places that may check the given operation on an object of the given type: those
	 * checking it on the type or on one of its generalizations, and those not declaring the type
	 * they check.
	 *
	 * @param type
	 *        The type of the checked objects.
	 * @param operation
	 *        The checked operation.
	 * @return The places in the order of {@link #getOperationUses()}.
	 */
	public List<OperationUse> getUses(TLClass type, BoundCommandGroup operation) {
		return _uses.stream()
			.filter(use -> use.operation().equals(operation.getID()))
			.filter(use -> use.mayCheck(type))
			.toList();
	}

	/**
	 * The places checking the given operation on the security root.
	 *
	 * @param operation
	 *        The checked operation.
	 * @return The places in the order of {@link #getOperationUses()}.
	 */
	public List<OperationUse> getRootUses(BoundCommandGroup operation) {
		return _uses.stream()
			.filter(use -> use.operation().equals(operation.getID()))
			.filter(use -> use.target() == OperationUse.Target.ROOT)
			.toList();
	}

	/**
	 * The findings about {@link #getOperationUses() operation uses} that belong to no analyzed
	 * type, so that no {@link #analyze(TLClass) type coverage} reports them.
	 *
	 * <p>
	 * These are the unknown operations of places not declaring the type they check, and the
	 * findings of places checking the security root when the type of the root is not analyzed.
	 * </p>
	 */
	public List<CoverageFinding> getUnattachedFindings() {
		List<CoverageFinding> result = new ArrayList<>();
		for (Map.Entry<TLClass, List<CoverageFinding>> entry : _useFindings.entrySet()) {
			TLClass type = entry.getKey();
			if (type == null || !isAnalyzed(type)) {
				result.addAll(entry.getValue());
			}
		}
		return result;
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
		Set<BoundedRole> discoverRoles =
			Set.copyOf(_accessRights.getAllowedRoles(type, SimpleBoundCommandGroup.DISCOVER));
		TLModelPart withoutSecurity = _accessRights.getWithoutSecurityOrigin(type);
		TLModelPart internal = _accessRights.getInternalOrigin(type);
		AccessParentFunction accessParent = _accessRights.getAccessParent(type);
		List<TLReference> containers = containerReferences(type);

		List<CoverageFinding> findings = new ArrayList<>();
		if (withoutSecurity != null || internal != null) {
			// An exempt type needs no definition.
		} else if (AccessParentFunction.delegatesAll(accessParent)) {
			List<String> shadowed = shadowedRules(roleRules, parentRules);
			if (!shadowed.isEmpty()) {
				findings.add(CoverageFinding.shadowedRules(type, shadowed));
			}
			addIgnoredGrantFindings(type, findings);
		} else {
			// A type whose access parent decides only some operations decides the others itself.
			if (roleRules.isEmpty() && parentRules.isEmpty()
				&& (accessParent == null || hasOwnGrant(type, accessParent))) {
				findings.add(CoverageFinding.noRoleSource(type));
			}
			// Every grant on the type or on one of its attributes implies Discover. Discover is
			// delegated together with any operation, the declared operations not delegated are
			// reported one by one. A type deciding Read itself is reported when it grants Read to
			// nobody, as a type deciding for itself.
			if (accessParent == null) {
				if (discoverRoles.isEmpty()) {
					findings.add(CoverageFinding.noReadGrant(type));
				}
			} else if (readRoles.isEmpty() && !readsThroughParent(type, accessParent)) {
				findings.add(CoverageFinding.noReadGrant(type));
			}
			addIgnoredGrantFindings(type, findings);
			addDeadGrantFindings(type, accessParent, findings);
			addUngrantedOperationFindings(type, accessParent, findings);
			addUncheckedGrantFindings(type, accessParent, findings);
			_historyRuleCheck.addFindings(type, findings);
		}
		if (withoutSecurity == null && internal == null) {
			findings.addAll(_useFindings.getOrDefault(type, Collections.emptyList()));
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
	 * Whether the given access parent decides Read on objects of the given type: it delegates Read
	 * itself or an operation implying Read.
	 */
	private boolean readsThroughParent(TLClass type, AccessParentFunction accessParent) {
		if (accessParent.delegates(SimpleBoundCommandGroup.READ)) {
			return true;
		}
		for (BoundCommandGroup implying : _accessRights.getOperations(type).getImplying(SimpleBoundCommandGroup.READ)) {
			if (accessParent.delegates(implying)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Reports every operation granted on the given type that its access parent decides alone, see
	 * {@link ModelAccessRights#getIgnoredRoles(TLClass, BoundCommandGroup)}.
	 */
	private void addIgnoredGrantFindings(TLClass type, List<CoverageFinding> findings) {
		for (BoundCommandGroup operation : sortedById(_accessRights.getOperations(type).getOperations())) {
			Set<BoundedRole> ignored = _accessRights.getIgnoredRoles(type, operation);
			if (!ignored.isEmpty()) {
				findings.add(CoverageFinding.ignoredGrant(type, operation, ignored));
			}
		}
	}

	/**
	 * Whether the given type grants an operation it decides itself, see
	 * {@link AccessParentFunction#decidesItself(AccessParentFunction, BoundCommandGroup)}.
	 */
	private boolean hasOwnGrant(TLClass type, AccessParentFunction accessParent) {
		for (BoundCommandGroup operation : _accessRights.getOperations(type).getOperations()) {
			if (AccessParentFunction.decidesItself(accessParent, operation)
				&& !_accessRights.getAllowedRoles(type, operation).isEmpty()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Reports every operation whose grant names a role that cannot be delivered where the operation
	 * is checked, see {@link #isLiveGrant(TLClass, BoundCommandGroup, BoundedRole)}.
	 *
	 * @param accessParent
	 *        The access parent deciding some operations of the type, <code>null</code> when the
	 *        type decides all of them itself.
	 */
	private void addDeadGrantFindings(TLClass type, AccessParentFunction accessParent,
			List<CoverageFinding> findings) {
		for (BoundCommandGroup operation : _operations) {
			if (SimpleBoundCommandGroup.DISCOVER.equals(operation)
				|| !AccessParentFunction.decidesItself(accessParent, operation)) {
				// Its roles are derived; every source grant is already checked on its own.
				continue;
			}
			Set<BoundedRole> granted = getGrantedRoles(type, operation);
			if (granted.isEmpty()) {
				continue;
			}
			Set<BoundedRole> dead = new LinkedHashSet<>();
			for (BoundedRole role : granted) {
				if (!isLiveGrant(type, operation, role)) {
					dead.add(role);
				}
			}
			if (dead.isEmpty()) {
				continue;
			}
			if (SimpleBoundCommandGroup.CREATE.equals(operation)) {
				findings.add(CoverageFinding.deadCreateGrant(type, dead, createContextTypes(type)));
			} else {
				findings.add(CoverageFinding.deadGrant(type, operation, dead));
			}
		}
	}

	/**
	 * Reports every operation declared for the given type that no role holds on it.
	 *
	 * <p>
	 * A built-in operation is not reported: a type may well be read-only. An operation the access
	 * parent of the type decides is not reported either, since the type does not grant it.
	 * </p>
	 *
	 * @param accessParent
	 *        The access parent deciding some operations of the type, <code>null</code> when the
	 *        type decides all of them itself.
	 */
	private void addUngrantedOperationFindings(TLClass type, AccessParentFunction accessParent,
			List<CoverageFinding> findings) {
		for (BoundCommandGroup operation : sortedById(_accessRights.getOperations(type).getOperations())) {
			if (ApplicableOperations.isBuiltIn(operation)
				|| !AccessParentFunction.decidesItself(accessParent, operation)) {
				continue;
			}
			if (_accessRights.getAllowedRoles(type, operation).isEmpty()) {
				findings.add(CoverageFinding.ungrantedOperation(type, operation));
			}
		}
	}

	/**
	 * Reports every operation declared for an application, not a built-in one, that is granted on
	 * the given type although no place of the application checks it there.
	 *
	 * <p>
	 * A grant of an operation is in use when a place checks the operation itself or an operation
	 * it implies on objects of the type: holding an operation means holding every operation it
	 * implies, so a check of an implied operation is a use of the grant. An operation implying a
	 * built-in one is in use, since the model access rights check the built-in operations on every
	 * access. A place checks objects of the type when it checks objects of the type or of one of
	 * its generalizations, when it checks objects delegating to an access parent of the type, when
	 * it checks the security root and the root is of the type, and when it does not declare which
	 * type it checks at all - a privileged script, a form, a command outside a unit of its view -
	 * so that an operation such a place checks is in use on every type.
	 * </p>
	 *
	 * <p>
	 * Only reported when the places checking an operation are known.
	 * </p>
	 *
	 * @param accessParent
	 *        The access parent deciding some operations of the type, <code>null</code> when the
	 *        type decides all of them itself.
	 */
	private void addUncheckedGrantFindings(TLClass type, AccessParentFunction accessParent,
			List<CoverageFinding> findings) {
		if (!_usesKnown) {
			return;
		}
		ApplicableOperations applicable = _accessRights.getOperations(type);
		for (BoundCommandGroup operation : sortedById(applicable.getOperations())) {
			if (ApplicableOperations.isBuiltIn(operation)
				|| !AccessParentFunction.decidesItself(accessParent, operation)) {
				continue;
			}
			Set<BoundedRole> granted = getGrantedRoles(type, operation);
			if (granted.isEmpty()) {
				continue;
			}
			Set<BoundCommandGroup> checked = withImplied(applicable, operation);
			if (checked.stream().anyMatch(ApplicableOperations::isBuiltIn)) {
				continue;
			}
			boolean used = _uses.stream()
				.filter(use -> checked.stream().anyMatch(op -> op.getID().equals(use.operation())))
				.anyMatch(use -> checks(use, type));
			if (!used) {
				findings.add(CoverageFinding.uncheckedGrant(type, operation, granted));
			}
		}
	}

	/**
	 * The given operation and all operations it implies, directly or transitively.
	 */
	private static Set<BoundCommandGroup> withImplied(ApplicableOperations applicable, BoundCommandGroup operation) {
		Set<BoundCommandGroup> result = new LinkedHashSet<>();
		result.add(operation);
		for (BoundCommandGroup other : applicable.getOperations()) {
			if (applicable.getImplying(other).contains(operation)) {
				result.add(other);
			}
		}
		return result;
	}

	/**
	 * Whether the given place checks its operation on objects of the given type.
	 *
	 * @see #addUncheckedGrantFindings(TLClass, AccessParentFunction, List)
	 */
	private boolean checks(OperationUse use, TLClass type) {
		return switch (use.target()) {
			case UNKNOWN -> true;
			case ROOT -> {
				TLObject root = securityRoot();
				yield root != null && root.tType() instanceof TLClass rootType
					&& TLModelUtil.isGeneralization(type, rootType);
			}
			case TYPE -> checksType(use.type(), type, new HashSet<>());
		};
	}

	/**
	 * Whether a check on objects of the given checked type is decided on objects of the given
	 * type: the checked type is a generalization of the type, or an object of the checked type
	 * delegates to an access parent of the type, see
	 * {@link ModelAccessRights#getAccessParentTypes(TLClass)}.
	 *
	 * @param visited
	 *        The checked types inspected along the access parents, guarding against cycles.
	 * @return Also <code>true</code> when the type of an access parent cannot be determined
	 *         statically, so that an undecidable delegation never produces a finding.
	 */
	private boolean checksType(TLClass checked, TLClass type, Set<TLClass> visited) {
		if (!visited.add(checked)) {
			return false;
		}
		if (TLModelUtil.isGeneralization(checked, type)) {
			return true;
		}
		for (TLClass delegating : TLModelUtil.getReflexiveTransitiveSpecializations(checked)) {
			Set<TLClass> parentTypes = _accessRights.getAccessParentTypes(delegating);
			if (parentTypes == null) {
				return true;
			}
			for (TLClass parentType : parentTypes) {
				if (checksType(parentType, type, visited)) {
					return true;
				}
			}
		}
		return false;
	}

	private static List<BoundCommandGroup> sortedById(Collection<BoundCommandGroup> operations) {
		List<BoundCommandGroup> result = new ArrayList<>(operations);
		result.sort(Comparator.comparing(BoundCommandGroup::getID));
		return result;
	}

	/**
	 * The findings about the {@link #_uses}, by the type they are reported for.
	 *
	 * <p>
	 * A place checking a type checks the objects of its concrete specializations, so a finding is
	 * reported for each analyzed one of them it applies to. A type with an access parent is not
	 * checked for the applicability of an operation: the operation is decided on the access
	 * parent, whose type is not always known statically. A place checking the security root is
	 * reported for the type of the root. The uses of a finding are those of the same operation on
	 * the same type, in the order they were given.
	 * </p>
	 */
	private Map<TLClass, List<CoverageFinding>> useFindings() {
		if (_uses.isEmpty()) {
			return Collections.emptyMap();
		}
		Map<TLClass, Map<String, List<OperationUse>>> unknown = new LinkedHashMap<>();
		Map<TLClass, Map<BoundCommandGroup, List<OperationUse>>> inapplicable = new LinkedHashMap<>();
		for (OperationUse use : _uses) {
			BoundCommandGroup operation = use.resolveOperation();
			switch (use.target()) {
				case TYPE -> {
					for (TLClass type : TLModelUtil.getConcreteSpecializations(use.type())) {
						if (!isAnalyzed(type) || isExempt(type)) {
							continue;
						}
						if (operation == null) {
							addUse(unknown, type, use.operation(), use);
						} else if (_accessRights.getAccessParent(type) == null
							&& !_accessRights.appliesTo(type, operation)) {
							addUse(inapplicable, type, operation, use);
						}
					}
				}
				case ROOT -> {
					TLObject root = securityRoot();
					TLClass rootType = root != null && root.tType() instanceof TLClass type ? type : null;
					if (operation == null) {
						addUse(unknown, rootType, use.operation(), use);
					} else if (root != null && !_accessRights.getObjectOperations(root).appliesTo(operation)) {
						addUse(inapplicable, rootType, operation, use);
					}
				}
				case UNKNOWN -> {
					if (operation == null) {
						addUse(unknown, null, use.operation(), use);
					}
				}
			}
		}
		Map<TLClass, List<CoverageFinding>> result = new LinkedHashMap<>();
		inapplicable.forEach((type, byOperation) -> byOperation.forEach((operation, uses) -> result
			.computeIfAbsent(type, unused -> new ArrayList<>())
			.add(CoverageFinding.inapplicableOperation(type, operation, uses))));
		unknown.forEach((type, byOperation) -> byOperation.forEach((operation, uses) -> result
			.computeIfAbsent(type, unused -> new ArrayList<>())
			.add(CoverageFinding.unknownOperation(type, operation, uses))));
		return result;
	}

	private static <K> void addUse(Map<TLClass, Map<K, List<OperationUse>>> uses, TLClass type, K operation,
			OperationUse use) {
		uses.computeIfAbsent(type, unused -> new LinkedHashMap<>())
			.computeIfAbsent(operation, unused -> new ArrayList<>())
			.add(use);
	}

	/**
	 * Whether the given type needs no access definition, see {@link CoverageStatus#EXEMPT}.
	 */
	private boolean isExempt(TLClass type) {
		return _accessRights.getWithoutSecurityOrigin(type) != null || _accessRights.getInternalOrigin(type) != null;
	}

	/**
	 * The security root, the object a place checks when it names no object.
	 *
	 * @return <code>null</code> when the application has none.
	 */
	private static TLObject securityRoot() {
		Object root = SecurityRootObjectProvider.INSTANCE.getSecurityRoot();
		return root instanceof TLObject object ? object : null;
	}

	/**
	 * The roles allowed the given operation on the given type, without those allowed it only
	 * because they are allowed an operation implying it.
	 *
	 * <p>
	 * A role holding an implying operation is left out also when it is granted the given
	 * operation directly: that grant changes nothing as long as the implying operation is granted.
	 * A dead role of an implying operation is therefore reported for that operation alone.
	 * </p>
	 *
	 * @param type
	 *        The type the operation is performed on.
	 * @param operation
	 *        The operation.
	 * @return The roles granted the operation in effect.
	 */
	public Set<BoundedRole> getGrantedRoles(TLClass type, BoundCommandGroup operation) {
		Set<BoundedRole> granted = _accessRights.getAllowedRoles(type, operation);
		Set<BoundCommandGroup> implying = _accessRights.getOperations(type).getImplying(operation);
		if (implying.isEmpty() || granted.isEmpty()) {
			return granted;
		}
		Set<BoundedRole> result = new LinkedHashSet<>(granted);
		for (BoundCommandGroup implyingOperation : implying) {
			if (implyingOperation != operation) {
				result.removeAll(_accessRights.getAllowedRoles(type, implyingOperation));
			}
		}
		return result;
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
	 * <p>
	 * An abstract type, for example the owner of a reference a role parent rule navigates
	 * backwards, has no objects of its own: the rules of the {@link ElementAccessManager} are
	 * registered for its concrete specializations only. At runtime the concrete type of the role
	 * parent decides, therefore the role counts as delivered on an abstract type when it is
	 * delivered on at least one of its concrete specializations.
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
		if (type.isAbstract()) {
			for (TLClass specialization : TLModelUtil.getConcreteSpecializations(type)) {
				if (canDeliver(specialization, role, visited)) {
					return true;
				}
			}
			return false;
		}
		if (_accessManager.canHaveRole(type, role) || isAssignedDirectly(type, role)) {
			return true;
		}
		Collection<NavigationRule> parentRules = _accessManager.getRoleParentRules(type);
		if (parentRules.isEmpty()) {
			return false;
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
	 * The types an object is created in, by the type of the object.
	 *
	 * <p>
	 * The containers are the concrete classes whose compositions, as they are in effect on the
	 * class, hold the type: a composition holds objects of its target type and of all its
	 * specializations. A specialization overriding a composition with a narrower target type holds
	 * only that type, so the compositions are taken from the parts of each concrete class, not from
	 * the class declaring them.
	 * </p>
	 *
	 * <p>
	 * A derived composition is left out, as for {@link #compositeReferences(TLModel)}: no object is
	 * created in it.
	 * </p>
	 */
	private static Map<TLClass, Set<TLClass>> containerTypes(TLModel model) {
		Map<TLClass, Set<TLClass>> result = new HashMap<>();
		for (TLClass container : TLModelUtil.getAllGlobalClasses(model)) {
			if (container.isAbstract()) {
				continue;
			}
			for (TLStructuredTypePart part : container.getAllParts()) {
				if (!(part instanceof TLReference reference) || !reference.isComposite() || reference.isDerived()) {
					continue;
				}
				if (!(reference.getType() instanceof TLClass contentType)) {
					continue;
				}
				for (TLClass content : TLModelUtil.getReflexiveTransitiveSpecializations(contentType)) {
					result.computeIfAbsent(content, unused -> new LinkedHashSet<>()).add(container);
				}
			}
		}
		return result;
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
