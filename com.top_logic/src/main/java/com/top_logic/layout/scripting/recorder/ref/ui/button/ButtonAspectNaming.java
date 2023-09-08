/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.ui.button;

import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.UnrecordableNamingScheme;
import com.top_logic.layout.scripting.runtime.ActionContext;

/**
 * An {@link UnrecordableNamingScheme} about an aspect of a button, like its label or tooltip.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public abstract class ButtonAspectNaming<M, N extends ButtonAspectNaming.ButtonAspectName>
		extends UnrecordableNamingScheme<M, N> {

	/** The {@link ModelName} of the {@link ButtonAspectNaming}. */
	@Abstract
	public interface ButtonAspectName extends ModelName {

		/** The {@link CommandModel} of the button. */
		ModelName getButton();

		/** @see #getButton() */
		void setButton(ModelName button);

	}

	/** Creates a {@link ButtonAspectNaming}. */
	public ButtonAspectNaming(Class<M> modelClass, Class<N> nameClass) {
		super(modelClass, nameClass);
	}

	@Override
	public final M locateModel(ActionContext context, N name) {
		CommandModel button = (CommandModel) context.resolve(name.getButton());
		return locateButtonAspect(context, button);
	}

	/** Locate the aspect of the given button. */
	protected abstract M locateButtonAspect(ActionContext context, CommandModel button);

}
