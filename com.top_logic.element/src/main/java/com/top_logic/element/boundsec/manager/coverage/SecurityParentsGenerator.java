/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.element.boundsec.manager.coverage;

import java.util.Collection;
import java.util.Comparator;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.element.boundsec.manager.ElementAccessManager;
import com.top_logic.element.boundsec.manager.rule.config.SecurityParentsConfig;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.autoconf.InAppServiceConfigStore;

/**
 * Turns the security parents proposed by the {@link SecurityCoverageAnalysis} into the
 * configuration that defines them.
 *
 * <p>
 * A type without a role source that is contained in exactly one composition gets a security parent
 * rule navigating that composition backwards. This generator collects those rules and renders them
 * as the XML that belongs into the access manager configuration, for the developer to add them
 * there like any other rule.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SecurityParentsGenerator {

	/** Tag name of a {@link SecurityParentsConfig} in the access manager configuration. */
	public static final String SECURITY_PARENTS_TAG = ElementAccessManager.Config.SECURITY_PARENTS;

	/**
	 * Collects the security parent rules proposed by the given analysis result.
	 *
	 * @param coverage
	 *        The result of {@link SecurityCoverageCheck#analyze()}.
	 * @return The proposed rules, ordered by the qualified name of the type they apply to.
	 *
	 * @see FindingKind#SUGGESTED_PARENT
	 */
	public SecurityParentsConfig collect(Collection<TypeCoverage> coverage) {
		SecurityParentsConfig result = TypedConfiguration.newConfigItem(SecurityParentsConfig.class);
		coverage.stream()
			.sorted(Comparator.comparing(entry -> TLModelUtil.qualifiedName(entry.type())))
			.flatMap(entry -> entry.findings(FindingKind.SUGGESTED_PARENT).stream())
			.map(CoverageFinding::getSuggestedRule)
			.forEach(rule -> result.getRules().add(TypedConfiguration.copy(rule)));
		return result;
	}

	/**
	 * Renders the given rules as the XML that belongs into the access manager configuration.
	 *
	 * @param rules
	 *        The rules to render.
	 * @return The {@link #SECURITY_PARENTS_TAG} element, pretty printed.
	 */
	public String toXml(SecurityParentsConfig rules) {
		return InAppServiceConfigStore.toXml(SECURITY_PARENTS_TAG, rules, InAppServiceConfigStore.LAYER_ONTO_BASE);
	}

}
