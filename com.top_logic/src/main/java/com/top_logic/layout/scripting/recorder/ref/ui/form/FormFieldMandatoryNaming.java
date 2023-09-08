/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.ui.form;

import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.runtime.ActionContext;

/**
 * {@link ModelNamingScheme} for {@link FormField#isMandatory()}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class FormFieldMandatoryNaming
		extends FormMemberAspectNaming<Boolean, FormFieldMandatoryNaming.FormFieldMandatoryName> {

	/**
	 * The {@link ModelName} of the {@link FormFieldMandatoryNaming}.
	 */
	public interface FormFieldMandatoryName extends FormMemberAspectNaming.FormMemberAspectName {

		// Nothing needed by the type itself.

	}

	/** Creates a {@link FormFieldMandatoryNaming}. */
	public FormFieldMandatoryNaming() {
		super(Boolean.class, FormFieldMandatoryName.class);
	}

	@Override
	public Boolean locateFormMemberAspect(ActionContext context, FormFieldMandatoryName name, FormMember formMember) {
		return ((FormField) formMember).isMandatory();
	}

}
