/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.ui.button;

import static com.top_logic.basic.StringServices.isEmpty;
import static com.top_logic.basic.shared.string.StringServicesShared.*;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.runtime.ActionContext;

/**
 * {@link ModelNamingScheme} for {@link CommandModel#getTooltip()} with
 * {@link CommandModel#getLabel()} as fallback.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ButtonTooltipElseLabelNaming
		extends ButtonAspectNaming<String, ButtonTooltipElseLabelNaming.ButtonTooltipElseLabelName> {

	/**
	 * The {@link ModelName} of the {@link ButtonTooltipElseLabelNaming}.
	 */
	public interface ButtonTooltipElseLabelName extends ButtonAspectNaming.ButtonAspectName {

		// Nothing needed by the type itself.

	}

	/** Creates a {@link ButtonTooltipElseLabelNaming}. */
	public ButtonTooltipElseLabelNaming() {
		super(String.class, ButtonTooltipElseLabelName.class);
	}

	@Override
	public String locateButtonAspect(ActionContext context, CommandModel button) {
		ResKey tooltip = button.getTooltip();
		if (isEmpty(tooltip)) {
			return nonNull(context.getDisplayContext().getResources().getString(button.getLabel()));
		}
		return nonNull(context.getDisplayContext().getResources().getString(tooltip));
	}

}
