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

import com.top_logic.basic.StringServices;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.element.boundsec.manager.ElementAccessImporter.ApplicationRoleHolder;
import com.top_logic.element.boundsec.manager.rule.IdentityPathElement;
import com.top_logic.element.boundsec.manager.rule.PathElement;
import com.top_logic.element.boundsec.manager.rule.RoleProvider;
import com.top_logic.element.boundsec.manager.rule.RoleProvider.Type;
import com.top_logic.element.boundsec.manager.rule.RoleRule;
import com.top_logic.element.boundsec.manager.rule.config.PathElementConfig;
import com.top_logic.element.boundsec.manager.rule.config.RoleRuleConfig;
import com.top_logic.element.boundsec.manager.rule.config.RoleRulesConfig;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.util.error.TopLogicException;

/**
 * @author    <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class RoleRulesImporter {

	/**
	 * Message key used when a configured role is unknown.
	 */
	static final ResKey1 UNKNOWN_ROLE = I18NConstants.ROLE_RULES_PROBLEM_UNKNOWN_ROLE;

	/**
	 * Message key used when the source {@link TLClass} of a rule is abstract.
	 */
	static final ResKey1 ABSTRACT_SOURCE_TYPE = I18NConstants.ABSTRACT_SOURCE_TYPE;

	/**
	 * Message key used when the {@link TLClass} of a rule is abstract and the rule is no
	 * "inherit" rule. In such case the rule is useless.
	 */
	static final ResKey1 ABSTRACT_TYPE_WITHOUT_INHERITANCE =
			I18NConstants.ABSTRACT_TYPE_WITHOUT_INHERITANCE;

	/**
	 * Message key used when neither a {@link TLClass} nor a {@link MetaObject} is declared in a
	 * rule.
	 */
	static final ResKey NO_ELEMENT_OR_OBJECT_DECLARED =
			I18NConstants.META_ELEMENT_AND_META_OBJECT_NOT_DECLARED;

	/**
	 * Message key used when both, {@link TLClass} and {@link MetaObject}, are declared in a
	 * rule.
	 */
	static final ResKey2 ELEMENT_AND_OBJECT_DECLARED =
			I18NConstants.META_ELEMENT_AND_META_OBJECT_DECLARED;

	/**
	 * Message key used when configured {@link TLClass} is unknown.
	 */
	static final ResKey1 UNKNOWN_META_ELEMENT = I18NConstants.UNKNOWN_META_ELEMENT;

	/**
	 * Message key used when configured {@link MetaObject} is unknown.
	 */
	static final ResKey1 UNKNOWN_META_OBJECT = I18NConstants.UNKNOWN_META_OBJECT;

	/**
	 * Message key used when both, {@link TLStructuredTypePart} and {@link KnowledgeAssociation} are
	 * declared in a rule.
	 */
	static final ResKey2 ATTRIBUTE_AND_ASSOCIATION_DECLARED =
		I18NConstants.ATTRIBUTE_AND_ASSOCIATION_GIVEN;

	/**
	 * Message key used when neither a {@link TLStructuredTypePart} nor an {@link KnowledgeAssociation} is
	 * declared in a rule.
	 */
	static final ResKey NO_ATTRIBUTE_OR_ASSOCIATION_DECLARED =
		I18NConstants.NO_ATTRIBUTE_OR_ASSOCIATION;

	/**
	 * Message key used when the {@link TLClass} in a path is not a super tpye of the
	 * {@link TLClass}.
	 */
	static final ResKey2 NOT_A_SUPER_TYPE = I18NConstants.ILLEGAL_META_ELEMENT;

	/**
	 * Message key used when declared {@link TLStructuredTypePart} is not known in {@link TLClass}.
	 */
	static final ResKey2 UNKNOWN_ATTRIBUTE = I18NConstants.UNKNOWN_ATTRIBUTE;

	private List<ResKey> _problems = new ArrayList<>();

    /**
     * holds the rules impotrted indexed by either the meta object or the meta element
     * depending on which is declared for the rule.
     */
	private Map<Object, Collection<RoleProvider>> _rules = new HashMap<>();

	private ApplicationRoleHolder _roleProvider;

	private final KnowledgeBase _kb = PersistencyLayer.getKnowledgeBase();

	private TLClass _metaElement;

	private MetaObject _metaObject;

	private TLClass _sourceMetaElement;

	private MetaObject _sourceMetaObject;

	private ResKey _resKey;

	private String _base;

	private boolean _inherit;

    /**
     * Creates a {@link RoleRulesImporter}.
     *
     */
    public RoleRulesImporter(ApplicationRoleHolder aRoleProvieder) {
		this._roleProvider = aRoleProvieder;
    }

	void addProblem(ResKey aProblem) {
		_problems.add(aProblem);
    }

    void addRule(RoleRule aRule) {
        MetaObject  theMO     = aRule.getMetaObject();
        TLClass theME     = aRule.getMetaElement();
        Object      theTarget = theME != null ? (Object) theME : (Object) theMO;

		Collection<RoleProvider> theRules = this._rules.get(theTarget);
        if (theRules == null) {
			theRules = new ArrayList<>();
			this._rules.put(theTarget, theRules);
        }
        theRules.add(aRule);
    }

    private TLClass getMetaElement(String aMetaElementName) {
		if (StringServices.isEmpty(aMetaElementName)) {
			return null;
		}
		try {
			return (TLClass) TLModelUtil.findType(aMetaElementName);
		} catch (TopLogicException ex) {
			addProblem(UNKNOWN_META_ELEMENT.fill(aMetaElementName));
			return null;
		}
    }

	private MetaObject getMetaObject(String aMetaObjectName) {
		if (StringServices.isEmpty(aMetaObjectName)) {
			return null;
		}
		try {
			return _kb.getMORepository().getMetaObject(aMetaObjectName);
		} catch (UnknownTypeException ex) {
			addProblem(UNKNOWN_META_OBJECT.fill(aMetaObjectName));
			return null;
		}
    }

	/**
	 * Creates a {@link RoleRulesImporter} that has loaded the given {@link RoleRulesConfig}.
	 * 
	 * @param roleRules
	 *        The {@link RoleRulesConfig role rules configuration} to create {@link RoleRule} from.
	 */
	public static RoleRulesImporter loadRules(ElementAccessManager accessManager, RoleRulesConfig roleRules) {
		RoleRulesImporter importer = new RoleRulesImporter(new ApplicationRoleHolder(accessManager));
		importer.loadRules(roleRules);
		return importer;
	}

	/**
	 * Loads the given {@link RoleRulesConfig}.
	 */
	public void loadRules(RoleRulesConfig roleRules) {
		for (RoleRuleConfig roleRule : roleRules.getRules()) {
			loadRule(roleRule);
		}
	}

	/**
	 * Loads the given {@link RoleRuleConfig}.
	 */
	public void loadRule(RoleRuleConfig roleRule) {
		int numberProblems = _problems.size();
		_base = roleRule.getBase();
		_resKey = roleRule.getResKey();

		_inherit = roleRule.isInherit();
		_metaElement = getMetaElement(roleRule.getMetaElement());
		_metaObject = getMetaObject(roleRule.getMetaObject());
		if (roleRule.getMetaElement().isEmpty() && roleRule.getMetaObject().isEmpty()) {
			addProblem(NO_ELEMENT_OR_OBJECT_DECLARED);
		}
		if (!roleRule.getMetaElement().isEmpty() && !roleRule.getMetaObject().isEmpty()) {
			addProblem(ELEMENT_AND_OBJECT_DECLARED.fill(roleRule.getMetaElement(), roleRule.getMetaObject()));
		}
		_sourceMetaElement = getMetaElement(roleRule.getSourceMetaElement());
		if (_metaElement != null && _metaElement.isAbstract() && !_inherit) {
			addProblem(ABSTRACT_TYPE_WITHOUT_INHERITANCE.fill(roleRule.getMetaElement()));
		}
		if (_sourceMetaElement != null && _sourceMetaElement.isAbstract()) {
			addProblem(ABSTRACT_SOURCE_TYPE.fill(roleRule.getSourceMetaObject()));
		}
		_sourceMetaObject = getMetaObject(roleRule.getSourceMetaObject());
		Collection<BoundedRole> roles = getRoles(roleRule.getRole());
		Collection<BoundedRole> sourceRoles = getRoles(roleRule.getSourceRole());

		if (numberProblems < _problems.size()) {
			// new problems occurred
			return;
		}
		List<PathElement> path = new ArrayList<>();
		for (PathElementConfig pathElement : roleRule.getPathElements()) {
			handlePath(path, pathElement);
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
		for (BoundedRole role : roles) {
			if (_metaElement != null) {
				if (Type.inheritance.equals(roleRuleConfig.getType())) {
					addRule(
						new RoleRule(_metaElement, _sourceMetaElement, _sourceMetaObject, _inherit,
							role, sourceRole, path, _base, _resKey));
				} else {
					addRule(
						new RoleRule(_metaElement, _inherit, role, path, _base,
							_resKey));
				}
			} else {
				if (Type.inheritance.equals(roleRuleConfig.getType())) {
					addRule(new RoleRule(_metaObject, _sourceMetaElement, _sourceMetaObject, role,
						sourceRole, path, _base, _resKey));
				} else {
					addRule(new RoleRule(_metaObject, role, path, _base, _resKey));
				}
			}
		}
	}

	private void handlePath(List<PathElement> path, PathElementConfig pathElement) {
		String metaElementName = pathElement.getMetaElement();
		String metaAtributeName = pathElement.getAttribute();
		String associationName = pathElement.getAssociation();
		boolean inverse = pathElement.isInverse();
		if (metaAtributeName.isEmpty() && associationName.isEmpty()) {
			addProblem(NO_ATTRIBUTE_OR_ASSOCIATION_DECLARED);
			return;
		}
		if (!metaAtributeName.isEmpty() && !associationName.isEmpty()) {
			addProblem(ATTRIBUTE_AND_ASSOCIATION_DECLARED.fill(metaAtributeName, associationName));
			return;
		}
		if (metaAtributeName.isEmpty()) {
			path.add(new PathElement(associationName, inverse));
			return;
		}
		TLClass theME;
		if (metaElementName.isEmpty() && !inverse && path.isEmpty()) {
			theME = _metaElement;
		} else {
			theME = getMetaElement(metaElementName);
			if (theME == null) {
				return;
			}
			if (metaElementName.isEmpty()) {
				// No meta element given.
				addProblem(UNKNOWN_META_ELEMENT.fill(""));
				return;
			}
		}

		if (path.isEmpty()
			&& !inverse
			&& StringServices.isEmpty(_base)
			&& _metaElement != null) {
			if (!MetaElementUtil.isSuperType(theME, _metaElement)) {
				addProblem(NOT_A_SUPER_TYPE.fill(metaElementName, _metaElement.getName()));
			}
		}
		TLStructuredTypePart theMA = MetaElementUtil.getMetaAttributeOrNull(theME, metaAtributeName);
		if (theMA == null) {
			addProblem(UNKNOWN_ATTRIBUTE.fill(metaElementName, metaAtributeName));
			return;
		}
		path.add(new PathElement(theMA, inverse));
	}

	private Collection<BoundedRole> getRoles(List<String> roleNames) {
		if (roleNames.isEmpty()) {
			return Collections.emptyList();
		}
		ArrayList<BoundedRole> result = new ArrayList<>(roleNames.size());
		for (String roleName : roleNames) {
			BoundedRole role = _roleProvider.getRole(roleName);

			if (role == null) {
				role = BoundedRole.getRoleByName(_kb, roleName);
			}

			if (role != null) {
				result.add(role);
			} else {
				addProblem(UNKNOWN_ROLE.fill(roleName));
			}

		}
		return result;
	}

	/**
	 * Returns the modifiable {@link Map} containing the parsed {@link RoleProvider}.
	 */
	public Map<Object, Collection<RoleProvider>> getRules() {
		return _rules;
	}

	/**
	 * Returns the modifiable list of problems.
	 */
	public List<ResKey> getProblems() {
		return _problems;
	}

}

