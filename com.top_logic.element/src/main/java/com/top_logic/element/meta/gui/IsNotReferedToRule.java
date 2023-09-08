/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.gui;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.element.meta.kbbased.WrapperMetaAttributeUtil;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.util.TLModelI18N;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.util.Resources;

/**
 * An ExecutabilityRule rule that will not allow when model of LayoutComponent is referred to by
 * some {@link Wrapper} wrapper. This should be most useful for messages like (User 'xxx')
 * "is referred"
 * 
 * @author <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class IsNotReferedToRule implements ExecutabilityRule {

	/** The MetaAttributes in question. */
	Set<TLStructuredTypePart> _attributes;

	/**
	 * The State to rule to return for
	 * {@link WrapperMetaAttributeUtil#hasWrappersWithValue(TLStructuredTypePart, com.top_logic.model.TLObject)}.
	 */
	ExecutableState notAllowedRefered;

	/**
	 * Create a rule that will State 'is Referred to'. Be aware that this Rule is not state less as
	 * it must resolve Resources.
	 */
	public IsNotReferedToRule(TLClass metaElem, String attrName) throws NoSuchAttributeException {
		this(metaElem, attrName, I18NConstants.ERROR_IS_REFERENCED__ATTRIBUTE);
	}

	public IsNotReferedToRule(TLClass metaElem, String attrName, ResKey1 aReason) throws NoSuchAttributeException {
		TLStructuredTypePart theMA = MetaElementUtil.getMetaAttribute(metaElem, attrName);
		ResKey maKey = TLModelI18N.getI18NKey(metaElem, theMA);
		this._attributes = Collections.singleton(theMA);
		maKey = aReason.fill(Resources.getInstance().getString(maKey));
		notAllowedRefered = ExecutableState.createDisabledState(maKey);
	}

	/**
	 * Create a rule that will State 'are Referred to'. Be aware that this Rule is not state less as
	 * it must resolve Resources.
	 */
	public IsNotReferedToRule(TLClass metaElem, Collection<String> attrNames) throws NoSuchAttributeException {
		this(metaElem, attrNames, I18NConstants.ARE_REFERRED_TO);
	}

	/**
	 * Create a rule that will State 'are Referred to'. Be aware that this Rule is not state less as
	 * it must resolve Resources.
	 */
	public IsNotReferedToRule(TLClass metaElem, Collection<String> attrNames, ResKey aReason)
			throws NoSuchAttributeException {
		if (attrNames != null) {
			this._attributes = new HashSet<>(attrNames.size());
			for (String attrName : attrNames) {
				TLStructuredTypePart theMA = MetaElementUtil.getMetaAttribute(metaElem, attrName);
				this._attributes.add(theMA);
			}
		}
		notAllowedRefered = ExecutableState.createDisabledState(aReason);
	}

	/**
	 * Create a rule with a VISIBILITY_DISABLED rule and given reasonKey. Be aware that this Rule is
	 * not state less as it must resolve Resources.
	 * 
	 * @param aReasonKey
	 *        The reason key.
	 */
	public IsNotReferedToRule(TLStructuredTypePart attribute, ResKey aReasonKey) {
		this._attributes = Collections.singleton(attribute);
		this.notAllowedRefered = ExecutableState.createDisabledState(aReasonKey);
	}

	/**
	 * Do not allow execution when
	 * {@link WrapperMetaAttributeUtil#hasWrappersWithValue(TLStructuredTypePart, com.top_logic.model.TLObject)}.
	 * is true.
	 */
	@Override
	public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> aSomeValues) {
		Wrapper theWrapper = extractWrapper(aComponent);
		if (theWrapper != null && WrapperMetaAttributeUtil.hasWrappersWithValues(theWrapper, _attributes)) {
			return notAllowedRefered;
		}
		return ExecutableState.EXECUTABLE;
	}

	/**
	 * Allow subclasses to extract secondary or derived models for aComponent
	 * 
	 * @return {@link LayoutComponent#getModel()} casted to {@link Wrapper}.
	 */
	protected Wrapper extractWrapper(LayoutComponent aComponent) {
        Object model =  aComponent.getModel();
        if (model instanceof Wrapper) { // implies != null
            return (Wrapper) model; 
        }
        return null;
	}
}
