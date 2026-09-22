/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.element.boundsec.manager.coverage;

import java.util.List;
import java.util.Set;

import com.top_logic.element.boundsec.manager.rule.NavigationRule;
import com.top_logic.element.boundsec.manager.rule.RoleProvider;
import com.top_logic.model.TLClass;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.boundsec.wrap.BoundedRole;

/**
 * The access definition of a single type, as computed by the {@link SecurityCoverageAnalysis}.
 *
 * @param type
 *        The concrete type this result describes.
 * @param withoutSecurity
 *        Whether the type is excluded from access control. Such a type never has a finding.
 * @param readRoles
 *        The roles granted {@link SimpleBoundCommandGroup#READ} on the type.
 * @param roleRules
 *        The role rules applying to objects of the type, inherited rules included.
 * @param securityParentRules
 *        The security parent rules applying to objects of the type, inherited rules included.
 * @param findings
 *        The gaps found in the access definition of the type, empty when it is
 *        {@link CoverageStatus#COVERED}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public record TypeCoverage(TLClass type, boolean withoutSecurity, Set<BoundedRole> readRoles,
		List<RoleProvider> roleRules, List<NavigationRule> securityParentRules, List<CoverageFinding> findings) {

	/**
	 * The overall result for {@link #type()}.
	 */
	public CoverageStatus status() {
		return findings.isEmpty() ? CoverageStatus.COVERED : CoverageStatus.INCOMPLETE;
	}

	/**
	 * The findings of the given kind.
	 *
	 * @param kind
	 *        The kind to filter for.
	 * @return The matching findings, in the order they were reported.
	 */
	public List<CoverageFinding> findings(FindingKind kind) {
		return findings.stream().filter(finding -> finding.getKind() == kind).toList();
	}

	/**
	 * Whether a finding of the given kind was reported.
	 *
	 * @param kind
	 *        The kind to look for.
	 */
	public boolean hasFinding(FindingKind kind) {
		return findings.stream().anyMatch(finding -> finding.getKind() == kind);
	}

	@Override
	public String toString() {
		return TLModelUtil.qualifiedName(type) + ": " + status() + " " + findings;
	}

}
