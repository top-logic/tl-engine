/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.ui.button;

import static com.top_logic.basic.StringServices.*;

import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.runtime.ActionContext;

/**
 * {@link ModelNamingScheme} for {@link CommandModel#getTooltipCaption()}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ButtonTooltipCaptionNaming
		extends ButtonAspectNaming<String, ButtonTooltipCaptionNaming.ButtonTooltipCaptionName> {

	/**
	 * The {@link ModelName} of the {@link ButtonTooltipCaptionNaming}.
	 */
	public interface ButtonTooltipCaptionName extends ButtonAspectNaming.ButtonAspectName {

		// Nothing needed by the type itself.

	}

	/** Creates a {@link ButtonTooltipCaptionNaming}. */
	public ButtonTooltipCaptionNaming() {
		super(String.class, ButtonTooltipCaptionName.class);
	}

	@Override
	public String locateButtonAspect(ActionContext context, CommandModel button) {
		return nonNull(button.getTooltipCaption());
	}

}
