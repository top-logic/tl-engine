/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.element.boundsec.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.map.MultiMaps;
import com.top_logic.element.boundsec.manager.rule.NavigationRule;
import com.top_logic.element.boundsec.manager.rule.PathElement;
import com.top_logic.element.boundsec.manager.rule.config.NavigationRuleConfig;
import com.top_logic.model.TLClass;

/**
 * The class {@link NavigationRulesImporter} TODO dbu 
 * 
 * @author    <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class NavigationRulesImporter extends AbstractNavigationRulesImporter {

	/**
	 * Holds the imported rules indexed by their {@link TLClass}.
	 */
	private Map<TLClass, Collection<NavigationRule>> _rules = new HashMap<>();

	@Override
	protected String getBase() {
		return StringServices.EMPTY_STRING;
	}

	/**
	 * Loads the given {@link NavigationRuleConfig}.
	 */
	public void loadRule(NavigationRuleConfig rule) {
		int numberProblems = getProblems().size();

		initVariables(rule);

		List<PathElement> path = resolvePath(rule);

		if (numberProblems < getProblems().size()) {
			// new problems occurred
			return;
		}
		addRule(new NavigationRule(getMetaElement(), isInherit(), path));
	}

	void addRule(NavigationRule rule) {
		TLClass theME = rule.getMetaElement();
		MultiMaps.add(_rules, theME, rule, ArrayList::new);
	}

	/**
	 * Creates a {@link NavigationRulesImporter} that has loaded the given {@link NavigationRule}s.
	 * 
	 * @param roleRules
	 *        The {@link NavigationRuleConfig navigation rules configuration} to create
	 *        {@link NavigationRule} from.
	 */
	public static NavigationRulesImporter loadRules(Iterable<? extends NavigationRuleConfig> roleRules) {
		NavigationRulesImporter importer = new NavigationRulesImporter();
		roleRules.forEach(importer::loadRule);
		return importer;
	}

	/**
	 * This method returns the rules.
	 * 
	 * @return Returns the rules.
	 */
	public Map<TLClass, Collection<NavigationRule>> getRules() {
		return _rules;
	}
}

