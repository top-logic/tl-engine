/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.ui.button;

import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.runtime.ActionContext;

/**
 * {@link ModelNamingScheme} for {@link CommandModel#isVisible()}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ButtonVisibilityNaming extends ButtonAspectNaming<Boolean, ButtonVisibilityNaming.ButtonVisibilityName> {

	/**
	 * The {@link ModelName} of the {@link ButtonVisibilityNaming}.
	 */
	public interface ButtonVisibilityName extends ButtonAspectNaming.ButtonAspectName {

		// Nothing needed by the type itself.

	}

	/** Creates a {@link ButtonVisibilityNaming}. */
	public ButtonVisibilityNaming() {
		super(Boolean.class, ButtonVisibilityName.class);
	}

	@Override
	public Boolean locateButtonAspect(ActionContext context, CommandModel button) {
		return button.isVisible();
	}

}
