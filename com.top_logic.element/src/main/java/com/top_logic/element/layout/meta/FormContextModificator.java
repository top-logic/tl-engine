/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta;

import com.top_logic.element.layout.grid.GridComponent;
import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.element.meta.AttributeUpdateContainer;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.element.meta.form.AttributeFormContext;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormMember;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link GridComponent} will use this instance when creating new input fields
 * for a selected row. This will happen during a row selection. 
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public interface FormContextModificator {

    /** 
     * This method will be called before the form fields for the given attributed will be created.
     * @param component The context component
     * @param type The type of the edited object.
     * @param    anAttributed    The attributed to be modified in the next steps.
     * @return   <code>true</code> if form fields should be created for the given attributed.
     */
    public boolean preModify(LayoutComponent component, TLClass type, TLObject anAttributed);

    /** 
     * Modify the given form member.
     * @param component The context component
     * @param    aName           The name of the meta attribute the form member represents, must not be <code>null</code>. 
     * @param    aMember         The form member to be modified, must not be <code>null</code>.
     * @param    aMA             The meta attribute the form member represents, must not be <code>null</code>.
     * @param type The type of the edited object.
     * @param    anAttributed    The attributed represented by the given form member, must not be <code>null</code>.
     * @param    anUpdate        The update object contained in the {@link AttributeUpdateContainer}, must not be <code>null</code>.
     * @param    aContext        The form context the member will be hung into, must not be <code>null</code>.
     * @param currentGroup The {@link FormContainer} in which fields are created.
     */
    public void modify(LayoutComponent component, String aName, FormMember aMember, TLStructuredTypePart aMA, TLClass type, TLObject anAttributed, AttributeUpdate anUpdate, AttributeFormContext aContext, FormContainer currentGroup);

    /** 
     * This method will be called after the creation of all form fields for the given attributed.
     * @param component The context component
     * @param type The type of the edited object.
     * @param    anAttributed    The attributed the form fields have been created for.
     * @param currentGroup The {@link FormContainer} in which fields are created.
     */
    public void postModify(LayoutComponent component, TLClass type, TLObject anAttributed, AttributeFormContext aContext, FormContainer currentGroup);
    
    /**
	 * Removes additional fields created in
	 * {@link #postModify(LayoutComponent, TLClass, TLObject, AttributeFormContext, FormContainer)}
	 * from the given current group.
	 * 
	 * @param component
	 *        The context component
	 * @param type
	 *        The type of the edited object.
	 * @param anAttributed
	 *        The attributed the form fields have been created for.
	 * @param aContainer
	 *        The corresponding {@link AttributeUpdateContainer}.
	 * @param currentGroup
	 *        The {@link FormContainer} in which fields are created.
	 */
    public void clear(LayoutComponent component, TLClass type, TLObject anAttributed, AttributeUpdateContainer aContainer, FormContainer currentGroup);
}

