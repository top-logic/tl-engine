/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.boundsec.manager.coverage;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.top_logic.basic.util.ResKey;
import com.top_logic.model.TLClass;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.wrap.BoundedRole;

/**
 * A single gap in the model-based access definition of a type, reported by the
 * {@link SecurityCoverageAnalysis}.
 * 
 * <p>
 * A finding carries its {@link #getKind() kind}, a {@link #getMessage() message} describing the gap
 * to the developer, and the details that belong to the kind. A detail that the kind does not define
 * is <code>null</code> or empty.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class CoverageFinding {

	/** Separator between the entries of a list rendered into a {@link #getMessage() message}. */
	private static final String DETAIL_SEPARATOR = ", ";

	private final FindingKind _kind;

	private final TLClass _type;

	private final ResKey _message;

	private final BoundCommandGroup _operation;

	private final Set<BoundedRole> _roles;

	private final List<String> _ruleIds;

	private final String _operationName;

	private final List<OperationUse> _uses;

	private CoverageFinding(FindingKind kind, TLClass type, ResKey message, BoundCommandGroup operation,
			Set<BoundedRole> roles, List<String> ruleIds) {
		this(kind, type, message, operation, operation == null ? null : operation.getID(), roles, ruleIds,
			Collections.emptyList());
	}

	private CoverageFinding(FindingKind kind, TLClass type, ResKey message, BoundCommandGroup operation,
			String operationName, Set<BoundedRole> roles, List<String> ruleIds, List<OperationUse> uses) {
		_kind = Objects.requireNonNull(kind);
		_type = type;
		_message = Objects.requireNonNull(message);
		_operation = operation;
		_operationName = operationName;
		_roles = roles;
		_ruleIds = ruleIds;
		_uses = uses;
	}

	/**
	 * Creates a {@link FindingKind#NO_ROLE_SOURCE} finding.
	 *
	 * @param type
	 *        The type without a role source.
	 * @return The new finding.
	 */
	public static CoverageFinding noRoleSource(TLClass type) {
		ResKey message = I18NConstants.NO_ROLE_SOURCE__TYPE.fill(TLModelUtil.qualifiedName(type));
		return new CoverageFinding(FindingKind.NO_ROLE_SOURCE, type, message, null, Collections.emptySet(),
			Collections.emptyList());
	}

	/**
	 * Creates a {@link FindingKind#NO_READ_GRANT} finding.
	 * 
	 * @param type
	 *        The type nobody may read.
	 * @return The new finding.
	 */
	public static CoverageFinding noReadGrant(TLClass type) {
		ResKey message = I18NConstants.NO_READ_GRANT__TYPE.fill(TLModelUtil.qualifiedName(type));
		return new CoverageFinding(FindingKind.NO_READ_GRANT, type, message, null, Collections.emptySet(),
			Collections.emptyList());
	}

	/**
	 * Creates a {@link FindingKind#DEAD_GRANT} finding.
	 * 
	 * <p>
	 * A role counts as deliverable when a rule computes it on the type or on one of its role
	 * parents, and equally when it is assigned directly on an object of one of those types. Only a
	 * role that neither a rule nor an assignment can put on an object belongs into this finding.
	 * </p>
	 * 
	 * @param type
	 *        The type carrying the grant.
	 * @param operation
	 *        The granted operation.
	 * @param roles
	 *        The granted roles that cannot be delivered on the type.
	 * @return The new finding.
	 */
	public static CoverageFinding deadGrant(TLClass type, BoundCommandGroup operation, Set<BoundedRole> roles) {
		String roleNames = roles.stream().map(BoundedRole::getName).sorted().collect(Collectors.joining(DETAIL_SEPARATOR));
		ResKey message = I18NConstants.DEAD_GRANT__TYPE_OPERATION_ROLES
			.fill(TLModelUtil.qualifiedName(type), operationLabel(operation), roleNames);
		return new CoverageFinding(FindingKind.DEAD_GRANT, type, message, operation,
			Collections.unmodifiableSet(roles), Collections.emptyList());
	}

	/**
	 * Creates a {@link FindingKind#SHADOWED_RULES} finding.
	 * 
	 * @param type
	 *        The type with an access parent.
	 * @param ruleIds
	 *        The ids of the rules applying to the type that the access parent shadows.
	 * @return The new finding.
	 */
	public static CoverageFinding shadowedRules(TLClass type, List<String> ruleIds) {
		ResKey message = I18NConstants.SHADOWED_RULES__TYPE_RULES
			.fill(TLModelUtil.qualifiedName(type), String.join(DETAIL_SEPARATOR, ruleIds));
		return new CoverageFinding(FindingKind.SHADOWED_RULES, type, message, null, Collections.emptySet(),
			List.copyOf(ruleIds));
	}

	/**
	 * Creates a {@link FindingKind#UNGRANTED_OPERATION} finding.
	 *
	 * @param type
	 *        The type the operation applies to.
	 * @param operation
	 *        The operation declared for the type that no role holds on it.
	 * @return The new finding.
	 */
	public static CoverageFinding ungrantedOperation(TLClass type, BoundCommandGroup operation) {
		ResKey message = I18NConstants.UNGRANTED_OPERATION__TYPE_OPERATION
			.fill(TLModelUtil.qualifiedName(type), operationLabel(operation));
		return new CoverageFinding(FindingKind.UNGRANTED_OPERATION, type, message, operation,
			Collections.emptySet(), Collections.emptyList());
	}

	/**
	 * Creates a {@link FindingKind#UNCHECKED_GRANT} finding.
	 *
	 * @param type
	 *        The type carrying the grant.
	 * @param operation
	 *        The granted operation nothing checks on the type.
	 * @param roles
	 *        The roles the operation is granted to.
	 * @return The new finding.
	 */
	public static CoverageFinding uncheckedGrant(TLClass type, BoundCommandGroup operation, Set<BoundedRole> roles) {
		String roleNames =
			roles.stream().map(BoundedRole::getName).sorted().collect(Collectors.joining(DETAIL_SEPARATOR));
		ResKey message = I18NConstants.UNCHECKED_GRANT__TYPE_OPERATION_ROLES
			.fill(TLModelUtil.qualifiedName(type), operationLabel(operation), roleNames);
		return new CoverageFinding(FindingKind.UNCHECKED_GRANT, type, message, operation,
			Collections.unmodifiableSet(new LinkedHashSet<>(roles)), Collections.emptyList());
	}

	/**
	 * Creates a {@link FindingKind#INAPPLICABLE_OPERATION} finding.
	 *
	 * @param type
	 *        The type an object of which the operation is checked on, the type of the security
	 *        root for uses checking the security root.
	 * @param operation
	 *        The operation that does not apply to the type.
	 * @param uses
	 *        The places checking the operation on an object of the type.
	 * @return The new finding.
	 */
	public static CoverageFinding inapplicableOperation(TLClass type, BoundCommandGroup operation,
			List<OperationUse> uses) {
		ResKey message = I18NConstants.INAPPLICABLE_OPERATION__TYPE_OPERATION_USERS
			.fill(TLModelUtil.qualifiedName(type), operationLabel(operation), users(uses));
		return new CoverageFinding(FindingKind.INAPPLICABLE_OPERATION, type, message, operation, operation.getID(),
			Collections.emptySet(), Collections.emptyList(), List.copyOf(uses));
	}

	/**
	 * Creates a {@link FindingKind#UNKNOWN_OPERATION} finding.
	 *
	 * @param type
	 *        The type an object of which the operation is checked on, <code>null</code> when the
	 *        uses do not declare it.
	 * @param operationName
	 *        The name that is no registered command group.
	 * @param uses
	 *        The places checking the operation.
	 * @return The new finding.
	 */
	public static CoverageFinding unknownOperation(TLClass type, String operationName, List<OperationUse> uses) {
		ResKey message = I18NConstants.UNKNOWN_OPERATION__OPERATION_USERS.fill(operationName, users(uses));
		return new CoverageFinding(FindingKind.UNKNOWN_OPERATION, type, message, null, operationName,
			Collections.emptySet(), Collections.emptyList(), List.copyOf(uses));
	}

	/**
	 * The label of the given operation, as the administration shows it: the label of its command
	 * group, its name where the command group has none.
	 *
	 * @param operation
	 *        The operation.
	 * @return The internationalized label.
	 */
	public static ResKey operationLabel(BoundCommandGroup operation) {
		return ResKey.fallback(
			com.top_logic.tool.boundsec.simple.I18NConstants.COMMAND_GROUP_NAMES.key(operation.getID()),
			ResKey.text(operation.getID()));
	}

	/**
	 * The label of the {@link #getOperationName() operation} this finding is about.
	 *
	 * @return The label of the command group, the bare name of an
	 *         {@link FindingKind#UNKNOWN_OPERATION unknown operation}, <code>null</code> for a
	 *         finding about no operation.
	 */
	public ResKey getOperationLabel() {
		if (_operation != null) {
			return operationLabel(_operation);
		}
		return _operationName == null ? null : ResKey.text(_operationName);
	}

	/**
	 * The descriptions of the given uses as a single text.
	 */
	private static String users(List<OperationUse> uses) {
		return uses.stream().map(OperationUse::user).collect(Collectors.joining(DETAIL_SEPARATOR));
	}

	/**
	 * The classification of this finding.
	 */
	public FindingKind getKind() {
		return _kind;
	}

	/**
	 * The type this finding was reported for.
	 *
	 * @return <code>null</code> only for a {@link FindingKind#UNKNOWN_OPERATION} of places that
	 *         do not declare the type they check.
	 */
	public TLClass getType() {
		return _type;
	}

	/**
	 * The message describing this finding to the developer.
	 */
	public ResKey getMessage() {
		return _message;
	}

	/**
	 * The operation the finding is about: the one whose grant is dead, which is not granted, or
	 * which does not apply.
	 * 
	 * @return <code>null</code> for a finding of another kind, and for an
	 *         {@link FindingKind#UNKNOWN_OPERATION}, which names no registered command group, see
	 *         {@link #getOperationName()}.
	 */
	public BoundCommandGroup getOperation() {
		return _operation;
	}

	/**
	 * The name of the operation the finding is about.
	 *
	 * @return <code>null</code> for a finding about no operation.
	 * @see #getOperation()
	 */
	public String getOperationName() {
		return _operationName;
	}

	/**
	 * The places checking the operation of a {@link FindingKind#INAPPLICABLE_OPERATION} or an
	 * {@link FindingKind#UNKNOWN_OPERATION}.
	 *
	 * @return An empty list for a finding of another kind.
	 */
	public List<OperationUse> getUses() {
		return _uses;
	}

	/**
	 * The granted roles that cannot be delivered on {@link #getType()}, neither by a rule nor by a
	 * direct role assignment, or, for a {@link FindingKind#UNCHECKED_GRANT}, the roles granted the
	 * operation nothing checks.
	 * 
	 * @return An empty set for a finding other than {@link FindingKind#DEAD_GRANT} and
	 *         {@link FindingKind#UNCHECKED_GRANT}.
	 */
	public Set<BoundedRole> getRoles() {
		return _roles;
	}

	/**
	 * The ids of the rules the access parent of {@link #getType()} shadows.
	 * 
	 * @return An empty list for a finding other than {@link FindingKind#SHADOWED_RULES}.
	 */
	public List<String> getRuleIds() {
		return _ruleIds;
	}

	@Override
	public String toString() {
		return _kind + "(" + (_type == null ? "?" : TLModelUtil.qualifiedName(_type))
			+ (_operationName == null ? "" : ", " + _operationName) + ")";
	}

}
