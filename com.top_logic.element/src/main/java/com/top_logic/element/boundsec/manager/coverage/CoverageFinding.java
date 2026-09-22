/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.element.boundsec.manager.coverage;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.top_logic.basic.util.ResKey;
import com.top_logic.element.boundsec.manager.rule.config.NavigationRuleConfig;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLReference;
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

	private final List<TLReference> _containerReferences;

	private final NavigationRuleConfig _suggestedRule;

	private final boolean _rootFallbackActive;

	private CoverageFinding(FindingKind kind, TLClass type, ResKey message, BoundCommandGroup operation,
			Set<BoundedRole> roles, List<TLReference> containerReferences, NavigationRuleConfig suggestedRule,
			boolean rootFallbackActive) {
		_kind = Objects.requireNonNull(kind);
		_type = Objects.requireNonNull(type);
		_message = Objects.requireNonNull(message);
		_operation = operation;
		_roles = roles;
		_containerReferences = containerReferences;
		_suggestedRule = suggestedRule;
		_rootFallbackActive = rootFallbackActive;
	}

	/**
	 * Creates a {@link FindingKind#NO_ROLE_SOURCE} finding.
	 *
	 * @param type
	 *        The type without a role source.
	 * @param rootFallbackActive
	 *        Whether objects without a security parent fall back to the security root.
	 * @return The new finding.
	 */
	public static CoverageFinding noRoleSource(TLClass type, boolean rootFallbackActive) {
		ResKey message = rootFallbackActive
			? I18NConstants.NO_ROLE_SOURCE_ROOT_FALLBACK__TYPE.fill(TLModelUtil.qualifiedName(type))
			: I18NConstants.NO_ROLE_SOURCE__TYPE.fill(TLModelUtil.qualifiedName(type));
		return new CoverageFinding(FindingKind.NO_ROLE_SOURCE, type, message, null, Collections.emptySet(),
			Collections.emptyList(), null, rootFallbackActive);
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
			Collections.emptyList(), null, false);
	}

	/**
	 * Creates a {@link FindingKind#DEAD_GRANT} finding.
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
			.fill(TLModelUtil.qualifiedName(type), operation.getID(), roleNames);
		return new CoverageFinding(FindingKind.DEAD_GRANT, type, message, operation,
			Collections.unmodifiableSet(roles), Collections.emptyList(), null, false);
	}

	/**
	 * Creates a {@link FindingKind#SUGGESTED_PARENT} finding.
	 *
	 * @param type
	 *        The type without a role source.
	 * @param containerReference
	 *        The single composition the type is contained in.
	 * @param suggestedRule
	 *        The security parent rule navigating the composition backwards.
	 * @return The new finding.
	 */
	public static CoverageFinding suggestedParent(TLClass type, TLReference containerReference,
			NavigationRuleConfig suggestedRule) {
		ResKey message = I18NConstants.SUGGESTED_PARENT__TYPE_REFERENCE
			.fill(TLModelUtil.qualifiedName(type), TLModelUtil.qualifiedName(containerReference));
		return new CoverageFinding(FindingKind.SUGGESTED_PARENT, type, message, null, Collections.emptySet(),
			List.of(containerReference), Objects.requireNonNull(suggestedRule), false);
	}

	/**
	 * Creates a {@link FindingKind#AMBIGUOUS_PARENT} finding.
	 *
	 * @param type
	 *        The type without a role source.
	 * @param containerReferences
	 *        The compositions the type is contained in.
	 * @return The new finding.
	 */
	public static CoverageFinding ambiguousParent(TLClass type, List<TLReference> containerReferences) {
		String referenceNames = containerReferences.stream()
			.map(TLModelUtil::qualifiedName)
			.collect(Collectors.joining(DETAIL_SEPARATOR));
		ResKey message = I18NConstants.AMBIGUOUS_PARENT__TYPE_REFERENCES
			.fill(TLModelUtil.qualifiedName(type), referenceNames);
		return new CoverageFinding(FindingKind.AMBIGUOUS_PARENT, type, message, null, Collections.emptySet(),
			List.copyOf(containerReferences), null, false);
	}

	/**
	 * The classification of this finding.
	 */
	public FindingKind getKind() {
		return _kind;
	}

	/**
	 * The type this finding was reported for.
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
	 * The operation whose grant is dead.
	 *
	 * @return <code>null</code> for a finding other than {@link FindingKind#DEAD_GRANT}.
	 */
	public BoundCommandGroup getOperation() {
		return _operation;
	}

	/**
	 * The granted roles that cannot be delivered on {@link #getType()}.
	 *
	 * @return An empty set for a finding other than {@link FindingKind#DEAD_GRANT}.
	 */
	public Set<BoundedRole> getRoles() {
		return _roles;
	}

	/**
	 * The compositions {@link #getType()} is contained in.
	 *
	 * @return The single candidate for a {@link FindingKind#SUGGESTED_PARENT} finding, all
	 *         candidates for an {@link FindingKind#AMBIGUOUS_PARENT} finding, an empty list
	 *         otherwise.
	 */
	public List<TLReference> getContainerReferences() {
		return _containerReferences;
	}

	/**
	 * The security parent rule proposed for {@link #getType()}.
	 *
	 * <p>
	 * The rule is meant to be added to the access manager configuration by the developer. It is
	 * never applied implicitly.
	 * </p>
	 *
	 * @return <code>null</code> for a finding other than {@link FindingKind#SUGGESTED_PARENT}.
	 */
	public NavigationRuleConfig getSuggestedRule() {
		return _suggestedRule;
	}

	/**
	 * Whether objects without a security parent fall back to the security root.
	 *
	 * @see com.top_logic.tool.boundsec.BoundHelper#useDefaultObject()
	 */
	public boolean isRootFallbackActive() {
		return _rootFallbackActive;
	}

	@Override
	public String toString() {
		return _kind + "(" + TLModelUtil.qualifiedName(_type) + ")";
	}

}
