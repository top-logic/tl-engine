/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.ui.form;

import java.util.List;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.SelectFieldUtils;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.runtime.ActionContext;

/**
 * {@link ModelNamingScheme} for {@link SelectField#getOptions()}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SelectFieldOptionsNaming
		extends FormMemberAspectNaming<List, SelectFieldOptionsNaming.SelectFieldOptionsName> {

	/**
	 * The {@link ModelName} of the {@link SelectFieldOptionsNaming}.
	 */
	@Label("Options of {form-member}")
	public interface SelectFieldOptionsName extends FormMemberAspectNaming.FormMemberAspectName {

		/**
		 * Whether the selected elements which are not part of the options are included in the
		 * options.
		 */
		@BooleanDefault(true)
		boolean isIncludeSelection();

		/**
		 * Setter for #{@link #isIncludeSelection()}.
		 */
		void setIncludeSelection(boolean value);

	}

	/** Creates a {@link SelectFieldOptionsNaming}. */
	public SelectFieldOptionsNaming() {
		super(List.class, SelectFieldOptionsName.class);
	}

	@Override
	public List locateFormMemberAspect(ActionContext context, SelectFieldOptionsName name, FormMember formMember) {
		if (name.isIncludeSelection()) {
			return SelectFieldUtils.getOptionAndSelectionOuterJoinOrdered((SelectField) formMember);
		} else {
			return ((SelectField) formMember).getOptions();
		}
	}

}
