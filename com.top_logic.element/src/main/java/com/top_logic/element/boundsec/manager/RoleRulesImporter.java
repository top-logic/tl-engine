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
import java.util.function.Function;
import java.util.stream.Collectors;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.map.MultiMaps;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.boundsec.manager.rule.DefaultRoleRule;
import com.top_logic.element.boundsec.manager.rule.IdentityPathElement;
import com.top_logic.element.boundsec.manager.rule.PathElement;
import com.top_logic.element.boundsec.manager.rule.RoleProvider;
import com.top_logic.element.boundsec.manager.rule.RoleProvider.Type;
import com.top_logic.element.boundsec.manager.rule.RoleRule;
import com.top_logic.element.boundsec.manager.rule.SingletonRule;
import com.top_logic.element.boundsec.manager.rule.config.RoleRuleConfig;
import com.top_logic.element.boundsec.manager.rule.config.RoleRulesConfig;
import com.top_logic.element.boundsec.manager.rule.config.SingletonRuleConfig;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.BoundRole;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.util.error.TopLogicException;

/**
 * @author    <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class RoleRulesImporter {

	private List<ResKey> _problems = new ArrayList<>();

    /**
	 * Holds the imported rules indexed by their {@link TLStructuredType}.
	 */
	private Map<TLClass, Collection<RoleProvider>> _rules = new HashMap<>();

	private Function<String, BoundedRole> _roleProvider;

	private final KnowledgeBase _kb = PersistencyLayer.getKnowledgeBase();

	private TLClass _metaElement;

	private TLClass _sourceMetaElement;

	private ResKey _resKey;

	private boolean _inherit;

    /**
     * Creates a {@link RoleRulesImporter}.
     *
     */
	public RoleRulesImporter(Function<String, BoundedRole> roleProvider) {
		this._roleProvider = roleProvider;
	}

	void addProblem(ResKey aProblem) {
		_problems.add(aProblem);
    }

    void addRule(RoleRule aRule) {
        TLClass theME     = aRule.getMetaElement();
		MultiMaps.add(_rules, theME, aRule, ArrayList::new);
	}

    private TLClass getMetaElement(String aMetaElementName) {
		if (StringServices.isEmpty(aMetaElementName)) {
			return null;
		}
		try {
			return (TLClass) TLModelUtil.findType(aMetaElementName);
		} catch (TopLogicException ex) {
			addProblem(I18NConstants.UNKNOWN_META_ELEMENT.fill(aMetaElementName));
			return null;
		}
    }

	/**
	 * Creates a {@link RoleRulesImporter} that has loaded the given {@link RoleRulesConfig}.
	 * 
	 * @param roleRules
	 *        The {@link RoleRulesConfig role rules configuration} to create {@link RoleRule} from.
	 */
	public static RoleRulesImporter loadRules(RoleRulesConfig roleRules) {
		Map<String, BoundedRole> availableRoles = BoundedRole.getAll()
			.stream()
			.collect(Collectors.toMap(BoundRole::getName, Function.identity()));

		RoleRulesImporter importer = new RoleRulesImporter(availableRoles::get);
		importer.load(roleRules);
		return importer;
	}

	/**
	 * Loads the given {@link RoleRulesConfig}.
	 */
	public void load(RoleRulesConfig roleRules) {
		for (RoleRuleConfig roleRule : roleRules.getRules()) {
			loadRule(roleRule);
		}
	}

	/**
	 * Loads the given {@link RoleRuleConfig}.
	 */
	public void loadRule(RoleRuleConfig roleRule) {
		int numberProblems = _problems.size();
		_resKey = roleRule.getResKey();

		_inherit = roleRule.isInherit();
		_metaElement = getMetaElement(roleRule.getMetaElement());
		if (roleRule.getMetaElement() == null && !(roleRule instanceof SingletonRuleConfig)) {
			addProblem(I18NConstants.NO_META_ELEMENT_DECLARED);
		}
		_sourceMetaElement = getMetaElement(roleRule.getSourceMetaElement());
		if (_metaElement != null && _metaElement.isAbstract() && !_inherit) {
			addProblem(I18NConstants.ABSTRACT_TYPE_WITHOUT_INHERITANCE.fill(roleRule.getMetaElement()));
		}
		if (_sourceMetaElement != null && _sourceMetaElement.isAbstract()) {
			addProblem(I18NConstants.ABSTRACT_SOURCE_TYPE.fill(roleRule.getSourceMetaElement()));
		}
		Collection<BoundedRole> roles = getRoles(roleRule.getRole());
		Collection<BoundedRole> sourceRoles = getRoles(roleRule.getSourceRole());

		if (numberProblems < _problems.size()) {
			// new problems occurred
			return;
		}
		List<PathElement> path = new ArrayList<>();
		for (PolymorphicConfiguration<? extends PathElement> pathElementConf : roleRule.getPathElements()) {
			try {
				PathElement pathElement = TypedConfigUtil.createInstance(pathElementConf);
				path.add(pathElement);
			} catch (I18NRuntimeException ex) {
				Logger.error("Problem creating path element.", ex, RoleRulesImporter.class);
				addProblem(ex.getErrorKey());
			}
		}
		if (path.isEmpty()) {
			path.add(new IdentityPathElement());
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
		if (roleRuleConfig instanceof SingletonRuleConfig singletonRuleConf) {
			TLObject singleton;
			try {
				singleton = TLModelUtil.resolveQualifiedName(singletonRuleConf.getTarget());
			} catch (Exception ex) {
				addProblem(I18NConstants.INVALID_SINGLETON__NAME.fill(singletonRuleConf.getTarget()));
				return;
			}
			if (singleton == null) {
				addProblem(I18NConstants.INVALID_SINGLETON__NAME.fill(singletonRuleConf.getTarget()));
				return;
			}
			for (BoundedRole role : roles) {
				addRule(new SingletonRule(singleton, role, path, _resKey));
			}
		} else {
			for (BoundedRole role : roles) {
				if (Type.inheritance.equals(roleRuleConfig.getType())) {
					addRule(new DefaultRoleRule(_metaElement, _sourceMetaElement, _inherit, role, sourceRole, path,
						Type.inheritance, _resKey));
				} else {
					addRule(
						new DefaultRoleRule(_metaElement, null, _inherit, role, null, path, Type.reference, _resKey));
				}
			}
		}
	}

	private Collection<BoundedRole> getRoles(List<String> roleNames) {
		if (roleNames.isEmpty()) {
			return Collections.emptyList();
		}
		ArrayList<BoundedRole> result = new ArrayList<>(roleNames.size());
		for (String roleName : roleNames) {
			BoundedRole role = _roleProvider.apply(roleName);

			if (role == null) {
				role = BoundedRole.getRoleByName(_kb, roleName);
			}

			if (role != null) {
				result.add(role);
			} else {
				addProblem(I18NConstants.ROLE_RULES_PROBLEM_UNKNOWN_ROLE
					.fill(roleName,
						BoundedRole.getAll().stream().map(x -> x.getName()).collect(Collectors.joining(", "))));
			}

		}
		return result;
	}

	/**
	 * Returns the modifiable {@link Map} containing the parsed {@link RoleProvider}.
	 */
	public Map<TLClass, Collection<RoleProvider>> getRules() {
		return _rules;
	}

	/**
	 * Returns the modifiable list of problems.
	 */
	public List<ResKey> getProblems() {
		return _problems;
	}

}

