/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.boundsec.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.top_logic.basic.col.map.MultiMaps;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.boundsec.manager.ElementAccessImporter.ApplicationRoleHolder;
import com.top_logic.element.boundsec.manager.rule.PathElement;
import com.top_logic.element.boundsec.manager.rule.RoleProvider;
import com.top_logic.element.boundsec.manager.rule.RoleProvider.Type;
import com.top_logic.element.boundsec.manager.rule.RoleRule;
import com.top_logic.element.boundsec.manager.rule.config.RoleRuleConfig;
import com.top_logic.element.boundsec.manager.rule.config.RoleRulesConfig;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLStructuredType;
import com.top_logic.tool.boundsec.wrap.BoundedRole;

/**
 * @author    <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class RoleRulesImporter extends AbstractNavigationRulesImporter {

	/**
	 * Holds the imported rules indexed by their {@link TLStructuredType}.
	 */
	private Map<TLClass, Collection<RoleProvider>> _rules = new HashMap<>();

	private ApplicationRoleHolder _roleProvider;

	private final KnowledgeBase _kb = PersistencyLayer.getKnowledgeBase();

	private TLClass _sourceMetaElement;

	private ResKey _resKey;

	private String _base;

    /**
     * Creates a {@link RoleRulesImporter}.
     *
     */
    public RoleRulesImporter(ApplicationRoleHolder aRoleProvieder) {
		this._roleProvider = aRoleProvieder;
    }

    void addRule(RoleRule aRule) {
        TLClass theME     = aRule.getMetaElement();
		MultiMaps.add(_rules, theME, aRule, ArrayList::new);
	}

	/**
	 * Creates a {@link RoleRulesImporter} that has loaded the given {@link RoleRulesConfig}.
	 * 
	 * @param roleRules
	 *        The {@link RoleRulesConfig role rules configuration} to create {@link RoleRule} from.
	 */
	public static RoleRulesImporter loadRules(ElementAccessManager accessManager, RoleRulesConfig roleRules) {
		return loadRules(accessManager, roleRules.getRules());
	}

	/**
	 * Creates a {@link RoleRulesImporter} that has loaded the given {@link RoleRuleConfig}s.
	 * 
	 * @param roleRules
	 *        The {@link RoleRulesConfig role rules configuration} to create {@link RoleRule} from.
	 */
	public static RoleRulesImporter loadRules(ElementAccessManager accessManager,
			Iterable<? extends RoleRuleConfig> roleRules) {
		RoleRulesImporter importer = new RoleRulesImporter(new ApplicationRoleHolder(accessManager));
		roleRules.forEach(importer::loadRule);
		return importer;
	}

	/**
	 * Loads the given {@link RoleRuleConfig}.
	 */
	public void loadRule(RoleRuleConfig roleRule) {
		int numberProblems = getProblems().size();
		_base = roleRule.getBase();
		_resKey = roleRule.getResKey();

		initVariables(roleRule);

		_sourceMetaElement = getMetaElement(roleRule.getSourceMetaElement());
		if (_sourceMetaElement != null && _sourceMetaElement.isAbstract()) {
			addProblem(I18NConstants.ABSTRACT_SOURCE_TYPE.fill(roleRule.getSourceMetaElement()));
		}
		Collection<BoundedRole> roles = getRoles(roleRule.getRole());
		Collection<BoundedRole> sourceRoles = getRoles(roleRule.getSourceRole());

		List<PathElement> path = resolvePath(roleRule);

		if (numberProblems < getProblems().size()) {
			// new problems occurred
			return;
		}
		if (sourceRoles.isEmpty()) {
			createRules(roleRule, null, roles, path);
		} else {
			for (BoundedRole sourceRole : sourceRoles) {
				createRules(roleRule, sourceRole, roles, path);
			}
		}

	}

	private void createRules(RoleRuleConfig roleRuleConfig, BoundedRole sourceRole, Collection<BoundedRole> roles,
			List<PathElement> path) {
		for (BoundedRole role : roles) {
			if (Type.inheritance.equals(roleRuleConfig.getType())) {
				addRule(
					new RoleRule(getMetaElement(), _sourceMetaElement, isInherit(), role, sourceRole, path, _base,
						_resKey));
			} else {
				addRule(
					new RoleRule(getMetaElement(), isInherit(), role, path, _base, _resKey));
			}
		}
	}

	private Collection<BoundedRole> getRoles(List<String> roleNames) {
		if (roleNames.isEmpty()) {
			return Collections.emptyList();
		}
		ArrayList<BoundedRole> result = new ArrayList<>(roleNames.size());
		for (String roleName : roleNames) {
			BoundedRole role = _roleProvider.getRole(roleName);

			Set<String> availableRoles = Collections.emptySet();
			if (role == null) {
				role = BoundedRole.getRoleByName(_kb, roleName);
			}

			if (role != null) {
				result.add(role);
			} else {
				addProblem(I18NConstants.ROLE_RULES_PROBLEM_UNKNOWN_ROLE
					.fill(roleName,
						availableRoles.stream().collect(Collectors.joining(", ")),
						BoundedRole.getAll().stream().map(x -> x.getName()).collect(Collectors.joining(", "))));
			}

		}
		return result;
	}

	@Override
	protected String getBase() {
		return _base;
	}

	/**
	 * Returns the modifiable {@link Map} containing the parsed {@link RoleProvider}.
	 */
	public Map<TLClass, Collection<RoleProvider>> getRules() {
		return _rules;
	}

}

