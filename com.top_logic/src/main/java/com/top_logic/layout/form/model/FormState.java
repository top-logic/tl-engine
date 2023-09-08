/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;

import java.io.IOException;
import java.util.Iterator;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.layout.form.CollapsibleFormMember;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.component.FormComponent;

/**
 * Static helpers to save and load the state of a form to and from the
 * {@link PersonalConfiguration}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class FormState {

	/**
	 * Key prefix to save the collapse state in the {@link PersonalConfiguration}.
	 */
	private static final String KEY_PREFIX_FORM_GROUP_COLLAPSE_STATE_="formGroupCollapseState_";
	
	/**
	 * This method stores the states of all {@link CollapsibleFormMember} members of the given
	 * {@link FormContainer} to the given {@link PersonalConfiguration}.
	 * 
	 * @see #saveCollapsedState(PersonalConfiguration, FormComponent, CollapsibleFormMember)
	 */
	public static void saveFormState(PersonalConfiguration config, FormComponent formComponent, FormContainer form) {
		for (Iterator<? extends FormMember> it = form.getDescendants(); it.hasNext(); ) {
			FormMember member = it.next();
			if (member instanceof CollapsibleFormMember) {
				saveCollapsedState(config, formComponent, (CollapsibleFormMember) member);
			}
		}
	}
	
	/**
	 * This method loads the stored collapsed states of the {@link CollapsibleFormMember} members of
	 * the given {@link FormContainer}. The key to get the collapsed state is computed from the name
	 * of the {@link FormComponent} and the {@link FormMember#getQualifiedName() qualified name} of
	 * the member whose collapse state is loaded.
	 */
	public static void loadFormState(PersonalConfiguration config, FormComponent formComponent, FormContainer form) {
		for (Iterator<? extends FormMember> it = form.getDescendants(); it.hasNext(); ) {
			FormMember member = it.next();
			if (member instanceof CollapsibleFormMember) {
				CollapsibleFormMember container = (CollapsibleFormMember) member;
				if (config.hasBoolean(getKey(formComponent, container))) {
					container.setCollapsed(config.getBoolean(getKey(formComponent, container)));
				}
			}
		}
	}

	/**
	 * This method saves the collapse state of the given {@link CollapsibleFormMember} to the given
	 * {@link PersonalConfiguration} under a certain key. The key is computed from the name of the
	 * given {@link FormComponent} and the {@link FormMember#getQualifiedName() qualified name} of
	 * the {@link CollapsibleFormMember}.
	 * 
	 * @param config
	 *        the {@link PersonalConfiguration} to store the collapsed state
	 * @param formComponent
	 *        the {@link FormComponent} whose name is used to create the key
	 * @param member
	 *        the collapsible member whose state should stored.
	 */
	public static void saveCollapsedState(PersonalConfiguration config, FormComponent formComponent,
			CollapsibleFormMember member) {
		config.setBoolean(getKey(formComponent, member), member.isCollapsed());
	}

	private static String getKey(FormComponent formComponent, FormMember member) {
		StringBuilder out = new StringBuilder();
		try {
			out.append(FormState.KEY_PREFIX_FORM_GROUP_COLLAPSE_STATE_);
			out.append(formComponent.getName());
			member.appendQualifiedName(out);
		} catch (IOException ex) {
			throw new UnreachableAssertion(ex);
		}
		return out.toString();
	}
	
}
