/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta;

import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.DisplayAnnotations;

/**
 * Factory service for {@link AttributeUpdate} implementations.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class AttributeUpdateFactory {

	/**
	 * Create an AttributeUpdate for creation (no object available)
	 * 
	 * @param updateContainer
	 *        The {@link AttributeUpdateContainer} to create the new update in.
	 * @param type
	 *        The type of the object to create. Note: This cannot be derived from the given
	 *        attribute, since the attribute may be owned by a generalization of the create type.
	 * @param attribute
	 *        the attribute. Must not be <code>null</code>.
	 * @return the AttributeUpdate or <code>null</code> if none is defined for this attribute
	 */
	public static AttributeUpdate createAttributeUpdateForCreate(AttributeUpdateContainer updateContainer,
			TLStructuredType type, TLStructuredTypePart attribute) {
		return createAttributeUpdateForCreate(updateContainer, type, attribute, null);
	}

	/**
	 * Create an AttributeUpdate for creation (no object available)
	 * 
	 * @param updateContainer
	 *        The {@link AttributeUpdateContainer} to create the new update in.
	 * @param type
	 *        The type of the object to create. Note: This cannot be derived from the given
	 *        attribute, since the attribute may be owned by a generalization of the create type.
	 * @param attribute
	 *        the attribute. Must not be <code>null</code>.
	 * @param domain
	 *        The identifier to the newly created object.
	 * @return the AttributeUpdate or <code>null</code> if none is defined for this attribute
	 */
	public static AttributeUpdate createAttributeUpdateForCreate(AttributeUpdateContainer updateContainer,
			TLStructuredType type, TLStructuredTypePart attribute, String domain) {
		return updateContainer.createObject(type, domain).newCreateUpdate(attribute);
	}

	/**
	 * Create an AttributeUpdate for update (no object available)
	 * 
	 * <p>
	 * The {@link AttributeUpdate} is {@link AttributeUpdate#isDisabled()} if
	 * <code>isDisabled</code> or the attribute is
	 * {@link DisplayAnnotations#isEditable(com.top_logic.model.TLModelPart) not editable}.
	 * </p>
	 * 
	 * @param updateContainer
	 *        The {@link AttributeUpdateContainer} to create the new update in.
	 * @param attribute
	 *        the attribute. Must not be <code>null</code>.
	 * @param object
	 *        the object to get the current values from.
	 * @param isDisabled
	 *        if true no values can be changes (for FormConstraints)
	 * 
	 * @return the AttributeUpdate or <code>null</code> if none is defined for this attribute
	 */
	public static AttributeUpdate createAttributeUpdateForEdit(AttributeUpdateContainer updateContainer,
			TLStructuredTypePart attribute, Wrapper object, boolean isDisabled) {
		return DisplayAnnotations.isHidden(attribute) ? null
			: updateContainer.editObject(object).newEditUpdateDefault(attribute, isDisabled);
	}

	/**
	 * Creates an {@link AttributeUpdate} for the update of the value for the given attribute in the
	 * given object.
	 * 
	 * @param updateContainer
	 *        The {@link AttributeUpdateContainer} to create the new update in.
	 * @param attribute
	 *        The attribute to {@link AttributeUpdate} for.
	 * @param object
	 *        The object to get the current values from.
	 * @param disabled
	 *        Whether the {@link AttributeUpdate} should be disabled.
	 * @param mandatory
	 *        Whether the given {@link AttributeUpdate} should be mandatory for functional reasons.
	 *        Note: The result {@link AttributeUpdate} is always mandatory when the attribute is
	 *        mandatory.
	 * 
	 * @return The desired {@link AttributeUpdate}.
	 */
	public static AttributeUpdate createAttributeUpdateForEdit(AttributeUpdateContainer updateContainer,
			TLStructuredTypePart attribute,
			Wrapper object, boolean disabled, boolean mandatory) {
		return updateContainer.editObject(object).newEditUpdateCustom(attribute, disabled, mandatory);
	}

	/**
	 * Get an AttributeUpdate for a search. May contain a search range.
	 * 
	 * @param updateContainer
	 *        The {@link AttributeUpdateContainer} to create the new update in.
	 * @param attribute
	 *        the attribute. Must not be <code>null</code>.
	 * @param presetValue
	 *        the search value. For range-based searches the start value of the range.
	 * @param aPreset2Value
	 *        for range-based searches the end value of the range. Ignored otherwise.
	 * 
	 * @return the AttributeUpdate or <code>null</code> if none is defined for this attribute
	 */
	public static AttributeUpdate createAttributeUpdateForSearch(AttributeUpdateContainer updateContainer,
			TLStructuredType type, TLStructuredTypePart attribute, Object presetValue, Object aPreset2Value,
			String domain) {
		return updateContainer.createObject(type, domain).newSearchUpdate(attribute, presetValue, aPreset2Value);
	}

}
