/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta;

import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.AnnotationLookup;

/**
 * Factory for {@link AttributeUpdate}s for an object being edited or created.
 * 
 * @see AttributeUpdateContainer#createObject(com.top_logic.model.TLStructuredType, String)
 * @see AttributeUpdateContainer#editObject(com.top_logic.model.TLObject)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface UpdateFactory {

	/**
	 * Creates an {@link AttributeUpdate} for displaying a form input for an attribute of an edited
	 * object.
	 *
	 * @param attribute
	 *        The attribute to display.
	 * @param disabled
	 *        Whether to explicitly disable the input.
	 * @return The created {@link AttributeUpdate}, or <code>null</code>, if the given attribute
	 *         does not belong to the type of the edited object.
	 * 
	 * @see AttributeUpdateContainer#editObject(com.top_logic.model.TLObject)
	 */
	AttributeUpdate newEditUpdateDefault(TLStructuredTypePart attribute, boolean disabled);

	/**
	 * Creates an {@link AttributeUpdate} for displaying a form input for an attribute of an edited
	 * object.
	 *
	 * @param attribute
	 *        The attribute to display.
	 * @param disabled
	 *        Whether to explicitly disable the input.
	 * @param mandatory
	 *        Whether to explicitly set the input to mandatory (only possible, if it is not
	 *        disabled).
	 * @return The created {@link AttributeUpdate}, or <code>null</code>, if the given attribute
	 *         does not belong to the type of the edited object.
	 * 
	 * @see AttributeUpdateContainer#editObject(com.top_logic.model.TLObject)
	 */
	AttributeUpdate newEditUpdateCustom(TLStructuredTypePart attribute, boolean disabled, boolean mandatory);

	/**
	 * Creates an {@link AttributeUpdate} for displaying a form input for an attribute of an edited
	 * object.
	 *
	 * @param attribute
	 *        The attribute to display.
	 * @param disabled
	 *        Whether to explicitly disable the input.
	 * @param mandatory
	 *        Whether to explicitly set the input to mandatory (only possible, if it is not
	 *        disabled).
	 * @param annotations
	 *        Custom attribute annotations.
	 * @return The created {@link AttributeUpdate}, or <code>null</code>, if the given attribute
	 *         does not belong to the type of the edited object.
	 * 
	 * @see AttributeUpdateContainer#editObject(com.top_logic.model.TLObject)
	 */
	AttributeUpdate newEditUpdateCustom(TLStructuredTypePart attribute, boolean disabled, boolean mandatory,
			AnnotationLookup annotations);

	/**
	 * Creates an {@link AttributeUpdate} for displaying a form input for an attribute of an object
	 * being created.
	 *
	 * @param attribute
	 *        The attribute to display.
	 * @return The created {@link AttributeUpdate}, or <code>null</code>, if the given attribute
	 *         does not belong to the type of the edited object.
	 * 
	 * @see AttributeUpdateContainer#createObject(com.top_logic.model.TLStructuredType, String)
	 */
	AttributeUpdate newCreateUpdate(TLStructuredTypePart attribute);

	/**
	 * Creates an {@link AttributeUpdate} for displaying a form input for a search mask of an
	 * attribute.
	 *
	 * @param attribute
	 *        The attribute to display.
	 * @param fromValue
	 *        The initial start value of the search range.
	 * @param toValue
	 *        The initial end of the search range.
	 * @return The created {@link AttributeUpdate}, or <code>null</code>, if the given attribute
	 *         does not belong to the type of the edited object.
	 * 
	 * @see AttributeUpdateContainer#createObject(com.top_logic.model.TLStructuredType, String)
	 */
	AttributeUpdate newSearchUpdate(TLStructuredTypePart attribute, Object fromValue, Object toValue);

}
