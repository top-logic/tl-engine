/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.ui.form;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.runtime.ActionContext;

/**
 * {@link ModelNamingScheme} for {@link FormMember#getTooltipCaption()}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class FormMemberTooltipCaptionNaming extends
		FormMemberAspectNaming<ResKey, FormMemberTooltipCaptionNaming.FormMemberTooltipCaptionName> {

	/**
	 * The {@link ModelName} of the {@link FormMemberTooltipCaptionNaming}.
	 */
	public interface FormMemberTooltipCaptionName extends FormMemberAspectNaming.FormMemberAspectName {

		// Nothing needed by the type itself.

	}

	/** Creates a {@link FormMemberTooltipCaptionNaming}. */
	public FormMemberTooltipCaptionNaming() {
		super(ResKey.class, FormMemberTooltipCaptionName.class);
	}

	@Override
	public ResKey locateFormMemberAspect(ActionContext context, FormMemberTooltipCaptionName name,
			FormMember formMember) {
		return formMember.getTooltipCaption();
	}

}
