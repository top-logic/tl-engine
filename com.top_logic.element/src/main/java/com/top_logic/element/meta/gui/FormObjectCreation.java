/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.gui;

import java.util.Iterator;
import java.util.Map;

import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.element.meta.form.AttributeFormContext;
import com.top_logic.element.meta.form.AttributeFormFactory;
import com.top_logic.element.meta.form.overlay.TLFormObject;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.component.CreateFunction;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;

/**
 * Generic {@link CreateFunction} that applies form values to new instances of a given
 * {@link TLClass type}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class FormObjectCreation implements CreateFunction {

	/**
	 * Singleton {@link FormObjectCreation} instance.
	 */
	public static final FormObjectCreation INSTANCE = new FormObjectCreation();

	/**
	 * Creates a {@link FormObjectCreation}.
	 */
	protected FormObjectCreation() {
		super();
	}

	@Override
	public Object createObject(LayoutComponent component, Object createContext, FormContainer form,
			Map<String, Object> arguments) {
		AttributeFormContext formContext = formContext(form);
		formContext.store();

		TLFormObject overlay = formContext.getAttributeUpdateContainer().getOverlay(null, null);
		TLObject newObject = overlay.getEditedObject();
		assert newObject != null;
		storeValues(newObject, form);

		initContainer(overlay.tContainer(), newObject, createContext);

		return newObject;

	}

	/**
	 * Connects the newly created object to its container.
	 * 
	 * @param container
	 *        The container for the new object.
	 * @param newObject
	 *        The newly created object.
	 * @param createContext
	 *        The create context ("parent" object) of the new object.
	 */
	protected void initContainer(TLObject container, TLObject newObject, Object createContext) {
		// hook for subclasses
	}

	private AttributeFormContext formContext(FormContainer form) {
		while (!(form instanceof AttributeFormContext)) {
			if (form == null) {
				throw new IllegalArgumentException("No AttributeFormContext found.");
			}
			form = form.getParent();
		}
		return (AttributeFormContext) form;
	}

	/**
	 * Stores all values of the given {@link FormContainer} in the given new instance.
	 *
	 * @param newInstance
	 *        The plain new instance.
	 * @param form
	 *        The values from the UI.
	 * @return The given new instance filled with values from the given form.
	 */
	protected TLObject storeValues(TLObject newInstance, FormContainer form) {
		for (Iterator<? extends FormMember> it = form.getMembers(); it.hasNext();) {
			storeField(newInstance, it.next());
		}
		return newInstance;
	}

	/**
	 * Stores a single field value to the given new instance.
	 *
	 * @param newInstance
	 *        The new instance to store into.
	 * @param field
	 *        The field from the UI.
	 */
	protected void storeField(TLObject newInstance, FormMember field) {
		AttributeUpdate update = AttributeFormFactory.getAttributeUpdate(field);
		if (update == null) {
			storeRawMember(newInstance, field);
		}
	}

	/**
	 * Hook for storing a raw form member values to the given new instance.
	 *
	 * @param newInstance
	 *        The new instance to store into.
	 * @param field
	 *        The field from the UI.
	 */
	protected void storeRawMember(TLObject newInstance, FormMember field) {
		// Ignore.
	}

}
