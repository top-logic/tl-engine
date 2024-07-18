/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.overlay;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.element.meta.AttributeUpdateContainer;
import com.top_logic.element.meta.form.AttributeFormContext;
import com.top_logic.layout.scripting.recorder.ref.AbstractModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertions;
import com.top_logic.model.TLObject;

/**
 * {@link ModelNamingScheme} for {@link TLFormObject} overlays.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TLFormObjectNaming extends AbstractModelNamingScheme<TLFormObject, TLFormObjectNaming.Name> {

	/**
	 * {@link ModelName} for {@link TLFormObjectNaming}.
	 */
	public interface Name extends ModelName {

		/**
		 * The {@link AttributeFormContext} to search in.
		 */
		ModelName getFormContext();

		/**
		 * Optional object being edited.
		 */
		ModelName getEditedObject();

		/**
		 * Domain of object being created.
		 */
		@Nullable
		String getDomain();

		/**
		 * @see #getFormContext()
		 */
		void setFormContext(ModelName value);

		/**
		 * @see #getEditedObject()
		 */
		void setEditedObject(ModelName value);

		/**
		 * @see #getDomain()
		 */
		void setDomain(String domain);
	}

	/**
	 * Creates a {@link TLFormObjectNaming}.
	 */
	public TLFormObjectNaming() {
		super(TLFormObject.class, Name.class);
	}

	@Override
	protected void initName(Name name, TLFormObject model) {
		name.setFormContext(ModelResolver.buildModelName(model.getScope().getFormContext()));
		name.setEditedObject(ModelResolver.buildModelName(model.getEditedObject()));
		name.setDomain(model.getDomain());
	}

	@Override
	public TLFormObject locateModel(ActionContext context, Name name) {
		AttributeFormContext formContext =
			(AttributeFormContext) ModelResolver.locateModel(context, name.getFormContext());
		TLObject editedObject = (TLObject) ModelResolver.locateModel(context, name.getEditedObject());
		String domain = name.getDomain();
		AttributeUpdateContainer updateContainer = formContext.getAttributeUpdateContainer();
		TLFormObject result = updateContainer.getOverlay(editedObject, domain);
		if (result == null) {
			List<String> existingDomains = new ArrayList<>();
			for (TLFormObject obj : updateContainer.getAllOverlays()) {
				if (editedObject != null && editedObject == obj.getEditedObject()) {
					throw ApplicationAssertions.fail(name, "Wrong domain in object overlay reference, domain: " + domain
						+ ", expecting: " + obj.getDomain());
				}

				if (obj.getEditedObject() == null) {
					existingDomains.add(obj.getDomain());
				}
			}

			if (editedObject == null && existingDomains.size() == 1) {
				throw ApplicationAssertions.fail(name, "Wrong domain in object overlay reference, domain: " + domain
					+ ", expecting: " + existingDomains.get(0));
			}

			if (editedObject == null) {
				ApplicationAssertions.fail(name, "Form create overlay cannot be resolved (domain '" + domain
					+ "'), existing domains: " + existingDomains);
			} else {
				ApplicationAssertions.fail(name, "Form object overlay cannot be resolved. ");
			}
		}
		return result;
	}

}
