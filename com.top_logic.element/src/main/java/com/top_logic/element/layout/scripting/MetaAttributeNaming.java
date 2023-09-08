/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.scripting;

import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.layout.scripting.recorder.ref.AbstractModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.recorder.ref.NamedModelName;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertions;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLStructuredTypePart;

/**
 * {@link ModelNamingScheme} for {@link TLStructuredTypePart}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class MetaAttributeNaming extends AbstractModelNamingScheme<TLStructuredTypePart, MetaAttributeNaming.MetaAttributeName> {

	/**
	 * Name to identify {@link TLStructuredTypePart}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface MetaAttributeName extends NamedModelName {

		/**
		 * The owner of the represented {@link TLStructuredTypePart}.
		 */
		ModelName getOwner();

		/**
		 * Setter for {@link #getOwner()}.
		 */
		void setOwner(ModelName owner);
	}

	@Override
	public Class<MetaAttributeName> getNameClass() {
		return MetaAttributeName.class;
	}

	@Override
	public Class<TLStructuredTypePart> getModelClass() {
		return TLStructuredTypePart.class;
	}

	@Override
	public TLStructuredTypePart locateModel(ActionContext context, MetaAttributeName name) {
		Object owner = context.resolve(name.getOwner());
		if (owner == null) {
			throw ApplicationAssertions.fail(name, "Owner does not exist.");
		}
		try {
			return MetaElementUtil.getMetaAttribute(((TLClass) owner), name.getName());
		} catch (NoSuchAttributeException ex) {
			throw ApplicationAssertions.fail(name,
				"Attribute '" + name.getName() + "' does not exist in element '" + owner + "'.", ex);
		}
	}

	@Override
	protected void initName(MetaAttributeName name, TLStructuredTypePart model) {
		TLClass metaElement = AttributeOperations.getMetaElement(model);
		name.setOwner(ModelResolver.buildModelName(metaElement));
		name.setName(model.getName());
	}

}

