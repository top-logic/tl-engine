/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form;

import com.top_logic.layout.form.FormMemberBusinessObjectNamingScheme.FormMemberBusinessObjectName;
import com.top_logic.layout.scripting.recorder.ref.AbstractModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.runtime.ActionContext;

/**
 * {@link ModelNamingScheme} that locates the business object attached to a {@link FormMember} via
 * {@link FormMember#setStableIdSpecialCaseMarker(Object)}
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class FormMemberBusinessObjectNamingScheme extends AbstractModelNamingScheme<Object, FormMemberBusinessObjectName> {

	/**
	 * {@link ModelName} used by {@link FormMemberBusinessObjectNamingScheme}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface FormMemberBusinessObjectName extends ModelName {

		/**
		 * The {@link ModelName} of the {@link FormMember} to retrieve the business object
		 *         from.
		 */
		ModelName getFormMember();
	}

	@Override
	public Class<FormMemberBusinessObjectName> getNameClass() {
		return FormMemberBusinessObjectName.class;
	}

	@Override
	public Class<Object> getModelClass() {
		return Object.class;
	}

	@Override
	public Object locateModel(ActionContext context, FormMemberBusinessObjectName name) {
		FormMember formMember = (FormMember) ModelResolver.locateModel(context, name.getFormMember());
		return formMember.getStableIdSpecialCaseMarker();
	}

	@Override
	protected void initName(FormMemberBusinessObjectName name, Object model) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected boolean isCompatibleModel(Object model) {
		return false;
	}
}

