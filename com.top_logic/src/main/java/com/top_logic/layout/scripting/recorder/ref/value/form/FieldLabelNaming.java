/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.value.form;

import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.value.AspectNaming;
import com.top_logic.layout.scripting.runtime.ActionContext;

/**
 * {@link ModelNamingScheme} for resolving the label of a given form member.
 * 
 * @see FormMember#getLabel()
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class FieldLabelNaming extends AspectNaming<String, FieldLabelNaming.Name, FormMember> {

	/**
	 * Reference to the option list of a given field.
	 * 
	 * @see #getModel()
	 */
	public interface Name extends AspectNaming.Name {
		// Pure marker interface.
	}

	@Override
	public Class<? extends Name> getNameClass() {
		return Name.class;
	}

	@Override
	public Class<String> getModelClass() {
		return String.class;
	}

	@Override
	protected Class<FormMember> baseType() {
		return FormMember.class;
	}

	@Override
	protected String localteModel(ActionContext context, Name name, FormMember member) {
		return member.getLabel();
	}

}
