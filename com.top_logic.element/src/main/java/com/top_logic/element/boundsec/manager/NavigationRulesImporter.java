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

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.map.MultiMaps;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.boundsec.manager.rule.IdentityPathElement;
import com.top_logic.element.boundsec.manager.rule.NavigationRule;
import com.top_logic.element.boundsec.manager.rule.PathElement;
import com.top_logic.element.boundsec.manager.rule.config.NavigationRuleConfig;
import com.top_logic.model.TLClass;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.error.TopLogicException;

/**
 * Imports {@link NavigationRuleConfig}s into {@link NavigationRule}s.
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class NavigationRulesImporter {

	private final List<ResKey> _problems = new ArrayList<>();

	/**
	 * Holds the imported rules indexed by their {@link TLClass}.
	 */
	private final Map<TLClass, Collection<NavigationRule>> _rules = new HashMap<>();

	/**
	 * Registers the given problem.
	 */
	void addProblem(ResKey problem) {
		_problems.add(problem);
	}

	/**
	 * Returns the modifiable list of problems.
	 */
	public List<ResKey> getProblems() {
		return _problems;
	}

	/**
	 * Returns the modifiable {@link Map} containing the parsed {@link NavigationRule}s.
	 */
	public Map<TLClass, Collection<NavigationRule>> getRules() {
		return _rules;
	}

	private TLClass getMetaElement(String metaElementName) {
		if (StringServices.isEmpty(metaElementName)) {
			return null;
		}
		try {
			return (TLClass) TLModelUtil.findType(metaElementName);
		} catch (TopLogicException ex) {
			addProblem(I18NConstants.UNKNOWN_META_ELEMENT.fill(metaElementName));
			return null;
		}
	}

	/**
	 * Loads the given {@link NavigationRuleConfig}.
	 */
	public void loadRule(NavigationRuleConfig rule) {
		int numberProblems = _problems.size();

		boolean inherit = rule.isInherit();
		TLClass metaElement = getMetaElement(rule.getMetaElement());
		if (rule.getMetaElement() == null) {
			addProblem(I18NConstants.NO_META_ELEMENT_DECLARED);
		}
		if (metaElement != null && metaElement.isAbstract() && !inherit) {
			addProblem(I18NConstants.ABSTRACT_TYPE_WITHOUT_INHERITANCE.fill(rule.getMetaElement()));
		}

		List<PathElement> path = new ArrayList<>();
		for (PolymorphicConfiguration<? extends PathElement> pathElementConf : rule.getPathElements()) {
			try {
				path.add(TypedConfigUtil.createInstance(pathElementConf));
			} catch (I18NRuntimeException ex) {
				Logger.error("Problem creating path element.", ex, NavigationRulesImporter.class);
				addProblem(ex.getErrorKey());
			}
		}
		if (path.isEmpty()) {
			path.add(new IdentityPathElement());
		}

		if (numberProblems < _problems.size()) {
			// new problems occurred
			return;
		}
		addRule(new NavigationRule(metaElement, inherit, path));
	}

	void addRule(NavigationRule rule) {
		MultiMaps.add(_rules, rule.getMetaElement(), rule, ArrayList::new);
	}

	/**
	 * Creates a {@link NavigationRulesImporter} that has loaded the given {@link NavigationRule}s.
	 *
	 * @param rules
	 *        The {@link NavigationRuleConfig navigation rules configuration} to create
	 *        {@link NavigationRule} from.
	 */
	public static NavigationRulesImporter loadRules(Iterable<? extends NavigationRuleConfig> rules) {
		NavigationRulesImporter importer = new NavigationRulesImporter();
		rules.forEach(importer::loadRule);
		return importer;
	}
}
