/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.element.boundsec.manager;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.boundsec.manager.rule.IdentityPathElement;
import com.top_logic.element.boundsec.manager.rule.PathElement;
import com.top_logic.element.boundsec.manager.rule.config.NavigationRuleConfig;
import com.top_logic.element.boundsec.manager.rule.config.PathElementConfig;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.error.TopLogicException;

/**
 * Superclass for the import of {@link NavigationRuleConfig}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractNavigationRulesImporter {

	private TLClass _metaElement;

	private boolean _inherit;

	private List<ResKey> _problems = new ArrayList<>();

	/**
	 * Registers the given problem.
	 */
	protected void addProblem(ResKey aProblem) {
		_problems.add(aProblem);
	}

	/**
	 * Returns the modifiable list of problems.
	 */
	public List<ResKey> getProblems() {
		return _problems;
	}

	/**
	 * Access to the resolved {@link TLClass} from {@link NavigationRuleConfig#getMetaElement()}.
	 */
	protected TLClass getMetaElement() {
		return _metaElement;
	}

	/**
	 * Value of the {@link NavigationRuleConfig#isInherit()}.
	 * 
	 * @see #initVariables(NavigationRuleConfig)
	 */
	protected boolean isInherit() {
		return _inherit;
	}

	/**
	 * Resolves the {@link TLClass} with the given name.
	 */
	protected TLClass getMetaElement(String aMetaElementName) {
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
	 * Initializes values {@link #getMetaElement()} and {@link #isInherit()}.
	 */
	protected void initVariables(NavigationRuleConfig rule) {
		_inherit = rule.isInherit();
		_metaElement = getMetaElement(rule.getMetaElement());
		if (rule.getMetaElement() == null) {
			addProblem(I18NConstants.NO_META_ELEMENT_DECLARED);
		}
		if (_metaElement != null && _metaElement.isAbstract() && !_inherit) {
			addProblem(I18NConstants.ABSTRACT_TYPE_WITHOUT_INHERITANCE.fill(rule.getMetaElement()));
		}
	}

	/**
	 * Resolves {@link NavigationRuleConfig#getPathElements()}.
	 */
	protected List<PathElement> resolvePath(NavigationRuleConfig rule) {
		List<PathElement> path = new ArrayList<>();
		for (PathElementConfig pathElement : rule.getPathElements()) {
			handlePath(path, pathElement);
		}
		if (path.isEmpty()) {
			path.add(new IdentityPathElement());
		}
		return path;
	}

	private void handlePath(List<PathElement> path, PathElementConfig pathElement) {
		String metaElementName = pathElement.getMetaElement();
		String metaAtributeName = pathElement.getAttribute();
		boolean inverse = pathElement.isInverse();
		if (metaAtributeName.isEmpty()) {
			addProblem(I18NConstants.NO_ATTRIBUTE_DECLARED);
			return;
		}
		TLClass theME;
		if (metaElementName.isEmpty() && !inverse && path.isEmpty()) {
			theME = _metaElement;
		} else {
			theME = getMetaElement(metaElementName);
			if (theME == null) {
				// Error is logged in #getMetaElement(..)
				return;
			}
			if (metaElementName.isEmpty()) {
				// No meta element given.
				addProblem(I18NConstants.UNKNOWN_META_ELEMENT.fill(""));
				return;
			}
		}

		if (path.isEmpty() && !inverse && getBase().isEmpty() && _metaElement != null) {
			if (!MetaElementUtil.isSuperType(theME, _metaElement)) {
				addProblem(I18NConstants.ILLEGAL_META_ELEMENT.fill(metaElementName, _metaElement.getName()));
			}
		}
		TLStructuredTypePart theMA = MetaElementUtil.getMetaAttributeOrNull(theME, metaAtributeName);
		if (theMA == null) {
			String qMEName = TLModelUtil.qualifiedName(theME);
			addProblem(I18NConstants.UNKNOWN_ATTRIBUTE.fill(qMEName, metaAtributeName));
			return;
		}
		path.add(new PathElement(theMA.getDefinition(), inverse));
	}

	/**
	 * Base element for the import run.
	 */
	protected abstract String getBase();

}

