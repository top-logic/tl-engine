/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.overlay;

import com.top_logic.basic.config.annotation.Nullable;
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
		TLFormObject result = formContext.getAttributeUpdateContainer().getOverlay(editedObject, domain);
		if (result == null) {
			ApplicationAssertions.assertNotNull(name, "Form object overlay cannot be resolved.", result);
		}
		return result;
	}

}
